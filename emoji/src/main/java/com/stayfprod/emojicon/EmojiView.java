package com.stayfprod.emojicon;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class EmojiView extends View {

    private static final int TRANSLATE = EmojiConstants.EMOJI_DP_LAYOUT_SIZE - EmojiConstants.EMOJI_DP_SMILE_LIST;

    private EmojDrawable emojDrawable;

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
        emojDrawable.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(EmojiConstants.EMOJI_DP_LAYOUT_SIZE, EmojiConstants.EMOJI_DP_LAYOUT_SIZE);
    }

    public void setEmojDrawable(String text) {
        int pos = EmojiconHandler.getEmojiPosition(text);
        String key = pos + "_" + EmojiConstants.EMOJI_DP_SMILE_LIST;
        emojDrawable = EmojiCache.getInstance().getEmojDrawableFromCache(key, getResources());
    }
}
