package com.stayfprod.utter.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;

import com.stayfprod.utter.R;
import com.stayfprod.utter.util.FileUtil;

public class SelectedCircleView extends AbstractScaleAnimatedView {

    private static final BitmapDrawable SELECTED_DRAWABLE;

    static {
        SELECTED_DRAWABLE = FileUtil.decodeImageResource(R.mipmap.ic_attach_check);
    }

    public SelectedCircleView(Context context) {
        super(context);
    }

    public SelectedCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SelectedCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        SELECTED_DRAWABLE.setBounds(0, 0, getWidth(), getHeight());
        SELECTED_DRAWABLE.draw(canvas);
    }
}
