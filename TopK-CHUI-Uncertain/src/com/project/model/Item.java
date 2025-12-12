package com.project.model;

public class Item {
    private final int itemId;
    private final double utility;
    private final double probability;

    public Item(int itemId, double utility, double probability) {
        this.itemId = itemId;
        this.utility = utility;
        this.probability = probability;
    }

    public int getItemId() { return itemId; }
    public double getUtility() { return utility; }
    public double getProbability() { return probability; }

    public double getExpectedUtility() {
        return utility * probability;
    }
    
    @Override
    public String toString() {
        return itemId + ""; 
    }
}