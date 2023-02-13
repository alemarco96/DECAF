package it.unipd.dei.utils;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code Reflection} class provides static utility methods for reflection.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class ReflectionUtils
{
    // Regular expression used to parse the type name, looking for the number of [] present in case of array type.
    private static final Pattern REGEX = Pattern.compile("([A-Za-z0-9_.]+)((?:\\[])*)");

    // Object of type Gson, used to deserialize non-primitive and non-string objects.
    private static final Gson GSON = new Gson();

    // Mapping of all primitive types with their class. Used to create variables of primitive types using reflection.
    private static final Map<String, Class<?>> PRIMITIVE_TYPES_CLASS = new HashMap<>();

    /*
    Mapping of all primitive types with their matching class name. Used to create array variables of
    primitive types using reflection.
    */
    private static final Map<String, String> PRIMITIVE_TYPES_ARRAY = new HashMap<>();

    // Adds all the data needed ho handle primitive types using reflection.
    static
    {
        PRIMITIVE_TYPES_CLASS.put("boolean", boolean.class);
        PRIMITIVE_TYPES_ARRAY.put("boolean", "Z");

        PRIMITIVE_TYPES_CLASS.put("byte", byte.class);
        PRIMITIVE_TYPES_ARRAY.put("byte", "B");

        PRIMITIVE_TYPES_CLASS.put("char", char.class);
        PRIMITIVE_TYPES_ARRAY.put("char", "C");

        PRIMITIVE_TYPES_CLASS.put("short", short.class);
        PRIMITIVE_TYPES_ARRAY.put("short", "S");

        PRIMITIVE_TYPES_CLASS.put("int", int.class);
        PRIMITIVE_TYPES_ARRAY.put("int", "I");

        PRIMITIVE_TYPES_CLASS.put("long", long.class);
        PRIMITIVE_TYPES_ARRAY.put("long", "J");

        PRIMITIVE_TYPES_CLASS.put("float", float.class);
        PRIMITIVE_TYPES_ARRAY.put("float", "F");

        PRIMITIVE_TYPES_CLASS.put("double", double.class);
        PRIMITIVE_TYPES_ARRAY.put("double", "D");
    }


    // Disable the default constructor.
    private ReflectionUtils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Returns a {@link Class} object corresponding to the provided type name.
     *
     * @param typeName The name of the type.
     * @throws NullPointerException If the provided type name is null.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     * @return The class corresponding to the type name.
     */
    @NotNull
    public static Class<?> getTypeByName(String typeName)
    {
        if (typeName == null)
            throw new NullPointerException("The provided type name is null.");

        try
        {
            // Check the provided type name against the regex.
            final Matcher matcher = REGEX.matcher(typeName);
            if (!matcher.find())
                throw new RuntimeException("The provided type name \"" + typeName + "\" is invalid.");


            // The type name of the object or the array elements.
            final String baseType = matcher.group(1);
            // The number of "[]" after the base type. If 0, the provided type is not an array.
            final int numArrays = matcher.group(2).length() / 2;

            // Check if the provided type name is an array.
            if (numArrays == 0)
            {
                // The provided type name is not an array.
                try
                {
                    Class<?> cl = PRIMITIVE_TYPES_CLASS.get(typeName);

                    return (cl != null) ? cl : Class.forName(typeName);
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("The provided type name \"" + typeName + "\" is non existent.");
                }
            }
            else
            {
                // The provided type name is an array.
                try
                {
                    return Class.forName("[".repeat(numArrays) +
                            PRIMITIVE_TYPES_ARRAY.getOrDefault(baseType, "L" + baseType + ";"));
                } catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("The provided type name \"" + typeName + "\" is non existent.");
                }
            }
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }


    /**
     * Deserializes the string value in an object of type corresponding to the class parameter.
     * If the type is not a primitive one ({@code boolean}, {@code byte}, {@code char}, {@code short},
     * {@code int}, {@code long}, {@code float}, {@code double}) or {@link String},
     * the expected format for the value is the same as used by JSON.
     * <p>
     * If the string value is {@code null}, returns {@code null}.
     * </p>
     *
     * @param <T> The type of the value.
     * @param valueData The serialized representation of the value.
     * @param classOfT The class corresponding to the type of the value.
     * @throws NullPointerException If the provided class of the value is null.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     * @return The deserialized value, or {@code null} if the string value is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(String valueData, Class<T> classOfT)
    {
        if (valueData == null)
            return null;

        if (classOfT == null)
            throw new NullPointerException("The provided class is null.");

        try
        {
            /*
            Special handling for strings, in order to avoid all processing required by JSON
            (such as: wrapping the whole string between "",
            handling some characters by prepending them with '\', ...).
            */
            if (classOfT.equals(String.class))
                return (T) valueData;
            else
                // Handles in the correct way all primitive types.
                return GSON.fromJson(valueData, classOfT);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }

    /**
     * Creates an object using the constructor, provided as the entry's key,
     * passing the values provided as the entry's value.
     *
     * @param <T> The type of the object.
     * @param entry The data needed to create the object.
     * @return The newly-created object.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createObject(Map.Entry<Constructor<?>, Object[]> entry)
    {
        if (entry == null)
            throw new NullPointerException("The provided constructor data is null.");

        try
        {
            if (entry.getValue() == null)
                return (T) entry.getKey().newInstance();
            else
                return (T) entry.getKey().newInstance(entry.getValue());
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }
}
