package it.unipd.dei.serialize;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;


/**
 * The {@code CAsT2019} class is a final class used to deserialize the Automatic Topics for
 * <a href="https://www.treccast.ai/">TREC CAsT 2019</a>. It is made available in JSON format, and processed
 * using the <a href="https://mvnrepository.com/artifact/com.google.code.gson/gson">Gson</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class CAsT2019
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
    private CAsT2019()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }
}
