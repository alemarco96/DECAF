package it.unipd.dei.writer;

import it.unipd.dei.conversation.Conversation;

/**
 * The {@code RunWriter} interface is used to generate a run.
 *
 * @author Marco Alessio
 */
public interface RunWriter extends AutoCloseable
{
    /**
     * Appends the data of the current query to this run.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @param queryId The ID of the query.
     */
    void write(String utteranceId, Conversation conversation, String queryId);
}
