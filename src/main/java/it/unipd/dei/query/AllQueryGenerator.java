package it.unipd.dei.query;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;
import it.unipd.dei.utils.LuceneUtils;
import it.unipd.dei.utils.SpladeUtils;
import org.apache.lucene.search.Query;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The {@code AllQueryGenerator} class is a {@link QueryGenerator} that generate a query considering the rewritten
 * text of all {@link Utterance}s belonging to the current {@link Conversation}, giving equal weight to each of them.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class AllQueryGenerator
{
    /**
     * The BoW-based implementation of the {@link AllQueryGenerator}.
     */
    public static final class BoW extends AbstractBoWQueryGenerator
    {
        /**
         * Create the {@link QueryGenerator}.
         */
        public BoW()
        {
        }


        /**
         * Generate the query used by any BoW-based {@link Searcher} or {@link Reranker} to perform their job.
         *
         * @param utteranceId The ID of the utterance.
         * @param conversation The current conversation.
         * @return The query object.
         * @throws RuntimeException If this object has not been initialized or if an exception has occurred
         * while generating the query.
         */
        @Override
        public Query generate(String utteranceId, Conversation conversation)
        {
            if (qp == null)
                throw new RuntimeException("This object has not been initialized, using the init() method.");

            try
            {
                final List<Map.Entry<String, Double>> query = generateQueryContent(utteranceId, conversation);
                return LuceneUtils.buildQuery(query, qp);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    /**
     * The dense representation-based implementation of the {@link AllQueryGenerator}.
     */
    public static final class Dense extends AbstractDenseQueryGenerator
    {
        /**
         * Create the {@link QueryGenerator}.
         */
        public Dense()
        {
        }


        /**
         * Generate the query used by any dense representation-based {@link Searcher} or
         * {@link Reranker} to perform their job.
         *
         * @param utteranceId  The ID of the utterance.
         * @param conversation The current conversation.
         * @return The query object.
         * @throws RuntimeException If an exception has occurred while generating the query.
         */
        @Override
        public List<Map.Entry<String, Double>> generate(String utteranceId, Conversation conversation)
        {
            try
            {
                return generateQueryContent(utteranceId, conversation);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    /**
     * The SPLADE-based implementation of the {@link AllQueryGenerator}.
     */
    public static final class Splade extends AbstractSpladeQueryGenerator
    {
        /**
         * Create the {@link QueryGenerator}.
         */
        public Splade()
        {
        }


        /**
         * Generate the query used by any Splade-based {@link Searcher} or {@link Reranker} to perform their job.
         *
         * @param utteranceId  The ID of the utterance.
         * @param conversation The current conversation.
         * @return The query object.
         * @throws RuntimeException If this object has not been initialized or if an exception has occurred
         * while generating the query.
         */
        @Override
        public Query generate(String utteranceId, Conversation conversation)
        {
            if (sd == null)
                throw new RuntimeException("This object has not been initialized, using the init() method.");

            try
            {
                final List<Map.Entry<String, Double>> query = generateQueryContent(utteranceId, conversation);
                return SpladeUtils.buildQuery(query, sd);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    // Disable the default constructor.
    private AllQueryGenerator()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Generate the {@link List}&lt;{@link Map.Entry}&lt;{@link String},{@link Double}&gt;&gt; used by all
     * types of query generators to generate the query object.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @throws NullPointerException If any of the provided utterance ID or conversation is null.
     * @throws IllegalArgumentException If the provided conversation is empty.
     * @throws RuntimeException If an exception has occurred while generating the query.
     */
    private static List<Map.Entry<String, Double>> generateQueryContent(String utteranceId, Conversation conversation)
    {
        if (utteranceId == null)
            throw new NullPointerException("The provided utterance ID is null.");

        if (conversation == null)
            throw new NullPointerException("The provided conversation is null.");

        if (conversation.size() == 0)
            throw new IllegalArgumentException("The provided conversation is empty.");

        final List<Map.Entry<String, Double>> queryContent = new ArrayList<>();

        for (Utterance utterance : conversation)
            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(utterance.getRewrittenContent(), 1.0));

        return queryContent;
    }
}
