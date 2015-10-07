package com.stayfprod.emojicon;


import android.content.Context;

public class EmojiConstants {
    private static float DENSITY;
    public static int EMOJI_DP_CHAT;
    public static int EMOJI_DP_SMILE_LIST;
    public static int EMOJI_DP_LAYOUT_SIZE;
    public static int EMOJI_GRID_VIEW_WIDTH;

    public static int STICKER_THUMB_WIDTH;
    public static int STICKER_THUMB_HEIGHT;

    public static void init(Context context) {
        DENSITY = context.getResources().getDisplayMetrics().density;
        EMOJI_DP_CHAT = dp(20);
        EMOJI_DP_SMILE_LIST = dp(32);
        EMOJI_DP_LAYOUT_SIZE = dp(42);
        EMOJI_GRID_VIEW_WIDTH = EMOJI_DP_LAYOUT_SIZE;
        STICKER_THUMB_WIDTH = dp(70);
        STICKER_THUMB_HEIGHT = dp(70);
    }

    public static int dp(float val) {
        return (int) (DENSITY * val + 0.5f);
    }

}
