package com.stayfprod.utter.ui.adapter.holder;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.ui.view.CircleProgressView;

public class LoadingHolder extends AbstractHolder {

    private static final int LAYOUT_HEIGHT = Constant.DP_45;

    public LoadingHolder(Context context) {
        super(new LinearLayout(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, LAYOUT_HEIGHT));

        CircleProgressView circleProgressView = new CircleProgressView(context, CircleProgressView.FOR_LIST, true);
        ((LinearLayout) itemView).addView(circleProgressView);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) circleProgressView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
    }

    @Override
    public void setValues(Object record, int i, Context context) {

    }
}
