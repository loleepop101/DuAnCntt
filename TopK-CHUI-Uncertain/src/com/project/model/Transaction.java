package com.project.model;

import com.project.utils.MathUtils;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a transaction in the dataset.
 * Items are automatically sorted by ID upon construction to optimize search operations.
 */
public class Transaction {
    private final List<Item> items;
    private final int[] itemIds; 
    private final double transactionUtility; 
    private final double expectedTransactionUtility; 

    public Transaction(List<Item> items, double transactionUtility) {
        // Sort items by ID to enable efficient linear scan operations (O(N))
        items.sort(Comparator.comparingInt(Item::getItemId));
        
        this.items = items;
        this.transactionUtility = transactionUtility;
        
        // Pre-calculate expected utility and cache item IDs for performance
        double sum = 0;
        this.itemIds = new int[items.size()];
        
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            this.itemIds[i] = item.getItemId();
            sum += item.getExpectedUtility();
        }
        this.expectedTransactionUtility = sum;
    }

    public List<Item> getItems() { return items; }
    public int[] getItemIds() { return itemIds; } 
    public double getTransactionUtility() { return transactionUtility; }
    public double getExpectedTransactionUtility() { return expectedTransactionUtility; }

    /**
     * Calculates the existential probability of an itemset in this transaction.
     * P(X) = product of P(i) for all i in X.
     */
    public double calculateProbabilityOf(int[] candidateItems) {
        double prob = 1.0;
        int i = 0; // Pointer for candidateItems
        int j = 0; // Pointer for this.items
        
        while (i < candidateItems.length && j < this.items.size()) {
            int transItemId = this.items.get(j).getItemId();
            int candItemId = candidateItems[i];
            
            if (transItemId < candItemId) {
                j++;
            } else if (transItemId == candItemId) {
                prob *= this.items.get(j).getProbability();
                i++;
                j++;
            } else {
                return 0; // Item not in transaction
            }
        }
        return (i == candidateItems.length) ? prob : 0;
    }

    /**
     * Checks if the transaction contains all items in the given candidate set.
     * Uses a two-pointer approach for O(N) complexity.
     */
    public boolean containsAll(int[] candidateItems) {
        return MathUtils.isSubsetSorted(candidateItems, this.itemIds);
    }

    /**
     * Calculates the total expected utility of the given items within this transaction.
     * Formula: EU(X, T) = (Sum of utilities of items in X) * (Product of probabilities of items in X)
     * Uses a two-pointer linear scan for efficiency.
     */
    public double calculateUtilityOf(int[] candidateItems) {
        double sumU = 0;
        double prodP = 1.0;
        int i = 0; // Pointer for candidateItems
        int j = 0; // Pointer for this.items
        
        while (i < candidateItems.length && j < this.items.size()) {
            int transItemId = this.items.get(j).getItemId();
            int candItemId = candidateItems[i];
            
            if (transItemId < candItemId) {
                j++;
            } else if (transItemId == candItemId) {
                Item item = this.items.get(j);
                sumU += item.getUtility();
                prodP *= item.getProbability();
                i++;
                j++;
            } else {
                return 0; 
            }
        }
        
        if (i == candidateItems.length) {
            return sumU * prodP;
        }
        return 0;
    }
}
