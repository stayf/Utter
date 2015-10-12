package com.stayfprod.emojicon;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

public class EmojiconSpan extends DynamicDrawableSpan {
    private final int size;
    private final EmojDrawable mDrawable;

    public EmojiconSpan(Resources resources, int pos, int size) {
        super();
        this.size = size;
        String key = pos + "_" + size;
        mDrawable = EmojiCache.getInstance().getEmojDrawableFromCache(key, resources);
    }

    public Drawable getDrawable() {
        return mDrawable;
    }
}