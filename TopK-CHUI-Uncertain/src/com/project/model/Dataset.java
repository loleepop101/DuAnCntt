package com.project.model;

import java.util.ArrayList;
import java.util.List;

public class Dataset {
    private List<Transaction> transactions;
    private int maxItemId;

    public Dataset() {
        this.transactions = new ArrayList<>();
        this.maxItemId = 0;
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