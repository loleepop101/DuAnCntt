package com.project.algorithms.base;

import com.project.model.Dataset;
import com.project.model.Itemset;
import com.project.utils.MathUtils;
import com.project.utils.MemoryLogger;

import java.util.*;

/**
 * Base class for all Top-K High Utility Itemset mining algorithms.
 * Provides common functionality for Top-K maintenance and Closed Itemset checking.
 */
public abstract class MiningAlgorithm {
    protected TopKQueue topKBuffer;
    protected double minUtility = 0;
    
    /** Stores TWU values for sorting consistency across all miners */
    protected Map<Integer, Double> mapItemToTWU;

    /** 
     * Index patterns by expected support to optimize Closed Constraint checking.
     * Key: Expected Support -> Value: List of Itemsets with that support
     */
    protected Map<Double, List<Itemset>> closedIndex = new HashMap<>(); 

    public abstract Stats runAlgorithm(Dataset db, int k);

    /**
     * Initializes common buffers and resets memory logging.
     */
    protected void setup(int k) {
        this.topKBuffer = new TopKQueue(k);
        this.minUtility = 0;
        this.closedIndex.clear();
        MemoryLogger.getInstance().reset();
    }

    /**
     * Populates a Stats object with performance metrics after algorithm execution.
     */
    protected Stats createStats(String algoName, long startTime) {
        Stats stats = new Stats(algoName);
        MemoryLogger.getInstance().checkMemory();
        stats.setRuntime(System.currentTimeMillis() - startTime);
        stats.setMemory(MemoryLogger.getInstance().getMaxMemory());
        stats.setPatternCount(topKBuffer.size());
        stats.setMinUtilThreshold(minUtility);
        return stats;
    }

    /**
     * Handles Top-K maintenance and Closed Constraint checking for a discovered pattern.
     * Uses support-based indexing for efficient forward and backward checks.
     */
    protected void savePattern(Itemset candidate) {
        // Early exit if the candidate cannot enter the Top-K queue
        if (topKBuffer.isFull() && !MathUtils.greaterThan(candidate.getUtility(), minUtility)) {
             return;
        }

        // Closed Constraint Check: A pattern X is closed if no superset Y exists with the same support.
        List<Itemset> sameSupportGroup = findGroup(candidate.getExpectedSupport());

        if (sameSupportGroup != null) {
            Iterator<Itemset> it = sameSupportGroup.iterator();
            while (it.hasNext()) {
                Itemset existing = it.next();

                // Forward Check: Is candidate a subset of an existing closed pattern?
                if (MathUtils.isSubsetSorted(candidate.getItems(), existing.getItems())) {
                    return; 
                }

                // Backward Check: Is candidate a superset of an existing pattern?
                if (MathUtils.isSubsetSorted(existing.getItems(), candidate.getItems())) {
                    topKBuffer.remove(existing); 
                    it.remove(); 
                }
            }
            // Cleanup if group became empty after backward checks
            if (sameSupportGroup.isEmpty()) {
                removeGroup(candidate.getExpectedSupport());
            }
        }

        // Add to queue and handle eviction if necessary
        Itemset evicted = topKBuffer.add(candidate);
        
        if (topKBuffer.getQueue().contains(candidate)) { 
            addToIndex(candidate);
            
            if (evicted != null) {
                removeFromIndex(evicted);
            }
            
            if (topKBuffer.isFull()) {
                minUtility = topKBuffer.peek().getUtility();
            }
        }
    }

    // Helper: Find group with epsilon-aware support check
    private List<Itemset> findGroup(double support) {
        for (Map.Entry<Double, List<Itemset>> entry : closedIndex.entrySet()) {
            if (MathUtils.equals(entry.getKey(), support)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // Helper: Remove group with epsilon-aware support check
    private void removeGroup(double support) {
        Double keyToRemove = null;
        for (Double key : closedIndex.keySet()) {
            if (MathUtils.equals(key, support)) {
                keyToRemove = key;
                break;
            }
        }
        if (keyToRemove != null) {
            closedIndex.remove(keyToRemove);
        }
    }

    // Helper: Add to index
    private void addToIndex(Itemset itemset) {
        double support = itemset.getExpectedSupport();
        List<Itemset> group = findGroup(support);
        if (group == null) {
            group = new ArrayList<>();
            closedIndex.put(support, group);
        }
        group.add(itemset);
    }

    // Helper: Remove from index (Fix for Memory Leak)
    private void removeFromIndex(Itemset itemset) {
        List<Itemset> group = findGroup(itemset.getExpectedSupport());
        if (group != null) {
            group.remove(itemset);
            if (group.isEmpty()) {
                removeGroup(itemset.getExpectedSupport());
            }
        }
    }
}