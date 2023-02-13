package it.unipd.dei.topics;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * The {@code CAsT2019ManualTopicsParser} class is a {@link TopicsParser} for parsing the Manual Topics
 * of <a href="https://www.treccast.ai/">TREC CAsT 2019</a>.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class CAsT2019ManualTopicsParser implements TopicsParser
{
    private BufferedReader reader;
    private String prevConversation;
    private Utterance prevUtterance;


    /**
     * Create the {@link TopicsParser}.
     *
     * @param topicsFilename The filename to the <a href="https://www.treccast.ai/">TREC CAsT 2019</a> Automatic Topics.
     * @throws NullPointerException If the provided topics filename is null.
     * @throws RuntimeException If an exception has occurred while creating the topics parser.
     */
    public CAsT2019ManualTopicsParser(String topicsFilename)
    {
        if (topicsFilename == null)
            throw new NullPointerException("The provided topics file is null.");

        try
        {
            final Path topicsPath = Paths.get(topicsFilename);

            try
            {
                reader = Files.newBufferedReader(topicsPath, StandardCharsets.UTF_8);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("The provided topics file \"" + topicsPath.toAbsolutePath() +
                        "\" does not exist.");
            }

            prevConversation = null;
            prevUtterance = null;
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the topics parser.\n", th);
        }
    }


    /**
     * Check if a new {@link Conversation} has not already been processed.
     *
     * @return {@code true} if a new conversation has not already processed, otherwise {@code false}.
     */
    @Override
    public boolean hasNextConversation()
    {
        if (prevUtterance != null)
            return true;

        if (reader == null)
            return false;

        try
        {
            reader.mark(2);
            final int read = reader.read();
            reader.reset();

            return read != -1;
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }


    /**
     * Retrieve the next {@link Conversation} that has not already been processed.
     *
     * @return The next conversation.
     * @throws RuntimeException If an exception has occurred while creating the conversation.
     */
    @Override
    public Iterable<Utterance> nextConversation()
    {
        if (!hasNextConversation())
            return null;

        try
        {
            List<Utterance> result = new ArrayList<>();
            if (prevUtterance != null)
            {
                result.add(prevUtterance);
                prevUtterance = null;
            }

            String line;
            while ((line = reader.readLine()) != null)
            {
                final int undLoc = line.indexOf('_');
                final int sepLoc = line.indexOf('\t');

                if (sepLoc == -1)
                    throw new RuntimeException("Separator '\\t' not found.");

                if ((undLoc == -1) || (!(undLoc < sepLoc)))
                    throw new RuntimeException("Separator '_' not found.");

                final String conv = line.substring(0, undLoc);
                final String id = line.substring(0, sepLoc);
                final String content = line.substring(sepLoc);

                final Utterance utterance = new Utterance(id, Utterance.Type.QUERY, Utterance.Source.SYSTEM,
                        content);

                if (prevConversation == null)
                    prevConversation = conv;

                if (!conv.equals(prevConversation))
                {
                    prevConversation = conv;
                    prevUtterance = utterance;

                    return result;
                }
                else
                    result.add(utterance);
            }

            return result;
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the conversation.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (Throwable ignored)
            {
            }

            reader = null;
        }
    }
}
