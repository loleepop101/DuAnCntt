package com.project.algorithms.utko;

public class Element {
    // IMMUTABLE for better performance and safety
    private final int tid;
    private final double sumUtility;      // Sum of raw utilities of items in the itemset
    private final double prodProbability; // Product of probabilities of items in the itemset
    private final double rutils;          // Remaining utility bound (sum of expected utilities)

    public Element(int tid, double sumUtility, double prodProbability, double rutils) {
        this.tid = tid;
        this.sumUtility = sumUtility;
        this.prodProbability = prodProbability;
        this.rutils = rutils;
    }

    public int getTid() { return tid; }
    public double getSumUtility() { return sumUtility; }
    public double getProdProbability() { return prodProbability; }
    public double getRutils() { return rutils; }
    
    /**
     * Calculates the Expected Utility of the itemset in this transaction.
     * EU(X, T) = (Sum of Utilities) * (Product of Probabilities)
     */
    public double getExpectedUtility() {
        return sumUtility * prodProbability;
    }
}