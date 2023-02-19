package it.unipd.dei.index;

import it.unipd.dei.corpus.CorpusParser;
import it.unipd.dei.pipeline.IndexTimingInfo;

import java.util.Iterator;
import java.util.Map;


/**
 * The {@code Indexer} interface is used to index some documents, whose data is given by a {@link CorpusParser}.
 * The {@link Indexer#init(CorpusParser)} method must be called immediately after the constructor has been called.
 * This class has been designed around the idea of indexing the documents in chunks, but any subclass is free to
 * perform their operations in one go if it wishes to do so.
 * The {@link Indexer#next()} method is called to index each chunk, and returns a pair containing the number of
 * documents indexed and an {@link IndexTimingInfo} object with timing information for the current chunk.
 * The calling code is free to show the progress of the indexing phase between each interaction of the indexing loop.
 *
 * @author Marco Alessio
 */
public interface Indexer extends Iterable<Integer>, Iterator<Integer>, AutoCloseable
        //Iterator<Map.Entry<Integer, IndexTimingInfo>>, Iterable<Map.Entry<Integer, IndexTimingInfo>>, AutoCloseable
{
    /**
     * Initialize the current object with the corpus parser needed to index the data. This method should
     * be called before attempting to perform the index phase.
     *
     @param parser The corpus parser that feeds this objects with documents.
     */
    void init(CorpusParser parser);
}
