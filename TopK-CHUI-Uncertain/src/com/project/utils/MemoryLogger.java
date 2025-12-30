package com.project.utils;

/**
 * Singleton utility for tracking peak memory usage during algorithm execution.
 */
public class MemoryLogger {
    private static MemoryLogger instance = new MemoryLogger();
    private double maxMemory = 0;

    private MemoryLogger() {}

    public static MemoryLogger getInstance() {
        return instance;
    }

    public double getMaxMemory() {
        return maxMemory;
    }

    public void reset() {
        maxMemory = 0;
    }

    public void checkMemory() {
        double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }
}
