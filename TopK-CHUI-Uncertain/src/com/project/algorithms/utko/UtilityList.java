package com.project.algorithms.utko;

import java.util.ArrayList;
import java.util.List;

public class UtilityList {
    private final int itemId;
    private final List<Element> elements;
    private double sumExpectedUtility; // Sum of EU(X, T) across transactions
    private double sumRutils;          // Sum of remaining utility bounds
    private double sumExpectedSupport; // Sum of product probabilities (Expected Support)

    public UtilityList(int itemId) {
        this.itemId = itemId;
        this.elements = new ArrayList<>();
        this.sumExpectedUtility = 0;
        this.sumRutils = 0;
        this.sumExpectedSupport = 0;
    }

    public void addElement(Element element) {
        elements.add(element);
        sumExpectedUtility += element.getExpectedUtility();
        sumRutils += element.getRutils();
        sumExpectedSupport += element.getProdProbability();
    }

    public int getItemId() { return itemId; }
    public double getSumIutils() { return sumExpectedUtility; }
    public double getSumRutils() { return sumRutils; }
    
    // Total Potential Utility (Upper Bound) used for pruning
    public double getSumUtilities() {
        return sumExpectedUtility + sumRutils;
    }
    
    // Expected Support is the sum of probabilities across transactions
    public double getExpectedSupport() {
        return sumExpectedSupport;
    }

    public List<Element> getElements() {
        return elements;
    }
}