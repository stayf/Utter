package com.stayfprod.utter.manager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stayfprod.utter.ui.activity.EnterPassCodeActivity;
import com.stayfprod.utter.util.LockUtil;

@SuppressLint("CommitPrefEdits")
public class PassCodeManager {
    private static final String LOCKER = "LOCKER";
    private static final String AUTO_LOCK_TYPE = "AUTO_LOCK_TYPE";
    private static final String START_TIMER = "START_TIMER";
    private static final String LOCKED_BY_USER = "LOCKED_BY_USER";
    private static final String IS_ENABLED_PASS_CODE = "IS_ENABLED_PASS_CODE";
    private static final String PASS_CODE_TYPE = "PASS_CODE_TYPE";

    private static volatile PassCodeManager sPassCodeManager;

    public static final int AUTO_LOCK_DISABLE = 0;
    public static final int AUTO_LOCK_IN_5_MIN = 5;
    public static final int AUTO_LOCK_IN_15_MIN = 15;
    public static final int AUTO_LOCK_IN_30_MIN = 30;
    public static final int AUTO_LOCK_IN_60_MIN = 60;

    public static PassCodeManager getManager() {
        if (sPassCodeManager == null) {
            synchronized (PassCodeManager.class) {
                if (sPassCodeManager == null) {
                    sPassCodeManager = new PassCodeManager();
                }
            }
        }
        return sPassCodeManager;
    }

    public void setLockByUser(Context context, boolean val) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putBoolean(LOCKED_BY_USER, val).commit();
    }

    public boolean isLockedByUser(Context context) {
        return getSharedPreferences(context).getBoolean(LOCKED_BY_USER, false);
    }

    public void setEnablePassCode(Context context, boolean val, LockUtil.Type type) {
        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (!val) {
            editor.remove(PASS_CODE_TYPE);
        } else {
            editor.putString(PASS_CODE_TYPE, type.name());
        }
        editor.putBoolean(IS_ENABLED_PASS_CODE, val).commit();
    }

    public boolean isEnabledPassCode(Context context) {
        return getSharedPreferences(context).getBoolean(IS_ENABLED_PASS_CODE, false);
    }

    //для определения какой слой показывать при требовании пароля
    public LockUtil.Type getPassCodeType(Context context) {
        LockUtil.Type type = null;
        try {
            type = LockUtil.Type.valueOf(getSharedPreferences(context).getString(PASS_CODE_TYPE, ""));
        } catch (Exception e) {
            //
        }
        return type;
    }

    public void addPassCode(Object value, LockUtil.Type type, boolean isTemp) {
        LockUtil.savePassCode(value, type, isTemp);
    }

    public boolean comparePassCode(Object value, LockUtil.Type type, boolean isTemp) {
        return LockUtil.comparePassCode(value, type, isTemp);
    }

    public SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(LOCKER, Context.MODE_PRIVATE);
    }

    public int getCurrentAutoLockStatus(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getInt(AUTO_LOCK_TYPE, 0);
    }

    //определяем на какой период блочим
    public void setCurrentAutoLockType(Context context, int val) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putInt(AUTO_LOCK_TYPE, val).commit();
    }

    public void setCurrentAutoLockStartTimer(Context context, long val) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putLong(START_TIMER, val).commit();
    }

    public void setCurrentAutoLockStartTimerAsync(Context context, long val) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putLong(START_TIMER, val).apply();
    }

    private long getCurrentAutoLockStartTimer(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getLong(START_TIMER, 0);
    }

    //при открытии активити после сна
    public boolean isNeedPassCode(Context context) {
        //сначало проверяем не заблоченно ли юзером!!

        if (!isEnabledPassCode(context)) {
            return false;
        }

        if (isLockedByUser(context)) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long storageTime = getCurrentAutoLockStartTimer(context);

        int maxDiff = getCurrentAutoLockStatus(context);

        return maxDiff != 0 && (currentTime - storageTime) / (1000 * 60) >= maxDiff;
    }

    public void openEnterPassCodeActivity(Context context, boolean isInSettings) {
        AppCompatActivity activity = (AppCompatActivity) context;
        Intent intent = new Intent(context, EnterPassCodeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isInSettings", isInSettings);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

}
