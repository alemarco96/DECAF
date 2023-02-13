package it.unipd.dei.external;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@code OutputStreamHandler} class is used to manage an output stream of an external {@link Process},
 * usually launched using {@link ProcessBuilder#start()} or {@link Runtime#exec(String[])}.
 * It enables to retrieve the whole content of the stream line by line in a streaming fashion
 * using the {@link OutputStreamHandler#nextLine()} and {@link OutputStreamHandler#nextText()} methods.
 * It is implemented as a separate daemon {@link Thread}.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class OutputStreamHandler extends Thread
{
    // The output or error stream of the external process.
    private final BufferedReader stream;

    // The queue with all text lines extracted from the stream.
    private final Queue<String> textQueue;

    // The synchronization lock object.
    private final Lock lock;

    // The waiting condition.
    private final Condition notEmpty;


    /**
     * Creates the {@code OutputStreamHandler}, using UTF-8 as the stream {@link Charset}.
     *
     * @param stream The stream of the external process.
     * @throws NullPointerException If the provided stream is null.
     */
    public OutputStreamHandler(InputStream stream)
    {
        this(stream, null);
    }

    /**
     * Creates the {@code OutputStreamHandler}, using the provided {@link Charset} for the stream.
     * If the provided {@link Charset} is null, UTF-8 is utilized instead.
     *
     * @param stream The stream of the external process.
     * @param charset The charset used for the stream.
     * @throws NullPointerException If the provided stream is null.
     */
    public OutputStreamHandler(InputStream stream, Charset charset)
    {
        if (stream == null)
            throw new NullPointerException("The provided stream is null.");

        if (charset == null)
            charset = StandardCharsets.UTF_8;

        this.stream = new BufferedReader(new InputStreamReader(stream, charset));
        textQueue = new LinkedList<>();

        lock = new ReentrantLock();
        notEmpty = lock.newCondition();

        setDaemon(true);
        start();
    }

    /**
     * Checks if a new text line is available.
     *
     * @return {@code true} if a new line is available, {@code false} otherwise.
     */
    public boolean hasNextLine()
    {
        try
        {
            lock.lock();

            return textQueue.peek() != null;
        }
        finally
        {
            lock.unlock();
        }
    }


    /**
     * Returns the next text line extracted from the stream, or {@code null} if one is not available.
     *
     * @return The next text line as a {@link String} object, {@code null} if one is not available.
     */
    @Nullable
    public String nextLine()
    {
        try
        {
            lock.lock();

            return textQueue.poll();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns all next text lines extracted from the stream, or {@code null} if none are available.
     *
     * @return All the next text lines as a {@link String} object, {@code null} if none are available.
     */
    @Nullable
    public String nextText()
    {
        try
        {
            lock.lock();

            // Checks if there are some line available. If none are available, return null.
            if (textQueue.peek() == null)
                return null;

            // Build the text string with all text lines extracted from the stream.
            final StringBuilder sb = new StringBuilder(textQueue.remove());

            String line;
            while ((line = textQueue.poll()) != null)
                sb.append('\n').append(line);

            // Return all text lines.
            return sb.toString();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the stream, and returns it
     * as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return The next text line as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextLine() throws InterruptedException
    {
        try
        {
            lock.lock();

            // Checks if there are some line available. If none are available, waits.
            while (textQueue.peek() == null)
                notEmpty.await();

            return textQueue.remove();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the stream, and returns all
     * text lines as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return All the next text line as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextText() throws InterruptedException
    {
        try
        {
            lock.lock();

            // Checks if there are some line available. If none are available, waits.
            while (textQueue.peek() == null)
                notEmpty.await();

            // Build the text string with all text lines extracted from the stream.
            final StringBuilder sb = new StringBuilder(textQueue.remove());

            String line;
            while ((line = textQueue.poll()) != null)
                sb.append('\n').append(line);

            // Return all text lines.
            return sb.toString();
        }
        finally
        {
            lock.unlock();
        }
    }


    /**
     * Executes the code to retrieve the content of the stream.
     *
     * @throws RuntimeException If unable to read from the provided stream.
     */
    @Override
    public void run()
    {
        try
        {
            /*
            Continuously reads lines from the stream and append them to the text queue, until the whole
            stream is read. Signal all threads waiting for some line that one has been written to the stream.
            */
            String line;
            while ((line = stream.readLine()) != null)
            {
                try
                {
                    lock.lock();

                    // Notify all waiting threads that a new line has been added.
                    notEmpty.signalAll();

                    // Add the new text line extracted from the stream to the text queue.
                    textQueue.add(line);

                    //System.err.printf("Read: \"%s\".\n", line);
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read from the stream.");
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }
}
