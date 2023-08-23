package com.daytrip.aicraft.util;


public record Edge(EdgeType type, Double weight) {
    public enum EdgeType {
        CRAFT,
        DROP,
        CHEST
    }
}
