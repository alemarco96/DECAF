package it.unipd.dei.utils;

import it.unipd.dei.index.ParsedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.util.*;


/**
 * The {@code LuceneUtils} utility class performs common operations for Lucene-based components of the framework.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class LuceneUtils
{
    // Disable the default constructor.
    private LuceneUtils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Tokenize the input text using the provided {@code Analyzer}, and return the {@code List} of tokens.
     *
     * @param text The input text to tokenize.
     * @param analyzer The {@code Analyzer} to use.
     * @throws NullPointerException If any of the provided text or analyzer is null.
     * @return The {@code List} of tokens.
     */
    public static List<String> tokenizeText(String text, Analyzer analyzer)
    {
        if (text == null)
            throw new NullPointerException("The provided text is null.");

        if (analyzer == null)
            throw new NullPointerException("The provided analyzer is null.");

        try
        {
            final List<String> result = new ArrayList<>();

            final TokenStream stream = analyzer.tokenStream(ParsedDocument.CONTENT_FIELD_NAME, text);
            stream.reset();

            while (stream.incrementToken())
            {
                final CharTermAttribute attr = stream.getAttribute(CharTermAttribute.class);

                result.add(attr.toString());
            }

            stream.end();
            stream.close();

            return result;
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing utility function.\n", th);
        }
    }


    /**
     * Creates the {@link Query} used by Lucene searcher to perform document retrieval.
     *
     * @param queryContent The provided list of document ID-boost pairs.
     * @param qp The provided query parser.
     * @throws NullPointerException If any of the provided parameters is null.
     * @return The {@link Query} to be fed to Lucene searcher.
     * @throws ParseException If an exception occurred while creating the {@link Query}.
     */
    public static Query buildQuery(List<Map.Entry<String, Double>> queryContent, QueryParser qp) throws ParseException
    {
        if (queryContent == null)
            throw new NullPointerException("The provided query content is null.");

        if (qp == null)
            throw new NullPointerException("The provided query parser is null.");

        final BooleanQuery.Builder result = new BooleanQuery.Builder();

        for (Map.Entry<String, Double> entry : queryContent)
        {
            final String query = entry.getKey();
            final double weight = entry.getValue();

            if ((!Double.isFinite(weight)) || (weight <= 0.0))
            {
                result.add(new BoostQuery(qp.parse(QueryParser.escape(query)), 0.0f),
                        BooleanClause.Occur.SHOULD);
            }
            else
            {
                result.add(new BoostQuery(qp.parse(QueryParser.escape(query)), (float)weight),
                        BooleanClause.Occur.SHOULD);
            }
        }

        return result.build();
    }
}
