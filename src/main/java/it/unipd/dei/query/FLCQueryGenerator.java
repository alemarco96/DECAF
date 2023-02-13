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
 * The {@code FLCQueryGenerator} class is a {@link QueryGenerator} that generate a query considering only the First,
 * Last and Current {@link Utterance}s of the current {@link Conversation}, each weighted with three different weights.
 * Special care is given for the situations when the conversation has size 1 or 2.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class FLCQueryGenerator
{
    /**
     * The BoW-based implementation of the {@link FLCQueryGenerator}.
     */
    public static final class BoW extends AbstractBoWQueryGenerator
    {
        private final double qC1;

        private final double qF2;
        private final double qC2;

        private final double qF3;
        private final double qL3;
        private final double qC3;


        /**
         * Create the {@link QueryGenerator}.
         *
         * @param qC1 The boost weight for the Current utterance rewritten text, when the conversation has size 1.
         * @param qF2 The boost weight for the First utterance rewritten text, when the conversation has size 2.
         * @param qC2 The boost weight for the Current utterance rewritten text, when the conversation has size 2.
         * @param qF3 The boost weight for the First utterance rewritten text, when the conversation has size 3.
         * @param qL3 The boost weight for the Last utterance rewritten text, when the conversation has size 3.
         * @param qC3 The boost weight for the Current utterance rewritten text, when the conversation has size 3.
         * @throws IllegalArgumentException If any of the provided parameters is not a finite positive number.
         */
        public BoW(double qC1, double qF2, double qC2, double qF3, double qL3, double qC3)
        {
            checkParameters(qC1, qF2, qC2, qF3, qL3, qC3);

            this.qC1 = qC1;
            this.qF2 = qF2;
            this.qC2 = qC2;
            this.qF3 = qF3;
            this.qL3 = qL3;
            this.qC3 = qC3;
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
                final List<Map.Entry<String, Double>> query = generateQueryContent(utteranceId, conversation,
                        qC1, qF2, qC2, qF3, qL3, qC3);
                return LuceneUtils.buildQuery(query, qp);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    /**
     * The dense representation-based implementation of the {@link FLCQueryGenerator}.
     */
    public static final class Dense extends AbstractDenseQueryGenerator
    {
        private final double qC1;

        private final double qF2;
        private final double qC2;

        private final double qF3;
        private final double qL3;
        private final double qC3;


        /**
         * Create the {@link QueryGenerator}.
         *
         * @param qC1 The boost weight for the Current utterance rewritten text, when the conversation has size 1.
         * @param qF2 The boost weight for the First utterance rewritten text, when the conversation has size 2.
         * @param qC2 The boost weight for the Current utterance rewritten text, when the conversation has size 2.
         * @param qF3 The boost weight for the First utterance rewritten text, when the conversation has size 3.
         * @param qL3 The boost weight for the Last utterance rewritten text, when the conversation has size 3.
         * @param qC3 The boost weight for the Current utterance rewritten text, when the conversation has size 3.
         * @throws IllegalArgumentException If any of the provided parameters is not a finite positive number.
         */
        public Dense(double qC1, double qF2, double qC2, double qF3, double qL3, double qC3)
        {
            checkParameters(qC1, qF2, qC2, qF3, qL3, qC3);

            this.qC1 = qC1;
            this.qF2 = qF2;
            this.qC2 = qC2;
            this.qF3 = qF3;
            this.qL3 = qL3;
            this.qC3 = qC3;
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
                return generateQueryContent(utteranceId, conversation, qC1, qF2, qC2, qF3, qL3, qC3);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    /**
     * The SPLADE-based implementation of the {@link FLCQueryGenerator}.
     */
    public static final class Splade extends AbstractSpladeQueryGenerator
    {
        private final double qC1;

        private final double qF2;
        private final double qC2;

        private final double qF3;
        private final double qL3;
        private final double qC3;


        /**
         * Create the {@link QueryGenerator}.
         *
         * @param qC1 The boost weight for the Current utterance rewritten text, when the conversation has size 1.
         * @param qF2 The boost weight for the First utterance rewritten text, when the conversation has size 2.
         * @param qC2 The boost weight for the Current utterance rewritten text, when the conversation has size 2.
         * @param qF3 The boost weight for the First utterance rewritten text, when the conversation has size 3.
         * @param qL3 The boost weight for the Last utterance rewritten text, when the conversation has size 3.
         * @param qC3 The boost weight for the Current utterance rewritten text, when the conversation has size 3.
         * @throws IllegalArgumentException If any of the provided parameters is not a finite positive number.
         */
        public Splade(double qC1, double qF2, double qC2, double qF3, double qL3, double qC3)
        {
            checkParameters(qC1, qF2, qC2, qF3, qL3, qC3);

            this.qC1 = qC1;
            this.qF2 = qF2;
            this.qC2 = qC2;
            this.qF3 = qF3;
            this.qL3 = qL3;
            this.qC3 = qC3;
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
                final List<Map.Entry<String, Double>> query = generateQueryContent(utteranceId, conversation,
                        qC1, qF2, qC2, qF3, qL3, qC3);
                return SpladeUtils.buildQuery(query, sd);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("An exception has occurred while generating the query.\n", th);
            }
        }
    }


    /**
     * Check if all boost weight parameters are valid.
     *
     * @param qC1 The boost weight for the Current utterance rewritten text, when the conversation has size 1.
     * @param qF2 The boost weight for the First utterance rewritten text, when the conversation has size 2.
     * @param qC2 The boost weight for the Current utterance rewritten text, when the conversation has size 2.
     * @param qF3 The boost weight for the First utterance rewritten text, when the conversation has size 3.
     * @param qL3 The boost weight for the Last utterance rewritten text, when the conversation has size 3.
     * @param qC3 The boost weight for the Current utterance rewritten text, when the conversation has size 3.
     * @throws IllegalArgumentException If any of the provided parameters is not a finite positive number.
     */
    private static void checkParameters(double qC1, double qF2, double qC2, double qF3, double qL3, double qC3)
    {
        if ((!Double.isFinite(qC1)) || (qC1 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qC1 (%f) " +
                    "is not a valid finite positive number.", qC1));
        }

        if ((!Double.isFinite(qF2)) || (qF2 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qF2 (%f) " +
                    "is not a valid finite positive number.", qF2));
        }

        if ((!Double.isFinite(qC2)) || (qC2 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qC2 (%f) " +
                    "is not a valid finite positive number.", qC2));
        }

        if ((!Double.isFinite(qF3)) || (qF3 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qF3 (%f) " +
                    "is not a valid finite positive number.", qF3));
        }

        if ((!Double.isFinite(qL3)) || (qL3 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qL3 (%f) " +
                    "is not a valid finite positive number.", qL3));
        }

        if ((!Double.isFinite(qC3)) || (qC3 < 0.0))
        {
            throw new IllegalArgumentException(String.format("The provided qC3 (%f) " +
                    "is not a valid finite positive number.", qC3));
        }
    }


    // Disable the default constructor.
    private FLCQueryGenerator()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Generate the {@link List}&lt;{@link Map.Entry}&lt;{@link String},{@link Double}&gt;&gt; used by all
     * types of query generators to generate the query object.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @param qC1 The boost weight for the Current utterance rewritten text, when the conversation has size 1.
     * @param qF2 The boost weight for the First utterance rewritten text, when the conversation has size 2.
     * @param qC2 The boost weight for the Current utterance rewritten text, when the conversation has size 2.
     * @param qF3 The boost weight for the First utterance rewritten text, when the conversation has size 3.
     * @param qL3 The boost weight for the Last utterance rewritten text, when the conversation has size 3.
     * @param qC3 The boost weight for the Current utterance rewritten text, when the conversation has size 3.
     * @throws NullPointerException If any of the provided utterance ID or conversation is null.
     * @throws IllegalArgumentException If the provided conversation is empty.
     * @throws RuntimeException If an exception has occurred while generating the query.
     */
    private static List<Map.Entry<String, Double>> generateQueryContent(String utteranceId, Conversation conversation,
            double qC1, double qF2, double qC2, double qF3, double qL3, double qC3)
    {
        // Note that all boost weight parameters have already been checked in the checkParameters() method.

        if (utteranceId == null)
            throw new NullPointerException("The provided utterance ID is null.");

        if (conversation == null)
            throw new NullPointerException("The provided conversation is null.");

        if (conversation.size() == 0)
            throw new IllegalArgumentException("The provided conversation is empty.");

        // Filter the conversation to extract only the query utterances from the user.
        final List<Utterance> queryUtterances = new ArrayList<>();
        for (Utterance utterance : conversation)
        {
            if ((utterance.getType() == Utterance.Type.QUERY) && (utterance.getSource() == Utterance.Source.USER))
                queryUtterances.add(utterance);
        }

        if (queryUtterances.size() == 0)
            throw new IllegalArgumentException("The provided conversation does not contain user questions.");


        final List<Map.Entry<String, Double>> queryContent = new ArrayList<>();

        if (queryUtterances.size() == 1)
        {
            final Utterance cUtterance = queryUtterances.get(0);

            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(cUtterance.getRewrittenContent(), qC1));
        }
        else if (queryUtterances.size() == 2)
        {
            final Utterance fUtterance = queryUtterances.get(0);
            final Utterance cUtterance = queryUtterances.get(1);

            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(fUtterance.getRewrittenContent(), qF2));
            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(cUtterance.getRewrittenContent(), qC2));
        }
        else
        {
            final Utterance fUtterance = queryUtterances.get(0);
            final Utterance lUtterance = queryUtterances.get(queryUtterances.size() - 2);
            final Utterance cUtterance = queryUtterances.get(queryUtterances.size() - 1);

            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(fUtterance.getRewrittenContent(), qF3));
            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(lUtterance.getRewrittenContent(), qL3));
            queryContent.add(new AbstractMap.SimpleImmutableEntry<>(cUtterance.getRewrittenContent(), qC3));
        }

        return queryContent;
    }
}
