package it.unipd.dei.corpus;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import it.unipd.dei.index.ParsedDocument;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;


/**
 * The {@code TRECCARv2Parser} is a {@link CorpusParser} for the TREC-CARv2 document corpus.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class TRECCARv2CorpusParser implements CorpusParser
{
    private final Iterator<Data.Paragraph> iterator;


    /**
     * Create the {@link CorpusParser}, reading data from the specified file.
     *
     * @param corpusFilename The filename of the TREC-CARv2 documents corpus.
     * @throws NullPointerException If the provided filename is null.
     * @throws RuntimeException If an exception has occurred while creating the corpus parser.
     */
    public TRECCARv2CorpusParser(String corpusFilename)
    {
        if (corpusFilename == null)
            throw new NullPointerException("The provided corpus filename is null.");

        try
        {
            final Path corpusPath = Paths.get(corpusFilename);

            try
            {
                iterator = DeserializeData.iterableParagraphs(new FileInputStream(corpusPath.toFile())).iterator();
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Unable to open input file: \"" + corpusPath.toAbsolutePath() + "\".");
            }
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
    @NotNull
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
        return iterator.hasNext();
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
        try
        {
            Data.Paragraph paragraph = iterator.next();
            if (paragraph == null)
                return null;

            final String id = paragraph.getParaId();
            final String content = paragraph.getTextOnly();

            // Discard the document if the <id> and/or <text_content> does not contain any printable character.
            if (id == null || id.isBlank() || content.isBlank())
                return null;

            return new ParsedDocument("CAR_" + id, content);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while parsing a new document.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
