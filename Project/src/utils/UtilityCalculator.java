package utils;

import model.UncertainDatabase;
import model.UncertainDatabase.ItemData;

import java.util.Map;
import java.util.Set;

public class UtilityCalculator {


    public static double expectedSupport(UncertainDatabase db, Set<String> itemset) {
        double s = 0.0;
        for (Map<String, ItemData> trans : db.getTransactions()) {
            double prod = 1.0;
            for (String it : itemset) {
                ItemData d = trans.get(it);
                if (d == null) { prod = 0.0; break; }
                prod *= d.probability;
            }
            s += prod;
        }
        return s;
    }


    public static double expectedUtility(UncertainDatabase db, Set<String> itemset) {
        double eu = 0.0;
        for (Map<String, ItemData> trans : db.getTransactions()) {
            double prod = 1.0;
            double utilSum = 0.0;
            boolean ok = true;
            for (String it : itemset) {
                ItemData d = trans.get(it);
                if (d == null) { ok = false; break; }
                prod *= d.probability;

                utilSum += d.utility;
            }
            if (ok) eu += prod * utilSum;
        }
        return eu;
    }
}
