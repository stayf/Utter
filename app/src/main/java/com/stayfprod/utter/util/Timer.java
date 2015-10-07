package com.stayfprod.utter.util;


import android.util.Log;

public class Timer {

    static long start;
    static long rem;

    public static void start() {
        start = System.currentTimeMillis();
    }

    public static void rem() {
        long end = System.nanoTime();
        rem = (((end - start)) / 1000);
    }

    public static void showRem() {
        AndroidUtil.showToastShort(rem + "");
    }

    public static void show(Object o, boolean... inUi) {
        long end = System.nanoTime();
        if (inUi.length > 0 && inUi[0]) {
            AndroidUtil.showToastShort((((end - start)) / 1000) + "");
        }
        Log.e("Timer", o.getClass().getSimpleName() + ": " + (((end - start)) / 1000));
    }
}
