package com.stayfprod.utter.ui.view;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.stayfprod.utter.R;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.util.Logs;

public abstract class AbstractScaleAnimatedView extends View {

    protected boolean mHidden = false;

    public AbstractScaleAnimatedView(Context context) {
        super(context);
        init();
    }

    public AbstractScaleAnimatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbstractScaleAnimatedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AbstractScaleAnimatedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(GONE);
    }

    public void hideButton() {
        mHidden = true;
        setVisibility(GONE);
    }

    public void showButton() {
        mHidden = false;
        setVisibility(VISIBLE);
    }

    public void switchAnimated(View... image) {
        if (!mHidden && getVisibility() == VISIBLE) {
            hideButtonAnimated(image);
            if (image.length == 0)
                ((ViewGroup) this.getParent().getParent()).setBackgroundResource(R.drawable.item_click_transparent);
        } else {
            mHidden = true;
            showButtonAnimated(image);
            if (image.length == 0)
                ((ViewGroup) this.getParent().getParent()).setBackgroundColor(0xFFF5F5F5);
        }
    }

    public void hideButtonAnimated(View... image) {
        if (!mHidden) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
            AnimatorSet animSetXY = new AnimatorSet();

            if (image.length > 0) {
                ObjectAnimator scaleImageX = ObjectAnimator.ofFloat(image[0], "scaleX", 0.75f, 1f);
                ObjectAnimator scaleImageY = ObjectAnimator.ofFloat(image[0], "scaleY", 0.75f, 1f);
                animSetXY.playTogether(scaleX, scaleY, scaleImageX, scaleImageY);
            } else {
                animSetXY.playTogether(scaleX, scaleY);
            }

            animSetXY.setInterpolator(new AccelerateInterpolator());
            animSetXY.setDuration(100);
            animSetXY.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                }
            });
            animSetXY.start();
            mHidden = true;
        }
    }

    public void showButtonAnimated(View... image) {
        if (mHidden) {
            setVisibility(VISIBLE);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1f);
            AnimatorSet animSetXY = new AnimatorSet();

            if (image.length > 0) {
                ObjectAnimator scaleImageX = ObjectAnimator.ofFloat(image[0], "scaleX", 1f, 0.75f);
                ObjectAnimator scaleImageY = ObjectAnimator.ofFloat(image[0], "scaleY", 1f, 0.75f);
                animSetXY.playTogether(scaleX, scaleY, scaleImageX, scaleImageY);
            } else {
                animSetXY.playTogether(scaleX, scaleY);
            }

            animSetXY.setInterpolator(new OvershootInterpolator());
            animSetXY.setDuration(200);
            animSetXY.start();
            mHidden = false;
        }
    }

    public boolean isHidden() {
        return mHidden;
    }
}
