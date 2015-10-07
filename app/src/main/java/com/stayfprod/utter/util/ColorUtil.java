package com.stayfprod.utter.util;

import android.graphics.Color;

public class ColorUtil {

    private static int getMiddleValue(int prev, int next, float factor) {
        return Math.round((float)prev + (float)(next - prev) * factor);
    }

    public static int getMiddleColor(int prevColor, int curColor, float factor) {
        if(prevColor == curColor) {
            return curColor;
        } else if(factor == 0.0F) {
            return prevColor;
        } else if(factor == 1.0F) {
            return curColor;
        } else {
            int a = getMiddleValue(Color.alpha(prevColor), Color.alpha(curColor), factor);
            int r = getMiddleValue(Color.red(prevColor), Color.red(curColor), factor);
            int g = getMiddleValue(Color.green(prevColor), Color.green(curColor), factor);
            int b = getMiddleValue(Color.blue(prevColor), Color.blue(curColor), factor);
            return Color.argb(a, r, g, b);
        }
    }

    public static int getColor(int baseColor, float alphaPercent) {
        int alpha = Math.round((float)Color.alpha(baseColor) * alphaPercent);
        return baseColor & 16777215 | alpha << 24;
    }
}