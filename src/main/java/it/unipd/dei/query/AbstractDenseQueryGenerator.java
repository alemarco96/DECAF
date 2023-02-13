package it.unipd.dei.query;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;

import java.util.List;
import java.util.Map;


/**
 * The {@code AbstractDenseQueryGenerator} abstract class is a {@link QueryGenerator} specific for generating
 * the query used by any {@link Searcher} or {@link Reranker} based on dense representation, employing the
 * <a href="https://huggingface.co/">Transformers</a> and possibly <a href="https://faiss.ai/">Faiss</a>) libraries.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public abstract class AbstractDenseQueryGenerator implements QueryGenerator
{
    /**
     * Create the {@link QueryGenerator}.
     */
    public AbstractDenseQueryGenerator()
    {
    }


    /**
     * Generate the query used by any dense representation-based {@link Searcher} or
     * {@link Reranker} to perform their job.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @return The query object.
     */
    public abstract List<Map.Entry<String, Double>> generate(String utteranceId, Conversation conversation);


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
