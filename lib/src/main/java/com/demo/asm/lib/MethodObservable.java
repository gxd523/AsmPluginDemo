package com.demo.asm.lib;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一个被观察者，对应多种事件(key)，每个事件对应多个观察者
 */
public enum MethodObservable {
    INSTANCE;
    private final Map<String, List<MethodObserver>> methodObserverListMap = new HashMap<>();

    public void addMethodObserver(MethodObserver observer) {
        addMethodObserver(observer, "addFromListDefaultTag");
    }

    public void addMethodObserver(MethodObserver observer, String tag) {
        if (observer == null) {
            return;
        }

        List<MethodObserver> methodObserverList = methodObserverListMap.get(tag);
        if (methodObserverList == null) {
            methodObserverList = new ArrayList<>();
        }
        methodObserverList.add(observer);
        methodObserverListMap.put(tag, methodObserverList);
    }

    public void notifyMethodStart(String tag, String methodName) {
        List<MethodObserver> methodObserverList = methodObserverListMap.get(tag);
        if (methodObserverList == null) {
            return;
        }
        for (MethodObserver methodObserver : methodObserverList) {
            methodObserver.onMethodStart(tag, methodName);
        }
    }

    public void notifyMethodEnd(String tag, String methodName) {
        List<MethodObserver> methodObserverList = methodObserverListMap.get(tag);
        if (methodObserverList == null) {
            return;
        }
        for (MethodObserver observer : methodObserverList) {
            observer.onMethodEnd(tag, methodName);
        }
    }
}
