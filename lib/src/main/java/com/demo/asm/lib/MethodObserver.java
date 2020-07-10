package com.demo.asm.lib;

public interface MethodObserver {
    default void onMethodStart(String tag, String methodName) {
    }

    default void onMethodEnd(String tag, String methodName) {
    }
}
