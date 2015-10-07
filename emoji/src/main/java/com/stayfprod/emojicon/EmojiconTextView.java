package com.stayfprod.emojicon;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;

public class EmojiconTextView extends TextView {
    private int mEmojiconSize;
    private int mTextStart = 0;
    private int mTextLength = -1;

    public EmojiconTextView(Context context, int size) {
        super(context);
        init(null, size);
    }

    public EmojiconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs, int... size) {
        if (attrs == null) {
            if (size.length > 0) {
                mEmojiconSize = size[0];
            } else {
                mEmojiconSize = (int) getTextSize();
            }
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
            mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
            mTextStart = a.getInteger(R.styleable.Emojicon_emojiconTextStart, 0);
            mTextLength = a.getInteger(R.styleable.Emojicon_emojiconTextLength, -1);
            a.recycle();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            text = "";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        EmojiconHandler.addEmojis(getContext(), builder, mEmojiconSize, mTextStart, mTextLength, false);
        super.setText(builder, type);
    }

    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
    }
}
