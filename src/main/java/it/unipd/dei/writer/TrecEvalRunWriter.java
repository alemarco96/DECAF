package it.unipd.dei.writer;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.utils.RankingUtils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * The {@code TrecEvalRunWriter} class is a {@link RunWriter} used to generate a run in TREC-eval format.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class TrecEvalRunWriter implements RunWriter
{
    private PrintWriter writer;
    private final String runId;


    /**
     * Create the {@link RunWriter}.
     *
     * @param outputFilename The output file.
     * @param runId The name of the run.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while creating the run writer.
     */
    public TrecEvalRunWriter(String outputFilename, String runId)
    {
        if (outputFilename == null)
            throw new NullPointerException("The provided output file is null.");

        if (runId == null)
            throw new NullPointerException("The provided run ID is null.");

        try
        {
            writer = new PrintWriter(outputFilename);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(String.format("The provided output file \"%s\" is invalid.",
                    outputFilename));
        }

        this.runId = runId;
    }


    /**
     * Append the data of the current query in TREC-eval format to this run.
     * This method is {@code synchronized}, allowing multiple threads to share an instance of
     * {@code TrecEvalRunWriter} without requiring external synchronization.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @param queryId The ID of the query.
     * @throws RuntimeException If this object has already been closed or if an exception has occurred while
     * writing the run.
     */
    @Override
    public synchronized void write(String utteranceId, Conversation conversation, String queryId)
    {
        if (writer == null)
            throw new RuntimeException("The writer has already been closed.");

        if (utteranceId == null)
            throw new NullPointerException("The provided utterance ID is null.");

        if (conversation == null)
            throw new NullPointerException("The provided conversation is null.");

        if (conversation.size() == 0)
            throw new IllegalArgumentException("The provided conversation is empty.");

        if (queryId == null)
            throw new NullPointerException("The provided query ID is null.");

        try
        {
            final Utterance utterance = conversation.getUtteranceByID(utteranceId);
            if (utterance == null)
            {
                throw new RuntimeException("No utterance with ID \"" + utteranceId +
                        "\" can be found in the conversation.");
            }

            // Obtain the ranking from the utterance, then print it in TREC-eval format.
            final Map<String, Double> ranking = utterance.getRerankedRanking();
            RankingUtils.writeRanking(writer, ranking, runId, queryId);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while writing the run.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
        if (writer != null)
        {
            try
            {
                writer.flush();
                writer.close();
            }
            catch (Throwable ignored)
            {
            }

            writer = null;
        }
    }
}
