package it.unipd.dei.rewrite;

import it.unipd.dei.conversation.Conversation;
import it.unipd.dei.conversation.Utterance;
import it.unipd.dei.exception.PythonRuntimeException;
import it.unipd.dei.external.ExternalScriptDriver;

import java.io.PrintWriter;


/**
 * The {@code PythonRewriter} class is a {@link Rewriter} that uses an external Python library.
 * The Python executable and script locations, together with the model and separator used are
 * passed as {@link String} parameters in the constructor.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class PythonRewriter implements Rewriter
{
    private final ExternalScriptDriver sd;

    /**
     * Create the {@link Rewriter}.
     *
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the Python model to use.
     * @param separator The separator string used to separate utterances.
     * @throws NullPointerException If any of the provided Python executable or script filename,
     * Python rewriting model, or separator is null.
     * @throws RuntimeException If any exception has occurred while creating the rewriter.
     */
    public PythonRewriter(String pythonFilename, String workingDirectory, String scriptFilename,
                          String model, String separator)
    {
        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided model is null.");

        if (separator == null)
            throw new NullPointerException("The provided separator is null.");

        try
        {
            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--model=%s", model),
                    String.format("--separator=%s", separator)
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
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the rewriter.\n", th);
        }
    }


    /**
     * Create the {@link Rewriter}.
     *
     * @param pythonFilename The Python executable filename.
     * @param workingDirectory The path of the Python executable working directory.
     * @param scriptFilename The Python script filename.
     * @param model The name of the rewriting model to use.
     * @param maxTokens The maximum number of tokens handled by the model.
     * @param separator The separator string used to separate utterances.
     * @throws NullPointerException If any of the provided Python executable or script filename,
     * Python rewriting model, or separator is null.
     * @throws IllegalArgumentException If the provided number of tokens is not a positive integer number.
     * @throws RuntimeException If any exception has occurred while creating the rewriter.
     */
    public PythonRewriter(String pythonFilename, String workingDirectory, String scriptFilename,
                          String model, int maxTokens, String separator)
    {
        if (pythonFilename == null)
            throw new NullPointerException("The provided Python executable is null.");

        if (scriptFilename == null)
            throw new NullPointerException("The provided script filename is null.");

        if (model == null)
            throw new NullPointerException("The provided model is null.");

        if (maxTokens < 1)
        {
            throw new IllegalArgumentException("The provided number of maximum tokens (" + maxTokens +
                    ") is not a positive integer number.");
        }

        if (separator == null)
            throw new NullPointerException("The provided separator is null.");

        try
        {
            sd = new ExternalScriptDriver(workingDirectory, pythonFilename, scriptFilename,
                    String.format("--model=%s", model),
                    String.format("--max_tokens=%d", maxTokens),
                    String.format("--separator=%s", separator)
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
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while creating the rewriter.\n", th);
        }
    }


    /**
     * Rewrites the utterance text by applying the Python rewriting model.
     *
     * @param utteranceId The ID of the utterance.
     * @param conversation The current conversation.
     * @throws NullPointerException If any of the provided utterance ID or conversation is null.
     * @throws IllegalArgumentException If the provided conversation is empty, the provided utterance
     * can not be found in the conversation, or the provided utterance is not of type
     * {@link Utterance.Type#QUERY}.
     * @throws RuntimeException If any exception has occurred while rewriting the utterance.
     */
    @Override
    public void rewrite(String utteranceId, Conversation conversation)
    {
        if (utteranceId == null)
            throw new NullPointerException("The provided utterance ID is null.");

        if (conversation == null)
            throw new NullPointerException("The provided conversation is null.");

        if (conversation.size() == 0)
            throw new IllegalArgumentException("The provided conversation is empty.");

        final Utterance utterance = conversation.getUtteranceByID(utteranceId);
        if (utterance == null)
        {
            throw new IllegalArgumentException("No utterance with ID \"" + utteranceId +
                    "\" can be found in the conversation.");
        }

        if (utterance.getType() != Utterance.Type.QUERY)
            throw new IllegalArgumentException("The provided utterance is not a query.");


        try
        {
            //Run phase - Push original
            final PrintWriter toIn = sd.getInputStream();

            // Print an empty line to Python code every time the conversation changes.
            if (conversation.size() == 1)
                toIn.print('\n');

            toIn.println(utterance.getOriginalContent());
            toIn.flush();


            /*
            Wait for Python script to output a synchronization line in error stream after having
            successfully rewritten the utterance. When reading from error stream two cases are possible:
                - Single empty line: all OK in the Python script.
                - Otherwise: an exception has been thrown in the Python script,
                             therefore raise a PythonRuntimeException.
            */
            final String errRun = sd.waitNextErrorText();
            if (!errRun.isBlank())
                throw new PythonRuntimeException(errRun);


            /*
            This waiting is needed to avoid cases where the rewritten text has not yet
            been flushed from Python code, so rewritten is null.
            */
            final String rewritten = sd.waitNextOutputLine();
            utterance.setRewrittenContent(rewritten);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while rewriting the utterance.\n", th);
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
            if (sd != null)
            {
                sd.getInputStream().close();
                sd.getOutputStream().close();
                sd.getErrorStream().close();
                sd.close();
            }
        }
        catch (Throwable ignored)
        {
        }
    }
}
