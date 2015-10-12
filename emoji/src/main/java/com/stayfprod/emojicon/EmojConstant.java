package com.stayfprod.emojicon;


import android.content.Context;

public class EmojConstant {
    private static float sDensity;
    public static int sEmojDpChat;
    public static int sEmojDpSimpleList;
    public static int sEmojDpLayoutSize;
    public static int sEmojGridWidth;

    public static int sStickerThumbWidth;
    public static int sStickerThumbHeight;

    public static void init(Context context) {
        sDensity = context.getResources().getDisplayMetrics().density;
        sEmojDpChat = dp(20);
        sEmojDpSimpleList = dp(32);
        sEmojDpLayoutSize = dp(42);
        sEmojGridWidth = sEmojDpLayoutSize;
        sStickerThumbWidth = dp(70);
        sStickerThumbHeight = dp(70);
    }

    public static int dp(float val) {
        return (int) (sDensity * val + 0.5f);
    }

}
