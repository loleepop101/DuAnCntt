package com.project.model;

import com.project.utils.MathUtils;
import java.util.Arrays;

/**
 * Represents a set of items (pattern) discovered by the mining algorithm.
 * Stores items in a primitive array for memory efficiency.
 */
public class Itemset implements Comparable<Itemset> {
    private final int[] items; 
    private final double utility;
    private final double expectedSupport; 

    public Itemset(int[] items, double utility, double expectedSupport) {
        this.items = items;
        Arrays.sort(this.items);
        this.utility = utility;
        this.expectedSupport = expectedSupport;
    }

    public int[] getItems() { return items; }
    public double getUtility() { return utility; }
    public double getExpectedSupport() { return expectedSupport; }

    /**
     * Compares itemsets based on their utility.
     * Uses MathUtils for epsilon-aware equality checks.
     */
    @Override
    public int compareTo(Itemset o) {
        if (MathUtils.equals(this.utility, o.utility)) {
            return 0;
        }
        return Double.compare(this.utility, o.utility);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int item : items){
            sb.append(item).append(" ");
        }
        sb.append("#UTIL: ").append(utility);
        sb.append(" #EXP_SUP: ").append(String.format("%.4f", expectedSupport));
        return sb.toString();
    }
    
    // Helper for hashing if needed in HashMaps
    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Itemset other = (Itemset) obj;
        return Arrays.equals(items, other.items);
    }
}