package it.unipd.dei.writer;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.search.Searcher;
import it.unipd.dei.utils.RankingUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * The {@code DebugRunWriter} class is a {@link RunWriter} used to generate multiple outputs for debugging purposes,
 * such as:
 * <ul>
 *     <li>
 *         For both the initial and reranked rankings (produced respectively by the {@link Searcher} and
 *         by the {@link Reranker}):
 *         <ul>
 *             <li>The run in TREC-eval format.</li>
 *             <li>Separate runs for every conversation in TREC-eval format.</li>
 *             <li>Separate runs for every utterances divided by their turn (1 to 7, and 8+), in TREC-eval format.</li>
 *             <li>Text file containing the rewritten text along with the top 10 responses for every utterance.</li>
 *         </ul>
 *     </li>
 *     <li>Text file containing both the original and the rewritten text for every utterance.</li>
 *     <li>
 *         Text file containing the number of documents belonging to each utterance and all previous utterances
 *         of the same conversation.
 *     </li>
 * </ul>
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class DebugRunWriter implements RunWriter
{
    private static final int NUM_TURNS = 8;
    
    private PrintWriter iFullWriter;
    private PrintWriter[] iTurnWriters;
    private PrintWriter iConversationWriter;
    private PrintWriter iResponsesWriter;

    private PrintWriter rFullWriter;
    private PrintWriter[] rTurnWriters;
    private PrintWriter rConversationWriter;
    private PrintWriter rResponsesWriter;


    private PrintWriter overlapsWriter;
    private PrintWriter utterancesWriter;


    private final String outputDirectory;
    private final String runId;
    private String conversationId;


    /**
     * Create the {@link RunWriter}.
     *
     * @param outputDirectory The path to the folder where all output files will be stored.
     * @param runId The name of the run.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while creating the run writer.
     */
    public DebugRunWriter(String outputDirectory, String runId)
    {
        if (outputDirectory == null)
            throw new NullPointerException("The provided output directory is null.");

        if (runId == null)
            throw new NullPointerException("The provided run ID is null.");

        try
        {
            Files.createDirectories(Paths.get(outputDirectory));
            Files.createDirectories(Paths.get(outputDirectory + "/initial/"));
            Files.createDirectories(Paths.get(outputDirectory + "/initial/conv/"));
            Files.createDirectories(Paths.get(outputDirectory + "/initial/turn/"));
            Files.createDirectories(Paths.get(outputDirectory + "/reranked/"));
            Files.createDirectories(Paths.get(outputDirectory + "/reranked/conv/"));
            Files.createDirectories(Paths.get(outputDirectory + "/reranked/turn/"));

            this.outputDirectory = outputDirectory;
            this.runId = runId;
            this.conversationId = null;


            iFullWriter = new PrintWriter(outputDirectory + "/initial/full.txt", StandardCharsets.UTF_8);
            iTurnWriters = new PrintWriter[NUM_TURNS + 1];
            for (int i = 1; i < iTurnWriters.length; i++)
            {
                iTurnWriters[i] = new PrintWriter(outputDirectory + "/initial/turn/" + i + ".txt",
                        StandardCharsets.UTF_8);
            }
            iConversationWriter = null;
            iResponsesWriter = new PrintWriter(outputDirectory + "/initial/responses.txt",
                    StandardCharsets.UTF_8);


            rFullWriter = new PrintWriter(outputDirectory + "/reranked/full.txt", StandardCharsets.UTF_8);
            rTurnWriters = new PrintWriter[NUM_TURNS + 1];
            for (int i = 1; i < rTurnWriters.length; i++)
            {
                rTurnWriters[i] = new PrintWriter(outputDirectory + "/reranked/turn/" + i + ".txt",
                        StandardCharsets.UTF_8);
            }
            rConversationWriter = null;
            rResponsesWriter = new PrintWriter(outputDirectory + "/reranked/responses.txt",
                    StandardCharsets.UTF_8);


            overlapsWriter = new PrintWriter(outputDirectory + "/overlaps.txt", StandardCharsets.UTF_8);
            utterancesWriter = new PrintWriter(outputDirectory + "/utterances.txt", StandardCharsets.UTF_8);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while creating the run writer.\n", th);
        }
    }


    /**
     * Append the data of the current query to each output file.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @param queryId The ID of the query.
     * @throws RuntimeException If this object has already been closed or if an exception has occurred while
     * writing the run.
     */
    @Override
    public void write(String utteranceId, Conversation conversation, String queryId)
    {
        if (iFullWriter == null)
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
            final Utterance utterance1 = conversation.getUtteranceByID(utteranceId);
            final int turn = conversation.size();

            // Check if the conversation has changed since the last invocation of write() method.
            if (!conversation.getID().equals(conversationId))
            {
                if (iConversationWriter != null)
                {
                    try
                    {
                        iConversationWriter.flush();
                        iConversationWriter.close();
                    }
                    catch (Throwable ignored)
                    {
                    }

                    iConversationWriter = null;
                }

                if (rConversationWriter != null)
                {
                    try
                    {
                        rConversationWriter.flush();
                        rConversationWriter.close();
                    }
                    catch (Throwable ignored)
                    {
                    }

                    rConversationWriter = null;
                }


                conversationId = conversation.getID();
                try
                {
                    iConversationWriter = new PrintWriter(outputDirectory + "/initial/conv/" + conversationId +
                            ".txt", StandardCharsets.UTF_8);

                    rConversationWriter = new PrintWriter(outputDirectory + "/reranked/conv/" + conversationId +
                            ".txt", StandardCharsets.UTF_8);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Unable to create new writers for conversation \"" +
                            conversationId + "\".", e);
                }
            }


            RankingUtils.writeRanking(iFullWriter, utterance1.getInitialRanking(), runId, queryId);
            RankingUtils.writeRanking(rFullWriter, utterance1.getRerankedRanking(), runId, queryId);

            RankingUtils.writeRanking(iConversationWriter, utterance1.getInitialRanking(), runId, queryId);
            RankingUtils.writeRanking(rConversationWriter, utterance1.getRerankedRanking(), runId, queryId);

            RankingUtils.writeRanking(turn >= NUM_TURNS ? iTurnWriters[NUM_TURNS] : iTurnWriters[turn],
                    utterance1.getInitialRanking(), runId, queryId);
            RankingUtils.writeRanking(turn >= NUM_TURNS ? rTurnWriters[NUM_TURNS] : rTurnWriters[turn],
                    utterance1.getRerankedRanking(), runId, queryId);

            utterancesWriter.printf("%s\t%s\t%s\t%s\n", conversation.getID(), utteranceId,
                    utterance1.getOriginalContent(), utterance1.getRewrittenContent());


            final Map<String, String> documentsText = utterance1.getDocumentsText();

            iResponsesWriter.printf("%s  => %s\n", utteranceId, utterance1.getRewrittenContent());
            rResponsesWriter.printf("%s  => %s\n", utteranceId, utterance1.getRewrittenContent());

            final List<Map.Entry<String, Double>> iRanking = RankingUtils.sortRanking(utterance1.getInitialRanking());
            final List<Map.Entry<String, Double>> rRanking = RankingUtils.sortRanking(utterance1.getRerankedRanking());

            for (int i = 0; i < Math.min(iRanking.size(), 10); i++)
            {
                final String id = iRanking.get(i).getKey();
                final String response = documentsText.get(id);

                iResponsesWriter.println("*".repeat(50));
                iResponsesWriter.println(response);
            }
            iResponsesWriter.println("\n\n");

            for (int i = 0; i < Math.min(rRanking.size(), 10); i++)
            {
                final String id = rRanking.get(i).getKey();
                final String response = documentsText.get(id);

                rResponsesWriter.println("*".repeat(50));
                rResponsesWriter.println(response);
            }
            rResponsesWriter.println("\n\n");


            final List<Integer> overlaps = new ArrayList<>();
            for (int i = 0; i < conversation.size() - 1; i++)
            {
                final Utterance utterance2 = conversation.getUtteranceByTurn(i);

                final Map<String, Double> ranking1 = utterance1.getInitialRanking();
                final Map<String, Double> ranking2 = utterance2.getInitialRanking();

                overlaps.add(RankingUtils.computeRankingIntersection(ranking1, ranking2).size());
            }

            overlapsWriter.printf("%s\t%s", conversation.getID(), utteranceId);
            for (int ol : overlaps)
                overlapsWriter.printf(Locale.US, "\t%d", ol);
            overlapsWriter.println();
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
    public void close() throws Exception
    {
        if (iFullWriter != null)
        {
            try
            {
                iFullWriter.flush();
                iFullWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            iFullWriter = null;
        }

        for (int i = 0; i < iTurnWriters.length; i++)
        {
            if (iTurnWriters[i] != null)
            {
                try
                {
                    iTurnWriters[i].flush();
                    iTurnWriters[i].close();
                }
                catch (Throwable ignored)
                {
                }

                iTurnWriters[i] = null;
            }
        }
        iTurnWriters = null;

        if (iConversationWriter != null)
        {
            try
            {
                iConversationWriter.flush();
                iConversationWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            iConversationWriter = null;
        }

        if (iResponsesWriter != null)
        {
            try
            {
                iResponsesWriter.flush();
                iResponsesWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            iResponsesWriter = null;
        }


        if (rFullWriter != null)
        {
            try
            {
                rFullWriter.flush();
                rFullWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            rFullWriter = null;
        }

        for (int i = 0; i < rTurnWriters.length; i++)
        {
            if (rTurnWriters[i] != null)
            {
                try
                {
                    rTurnWriters[i].flush();
                    rTurnWriters[i].close();
                }
                catch (Throwable ignored)
                {
                }

                rTurnWriters[i] = null;
            }
        }
        rTurnWriters = null;

        if (rConversationWriter != null)
        {
            try
            {
                rConversationWriter.flush();
                rConversationWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            rConversationWriter = null;
        }

        if (rResponsesWriter != null)
        {
            try
            {
                rResponsesWriter.flush();
                rResponsesWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            rResponsesWriter = null;
        }


        if (overlapsWriter != null)
        {
            try
            {
                overlapsWriter.flush();
                overlapsWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            overlapsWriter = null;
        }

        if (utterancesWriter != null)
        {
            try
            {
                utterancesWriter.flush();
                utterancesWriter.close();
            }
            catch (Throwable ignored)
            {
            }

            utterancesWriter = null;
        }
    }
}
