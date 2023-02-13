package it.unipd.dei.topics;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.serialize.CAsT2020;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * The {@code CAsT2020RewrittenTopicsParser} class is a {@link TopicsParser} for parsing the Automatically-rewritten
 * Topics of <a href="https://www.treccast.ai/">TREC CAsT 2020</a>.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class CAsT2020RewrittenTopicsParser implements TopicsParser
{
    private JsonReader reader;


    /**
     * Create the {@link TopicsParser}.
     *
     * @param topicsFilename The filename to the <a href="https://www.treccast.ai/">TREC CAsT 2020</a>
     *                       Automatically-rewritten Topics.
     * @throws NullPointerException If the provided topics filename is null.
     * @throws RuntimeException If an exception has occurred while creating the topics parser.
     */
    public CAsT2020RewrittenTopicsParser(String topicsFilename)
    {
        if (topicsFilename == null)
            throw new NullPointerException("The provided topics file is null.");

        try
        {
            final Path topicsPath = Paths.get(topicsFilename);

            // Create the Gson reader for parsing the topics file.
            try
            {
                reader = new JsonReader(Files.newBufferedReader(topicsPath, StandardCharsets.UTF_8));
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("The provided topics file \"" + topicsPath.toAbsolutePath() + "\" " +
                        "does not exist.");
            }

            // Discard the '[' at the start of the file.
            try
            {
                reader.beginArray();
            }
            catch (IOException e)
            {
                throw new RuntimeException("The provided topics file \"" + topicsPath.toAbsolutePath() +
                        "\" has not the expected format.");
            }
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
        try
        {
            return (reader != null) && (reader.hasNext()) && (!reader.peek().equals(JsonToken.END_ARRAY));
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
            final List<Utterance> conversation = new ArrayList<>();

            final Gson gson = new Gson();
            final CAsT2020.Topic topic = gson.fromJson(reader, CAsT2020.Topic.class);
            for (CAsT2020.Topic.Turn turn : topic.turn)
            {
                final String id = topic.number + "_" + turn.number;

                final String content;
                if (turn.automatic_rewritten_utterance != null)
                    content = turn.automatic_rewritten_utterance;
                else
                    content = turn.raw_utterance;


                final Utterance utterance = new Utterance(id, Utterance.Type.QUERY,
                        Utterance.Source.SYSTEM, content);

                conversation.add(utterance);
            }

            return conversation;
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
