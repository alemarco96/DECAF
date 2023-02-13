package it.unipd.dei.exception;

import it.unipd.dei.external.OutputStreamHandler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The {@code PythonRuntimeException} is the class of exceptions thrown every time an exception
 * in a Python script has been raised. It extends {@link RuntimeException}, therefore it is an
 * unchecked exception.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class PythonRuntimeException extends RuntimeException
{
    /**
     * Read the message of the exception raised in external Python code from the log file.
     * This ensures to output the entire content of it, as it is possible that only a fraction of the message
     * is retrieved from the {@link OutputStreamHandler} dedicated to the error stream of the Python process.
     *
     * @return The full error message read from the log file, if it can be successfully retrieved.
     * Otherwise, the provided one is returned.
     */
    private static String readMessageFromLogFile(String message)
    {
        try
        {
            final String errorsFilename = System.getenv("DECAF_PYTHON_ERROR_LOG_FILENAME");

            // Return the provided message if the environmental variable has not been set.
            if (errorsFilename == null)
                return message;

            final Path errorsPath = Paths.get(errorsFilename);

            // Read the whole errors log file into a string, and return it.
            return Files.readString(errorsPath, StandardCharsets.UTF_8);
        }
        catch (Throwable th)
        {
            return message;
        }
    }



    /**
     * Constructs a new exception with {@code null} as its detail message and cause.
     */
    public PythonRuntimeException()
    {
        super();
    }

    /**
     * Constructs a nex exception with the provided detail message and {@code null} cause.
     *
     * @param message The detail message explaining what happened.
     */
    public PythonRuntimeException(String message)
    {
        super(readMessageFromLogFile(message));
    }

    /**
     * Constructs a nex exception with the {@code null} detail message and the provided cause.
     *
     * @param cause The cause of this exception.
     */
    public PythonRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a nex exception with the provided detail message and cause.
     *
     * @param message The detail message explaining what happened.
     * @param cause The cause of this exception.
     */
    public PythonRuntimeException(String message, Throwable cause)
    {
        super(readMessageFromLogFile(message), cause);
    }
}
