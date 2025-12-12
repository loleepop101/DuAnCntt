package com.project.utils;

public class MathUtils {
    private static final double EPSILON = 0.00001;

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean greaterThan(double a, double b) {
        return a > b + EPSILON;
    }

    public static boolean lessThan(double a, double b) {
        return a < b - EPSILON;
    }
}
