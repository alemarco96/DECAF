package it.unipd.dei.search;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.index.ParsedDocument;
import it.unipd.dei.query.AbstractSpladeQueryGenerator;
import it.unipd.dei.query.QueryGenerator;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The {@code SpladeSearcher} class is a {@link Searcher} that retrieves the set of documents relevant for the
 * query produced by the {@link QueryGenerator}. During the search phase, each query text is being fed
 * to the SPLADE model, which generates a list of tokens-weight pairs used for retrieval.
 * At indexing phase, each document has been expanded with the SPLADE model, and the inverted index has been
 * created using <a href="https://lucene.apache.org/">Lucene</a> Java library. Since Lucene does not support
 * floating-point term frequencies, each weight has been multiplied by a constant factor (such as
 * {@code 100.0} or {@code 1000.0}), then the result has been rounded to the nearest integer.
 * Such approximation may cause slightly different results w.r.t. the one reported in the SPLADE paper(s).
 *
 * @author Marco Alessio
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class SpladeSearcher implements Searcher
{
    private final ExternalScriptDriver sd;
    private final IndexReader reader;
    private final Similarity similarity;
    private final AbstractSpladeQueryGenerator queryGenerator;
    private final QueryParser qp;
    private final int numDocuments;
    private final int numThreads;

    /**
     * Create the {@link Searcher}. It is just a shortcut for {@link SpladeSearcher#SpladeSearcher(String, Similarity,
     * String, String, String, String, int, AbstractSpladeQueryGenerator, int, int)}, with the number
     * of threads set to {@code 1}.
     *
     * @param indexDirectory The path to the folder where the index is stored.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     *                   the query and a document.
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the SPLADE model to use.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param queryGenerator The query generator used to produce the query.
     * @param numDocuments The (maximum) number of documents to retrieve.
     * @throws NullPointerException If any of the provided Lucene index directory or similarity function,
     * Python executable or script filename, Python SPLADE model or query generator is null.
     * @throws IllegalArgumentException If the provided number of documents is not an integer greater or equal to 1.
     * @throws RuntimeException If an exception has occurred while creating the searcher.
     */
    public SpladeSearcher(String indexDirectory, Similarity similarity,
                          String pythonFilename, String workingDirectory, String scriptFilename,
                          String model, int maxTokens, AbstractSpladeQueryGenerator queryGenerator, int numDocuments)
    {
        this(indexDirectory, similarity, pythonFilename, workingDirectory, scriptFilename,
                model, maxTokens, queryGenerator, numDocuments, 1);
    }


    /**
     * Create the {@link Searcher}.
     *
     * @param indexDirectory The path to the folder where the index is stored.
     * @param similarity The Lucene similarity function used to evaluate the similarity of the text between
     *                   the query and a document.
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the SPLADE model to use.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param queryGenerator The query generator used to produce the query.
     * @param numDocuments The (maximum) number of documents to retrieve.
     * @param numThreads The number of threads to use to parallelize the Lucene search.
     * @throws NullPointerException If any of the provided Lucene index directory or similarity function,
     * Python executable or script filename, Python SPLADE model or query generator is null.
     * @throws IllegalArgumentException If the provided number of documents and of threads is not
     * an integer greater or equal to 1.
     * @throws RuntimeException If an exception has occurred while creating the searcher.
     */
    public SpladeSearcher(String indexDirectory, Similarity similarity,
                          String pythonFilename, String workingDirectory, String scriptFilename,
                          String model, int maxTokens, AbstractSpladeQueryGenerator queryGenerator,
                          int numDocuments, int numThreads)
    {
        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (similarity == null)
            throw new NullPointerException("The provided similarity is null.");

        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided Python script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided model is null.");

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided maximum number of tokens (" + maxTokens + ") must be " +
                    "a positive integer number.");
        }

        if (queryGenerator == null)
            throw new NullPointerException("The provided query generator is null.");

        if (numDocuments < 1)
        {
            throw new IllegalArgumentException("The provided number of documents (" + numDocuments + ") must be " +
                    "a positive integer number.");
        }

        if (numThreads < 1)
        {
            throw new IllegalArgumentException("The provided number of threads (" + numThreads + ") must be " +
                    "a positive integer number.");
        }


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

            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--model=%s", model),
                    String.format("--max_tokens=%d", maxTokens)
            );

            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully loaded the predictor. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errInit = sd.waitNextErrorText();
            if (!errInit.isBlank())
                throw new PythonRuntimeException(errInit);


            this.similarity = similarity;
            this.queryGenerator = queryGenerator;
            this.qp = new QueryParser(ParsedDocument.CONTENT_FIELD_NAME, new WhitespaceAnalyzer());
            this.numDocuments = numDocuments;
            this.numThreads = numThreads;

            // Initialize the query generator.
            this.queryGenerator.init(this.sd);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the searcher.\n", th);
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
