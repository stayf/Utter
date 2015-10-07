package com.stayfprod.utter.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureUtils;
import android.gesture.Prediction;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.ui.view.LockPatternView;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LockUtils {
    private static final String LOG = LockUtils.class.getSimpleName();

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String FILE_GESTURE = "gesture";
    private static final String PASS_CODE = "PASS_CODE";
    private static final String TEMP = "TEMP_";

    public static final String UTF8 = "UTF-8";
    public static final String SHA1 = "SHA-1";

    public enum Type {
        PASSWORD,
        PIN,
        PATTERN,
        GESTURE
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        List<LockPatternView.Cell> result = new ArrayList<LockPatternView.Cell>();
        try {
            final byte[] bytes = string.getBytes(UTF8);
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                result.add(LockPatternView.Cell.of(b / 3, b % 3));
            }
        } catch (UnsupportedEncodingException e) {
            //
        }
        return result;
    }

    public static String patternToSha1(List<LockPatternView.Cell> pattern) {
        return sha1Hash(patternToStringForHash(pattern));
    }

    private static String sha1Hash(String toHash) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA1);
            byte[] bytes = toHash.getBytes(UTF8);
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            hash = bytesToHex(bytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            //
        }
        return hash;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.row * 3 + cell.column);
        }
        try {
            return new String(res, UTF8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String patternToStringForHash(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();
        StringBuilder stringBuilder = new StringBuilder(patternSize);

        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            stringBuilder.append(cell.row * 3 + cell.column);
        }
        return stringBuilder.toString();
    }

    @SuppressLint("CommitPrefEdits")
    @SuppressWarnings("unchecked")
    public static void savePassCode(Object value, Type type, boolean isTemp) {
        SharedPreferences preferences = App.getAppContext().getSharedPreferences(PASS_CODE, Context.MODE_PRIVATE);
        String prefix = isTemp ? TEMP : "";
        switch (type) {
            case PASSWORD:
                String inputPassword = (String) value;
                preferences.edit().putString(prefix + type.name(), sha1Hash(inputPassword)).commit();
                break;
            case PIN:
                String inputPin = (String) value;
                preferences.edit().putString(prefix + type.name(), sha1Hash(inputPin)).commit();
                break;
            case PATTERN:
                List<LockPatternView.Cell> inputPattern = (List<LockPatternView.Cell>) value;
                preferences.edit().putString(prefix + type.name(), patternToSha1(inputPattern)).commit();
                break;
            case GESTURE:
                try {
                    Gesture inputGesture = (Gesture) value;
                    GestureLibrary gestureLibrary = GestureLibraries.fromFile(FileUtils.createFile(App.STG_FOLDER_MAIN, FILE_GESTURE));
                    try {
                        gestureLibrary.load();
                    } catch (Exception e) {
                        Log.w(LOG, "", e);
                    }

                    if (isTemp) {
                        gestureLibrary.removeEntry(prefix + type.name());
                    } else {
                        gestureLibrary.removeEntry(type.name());
                        gestureLibrary.removeEntry(TEMP + type.name());
                    }

                    gestureLibrary.addGesture(prefix + type.name(), inputGesture);
                    gestureLibrary.save();
                } catch (Exception e) {
                    Log.e(LOG, "", e);
                    Crashlytics.logException(e);
                }
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean comparePassCode(Object value, Type type, boolean isTemp) {
        SharedPreferences preferences = App.getAppContext().getSharedPreferences(PASS_CODE, Context.MODE_PRIVATE);
        String prefix = isTemp ? TEMP : "";
        switch (type) {
            case PASSWORD:
                String inputPassword = (String) value;
                String inputPassHash = sha1Hash(inputPassword);
                String stgHashPass = preferences.getString(prefix + type.name(), "");
                return inputPassHash.equals(stgHashPass);
            case PIN:
                String inputPin = (String) value;
                String inputPinHash = sha1Hash(inputPin);
                String stgHashPin = preferences.getString(prefix + type.name(), "");
                return inputPinHash.equals(stgHashPin);
            case PATTERN:
                List<LockPatternView.Cell> inputPattern = (List<LockPatternView.Cell>) value;
                String inputPatternHash = patternToSha1(inputPattern);
                String stgHashPattern = preferences.getString(prefix + type.name(), "");
                return inputPatternHash.equals(stgHashPattern);
            case GESTURE:
                try {
                    Gesture inputGesture = (Gesture) value;
                    GestureLibrary gestureLibrary = GestureLibraries.fromFile(FileUtils.createFile(App.STG_FOLDER_MAIN, FILE_GESTURE));
                    try {
                        gestureLibrary.load();
                    } catch (Exception e) {
                        Log.w(LOG, "", e);
                    }

                    ArrayList<Prediction> predictions = gestureLibrary.recognize(inputGesture);

                    if (predictions.size() > 0) {
                        Prediction prediction = predictions.get(0);
                        if (prediction.score > 1.5) {
                            if (prediction.name.equals(prefix + type.name())) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG, "", e);
                    Crashlytics.logException(e);
                }
                return false;
        }
        return false;
    }
}
