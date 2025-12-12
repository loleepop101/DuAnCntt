package com.project.algorithms.utku;

import java.util.ArrayList;
import java.util.List;

public class UPNode {
    private int itemId;
    private double nodeUtility; // Tích lũy Expected Utility tại node này
    private int count;          // Support count 
    
    private List<UPNode> children;
    private UPNode parent;
    private UPNode nodeLink; 

    public UPNode(int itemId, double nodeUtility) {
        this.itemId = itemId;
        this.nodeUtility = nodeUtility;
        this.count = 1;
        this.children = new ArrayList<>();
        this.nodeLink = null;
    }

    public void increaseUtility(double value) {
        this.nodeUtility += value;
        this.count++;
    }

    public int getItemId() { return itemId; }
    public double getNodeUtility() { return nodeUtility; }
    public int getCount() { return count; }

    public UPNode getParent() { return parent; }
    public void setParent(UPNode parent) { this.parent = parent; }

    public UPNode getNodeLink() { return nodeLink; }
    public void setNodeLink(UPNode nodeLink) { this.nodeLink = nodeLink; }

    public List<UPNode> getChildren() { return children; }
    
    public UPNode getChild(int itemId) {
        for (UPNode child : children) {
            if (child.getItemId() == itemId) {
                return child;
            }
        }
        return null;
    }

    public void addChild(UPNode child) {
        children.add(child);
        child.setParent(this);
    }
}