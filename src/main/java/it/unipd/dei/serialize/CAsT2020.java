package it.unipd.dei.serialize;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;


/**
 * The {@code CAsT2020} class is a final class used to deserialize all Topics for
 * <a href="https://www.treccast.ai/">TREC CAsT 2020</a>. They are made available in JSON format, and processed
 * using the <a href="https://mvnrepository.com/artifact/com.google.code.gson/gson">Gson</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class CAsT2020
{
    /**
     * Class representing a topic ({@link Conversation}).
     */
    public static final class Topic
    {
        /**
         * Class representing each turn ({@link Utterance}) that composes a topic ({@link Conversation}).
         */
        public static final class Turn
        {
            /**
             * The "number" field inside a turn.
             */
            public int number;

            /**
             * The "raw_utterance" field inside a turn.
             */
            public String raw_utterance;

            /**
             * The "automatic_rewritten_utterance" field inside a turn.
             */
            public String automatic_rewritten_utterance;

            /**
             * The "manual_rewritten_utterance" field inside a turn.
             */
            public String manual_rewritten_utterance;

            /**
             * The "automatic_canonical_result_id" field inside a turn.
             */
            public String automatic_canonical_result_id;

            /**
             * The "manual_canonical_result_id" field inside a turn.
             */
            public String manual_canonical_result_id;

            /**
             * The "query_turn_dependence" field inside a turn.
             */
            public int[] query_turn_dependence;


            /**
             * Create this object.
             */
            public Turn()
            {
            }
        }

        /**
         * The "number" field inside a topic.
         */
        public int number;

        /**
         * The "title" field inside a topic.
         */
        public String title;

        /**
         * The "description" field inside a topic.
         */
        public String description;

        /**
         * The "turn" field inside a topic.
         */
        public Turn[] turn;


        /**
         * Create this object.
         */
        public Topic()
        {
        }
    }


    // Disable the default constructor.
    private CAsT2020()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }
}
