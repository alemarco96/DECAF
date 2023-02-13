package it.unipd.dei.utils;

import it.unipd.dei.io.PropertiesDriver;
import it.unipd.dei.query.AbstractBoWQueryGenerator;
import it.unipd.dei.search.BoWSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.Similarity;

import java.lang.reflect.Constructor;
import java.util.AbstractMap;
import java.util.Map;


/**
 * The {@code PropertiesUtils} is a utility class containing methods that simplifies the creation of objects starting
 * from the specification found in the input {@code .properties} file. The specification followed by this class are
 * the following:
 *
 * <p>
 * <b>Simple objects ({@link String} and primitive types variables/arrays):</b>
 * <ol>
 *     <li>
 *         <b>String object:</b>
 *         <ul>
 *             <li>{@code <prefix> = <value>}</li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code String variable = "example text";}
 *         <ul>
 *             <li>{@code <prefix> = example text}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>Primitive (int, double, ...) variable:</b>
 *         <ul>
 *             <li>{@code <prefix>.type = <primitive_type>}</li>
 *             <li>{@code <prefix> = <value>}</li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code int variable = 1234;}
 *         <ul>
 *             <li>{@code <base_key>.type = int}</li>
 *             <li>{@code <base_key> = 1234}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>String array:</b>
 *         <ul>
 *             <li>{@code <prefix>.type = java.lang.String[]}</li>
 *             <li>{@code <prefix> = [ "<v1>", "<v2>", ..., "<vN>" ]}</li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code String[] variable = new String[]{ "first", "second" };}
 *         <ul>
 *             <li>{@code <base_key>.type = java.lang.String[]}</li>
 *             <li>{@code <base_key> = [ "first", "second" ]}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>Primitive (int, double, ...) array:</b>
 *         <ul>
 *             <li>{@code <prefix>.type = <primitive_type>[]}</li>
 *             <li>{@code <prefix> = [ <v1>, <v2>, ..., <vN> ]}</li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code long[][] variable = new long[2]; variable[0] = new long[]{ 1L, 2L, 3L };
 *         variable[2] = new long[]{ 4L, 5L, 6L };}
 *         <ul>
 *             <li>{@code <base_key>.type = long[][]}</li>
 *             <li>{@code <base_key> = [ [ 1, 2, 3 ], [ 4, 5, 6 ] }</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>
 * <b>Complex objects (objects requiring to call a constructor):</b>
 * <ol start="5">
 *     <li>
 *         <b>Object created using the default constructor with no parameters:</b>
 *         <ul>
 *             <li>{@code <prefix>.type = <base_type>}</li>
 *             <li>{@code <prefix> = <id>}</li>
 *             <li>{@code <prefix>.<id>.class = <class_type>}</li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code Interface variable = new Class();}
 *         <ul>
 *             <li>{@code <prefix>.type = package.subpackage.etc.Interface}</li>
 *             <li>{@code <prefix> = x}</li>
 *             <li>{@code <prefix>.x.class = package.subpackage.etc.Class}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>Object created using a constructor with parameters:</b>
 *         <ul>
 *             <li>{@code <prefix>.type = <base_type>}</li>
 *             <li>{@code <prefix> = <id>}</li>
 *             <li>{@code <prefix>.<id>.class = <class_type>}</li>
 *             <li>{@code <prefix>.<id>.params = <param1>, <param2>, ..., <paramN>}</li>
 *             <li>{@code Definition of all parameters using any of the rules in this list. }</li>
 *             <li>
 *                 {@code To pass a null object, the parameter type must be defined while its value
 *                 must be omitted. }
 *             </li>
 *         </ul>
 *
 *         <p>
 *         Example: {@code Writer variable = new PrintWriter(new File("/path/to/file.txt"), "UTF-8");}
 *         <ul>
 *             <li>{@code <prefix>.type = java.io.Writer}</li>
 *             <li>{@code <prefix> = x}</li>
 *             <li>{@code <prefix>.x.class = java.io.PrintWriter}</li>
 *             <li>{@code <prefix>.x.params = file, charset}</li>
 *             <li>{@code <prefix>.x.params.file.type = java.io.File}</li>
 *             <li>{@code <prefix>.x.params.file = y}</li>
 *             <li>{@code <prefix>.x.params.file.y.class = java.io.File}</li>
 *             <li>{@code <prefix>.x.params.file.y.params = filename}</li>
 *             <li>{@code <prefix>.x.params.file.y.params.filename = /path/to/file.txt}</li>
 *             <li>{@code <prefix>.x.params.charset.type = java.lang.String}</li>
 *             <li>{@code <prefix>.x.params.charset = UTF-8}</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>
 * Note that, for all {@code .type} and {@code .class} tags, it is mandatory that the Java type must be specified with
 * its fully-quantified class name. For example, {@link java.io.File} must be specified as {@code java.io.File}.
 * This rule must also be followed for classing belonging to the {@code java.lang} package, differently from
 * what happens in Java code.
 *
 * <p>
 * In the definition of complex variables, the {@code <id>} and {@code <param>} are any valid identifier (any string
 * without a dot). The {@code <id>} enables to define multiple alternatives for that field: by only changes
 * the value {@code <id>} in this line, a different object is instantiated at runtime. Consider this example:
 * while defining the {@link BoWSearcher} on the {@code .properties} file, a similarity
 * function must be specified. Using this feature, it is possible to quickly change it without rewriting again and
 * again the same lines on the {@code .properties} file:
 * <ul>
 *     <li>{@code <prefix>.searcher = Lucene}</li>
 *     <li>{@code <prefix>.searcher.Lucene.class = it.unipd.dei.search.LuceneSearch}</li>
 *     <li>{@code ...}</li>
 *     <li>
 *         {@code <prefix>.searcher.Lucene.params.similarity.type = org.apache.lucene.search.similarities.Similarity}
 *     </li>
 *     <li>{@code #Comment with '#' all but one of this lines to select which value to use.}</li>
 *     <li>{@code <prefix>.searcher.Lucene.params.similarity = BM25}</li>
 *     <li>{@code <prefix>.searcher.Lucene.params.similarity = Dirichlet}</li>
 *     <li>
 *         {@code <prefix>.searcher.Lucene.params.similarity.BM25.class =
 *         org.apache.lucene.search.similarities.BM25Similarity}
 *     </li>
 *     <li>{@code ...}</li>
 *     <li>
 *         {@code <prefix>.searcher.Lucene.params.similarity.Dirichlet.class =
 *         org.apache.lucene.search.similarities.LMDirichletSimilarity}
 *     </li>
 *     <li>{@code ...}</li>
 * </ul>
 *
 * <p>
 * It should be noted that, for cases 5 and 6, the {@code .type} of the external object is ignored if the calling Java
 * code specifies it using {@link PropertiesUtils#retrieveObject(PropertiesDriver, String, Class)}, which also takes
 * the third parameter of type {@link Class}. In all cases, the {@code .class} type must be assignable to type
 * {@code .type}, otherwise an exception will be thrown.
 *
 * <p>
 * Note that the {@code .type} of an object's parameter enables to create it as an instance of a subtype
 * w.r.t the type specified in the constructor signature. For example, the LuceneSearcher constructor
 * {@link BoWSearcher#BoWSearcher(String, Analyzer, Similarity,AbstractBoWQueryGenerator, int, int)} requires to
 * specify a parameter of type {@link Analyzer}. By using the {@code .class} tag, it is possible to instantiate
 * any of its subclasses like {@link org.apache.lucene.analysis.en.EnglishAnalyzer}.
 *
 * <p>
 * If the need of instantiating any of the Java collections, such as {@link java.util.List}, {@link Map}, ..., should
 * arise, the {@code utils} package contains utility classes to do so. Please check {@link ListBuilder},
 * {@link SetBuilder} and {@link MapBuilder} documentation for more details on their usage.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class PropertiesUtils
{
    // Disable the default constructor.
    private PropertiesUtils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Build an object using reflection, starting from the specified key. The data needed is taken from the
     * provided {@link PropertiesDriver} object, in which all data of the {@code .properties} file is stored.
     *
     * @param <T> The generic type used by the type class parameter.
     * @param driver The properties driver used to parse the {@code .properties} file.
     * @param key The key used in the {@code .properties} file for the object to build.
     * @param typeClass The {@link Class} object representing the expected type of the object to build.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     * @return The object requested, built using reflection.
     */
    @SuppressWarnings("unchecked")
    public static <T> T retrieveObject(PropertiesDriver driver, String key, Class<T> typeClass)
    {
        if (driver == null)
            throw new NullPointerException("The provided properties driver is null.");

        if (key == null)
            throw new NullPointerException("The provided key is null.");

        if (typeClass == null)
            throw new NullPointerException("The provided class is null.");

        try
        {
            // Retrieve the class name for the object to create.
            final String classStr = driver.getProperty(key + ".class");
            if (classStr == null)
                throw new RuntimeException(String.format("The class for \"%s\" is null.", key));

            // Obtain the Class object corresponding to the class name for the object to create.
            final Class<?> classClass = ReflectionUtils.getTypeByName(classStr);

            // Check if the class of the object to create has the provided typeClass as a super class.
            if (!typeClass.isAssignableFrom(classClass))
            {
                throw new RuntimeException(String.format("The class type \"%s\" is not assignable to type \"%s\".",
                        classStr, typeClass.getName()));
            }

            // Check if there are constructor parameters that must be parsed to create the object.
            final String[] paramsStr = driver.getProperties(key + ".params");
            if ((paramsStr == null) || (paramsStr.length < 1))
            {
                // No parameters: invoke the constructor with no parameters.
                final Constructor<?> constructor = classClass.getConstructor();
                return (T) constructor.newInstance();
            }

            // There are at least 1 constructor parameter(s): parse all of them.
            final Class<?>[] paramsClasses = new Class<?>[paramsStr.length];
            final Object[] paramsValues = new Object[paramsStr.length];
            for (int i = 0; i < paramsStr.length; i++)
            {
                // Retrieve the i-th constructor parameter and add them to the appropriate array.
                final Map.Entry<Class<?>, Object> entry = retrieveObject(driver,
                        key + ".params." + paramsStr[i]);
                paramsClasses[i] = entry.getKey();
                paramsValues[i] = entry.getValue();
            }

            // Finally invoke the right constructor, also passing the values corresponding to each parameter.
            final Constructor<?> constructor = classClass.getConstructor(paramsClasses);
            return (T) constructor.newInstance(paramsValues);
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }


    /**
     * Build an object using reflection, starting from the specified key. The data needed is taken from the
     * provided {@link PropertiesDriver} object, in which all data of the {@code .properties} file is stored.
     *
     * @param driver The properties driver used to parse the {@code .properties} file.
     * @param key The key used in the {@code .properties} file for the object to build.
     * @throws NullPointerException If any of the provided parameters is null.
     * @throws RuntimeException If an exception has occurred while performing the operation.
     * @return The object requested, built using reflection.
     */
    public static Map.Entry<Class<?>, Object> retrieveObject(PropertiesDriver driver, String key)
    {
        if (driver == null)
            throw new NullPointerException("The provided properties driver is null.");

        if (key == null)
            throw new NullPointerException("The provided key is null.");

        try
        {
            // Retrieve the class name for the object to create.
            final String valueStr = driver.getProperty(key);
            if (valueStr == null)
            {
                // No class name: return the (type, null) pair.
                final String typeStr = driver.getProperty(key + ".type");
                final Class<?> typeClass = (typeStr == null) ? String.class : ReflectionUtils.getTypeByName(typeStr);

                return new AbstractMap.SimpleImmutableEntry<>(typeClass, null);
            }

            /*
            Retrieve, if present, the class name associated with the value found before. When not null, it means
            that there is a nested object that should be created before continuing building the current one.
            */
            final String classStr = driver.getProperty(key + "." + valueStr + ".class");
            if (classStr == null)
            {
                // No class name: The value found before is the final value of the object, return (type, value) pair.
                final String typeStr = driver.getProperty(key + ".type");
                final Class<?> typeClass = (typeStr == null) ? String.class : ReflectionUtils.getTypeByName(typeStr);

                return new AbstractMap.SimpleImmutableEntry<>(typeClass, ReflectionUtils.getValue(valueStr, typeClass));
            }

            // Retrieve the type of the nested object.
            final String typeStr = driver.getProperty(key + ".type");
            final Class<?> typeClass = ReflectionUtils.getTypeByName(typeStr);

            /*
            Return a (type, value) pair, where value is the nested object retrieved by a recursive call to this method.
            */
            return new AbstractMap.SimpleImmutableEntry<>(typeClass, retrieveObject(driver,
                    key + "." + valueStr, typeClass));
        }
        catch (Throwable th)
        {
            throw new RuntimeException("An exception has occurred while performing the operation.\n", th);
        }
    }
}
