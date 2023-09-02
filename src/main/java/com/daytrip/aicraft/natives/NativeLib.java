package com.daytrip.aicraft.natives;

public interface NativeLib {
    void init();

    void pf_init(State start, State goal);

    void pf_update_cell(State cell, float cost);

    void pf_update_start(State start);

    void pf_update_goal(State goal);

    short pf_replan();

    int pf_get_path_len();

    void pf_get_path(State[] arr);

    int pf_get_debug_len();

    void pf_get_debug(State[] arr);

    void chunk_build(int x, int y, byte[] arr, int length);

    void chunk_remove(int x, int y);

    void chunk_set(int x, int y, int z, byte value);
}
