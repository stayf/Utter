package com.stayfprod.utter.service;

import android.annotation.SuppressLint;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;

import java.lang.reflect.Constructor;

@SuppressLint("NewApi")
public class StaticLayoutFactory {
    private static final String LOG = StaticLayoutFactory.class.getSimpleName();

    private static Constructor staticLayoutConstructor;
    private static Object FIRST_STRONG_LTR;

    static {
        try {
            Class textDir;
            if (App.CURRENT_VERSION_SDK < 18) {
                ClassLoader classLoader = StaticLayoutFactory.class.getClassLoader();
                textDir = classLoader.loadClass("android.text.TextDirectionHeuristic");
                FIRST_STRONG_LTR = classLoader.loadClass("android.text.TextDirectionHeuristics").getField("FIRSTSTRONG_LTR").get(null);
            } else {
                textDir = TextDirectionHeuristic.class;
                FIRST_STRONG_LTR = TextDirectionHeuristics.FIRSTSTRONG_LTR;
            }

            Class[] paramTypes = new Class[]{
                    CharSequence.class,
                    int.class,
                    int.class,
                    TextPaint.class,
                    int.class,
                    Layout.Alignment.class,
                    textDir,
                    float.class,
                    float.class,
                    boolean.class,
                    TextUtils.TruncateAt.class,
                    int.class,
                    int.class
            };

            staticLayoutConstructor = StaticLayout.class.getDeclaredConstructor(paramTypes);
            staticLayoutConstructor.setAccessible(true);
        } catch (Throwable e) {
            Log.e(LOG, "init", e);
            Crashlytics.logException(e);
        }
    }

    private static StaticLayout createLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align,
                                             float spacingmult, float spacingadd, TextUtils.TruncateAt ellipsize, int maxLines) {
        try {
            return (StaticLayout) staticLayoutConstructor.newInstance(source, 0, source.length(), paint, width, align,
                    FIRST_STRONG_LTR, spacingmult, spacingadd,
                    false, ellipsize, width, maxLines);
        } catch (Throwable e) {
            Log.e(LOG, "createStaticLayout", e);
            Crashlytics.logException(e);
        }
        return null;
    }

    public static StaticLayout createSimpleLayout(CharSequence source, TextPaint paint, int width, TextUtils.TruncateAt ellipsize, int... maxLines) {
        if (maxLines.length > 0) {
            int max = maxLines[0];
            //info косячит если нужна одна строка(не всегда выводит одну)
            StaticLayout staticLayout = createLayout(source, paint, width,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                    ellipsize, maxLines[0]);

            if (staticLayout != null) {
                if (staticLayout.getLineCount() > max) {
                    int line = max - 1;
                    return new StaticLayout(staticLayout.getText()
                            .subSequence(0, staticLayout.getOffsetForHorizontal(line, staticLayout.getLineWidth(line)) + 1)
                            , paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
            }
            return staticLayout;
        } else {
            return new StaticLayout(source, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
    }
}
