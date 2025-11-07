package core;

import java.util.List;
import algorithms.TKUAlgorithm; // not necessary but ok

public class AlgorithmResult {
    public final String algorithmName;
    public final List<ItemsetResult> topK;
    public final long timeMillis;
    public final long evaluatedCount;

    public AlgorithmResult(String algorithmName, List<ItemsetResult> topK, long timeMillis, long evaluatedCount) {
        this.algorithmName = algorithmName;
        this.topK = topK;
        this.timeMillis = timeMillis;
        this.evaluatedCount = evaluatedCount;
    }

    public void printSummary() {
        System.out.println("=== " + algorithmName + " ===");
        System.out.println("Time (ms): " + timeMillis);
        System.out.println("Itemsets evaluated (approx): " + evaluatedCount);
        System.out.println("Top-" + topK.size() + ":");
        int i = 1;
        for (ItemsetResult r : topK) {
            System.out.printf("%2d) %s\n", i++, r);
        }
        System.out.println();
    }
}
