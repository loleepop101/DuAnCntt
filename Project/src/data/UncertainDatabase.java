package data;

import java.util.*;

public class UncertainDatabase {
    private List<Map<String, ItemData>> transactions;

    public static class ItemData {
        public double utility;
        public double probability;

        public ItemData(double utility, double probability) {
            this.utility = utility;
            this.probability = probability;
        }
    }

    public UncertainDatabase() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Map<String, ItemData> transaction) {
        transactions.add(transaction);
    }

    public List<Map<String, ItemData>> getTransactions() {
        return transactions;
    }

    public List<String> getAllUniqueItems() {
        Set<String> items = new HashSet<>();
        for (Map<String, ItemData> t : transactions) {
            items.addAll(t.keySet());
        }
        return new ArrayList<>(items);
    }

    // Tạo dữ liệu giả lập để test
    public static UncertainDatabase createSampleDatabase() {
        UncertainDatabase db = new UncertainDatabase();

        Map<String, ItemData> t1 = new HashMap<>();
        t1.put("A", new ItemData(5, 0.9));
        t1.put("B", new ItemData(3, 0.8));
        t1.put("C", new ItemData(2, 0.6));
        db.addTransaction(t1);

        Map<String, ItemData> t2 = new HashMap<>();
        t2.put("A", new ItemData(4, 0.7));
        t2.put("C", new ItemData(5, 0.9));
        db.addTransaction(t2);

        Map<String, ItemData> t3 = new HashMap<>();
        t3.put("B", new ItemData(6, 0.8));
        t3.put("C", new ItemData(4, 0.6));
        db.addTransaction(t3);

        return db;
    }
}
