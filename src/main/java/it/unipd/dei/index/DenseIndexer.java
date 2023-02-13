package it.unipd.dei.index;

import it.unipd.dei.corpus.CorpusParser;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.pipeline.IndexTimingInfo;
import it.unipd.dei.search.DenseSearcher;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * The {@code DenseIndexer} class is an {@link Indexer} used to index the corpora data and later perform
 * dense retrieval on it using the {@link DenseSearcher} class. For each document text,
 * an embedding is computed by the <a href="https://huggingface.co/">Transformers</a> library, and the data is indexed
 * using the <a href="https://faiss.ai/">Faiss</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class DenseIndexer implements Indexer
{
    private CorpusParser parser;
    private ExternalScriptDriver sd;
    private final int batchSize;
    private final int chunksSize;
    private int chunkIndex;

    private PrintWriter docsWriter;
    private PrintWriter refsWriter;
    private long refsIndex;


    /**
     * Create the {@link Indexer}.
     *
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param indexDirectory The path to the folder where the index will be stored.
     * @param model The name of the dense retrieval model to use.
     * @param vectorSize The number of elements in the embedding produced by the model.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param similarity The similarity function that will be later used to compare query and documents.
     * Accepted values are:
     * <ul>
     *     <li>\"dot\": Dot product</li>
     *     <li>\"cos\": Cosine similarity</li>
     *     <li>\"l2\": Euclidean distance</li>
     *     <li>\"l2sq\": Squared Euclidean distance</li>
     * </ul>
     * @param batchSize The number of documents to be processed together in a single batch.
     * @param chunksSize The number of documents to be processed in a single chunk.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws IllegalArgumentException If any the provided vector size, maximum number of tokens, batch size or
     * chunk size is not a positive integer number.
     * @throws RuntimeException If an exception has occurred while creating the indexer.
     */
    public DenseIndexer(String pythonFilename, String workingDirectory, String scriptFilename, String indexDirectory,
                        String model, int vectorSize, int maxTokens, String similarity, int batchSize, int chunksSize)
    {
        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable filename is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided Python script filename is null.");

        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (model == null)
            throw new NullPointerException("The provided Transformers model is null.");

        if (vectorSize < 1)
        {
            throw new IllegalArgumentException("The provided vector size (" + vectorSize + ") is not a " +
                    "positive integer number.");
        }

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided maximum number of tokens (" + maxTokens + ") is not a " +
                    "positive integer number.");
        }

        if (similarity == null)
            throw new NullPointerException("The provided similarity function is null.");

        if (batchSize < 1)
        {
            throw new IllegalArgumentException("The provided batch size (" + batchSize + ") is not a " +
                    "positive integer number.");
        }

        if (chunksSize <= 0)
        {
            throw new IllegalArgumentException("The provided chunk size (" + chunksSize + ") is not a " +
                    "positive integer number.");
        }


        try
        {
            this.batchSize = batchSize;
            this.chunksSize = chunksSize;
            this.chunkIndex = 0;

            final Path indexDirectoryPath = Paths.get(indexDirectory);

            if (Files.exists(indexDirectoryPath) && (!Files.isDirectory(indexDirectoryPath)))
            {
                throw new RuntimeException("The provided index directory \"" + indexDirectoryPath.toAbsolutePath() +
                        "\" already exists and is not a directory.");
            }

            try
            {
                Files.createDirectories(indexDirectoryPath);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("The provided index directory \"" + indexDirectoryPath.toAbsolutePath() +
                        "\" cannot be created successfully.");
            }

            if (!Files.isWritable(indexDirectoryPath))
            {
                throw new RuntimeException("The provided index directory \"" + indexDirectoryPath.toAbsolutePath() +
                        "\" is not a writable directory.");
            }

            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--index_filename=%s", indexDirectory + "/index_%d.faiss"),
                    String.format("--model=%s", model),
                    String.format("--vector_size=%d", vectorSize),
                    String.format("--max_tokens=%d", maxTokens),
                    String.format("--similarity=%s", similarity),
                    String.format("--batch_size=%d", batchSize),
                    String.format("--chunks_size=%d", chunksSize)
                    //Add other parameters for the Python code used for indexing with Faiss.
            );

            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully loaded the indexer. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errInit = sd.waitNextErrorText();
            if (!errInit.isBlank())
                throw new PythonRuntimeException(errInit);


            this.docsWriter = new PrintWriter(indexDirectory + "/docs.txt", StandardCharsets.UTF_8);
            this.refsWriter = new PrintWriter(indexDirectory + "/refs.txt", StandardCharsets.UTF_8);
            this.refsIndex = 0L;
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the indexer.\n", th);
        }
    }


    /**
     * Return an {@link Iterator} of {@link Map.Entry}&lt;{@link Integer},{@link IndexTimingInfo}&gt;,
     * each one representing the number of documents indexed and the timing information for a specific chunk.
     *
     * @return An {@link Iterator} of {@link Map.Entry}&lt;{@link Integer},{@link IndexTimingInfo}&gt;.
     */
    @NotNull
    @Override
    public Iterator<Map.Entry<Integer, IndexTimingInfo>> iterator()
    {
        return this;
    }


    /**
     * Check if there is a new chunk of documents to index.
     *
     * @return {@code true} if there is a new chunk of documents to index, otherwise {@code false}.
     */
    @Override
    public boolean hasNext()
    {
        return parser.hasNext();
    }


    /**
     * Initialize the current object with the corpus parser needed to index the data. This method should
     * be called before attempting to perform the index phase.
     *
     @param parser The corpus parser that feeds this objects with documents.
     @throws NullPointerException If the provided corpus parser is null.
     */
    @Override
    public void init(CorpusParser parser)
    {
        if (parser == null)
            throw new NullPointerException("The provided corpus parser is null.");

        this.parser = parser;
    }


    /**
     * Process the next chunk of documents.
     *
     * @return A pair with the number of documents indexed in this chunk and with timing information.
     * @throws RuntimeException If an exception has occurred while performing indexing.
     */
    @Override
    public Map.Entry<Integer, IndexTimingInfo> next()
    {
        if (parser == null)
            throw new RuntimeException("This object has not been initialized, using the init() method.");

        try
        {
            final IndexTimingInfo timingInfo = new IndexTimingInfo(1000L);

            timingInfo.subTotal(System.currentTimeMillis());

            // Return null if there are no more chunks to index.
            if (!hasNext())
                return null;

            chunkIndex++;

            final PrintWriter toIn = sd.getInputStream();

            int counter = 0;
            int batchCounter = 0;

            timingInfo.subParse(System.currentTimeMillis());
            while ((parser.hasNext()) && (counter < chunksSize))
            {
                // Read the next document from the collection parser.
                final ParsedDocument parDoc = parser.next();
                if ((parDoc == null) || (parDoc.id == null) || (parDoc.text == null) || (parDoc.content == null) ||
                        (parDoc.id.isBlank()) || (parDoc.text.isBlank()) || (parDoc.content.isBlank()))
                    continue;

                timingInfo.addParse(System.currentTimeMillis());
                timingInfo.subIndex(System.currentTimeMillis());

                // Write a "<id>\t<text>\n" line to the "docs" writer, and the index of the first byte
                // of such string, encoded in UTF-8 charset, in the "refs" writer.
                final String line = String.format("%s\t%s\n", parDoc.id, parDoc.text);
                final int lineLength = StandardCharsets.UTF_8.encode(line).limit();

                docsWriter.print(line);
                docsWriter.flush();
                refsWriter.println(refsIndex);
                refsWriter.flush();

                refsIndex += lineLength;

                // Write the content to Faiss (Python code).
                toIn.println(parDoc.content);

                timingInfo.addIndex(System.currentTimeMillis());
                timingInfo.subParse(System.currentTimeMillis());

                counter++;
                batchCounter++;
                if (batchCounter < batchSize)
                    continue;

                timingInfo.addParse(System.currentTimeMillis());

                toIn.flush();

                /*
                Wait for Python script to output a synchronization line in error stream after having
                successfully processed the document. When reading from error stream two cases are possible:
                    - Single empty line: all OK in the Python script.
                    - Otherwise: an exception has been thrown in the Python script,
                                 therefore raise a PythonRuntimeException.
                */
                final String errRun = sd.waitNextErrorText();
                if (!errRun.isBlank())
                    throw new PythonRuntimeException(errRun);

                // Read the timing information from Python code.
                final long externalTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                final long indexTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                timingInfo.addExternal(externalTime);
                timingInfo.addIndex(indexTime);

                timingInfo.subParse(System.currentTimeMillis());

                // Reset the batch status to empty.
                batchCounter = 0;
            }

            timingInfo.addParse(System.currentTimeMillis());

            if (batchCounter > 0)
            {
                toIn.println();
                toIn.flush();

                /*
                Wait for Python script to output a synchronization line in error stream after having
                successfully processed the document. When reading from error stream two cases are possible:
                    - Single empty line: all OK in the Python script.
                    - Otherwise: an exception has been thrown in the Python script,
                                 therefore raise a PythonRuntimeException.
                */
                final String errRun = sd.waitNextErrorText();
                if (!errRun.isBlank())
                    throw new PythonRuntimeException(errRun);

                // Read the timing information from Python code.
                final long externalTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                final long indexTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                timingInfo.addExternal(externalTime);
                timingInfo.addIndex(indexTime);
            }

            // Flush the "docs" and "refs" writers.
            timingInfo.subIndex(System.currentTimeMillis());
            docsWriter.flush();
            refsWriter.flush();
            timingInfo.addIndex(System.currentTimeMillis());
            timingInfo.addTotal(System.currentTimeMillis());

            if (!hasNext())
            {
                toIn.flush();
                toIn.close();

                /*
                Wait for Python script to output a synchronization line in error stream after having
                successfully processed the document. When reading from error stream two cases are possible:
                    - Single empty line: all OK in the Python script.
                    - Otherwise: an exception has been thrown in the Python script,
                                 therefore raise a PythonRuntimeException.
                */
                final String errRun = sd.waitNextErrorText();
                if (!errRun.isBlank())
                    throw new PythonRuntimeException(errRun);

                // Read the timing information from Python code.
                final long externalTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                final long indexTime = Math.round(Double.parseDouble(sd.waitNextOutputLine()) * 1000.0);
                timingInfo.addExternal(externalTime);
                timingInfo.addIndex(indexTime);
            }

            // Return progress information about the indexing of this chunk.
            return new AbstractMap.SimpleImmutableEntry<>(counter, timingInfo);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while performing indexing.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
        if (parser != null)
        {
            try
            {
                parser.close();
            }
            catch (Throwable ignored)
            {
            }
        }

        if (sd != null)
        {
            try
            {
                sd.getInputStream().close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                sd.getOutputStream().close();
            }
            catch (Throwable ignored)
            {
            }
            try
            {
                sd.getErrorStream().close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                sd.close();
            }
            catch (Throwable ignored)
            {
            }

            sd = null;
        }

        if (docsWriter != null)
        {
            try
            {
                docsWriter.flush();
                docsWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            docsWriter = null;
        }

        if (refsWriter != null)
        {
            try
            {
                refsWriter.flush();
                refsWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            refsWriter = null;
        }
    }
}
