package com.demo.asm.plugin;

/**
 * Created by guoxiaodong on 2020/9/3 11:25
 */
public class Util {
    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
}
