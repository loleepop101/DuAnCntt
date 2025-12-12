package com.project.model;

import java.util.List;

public class Transaction {
    private final List<Item> items;
    private final double transactionUtility; // TU gốc
    private final double transactionProb;
    private final double expectedTransactionUtility; // Cache giá trị này

    public Transaction(List<Item> items, double transactionUtility, double transactionProb) {
        this.items = items;
        this.transactionUtility = transactionUtility;
        this.transactionProb = transactionProb;
        
        // Tính toán trước (Pre-calculate) để tối ưu hiệu năng
        double sum = 0;
        for (Item item : items) {
            sum += item.getExpectedUtility();
        }
        this.expectedTransactionUtility = sum;
    }

    public List<Item> getItems() { return items; }
    public double getTransactionUtility() { return transactionUtility; }
    public double getTransactionProb() { return transactionProb; }
    
    // Chỉ cần return giá trị đã tính, độ phức tạp O(1)
    public double getExpectedTransactionUtility() {
        return expectedTransactionUtility;
    }
}