package it.unipd.dei.corpus;

import it.unipd.dei.index.ParsedDocument;

import java.util.Iterator;
import java.util.List;


/**
 * The {@code MultiCorpusParser} is a {@link CorpusParser} used to combine multiple instances in a single
 * one. Its intended usage is to allow to reuse existing corpus parsers, each developed for a specific corpus,
 * when indexing multiple corpora at once (example: for indexing TREC-CAsT 2019 and 2020 data, the
 * {@link MSMARCOv1CorpusParser} can be used for MS-MARCOv1, while {@link TRECCARv2CorpusParser} for TREC-CARv2).
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class MultiCorpusParser implements CorpusParser
{
    private final List<CorpusParser> corporaParser;
    private int corpusIndex;
    private CorpusParser parser;


    /**
     * Create the {@link CorpusParser}, combining multiple instances in a single one.
     *
     * @param corporaParser The list of {@link CorpusParser}s.
     */
    public MultiCorpusParser(List<CorpusParser> corporaParser)
    {
        if (corporaParser == null)
            throw new NullPointerException("The provided corpora parser list is null.");

        if (corporaParser.size() == 0)
            throw new IllegalArgumentException("The provided corpora parser list is empty.");


        try
        {
            this.corporaParser = corporaParser;
            this.corpusIndex = -1;
            this.parser = null;
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while creating the corpus parser.\n", th);
        }
    }


    /**
     * Return an {@link Iterator} of {@link ParsedDocument}.
     *
     * @return An {@link Iterator} of {@link ParsedDocument} view of this object.
     */
    @Override
    public Iterator<ParsedDocument> iterator()
    {
        return this;
    }


    /**
     * Check if there is a new document to read from the corpus.
     *
     * @return {@code true} if there is a new document to read, otherwise {@code false}.
     */
    @Override
    public boolean hasNext()
    {
        return !((parser == null) && (corpusIndex >= (corporaParser.size() - 1)));
    }


    /**
     * Read the next document from the corpus.
     *
     * @throws RuntimeException If an exception has occurred while parsing a new document.
     * @return The next document read from the corpus, or {@code null} if none are available.
     */
    @Override
    public ParsedDocument next()
    {
        while (hasNext())
        {
            if (parser == null)
            {
                corpusIndex++;
                parser = corporaParser.get(corpusIndex);
            }

            if (parser.hasNext())
                return parser.next();
            else
                parser = null;
        }

        return null;
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
        if (corporaParser != null)
        {
            for (CorpusParser corpusParser : corporaParser)
            {
                try
                {
                    corpusParser.close();
                }
                catch (Throwable ignored)
                {
                }
            }
        }
    }
}
