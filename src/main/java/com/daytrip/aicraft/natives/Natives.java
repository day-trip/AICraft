package com.daytrip.aicraft.natives;

import jnr.ffi.LibraryLoader;

public class Natives {
    public static NativeLib INSTANCE;

    public static void init() {
        INSTANCE = LibraryLoader.create(NativeLib.class).load("C:\\Users\\jai\\IdeaProjects\\AiCraft\\nativelib\\target\\release\\pathlib.dll");
        INSTANCE.init();
    }
}
