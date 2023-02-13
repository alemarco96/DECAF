package it.unipd.dei.fusion;

import it.unipd.dei.utils.RankingUtils;

import java.util.*;


/**
 * The {@code LinearRunFusion} class is a {@link RunFusion} that fuses the two provided rankings
 * by performing Min-Max normalization, then a linear combination of both, with coefficients set to respectively
 * {@code 1.0 - alpha} and {@code alpha}.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class LinearRunFusion implements RunFusion
{
    private final double alpha;

    /**
     * Create the {@link RunFusion}.
     *
     * @param alpha The linear combination coefficient for the second ranking. It must be in the {@code [0.0, 1.0]}
     * range, so the coefficient for the first one ({@code 1.0 - alpha}) is also within the same range.
     * @throws IllegalArgumentException If the provided alpha is not between 0.0 and 1.0.
     */
    public LinearRunFusion(double alpha)
    {
        if ((alpha < 0.0) || (alpha > 1.0))
        {
            throw new IllegalArgumentException("The provided alpha (" + alpha + ")  is not a valid number " +
                    "between 0.0 and 1.0.");
        }

        this.alpha = alpha;
    }


    /**
     * Fuse the two provided rankings.
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @return The fused ranking.
     * @throws RuntimeException If an exception occurred while performing run fusion.
     */
    @Override
    public Map<String, Double> merge(Map<String, Double> ranking1, Map<String, Double> ranking2)
    {
        try
        {
            return RankingUtils.linearFusion(ranking1, ranking2, 1.0 - alpha, alpha);
        }
        catch (Throwable th)
        {
            close();

            throw new RuntimeException("An exception has occurred while performing run fusion.\n", th);
        }
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
