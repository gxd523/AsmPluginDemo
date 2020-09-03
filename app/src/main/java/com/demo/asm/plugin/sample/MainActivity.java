package com.demo.asm.plugin.sample;

import android.app.Activity;
import android.os.Bundle;

import com.demo.asm.lib.TrackMethod;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    @TrackMethod(tag = "time")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < 2; i++) {
            new Thread(this::downloadTask).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @TrackMethod(tag = "static")
    private void downloadTask() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        IoUtil.copyFile();
    }
}