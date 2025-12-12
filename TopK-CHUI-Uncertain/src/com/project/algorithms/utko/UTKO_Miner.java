package com.project.algorithms.utko;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.algorithms.base.TopKQueue;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MemoryLogger;

import java.util.*;

public class UTKO_Miner extends MiningAlgorithm {

    private Map<Integer, Integer> mapItemToRank;

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        Stats stats = new Stats("U-TKO");
        MemoryLogger.getInstance().reset();
        long start = System.currentTimeMillis();

        this.topKBuffer = new TopKQueue(k);
        this.minUtility = 0;
        this.mapItemToRank = new HashMap<>();

        // Bước 1: Tính TWU
        Map<Integer, Double> mapTWU = new HashMap<>();
        for (Transaction t : db.getTransactions()) {
            double tu = t.getTransactionUtility(); 
            for (Item item : t.getItems()) {
                double contribution = tu * item.getProbability();
                mapTWU.put(item.getItemId(), mapTWU.getOrDefault(item.getItemId(), 0.0) + contribution);
            }
        }

        List<Integer> allItems = new ArrayList<>(mapTWU.keySet());
        allItems.sort((a, b) -> Double.compare(mapTWU.get(a), mapTWU.get(b)));

        for (int i = 0; i < allItems.size(); i++) {
            mapItemToRank.put(allItems.get(i), i);
        }

        // Bước 2: Xây dựng Utility List ban đầu
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
            sortedItems.sort((a, b) -> Integer.compare(
                mapItemToRank.get(a.getItemId()), 
                mapItemToRank.get(b.getItemId())
            ));

            double remainingUtility = 0;
            for (int i = sortedItems.size() - 1; i >= 0; i--) {
                Item item = sortedItems.get(i);
                double expectedUtil = item.getExpectedUtility();
                
                Element element = new Element(tid, expectedUtil, remainingUtility);
                mapIdToUL.get(item.getItemId()).addElement(element);

                remainingUtility += expectedUtil;
            }
            tid++;
        }

        // Bước 3: Mining Đệ quy
        search(listOfUtilityLists, new ArrayList<>(), null);

        MemoryLogger.getInstance().checkMemory();
        long end = System.currentTimeMillis();

        stats.setRuntime(end - start);
        stats.setMemory(MemoryLogger.getInstance().getMaxMemory());
        stats.setPatternCount(topKBuffer.size());
        stats.setMinUtilThreshold(minUtility);

        return stats;
    }

    /**
     * @param prefixUL: UtilityList của phần tiền tố (dùng để trừ phần trùng lặp)
     */
    private void search(List<UtilityList> uls, List<Integer> prefixIds, UtilityList prefixUL) {
        for (int i = 0; i < uls.size(); i++) {
            UtilityList X = uls.get(i);

            if (X.getSumUtilities() < minUtility) continue;

            List<Integer> newPattern = new ArrayList<>(prefixIds);
            newPattern.add(X.getItemId());
            
            if (X.getSumIutils() >= minUtility) {
                Itemset itemset = new Itemset(newPattern, X.getSumIutils(), X.getSupport());
                savePattern(itemset);
            }

            if (X.getSumUtilities() >= minUtility) {
                List<UtilityList> nextULs = new ArrayList<>();
                
                for (int j = i + 1; j < uls.size(); j++) {
                    UtilityList Y = uls.get(j);
                    
                    UtilityList Z = construct(X, Y, prefixUL);
                    
                    if (Z != null && Z.getSumUtilities() >= minUtility) {
                        nextULs.add(Z);
                    }
                }
                
                if (!nextULs.isEmpty()) {
                    search(nextULs, newPattern, X);
                }
            }
        }
    }

    /**
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
        
        while (idx1 < size1 && idx2 < size2) {
            Element e1 = list1.get(idx1);
            Element e2 = list2.get(idx2);
            
            if (e1.getTid() < e2.getTid()) {
                idx1++;
            } else if (e1.getTid() > e2.getTid()) {
                idx2++;
            } else {
                double prefixUtil = 0;
                if (listP != null) {
                    while (idxP < sizeP && listP.get(idxP).getTid() < e1.getTid()) {
                        idxP++;
                    }
                    if (idxP < sizeP && listP.get(idxP).getTid() == e1.getTid()) {
                        prefixUtil = listP.get(idxP).getIutils();
                    }
                }

                double newIutils = e1.getIutils() + e2.getIutils() - prefixUtil;
                double newRutils = e2.getRutils();
                
                Element newElement = new Element(e1.getTid(), newIutils, newRutils);
                result.addElement(newElement);
                
                idx1++;
                idx2++;
            }
        }
        
        return result.getElements().isEmpty() ? null : result;
    }
}