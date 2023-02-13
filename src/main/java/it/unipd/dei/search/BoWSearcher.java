package it.unipd.dei.search;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.index.ParsedDocument;
import it.unipd.dei.query.AbstractBoWQueryGenerator;
import it.unipd.dei.query.QueryGenerator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The {@code BoWSearcher} class is a {@link Searcher} that retrieves the set of documents relevant for the
 * query produced by the {@link QueryGenerator}, using <a href="https://lucene.apache.org/">Lucene</a> Java library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class BoWSearcher implements Searcher
{
    private final IndexReader reader;
    private final Similarity similarity;
    private final AbstractBoWQueryGenerator queryGenerator;
    private final QueryParser qp;
    private final int numDocuments;
    private final int numThreads;


    /**
     * Create the {@link Searcher}. It is just a shortcut for
     * {@link BoWSearcher#BoWSearcher(String, Analyzer, Similarity, AbstractBoWQueryGenerator, int, int)},
     * with the number of threads set to {@code 1}.
     *
     * @param indexDirectory The path to the folder where the index is stored.
     * @param analyzer The Lucene analyzer that is applied to the text of the query.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     *                   the query and a document.
     * @param queryGenerator The query generator used to produce the query.
     * @param numDocuments The (maximum) number of documents to retrieve.
     * @throws NullPointerException If any of the provided index directory, analyzer, similarity function
     * or query generator is null.
     * @throws IllegalArgumentException If the provided number of documents is not a positive integer number.
     * @throws RuntimeException If an exception has occurred while creating the searcher.
     */
    public BoWSearcher(String indexDirectory, Analyzer analyzer, Similarity similarity,
                       AbstractBoWQueryGenerator queryGenerator, int numDocuments)
    {
        this(indexDirectory, analyzer, similarity, queryGenerator, numDocuments, 1);
    }


    /**
     * Create the {@link Searcher}.
     *
     * @param indexDirectory The path to the folder where the index is stored.
     * @param analyzer The Lucene analyzer that is applied to the text of the query.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     * the query and a document.
     * @param queryGenerator The query generator used to produce the query.
     * @param numDocuments The (maximum) number of documents to retrieve.
     * @param numThreads The number of threads to use to parallelize the Lucene search.
     * @throws NullPointerException If any of the provided Lucene index directory, analyzer, similarity function
     * or query generator is null.
     * @throws IllegalArgumentException If any of the provided number of documents and threads is not
     * a positive integer number.
     * @throws RuntimeException If an exception has occurred while creating the searcher.
     */
    public BoWSearcher(String indexDirectory, Analyzer analyzer, Similarity similarity,
                       AbstractBoWQueryGenerator queryGenerator, int numDocuments, int numThreads)
    {
        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (analyzer == null)
            throw new NullPointerException("The provided analyzer is null.");

        if (similarity == null)
            throw new NullPointerException("The provided similarity is null.");

        if (queryGenerator == null)
            throw new NullPointerException("The provided query generator is null.");

        if (numDocuments < 1)
            throw new IllegalArgumentException("The provided number of documents (" + numDocuments + ") must be " +
                    "a positive integer number.");

        if (numThreads < 1)
            throw new IllegalArgumentException("The provided number of threads (" + numThreads + ") must be " +
                    "a positive integer number.");


        try
        {
            final Path indexDirectoryPath = Paths.get(indexDirectory);

            if (!Files.isReadable(indexDirectoryPath))
            {
                throw new RuntimeException(String.format("The provided index directory \"%s\" is not readable.",
                        indexDirectoryPath.toAbsolutePath()));
            }

            if (!Files.isDirectory(indexDirectoryPath))
            {
                throw new RuntimeException(String.format("The provided index directory \"%s\" is not a directory.",
                        indexDirectoryPath.toAbsolutePath()));
            }


            try
            {
                reader = DirectoryReader.open(FSDirectory.open(indexDirectoryPath));
            }
            catch (Throwable th)
            {
                throw new RuntimeException(String.format("Unable to read the index from the directory \"%s\".",
                        indexDirectoryPath.toAbsolutePath()), th);
            }


            this.similarity = similarity;
            this.queryGenerator = queryGenerator;
            this.qp = new QueryParser(ParsedDocument.CONTENT_FIELD_NAME, analyzer);
            this.numDocuments = numDocuments;
            this.numThreads = numThreads;

            // Initialize the query generator.
            this.queryGenerator.init(this.qp);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while creating the searcher.");
        }
    }


    /**
     * Retrieve a small set of documents relevant for the query produced by the {@link QueryGenerator}.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @throws RuntimeException If an exception has occurred while performing the search.
     */
    @Override
    public void search(String utteranceId, Conversation conversation)
    {
        try
        {
            /*
            Create the ExecutorService to parallelize the search performed by Lucene (if the number of threads
            assigned to the Java process is greater than 1). Then create the IndexSearcher to perform such search.
            */
            final ExecutorService es;
            final IndexSearcher searcher;
            if (numThreads > 1)
            {
                es = Executors.newFixedThreadPool(numThreads);
                searcher = new IndexSearcher(reader, es);
            }
            else
            {
                es = null;
                searcher = new IndexSearcher(reader);
            }
            searcher.setSimilarity(similarity);

            // Retrieve the current utterance.
            final Utterance utterance = conversation.getUtteranceByID(utteranceId);

            // Generate the query.
            final Query query = queryGenerator.generate(utteranceId, conversation);

            // Perform the search using Lucene.
            final ScoreDoc[] docs = searcher.search(query, numDocuments).scoreDocs;
            if (es != null)
                es.shutdown();

            // Retrieve the results of the search from Lucene.
            final Map<String, Integer> mapping = new HashMap<>();
            final Map<String, String> texts = new HashMap<>();
            final Map<String, Double> ranking = new HashMap<>();
            for (ScoreDoc sd : docs)
            {
                final Document doc = reader.document(sd.doc);

                final String id = doc.get(ParsedDocument.ID_FIELD_NAME);
                final String text = doc.get(ParsedDocument.TEXT_FIELD_NAME);
                final int index = sd.doc;
                final double score = sd.score;

                mapping.put(id, index);
                texts.put(id, text);
                ranking.put(id, score);
            }

            // Save the results into the current utterance.
            utterance.setDocumentsMapping(mapping);
            utterance.setDocumentsText(texts);
            utterance.setInitialRankings(ranking);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while performing the search.\n", th);
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
            queryGenerator.close();
        }
        catch (Throwable ignored)
        {
        }

        try
        {
            reader.close();
        }
        catch (Throwable ignored)
        {
        }
    }
}
