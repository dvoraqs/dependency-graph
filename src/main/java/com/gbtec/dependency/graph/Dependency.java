package com.gbtec.dependency.graph;

public class Dependency {

    private final String from;
    private final String to;

    public Dependency(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String to(){
        return to;
    }

    public String from(){
        return from;
    }
}
