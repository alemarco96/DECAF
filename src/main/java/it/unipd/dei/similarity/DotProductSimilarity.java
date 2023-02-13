package it.unipd.dei.similarity;

import org.apache.lucene.search.similarities.ClassicSimilarity;


/**
 * Dot product similarity between the term frequency and the query term boost.
 * The intended use is in conjunction with SPLADE searcher.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class DotProductSimilarity extends ClassicSimilarity
{
    /**
     * Create the {@link org.apache.lucene.search.similarities.Similarity}.
     */
    public DotProductSimilarity()
    {
    }


    /**
     * Returns the frequency of the current term within a document (same as the freq parameter).
     *
     * @param freq The frequency of the term within a document.
     * @return The frequency of the term within a document.
     */
    @Override
    public float tf(float freq)
    {
        return Math.max(freq, 0.0f);
    }

    /**
     * Returns the inverse term frequency factor, which is always 1.0.
     *
     * @param docFreq The number of documents in which the term appears.
     * @param docCount The total number of documents in the collection.
     * @return 1.0
     */
    @Override
    public float idf(long docFreq, long docCount)
    {
        return 1.0f;
    }

    /**
     * Returns the document length normalization factor, which is always 1.0.
     *
     * @param numTerms The number of terms in the current document.
     * @return 1.0
     */
    @Override
    public float lengthNorm(int numTerms)
    {
        return 1.0f;
    }
}
