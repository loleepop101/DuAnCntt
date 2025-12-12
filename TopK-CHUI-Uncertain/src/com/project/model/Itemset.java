package com.project.model;

import java.util.List;

public class Itemset implements Comparable<Itemset> {
    private List<Integer> items; 
    private double utility;
    private int support; 

    public Itemset(List<Integer> items, double utility, int support) {
        this.items = items;
        this.utility = utility;
        this.support = support;
    }

    public List<Integer> getItems() { return items; }
    public double getUtility() { return utility; }
    public int getSupport() { return support; }
    
    public void setUtility(double utility) { this.utility = utility; }

    /**
     * So sánh để dùng trong PriorityQueue (Min-Heap).
     * Sắp xếp tăng dần theo Utility.
     */
    @Override
    public int compareTo(Itemset o) {
        if (this.utility == o.utility) {
            return 0; 
        }
        return Double.compare(this.utility, o.utility);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Integer item : items){
            sb.append(item).append(" ");
        }
        sb.append("#UTIL: ").append(utility);
        sb.append(" #SUP: ").append(support);
        return sb.toString();
    }
}