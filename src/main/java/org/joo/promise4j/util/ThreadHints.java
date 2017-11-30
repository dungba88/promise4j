package org.joo.promise4j.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ThreadHints {

    private static final MethodHandle ON_SPIN_WAIT_METHOD_HANDLE;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle methodHandle = null;
        try {
            methodHandle = lookup.findStatic(Thread.class, "onSpinWait", MethodType.methodType(void.class));
        } catch (final Exception ignore) {
        }

        ON_SPIN_WAIT_METHOD_HANDLE = methodHandle;
    }

    private ThreadHints() {
    }

    /**
     * Indicates that the caller is momentarily unable to progress, until the
     * occurrence of one or more actions on the part of other activities. By
     * invoking this method within each iteration of a spin-wait loop construct, the
     * calling thread indicates to the runtime that it is busy-waiting. The runtime
     * may take action to improve the performance of invoking spin-wait loop
     * constructions.
     */
    public static void onSpinWait() {
        if (null != ON_SPIN_WAIT_METHOD_HANDLE) {
            try {
                ON_SPIN_WAIT_METHOD_HANDLE.invokeExact();
            } catch (final Throwable ignore) {
            }
        }
    }
}
