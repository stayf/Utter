package com.stayfprod.emojicon.emoji;

import java.io.Serializable;

public class Emojicon implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mEmoji;

    private Emojicon() {
    }

    public static Emojicon fromCodePoint(int codePoint) {
        Emojicon emoji = new Emojicon();
        emoji.mEmoji = newString(codePoint);
        return emoji;
    }

    public static Emojicon fromChar(char ch) {
        Emojicon emoji = new Emojicon();
        emoji.mEmoji = Character.toString(ch);
        return emoji;
    }

    public static Emojicon fromChars(String chars) {
        Emojicon emoji = new Emojicon();
        emoji.mEmoji = chars;
        return emoji;
    }

    public Emojicon(String emoji) {
        this.mEmoji = emoji;
    }

    public String getEmoji() {
        return mEmoji;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Emojicon && mEmoji.equals(((Emojicon) o).mEmoji);
    }

    @Override
    public int hashCode() {
        return mEmoji.hashCode();
    }

    public static String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }
}
