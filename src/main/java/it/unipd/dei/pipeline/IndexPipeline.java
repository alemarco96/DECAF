package it.unipd.dei.pipeline;

import it.unipd.dei.corpus.CorpusParser;
import it.unipd.dei.index.Indexer;
import it.unipd.dei.io.PropertiesDriver;
import it.unipd.dei.utils.PropertiesUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * The {@code IndexPipeline} class is in charge of initializing the Index pipeline from a {@code .properties} file
 * and running it. It will log debug information to the output stream, such as the successful processing of the
 * indexing phase along with the final timing recap.
 *
 * @author Marco Alessio
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class IndexPipeline extends AbstractPipeline
{
    private final CorpusParser corpusParser;
    private final Indexer indexer;
    private final int numThreads;

    private final IndexTimingInfo timingInfo;


    /**
     * Create the Index Pipeline, initializing the components from the {@code .properties} file.
     *
     * @param propertiesFilename The {@code .properties} filename.
     * @param charset The {@link Charset} used while reading the {@code .properties} file.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while creating the pipeline.
     */
    public IndexPipeline(String propertiesFilename, Charset charset)
    {
        if (propertiesFilename == null)
            throw new NullPointerException("The provided properties filename is null.");

        if (charset == null)
            throw new NullPointerException("The provided charset is null.");

        try
        {
            timingInfo = new IndexTimingInfo(1000L);

            System.out.println("\nStarting Initialization phase in 0.000 s.");

            timingInfo.subTotal(System.currentTimeMillis());

            timingInfo.subCreation(System.currentTimeMillis());

            /*
            Add the "ROOT_FOLDER" and "VENV_FOLDER" properties to the property driver object.
            Note that the value of these two properties is provided from environmental variables,
            set up in the 'index.sh' launch script, according to this scheme:

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

            // ******************** Corpus Parser ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String corpusParserStr = driver.getProperty("launch.corpus");
            if (corpusParserStr == null)
                throw new RuntimeException("No valid corpus parser has been found in the launch configuration.");

            corpusParser = PropertiesUtils.retrieveObject(driver, String.format("corpus.%s", corpusParserStr),
                    CorpusParser.class);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Corpus Parser \"%s\" created in %.3f s.\n",
                    corpusParserStr, timingInfo.getCreation());

            // ******************** Indexer ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String indexerStr = driver.getProperty("launch.indexer");
            if (indexerStr == null)
                throw new RuntimeException("No valid indexer has been found in the launch configuration.");

            indexer = PropertiesUtils.retrieveObject(driver, String.format("indexer.%s", indexerStr),
                    Indexer.class);

            indexer.init(corpusParser);

            timingInfo.addCreation(System.currentTimeMillis());

            System.out.printf(Locale.US, "Indexer \"%s\" created in %.3f s.\n",
                    indexerStr, timingInfo.getCreation());

            // ******************** # Threads ********************
            timingInfo.subCreation(System.currentTimeMillis());

            final String numThreadsStr = driver.getProperty("launch.num_threads");
            if (numThreadsStr == null)
                throw new RuntimeException("No valid number of threads has been found in the launch configuration.");

            numThreads = Integer.parseInt(numThreadsStr);

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
     * Run the Index pipeline.
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

            while (indexer.hasNext())
            {
                timingInfo.subIndex(System.currentTimeMillis());
                final Integer numDocumentsIndexed = indexer.next();
                timingInfo.addIndex(System.currentTimeMillis());

                // Retry if the returned value is null.
                if (numDocumentsIndexed == null)
                    continue;

                counter++;

                System.out.printf(Locale.US, "Chunk #%d (with %d documents) indexed in %.3f s!\n",
                        counter, numDocumentsIndexed, timingInfo.getTotal());
            }

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
                indexer.close();
            }
            catch (Throwable ignored)
            {
            }
        }
    }
}
