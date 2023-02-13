package it.unipd.dei.utils;

import java.io.PrintWriter;
import java.util.*;


/**
 * The {@code RankingUtils} utility class performs common operations on rankings, such as:
 * <ul>
 *     <li>Sorting by score</li>
 *     <li>Perform Min-Max normalization</li>
 *     <li>Perform Linear run fusion</li>
 *     <li>Perform Reciprocal Rank run fusion</li>
 *     <li>Writing to output file in TREC-EVAL format</li>
 * </ul>
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class RankingUtils
{
    // Disable the default constructor.
    private RankingUtils()
    {
        throw new RuntimeException("This class can not be instantiated.");
    }


    /**
     * Sort the ranking (document ID-score pairs) by score in descending order.
     *
     * @param ranking The ranking to be sorted.
     * @throws NullPointerException If the provided ranking is {@code null}.
     * @return The list of document ID-score pairs sorted by score in descending order.
     */
    public static List<Map.Entry<String, Double>> sortRanking(Map<String, Double> ranking)
    {
        if (ranking == null)
            throw new NullPointerException("The provided ranking is null.");


        final List<Map.Entry<String, Double>> rankingList = new ArrayList<>(ranking.entrySet());
        rankingList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        return rankingList;
    }


    /**
     * Perform Min-Max normalization of all pairs in the ranking. It scales all scores in the [0,1] range.
     *
     * @param ranking The ranking to normalize.
     * @throws NullPointerException If the provided ranking is {@code null}.
     * @return The Min-Max normalized ranking.
     */
    public static Map<String, Double> minMaxNormalization(Map<String, Double> ranking)
    {
        if (ranking == null)
            throw new NullPointerException("The provided ranking is null.");


        // Sort the ranking by score.
        final List<Map.Entry<String, Double>> rankingList = new ArrayList<>(ranking.entrySet());
        rankingList.sort(Map.Entry.comparingByValue());

        // Find the minimum and maximum value of the score.
        final double minValue = rankingList.get(0).getValue();
        final double maxValue = rankingList.get(rankingList.size() - 1).getValue();

        // Min-Max normalization of all document ID-score pairs.
        final Map<String, Double> result = new HashMap<>(ranking);
        result.replaceAll((k, v) -> (v - minValue) / (maxValue - minValue));

        return result;
    }


    /**
     * Compute the {@link Set} of document IDs that appear on both rankings.
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @throws NullPointerException If any of the provided rankings is {@code null}.
     * @return The {@link Set} of document IDs that appear on both rankings.
     */
    public static Set<String> computeRankingIntersection(Map<String, Double> ranking1, Map<String, Double> ranking2)
    {
        if (ranking1 == null)
            throw new NullPointerException("The provided first ranking is null.");

        if (ranking2 == null)
            throw new NullPointerException("The provided second ranking is null.");


        final Set<String> docs = new HashSet<>(ranking1.keySet());
        docs.retainAll(ranking2.keySet());
        return docs;
    }


    /**
     * Perform run fusion by Min-Max normalization on both ranking, then perform a linear combination of the scores.
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @param alpha1 The first linear combination coefficient.
     * @param alpha2 The second linear combination coefficient.
     * @throws NullPointerException If any of the provided ranking is {@code null}.
     * @throws IllegalArgumentException If any of the provided linear combination coefficient
     * is not a non-negative finite number.
     * @return The fused ranking.
     */
    public static Map<String, Double> linearFusion(Map<String, Double> ranking1, Map<String, Double> ranking2,
                                            double alpha1, double alpha2)
    {
        if (ranking1 == null)
            throw new NullPointerException("The provided first ranking is null.");

        if (ranking2 == null)
            throw new NullPointerException("The provided second ranking is null.");

        if ((!Double.isFinite(alpha1)) || (alpha1 < 0.0))
        {
            throw new IllegalArgumentException("The provided first alpha (" + alpha1 + ") is not " +
                    "a non-negative finite number.");
        }

        if ((!Double.isFinite(alpha2)) || (alpha2 < 0.0))
        {
            throw new IllegalArgumentException("The provided first alpha (" + alpha2 + ") is not " +
                    "a non-negative finite number.");
        }


        // Sort the rankings by score.
        final List<Map.Entry<String, Double>> lRanking1 = sortRanking(ranking1);
        final List<Map.Entry<String, Double>> lRanking2 = sortRanking(ranking2);

        // Find the minimum and maximum value for both ranking.
        final double max1 = lRanking1.get(0).getValue();
        final double min1 = lRanking1.get(lRanking1.size() - 1).getValue();

        final double max2 = lRanking2.get(0).getValue();
        final double min2 = lRanking2.get(lRanking2.size() - 1).getValue();

        // Compute the denominator 1.0 / (max - min) for both.
        final double den1 = 1.0 / ((max1 - min1) + Double.MIN_VALUE);
        final double den2 = 1.0 / ((max2 - min2) + Double.MIN_VALUE);

        // Find all IDs of all documents that appear in at least 1 of the rankings.
        final Set<String> documents = new TreeSet<>(ranking1.keySet());
        documents.addAll(ranking2.keySet());

        // Perform run fusion.
        final Map<String, Double> result = new HashMap<>();
        for (String id : documents)
        {
            final Double score1 = ranking1.get(id);
            final Double score2 = ranking2.get(id);

            final double normScore1 = score1 != null ? ((score1 - min1) + Double.MIN_VALUE) * den1 : 0.0;
            final double normScore2 = score2 != null ? ((score2 - min2) + Double.MIN_VALUE) * den2 : 0.0;

            final double score = (normScore1 * alpha1) + (normScore2 * alpha2);

            result.put(id, score);
        }

        return result;
    }


    /**
     * Perform run fusion using reciprocal rank with a linear combination. The formula used is:
     * {@code score(d) = sum[(k / (k + rank(i, d))) * alpha(i)  for i in {1,2}]}
     *
     * @param ranking1 The first ranking.
     * @param ranking2 The second ranking.
     * @param k The denominator coefficient in the reciprocal rank formula.
     * @param alpha1 The first linear combination coefficient.
     * @param alpha2 The second linear combination coefficient.
     * @throws NullPointerException If any of the provided ranking is {@code null}.
     * @throws IllegalArgumentException If any of the provided linear combination coefficient and the
     * denominator coefficient is not a non-negative finite number.
     * @return The fused ranking.
     */
    public static Map<String, Double> reciprocalRankFusion(Map<String, Double> ranking1, Map<String, Double> ranking2,
                                                           double k, double alpha1, double alpha2)
    {
        if (ranking1 == null)
            throw new NullPointerException("The provided first ranking is null.");

        if (ranking2 == null)
            throw new NullPointerException("The provided second ranking is null.");

        if ((!Double.isFinite(k)) || (k < 0.0))
        {
            throw new IllegalArgumentException("The provided first k (" + k + ") is not " +
                    "a non-negative finite number.");
        }

        if ((!Double.isFinite(alpha1)) || (alpha1 < 0.0))
        {
            throw new IllegalArgumentException("The provided first alpha (" + alpha1 + ") is not " +
                    "a non-negative finite number.");
        }

        if ((!Double.isFinite(alpha2)) || (alpha2 < 0.0))
        {
            throw new IllegalArgumentException("The provided first alpha (" + alpha1 + ") is not " +
                    "a non-negative finite number.");
        }


        // Sort the rankings by score.
        final List<Map.Entry<String, Double>> lRanking1 = sortRanking(ranking1);
        final List<Map.Entry<String, Double>> lRanking2 = sortRanking(ranking2);

        // Perform the fusion.
        final Map<String, Double> result = new HashMap<>();

        for (int i = 0; i < lRanking1.size(); i++)
        {
            final Map.Entry<String, Double> entry = lRanking1.get(i);
            final String id = entry.getKey();
            final double score = result.getOrDefault(id, 0.0) + (k / (k + i)) * alpha1;

            result.put(id, score);
        }

        for (int i = 0; i < lRanking2.size(); i++)
        {
            final Map.Entry<String, Double> entry = lRanking2.get(i);
            final String id = entry.getKey();
            final double score = result.getOrDefault(id, 0.0) + (k / (k + i)) * alpha2;

            result.put(id, score);
        }

        return result;
    }


    /**
     * Print the ranking associated with the current query in TREC-EVAL format.
     *
     * @param writer The {@link PrintWriter} output stream where to print the data.
     * @param ranking The ranking to print.
     * @param runId The ID of the current run.
     * @param queryId The ID of the current query.
     * @throws NullPointerException If any of the provided writer, ranking, run ID or query ID is null.
     */
    public static void writeRanking(PrintWriter writer, Map<String, Double> ranking, String runId, String queryId)
    {
        writeRanking(writer, ranking, runId, queryId, ranking.size());
    }


    /**
     * Print the ranking associated with the current query in TREC-EVAL format. The maximum number of
     * document-score pairs considered is passed as parameter.
     *
     * @param writer The {@link PrintWriter} output stream where to print the data.
     * @param ranking The ranking to print.
     * @param runId The ID of the current run.
     * @param queryId The ID of the current query.
     * @param numDocuments The number of document-score pairs to print.
     * @throws NullPointerException If any of the provided writer, ranking, run ID or query ID is null.
     * @throws IllegalArgumentException If the provided number of documents is not a positive integer number.
     */
    public static void writeRanking(PrintWriter writer, Map<String, Double> ranking, String runId, String queryId,
                                    int numDocuments)
    {
        if (writer == null)
            throw new NullPointerException("The provided writer is null.");

        if (ranking == null)
            throw new NullPointerException("The provided ranking is null.");

        if (runId == null)
            throw new NullPointerException("The provided run ID is null.");

        if (queryId == null)
            throw new NullPointerException("The provided query ID is null.");

        if (numDocuments < 1)
        {
            throw new IllegalArgumentException("The provided number of documents(" + numDocuments + ") must be " +
                    "a positive integer number");
        }

        // Obtain a list of document-value elements, sorted by value descending.
        final List<Map.Entry<String, Double>> rankingList = sortRanking(ranking);

        // Loop through the rankingList in descending order of score.
        for (int i = 0; i < Math.min(numDocuments, rankingList.size()); i++)
        {
            final Map.Entry<String, Double> entry = rankingList.get(i);

            writer.printf(Locale.ENGLISH, "%s\tQ0\t%s\t%d\t%.6f\t%s\n",
                    queryId, entry.getKey(), i, entry.getValue(), runId);
        }

        writer.flush();
    }
}
