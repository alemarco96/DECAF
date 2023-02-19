package it.unipd.dei.index;

import it.unipd.dei.corpus.CorpusParser;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;


/**
 * The {@code BoWIndexer} class is an {@link Indexer} used to index the corpora data and build an
 * inverted index using <a href="https://lucene.apache.org/">Lucene</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class BoWIndexer implements Indexer
{
    private CorpusParser parser;
    private final IndexWriter writer;
    private final int chunksSize;


    /**
     * Create the {@link Indexer}.
     *
     * @param analyzer The Lucene analyzer that is applied to the text of the query.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     * the query and a document.
     * @param indexDirectory The path to the folder where the index will be stored.
     * @param chunksSize The number of documents to be processed in a single chunk.
     * @param ramBufferSizeMB The size of Lucene temporary index buffer, in MB.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws IllegalArgumentException If any the provided batch size or chunk size is not a positive integer number.
     * @throws RuntimeException If an exception has occurred while creating the indexer.
     */
    public BoWIndexer(Analyzer analyzer, Similarity similarity, String indexDirectory, int chunksSize,
                      int ramBufferSizeMB)
    {
        if (analyzer == null)
            throw new NullPointerException("The provided analyzer is null.");

        if (similarity == null)
            throw new NullPointerException("The provided similarity is null.");

        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (chunksSize <= 0)
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
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the indexer.\n", th);
        }
    }


    /**
     * Return an {@link Iterator} of {@link Integer}, each one representing the number of documents
     * indexed for a specific chunk.
     *
     * @return An {@link Iterator} of {@link Integer}.
     */
    @NotNull
    @Override
    public Iterator<Integer> iterator()
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
     * @return The number of documents indexed in this chunk.
     * @throws RuntimeException If an exception has occurred while performing indexing.
     */
    @Override
    public Integer next()
    {
        if (parser == null)
            throw new RuntimeException("This object has not been initialized, using the init() method.");

        try
        {
            // Return null if there are no more chunks to index.
            if (!hasNext())
                return null;

            int counter = 0;
            while ((parser.hasNext()) && (counter < chunksSize))
            {
                // Read the next document from the collection parser.
                final ParsedDocument parDoc = parser.next();
                if ((parDoc == null) || (parDoc.id == null) || (parDoc.text == null) || (parDoc.content == null) ||
                        (parDoc.id.isBlank()) || (parDoc.text.isBlank()) || (parDoc.content.isBlank()))
                    continue;

                // Add the ID, text and content fields to the Lucene representation of the document.
                final Document doc = new Document();
                doc.add(new StringField(ParsedDocument.ID_FIELD_NAME, parDoc.id, Field.Store.YES));
                doc.add(new TextField(ParsedDocument.TEXT_FIELD_NAME, parDoc.text, Field.Store.YES));
                doc.add(new ContentFieldWithNorms(parDoc.content));

                // Add the document to the Lucene index.
                writer.addDocument(doc);
                counter++;
            }

            // Commit all data added to the Lucene index on permanent storage.
            writer.commit();

            // Return progress information about the indexing of this chunk.
            return counter;
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
    }
}
