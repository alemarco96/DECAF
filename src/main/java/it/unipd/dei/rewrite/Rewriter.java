package it.unipd.dei.rewrite;

import it.unipd.dei.conversation.Conversation;


/**
 * The {@code Rewriter} interface is used to rewrite the utterance text. Its main purpose is to
 * being applied to query utterances for generating de-contextualized query text.
 *
 * @author Marco Alessio
 */
public interface Rewriter extends AutoCloseable
{
    /**
     * Rewrites the utterance text.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     */

    void rewrite(String utteranceId, Conversation conversation);
}
