// Generated by jextract

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$42 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$42() {}
    static final FunctionDescriptor ultoa$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG$LAYOUT
    );
    static final MethodHandle ultoa$MH = RuntimeHelper.downcallHandle(
        "ultoa",
        constants$42.ultoa$FUNC
    );
    static final FunctionDescriptor putenv$FUNC = FunctionDescriptor.of(Constants$root.C_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle putenv$MH = RuntimeHelper.downcallHandle(
        "putenv",
        constants$42.putenv$FUNC
    );
    static final FunctionDescriptor onexit$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle onexit$MH = RuntimeHelper.downcallHandle(
        "onexit",
        constants$42.onexit$FUNC
    );
    static final FunctionDescriptor init_native$FUNC = FunctionDescriptor.ofVoid();
    static final MethodHandle init_native$MH = RuntimeHelper.downcallHandle(
        "init_native",
        constants$42.init_native$FUNC
    );
    static final FunctionDescriptor init_pathfinder$FUNC = FunctionDescriptor.ofVoid(
        MemoryLayout.structLayout(
            Constants$root.C_LONG_LONG$LAYOUT.withName("x"),
            Constants$root.C_LONG_LONG$LAYOUT.withName("y"),
            Constants$root.C_LONG_LONG$LAYOUT.withName("z"),
            Constants$root.C_DOUBLE$LAYOUT.withName("k0"),
            Constants$root.C_DOUBLE$LAYOUT.withName("k1")
        ).withName("State"),
        MemoryLayout.structLayout(
            Constants$root.C_LONG_LONG$LAYOUT.withName("x"),
            Constants$root.C_LONG_LONG$LAYOUT.withName("y"),
            Constants$root.C_LONG_LONG$LAYOUT.withName("z"),
            Constants$root.C_DOUBLE$LAYOUT.withName("k0"),
            Constants$root.C_DOUBLE$LAYOUT.withName("k1")
        ).withName("State")
    );
    static final MethodHandle init_pathfinder$MH = RuntimeHelper.downcallHandle(
        "init_pathfinder",
        constants$42.init_pathfinder$FUNC
    );
    static final FunctionDescriptor blank$FUNC = FunctionDescriptor.of(MemoryLayout.structLayout(
        Constants$root.C_LONG_LONG$LAYOUT.withName("x"),
        Constants$root.C_LONG_LONG$LAYOUT.withName("y"),
        Constants$root.C_LONG_LONG$LAYOUT.withName("z"),
        Constants$root.C_DOUBLE$LAYOUT.withName("k0"),
        Constants$root.C_DOUBLE$LAYOUT.withName("k1")
    ).withName("State"));
    static final MethodHandle blank$MH = RuntimeHelper.downcallHandle(
        "blank",
        constants$42.blank$FUNC
    );
}

