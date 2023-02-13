package it.unipd.dei.rerank;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.search.Searcher;


/**
 * The {@code Reranker} interface is used to rerank the small set of documents initially retrieved by the
 * {@link Searcher}, changing their order to improve evaluation metrics.
 *
 * @author Marco Alessio
 */
public interface Reranker extends AutoCloseable
{
    /**
     * Rerank the small set of documents retrieved by the {@link Searcher}.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     */
    void rerank(String utteranceId, Conversation conversation);
}
