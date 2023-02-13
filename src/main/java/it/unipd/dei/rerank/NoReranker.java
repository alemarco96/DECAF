package it.unipd.dei.rerank;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;

import java.util.HashMap;
import java.util.Map;


/**
 * The {@code NoReranker} class is a dummy {@link Reranker} that simply copies
 * the original ranking as the reranked one.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class NoReranker implements Reranker
{
    /**
     * Create the {@link Reranker}.
     */
    public NoReranker()
    {
    }


    /**
     * Rerank the documents by copying the original ranking as the reranked one.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     */
    @Override
    public void rerank(String utteranceId, Conversation conversation)
    {
        final Utterance utterance = conversation.getUtteranceByID(utteranceId);

        final Map<String, Double> rankings = utterance.getInitialRanking();
        utterance.setRerankedRankings(new HashMap<>(rankings));
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close() throws Exception
    {
    }
}
