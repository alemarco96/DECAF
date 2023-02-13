package it.unipd.dei.fusion;

import it.unipd.dei.utils.RankingUtils;

import java.util.Map;


/**
 * The {@code ReciprocalRankRunFusion} class is a {@link RunFusion} that fuses the two provided rankings
 * by performing Reciprocal Rank run fusion. The formula used is:
 * {@code score(d) = sum[(k / (k + rank1(d))) * (1.0 - alpha); (k / (k + rank2(d))) * alpha}]}.
 * Note that the k factor at the numerator is only for scaling purposes, and it does not change the ranking w.r.t
 * the one produced by the original formulation. Also note that the alpha parameter is used to weight differently
 * the two rankings. The original formulation utilized the parameters {@code k = 60.0} and {@code alpha = 0.5}.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class ReciprocalRankRunFusion implements RunFusion
{
    private final double k;
    private final double alpha;


    /**
     * Create the {@link RunFusion}, using the standard parameters {@code k = 60.0} and {@code alpha = 0.5}.
     */
    public ReciprocalRankRunFusion()
    {
        this(60.0, 0.5);
    }


    /**
     * Create the {@link RunFusion}, using custom parameters.
     *
     * @param k The denominator coefficient.
     * @param alpha The linear combination coefficient for the reranked ranking. It must be in the {@code [0.0, 1.0]}
     * range, so the coefficient for the initial ranking ({@code 1.0 - alpha}) is also within the same range.
     * @throws IllegalArgumentException If the provided k is not a positive finite number,
     * or alpha is not between 0.0 and 1.0.
     */
    public ReciprocalRankRunFusion(double k, double alpha)
    {
        if ((!Double.isFinite(k)) || (k <= 0.0))
            throw new IllegalArgumentException("The provided k (" + k + ") must be a positive finite number.");

        if ((alpha < 0.0) || (alpha > 1.0))
        {
            throw new IllegalArgumentException("The provided alpha (" + alpha + ") is not a valid number " +
                    "between 0.0 and 1.0.");
        }

        this.k = k;
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
            return RankingUtils.reciprocalRankFusion(ranking1, ranking2, k, 1.0 - alpha, alpha);
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
