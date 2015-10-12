package com.stayfprod.emojicon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.concurrent.ConcurrentHashMap;

public class EmojiCache {

    private static volatile EmojiCache sEmojiCache;
    private static final SparseArray<Bitmap> MANDATORY_BITMAP_CACHE = new SparseArray<Bitmap>();

    private final ConcurrentHashMap<String, EmojDrawable> emojIconCache = new ConcurrentHashMap<String, EmojDrawable>();

    public static EmojiCache getInstance() {
        if (sEmojiCache == null) {
            synchronized (EmojiCache.class) {
                if (sEmojiCache == null) {
                    sEmojiCache = new EmojiCache();
                }
            }
        }
        return sEmojiCache;
    }


    public void initEmojiBitmaps(Resources resources) {
        MANDATORY_BITMAP_CACHE.put(R.drawable.people, decodeSource(resources, R.drawable.people));
        MANDATORY_BITMAP_CACHE.put(R.drawable.nature, decodeSource(resources, R.drawable.nature));
        MANDATORY_BITMAP_CACHE.put(R.drawable.objects, decodeSource(resources, R.drawable.objects));
        MANDATORY_BITMAP_CACHE.put(R.drawable.places, decodeSource(resources, R.drawable.places));
        MANDATORY_BITMAP_CACHE.put(R.drawable.symbols, decodeSource(resources, R.drawable.symbols));

        SparseIntArray sparseIntArray = EmojiconHandler.getEmojisMap();

        int[] addition = {1200, 1201, 1202, 1203, 1204, 1205, 1206, 1207, 1208, 1209, 1212,
                991, 995, 996, 993, 998, 1000, 997, 999, 994, 992};

        for (int i = 0; i < sparseIntArray.size(); i++) {
            int key = sparseIntArray.keyAt(i);
            int pos = sparseIntArray.get(key);
            String keyCache = pos + "_" + EmojConstant.sEmojDpChat;

            EmojDrawable emojDrawable = new EmojDrawable(resources, pos, EmojConstant.sEmojDpChat);
            emojDrawable.setBounds(0, 0, EmojConstant.sEmojDpChat, EmojConstant.sEmojDpChat);
            emojIconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < addition.length; i++) {
            String keyCache = addition[i] + "_" + EmojConstant.sEmojDpChat;
            EmojDrawable emojDrawable = new EmojDrawable(resources, addition[i], EmojConstant.sEmojDpChat);
            emojDrawable.setBounds(0, 0, EmojConstant.sEmojDpChat, EmojConstant.sEmojDpChat);
            emojIconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < addition.length; i++) {
            String keyCache = addition[i] + "_" + EmojConstant.sEmojDpSimpleList;
            EmojDrawable emojDrawable = new EmojDrawable(resources, addition[i], EmojConstant.sEmojDpSimpleList);
            emojDrawable.setBounds(0, 0, EmojConstant.sEmojDpSimpleList, EmojConstant.sEmojDpSimpleList);
            emojIconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < sparseIntArray.size(); i++) {
            int key = sparseIntArray.keyAt(i);
            int pos = sparseIntArray.get(key);
            String keyCache = pos + "_" + EmojConstant.sEmojDpSimpleList;
            EmojDrawable emojDrawable = new EmojDrawable(resources, pos, EmojConstant.sEmojDpSimpleList);
            emojDrawable.setBounds(0, 0, EmojConstant.sEmojDpSimpleList, EmojConstant.sEmojDpSimpleList);
            emojIconCache.put(keyCache, emojDrawable);
        }
    }

    public EmojDrawable getEmojDrawableFromCache(String key, Resources resources) {
        return emojIconCache.get(key);
    }

    public Bitmap getEmogiBitmap(Resources resources, int id) {
        return MANDATORY_BITMAP_CACHE.get(id);
    }

    @SuppressWarnings("deprecation")
    private static Bitmap decodeSource(Resources resources, int id) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inDither = false;
        bmOptions.inPurgeable = true;
        bmOptions.inInputShareable = true;
        bmOptions.inTempStorage = new byte[32768];
        return BitmapFactory.decodeResource(resources, id, bmOptions);
    }

}
