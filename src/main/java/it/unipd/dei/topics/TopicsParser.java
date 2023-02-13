package it.unipd.dei.topics;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;


/**
 * The {@code TopicsParser} interface is used to feed the search pipeline with new
 * {@link Conversation}s to process.
 *
 * @author Marco Alessio
 */
public interface TopicsParser extends AutoCloseable
{
    /**
     * Check if a new {@link Conversation} has not already been processed.
     *
     * @return {@code true} if a new conversation has not already processed, otherwise {@code false}.
     */
    boolean hasNextConversation();


    /**
     * Retrieve the next {@link Conversation} that has not already been processed.
     *
     * @return The next conversation.
     */
    Iterable<Utterance> nextConversation();
}
