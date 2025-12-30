package com.project.algorithms.base;

/**
 * Encapsulates performance statistics for a mining algorithm run.
 */
public class Stats {
    private String algorithmName = "Unknown";
    private long runtime;
    private double memory;
    private int patternCount;
    private double minUtilThreshold;

    public Stats() {} 

    public Stats(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setRuntime(long runtime) { this.runtime = runtime; }
    public long getRuntime() { return runtime; }

    public void setMemory(double memory) { this.memory = memory; }
    public double getMemory() { return memory; }

    public void setPatternCount(int patternCount) { this.patternCount = patternCount; }
    public int getPatternCount() { return patternCount; }

    public void setMinUtilThreshold(double minUtilThreshold) { this.minUtilThreshold = minUtilThreshold; }
    public double getMinUtilThreshold() { return minUtilThreshold; }
    
    @Override
    public String toString() {
        return String.format("%s | Time: %dms | Mem: %.2fMB | Count: %d | MinUtil: %.5f", 
            algorithmName, runtime, memory, patternCount, minUtilThreshold);
    }
}