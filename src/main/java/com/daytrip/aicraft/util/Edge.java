package com.daytrip.aicraft.util;


public record Edge(EdgeType type, int index, Double weight) {
    public enum EdgeType {
        CRAFT,
        MOB,
        CHEST,
        BLOCK
    }
}
