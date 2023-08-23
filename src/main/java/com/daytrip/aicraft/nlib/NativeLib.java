package com.daytrip.aicraft.nlib;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface NativeLib extends Library {
    NativeLib INSTANCE = Native.load("lib", NativeLib.class);

    int treble(int value);
}
