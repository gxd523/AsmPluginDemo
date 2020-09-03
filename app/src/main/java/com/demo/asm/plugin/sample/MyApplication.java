package com.demo.asm.plugin.sample;

import android.app.Application;

import com.demo.asm.lib.MethodObservable;
import com.demo.asm.plugin.sample.observer.DefaultObserver;
import com.demo.asm.plugin.sample.observer.StaticObserver;
import com.demo.asm.plugin.sample.observer.TimeObserver;

/**
 * Created by guoxiaodong on 2020/7/9 18:03
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MethodObservable.INSTANCE.addMethodObserver(new TimeObserver(), "time");
        MethodObservable.INSTANCE.addMethodObserver(new StaticObserver(), "static");
        MethodObservable.INSTANCE.addMethodObserver(new DefaultObserver());
    }
}
