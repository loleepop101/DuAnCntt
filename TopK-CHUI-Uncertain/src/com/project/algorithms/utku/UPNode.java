package com.project.algorithms.utku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UPNode {
    private final int itemId;
    private double nodeUtility;     // Accumulated Expected Utility
    private double expectedSupport; // Accumulated Expected Support (Probability sum)
    
    private List<UPNode> children = null;
    private UPNode parent;
    private UPNode nodeLink; 

    public UPNode(int itemId, double nodeUtility, double expectedSupport) {
        this.itemId = itemId;
        this.nodeUtility = nodeUtility;
        this.expectedSupport = expectedSupport;
        this.nodeLink = null;
    }

    public void increaseUtility(double util, double prob) {
        this.nodeUtility += util;
        this.expectedSupport += prob;
    }

    public int getItemId() { return itemId; }
    public double getNodeUtility() { return nodeUtility; }
    public double getExpectedSupport() { return expectedSupport; }

    public UPNode getParent() { return parent; }
    public void setParent(UPNode parent) { this.parent = parent; }

    public UPNode getNodeLink() { return nodeLink; }
    public void setNodeLink(UPNode nodeLink) { this.nodeLink = nodeLink; }

    public List<UPNode> getChildren() { 
        if (children == null) return Collections.emptyList();
        return children; 
    }
    
    public UPNode getChild(int itemId) {
        if (children == null) return null;
        // Linear scan is acceptable here as branching factor in pattern trees is usually small
        for (UPNode child : children) {
            if (child.getItemId() == itemId) {
                return child;
            }
        }
        return null;
    }

    public void addChild(UPNode child) {
        if (children == null) {
            children = new ArrayList<>(1);
        }
        children.add(child);
        child.setParent(this);
    }
}