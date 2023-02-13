package it.unipd.dei.pipeline;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.io.PropertiesDriver;
import it.unipd.dei.utils.PropertiesUtils;
import it.unipd.dei.writer.RunWriter;
import it.unipd.dei.rerank.Reranker;
import it.unipd.dei.rewrite.Rewriter;
import it.unipd.dei.search.Searcher;
import it.unipd.dei.topics.TopicsParser;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * The {@code SearchPipeline} class is in charge of initializing the Search pipeline from a {@code .properties} file
 * and running it. It will log debug information to the output stream, such as the successful processing of a
 * conversation along with the final timing recap.
 *
 * @author Marco Alessio
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class SearchPipeline extends AbstractPipeline
{
    private final TopicsParser topicsParser;
    private final Rewriter rewriter;
    private final Searcher searcher;
    private final Reranker reranker;
    private final RunWriter runWriter;

    private final int numThreads;
    private final int numDocuments;

    private final SearchTimingInfo timingInfo;


    /**
     * Create the Search Pipeline, initializing the components from the {@code .properties} file.
     *
     * @param propertiesFilename The {@code .properties} filename.
     * @param charset The {@link Charset} used while reading the {@code .properties} file.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while creating the pipeline.
     */
    public SearchPipeline(String propertiesFilename, Charset charset)
    {
        if (propertiesFilename == null)
            throw new NullPointerException("The provided properties filename is null.");

        if (charset == null)
            throw new NullPointerException("The provided charset is null.");

        try
        {
            timingInfo = new SearchTimingInfo(1000L);

            System.out.println("\nStarting Initialization phase in 0.000 s.");

            timingInfo.subTotal(System.currentTimeMillis());

            timingInfo.subCreation(System.currentTimeMillis());

            /*
            Add the "ROOT_FOLDER" and "VENV_FOLDER" properties to the property driver object.
            Note that the value of these two properties is provided from environmental variables,
            set up in the 'search.sh' launch script, according to this scheme:

            ROOT_FOLDER property <--- DECAF_CONTAINER_ROOT_FOLDER environmental variable
            VENV_FOLDER property <--- DECAF_VENV_FOLDER environmental variable
            */
            final String ROOT_FOLDER_KEY = "ROOT_FOLDER";
            final String ROOT_FOLDER_VALUE = System.getenv("DECAF_CONTAINER_ROOT_FOLDER");
            final String VENV_FOLDER_KEY = "VENV_FOLDER";
            final String VENV_FOLDER_VALUE = System.getenv("DECAF_VENV_FOLDER");

            if (ROOT_FOLDER_VALUE == null)
            {
                throw new RuntimeException("The \"DECAF_CONTAINER_ROOT_FOLDER\" environmental variable " +
                        "has not been set.");
            }

            if (VENV_FOLDER_VALUE == null)
                throw new RuntimeException("The \"DECAF_VENV_FOLDER\" environmental variable has not been set.");


            final Map<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put(ROOT_FOLDER_KEY, ROOT_FOLDER_VALUE);
            additionalProperties.put(VENV_FOLDER_KEY, VENV_FOLDER_VALUE);

            final PropertiesDriver driver = new PropertiesDriver(propertiesFilename, charset, additionalProperties);

            timingInfo.addCreation(System.currentTimeMillis());

            // ******************** Topics Parser ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String topicsParserStr = driver.getProperty("launch.topics");
            if (topicsParserStr == null)
                throw new RuntimeException("No valid topics parser has been found in the launch configuration.");

            topicsParser = PropertiesUtils.retrieveObject(driver, String.format("topics.%s", topicsParserStr),
                    TopicsParser.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Topics Parser \"%s\" created in %.3f s.\n",
                    topicsParserStr, timingInfo.getCreation());

            // ******************** Rewriter ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String rewriterStr = driver.getProperty("launch.rewriter");
            if (rewriterStr == null)
                throw new RuntimeException("No valid rewriter has been found in the launch configuration.");

            rewriter = PropertiesUtils.retrieveObject(driver, String.format("rewriter.%s", rewriterStr),
                    Rewriter.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Rewriter \"%s\" created in %.3f s.\n",
                    rewriterStr, timingInfo.getCreation());

            // ******************** Searcher ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String searcherStr = driver.getProperty("launch.searcher");
            if (searcherStr == null)
                throw new RuntimeException("No valid searcher has been found in the launch configuration.");

            searcher = PropertiesUtils.retrieveObject(driver, String.format("searcher.%s", searcherStr),
                    Searcher.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Searcher \"%s\" created in %.3f s.\n",
                    searcherStr, timingInfo.getCreation());

            // ******************** Reranker ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String rerankerStr = driver.getProperty("launch.reranker");
            if (rerankerStr == null)
                throw new RuntimeException("No valid reranker has been found in the launch configuration.");

            reranker = PropertiesUtils.retrieveObject(driver, String.format("reranker.%s", rerankerStr),
                    Reranker.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Reranker \"%s\" created in %.3f s.\n",
                    rerankerStr, timingInfo.getCreation());

            // ******************** Run Writer ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String runWriterStr = driver.getProperty("launch.run_writer");
            if (runWriterStr == null)
                throw new RuntimeException("No valid run writer has been found in the launch configuration.");

            runWriter = PropertiesUtils.retrieveObject(driver, String.format("run_writer.%s", runWriterStr),
                    RunWriter.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Run Writer \"%s\" created in %.3f s.\n",
                    runWriterStr, timingInfo.getCreation());

            // ******************** # Threads ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String numThreadsStr = driver.getProperty("launch.num_threads");
            if (numThreadsStr == null)
                throw new RuntimeException("No valid number of threads has been found in the launch configuration.");

            numThreads = Integer.parseInt(numThreadsStr);

            timingInfo.addCreation(System.currentTimeMillis());

            // ******************** # Documents ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String numDocumentsStr = driver.getProperty("launch.num_documents");
            if (numDocumentsStr == null)
                throw new RuntimeException("No valid number of document has been found in the launch configuration.");

            numDocuments = Integer.parseInt(numDocumentsStr);

            timingInfo.addCreation(System.currentTimeMillis());


            timingInfo.addTotal(System.currentTimeMillis());

            System.out.printf(Locale.US, "Initialization phase done in %.3f s.\n", timingInfo.getTotal());
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while creating the pipeline.\n", th);
        }
    }


    /**
     * Run the Search pipeline.
     *
     * @throws RuntimeException If an exception has occurred while running the pipeline.
     */
    @Override
    public void run()
    {
        try
        {
            System.out.printf(Locale.US, "\nStarting Run phase in %.3f s.\n",
                    timingInfo.getTotal());

            int counter = 0;

            timingInfo.subTotal(System.currentTimeMillis());
            timingInfo.subTopics(System.currentTimeMillis());
            while (topicsParser.hasNextConversation())
            {
                counter++;
                final Conversation conversation = new Conversation("#" + counter);
                for (Utterance utterance : topicsParser.nextConversation())
                {
                    if (utterance == null)
                        throw new RuntimeException("The TopicsParser failed to produce a valid utterance.");

                    conversation.addUtterance(utterance);
                    timingInfo.addTopics(System.currentTimeMillis());

                    if (utterance.getType() == Utterance.Type.QUERY)
                    {
                        // Execute the Rewriter.
                        timingInfo.subRewriter(System.currentTimeMillis());
                        rewriter.rewrite(utterance.getID(), conversation);
                        timingInfo.addRewriter(System.currentTimeMillis());

                        // Check that the Rewriter has set the rewritten content of the current utterance.
                        if (utterance.getRewrittenContent() == null)
                        {
                            throw new RuntimeException("The Rewriter failed to set the rewritten content for " +
                                    "the current utterance.");
                        }


                        // Execute the Searcher.
                        timingInfo.subSearcher(System.currentTimeMillis());
                        searcher.search(utterance.getID(), conversation);
                        timingInfo.addSearcher(System.currentTimeMillis());

                        // Check that the Searcher has set all expected data for the current utterance.
                        if (utterance.getInitialRanking() == null)
                        {
                            throw new RuntimeException("The Searcher failed to set the initial ranking for " +
                                    "the current utterance.");
                        }

                        if (utterance.getDocumentsMapping() == null)
                        {
                            throw new RuntimeException("The Searcher failed to set the documents mapping for " +
                                    "the current utterance.");
                        }

                        if (utterance.getDocumentsText() == null)
                        {
                            throw new RuntimeException("The Searcher failed to set the documents text for " +
                                    "the current utterance.");
                        }


                        // Execute the Reranker.
                        timingInfo.subReranker(System.currentTimeMillis());
                        reranker.rerank(utterance.getID(), conversation);
                        timingInfo.addReranker(System.currentTimeMillis());

                        // Check that the Reranker has set the reranked ranking of the current utterance.
                        if (utterance.getRerankedRanking() == null)
                        {
                            throw new RuntimeException("The Reranker failed to set the reranked ranking for " +
                                    "the current utterance.");
                        }


                        // Execute the Run Writer.
                        timingInfo.subWriter(System.currentTimeMillis());
                        runWriter.write(utterance.getID(), conversation, utterance.getID());
                        timingInfo.addWriter(System.currentTimeMillis());
                    }

                    timingInfo.subTopics(System.currentTimeMillis());
                }

                timingInfo.addTotal(System.currentTimeMillis());

                System.out.printf(Locale.US, "Topic %s (with %d utterances) done in %.3f s!\n",
                        conversation.getID(), conversation.size(), timingInfo.getTotal());

                timingInfo.subTotal(System.currentTimeMillis());
            }
            timingInfo.addTopics(System.currentTimeMillis());
            timingInfo.addTotal(System.currentTimeMillis());

            System.out.println("Done!\n");
            System.out.println(timingInfo);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while running the pipeline.\n", th);
        }
        finally
        {
            try
            {
                topicsParser.close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                rewriter.close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                searcher.close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                reranker.close();
            }
            catch (Throwable ignored)
            {
            }

            try
            {
                runWriter.close();
            }
            catch (Throwable ignored)
            {
            }
        }
    }
}
