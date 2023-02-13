package it.unipd.dei.corpus;

import it.unipd.dei.index.ParsedDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


/**
 * The {@code MSMARCOv1CorpusParser} is a {@link CorpusParser} for the MS-MARCOv1 document corpus.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class MSMARCOv1CorpusParser implements CorpusParser
{
    private final BufferedReader reader;
    private final Set<String> duplicateIDs;


    /**
     * Create the {@link CorpusParser}, reading data from the specified file.
     *
     * @param corpusFilename The filename of the MS-MARCOv1 documents corpus.
     * @throws NullPointerException If the provided corpus filename is null.
     * @throws RuntimeException If an exception has occurred while creating the corpus parser.
     */
    public MSMARCOv1CorpusParser(String corpusFilename)
    {
        this(corpusFilename, null);
    }


    /**
     * Create the {@link CorpusParser}, reading data from the specified files.
     *
     * @param corpusFilename The filename of the MS-MARCOv1 documents corpus.
     * @param duplicateFilename The filename of the MS-MARCOv1 documents ID to discard.
     * @throws NullPointerException If the provided corpus filename is null.
     * @throws RuntimeException If an exception has occurred while creating the corpus parser.
     */
    public MSMARCOv1CorpusParser(String corpusFilename, String duplicateFilename)
    {
        if (corpusFilename == null)
            throw new NullPointerException("The provided corpus filename is null.");

        try
        {
            final Path corpusPath = Paths.get(corpusFilename);

            try
            {
                this.reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(corpusPath.toFile()), StandardCharsets.UTF_8));
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Unable to open input file: \"" + corpusPath.toAbsolutePath() + "\".");
            }


            if (duplicateFilename != null)
            {
                final Path duplicatesPath = Paths.get(duplicateFilename);

                try
                {
                    duplicateIDs = MSMARCOv1Utils.findDuplicateIDs(duplicateFilename);
                }
                catch (Throwable th)
                {
                    throw new RuntimeException("An exception occurred while processing ID duplicate file: \"" +
                            duplicatesPath.toAbsolutePath() + "\".");
                }
            }
            else
                duplicateIDs = new TreeSet<>();
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
        try
        {
            reader.mark(2);

            final int next_char = reader.read();
            reader.reset();

            return next_char != -1;
        }
        catch (IOException e)
        {
            return false;
        }
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
            /*
            The loop is needed in all cases where the ID of the document is contained in the discard set:
            it enables to automatically read the next line.
            */
            while (true)
            {
                // Read a new line from the reader. Return null if the end of file has been reached.
                String line;
                try
                {
                    line = reader.readLine();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Unable to read from input reader.");
                }
                if (line == null)
                    return null;

                // Split the line across '\t'. The expected format is: "<id>\t<text_content>".
                final int sepLoc = line.indexOf('\t');
                if (sepLoc == -1)
                    throw new RuntimeException("Illegal format for line: \"" + line + "\".");

                final String id = "MARCO_" + line.substring(0, sepLoc);
                final String text = line.substring(sepLoc + 1);

                // Discard the document if the <id> and/or <text_content> does not contain any printable character.
                if (id.isBlank() || text.isBlank())
                    continue;

                // Check if the ID of the current document is in the set of IDs to discard. If yes, do so.
                if (duplicateIDs.contains(id))
                    continue;

                return new ParsedDocument(id, text);
            }
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
        try
        {
            reader.close();
        }
        catch (Throwable ignored)
        {
        }
    }
}
