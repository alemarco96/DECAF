package it.unipd.dei.pipeline;

import java.util.Locale;


/**
 * The {@code SearchTimingInfo} utility class is a container for timing information used by the Search pipeline.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class SearchTimingInfo
{
    private final double invTicks;

    private long total;
    private long creation;
    private long topics;
    private long rewriter;
    private long searcher;
    private long reranker;
    private long writer;


    /**
     * Create a new {@code SearchTimingInfo}, initializing all timers with value {@code 0}.
     *
     * @param ticks The number of ticks used to represent 1 second.
     * @throws IllegalArgumentException If the provided number of ticks is not a positive integer number.
     */
    public SearchTimingInfo(long ticks)
    {
        if (ticks <= 0L)
        {
            throw new IllegalArgumentException("The provided number of ticks (" + ticks + ") is not a " +
                    "positive integer number.");
        }

        invTicks = 1.0 / ticks;
        total = 0L;
        creation = 0L;
        topics = 0L;
        rewriter = 0L;
        searcher = 0L;
        reranker = 0L;
        writer = 0L;
    }


    /**
     * Add the provided timing to this object.
     *
     * @param timing The timing to add.
     * @throws NullPointerException If the provided timing is null.
     */
    public void add(SearchTimingInfo timing)
    {
        if (timing == null)
            throw new NullPointerException("The provided timing is null.");

        if (invTicks == timing.invTicks)
        {
            total += timing.total;
            creation += timing.creation;
            topics += timing.topics;
            rewriter += timing.rewriter;
            searcher += timing.searcher;
            reranker += timing.reranker;
            writer += timing.writer;
        }
        else
        {
            final double k = timing.invTicks / invTicks;

            total += Math.round(k * timing.total);
            creation += Math.round(k * timing.creation);
            topics += Math.round(k * timing.topics);
            rewriter += Math.round(k * timing.rewriter);
            searcher += Math.round(k * timing.searcher);
            reranker += Math.round(k * timing.reranker);
            writer += Math.round(k * timing.writer);
        }
    }


    /**
     * Subtract the provided timing to this object.
     *
     * @param timing The timing to subtract.
     * @throws NullPointerException If the provided timing is null.
     */
    public void sub(SearchTimingInfo timing)
    {
        if (timing == null)
            throw new NullPointerException("The provided timing is null.");

        if (invTicks == timing.invTicks)
        {
            total -= timing.total;
            creation -= timing.creation;
            topics -= timing.topics;
            rewriter -= timing.rewriter;
            searcher -= timing.searcher;
            reranker -= timing.reranker;
            writer -= timing.writer;
        }
        else
        {
            final double k = timing.invTicks / invTicks;

            total -= Math.round(k * timing.total);
            creation -= Math.round(k * timing.creation);
            topics -= Math.round(k * timing.topics);
            rewriter -= Math.round(k * timing.rewriter);
            searcher -= Math.round(k * timing.searcher);
            reranker -= Math.round(k * timing.reranker);
            writer -= Math.round(k * timing.writer);
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
     * Add the provided value to the Topics timer.
     *
     * @param time The value to sum.
     */
    public void addTopics(long time)
    {
        topics += time;
    }


    /**
     * Add the provided value to the Rewriter timer.
     *
     * @param time The value to sum.
     */
    public void addRewriter(long time)
    {
        rewriter += time;
    }


    /**
     * Add the provided value to the Searcher timer.
     *
     * @param time The value to sum.
     */
    public void addSearcher(long time)
    {
        searcher += time;
    }


    /**
     * Add the provided value to the Reranker timer.
     *
     * @param time The value to sum.
     */
    public void addReranker(long time)
    {
        reranker += time;
    }


    /**
     * Add the provided value to the Writer timer.
     *
     * @param time The value to sum.
     */
    public void addWriter(long time)
    {
        writer += time;
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
     * Get the value stored in the Topics timer.
     *
     * @return The value stored in the Topics timer.
     */
    public double getTopics()
    {
        return topics * invTicks;
    }


    /**
     * Get the value stored in the Rewriter timer.
     *
     * @return The value stored in the Rewriter timer.
     */
    public double getRewriter()
    {
        return rewriter * invTicks;
    }


    /**
     * Get the value stored in the Searcher timer.
     *
     * @return The value stored in the Searcher timer.
     */
    public double getSearcher()
    {
        return searcher * invTicks;
    }


    /**
     * Get the value stored in the Reranker timer.
     *
     * @return The value stored in the Reranker timer.
     */
    public double getReranker()
    {
        return reranker * invTicks;
    }


    /**
     * Get the value stored in the Writer timer.
     *
     * @return The value stored in the Writer timer.
     */
    public double getWriter()
    {
        return writer * invTicks;
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
     * Subtract the provided value to the Topics timer.
     *
     * @param time The value to subtract.
     */
    public void subTopics(long time)
    {
        topics -= time;
    }


    /**
     * Subtract the provided value to the Rewriter timer.
     *
     * @param time The value to subtract.
     */
    public void subRewriter(long time)
    {
        rewriter -= time;
    }


    /**
     * Subtract the provided value to the Searcher timer.
     *
     * @param time The value to subtract.
     */
    public void subSearcher(long time)
    {
        searcher -= time;
    }


    /**
     * Subtract the provided value to the Reranker timer.
     *
     * @param time The value to subtract.
     */
    public void subReranker(long time)
    {
        reranker -= time;
    }


    /**
     * Subtract the provided value to the Writer timer.
     *
     * @param time The value to subtract.
     */
    public void subWriter(long time)
    {
        writer -= time;
    }


    /**
     * Return a textual representation of this object, for printing purposes.
     *
     * @return The textual representation of this object.
     */
    @Override
    public String toString()
    {
        return String.format(Locale.US, "" +
                       "Total time:      %.3f s.\n" +
                       "Creation time:   %.3f s, equal to %.3f %% of total time.\n" +
                       "Topics time:     %.3f s, equal to %.3f %% of total time.\n" +
                       "Rewriter time:   %.3f s, equal to %.3f %% of total time.\n" +
                       "Searcher time:   %.3f s, equal to %.3f %% of total time.\n" +
                       "Reranker time:   %.3f s, equal to %.3f %% of total time.\n" +
                       "Run Writer time: %.3f s, equal to %.3f %% of total time.\n",
                 total * invTicks,
                       creation * invTicks, ((100.0 * creation) / total),
                       topics * invTicks, ((100.0 * topics) / total),
                       rewriter * invTicks, ((100.0 * rewriter) / total),
                       searcher * invTicks, ((100.0 * searcher) / total),
                       reranker * invTicks, ((100.0 * reranker) / total),
                       writer * invTicks, ((100.0 * writer) / total)
        );
    }
}
