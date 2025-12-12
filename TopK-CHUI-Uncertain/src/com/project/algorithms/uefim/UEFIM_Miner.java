package com.project.algorithms.uefim;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.algorithms.base.TopKQueue;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MemoryLogger;

import java.util.*;

public class UEFIM_Miner extends MiningAlgorithm {

    private int[][] transactions;
    private double[][] utilities;
    private Map<Integer, Double> mapItemToTWU;

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        Stats stats = new Stats("U-EFIM");
        MemoryLogger.getInstance().reset();
        long start = System.currentTimeMillis();

        this.topKBuffer = new TopKQueue(k);
        this.minUtility = 0;
        this.mapItemToTWU = new HashMap<>();

        // 1. Tính TWU
        for (Transaction t : db.getTransactions()) {
            double tu = t.getTransactionUtility(); 
            for (Item item : t.getItems()) {
                double contribution = tu * item.getProbability();
                mapItemToTWU.put(item.getItemId(), mapItemToTWU.getOrDefault(item.getItemId(), 0.0) + contribution);
            }
        }

        // 2. Convert to Arrays
        int numTrans = db.getTransactions().size();
        transactions = new int[numTrans][];
        utilities = new double[numTrans][];
        double[] originalTrWeights = new double[numTrans];

        int i = 0;
        for (Transaction t : db.getTransactions()) {
            List<Item> sortedItems = new ArrayList<>(t.getItems());
            sortedItems.sort((a, b) -> Double.compare(mapItemToTWU.get(a.getItemId()), mapItemToTWU.get(b.getItemId())));

            int size = sortedItems.size();
            transactions[i] = new int[size];
            utilities[i] = new double[size];
            
            double sumUtil = 0;
            for (int j = 0; j < size; j++) {
                Item item = sortedItems.get(j);
                transactions[i][j] = item.getItemId();
                utilities[i][j] = item.getExpectedUtility();
                sumUtil += utilities[i][j];
            }
            originalTrWeights[i] = sumUtil;
            i++;
        }

        double[] initialPrefixUtils = new double[numTrans]; 
        search(transactions, utilities, originalTrWeights, initialPrefixUtils, new ArrayList<>());

        MemoryLogger.getInstance().checkMemory();
        long end = System.currentTimeMillis();

        stats.setRuntime(end - start);
        stats.setMemory(MemoryLogger.getInstance().getMaxMemory());
        stats.setPatternCount(topKBuffer.size());
        stats.setMinUtilThreshold(minUtility);

        return stats;
    }

    private void search(int[][] trans, double[][] utils, double[] weights, 
                        double[] utilityOfPrefix, List<Integer> prefix) {
        
        // A. Tính Local Utility & Upper Bounds
        Map<Integer, Double> potentialHighUtils = new HashMap<>();
        Map<Integer, Double> subTreeUtility = new HashMap<>();

        for (int i = 0; i < trans.length; i++) {
            double pUtil = utilityOfPrefix[i];
            for (int j = 0; j < trans[i].length; j++) {
                int item = trans[i][j];
                double u = utils[i][j];
                
                potentialHighUtils.put(item, potentialHighUtils.getOrDefault(item, 0.0) + (pUtil + u));
                subTreeUtility.put(item, subTreeUtility.getOrDefault(item, 0.0) + weights[i]);
            }
        }

        // B. Lọc Secondary Items
        List<Integer> secondaryItems = new ArrayList<>();
        for (Integer item : subTreeUtility.keySet()) {
            if (subTreeUtility.get(item) >= minUtility) {
                secondaryItems.add(item);
            }
        }
        secondaryItems.sort((a, b) -> Double.compare(mapItemToTWU.get(a), mapItemToTWU.get(b)));

        // C. Depth-First Search
        for (int itemX : secondaryItems) {
            
            // 1. Check Top-K
            double exactUtility = potentialHighUtils.getOrDefault(itemX, 0.0);
            if (exactUtility >= minUtility) {
                List<Integer> newPattern = new ArrayList<>(prefix);
                newPattern.add(itemX);
                int support = countSupport(trans, itemX);
                savePattern(new Itemset(newPattern, exactUtility, support));
            }

            // 2. Projection 
            // Dùng List tạm để chứa dữ liệu chiếu
            List<int[]> nextTransList = new ArrayList<>();
            List<double[]> nextUtilsList = new ArrayList<>();
            List<Double> nextWeightsList = new ArrayList<>();
            List<Double> nextPrefixUtilsList = new ArrayList<>();

            for (int k = 0; k < trans.length; k++) {
                int idx = -1;
                for (int z = 0; z < trans[k].length; z++) {
                    if (trans[k][z] == itemX) {
                        idx = z;
                        break;
                    }
                }

                if (idx != -1) {
                    // Tìm các item hợp lệ phía sau itemX
                    List<Integer> pItems = new ArrayList<>();
                    List<Double> pUtils = new ArrayList<>();
                    double remainingSum = 0;

                    for (int z = idx + 1; z < trans[k].length; z++) {
                        int itemY = trans[k][z];
                        if (subTreeUtility.getOrDefault(itemY, 0.0) >= minUtility) {
                            pItems.add(itemY);
                            pUtils.add(utils[k][z]);
                            remainingSum += utils[k][z];
                        }
                    }

                    if (!pItems.isEmpty()) {
                        // Chuyển List sang array ngay lập tức
                        int[] pItemsArr = pItems.stream().mapToInt(i->i).toArray();
                        double[] pUtilsArr = pUtils.stream().mapToDouble(d->d).toArray();

                        // Tính Prefix Utility mới cho dòng này
                        double newPrefixUtil = utilityOfPrefix[k] + utils[k][idx];
                        double newWeight = newPrefixUtil + remainingSum;

                        nextTransList.add(pItemsArr);
                        nextUtilsList.add(pUtilsArr);
                        nextWeightsList.add(newWeight);
                        nextPrefixUtilsList.add(newPrefixUtil);
                    }
                }
            }

            // 3. Recursive Call
            if (!nextTransList.isEmpty()) {
                // Convert List of arrays to Array of arrays
                int[][] nextTrans = nextTransList.toArray(new int[0][]);
                double[][] nextUtils = nextUtilsList.toArray(new double[0][]);
                
                // Convert List<Double> to double[]
                double[] nextWeights = nextWeightsList.stream().mapToDouble(d->d).toArray();
                double[] nextPrefixUtils = nextPrefixUtilsList.stream().mapToDouble(d->d).toArray();
                
                List<Integer> nextPrefix = new ArrayList<>(prefix);
                nextPrefix.add(itemX);
                
                search(nextTrans, nextUtils, nextWeights, nextPrefixUtils, nextPrefix);
            }
        }
    }

    private int countSupport(int[][] trans, int itemX) {
        int count = 0;
        for (int[] row : trans) {
            for (int item : row) if (item == itemX) { count++; break; }
        }
        return count;
    }
}