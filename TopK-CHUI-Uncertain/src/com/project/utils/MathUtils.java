package com.project.utils;

/**
 * Utility class for mathematical operations and epsilon-aware comparisons.
 */
public class MathUtils {
    /** Standard epsilon for handling double precision errors in mining algorithms */
    private static final double EPSILON = 0.00000001; 

    private MathUtils() {
        // Prevent instantiation
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean greaterThan(double a, double b) {
        return a > b + EPSILON;
    }
    
    /**
     * Checks if a is greater than or equal to b, accounting for precision errors.
     * Used primarily for threshold checks (utility >= minUtil).
     */
    public static boolean greaterThanOrEqual(double a, double b) {
        return a > b - EPSILON;
    }

    public static boolean lessThan(double a, double b) {
        return a < b - EPSILON;
    }
    
    public static boolean lessThanOrEqual(double a, double b) {
        return a < b + EPSILON;
    }

    /**
     * Performs a fast subset check for sorted integer arrays.
     * @param sub The potential subset.
     * @param superSet The potential superset.
     * @return true if 'sub' is a subset of 'superSet'.
     */
    public static boolean isSubsetSorted(int[] sub, int[] superSet) {
        if (sub.length > superSet.length) return false;
        if (sub.length == 0) return true;

        // Bound check: If sub's range is outside superSet's range, it cannot be a subset
        if (sub[0] < superSet[0] || sub[sub.length-1] > superSet[superSet.length-1]) {
            return false;
        }

        // Two-pointer linear scan (O(N))
        int i = 0; // Pointer for sub
        int j = 0; // Pointer for superSet
        
        while (i < sub.length && j < superSet.length) {
            if (sub[i] < superSet[j]) {
                return false; 
            } else if (sub[i] == superSet[j]) {
                i++;
                j++;
            } else {
                j++;
            }
        }
        
        return i == sub.length;
    }
}