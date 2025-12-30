package com.project.algorithms.utko;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MathUtils;

import java.util.*;

public class UTKO_Miner extends MiningAlgorithm {

    private Map<Integer, Integer> mapItemToRank;

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        long start = System.currentTimeMillis();

        setup(k);
        this.mapItemToRank = new HashMap<>();

        // Step 1: Calculate TWU (Transaction Weighted Utilization)
        this.mapItemToTWU = db.calculateTWUs();

        List<Integer> allItems = new ArrayList<>(mapItemToTWU.keySet());
        // Sort Ascending by TWU for efficient pruning
        allItems.sort((a, b) -> Double.compare(mapItemToTWU.get(a), mapItemToTWU.get(b)));

        for (int i = 0; i < allItems.size(); i++) {
            mapItemToRank.put(allItems.get(i), i);
        }

        // Step 2: Build Initial Utility Lists
        List<UtilityList> listOfUtilityLists = new ArrayList<>();
        Map<Integer, UtilityList> mapIdToUL = new HashMap<>();

        for (Integer itemId : allItems) {
            UtilityList ul = new UtilityList(itemId);
            mapIdToUL.put(itemId, ul);
            listOfUtilityLists.add(ul);
        }

        int tid = 0;
        for (Transaction t : db.getTransactions()) {
            List<Item> sortedItems = new ArrayList<>();
            for (Item item : t.getItems()) {
                if (mapItemToRank.containsKey(item.getItemId())) {
                    sortedItems.add(item);
                }
            }
            // Sort items in transaction by Rank (Ascending)
            sortedItems.sort((a, b) -> Integer.compare(
                mapItemToRank.get(a.getItemId()), 
                mapItemToRank.get(b.getItemId())
            ));

            double remainingUtility = 0;
            // Iterate backwards to calculate Remaining Utility
            for (int i = sortedItems.size() - 1; i >= 0; i--) {
                Item item = sortedItems.get(i);
                double rawUtil = item.getUtility();
                double prob = item.getProbability();
                
                // For U-TKO: Element = {tid, sumUtility, prodProbability, rutil}
                Element element = new Element(tid, rawUtil, prob, remainingUtility);
                mapIdToUL.get(item.getItemId()).addElement(element);

                remainingUtility += item.getExpectedUtility();
            }
            tid++;
        }

        // Step 3: Recursive Mining
        // Start with empty int[] prefix
        search(listOfUtilityLists, new int[0], null);

        return createStats("U-TKO", start);
    }

    /**
     * Recursive Search Method
     * @param uls List of UtilityLists for extensions
     * @param prefixIds Current prefix pattern (int[])
     * @param prefixUL UtilityList of the prefix (used for intersection math)
     */
    private void search(List<UtilityList> uls, int[] prefixIds, UtilityList prefixUL) {
        for (int i = 0; i < uls.size(); i++) {
            UtilityList X = uls.get(i);

            // Pruning: If sum(iutils + rutils) < minUtility, this branch is dead.
            if (MathUtils.lessThan(X.getSumUtilities(), minUtility)) {
                continue;
            }

            // Create new pattern: Prefix + X
            int[] newPattern = new int[prefixIds.length + 1];
            System.arraycopy(prefixIds, 0, newPattern, 0, prefixIds.length);
            newPattern[prefixIds.length] = X.getItemId();
            
            // 1. Check if this pattern itself is a High Utility Itemset
            if (MathUtils.greaterThanOrEqual(X.getSumIutils(), minUtility)) {
                Itemset itemset = new Itemset(newPattern, X.getSumIutils(), X.getExpectedSupport());
                savePattern(itemset); // Generic method handles Closed check & Top-K logic
            }

            // 2. Try to extend this pattern
            if (MathUtils.greaterThanOrEqual(X.getSumUtilities(), minUtility)) {
                List<UtilityList> nextULs = new ArrayList<>();
                
                for (int j = i + 1; j < uls.size(); j++) {
                    UtilityList Y = uls.get(j);
                    
                    // Construct Z = X U Y
                    UtilityList Z = construct(X, Y, prefixUL);
                    
                    // Pruning on Child (Z)
                    if (Z != null && MathUtils.greaterThanOrEqual(Z.getSumUtilities(), minUtility)) {
                        nextULs.add(Z);
                    }
                }
                
                // Recurse
                if (!nextULs.isEmpty()) {
                    search(nextULs, newPattern, X);
                }
            }
        }
    }

    /**
     * Constructs the Utility List for Z = X U Y.
     * Formula: IUtil(Z) = IUtil(X) + IUtil(Y) - IUtil(Prefix)
     */
    private UtilityList construct(UtilityList ul1, UtilityList ul2, UtilityList prefixUL) {
        UtilityList result = new UtilityList(ul2.getItemId());
        
        List<Element> list1 = ul1.getElements();
        List<Element> list2 = ul2.getElements();
        List<Element> listP = (prefixUL != null) ? prefixUL.getElements() : null;
        
        int idx1 = 0, idx2 = 0, idxP = 0;
        int size1 = list1.size();
        int size2 = list2.size();
        int sizeP = (listP != null) ? listP.size() : 0;
        
        // Linear Join (Intersection of TIDs)
        while (idx1 < size1 && idx2 < size2) {
            Element e1 = list1.get(idx1);
            Element e2 = list2.get(idx2);
            
            if (e1.getTid() < e2.getTid()) {
                idx1++;
            } else if (e1.getTid() > e2.getTid()) {
                idx2++;
            } else {
                // Same TID -> Join
                
                // Find corresponding prefix element (if exists)
                double prefixSumUtil = 0;
                double prefixProdProb = 1.0;
                if (listP != null) {
                    while (idxP < sizeP && listP.get(idxP).getTid() < e1.getTid()) {
                        idxP++;
                    }
                    if (idxP < sizeP && listP.get(idxP).getTid() == e1.getTid()) {
                        prefixSumUtil = listP.get(idxP).getSumUtility();
                        prefixProdProb = listP.get(idxP).getProdProbability();
                    }
                }

                // Apply Formula for Multiplicative Model
                // Sum of utilities: u(X U Y) = u(X) + u(Y) - u(Prefix)
                double newSumUtility = e1.getSumUtility() + e2.getSumUtility() - prefixSumUtil;
                double newRutils = e2.getRutils(); // RUtil depends only on the extension item (Y)
                
                // Product of probabilities: p(X U Y) = p(X) * p(Y) / p(Prefix)
                double newProdProb = (MathUtils.greaterThan(prefixProdProb, 0)) ? 
                                 (e1.getProdProbability() * e2.getProdProbability()) / prefixProdProb : 0;
                
                Element newElement = new Element(e1.getTid(), newSumUtility, newProdProb, newRutils);
                result.addElement(newElement);
                
                idx1++;
                idx2++;
            }
        }
        
        return result.getElements().isEmpty() ? null : result;
    }
}