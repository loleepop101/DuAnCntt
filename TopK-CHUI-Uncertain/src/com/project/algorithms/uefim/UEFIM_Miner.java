package com.project.algorithms.uefim;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MathUtils;

import java.util.*;

public class UEFIM_Miner extends MiningAlgorithm {

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        long start = System.currentTimeMillis();

        setup(k);

        // 1. Calculate TWU (Transaction Weighted Utilization)
        this.mapItemToTWU = db.calculateTWUs();

        // 2. Convert Database to Primitive Arrays (Optimization)
        int numTrans = db.getTransactions().size();
        int[][] transactions = new int[numTrans][];
        double[][] utilities = new double[numTrans][];
        double[][] probabilities = new double[numTrans][];
        double[] originalTrWeights = new double[numTrans];

        int i = 0;
        for (Transaction t : db.getTransactions()) {
            // Sort items by TWU (Ascending)
            List<Item> sortedItems = new ArrayList<>(t.getItems());
            sortedItems.sort((a, b) -> Double.compare(mapItemToTWU.get(a.getItemId()), mapItemToTWU.get(b.getItemId())));

            int size = sortedItems.size();
            transactions[i] = new int[size];
            utilities[i] = new double[size];
            probabilities[i] = new double[size];
            
            double sumExpectedUtil = 0;
            for (int j = 0; j < size; j++) {
                Item item = sortedItems.get(j);
                transactions[i][j] = item.getItemId();
                utilities[i][j] = item.getUtility(); // Store raw utility
                probabilities[i][j] = item.getProbability();
                sumExpectedUtil += item.getExpectedUtility();
            }
            originalTrWeights[i] = sumExpectedUtil;
            i++;
        }

        // 3. Start Search with empty prefix
        double[] initialPrefixUtils = new double[numTrans]; 
        double[] initialPrefixProbs = new double[numTrans];
        Arrays.fill(initialPrefixProbs, 1.0); // Empty prefix has probability 1.0

        search(transactions, utilities, probabilities, originalTrWeights, initialPrefixUtils, initialPrefixProbs, new int[0]);

