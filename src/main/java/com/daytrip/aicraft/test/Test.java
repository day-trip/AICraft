package com.daytrip.aicraft.test;

public class Test {
    public static void main(String[] args) {
        long time = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            System.out.println("Hello world!");
        }
        System.out.println(System.nanoTime() - time);
    }
}
