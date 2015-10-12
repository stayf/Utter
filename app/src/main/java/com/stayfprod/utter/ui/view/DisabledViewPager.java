package com.stayfprod.utter.ui.view;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DisabledViewPager extends ViewPager {

    private boolean mEnabled;

    public DisabledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mEnabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.mEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }
}