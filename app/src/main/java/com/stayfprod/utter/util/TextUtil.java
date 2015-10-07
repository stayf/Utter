package com.stayfprod.utter.util;

import android.content.Context;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.stayfprod.utter.R;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

    public static String createGroupChatInitials(String title) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] strs = title.trim().replaceAll(" +", " ").split(" ");
        int l = strs.length > 2 ? 2 : strs.length;
        for (int i = 0; i < l; i++) {
            String str = strs[i];
            if (str.length() > 0) {
                stringBuilder.append(str.substring(0, 1));
            }
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String createUserInitials(TdApi.MessageContact messageContact) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isNotBlank(messageContact.firstName)) {
            stringBuilder.append(messageContact.firstName.substring(0, 1));
        }
        if (isNotBlank(messageContact.lastName)) {
            stringBuilder.append(messageContact.lastName.substring(0, 1));
        }

        return stringBuilder.toString();
    }

    public static String createUserInitials(TdApi.User user) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isNotBlank(user.firstName)) {
            stringBuilder.append(user.firstName.substring(0, 1));
        }
        if (isNotBlank(user.lastName)) {
            stringBuilder.append(user.lastName.substring(0, 1));
        }

        return stringBuilder.toString();
    }

    public static String createFullName(TdApi.MessageContact messageContact) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isNotBlank(messageContact.firstName)) {
            stringBuilder.append(messageContact.firstName);
        }

        if (isNotBlank(messageContact.lastName)) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
                stringBuilder.append(messageContact.lastName);
            }
        }

        if (isBlank(stringBuilder.toString())) {
            stringBuilder.append(AndroidUtil.getResourceString(R.string.incognito));
        }
        return stringBuilder.toString();
    }

    public static String createFullName(TdApi.User user) {

        StringBuilder stringBuilder = new StringBuilder();

        if (isNotBlank(user.firstName)) {
            stringBuilder.append(user.firstName);
        }

        if (isNotBlank(user.lastName)) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
                stringBuilder.append(user.lastName);
            }
        }

        if (isBlank(stringBuilder.toString())) {
            stringBuilder.append(AndroidUtil.getResourceString(R.string.incognito));
        }
        return stringBuilder.toString();
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(cs.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static List<SpannableString> breakByLines(TextPaint textPaint, SpannableString spannableString, float maxWidth) {
        List<SpannableString> stringList = new ArrayList<SpannableString>();
        int offset = 0;
        int rowCount = 0;
        while ((rowCount = textPaint.breakText(spannableString, offset, spannableString.length() - offset, true, maxWidth, null)) != 0) {
            offset += rowCount;
            stringList.add(new SpannableString(spannableString.subSequence(offset, rowCount)));
        }
        return stringList;
    }

    public static void ellipsizeTextForTwoOrientations(StringBuffer[] buffers, int[] maxWidth, TextPaint paint, String text) {
        if (buffers[0] == null || buffers[1] == null) {
            buffers[0] = new StringBuffer();
            buffers[1] = new StringBuffer();
        }
        buffers[0].setLength(0);
        buffers[1].setLength(0);

        buffers[0].append(TextUtils.ellipsize(text, paint, maxWidth[0], TextUtils.TruncateAt.END));
        buffers[1].append(TextUtils.ellipsize(text, paint, maxWidth[1], TextUtils.TruncateAt.END));
    }

    public static void ellipsizeTextForTwoOrientations(String[] strings, int[] maxWidth, TextPaint paint, String text) {
        strings[0] = TextUtils.ellipsize(text, paint, maxWidth[0], TextUtils.TruncateAt.END).toString();
        strings[1] = TextUtils.ellipsize(text, paint, maxWidth[1], TextUtils.TruncateAt.END).toString();
    }

    public static String replaceNewRowSymbols(String text) {
        //replace Нужен тк косячит StaticLayout
        return text.replaceAll("(\r\n|\n)", "");
    }

    public static void shakeText(final TextView textView) {
        Animation shake = AnimationUtils.loadAnimation(textView.getContext(), R.anim.shake);
        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        textView.startAnimation(shake);
        Vibrator vibe = (Vibrator) textView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 300/*, 300, 300*/};
        vibe.vibrate(pattern, -1);
    }
}
