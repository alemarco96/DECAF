package it.unipd.dei.query;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;

import org.apache.lucene.search.Query;


/**
 * The {@code AbstractSpladeQueryGenerator} abstract class is a {@link QueryGenerator} specific for generating the query
 * used by any {@link Searcher} or {@link Reranker} based on the usage of a SPLADE model. The documents are
 * indexed using the <a href="https://lucene.apache.org/">Lucene</a> library, using the following procedure:
 * <ol>
 *     <li>Each document is fed independently to the SPLADE model, which gives a list of (token, score) pairs.</li>
 *     <li>
 *         For each pair, the score is multiplied by a given constant (such as: 100, 1000), then it is rounded to
 *         the nearest integer number. Any pair with a score less than 1 is discarded.
 *     </li>
 *     <li>
 *         For each (token, integer score) pair, the token is added "integer score" times to the Lucene
 *         representation of the document.
 *     </li>
 * </ol>
 * Before its usage, it must be initialized first by calling the
 * {@link AbstractSpladeQueryGenerator#init(ExternalScriptDriver)} method, providing the {@link ExternalScriptDriver}
 * handling the external Python executable. This operation should be performed inside the
 * {@link Searcher} or {@link Reranker} constructor.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public abstract class AbstractSpladeQueryGenerator implements QueryGenerator
{
    /**
     * The external Python process handle.
     */
    protected ExternalScriptDriver sd;


    /**
     * Create the {@link QueryGenerator}.
     */
    public AbstractSpladeQueryGenerator()
    {
    }


    /**
     * Initialize the current object with all data needed to generate the query. This method should be called inside
     * the {@link Searcher} or {@link Reranker} constructor.
     *
     * @param sd The driver handling the external Python executable.
     * @throws NullPointerException If the provided script driver is null.
     * @throws RuntimeException If an exception has occurred while initializing the query generator.
     */
    public void init(ExternalScriptDriver sd)
    {
        if (sd == null)
            throw new NullPointerException("The provided script driver is null.");

        this.sd = sd;
    }


    /**
     * Generate the query used by any Splade-based {@link Searcher} or {@link Reranker} to perform their job.
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
