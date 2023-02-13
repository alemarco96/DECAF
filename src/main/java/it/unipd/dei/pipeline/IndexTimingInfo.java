package it.unipd.dei.pipeline;

import java.util.Locale;


/**
 * The {@code IndexTimingInfo} utility class is a container for timing information used by the Index pipeline.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class IndexTimingInfo
{
    private final double invTicks;

    private long total;
    private long creation;
    private long parse;
    private long index;
    private long external;


    /**
     * Create a new {@code IndexTimingInfo}, initializing all timers with value {@code 0}.
     *
     * @param ticks The number of ticks used to represent 1 second.
     * @throws IllegalArgumentException If the provided number of ticks is not a positive integer number.
     */
    public IndexTimingInfo(long ticks)
    {
        if (ticks <= 0L)
        {
            throw new IllegalArgumentException("The provided number of ticks (" + ticks + ") is not a " +
                    "positive integer number.");
        }

        invTicks = 1.0 / ticks;
        total = 0L;
        creation = 0L;
        parse = 0L;
        index = 0L;
        external = 0L;
    }


    /**
     * Add the provided timing to this object.
     *
     * @param timing The timing to add.
     * @throws NullPointerException If the provided timing is null.
     */
    public void add(IndexTimingInfo timing)
    {
        if (timing == null)
            throw new NullPointerException("The provided timing is null.");

        if (invTicks == timing.invTicks)
        {
            total += timing.total;
            creation += timing.creation;
            parse += timing.parse;
            index += timing.index;
            external += timing.external;
        }
        else
        {
            final double k = timing.invTicks / invTicks;

            total += Math.round(k * timing.total);
            creation += Math.round(k * timing.creation);
            parse += Math.round(k * timing.parse);
            index += Math.round(k * timing.index);
            external += Math.round(k * timing.external);
        }
    }


    /**
     * Subtract the provided timing to this object.
     *
     * @param timing The timing to subtract.
     * @throws NullPointerException If the provided timing is null.
     */
    public void sub(IndexTimingInfo timing)
    {
        if (timing == null)
            throw new NullPointerException("The provided timing is null.");

        if (invTicks == timing.invTicks)
        {
            total -= timing.total;
            creation -= timing.creation;
            parse -= timing.parse;
            index -= timing.index;
            external -= timing.external;
        }
        else
        {
            final double k = timing.invTicks / invTicks;

            total -= Math.round(k * timing.total);
            creation -= Math.round(k * timing.creation);
            parse -= Math.round(k * timing.parse);
            index -= Math.round(k * timing.index);
            external -= Math.round(k * timing.external);
        }
    }


    /**
     * Add the provided value to the Total timer.
     *
     * @param time The value to sum.
     */
    public void addTotal(long time)
    {
        total += time;
    }


    /**
     * Add the provided value to the Creation timer.
     *
     * @param time The value to sum.
     */
    public void addCreation(long time)
    {
        creation += time;
    }


    /**
     * Add the provided value to the Parse timer.
     *
     * @param time The value to sum.
     */
    public void addParse(long time)
    {
        parse += time;
    }


    /**
     * Add the provided value to the Index timer.
     *
     * @param time The value to sum.
     */
    public void addIndex(long time)
    {
        index += time;
    }


    /**
     * Add the provided value to the External timer.
     *
     * @param time The value to sum.
     */
    public void addExternal(long time)
    {
        external += time;
    }


    /**
     * Get the value stored in the Total timer.
     *
     * @return The value stored in the Total timer.
     */
    public double getTotal()
    {
        return total * invTicks;
    }


    /**
     * Get the value stored in the Creation timer.
     *
     * @return The value stored in the Creation timer.
     */
    public double getCreation()
    {
        return creation * invTicks;
    }


    /**
     * Get the value stored in the Parse timer.
     *
     * @return The value stored in the Parse timer.
     */
    public double getParse()
    {
        return parse * invTicks;
    }


    /**
     * Get the value stored in the Index timer.
     *
     * @return The value stored in the Index timer.
     */
    public double getIndex()
    {
        return index * invTicks;
    }


    /**
     * Get the value stored in the External timer.
     *
     * @return The value stored in the External timer.
     */
    public double getExternal()
    {
        return external * invTicks;
    }


    /**
     * Subtract the provided value to the Total timer.
     *
     * @param time The value to subtract.
     */
    public void subTotal(long time)
    {
        total -= time;
    }


    /**
     * Subtract the provided value to the Creation timer.
     *
     * @param time The value to subtract.
     */
    public void subCreation(long time)
    {
        creation -= time;
    }


    /**
     * Subtract the provided value to the Parse timer.
     *
     * @param time The value to subtract.
     */
    public void subParse(long time)
    {
        parse -= time;
    }


    /**
     * Subtract the provided value to the Index timer.
     *
     * @param time The value to subtract.
     */
    public void subIndex(long time)
    {
        index -= time;
    }


    /**
     * Subtract the provided value to the External timer.
     *
     * @param time The value to subtract.
     */
    public void subExternal(long time)
    {
        external -= time;
    }


    /**
     * Return a textual representation of this object, for printing purposes.
     *
     * @return The textual representation of this object.
     */
    public String toString()
    {
        return String.format(Locale.US, "" +
                "Total time:         %.3f s.\n" +
                "Creation time:      %.3f s, equal to %.3f %% of total time.\n" +
                "Parse time:         %.3f s, equal to %.3f %% of total time.\n" +
                "Index time:         %.3f s, equal to %.3f %% of total time.\n" +
                "External code time: %.3f s, equal to %.3f %% of total time.\n",
                total * invTicks,
                creation * invTicks, ((100.0 * creation) / total),
                parse * invTicks, ((100.0 * parse) / total),
                index * invTicks, ((100.0 * index) / total),
                external * invTicks, ((100.0 * external) / total)
        );
    }
}
