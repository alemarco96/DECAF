package it.unipd.dei.search;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;
import it.unipd.dei.query.AbstractDenseQueryGenerator;
import it.unipd.dei.query.QueryGenerator;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The {@code DenseSearcher} class is a {@link Searcher} that retrieves the set of documents relevant for the
 * query produced by the {@link QueryGenerator}, using <a href="https://faiss.ai/">Faiss</a> similarity search library.
 * The embeddings are produced using the <a href="https://huggingface.co/">Transformers</a> library.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class DenseSearcher implements Searcher
{
    private final AbstractDenseQueryGenerator queryGenerator;
    private final int numDocuments;
    private ExternalScriptDriver sd;


    /**
     * Create the {@link Searcher}.
     *
     * @param indexDirectory The path to the folder where the index is stored.
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the dense retrieval model to use.
     * @param vectorSize The number of elements in the embedding produced by the model.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param similarity The similarity function to use to compare queries and documents. Accepted values are:
     *                   <ul>
     *                      <li>\"cos\": Cosine similarity</li>
     *                      <li>\"dot\": Dot product</li>
     *                      <li>\"l2\": Euclidean distance</li>
     *                      <li>\"l2sq\": Squared Euclidean distance</li>
     *                   </ul>
     * @param queryGenerator The query generator used to produce the query.
     * @param numDocuments The (maximum) number of documents to retrieve.
     * @throws NullPointerException If any of the provided query generator, Python executable or script filename or
     * Transformers dense retrieval model is null.
     * @throws IllegalArgumentException If any of the provided vector size, maximum number of tokens or number of
     * documents is not a positive integer number, or the similarity function is unknown.
     * @throws RuntimeException If an exception has occurred while creating the searcher.
     */
    public DenseSearcher(String indexDirectory, String pythonFilename, String workingDirectory, String scriptFilename,
                         String model, int vectorSize, int maxTokens, String similarity,
                         AbstractDenseQueryGenerator queryGenerator, int numDocuments)
    {
        if (indexDirectory == null)
            throw new NullPointerException("The provided index directory is null.");

        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided Python script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided dense retrieval model is null.");

        if (vectorSize < 1)
        {
            throw new IllegalArgumentException("The provided vector size (" + vectorSize + ") must be " +
                    "a positive integer number.");
        }

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided maximum number of tokens (" + maxTokens + ") must be " +
                    "a positive integer number.");
        }

        if (similarity == null)
            throw new NullPointerException("The provided similarity function is null.");

        if (!(similarity.equals("cos") || similarity.equals("dot") || similarity.equals("l2") ||
                similarity.equals("l2sq")))
        {
            throw new IllegalArgumentException("The provided similarity function (" + similarity + ") is unknown. " +
                    "The available options are: \"cos\", \"dot\", \"l2\" and \"l2sq\".");
        }

        if (queryGenerator == null)
            throw new NullPointerException("The provided query generator is null.");

        if (numDocuments < 1)
        {
            throw new IllegalArgumentException("The provided number of documents (" + numDocuments + ") must be " +
                    "a positive integer number.");
        }


        try
        {
            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--index_filename=%s/%s", indexDirectory, "index_%d.faiss"),
                    String.format("--docs_filename=%s/%s", indexDirectory, "docs.txt"),
                    String.format("--refs_filename=%s/%s", indexDirectory, "refs.txt"),
                    String.format("--model=%s", model),
                    String.format("--vector_size=%d", vectorSize),
                    String.format("--max_tokens=%d", maxTokens),
                    String.format("--similarity=%s", similarity)
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

            this.queryGenerator = queryGenerator;
            this.numDocuments = numDocuments;
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
            final Utterance utterance = conversation.getUtteranceByID(utteranceId);

            // Generate the query. Put it and the number of documents to retrieve in the Python's input stream.
            final List<Map.Entry<String, Double>> query = queryGenerator.generate(utteranceId, conversation);

            final PrintWriter toIn = sd.getInputStream();

            toIn.println(query.size());
            for (Map.Entry<String, Double> entry : query)
            {
                toIn.println(entry.getKey());
                toIn.println(entry.getValue());
            }

            toIn.println(numDocuments);
            toIn.flush();

            // Python code will now perform the search using Faiss.

            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully searched the query. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errInit = sd.waitNextErrorText();
            if (!errInit.isBlank())
                throw new PythonRuntimeException(errInit);

            // Retrieve the results of the search from Python's output stream.
            final Map<String, Integer> mapping = new HashMap<>();
            final Map<String, String> texts = new HashMap<>();
            final Map<String, Double> ranking = new HashMap<>();

            // Read from Python code the number of documents actually retrieved by Faiss.
            final int numDocumentsRetrieved = Integer.parseInt(sd.waitNextOutputLine());

            for (int i = 0; i < numDocumentsRetrieved; i++)
            {
                final String id = sd.waitNextOutputLine();
                final double score = Double.parseDouble(sd.waitNextOutputLine());
                final int index = Integer.parseInt(sd.waitNextOutputLine());
                final String text = sd.waitNextOutputLine();

                ranking.put(id, score);
                mapping.put(id, index);
                texts.put(id, text);
            }

            // Save the results into the current utterance.
            utterance.setInitialRankings(ranking);
            utterance.setDocumentsMapping(mapping);
            utterance.setDocumentsText(texts);
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
