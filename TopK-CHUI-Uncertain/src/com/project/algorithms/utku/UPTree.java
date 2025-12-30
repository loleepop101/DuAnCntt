package com.project.algorithms.utku;

import com.project.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPTree {
    private final UPNode root;
    
    // Header table to access the first node of each item
    private final Map<Integer, UPNode> headerTable; 
    
    // Helper map to access the last node of each item (for fast linking)
    private final Map<Integer, UPNode> lastNodeLink; 

    public UPTree() {
        this.root = new UPNode(-1, 0, 0);
        this.headerTable = new HashMap<>();
        this.lastNodeLink = new HashMap<>();
    }

    /**
     * Inserts a sorted transaction into the tree.
     */
    public void addTransaction(List<Item> sortedItems) {
        UPNode currentNode = root;
        double currentPrefixProb = 1.0;

        for (Item item : sortedItems) {
            int itemId = item.getItemId();
            double expectedUtil = item.getExpectedUtility();
            double itemProb = item.getProbability();
            
            double nodeProb = currentPrefixProb * itemProb;

            UPNode child = currentNode.getChild(itemId);

            if (child == null) {
                // Create new node
                child = new UPNode(itemId, expectedUtil, nodeProb);
                currentNode.addChild(child);

                // Update Header Table & Node Links
                updateHeaderLink(child);
            } else {
                // Node exists -> Accumulate utility and probability
                child.increaseUtility(expectedUtil, nodeProb);
            }

            currentNode = child; // Move down
            currentPrefixProb = nodeProb; // Update prefix probability for next item
        }
    }

    private void updateHeaderLink(UPNode newNode) {
        int itemId = newNode.getItemId();
        if (headerTable.containsKey(itemId)) {
            // Append to the tail of the link list
            UPNode last = lastNodeLink.get(itemId);
            last.setNodeLink(newNode);
            lastNodeLink.put(itemId, newNode);
        } else {
            // First node of this item
            headerTable.put(itemId, newNode);
            lastNodeLink.put(itemId, newNode);
        }
    }

    public UPNode getRoot() { return root; }
    public Map<Integer, UPNode> getHeaderTable() { return headerTable; }
}