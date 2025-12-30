package com.project;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.algorithms.uefim.UEFIM_Miner;
import com.project.algorithms.utko.UTKO_Miner;
import com.project.algorithms.utku.UTKU_Miner;
import com.project.manager.DataLoader;
import com.project.manager.ResultWriter;
import com.project.model.Dataset;

import java.io.File;
import java.util.concurrent.*;

/**
 * Main entry point for running performance benchmarks on Top-K High Utility Itemset mining algorithms.
 * Executes U-TKU, U-TKO, and U-EFIM across multiple datasets and K values.
 */
public class MainTest {

    /** Maximum time allowed for a single algorithm run (5 minutes) */
    private static final long TIME_LIMIT_SECONDS = 300; 

    public static void main(String[] args) {
        String[] datasets = {"data/liquor.txt"};
        int[] kList = {10, 50, 100 , 500};
        
        System.out.println("Starting Benchmarks...");
        System.out.println("--------------------------------------------------");

        for (String path : datasets) {
            if (!new File(path).exists()) {
                System.err.println("File not found: " + path);
                continue;
            }

            System.out.println("Loading dataset: " + path);
            Dataset db = DataLoader.load(path);
            
            if (db.getTransactions().isEmpty()) {
                System.err.println("Dataset is empty. Skipping...");
                continue;
            }
            
            System.out.printf("Loaded %s. Trans: %d, Max ItemID: %d%n", 
                new File(path).getName(), 
                db.getTransactions().size(), 
                db.getMaxItemId()
            );

            for (int k : kList) {
                System.out.println("\n--- Experiment: K = " + k + " ---");
                
                // Run algorithms in order of expected performance
                runWithTimeout(new UTKU_Miner(), db, k, "U-TKU", path, true);
                runWithTimeout(new UTKO_Miner(), db, k, "U-TKO", path, true);
                runWithTimeout(new UEFIM_Miner(), db, k, "U-EFIM", path, true);
            }
        }
        System.out.println("\nAll experiments finished. Exiting.");
        System.exit(0);
    }
    
    /**
     * Executes a mining algorithm with a strict timeout to ensure fair benchmarking.
     * @param algo The algorithm instance to run.
     * @param db The dataset to mine.
     * @param k The number of top patterns to find.
     * @param algoName Display name for the algorithm.
     * @param dataName Name of the dataset file.
     * @param recordResult Whether to write the results to the output CSV.
     */
    private static void runWithTimeout(MiningAlgorithm algo, Dataset db, int k, String algoName, String dataName, boolean recordResult) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Stats> future = null;

        try {
            // Clean memory and pause briefly to ensure a consistent starting state
            System.gc(); 
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}

            System.out.printf("  %-8s ... ", algoName);

            future = executor.submit(() -> algo.runAlgorithm(db, k));

            // Wait for completion or timeout
            Stats stats = future.get(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

            System.out.println("Done. " + stats.toString());
            
            if (recordResult) {
                ResultWriter.write(algoName, dataName, k, stats);
            }

        } catch (TimeoutException e) {
            System.out.println("TIME OUT! (> " + TIME_LIMIT_SECONDS + "s)");
            
            if (future != null) future.cancel(true);
            
            if (recordResult) {
                Stats errorStats = new Stats(algoName);
                errorStats.setRuntime(-1); // -1 indicates Timeout
                ResultWriter.write(algoName, dataName, k, errorStats);
            }

        } catch (OutOfMemoryError | ExecutionException e) {
            if (e instanceof OutOfMemoryError || (e.getCause() != null && e.getCause() instanceof OutOfMemoryError)) {
                System.out.println("OOM ERROR! (Out of Memory)");
                if (recordResult) {
                    Stats errorStats = new Stats(algoName);
                    errorStats.setRuntime(-2); // -2 indicates OOM
                    ResultWriter.write(algoName, dataName, k, errorStats);
                }
            } else {
                System.out.println("EXECUTION ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }
}
