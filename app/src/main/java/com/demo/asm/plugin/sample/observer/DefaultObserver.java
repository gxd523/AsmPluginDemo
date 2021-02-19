package com.demo.asm.plugin.sample.observer;

import android.util.Log;

import com.demo.asm.lib.MethodObserver;

import java.util.HashMap;
import java.util.Map;

public class DefaultObserver implements MethodObserver {
    private final Map<String, Long> methodTimeMap = new HashMap<>();

    @Override
    public void onMethodStart(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        methodTimeMap.put(key, System.currentTimeMillis());
    }

    @Override
    public void onMethodEnd(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        Long startTime = methodTimeMap.get(key);
        if (startTime == null) {
            throw new IllegalStateException("method exit without enter");
        }
        long duration = System.currentTimeMillis() - startTime;
        Log.d("gxd", String.format("Default...%s...%sms", methodName, duration));
        methodTimeMap.remove(key);
    }

    private String generateKey(String tag, String methodName) {
        return String.format("%s-%s-%s", tag, methodName, Thread.currentThread().getName());
    }
}
