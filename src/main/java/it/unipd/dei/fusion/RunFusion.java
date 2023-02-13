package it.unipd.dei.fusion;

import java.util.Map;


/**
 * The {@code RunFusion} interface is used to merge two different rankings.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public interface RunFusion extends AutoCloseable
{
    /**
     * Fuse the two provided rankings.
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @return The fused ranking.
     */
    Map<String, Double> merge(Map<String, Double> ranking1, Map<String, Double> ranking2);
}
