package it.unipd.dei.corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * The {@code MSMARCOv1Utils} utility class contains functions for common pre-processing done to the
 * MS-MARCOv1 document corpus.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class MSMARCOv1Utils
{
    // Disable the default constructor.
    private MSMARCOv1Utils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Find the set of all IDs of MS-MARCOv1 document corpus that must be discarded during indexing phase.
     *
     * @param duplicateFilename The filename containing all duplicate data.
     * @throws NullPointerException If the provided filename is null.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     * @return The set of IDs to discard.
     */
    public static Set<String> findDuplicateIDs(String duplicateFilename)
    {
        if (duplicateFilename == null)
            throw new NullPointerException("The provided duplicate IDs file is null.");

        try
        {
            final Path duplicatePath = Paths.get(duplicateFilename);

            // Read all duplicate file into a list of String.
            List<String> lines;
            try
            {
                lines = Files.readAllLines(duplicatePath);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to read the provided duplicate IDs file \"" +
                        duplicatePath.toAbsolutePath() + "\".");
            }


            /*
            Process the List<String> read from the duplicate file, in order to find the ID of
            all documents to discard.
            */
            Set<String> result = new TreeSet<>();
            for (String line : lines)
            {
                // Split the line across the ':'. If none is found, raise an exception.
                final int pos = line.indexOf(':');
                if (pos == -1)
                    throw new RuntimeException("Separator ':' not found.");

                /*
                Extract, at the right of the ':' character, the comma-separated list of all aliases of the
                document whose ID is on the left of the ':' character. All documents in the list must be
                discarded during indexing phase.
                */
                final String aliases = line.substring(pos + 1);
                if (aliases.isBlank())
                    continue;

                result.addAll(List.of(aliases.split(",")));
            }

            return result;
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }
}
