package it.unipd.dei.utils;

import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.index.ParsedDocument;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


/**
 * The {@code SpladeUtils} utility class performs common operations for SPLADE model and Lucene-based
 * components of the framework.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class SpladeUtils
{
    // Disable the default constructor.
    private SpladeUtils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Creates the {@link Query} used by Splade searcher to perform document retrieval.
     *
     * @param queryContent The provided list of document ID-boost pairs.
     * @param sd The driver handling the external Python executable.
     * @throws NullPointerException If any of the provided parameters is null.
     * @return The {@link Query} to be fed to Splade searcher.
     * @throws InterruptedException If an exception occurred while creating the {@link Query}.
     * @throws PythonRuntimeException If an exception occurred while creating the {@link Query}.
     */
    public static Query buildQuery(List<Map.Entry<String, Double>> queryContent, ExternalScriptDriver sd)
            throws InterruptedException
    {
        final BooleanQuery.Builder result = new BooleanQuery.Builder();

        final PrintWriter toIn = sd.getInputStream();
        for (Map.Entry<String, Double> entry : queryContent)
        {
            final String queryText = entry.getKey();
            final double queryWeight = entry.getValue();

            // Write the content of the query to the input stream.
            toIn.println(queryText);
            toIn.flush();

            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully rewritten the utterance. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errRun = sd.waitNextErrorText();
            if (!errRun.isBlank())
                throw new PythonRuntimeException(errRun);


            // Generate the sub-query for the current (query text, query weight) pair.
            final BooleanQuery.Builder qb = new BooleanQuery.Builder();

            // Read the (token, weight) pairs from output stream.
            final int num_pairs = Integer.parseInt(sd.waitNextOutputLine());
            for (int i = 0; i < num_pairs; i++)
            {
                final String token = sd.waitNextOutputLine();
                final double weight = Double.parseDouble(sd.waitNextOutputLine());

                qb.add(new BoostQuery(new TermQuery(new Term(ParsedDocument.CONTENT_FIELD_NAME, token)),
                        (float)weight), BooleanClause.Occur.SHOULD);
            }

            // Add the current sub-query to the final one.
            result.add(new BoostQuery(qb.build(), (float)queryWeight), BooleanClause.Occur.SHOULD);
        }

        return result.build();
    }
}
