package com.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for the collection of transactions to be mined.
 * Provides utility methods for dataset-wide calculations like TWU.
 */
public class Dataset {
    private List<Transaction> transactions;
    private int maxItemId;

    public Dataset() {
        this.transactions = new ArrayList<>();
        this.maxItemId = 0;
    }

    /**
     * Calculates Expected Transaction Weighted Utilization (ETWU) for all items.
     * ETWU(i) = sum of Expected Transaction Utilities (ETU) of all transactions containing i.
     */
    public Map<Integer, Double> calculateTWUs() {
        Map<Integer, Double> mapItemToTWU = new HashMap<>();
        for (Transaction t : transactions) {
            double etu = t.getExpectedTransactionUtility();
            for (Item item : t.getItems()) {
                mapItemToTWU.put(item.getItemId(), 
                    mapItemToTWU.getOrDefault(item.getItemId(), 0.0) + etu);
            }
        }
        return mapItemToTWU;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getMaxItemId() {
        return maxItemId;
    }

    public void setMaxItemId(int maxItemId) {
        this.maxItemId = maxItemId;
    }
}