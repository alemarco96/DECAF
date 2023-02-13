package it.unipd.dei.rewrite;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;


/**
 * The {@code NoRewriter} class is a dummy {@link Rewriter} that simply copies
 * the original text as the rewritten one.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class NoRewriter implements Rewriter
{
    /**
     * Create the {@link Rewriter}.
     */
    public NoRewriter()
    {
    }


    /**
     * Rewrite the utterance text by copying the original text.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @throws NullPointerException If any of the provided utterance ID or conversation is null.
     * @throws IllegalArgumentException If the provided conversation is empty, the provided utterance
     * can not be found in the conversation, or the provided utterance is not of type
     * {@link Utterance.Type#QUERY}.
     */
    @Override
    public void rewrite(String utteranceId, Conversation conversation)
    {
        if (utteranceId == null)
            throw new NullPointerException("The provided utterance ID is null.");

        if (conversation == null)
            throw new NullPointerException("The provided conversation is null.");

        if (conversation.size() == 0)
            throw new IllegalArgumentException("The provided conversation is empty.");


        final Utterance utterance = conversation.getUtteranceByID(utteranceId);
        if (utterance == null)
        {
            throw new IllegalArgumentException("No utterance with ID \"" + utteranceId +
                    "\" can be found in the conversation.");
        }

        if (utterance.getType() != Utterance.Type.QUERY)
            throw new IllegalArgumentException("The provided utterance is not a query.");

        utterance.setRewrittenContent(utterance.getOriginalContent());
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
