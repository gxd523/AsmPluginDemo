package com.demo.asm.plugin;

/**
 * Created by Tanzhenxing
 * Date: 2020-03-03 10:07
 * Description:
 */
public class Logger {
    public static boolean isDebug = false;

    public static void gxd(String tag) {
        if (isDebug) {
            System.out.println(String.format("gxd...%s", tag));
        }
    }

    public static void log(String tag, String s) {
        logs(tag, s);
    }

    public static void log(String tag, String s, String s2) {
        logs(tag, s, s2);
    }

    public static void logs(String tag, String... s) {
        if (!isDebug) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(tag);
        if (s != null && s.length > 0) {
            for (String ss : s) {
                stringBuilder.append(" ").append(ss);
            }
        }
        System.out.println(stringBuilder.toString());
    }
}
