package com.project.algorithms.utko;

import java.util.ArrayList;
import java.util.List;

public class UtilityList {
    private int itemId;
    private List<Element> elements;
    private double sumIutils;
    private double sumRutils;

    public UtilityList(int itemId) {
        this.itemId = itemId;
        this.elements = new ArrayList<>();
        this.sumIutils = 0;
        this.sumRutils = 0;
    }

    public void addElement(Element element) {
        elements.add(element);
        sumIutils += element.getIutils();
        sumRutils += element.getRutils();
    }

    public int getItemId() { return itemId; }
    public double getSumIutils() { return sumIutils; }
    public double getSumRutils() { return sumRutils; }
    
    // Tổng tiềm năng (Upper Bound) dùng để cắt tỉa
    public double getSumUtilities() {
        return sumIutils + sumRutils;
    }
    
    // Support chính là số lượng giao dịch chứa item này
    public int getSupport() {
        return elements.size();
    }

    public List<Element> getElements() {
        return elements;
    }
}