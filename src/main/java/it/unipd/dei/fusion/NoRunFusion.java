package it.unipd.dei.fusion;

import java.util.HashMap;
import java.util.Map;


/**
 * The {@code NoRunFusion} class is a {@link RunFusion} that returns a copy of the second provided rankings.
 * Its intended usage is to provide a run fusion that, when applied to respectively the initial and the reranked
 * rankings of an utterance, it performs no operation and returns the second one.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class NoRunFusion implements RunFusion
{
    /**
     * Create the {@link RunFusion}.
     */
    public NoRunFusion()
    {
    }


    /**
     * Return a copy of the second ranking.
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @return The fused ranking.
     */
    @Override
    public Map<String, Double> merge(Map<String, Double> ranking1, Map<String, Double> ranking2)
    {
        return new HashMap<>(ranking2);
    }


    /**
     * Close this object and release the allocated resources.
     */
    @Override
    public void close()
    {
    }
}
