package algorithms;

import core.AlgorithmResult;
import core.ItemsetResult;
import core.TopKAlgorithm;
import model.UncertainDatabase;
import model.UncertainDatabase.ItemData;
import utils.Util;
import utils.UtilityCalculator;

import java.util.*;


public class TKUAlgorithm implements TopKAlgorithm {

    private static class UL {
        static class Entry { int tid; double p; double u; Entry(int tid, double p, double u){ this.tid=tid; this.p=p; this.u=u; } }
        List<Entry> entries = new ArrayList<>();
    }

    private final String name = "TKU-like";

    @Override
    public AlgorithmResult run(UncertainDatabase db, int K, int maxLen) {
        long start = System.currentTimeMillis();

        List<Map<String, ItemData>> transactions = db.getTransactions();
        Map<String, Double> globalProfit = db.getGlobalUnitProfit();
        List<String> items = new ArrayList<>(db.getAllItems());
        Collections.sort(items);

        // Build singleton utility-lists and compute singleton EU
        Map<String, UL> ulists = new LinkedHashMap<>();
        Map<String, Double> singletonEU = new LinkedHashMap<>();
        for (int tid = 0; tid < transactions.size(); tid++) {
            Map<String, ItemData> trans = transactions.get(tid);
            for (String it : trans.keySet()) {
                UL ul = ulists.computeIfAbsent(it, k -> new UL());
                ItemData d = trans.get(it);
                ul.entries.add(new UL.Entry(tid, d.probability, d.utility));
            }
        }
        for (String it : items) {
            UL ul = ulists.get(it);
            double eu = 0.0;
            if (ul != null) {
                for (UL.Entry e : ul.entries) eu += e.p * e.u;
            }
            singletonEU.put(it, eu);
        }

        // Order items by ascending singletonEU for join order (common heuristic)
        items.sort(Comparator.comparingDouble(singletonEU::get));

        // Priority queue to store top-K (min-heap by EU)
        PriorityQueue<ItemsetResult> topK = new PriorityQueue<>(Comparator.comparingDouble(a -> a.expectedUtility));
        // Map to store utilities for closed check later
        Map<Set<String>, double[]> utilitiesMap = new LinkedHashMap<>();
        long evaluated = 0L;

        // DFS recursion starting from each singleton
        for (int i = 0; i < items.size(); i++) {
            String it = items.get(i);
            UL ul = ulists.get(it);
            if (ul == null || ul.entries.isEmpty()) continue;
            Set<String> prefix = new LinkedHashSet<>();
            prefix.add(it);
            double euPrefix = euFromUL(ul);
            double esPrefix = esFromUL(ul);
            utilitiesMap.put(new LinkedHashSet<>(prefix), new double[]{esPrefix, euPrefix});
            evaluated++;

            // update topK
            offerTopK(topK, new ItemsetResult(prefix, esPrefix, euPrefix), K);

            // compute remaining singleton sum UB for pruning
            double remainingSum = 0.0;
            for (int j = i+1; j < items.size(); j++) remainingSum += singletonEU.getOrDefault(items.get(j), 0.0);

            dfs(prefix, ul, i+1, items, ulists, singletonEU, topK, utilitiesMap, K, maxLen, evaluated, remainingSum);
        }

        // Build result list from PQ (descending)
        List<ItemsetResult> res = new ArrayList<>(topK);
        res.sort((a,b) -> Double.compare(b.expectedUtility, a.expectedUtility));

        // Filter closed itemsets
        Map<Set<String>, double[]> closed = new LinkedHashMap<>();
        for (Set<String> s : utilitiesMap.keySet()) {
            if (isClosed(s, utilitiesMap)) closed.put(s, utilitiesMap.get(s));
        }
        // From closed select top-K by EU
        List<ItemsetResult> topClosed = new ArrayList<>();
        for (Map.Entry<Set<String>, double[]> e : closed.entrySet()) {
            topClosed.add(new ItemsetResult(e.getKey(), e.getValue()[0], e.getValue()[1]));
        }
        topClosed.sort((a,b) -> Double.compare(b.expectedUtility, a.expectedUtility));
        if (topClosed.size() > K) topClosed = topClosed.subList(0, K);

        long time = System.currentTimeMillis() - start;
        return new AlgorithmResult(name, topClosed, time, utilitiesMap.size());
    }

