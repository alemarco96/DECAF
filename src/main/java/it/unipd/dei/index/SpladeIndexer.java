package it.unipd.dei.index;

import it.unipd.dei.corpus.CorpusParser;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.pipeline.IndexTimingInfo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * The {@code SpladeIndexer} class is an {@link Indexer} used to index the corpora data using a SPLADE model
 * and build an inverted index using <a href="https://lucene.apache.org/">Lucene</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class SpladeIndexer implements Indexer
{
    private CorpusParser parser;
    private final IndexWriter writer;
    private ExternalScriptDriver sd;
    private final int batchSize;
    private final int chunksSize;


    /**
     * Create the {@link Indexer}.
     *
     * @param analyzer The Lucene analyzer that is applied to the text of the query.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     * the query and a document.
     * @param indexDirectory The path to the folder where the index will be stored.
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the SPLADE model to use.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param multiplier The factor for which the weight given by SPLADE is multiplied for.
     * @param batchSize The number of documents to be processed together in a single batch.
     * @param chunksSize The number of documents to be processed in a single chunk.
     * @param ramBufferSizeMB The size of Lucene temporary index buffer, in MB.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws IllegalArgumentException If any the provided maximum number of tokens, batch size, chunk size or
     * Lucene temporary buffer size in MB is not a positive integer number, otherwise if the provided multiplier
     * is not a positive finite number.
     * @throws RuntimeException If an exception has occurred while creating the indexer.
     */
    public SpladeIndexer(Analyzer analyzer, Similarity similarity, String indexDirectory, String pythonFilename,
                         String workingDirectory, String scriptFilename, String model, int maxTokens, double multiplier,
                         int batchSize, int chunksSize, int ramBufferSizeMB)
    {
        if (analyzer == null)
            throw new NullPointerException("The provided analyzer is null.");

        if (similarity == null)
            throw new NullPointerException("The provided similarity is null.");

        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable filename is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided Python script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided Transformers model is null.");

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided maximum number of tokens (" + maxTokens + ") is not a " +
                    "positive integer number.");
        }

        if ((!Double.isFinite(multiplier)) || (multiplier < 0.0))
        {
            throw new IllegalArgumentException("The provided multiplier (" + multiplier + ") is not a " +
                    "positive finite number.");
        }

        if (batchSize < 1)
        {
            throw new IllegalArgumentException("The provided batch size (" + batchSize + ") is not a " +
                    "positive integer number.");
        }

        if (chunksSize < 1)
        {
            throw new IllegalArgumentException("The provided chunks size (" + chunksSize + ") is not a " +
                    "positive integer number.");
        }

        if (ramBufferSizeMB < 0)
        {
            throw new IllegalArgumentException("The provided ram buffer size in MB (" + ramBufferSizeMB +
                    ") is not a non-negative integer number.");
        }


        try
        {
            this.batchSize = batchSize;
            this.chunksSize = chunksSize;

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

            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            iwc.setSimilarity(similarity);
            iwc.setCommitOnClose(true);
            iwc.setRAMBufferSizeMB(ramBufferSizeMB);

            try
            {
                writer = new IndexWriter(FSDirectory.open(indexDirectoryPath), iwc);
            }
            catch (Throwable th)
            {
                throw new RuntimeException("Unable to create the index writer at the provided location \"" +
                        indexDirectoryPath.toAbsolutePath() + "\".");
            }

            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--model=%s", model),
                    String.format("--max_tokens=%d", maxTokens),
                    String.format("--multiplier=%f", multiplier),
                    String.format("--batch_size=%d", batchSize)
                    //Add other parameters for the Python code used for indexing using SPLADE.
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


    // Process a batch of documents and index them using Lucene.
    private IndexTimingInfo processBatch(List<Map.Entry<String, String>> batchData) throws Exception
    {
        final IndexTimingInfo timingInfo = new IndexTimingInfo(1000L);

        timingInfo.subExternal(System.currentTimeMillis());

        /*
        Wait for Python script to output a synchronization line in error stream after having
        successfully rewritten the utterance. When reading from error stream two cases are possible:
            - Single empty line: all OK in the Python script.
            - Otherwise: an exception has been thrown in the Python script,
            therefore raise a PythonRuntimeException.
        */
        final String errRun = sd.waitNextErrorText();
        if (!errRun.isBlank())
            throw new PythonRuntimeException(errRun);

        timingInfo.addExternal(System.currentTimeMillis());
        timingInfo.subIndex(System.currentTimeMillis());

        for (final Map.Entry<String, String> entry : batchData)
        {
            final String id = entry.getKey();
            final String text = entry.getValue();

            // Add the ID and text fields to the Lucene representation of the document.
            final Document doc = new Document();
            doc.add(new StringField(ParsedDocument.ID_FIELD_NAME, id, Field.Store.YES));
            doc.add(new TextField(ParsedDocument.TEXT_FIELD_NAME, text, Field.Store.YES));

            timingInfo.addIndex(System.currentTimeMillis());
            timingInfo.subExternal(System.currentTimeMillis());

            // Read the data to index given by the SPLADE model.
            final int numTokens = Integer.parseInt(sd.waitNextOutputLine());
            for (int i = 0; i < numTokens; i++)
            {
                // Read the i-th (token, integer score) pair given by the SPLADE model.
                final String token = sd.waitNextOutputLine();
                final int num = Integer.parseInt(sd.waitNextOutputLine());

                timingInfo.addExternal(System.currentTimeMillis());
                timingInfo.subIndex(System.currentTimeMillis());

                // Add the given token "integer score" times in the content field of the document.
                doc.add(new ContentFieldWithoutNorms((token + " ").repeat(num)));

                timingInfo.addIndex(System.currentTimeMillis());
                timingInfo.subExternal(System.currentTimeMillis());
            }

            timingInfo.addExternal(System.currentTimeMillis());
            timingInfo.subIndex(System.currentTimeMillis());

            // Add the document to the Lucene index.
            writer.addDocument(doc);

            timingInfo.addIndex(System.currentTimeMillis());
        }

        // Return timing information.
        return timingInfo;
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

            final PrintWriter toIn = sd.getInputStream();

            int counter = 0;
            int batchCounter = 0;
            List<Map.Entry<String, String>> batchData = new ArrayList<>();

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

                // Save the ID of the document in the batch.
                batchData.add(new AbstractMap.SimpleImmutableEntry<>(parDoc.id, parDoc.text));

                // Write the content to SPLADE (Python code).
                toIn.println(parDoc.content);

                timingInfo.addIndex(System.currentTimeMillis());
                timingInfo.subParse(System.currentTimeMillis());

                counter++;
                batchCounter++;
                if (batchCounter < batchSize)
                    continue;

                timingInfo.addParse(System.currentTimeMillis());

                toIn.flush();

                // Process the batch of documents.
                final IndexTimingInfo timing = processBatch(batchData);
                timingInfo.add(timing);

                // Reset the batch status to empty.
                batchCounter = 0;
                batchData = new ArrayList<>();

                timingInfo.subParse(System.currentTimeMillis());
            }

            timingInfo.addParse(System.currentTimeMillis());

            // Process the half-filled batch of the remaining documents, if necessary.
            if (batchCounter > 0)
            {
                toIn.println();
                toIn.flush();

                final IndexTimingInfo timing = processBatch(batchData);
                timingInfo.add(timing);
            }

            // Commit all data added to the Lucene index on permanent storage.
            timingInfo.subIndex(System.currentTimeMillis());
            writer.commit();
            timingInfo.addIndex(System.currentTimeMillis());
            timingInfo.addTotal(System.currentTimeMillis());

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

        if (writer != null)
        {
            try
            {
                writer.close();
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
    }
}
