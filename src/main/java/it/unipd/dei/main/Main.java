package it.unipd.dei.main;

import it.unipd.dei.pipeline.IndexPipeline;
import it.unipd.dei.pipeline.SearchPipeline;

import java.nio.charset.StandardCharsets;


/**
 * The main class of the framework, executing either the "index" or the "search" pipeline based on the command-line
 * arguments provided. The expected usage is:
 * <ul>
 *     <li>{@code -i <index .properties file>}: run the "index" pipeline</li>
 *     <li>{@code -s <search .properties file>}: run the "search" pipeline</li>
 * </ul>
 * The {@code .properties} file specify the components to use when running the selected pipeline.
 *
 * @author Marco
 */
public class Main
{
    // Disable the default constructor.
    private Main()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * The {@code main()} method of the framework.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            if (args.length != 2)
                throw new RuntimeException("Expected number of parameters: 2.");

            switch (args[0])
            {
                case "-i":
                {
                    IndexPipeline pipeline = new IndexPipeline(args[1], StandardCharsets.UTF_8);
                    pipeline.run();
                    return;
                }
                case "-s":
                {
                    SearchPipeline pipeline = new SearchPipeline(args[1], StandardCharsets.UTF_8);
                    pipeline.run();
                    return;
                }
                default:
                {
                    System.out.println("Usage: -i <index .properties file>  to run the \"index\" pipeline OR\n" +
                                       "       -s <search .properties file> to run the \"search\" pipeline.\n");
                }
            }
        }
        catch (Throwable th)
        {
            Throwable cause = th, newCause;
            while ((newCause = cause.getCause()) != null)
                cause = newCause;

            System.err.println(cause.getMessage());
            System.err.println();
            th.printStackTrace(System.err);

            System.exit(1);
        }
    }
}
