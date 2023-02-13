package it.unipd.dei.corpus;

import it.unipd.dei.index.ParsedDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;


/**
 * The {@code TsvCorpusParser} is a {@link CorpusParser} used to read documents from a TSV file.
 * The expected format is: {@code <ID>\t<textual content>\n}, one document per line.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class TsvCorpusParser implements CorpusParser
{
    private final BufferedReader reader;


    /**
     * Create the {@link CorpusParser}, reading data from the specified file.
     *
     * @param filename The filename of the TSV file to read.
     * @throws NullPointerException If the provided filename is null.
     * @throws RuntimeException If an exception has occurred while creating the corpus parser.
     */
    public TsvCorpusParser(String filename)
    {
        if (filename == null)
            throw new NullPointerException("The provided input filename is null.");

        try
        {
            final Path inputPath = Paths.get(filename);

            try
            {
                this.reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Unable to open input file: \"" + inputPath.toAbsolutePath() + "\".");
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
            // Try to read the next character. There is a new document to read only in case of success.
            reader.mark(2);

            final int next_char = reader.read();
            reader.reset();

            return next_char != -1;
        }
        catch (Throwable ignored)
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

            final String id = line.substring(0, sepLoc);
            final String text = line.substring(sepLoc + 1);

            // Discard the document if the <id> and/or <text_content> does not contain any printable character.
            if (id.isBlank() || text.isBlank())
                return null;

            return new ParsedDocument(id, text);
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
