package com.stayfprod.emojicon;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class EmojiView extends View {

    private static final int TRANSLATE = EmojConstant.sEmojDpLayoutSize - EmojConstant.sEmojDpSimpleList;

    private EmojDrawable mEmojDrawable;

    public EmojiView(Context context) {
        super(context);
    }

    public EmojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(TRANSLATE, TRANSLATE);
        mEmojDrawable.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(EmojConstant.sEmojDpLayoutSize, EmojConstant.sEmojDpLayoutSize);
    }

    public void setEmojDrawable(String text) {
        int pos = EmojiconHandler.getEmojiPosition(text);
        String key = pos + "_" + EmojConstant.sEmojDpSimpleList;
        mEmojDrawable = EmojiCache.getInstance().getEmojDrawableFromCache(key, getResources());
    }
}
