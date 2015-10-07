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
        // ошибка в чате ксюши 17_72 откуда 72??? это 36 dp
        //todo как-то тут оказался NullPointerException после подгрузки стикеров в эмоджах(и потом прокрутки списка)
        //mDrawable.setBounds(0, 0, size, size);
        return mDrawable;
    }
}