package com.project;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.algorithms.uefim.UEFIM_Miner;
import com.project.algorithms.utko.UTKO_Miner;
import com.project.algorithms.utku.UTKU_Miner;
import com.project.manager.DataLoader;
import com.project.manager.ResultWriter;
import com.project.model.Dataset;

import java.util.concurrent.*;

public class MainTest {

    // Cấu hình Timeout 
    private static final long TIME_LIMIT_SECONDS = 120; 

    public static void main(String[] args) {
        // Đường dẫn file dataset
        String[] datasets = {"data/foodmart.txt"};
        int[] kList = {10, 50, 100, 500};
        
        for (String path : datasets) {
            System.out.println("=========================================");
            System.out.println("Loading dataset: " + path);
            Dataset db = DataLoader.load(path);
            
            if (db.getTransactions().isEmpty()) {
                System.err.println("Dataset is empty. Skipping...");
                continue;
            }
            
            System.out.println("Dataset loaded. Transaction count: " + db.getTransactions().size());
            System.out.println("Max Item ID: " + db.getMaxItemId());
            
            for (int k : kList) {
                System.out.println("\n--- Running experiments for K=" + k + " ---");
                
                runWithTimeout(new UTKU_Miner(), db, k, "U-TKU", path);
                runWithTimeout(new UTKO_Miner(), db, k, "U-TKO", path);
                runWithTimeout(new UEFIM_Miner(), db, k, "U-EFIM", path);
            }
        }
        System.exit(0);
    }
    
    private static void runWithTimeout(MiningAlgorithm algo, Dataset db, int k, String algoName, String dataName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Stats> future = null;

        try {
            System.gc(); 
            Thread.sleep(100); 
            
            System.out.print("  Running " + algoName + "...");

            future = executor.submit(() -> algo.runAlgorithm(db, k));

            Stats stats = future.get(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

            System.out.println(" -> Done. " + stats.toString());
            
            ResultWriter.write(algoName, dataName, k, stats);

        } catch (TimeoutException e) {
            System.out.println(" -> TIME OUT! (Skipped after " + TIME_LIMIT_SECONDS + "s)");
            if (future != null) future.cancel(true);
            
            Stats errorStats = new Stats(algoName);
            errorStats.setRuntime(-1); 
            ResultWriter.write(algoName, dataName, k, errorStats);

        } catch (OutOfMemoryError e) {
            System.out.println(" -> OOM ERROR! (Out of Memory)");
            Stats errorStats = new Stats(algoName);
            errorStats.setRuntime(-2); 
            ResultWriter.write(algoName, dataName, k, errorStats);

        } catch (Exception e) {
            System.out.println(" -> EXECUTION ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }
}