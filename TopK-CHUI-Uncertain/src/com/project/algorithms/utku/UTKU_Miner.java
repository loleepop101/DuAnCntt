package com.project.algorithms.utku;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MathUtils;

import java.util.*;

public class UTKU_Miner extends MiningAlgorithm {

    private Dataset database; // Reference to DB for verify phase

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        this.database = db; 
        long start = System.currentTimeMillis();
        
        setup(k);

        // Step 1: Calculate Global ETWU
        this.mapItemToTWU = db.calculateTWUs();

        // Step 2: Build Global UP-Tree
        UPTree tree = new UPTree();
        
        for (Transaction t : db.getTransactions()) {
            List<Item> sortedItems = new ArrayList<>();
            for (Item item : t.getItems()) {
                // Basic pruning using minUtility
                if (MathUtils.greaterThanOrEqual(mapItemToTWU.get(item.getItemId()), minUtility)) {
                    sortedItems.add(item);
                }
            }
            // Sort Descending by ETWU
            sortedItems.sort((a, b) -> Double.compare(
                mapItemToTWU.get(b.getItemId()), 
                mapItemToTWU.get(a.getItemId())
            ));

            if (!sortedItems.isEmpty()) {
                tree.addTransaction(sortedItems);
            }
        }

        // Step 3: Recursive Mining
        // Start with empty int[] prefix
        mine(tree, new int[0]);

        return createStats("U-TKU", start);
    }

    /**
     * Recursive mining function using int[] for memory efficiency.
     */
    private void mine(UPTree tree, int[] prefix) {
        // 1. Traverse Header Table Bottom-Up
        List<Integer> items = new ArrayList<>(tree.getHeaderTable().keySet());
        
        // Sort Ascending by ETWU
        items.sort((a, b) -> Double.compare(
            mapItemToTWU.get(a), 
            mapItemToTWU.get(b)
        ));

        for (Integer itemId : items) {
            // Create new pattern: Prefix + Current Item
            int[] newPattern = new int[prefix.length + 1];
            System.arraycopy(prefix, 0, newPattern, 0, prefix.length);
            newPattern[prefix.length] = itemId;

            // Estimate Utility
            double estimatedUtility = calculateEstimatedUtility(tree, itemId);

            // Pruning
            if (MathUtils.greaterThanOrEqual(estimatedUtility, minUtility)) {
                
                // Verify with original database (Phase 2)
                verifyAndAddResult(newPattern);

                // Build Conditional Tree
                UPTree conditionalTree = buildConditionalTree(tree, itemId);

                // Recursion
                if (conditionalTree.getRoot().getChildren().size() > 0) {
                    mine(conditionalTree, newPattern);
                }
            }
        }
    }

    private UPTree buildConditionalTree(UPTree tree, int itemId) {
        UPTree condTree = new UPTree();
        UPNode currentNode = tree.getHeaderTable().get(itemId);
        
        while (currentNode != null) {
            List<Item> path = new ArrayList<>();
            UPNode parent = currentNode.getParent();
            
            while (parent.getItemId() != -1) { 
                // Path value logic specific to UP-Tree (simplified for U-TKU)
                double pathVal = currentNode.getNodeUtility(); 
                
                // Note: creating temporary Item objects here is necessary for the tree API,
                // but acceptable as these are short-lived.
                path.add(0, new Item(parent.getItemId(), pathVal, 1.0)); 
                
                parent = parent.getParent();
            }

            if (!path.isEmpty()) {
                condTree.addTransaction(path);
            }

            currentNode = currentNode.getNodeLink();
        }
        
        return condTree;
    }

    private double calculateEstimatedUtility(UPTree tree, int itemId) {
        double sum = 0;
        UPNode node = tree.getHeaderTable().get(itemId);
        while (node != null) {
            sum += node.getNodeUtility();
            node = node.getNodeLink();
        }
        return sum;
    }

    /**
     * Updated Verification: Uses int[] and optimized Transaction methods.
     */
    private void verifyAndAddResult(int[] candidateItems) {
        double actualExpectedUtility = 0;
        double expectedSupport = 0;

        for (Transaction t : database.getTransactions()) {
            // OPTIMIZATION: Use Transaction's internal optimized check
            if (t.containsAll(candidateItems)) {
                // OPTIMIZATION: Use Transaction's internal optimized calculation
                actualExpectedUtility += t.calculateUtilityOf(candidateItems);
                expectedSupport += t.calculateProbabilityOf(candidateItems);
            }
        }

        // Check Top-K condition
        if (MathUtils.greaterThanOrEqual(actualExpectedUtility, minUtility)) {
            Itemset newItemset = new Itemset(candidateItems, actualExpectedUtility, expectedSupport);
            
            // Delegate "Closed" check to the generic parent method
            savePattern(newItemset); 
        }
    }
}