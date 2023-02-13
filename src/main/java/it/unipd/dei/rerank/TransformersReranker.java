package it.unipd.dei.rerank;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.fusion.RunFusion;
import it.unipd.dei.query.AbstractDenseQueryGenerator;
import it.unipd.dei.query.QueryGenerator;
import it.unipd.dei.search.Searcher;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The {@code TransformersReranker} class is a {@link Reranker} that reranks the small set of documents
 * retrieved by the {@link Searcher}, using a computationally expensive
 * machine-learning model based on the <a href="https://huggingface.co/">Transformers</a>
 * Python library. Hopefully this component succeeds to sort the documents in such a way
 * to improve the performance measures.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class TransformersReranker implements Reranker
{
    private final AbstractDenseQueryGenerator queryGenerator;
    private final RunFusion runFusion;
    private final ExternalScriptDriver sd;


    /**
     * Create the {@link Reranker}.
     *
     * @param queryGenerator The {@link QueryGenerator} used.
     * @param runFusion The {@link RunFusion} to apply between the initial ranking and the one produced by the
     *               machine-learning model.
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the reranking model to use.
     * @param vectorSize The number of elements in the embedding produced by the model.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param similarity The similarity function to use to compare queries and documents. Accepted values are:
     *                   <ul>
     *                      <li>\"cos\": Cosine similarity</li>
     *                      <li>\"dot\": Dot product</li>
     *                      <li>\"l2\": Euclidean distance</li>
     *                      <li>\"l2sq\": Squared Euclidean distance</li>
     *                   </ul>
     * @throws NullPointerException If any of the provided Lucene index directory, query generator, run fusion,
     * Python executable or script filename, Python reranking model or similarity is null.
     * @throws IllegalArgumentException If any of the vector size or the maximum number of tokens is not
     * a positive integer number, or the similarity function is unknown.
     * @throws RuntimeException If an exception has occurred while creating the reranker.
     */
    public TransformersReranker(AbstractDenseQueryGenerator queryGenerator, RunFusion runFusion,
                                String pythonFilename, String workingDirectory, String scriptFilename,
                                String model, int vectorSize, int maxTokens, String similarity)
    {
        if (queryGenerator == null)
            throw new NullPointerException("The provided query generator is null.");

        if (runFusion == null)
            throw new NullPointerException("The provided run fusion is null.");

        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided Python script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided Transformers model is null.");

        if (vectorSize < 1)
        {
            throw new IllegalArgumentException("The provided vector size (" + vectorSize + ") must be " +
                    "a positive integer number.");
        }

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided maximum number of tokens (" + maxTokens + ") must be " +
                    "a positive integer number.");
        }

        if (similarity == null)
            throw new NullPointerException("The provided similarity function is null.");

        if (!(similarity.equals("cos") || similarity.equals("dot") || similarity.equals("l2") ||
                similarity.equals("l2sq")))
        {
            throw new IllegalArgumentException("The provided similarity function (" + similarity + ") is unknown. " +
                    "The available options are: \"cos\", \"dot\", \"l2\" and \"l2sq\".");
        }


        try
        {
            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--model=%s", model),
                    String.format("--vector_size=%d", vectorSize),
                    String.format("--max_tokens=%d", maxTokens),
                    String.format("--similarity=%s", similarity));

            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully loaded the predictor. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errInit = sd.waitNextErrorText();
            if (!errInit.isBlank())
                throw new PythonRuntimeException(errInit);


            this.queryGenerator = queryGenerator;
            this.runFusion = runFusion;
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the reranker.\n", th);
        }
    }


    /**
     * Rerank the documents by using the Transformers-based machine-learning model.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @throws RuntimeException If an exception has occurred while performing reranking.
     */
    @Override
    public void rerank(String utteranceId, Conversation conversation)
    {
        final Utterance utterance = conversation.getUtteranceByID(utteranceId);
        final List<Map.Entry<String, String>> documentsText = new ArrayList<>(utterance.getDocumentsText().entrySet());
        final Map<String, Double> initialRanking = utterance.getInitialRanking();
        final Map<String, Double> rerankedRanking = new HashMap<>();

        try
        {
            // Generate the query.
            final List<Map.Entry<String, Double>> query = queryGenerator.generate(utteranceId, conversation);

            final PrintWriter toIn = sd.getInputStream();

            // Write the content of the query to the input stream.
            toIn.println(query.size());
            for (Map.Entry<String, Double> entry : query)
            {
                final String query_text = entry.getKey();
                final double query_weight = entry.getValue();

                toIn.println(query_text);
                toIn.println(query_weight);
            }

            // Write the number of documents for this ranking to the input stream.
            toIn.println(initialRanking.size());

            // Flush the input stream, so Python code can start computing the query embedding.
            toIn.flush();

            // For each entry of the ranking, write the document text to the input stream.
            for (Map.Entry<String, String> entry : documentsText)
            {
                final String text = entry.getValue();

                // Write the document content to the input stream.
                toIn.println(text);
            }

            // Flush the input stream, so Python code can start computing the documents embedding.
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


            // Read the score of each document from output stream, and save into reranked ranking.
            for (Map.Entry<String, String> entry : documentsText)
            {
                final String id = entry.getKey();
                final double rerankedScore = Double.parseDouble(sd.waitNextOutputLine());

                // Save the (document id, reranked score) pair to the reranked ranking.
                rerankedRanking.put(id, rerankedScore);
            }

            // Perform run fusion between the initial and reranked rankings, storing the result in the reranked ranking.
            final Map<String, Double> finalRanking = runFusion.merge(initialRanking, rerankedRanking);
            utterance.setRerankedRankings(finalRanking);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while performing reranking.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
        if (sd != null)
        {
            try
            {
                sd.getInputStream().close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                sd.getOutputStream().close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                sd.getErrorStream().close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                sd.close();
            }
            catch (Throwable ignored)
            {
            }
        }
    }
}
