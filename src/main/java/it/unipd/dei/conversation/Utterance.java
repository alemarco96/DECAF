package it.unipd.dei.conversation;

import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.rewrite.Rewriter;
import it.unipd.dei.search.Searcher;
import it.unipd.dei.writer.RunWriter;

import java.util.Map;


/**
 * The {@code Utterance} class is a container for all data belonging to the same utterance. The main kind of
 * information stored in this object are:
 * <ul>
 *     <li>The unique (at least w.r.t the {@link Conversation} where it is inserted) ID of the utterance.</li>
 *     <li>The {@link Type}: how the framework must act when processing the utterance.</li>
 *     <li>The {@link Source}: what entity has created the utterance.</li>
 *     <li>The original content: the original textual representation of the utterance.</li>
 *     <li>
 *         The rewritten content: for "query" utterances, the textual representation of the de-contextualized query
 *         produced by a {@link Rewriter}.
 *     </li>
 *     <li>
 *         The documents mapping: for "query" utterances, a mapping between each document ID and an integer index
 *         of the document inside the index used by a {@link Searcher} to perform the initial retrieval phase.
 *     </li>
 *     <li>
 *         The documents text: for "query" utterances, a mapping between each document ID and the textual content
 *         of the document. This data may be later consumed by a {@link Reranker} to perform its job.
 *     </li>
 *     <li>
 *         The initial ranking: for "query" utterances, a mapping between each document ID and the floating-point score
 *         produced by a {@link Searcher} when performing the initial retrieval phase.
 *         This data will be later consumed by a {@link Reranker} to compute a new score
 *         for each document, in such a way to permute their order to increase performance metrics.
 *     </li>
 *     <li>
 *         The reranked ranking: for "query" utterances, a mapping between each document ID and the floating-point score
 *         produced by a {@link Reranker} when performing the second reranking phase.
 *         This data is built by processing all documents inside the initial ranking mapping and then applying a
 *         possibly computationally expensive procedure to find a new score for every one of them. Such procedure
 *         enables to find a permutation of the documents that will (hopefully) increase performance metrics.
 *         This field will be later read by a {@link RunWriter} to perform its operation.
 *     </li>
 * </ul>
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class Utterance
{
    /**
     * Enumeration for all utterance types allowed in the framework.
     */
    public enum Type
    {
        /**
         * Utterance of type "query": the system must process it and compute a ranked list of documents as a result.
         */
        QUERY,

        /**
         * Utterance of type "response": the utterance stores the response given to a "query" utterance,
         * and the system does not have to compute anything for this kind of utterance.
         */
        RESPONSE
    }


    /**
     * Enumeration for all utterance sources allowed in the framework.
     */
    public enum Source
    {
        /**
         * Source "user": the utterance has been produced by the user of the system.
         */
        USER,

        /**
         * Source "system": the utterance has been produced by this framework.
         */
        SYSTEM
    }


    private final String id;
    private final Type type;
    private final Source source;

    private final String originalContent;
    private String rewrittenContent;


    private Map<String, Integer> documentsMapping;
    private Map<String, String> documentsText;


    private Map<String, Double> initialRanking;
    private Map<String, Double> rerankedRanking;


    /**
     * Create this utterance with the specified ID, type, source and original content. Additional data can be
     * stored by calling the setXXX() methods of this class.
     *
     * @param id The ID of the utterance.
     * @param type The type of the utterance.
     * @param source The source of the utterance.
     * @param originalContent The original content of the utterance.
     * @throws NullPointerException If any of the provided parameters is null.
     */
    public Utterance(String id, Type type, Source source, String originalContent)
    {
        if (id == null)
            throw new NullPointerException("The provided ID is null.");

        if (type == null)
            throw new NullPointerException("The provided type is null.");

        if (source == null)
            throw new NullPointerException("The provided source is null.");

        if (originalContent == null)
            throw new NullPointerException("The provided original content is null.");

        this.id = id;
        this.type = type;
        this.source = source;
        this.originalContent = originalContent;
    }


    /**
     * Return the ID of this utterance.
     *
     * @return The ID of this utterance.
     */
    public String getID()
    {
        return id;
    }


    /**
     * Return the type of this utterance.
     *
     * @return The type of this utterance.
     */
    public Type getType()
    {
        return type;
    }


    /**
     * Return the source of this utterance.
     *
     * @return The source of this utterance.
     */
    public Source getSource()
    {
        return source;
    }


    /**
     * Return the original content of this utterance.
     *
     * @return The original content of this utterance.
     */
    public String getOriginalContent()
    {
        return originalContent;
    }


    /**
     * Return the rewritten content of this utterance. Note that the return value can be {@code null}, if
     * the corresponding method {@link Utterance#setRewrittenContent(String)} has not been called.
     *
     * @return The rewritten content of this utterance, if set, otherwise {@code null}.
     */
    public String getRewrittenContent()
    {
        return rewrittenContent;
    }


    /**
     * Return the documents mapping of this utterance. Note that the return value can be {@code null}, if
     * the corresponding method {@link Utterance#setDocumentsMapping(Map)} has not been called.
     *
     * @return The documents mapping of this utterance, if set, otherwise {@code null}.
     */
    public Map<String, Integer> getDocumentsMapping()
    {
        return documentsMapping;
    }


    /**
     * Return the documents text of this utterance. Note that the return value can be {@code null}, if
     * the corresponding method {@link Utterance#setDocumentsMapping(Map)} has not been called.
     *
     * @return The documents text of this utterance, if set, otherwise {@code null}.
     */
    public Map<String, String> getDocumentsText()
    {
        return documentsText;
    }


    /**
     * Return the initial ranking of this utterance. Note that the return value can be {@code null}, if
     * the corresponding method {@link Utterance#setInitialRankings(Map)} has not been called.
     *
     * @return The initial ranking of this utterance, if set, otherwise {@code null}.
     */
    public Map<String, Double> getInitialRanking()
    {
        return initialRanking;
    }


    /**
     * Return the reranked ranking of this utterance. Note that the return value can be {@code null}, if
     * the corresponding method {@link Utterance#setRerankedRankings(Map)} has not been called.
     *
     * @return The reranked ranking of this utterance, if set, otherwise {@code null}.
     */
    public Map<String, Double> getRerankedRanking()
    {
        return rerankedRanking;
    }


    /**
     * Set the rewritten content to the value passed as parameter. Note that {@code null} is a valid value for it.
     *
     * @param content The new value of the rewritten content.
     */
    public void setRewrittenContent(String content)
    {
        rewrittenContent = content;
    }


    /**
     * Set the documents mapping to the value passed as parameter. Note that {@code null} is a valid value for it.
     *
     * @param mapping The new value of the documents mapping.
     */
    public void setDocumentsMapping(Map<String, Integer> mapping)
    {
        documentsMapping = mapping;
    }


    /**
     * Set the documents text to the value passed as parameter. Note that {@code null} is a valid value for it.
     *
     * @param documentsText The new value of the documents text.
     */
    public void setDocumentsText(Map<String, String> documentsText)
    {
        this.documentsText = documentsText;
    }


    /**
     * Set the initial ranking to the value passed as parameter. Note that {@code null} is a valid value for it.
     *
     * @param ranking The new value of the initial ranking.
     */
    public void setInitialRankings(Map<String, Double> ranking)
    {
        initialRanking = ranking;
    }


    /**
     * Set the reranked ranking to the value passed as parameter. Note that {@code null} is a valid value for it.
     *
     * @param ranking The new value of the reranked ranking.
     */
    public void setRerankedRankings(Map<String, Double> ranking)
    {
        rerankedRanking = ranking;
    }
}
