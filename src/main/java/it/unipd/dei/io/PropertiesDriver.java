package it.unipd.dei.io;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The {@code PropertiesDriver} class represent a set of properties, represented by key-value pairs.
 * It is developed to be a replacement for {@link java.util.Properties}, but it also supports
 * additional features, such as:
 * <ul>
 *     <li>
 *         No unsafe methods are present, due to {@link java.util.Properties} inheriting from
 *         {@link java.util.Hashtable}&lt;{@link Object},{@link Object}&gt;
 *     </li>
 *     <li>
 *         Direct support for different {@link Charset}s to decode the properties file
 *     </li>
 *     <li>
 *         Support for multiple-valued properties
 *     </li>
 *     <li>
 *         Support for properties substitution, enabling to replace {@code $(key)} with the value
 *         corresponding to such key
 *     </li>
 * </ul>
 *
 * This class is not thread-safe: multiple threads can share a single {@code PropertiesDriver} object,
 * but requires external synchronization when the content is changed.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class PropertiesDriver
{
    /*
    Used to find all references inside the value of a property entry.
    The format is: $(<referenced_key>).
    */
    private final static Pattern REGEX = Pattern.compile("(\\$\\(([^$()]*)\\))");

    // Stores all properties.
    private final Map<String, String> properties;


    /**
     * Create the {@code PropertiesDriver} loading the content from file, decoded using the {@code UTF-8}
     * {@link Charset}. No additional properties are provided.
     *
     * @param propertiesFilename The filename of the properties file to load.
     * @throws NullPointerException If the properties file is null.
     * @throws RuntimeException If the properties file can not be loaded successfully.
     */
    public PropertiesDriver(String propertiesFilename)
    {
        this(propertiesFilename, StandardCharsets.UTF_8, null);
    }


    /**
     * Create the {@code PropertiesDriver} loading the content from file, decoded using the {@link Charset} provided.
     * No additional properties are provided.
     *
     * @param propertiesFilename The filename of the properties file to load.
     * @param charset The charset to use to decode the properties file.
     * @throws NullPointerException If the properties file is null.
     * @throws RuntimeException If the properties file can not be loaded successfully.
     */
    public PropertiesDriver(String propertiesFilename, Charset charset)
    {
        this(propertiesFilename, charset, null);
    }


    /**
     * Create the {@code PropertiesDriver} loading the content from file, decoded using the {@link Charset} provided.
     * No additional properties are provided.
     *
     * @param propertiesFilename The filename of the properties file to load.
     * @param charset The charset to use to decode the properties file.
     * @param additionalProperties The additional properties that are included in this object, which are processed
     *                             before parsing the properties file.
     * @throws NullPointerException If the properties file is null.
     * @throws RuntimeException If the properties file can not be loaded successfully.
     */
    public PropertiesDriver(String propertiesFilename, Charset charset, Map<String, String> additionalProperties)
    {
        if (charset == null)
            charset = StandardCharsets.UTF_8;

        if (propertiesFilename == null)
            throw new NullPointerException("The provided properties file is null.");

        final Path propertiesPath = Paths.get(propertiesFilename);

        properties = new HashMap<>();

        try
        {
            // Insert the provided additional properties to this object.
            if (additionalProperties != null)
            {
                for (Map.Entry<String, String> entry : additionalProperties.entrySet())
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue();

                    properties.put(key, value);
                }
            }

            // Read the properties file using the java.util.Properties utility class.
            final Properties prop = new Properties();
            try (BufferedReader reader = Files.newBufferedReader(propertiesPath, charset))
            {
                prop.load(reader);
            }

            // Store all entries that contains a reference to another entry inside its value.
            Map<String, String> pending = new HashMap<>();

            /*
            Scan all the properties found by the Properties class, to search if there are references
            inside its value. If yes, the entry is placed in the pending map ("pending"), otherwise
            it is placed in the processed map ("properties").
            */
            for (Map.Entry<Object, Object> entry : prop.entrySet())
            {
                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();

                // Check if a reference has been found inside the value of the entry.
                final Matcher matcher = REGEX.matcher(value);
                if (!matcher.find())
                    properties.put(key, value);
                else
                    pending.put(key, value);
            }

            /*
            Continue to process, until all pending entries have been successfully resolved,
            or it has been detected that there are unresolvable references.
            */
            while (!pending.isEmpty())
            {
                /*
                Used to check if some progress has been done, or there are unresolvable
                references such as non-existent keys or cyclic references.
                */
                boolean flag = false;

                // Store the new still-pending entries.
                Map<String, String> newPending = new HashMap<>();

                for (Map.Entry<String, String> entry : pending.entrySet())
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue();

                    // Check if the value contains at least some references.
                    final Matcher matcher = REGEX.matcher(value);
                    if (!matcher.find())
                    {
                        // Set the flag to true, to indicate that some progress has been done.
                        flag = true;

                        // No more references in the value: the entry can be stored.
                        properties.put(key, value);
                    }
                    else
                    {
                        // Some reference(s) have been found: the entry must be processed.

                        // Stores the new value of the entry.
                        StringBuilder newValue = new StringBuilder();

                        // Stores the index of the last copied character of the value in the new one.
                        int lastPos = 0;

                        do
                        {
                            final String refKey = matcher.group(2);
                            final String refValue = properties.get(refKey);

                            // The index of the first and last character of the reference inside the value.
                            final int sPos = matcher.start(1);
                            final int ePos = matcher.end(1);

                            // Check if the reference key has been found.
                            if (refValue != null)
                            {
                                // Set the flag to true, to indicate that some progress has been done.
                                flag = true;

                                // Replace the referenced key with its value.
                                newValue.append(value, lastPos, sPos);
                                newValue.append(refValue);
                            }
                            else
                            {
                                /*
                                Just leave the reference in the string. Hopefully that is a reference to a still
                                pending entry, that will be resolved in the following iterations of the loop.
                                */
                                newValue.append(value, lastPos, ePos);
                            }

                            // Save the index of the last copied character of the value in the new one.
                            lastPos = ePos;
                        }
                        while (matcher.find());

                        // Copy the end of the value string to the new one.
                        newValue.append(value.substring(lastPos));

                        // Store the new entry in the new pending map.
                        newPending.put(key, newValue.toString());
                    }
                }


                // If flag is still false, it means that no progress can be done due to unresolvable references.
                if (!flag)
                    break;
                    //throw new RuntimeException("There are unresolvable references in the properties file \"" +
                    //        propertiesPath + "\"." +
                    //        Arrays.toString(pending.keySet().toArray(new String[0])));

                // Save the new pending entries.
                pending = newPending;
            }
        }
        catch (NoSuchFileException e)
        {
            throw new RuntimeException("Unable to load the provided properties file \"" +
                    propertiesPath.toAbsolutePath() + "\".");
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while loading the properties file \"" +
                    propertiesPath.toAbsolutePath() + "\".", th);
        }
    }


    /**
     * Returns the {@link Set} of all the keys in this property list.
     *
     * @return The set of properties' keys.
     */
    public Set<String> getKeySet()
    {
        return properties.keySet();
    }

    /**
     * Returns the {@link Set} view of the properties contained in this object.
     *
     * @return The set view of the properties contained in this object.
     */
    public Set<Map.Entry<String, String>> getEntrySet()
    {
        return properties.entrySet();
    }

    /**
     * Searches for a property with a specified key, and returns the value associated with it.
     * Returns {@code null} is no property can be found with the key equal to the provided one.
     *
     * @param key The key of the property to search.
     * @return The value associated with the key, or {@code null} if the key can not be found.
     */
    public String getProperty(String key)
    {
        return properties.get(key);
    }

    /**
     * Searches for a property with a specified key, and returns an array of {@link String} values
     * associated with it. Returns {@code null} is no property can be found with the key equal to
     * the provided one.
     *
     * @param key The key of the property to search.
     * @return An array of {@link String} values associated with the key, or {@code null}
     * if the key can not be found.
     */
    public String[] getProperties(String key)
    {
        // Retrieve the value associated with the provided key.
        final String value = properties.get(key);

        if (value != null)
        {
            // Split the value in tokens.
            String[] tokens = value.split(",");

            // Apply the trim function to each token, to remove useless whitespaces at both ends of the string.
            for (int i = 0; i < tokens.length; i++)
                tokens[i] = tokens[i].trim();

            return tokens;
        }
        else
            return null;
    }

    /**
     * Adds a new property in this object. If a property with the same key is already contained
     * in this object, the corresponding value is overwritten.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param value The value of the property to add.
     */
    public void setProperty(String key, String value)
    {
        value = value.trim();
        properties.put(key, value);
    }

    /**
     * Adds a new property in this object with multiple values. If a property with the same key is
     * already contained in this object, the corresponding value is overwritten.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param values The values of the property to add.
     */
    public void setProperties(String key, String... values)
    {
        if (values == null)
            properties.put(key, null);
        else
        {
            StringBuilder builder = new StringBuilder();

            for (String value : values)
                builder.append(value.trim()).append(", ");

            String value = builder.toString();
            if (value.endsWith(", "))
                value = value.substring(0, value.length() - 2);

            properties.put(key, value);
        }
    }

    /**
     * Adds a new property in this object with multiple values. If a property with the same key is
     * already contained in this object, the corresponding value is overwritten.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param values The values of the property to add.
     */
    public void setProperties(String key, Iterable<? extends String> values)
    {
        if (values == null)
            properties.put(key, null);
        else
        {
            StringBuilder builder = new StringBuilder();

            for (String value : values)
                builder.append(value.trim()).append(", ");

            String value = builder.toString();
            if (value.endsWith(", "))
                value = value.substring(0, value.length() - 2);

            properties.put(key, value);
        }
    }

    /**
     * Appends a new value for a property in this object. If a property with the same key is not
     * already contained in this object, a new property is created.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param value The value of the property to add.
     */
    public void appendProperty(String key, String value)
    {
        final String oldValue = properties.get(key);

        if (oldValue == null)
            properties.put(key, value.trim());
        else
        {
            if ((value == null) || (value.isEmpty()))
                return;

            properties.put(key, oldValue + ", " + value.trim());
        }
    }

    /**
     * Appends new values for a property in this object. If a property with the same key is not
     * already contained in this object, a new property is created.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param values The values of the property to add.
     */
    public void appendProperties(String key, String... values)
    {
        final String oldValue = properties.get(key);

        if (oldValue == null)
        {
            if (values == null)
                properties.put(key, null);
            else
                properties.put(key, "");
        }

        if (values == null)
            return;

        for (String value : values)
            appendProperty(key, value);
    }

    /**
     * Appends new values for a property in this object. If a property with the same key is not
     * already contained in this object, a new property is created.
     * Note that both the key and the value may be {@code null}.
     *
     * @param key The key of the property to add.
     * @param values The values of the property to add.
     */
    public void appendProperties(String key, Iterable<? extends String> values)
    {
        final String oldValue = properties.get(key);

        if (oldValue == null)
        {
            if (values == null)
                properties.put(key, null);
            else
                properties.put(key, "");
        }

        if (values == null)
            return;

        for (String value : values)
            appendProperty(key, value);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();

        int counter = 0;
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            counter++;

            result.append(counter).append("\t").append(entry.getKey()).append("\t\t");
            result.append(entry.getValue()).append("\n");
        }

        return result.toString();
    }
}
