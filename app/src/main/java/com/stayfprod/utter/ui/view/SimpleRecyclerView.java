package com.stayfprod.utter.ui.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class SimpleRecyclerView extends RecyclerView {

    public SimpleRecyclerView(Context context) {
        super(context);
    }

    public SimpleRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityY *= 0.8;
        return super.fling(velocityX, velocityY);
    }
}
