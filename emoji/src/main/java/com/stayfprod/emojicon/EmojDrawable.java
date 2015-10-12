package com.stayfprod.emojicon;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class EmojDrawable extends Drawable {
    private static final Paint PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    private static final float ICON_DIVIDER = 50f;
    private static final int COUNT_IN_ROW = 8;

    private RectF mRect = new RectF();
    private Rect mRectS = new Rect();
    private Bitmap mBitmap;

    public EmojDrawable(Resources resources, int pos, int size) {
        EmojiCache emojiCache = EmojiCache.getInstance();
        switch (pos / 300) {
            case 0:
                pos = pos - 1;
                mBitmap = emojiCache.getEmogiBitmap(resources, R.drawable.people);
                break;
            case 1:
                pos = pos - 300;
                mBitmap = emojiCache.getEmogiBitmap(resources, R.drawable.nature);
                break;
            case 2:
                pos = pos - 600;
                mBitmap = emojiCache.getEmogiBitmap(resources, R.drawable.objects);
                break;
            case 3:
                pos = pos - 900;
                mBitmap = emojiCache.getEmogiBitmap(resources, R.drawable.places);
                break;
            default:
                pos = pos - 1200;
                mBitmap = emojiCache.getEmogiBitmap(resources, R.drawable.symbols);
        }

        int column = pos / COUNT_IN_ROW;
        int row = pos % COUNT_IN_ROW;

        mRectS.set((int) (row * (ICON_DIVIDER)), (int) (column * (ICON_DIVIDER)),
                (int) (row * (ICON_DIVIDER)) + (int) ICON_DIVIDER, (int) (column * (ICON_DIVIDER)) + (int) ICON_DIVIDER);
        mRect.set(0, 0, size, size);
    }

    @Override
    public void draw(final Canvas canvas) {
        canvas.drawBitmap(mBitmap, mRectS, mRect, PAINT);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
