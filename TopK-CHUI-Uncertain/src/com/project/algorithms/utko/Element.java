package com.project.algorithms.utko;

public class Element {
    private int tid;
    private double iutils;
    private double rutils;

    public Element(int tid, double iutils, double rutils) {
        this.tid = tid;
        this.iutils = iutils;
        this.rutils = rutils;
    }

    public int getTid() {
        return tid;
    }

    public double getIutils() {
        return iutils;
    }

    public double getRutils() {
        return rutils;
    }
}
