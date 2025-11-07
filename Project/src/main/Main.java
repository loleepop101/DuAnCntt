package main;

import core.AlgorithmResult;
import model.UncertainDatabase;
import algorithms.TKUAlgorithm;

public class Main {
    public static void main(String[] args) {
        System.out.println("Generating sample uncertain database...");
        UncertainDatabase db = UncertainDatabase.sampleDatabase();

        int K = 5;
        int maxLen = 4;

        TKUAlgorithm tku = new TKUAlgorithm();
        AlgorithmResult result = tku.run(db, K, maxLen);

        result.printSummary();
    }
}
