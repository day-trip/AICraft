package com.daytrip.aicraft.natives;

import jnr.ffi.LibraryLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.util.Arrays;

public class Natives {
    public static NativeLib INSTANCE = null;

    public static void init() {
        System.out.println(Arrays.toString(FabricLoaderImpl.INSTANCE.getModContainer("aicraft").get().getRootPaths().toArray()));
        /*String osName = System.getProperty("os.name").toLowerCase();

        // Determine the native file suffix based on the operating system
        String nativeSuffix = null;
        if (osName.contains("win")) {
            nativeSuffix = "dll";
        } else if (osName.contains("mac")) {
            nativeSuffix = "dylib";
        } else if (osName.contains("nix") || osName.contains("nux")) {
            nativeSuffix = "so";
        }
        if (nativeSuffix == null) {
            throw new RuntimeException("Unknown operating system.");
        }

        URL url = Natives.class.getResource("/pathlib." + nativeSuffix);
        System.out.println(url);
        System.out.println(url.getPath());
        if (true) {
            throw new RuntimeException("HA HA JOKES ON YOU! LOLLL");
        }*/
        if (INSTANCE != null) {
            INSTANCE.dealloc();
        }
        INSTANCE = LibraryLoader.create(NativeLib.class).load("C:\\Users\\jai\\IdeaProjects\\AiCraft\\nativelib\\target\\release\\pathlib.dll");
        INSTANCE.init();
    }
}
