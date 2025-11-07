package model;

import java.util.*;


public class UncertainDatabase {
    public static class ItemData {
        public final double utility;
        public final double probability;

        public ItemData(double utility, double probability) {
            this.utility = utility;
            this.probability = probability;
        }
    }

    private final List<Map<String, ItemData>> transactions;
    private final Map<String, Double> globalUnitProfit;

    public UncertainDatabase(List<Map<String, ItemData>> transactions, Map<String, Double> globalUnitProfit) {
        this.transactions = transactions;
        this.globalUnitProfit = globalUnitProfit;
    }

    public List<Map<String, ItemData>> getTransactions() { return transactions; }
    public Map<String, Double> getGlobalUnitProfit() { return globalUnitProfit; }

    public Set<String> getAllItems() {
        return new LinkedHashSet<>(globalUnitProfit.keySet());
    }

    public static UncertainDatabase sampleDatabase() {
        List<Map<String, ItemData>> txs = new ArrayList<>();
        Map<String, Double> gp = new LinkedHashMap<>();
        gp.put("A", 5.0); gp.put("B", 3.0); gp.put("C", 2.0); gp.put("D", 4.0);

        Map<String, ItemData> t1 = new HashMap<>();
        t1.put("A", new ItemData(5.0, 0.9));
        t1.put("B", new ItemData(3.0, 0.8));
        t1.put("C", new ItemData(2.0, 0.3));
        txs.add(t1);

        Map<String, ItemData> t2 = new HashMap<>();
        t2.put("A", new ItemData(5.0, 0.7));
        t2.put("C", new ItemData(2.0, 0.9));
        txs.add(t2);

        Map<String, ItemData> t3 = new HashMap<>();
        t3.put("B", new ItemData(3.0, 0.6));
        t3.put("C", new ItemData(2.0, 0.4));
        t3.put("D", new ItemData(4.0, 0.5));
        txs.add(t3);

        return new UncertainDatabase(txs, gp);
    }
}
