package com.stayfprod.emojicon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.concurrent.ConcurrentHashMap;

public class EmojiCache {

    private static volatile EmojiCache emojiCache;
    private static final SparseArray<Bitmap> mandatoryBitmapCache = new SparseArray<Bitmap>();

    private final ConcurrentHashMap<String, EmojDrawable> emojiconCache = new ConcurrentHashMap<String, EmojDrawable>();

    public static EmojiCache getInstance() {
        if (emojiCache == null) {
            synchronized (EmojiCache.class) {
                if (emojiCache == null) {
                    emojiCache = new EmojiCache();
                }
            }
        }
        return emojiCache;
    }


    public void initEmojiBitmaps(Resources resources) {
        mandatoryBitmapCache.put(R.drawable.people, decodeSource(resources, R.drawable.people));
        mandatoryBitmapCache.put(R.drawable.nature, decodeSource(resources, R.drawable.nature));
        mandatoryBitmapCache.put(R.drawable.objects, decodeSource(resources, R.drawable.objects));
        mandatoryBitmapCache.put(R.drawable.places, decodeSource(resources, R.drawable.places));
        mandatoryBitmapCache.put(R.drawable.symbols, decodeSource(resources, R.drawable.symbols));

        SparseIntArray sparseIntArray = EmojiconHandler.getSEmojisMap();

        int[] addition = {1200, 1201, 1202, 1203, 1204, 1205, 1206, 1207, 1208, 1209, 1212,
                991, 995, 996, 993, 998, 1000, 997, 999, 994, 992};

        for (int i = 0; i < sparseIntArray.size(); i++) {
            int key = sparseIntArray.keyAt(i);
            int pos = sparseIntArray.get(key);
            String keyCache = pos + "_" + EmojiConstants.EMOJI_DP_CHAT;

            EmojDrawable emojDrawable = new EmojDrawable(resources, pos, EmojiConstants.EMOJI_DP_CHAT);
            emojDrawable.setBounds(0, 0, EmojiConstants.EMOJI_DP_CHAT, EmojiConstants.EMOJI_DP_CHAT);
            emojiconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < addition.length; i++) {
            String keyCache = addition[i] + "_" + EmojiConstants.EMOJI_DP_CHAT;
            EmojDrawable emojDrawable = new EmojDrawable(resources, addition[i], EmojiConstants.EMOJI_DP_CHAT);
            emojDrawable.setBounds(0, 0, EmojiConstants.EMOJI_DP_CHAT, EmojiConstants.EMOJI_DP_CHAT);
            emojiconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < addition.length; i++) {
            String keyCache = addition[i] + "_" + EmojiConstants.EMOJI_DP_SMILE_LIST;
            EmojDrawable emojDrawable = new EmojDrawable(resources, addition[i], EmojiConstants.EMOJI_DP_SMILE_LIST);
            emojDrawable.setBounds(0, 0, EmojiConstants.EMOJI_DP_SMILE_LIST, EmojiConstants.EMOJI_DP_SMILE_LIST);
            emojiconCache.put(keyCache, emojDrawable);
        }

        for (int i = 0; i < sparseIntArray.size(); i++) {
            int key = sparseIntArray.keyAt(i);
            int pos = sparseIntArray.get(key);
            String keyCache = pos + "_" + EmojiConstants.EMOJI_DP_SMILE_LIST;
            EmojDrawable emojDrawable = new EmojDrawable(resources, pos, EmojiConstants.EMOJI_DP_SMILE_LIST);
            emojDrawable.setBounds(0, 0, EmojiConstants.EMOJI_DP_SMILE_LIST, EmojiConstants.EMOJI_DP_SMILE_LIST);
            emojiconCache.put(keyCache,emojDrawable );
        }
    }

    public EmojDrawable getEmojDrawableFromCache(String key, Resources resources) {
        return emojiconCache.get(key);
    }

    public Bitmap getEmogiBitmap(Resources resources, int id) {
        return mandatoryBitmapCache.get(id);
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
