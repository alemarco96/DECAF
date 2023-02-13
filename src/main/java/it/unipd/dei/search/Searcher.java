package it.unipd.dei.search;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.query.QueryGenerator;
import it.unipd.dei.rerank.Reranker;


/**
 * The {@code Searcher} interface is used to retrieve a small set of documents that are considered relevant
 * for the query produced by the {@link QueryGenerator}. This set of documents will be later processed by the
 * {@link Reranker}, permuting their order to improve performance measures.
 *
 * @author Marco Alessio
 */
public interface Searcher extends AutoCloseable
{
    /**
     * Retrieve a small set of documents relevant for the query produced by the {@link QueryGenerator}.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     */
    void search(String utteranceId, Conversation conversation);
}