    // DFS helper
    private void dfs(Set<String> prefix, UL prefixUL, int startIndex, List<String> items,
                     Map<String, UL> ulists, Map<String, Double> singletonEU,
                     PriorityQueue<ItemsetResult> topK, Map<Set<String>, double[]> utilitiesMap,
                     int K, int maxLen, long evaluatedCounter, double remainingSum) {

        if (prefix.size() >= maxLen) return;

        for (int idx = startIndex; idx < items.size(); idx++) {
            String it = items.get(idx);
            if (prefix.contains(it)) continue;
            UL other = ulists.get(it);
            if (other == null) continue;
            UL joined = joinUL(prefixUL, other);
            if (joined.entries.isEmpty()) continue;

            double euJoined = euFromUL(joined);
            double esJoined = esFromUL(joined);

            // Upper bound: euJoined + sum of singletonEU of items after idx
            double rem = 0.0;
            for (int j = idx+1; j < items.size(); j++) rem += singletonEU.getOrDefault(items.get(j), 0.0);
            double ub = euJoined + rem;
            double currentMin = topK.isEmpty() || topK.size() < K ? 0.0 : topK.peek().expectedUtility;

            // prune if UB <= currentMin
            if (ub <= currentMin) continue;

            // record utility
            Set<String> newSet = new LinkedHashSet<>(prefix);
            newSet.add(it);
            utilitiesMap.put(newSet, new double[]{esJoined, euJoined});
            evaluatedCounter++;

            // update topK
            offerTopK(topK, new ItemsetResult(newSet, esJoined, euJoined), K);

            // recurse deeper
            dfs(newSet, joined, idx+1, items, ulists, singletonEU, topK, utilitiesMap, K, maxLen, evaluatedCounter, rem);
        }
    }

    // join two ULs
    private UL joinUL(UL a, UL b) {
        UL out = new UL();
        Map<Integer, UL.Entry> mapA = new HashMap<>();
        for (UL.Entry e : a.entries) mapA.put(e.tid, e);
        for (UL.Entry eb : b.entries) {
            UL.Entry ea = mapA.get(eb.tid);
            if (ea != null) {
                double p = ea.p * eb.p;
                double u = ea.u + eb.u;
                out.entries.add(new UL.Entry(ea.tid, p, u));
            }
        }
        return out;
    }

    private double euFromUL(UL ul) {
        double sum = 0.0;
        for (UL.Entry e : ul.entries) sum += e.p * e.u;
        return sum;
    }

    private double esFromUL(UL ul) {
        double sum = 0.0;
        for (UL.Entry e : ul.entries) sum += e.p;
        return sum;
    }

    private void offerTopK(PriorityQueue<ItemsetResult> pq, ItemsetResult r, int K) {
        if (pq.size() < K) { pq.offer(r); return; }
        if (r.expectedUtility > pq.peek().expectedUtility) {
            pq.poll();
            pq.offer(r);
        }
    }

    // closed check: no strict superset with same ES and EU
    private boolean isClosed(Set<String> s, Map<Set<String>, double[]> utilitiesMap) {
        double[] base = utilitiesMap.get(s);
        if (base == null) return true;
        for (Set<String> other : utilitiesMap.keySet()) {
            if (other.size() <= s.size()) continue;
            if (other.containsAll(s)) {
                double[] ou = utilitiesMap.get(other);
                if (Math.abs(ou[0] - base[0]) < 1e-9 && Math.abs(ou[1] - base[1]) < 1e-9) return false;
            }
        }
        return true;
    }
}
