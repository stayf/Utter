package com.stayfprod.utter.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.stayfprod.utter.Constant;

public class UnselectedCircleView extends View {

    private static final Paint mCirclePaint;

    static {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(0xFFEDEDED);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(Constant.DP_2);
    }

    private boolean isSmall;

    public UnselectedCircleView(Context context, boolean isSmall) {
        super(context);
        this.isSmall = isSmall;
    }

    public UnselectedCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnselectedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UnselectedCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, (float) (getHeight() / (isSmall ? 2.4 : 2.2)), mCirclePaint);
    }
}
