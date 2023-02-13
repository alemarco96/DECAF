package it.unipd.dei.query;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;


/**
 * The {@code AbstractBoWQueryGenerator} abstract class is a {@link QueryGenerator} specific for generating
 * the query used by any {@link Searcher} or {@link Reranker}
 * based on <a href="https://lucene.apache.org/">Lucene</a> information-retrieval library.
 * Before its usage, it must be initialized first by calling the {@link AbstractBoWQueryGenerator#init(QueryParser)}
 * method. This operation should be performed inside the {@link Searcher} or {@link Reranker} constructor.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public abstract class AbstractBoWQueryGenerator implements QueryGenerator
{
    /**
     * The query parser used to create the query.
     */
    protected QueryParser qp;


    /**
     * Create the {@link QueryGenerator}.
     */
    public AbstractBoWQueryGenerator()
    {
    }


    /**
     * Initialize the current object with all data needed to generate the query. This method should be called inside
     * the {@link Searcher} or {@link Reranker} constructor.
     *
     * @param qp The BoW query parser.
     * @throws NullPointerException If the provided analyzer is null.
     */
    public void init(QueryParser qp)
    {
        if (qp == null)
            throw new NullPointerException("The provided BoW query parser is null.");

        this.qp = qp;
    }


    /**
     * Generate the query used by any BoW-based {@link Searcher} or {@link Reranker} to perform their job.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @return The query object.
     */
    public abstract Query generate(String utteranceId, Conversation conversation);


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
