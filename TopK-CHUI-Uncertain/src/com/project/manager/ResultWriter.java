package com.project.manager;

import com.project.algorithms.base.Stats;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

/**
 * Utility for logging experiment results to a CSV file.
 */
public class ResultWriter {
    private static final String HEADER = "Algorithm,Dataset,K,Runtime(ms),Memory(MB),PatternCount,MinUtilThreshold";
    private static final String FILE_PATH = "output/experiments_result.csv";

    public static void write(String algoName, String dataName, int k, Stats stats) {
        File file = new File(FILE_PATH);
        
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        boolean isNew = !file.exists();
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (isNew) {
                pw.println(HEADER);
            }
            if (stats != null) {
                String shortDataName = new File(dataName).getName();
                
                pw.printf("%s,%s,%d,%d,%.2f,%d,%.5f%n", 
                    algoName, 
                    shortDataName, 
                    k, 
                    stats.getRuntime(), 
                    stats.getMemory(), 
                    stats.getPatternCount(), 
                    stats.getMinUtilThreshold()
                );
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}