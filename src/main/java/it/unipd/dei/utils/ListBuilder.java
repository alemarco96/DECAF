package it.unipd.dei.utils;

import java.util.*;


/**
 * Utility class for constructing and populating (with up to 16 elements) a {@link List}, {@link Queue}
 * or {@link Deque} directly from constructor. It is designed to fulfill the need to pass such objects
 * while specifying a {@code .properties} file. To use this class inside a {@code .properties file}, please use:
 * <ul>
 *     <li>{@code <key>.type = java.util.List/Queue/Deque}</li>
 *     <li>{@code <key>.class = it.unipd.dei.utils.ListBuilder}</li>
 *     <li>{@code <key>.params = e1, e2, ..., eN}</li>
 *     <li>{@code <key>.params.e1.type = package.subpackage.etc.ListElementType}</li>
 *     <li>{@code <key>.params.e1.class = package.subpackage.etc.ActualElementType}</li>
 *     <li>{@code ...}</li>
 *     <li>{@code <definition of all elements of the list...>}</li>
 * </ul>
 *
 * @param <T> The type of the elements.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class ListBuilder<T> extends LinkedList<T> implements List<T>, Queue<T>, Deque<T>
{
    /**
     * Create an empty list.
     */
    public ListBuilder()
    {
        super();
    }

    /**
     * Create a new list with a single element.
     *
     * @param p1 The #1 element.
     */
    public ListBuilder(T p1)
    {
        super(List.of(p1));
    }

    /**
     * Create a new list with 2 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     */
    public ListBuilder(T p1, T p2)
    {
        super(List.of(p1, p2));
    }

    /**
     * Create a new list with 3 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     */
    public ListBuilder(T p1, T p2, T p3)
    {
        super(List.of(p1, p2, p3));
    }

    /**
     * Create a new list with 4 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4)
    {
        super(List.of(p1, p2, p3, p4));
    }

    /**
     * Create a new list with 5 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5)
    {
        super(List.of(p1, p2, p3, p4, p5));
    }

    /**
     * Create a new list with 6 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6)
    {
        super(List.of(p1, p2, p3, p4, p5, p6));
    }

    /**
     * Create a new list with 7 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7));
    }

    /**
     * Create a new list with 8 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8));
    }

    /**
     * Create a new list with 9 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9));
    }

    /**
     * Create a new list with 10 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));
    }

    /**
     * Create a new list with 11 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));
    }

    /**
     * Create a new list with 12 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     * @param p12 The #12 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11, T p12)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12));
    }

    /**
     * Create a new list with 13 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     * @param p12 The #12 element.
     * @param p13 The #13 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11, T p12, T p13)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13));
    }

    /**
     * Create a new list with 14 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     * @param p12 The #12 element.
     * @param p13 The #13 element.
     * @param p14 The #14 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11, T p12, T p13, T p14)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14));
    }

    /**
     * Create a new list with 15 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     * @param p12 The #12 element.
     * @param p13 The #13 element.
     * @param p14 The #14 element.
     * @param p15 The #15 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11, T p12, T p13, T p14, T p15)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15));
    }

    /**
     * Create a new list with 16 elements.
     *
     * @param p1 The #1 element.
     * @param p2 The #2 element.
     * @param p3 The #3 element.
     * @param p4 The #4 element.
     * @param p5 The #5 element.
     * @param p6 The #6 element.
     * @param p7 The #7 element.
     * @param p8 The #8 element.
     * @param p9 The #9 element.
     * @param p10 The #10 element.
     * @param p11 The #11 element.
     * @param p12 The #12 element.
     * @param p13 The #13 element.
     * @param p14 The #14 element.
     * @param p15 The #15 element.
     * @param p16 The #16 element.
     */
    public ListBuilder(T p1, T p2, T p3, T p4, T p5, T p6, T p7, T p8, T p9, T p10, T p11, T p12, T p13, T p14, T p15, T p16)
    {
        super(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16));
    }

    /**
     * Create a new list with some elements.
     *
     * @param elements The elements.
     */
    @SafeVarargs
    public ListBuilder(T... elements)
    {
        super(Arrays.asList(elements));
    }

    /**
     * Create a copy of the provided collection.
     *
     * @param collection The collection to copy.
     */
    public ListBuilder(Collection<? extends T> collection)
    {
        super(collection);
    }
}
