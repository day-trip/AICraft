package com.daytrip.aicraft.natives;

import static com.daytrip.aicraft.natives.Natives.INSTANCE;

public class Chunk {
    public static void build(int x, int y, byte[] data) {
        INSTANCE.chunk_build(x, y, data, data.length);
    }

    public static void remove(int x, int y) {
        INSTANCE.chunk_remove(x, y);
    }

    public static void set(int x, int y, int z, byte value) {
        INSTANCE.chunk_set(x, y, z, value);
    }
}
