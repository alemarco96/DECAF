package it.unipd.dei.external;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.unipd.dei.exception.PythonRuntimeException;

import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * The {@code ExternalScriptDriver} class is used to launch an external process and manage its streams.
 * There are convenient methods, such as {@link ExternalScriptDriver#getInputStream()},
 * {@link ExternalScriptDriver#getOutputStream()} and {@link ExternalScriptDriver#getErrorStream()},
 * to obtain the streams of the external process as a {@link PrintWriter} or {@link BufferedReader} object.
 * For both output and error streams, it is possible to access them in a streaming fashion using the
 * {@link ExternalScriptDriver#hasNextOutputLine()} and {@link ExternalScriptDriver#nextOutputLine()} methods.
 * There are convenient methods {@link ExternalScriptDriver#waitNextOutputLine()} to suspend the
 * current thread while waiting for some text line being available.
 * It is also possible to retrieve the exit code of the external process.
 *
 * <p>
 *
 * The Java and the external processes can be synchronized by communicating using the input, output and error
 * streams of the external one. In all components of the framework that utilize this class, this task is performed
 * with the following procedure:
 * <ul>
 *     <li>
 *         <b>Java process</b>: It waits the external process by waiting on the error stream. The external process
 *         outputs a single empty line if everything went as expected, otherwise the full stack trace of the
 *         exception. In this case, it is utilized as the message of a {@link PythonRuntimeException}
 *         that the Java code raises.
 *     </li>
 *     <li>
 *         <b>External process</b>: It waits the Java process by waiting on the input stream. The Java process
 *         outputs the data to be processed by the external process on the input stream. When no more data remains,
 *         the Java code closes the input stream of the external process, that will autonomously proceed
 *         to perform its clean-up routine and finish.
 *     </li>
 * </ul>
 *
 * <p>
 *
 * In details, the typical lifecycle of the Java and of the external processes used in this framework is:
 * <ul>
 *     <li>
 *         <b>Initialization phase:</b>
 *         <ol>
 *             <li>The Java process starts its execution.</li>
 *             <li>
 *                 The Java process creates an instance of this class, possibly passing some command-line
 *                 arguments to it.
 *             </li>
 *             <li>While the external process starts its execution, the Java process waits on the error stream.</li>
 *         </ol>
 *     </li>
 *     <li>
 *         <b>Main phase:</b>
 *         <ol start="4">
 *             <li>
 *                 The external process completes its initialization phase, and outputs a single empty line on the error
 *                 stream. While it starts its main phase, and immediately waits on the input stream, the Java process
 *                 resumes its execution.
 *             </li>
 *             <li>
 *                 The Java process outputs some data to be processed on the input stream and waits on the error stream.
 *                 Meanwhile, the external process resumes its execution.
 *             </li>
 *             <li>
 *                 The external process prints the output to the output stream, then puts a single empty line on the
 *                 error stream for synchronization purposes. The Java process resumes its execution and reads the
 *                 results computed by the external process from its output stream.
 *             </li>
 *             <li>Steps 4, 5 and 6 are repeated until no more data has to be processed.</li>
 *         </ol>
 *     </li>
 *     <li>
 *         <b>Clean-up phase:</b>
 *         <ol start="8">
 *             <li>
 *                 The Java process closes the input stream of the external process. This triggers the external process
 *                 to start its clean-up phase and to finish when done. No further synchronization is performed,
 *                 therefore the Java processes remains unaffected by any exception triggered on the external process
 *                 from this point onwards.
 *             </li>
 *             <li>The external process has finished its execution.</li>
 *         </ol>
 *     </li>
 * </ul>
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class ExternalScriptDriver implements AutoCloseable
{
    // The external process.
    private final Process process;

    // The handler for the output stream of the external process.
    private final OutputStreamHandler outHandler;

    // The handler for the error stream of the external process.
    private final OutputStreamHandler errHandler;

    /**
     * Creates the {@code ExternalScriptDriver}, immediately launching the external process
     * created using the provided working directory and command. Note that the working
     * directory may be {@code null}: in this case, the working directory of the Java
     * process will be used instead.
     *
     * @param workingDirectory The working directory for the external process.
     * @param command The command to launch the external process.
     * @throws NullPointerException If the command is null.
     * @throws IllegalArgumentException If the command is empty (length of 0).
     * @throws RuntimeException If the external process can not be created successfully.
     */
    public ExternalScriptDriver(String workingDirectory, String... command)
    {
        if (command == null)
            throw new NullPointerException("The provided command is null.");

        if (command.length == 0)
            throw new IllegalArgumentException("The provided command is empty.");

        try
        {
            // Creates the external process using the provided command and executes it immediately.
            final ProcessBuilder pb = new ProcessBuilder(command);
            if ((workingDirectory != null) && (!workingDirectory.isBlank()))
                pb.directory(new File(workingDirectory));
            process = pb.start();

            // Creates the handlers for the output and error streams of the external process.
            outHandler = new OutputStreamHandler(process.getInputStream(), StandardCharsets.UTF_8);
            errHandler = new OutputStreamHandler(process.getErrorStream(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to create the external process \"" +
                    String.join(" ", command) + "\".", e);
        }
    }

    /**
     * Returns the input stream of the external process as a {@link PrintWriter} object.
     * <p>
     * Implementation detail: The returned stream should NOT be closed in the caller's code,
     * until all input for the external process has been sent.
     * </p>
     *
     * @return The input stream of the external process.
     */
    @NotNull
    public PrintWriter getInputStream()
    {
        if (!process.isAlive())
            throw new RuntimeException("The external process is not alive.");

        return new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Returns the output stream of the external process as a {@link BufferedReader} object.
     * <p>
     * Implementation detail: The returned stream must NOT be closed in the caller's code.
     * </p>
     *
     * @return The output stream of the external process.
     */
    @NotNull
    public BufferedReader getOutputStream()
    {
        if (!process.isAlive())
            throw new RuntimeException("The external process is not alive.");

        return new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Returns the error stream of the external process as a {@link BufferedReader} object.
     * <p>
     * Implementation detail: The returned stream must NOT be closed in the caller's code.
     * </p>
     *
     * @return The error stream of the external process.
     */
    @NotNull
    public BufferedReader getErrorStream()
    {
        if (!process.isAlive())
            throw new RuntimeException("The external process is not alive.");

        return new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
    }

    /**
     * Checks if a new text line is available from the output stream of the external process.
     *
     * @return {@code true} if a new text line is available, {@code false} otherwise.
     */
    public boolean hasNextOutputLine()
    {
        return outHandler.hasNextLine();
    }

    /**
     * Checks if a new text line is available from the error stream of the external process.
     *
     * @return {@code true} if a new text line is available, {@code false} otherwise.
     */
    public boolean hasNextErrorLine()
    {
        return errHandler.hasNextLine();
    }

    /**
     * Returns the next text line extracted from the output stream of the external process,
     * or {@code null} if one is not available.
     *
     * @return The next text line as a {@link String} object, {@code null} if one is not available.
     */
    @Nullable
    public String nextOutputLine()
    {
        return outHandler.nextLine();
    }

    /**
     * Returns the next text line extracted from the error stream of the external process,
     * or {@code null} if one is not available.
     *
     * @return The next text line as a {@link String} object, {@code null} if one is not available.
     */
    @Nullable
    public String nextErrorLine()
    {
        return errHandler.nextLine();
    }

    /**
     * Returns all the next text lines extracted from the output stream of the external process,
     * or {@code null} if one is not available.
     *
     * @return All the next text lines as a {@link String} object, {@code null} if one is not available.
     */
    @Nullable
    public String nextOutputText()
    {
        return outHandler.nextText();
    }

    /**
     * Returns all the next text lines extracted from the error stream of the external process,
     * or {@code null} if one is not available.
     *
     * @return All the next text lines as a {@link String} object, {@code null} if one is not available.
     */
    @Nullable
    public String nextErrorText()
    {
        return errHandler.nextText();
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the output stream,
     * and returns it as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return All the next text lines as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextOutputLine() throws InterruptedException
    {
        return outHandler.waitNextLine();
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the error stream,
     * and returns it as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return All the next text lines as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextErrorLine() throws InterruptedException
    {
        return errHandler.waitNextLine();
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the output stream, and returns
     * all the text lines as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return All the next text lines as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextOutputText() throws InterruptedException
    {
        return outHandler.waitNextText();
    }

    /**
     * Waits, if necessary, that a text line has been extracted from the error stream, and returns
     * all the text lines as a {@link String} object. Note that the returned value can never {@code null}.
     *
     * @return All the next text lines as a {@link String} object. It can never be {@code null}.
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @NotNull
    public String waitNextErrorText() throws InterruptedException
    {
        return errHandler.waitNextText();
    }

    /**
     * Waits for the external process to end, if necessary, and returns its exit code.
     *
     * @return The exit code of the external process.
     * @throws RuntimeException If some exception arises while waiting for the external process to end.
     */
    public int waitExitCode()
    {
        try
        {
            return process.waitFor();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("An exception has occurred while waiting for the external " +
                    "process to end.", e);
        }
    }

    /**
     * Returns a {@link Process} object of the external process.
     *
     * @return The external process.
     */
    @NotNull
    public Process getProcess()
    {
        return process;
    }


    /**
     * Close this object and release the allocated resources, waiting for the external process to end.
     *
     * @throws RuntimeException If some exception arises while waiting for the external process to end.
     */
    @Override
    public void close()
    {
        try
        {
            process.waitFor();
        }
        catch (Throwable ignored)
        {
        }

        try
        {
            outHandler.join();
        }
        catch (Throwable ignored)
        {
        }

        try
        {
            errHandler.join();
        }
        catch (Throwable ignored)
        {
        }
    }
}