        return createStats("U-EFIM", start);
    }

    /**
     * Recursive Depth-First Search for High Utility Itemsets
     * @param trans Projected transactions (Items)
     * @param utils Projected utilities (Expected Utility)
     * @param probs Projected probabilities
     * @param weights Sub-tree weights (Pruning Power)
     * @param utilityOfPrefix Utility of the prefix pattern in each transaction
     * @param probabilityOfPrefix Probability of the prefix pattern in each transaction
     * @param prefix Current pattern prefix
     */
    private void search(int[][] trans, double[][] utils, double[][] probs, double[] weights, 
                        double[] sumUtilityOfPrefix, double[] prodProbabilityOfPrefix, int[] prefix) {
        
        // A. Calculate Local Utility & Sub-tree Utility
        Map<Integer, Double> localUtility = new HashMap<>();
        Map<Integer, Double> subTreeUtility = new HashMap<>();

        for (int i = 0; i < trans.length; i++) {
            double pSumUtil = sumUtilityOfPrefix[i];
            double pProdProb = prodProbabilityOfPrefix[i];
            
            for (int j = 0; j < trans[i].length; j++) {
                int item = trans[i][j];
                double u = utils[i][j];
                double p = probs[i][j];
                
                // EU(Prefix U {item}, T) = (sumU_prefix + u_item) * (prodP_prefix * p_item)
                double expectedUtil = (pSumUtil + u) * (pProdProb * p);
                
                localUtility.put(item, localUtility.getOrDefault(item, 0.0) + expectedUtil);
                subTreeUtility.put(item, subTreeUtility.getOrDefault(item, 0.0) + weights[i]);
            }
        }

        // B. Identify Secondary Items (Items that can be extended)
        List<Integer> secondaryItems = new ArrayList<>();
        for (Integer item : subTreeUtility.keySet()) {
            if (MathUtils.greaterThanOrEqual(subTreeUtility.get(item), minUtility)) {
                secondaryItems.add(item);
            }
        }
        secondaryItems.sort((a, b) -> Double.compare(mapItemToTWU.get(a), mapItemToTWU.get(b)));

        // C. Depth-First Search loop
        for (int itemX : secondaryItems) {
            
            double exactUtility = localUtility.getOrDefault(itemX, 0.0);
            
            int[] newPattern = new int[prefix.length + 1];
            System.arraycopy(prefix, 0, newPattern, 0, prefix.length);
            newPattern[prefix.length] = itemX;

            if (MathUtils.greaterThanOrEqual(exactUtility, minUtility)) {
                double expectedSupport = calculateExpectedSupport(trans, probs, prodProbabilityOfPrefix, itemX);
                savePattern(new Itemset(newPattern, exactUtility, expectedSupport));
            }

            // 2. Database Projection
            int[][] nextTrans = new int[trans.length][];
            double[][] nextUtils = new double[trans.length][];
            double[][] nextProbs = new double[trans.length][];
            double[] nextWeights = new double[trans.length];
            double[] nextPrefixSumUtils = new double[trans.length];
            double[] nextPrefixProdProbs = new double[trans.length];
            
            int validTransCount = 0;

            for (int k = 0; k < trans.length; k++) {
                int idx = -1;
                for (int z = 0; z < trans[k].length; z++) {
                    if (trans[k][z] == itemX) {
                        idx = z;
                        break;
                    }
                }

                if (idx != -1) {
                    int countValid = 0;
                    double remainingExpectedSum = 0;

                    for (int z = idx + 1; z < trans[k].length; z++) {
                        int itemY = trans[k][z];
                        if (MathUtils.greaterThanOrEqual(subTreeUtility.getOrDefault(itemY, 0.0), minUtility)) {
                            countValid++;
                            // Bound uses sum of expected utilities of remaining items
                            remainingExpectedSum += (utils[k][z] * probs[k][z]);
                        }
                    }

                    if (countValid > 0) {
                        int[] pItems = new int[countValid];
                        double[] pUtils = new double[countValid];
                        double[] pProbs = new double[countValid];
                        int c = 0;
                        
                        for (int z = idx + 1; z < trans[k].length; z++) {
                            int itemY = trans[k][z];
                            if (MathUtils.greaterThanOrEqual(subTreeUtility.getOrDefault(itemY, 0.0), minUtility)) {
                                pItems[c] = itemY;
                                pUtils[c] = utils[k][z];
                                pProbs[c] = probs[k][z];
                                c++;
                            }
                        }

                        double newPrefixSumUtil = sumUtilityOfPrefix[k] + utils[k][idx];
                        double newPrefixProdProb = prodProbabilityOfPrefix[k] * probs[k][idx];
                        
                        // Pruning bound: EU(Prefix, T) + sum(EU(remaining items, T))
                        double newWeight = (newPrefixSumUtil * newPrefixProdProb) + remainingExpectedSum;

                        nextTrans[validTransCount] = pItems;
                        nextUtils[validTransCount] = pUtils;
                        nextProbs[validTransCount] = pProbs;
                        nextWeights[validTransCount] = newWeight;
                        nextPrefixSumUtils[validTransCount] = newPrefixSumUtil;
                        nextPrefixProdProbs[validTransCount] = newPrefixProdProb;
                        validTransCount++;
                    }
                }
            }

            if (validTransCount > 0) {
                int[][] finalNextTrans = Arrays.copyOfRange(nextTrans, 0, validTransCount);
                double[][] finalNextUtils = Arrays.copyOfRange(nextUtils, 0, validTransCount);
                double[][] finalNextProbs = Arrays.copyOfRange(nextProbs, 0, validTransCount);
                double[] finalNextWeights = Arrays.copyOfRange(nextWeights, 0, validTransCount);
                double[] finalNextPrefixSumUtils = Arrays.copyOfRange(nextPrefixSumUtils, 0, validTransCount);
                double[] finalNextPrefixProdProbs = Arrays.copyOfRange(nextPrefixProdProbs, 0, validTransCount);
                
                search(finalNextTrans, finalNextUtils, finalNextProbs, finalNextWeights, finalNextPrefixSumUtils, finalNextPrefixProdProbs, newPattern);
            }
        }
    }

    private double calculateExpectedSupport(int[][] trans, double[][] probs, double[] probOfPrefix, int itemX) {
        double expectedSupport = 0;
        for (int i = 0; i < trans.length; i++) {
            for (int j = 0; j < trans[i].length; j++) {
                if (trans[i][j] == itemX) {
                    expectedSupport += probOfPrefix[i] * probs[i][j];
                    break;
                }
            }
        }
        return expectedSupport;
    }
}