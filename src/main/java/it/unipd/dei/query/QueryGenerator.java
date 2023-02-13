package it.unipd.dei.query;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;


/**
 * The {@code QueryGenerator} interface is used to generate the query used by the
 * {@link Searcher} or {@link Reranker} to perform their job.
 *
 * @author Marco Alessio
 */
public interface QueryGenerator extends AutoCloseable
{
    /**
     * Generate the query used by the {@link Searcher} or {@link Reranker} to perform their job.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @return The query object.
     */
    Object generate(String utteranceId, Conversation conversation);
}
