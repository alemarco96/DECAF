package it.unipd.dei.utils;

import java.util.*;


/**
 * Utility class for constructing and populating (with up to 16 elements) a {@link Map} directly from constructor.
 * It is designed to fulfill the need to pass {@link Map} objects while specifying a {@code .properties} file.
 * To use this class inside a {@code .properties file}, please use:
 * <ul>
 *     <li>{@code <key>.type = java.util.Map}</li>
 *     <li>{@code <key>.class = it.unipd.dei.utils.MapBuilder}</li>
 *     <li>{@code <key>.params = k1, v1, k2, v2, ..., kN, vN}</li>
 *     <li>{@code <key>.params.k1.type = package.subpackage.etc.MapKeyType}</li>
 *     <li>{@code <key>.params.k1.class = package.subpackage.etc.ActualKeyType}</li>
 *     <li>{@code ...}</li>
 *     <li>{@code <key>.params.v1.type = package.subpackage.etc.MapValueType}</li>
 *     <li>{@code <key>.params.v1.class = package.subpackage.etc.ActualValueType}</li>
 *     <li>{@code ...}</li>
 *     <li>{@code <definition of all elements of the map...>}</li>
 * </ul>
 *
 * @param <K> The type for keys.
 * @param <V> The type for values.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class MapBuilder<K, V> extends TreeMap<K, V> implements Map<K, V>, NavigableMap<K, V>, SortedMap<K, V>
{
    /**
     * Create an empty map.
     */
    public MapBuilder()
    {
        super();
    }

    /**
     * Create a copy of the provided map.
     *
     * @param map The map to copy.
     */
    public MapBuilder(Map<? extends K, ? extends V> map)
    {
        super(map);
    }

    /**
     * Create a new map with a single entry.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     */
    public MapBuilder(K k1, V v1)
    {
        super(Map.of(k1, v1));
    }

    /**
     * Create a new map with 2 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2)
    {
        super(Map.of(k1, v1, k2, v2));
    }

    /**
     * Create a new map with 3 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3));
    }

    /**
     * Create a new map with 4 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    /**
     * Create a new map with 5 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    /**
     * Create a new map with 6 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
    }

    /**
     * Create a new map with 7 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
    }

    /**
     * Create a new map with 8 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
    }

    /**
     * Create a new map with 9 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
    }

    /**
     * Create a new map with 10 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
    }

    /**
     * Create a new map with 11 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
    }

    /**
     * Create a new map with 12 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     * @param k12 The #12 key.
     * @param v12 The #12 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
        put(k12, v12);
    }

    /**
     * Create a new map with 13 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     * @param k12 The #12 key.
     * @param v12 The #12 value.
     * @param k13 The #13 key.
     * @param v13 The #13 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
        put(k12, v12);
        put(k13, v13);
    }

    /**
     * Create a new map with 14 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     * @param k12 The #12 key.
     * @param v12 The #12 value.
     * @param k13 The #13 key.
     * @param v13 The #13 value.
     * @param k14 The #14 key.
     * @param v14 The #14 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
        put(k12, v12);
        put(k13, v13);
        put(k14, v14);
    }

    /**
     * Create a new map with 15 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     * @param k12 The #12 key.
     * @param v12 The #12 value.
     * @param k13 The #13 key.
     * @param v13 The #13 value.
     * @param k14 The #14 key.
     * @param v14 The #14 value.
     * @param k15 The #15 key.
     * @param v15 The #15 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14, K k15, V v15)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
        put(k12, v12);
        put(k13, v13);
        put(k14, v14);
        put(k15, v15);
    }

    /**
     * Create a new map with 16 entries.
     *
     * @param k1 The #1 key.
     * @param v1 The #1 value.
     * @param k2 The #2 key.
     * @param v2 The #2 value.
     * @param k3 The #3 key.
     * @param v3 The #3 value.
     * @param k4 The #4 key.
     * @param v4 The #4 value.
     * @param k5 The #5 key.
     * @param v5 The #5 value.
     * @param k6 The #6 key.
     * @param v6 The #6 value.
     * @param k7 The #7 key.
     * @param v7 The #7 value.
     * @param k8 The #8 key.
     * @param v8 The #8 value.
     * @param k9 The #9 key.
     * @param v9 The #9 value.
     * @param k10 The #10 key.
     * @param v10 The #10 value.
     * @param k11 The #11 key.
     * @param v11 The #11 value.
     * @param k12 The #12 key.
     * @param v12 The #12 value.
     * @param k13 The #13 key.
     * @param v13 The #13 value.
     * @param k14 The #14 key.
     * @param v14 The #14 value.
     * @param k15 The #15 key.
     * @param v15 The #15 value.
     * @param k16 The #16 key.
     * @param v16 The #16 value.
     */
    public MapBuilder(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8,
                      K k9, V v9, K k10, V v10, K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14, K k15, V v15, K k16, V v16)
    {
        super(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        put(k11, v11);
        put(k12, v12);
        put(k13, v13);
        put(k14, v14);
        put(k15, v15);
        put(k16, v16);
    }

    /**
     * Create a new map with a single entry.
     *
     * @param e1 The #1 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1)
    {
        super(Map.ofEntries(e1));
    }

    /**
     * Create a new map with 2 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2)
    {
        super(Map.ofEntries(e1, e2));
    }

    /**
     * Create a new map with 3 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3)
    {
        super(Map.ofEntries(e1, e2, e3));
    }

    /**
     * Create a new map with 4 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4)
    {
        super(Map.ofEntries(e1, e2, e3, e4));
    }

    /**
     * Create a new map with 5 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5));
    }

    /**
     * Create a new map with 6 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6));
    }

    /**
     * Create a new map with 7 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7));
    }

    /**
     * Create a new map with 8 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8));
    }

    /**
     * Create a new map with 9 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9));
    }

    /**
     * Create a new map with 10 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
    }

    /**
     * Create a new map with 11 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11));
    }

    /**
     * Create a new map with 12 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     * @param e12 The #12 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11, Map.Entry<? extends K, ? extends V> e12)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));
    }

    /**
     * Create a new map with 13 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     * @param e12 The #12 entry.
     * @param e13 The #13 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11, Map.Entry<? extends K, ? extends V> e12,
                      Map.Entry<? extends K, ? extends V> e13)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13));
    }

    /**
     * Create a new map with 14 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     * @param e12 The #12 entry.
     * @param e13 The #13 entry.
     * @param e14 The #14 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11, Map.Entry<? extends K, ? extends V> e12,
                      Map.Entry<? extends K, ? extends V> e13, Map.Entry<? extends K, ? extends V> e14)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14));
    }

    /**
     * Create a new map with 15 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     * @param e12 The #12 entry.
     * @param e13 The #13 entry.
     * @param e14 The #14 entry.
     * @param e15 The #15 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11, Map.Entry<? extends K, ? extends V> e12,
                      Map.Entry<? extends K, ? extends V> e13, Map.Entry<? extends K, ? extends V> e14,
                      Map.Entry<? extends K, ? extends V> e15)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15));
    }

    /**
     * Create a new map with 16 entries.
     *
     * @param e1 The #1 entry.
     * @param e2 The #2 entry.
     * @param e3 The #3 entry.
     * @param e4 The #4 entry.
     * @param e5 The #5 entry.
     * @param e6 The #6 entry.
     * @param e7 The #7 entry.
     * @param e8 The #8 entry.
     * @param e9 The #9 entry.
     * @param e10 The #10 entry.
     * @param e11 The #11 entry.
     * @param e12 The #12 entry.
     * @param e13 The #13 entry.
     * @param e14 The #14 entry.
     * @param e15 The #15 entry.
     * @param e16 The #16 entry.
     */
    public MapBuilder(Map.Entry<? extends K, ? extends V> e1, Map.Entry<? extends K, ? extends V> e2,
                      Map.Entry<? extends K, ? extends V> e3, Map.Entry<? extends K, ? extends V> e4,
                      Map.Entry<? extends K, ? extends V> e5, Map.Entry<? extends K, ? extends V> e6,
                      Map.Entry<? extends K, ? extends V> e7, Map.Entry<? extends K, ? extends V> e8,
                      Map.Entry<? extends K, ? extends V> e9, Map.Entry<? extends K, ? extends V> e10,
                      Map.Entry<? extends K, ? extends V> e11, Map.Entry<? extends K, ? extends V> e12,
                      Map.Entry<? extends K, ? extends V> e13, Map.Entry<? extends K, ? extends V> e14,
                      Map.Entry<? extends K, ? extends V> e15, Map.Entry<? extends K, ? extends V> e16)
    {
        super(Map.ofEntries(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16));
    }

    /**
     * Create a new map with some entries.
     *
     * @param entries The entries.
     */
    @SafeVarargs
    public MapBuilder(Map.Entry<? extends K, ? extends V>... entries)
    {
        super(Map.ofEntries(entries));
    }
}
