package com.stayfprod.utter.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.stayfprod.utter.ui.drawable.IndeterminateProgressDrawable;
import com.stayfprod.utter.util.AndroidUtil;

public class CircleProgressView extends View {

    private boolean mAutoStart;
    public static final int MODE_DETERMINATE = 0;
    public static final int MODE_INDETERMINATE = 1;

    public static final int FOR_LIST = 0;
    public static final int FOR_TOOLBAR = 1;

    private IndeterminateProgressDrawable mProgressDrawable;

    public CircleProgressView(Context context, int what, boolean autoStart) {
        super(context);
        this.mAutoStart = autoStart;
        init(what);
    }

    public CircleProgressView(Context context) {
        super(context);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void init(int what) {
        this.mProgressDrawable = (new IndeterminateProgressDrawable.Builder(what)).build();
        this.mProgressDrawable.setProgressMode(MODE_INDETERMINATE);

        this.setProgress(0.0F);
        this.setSecondaryProgress(0.0F);

        AndroidUtil.setBackground(this, this.mProgressDrawable);
    }

    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            if (this.mAutoStart) {
                if (visibility != GONE && visibility != INVISIBLE) {
                    this.start();
                } else {
                    this.stop();
                }
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.getVisibility() == VISIBLE && this.mAutoStart) {
            this.start();
        }
    }

    protected void onDetachedFromWindow() {
        if (this.mAutoStart) {
            this.stop();
        }

        super.onDetachedFromWindow();
    }

    public void setProgress(float percent) {
        (this.mProgressDrawable).setProgress(percent);
    }

    public void setSecondaryProgress(float percent) {
        (this.mProgressDrawable).setSecondaryProgress(percent);
    }

    public void start() {
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.start();
        }
    }

    public void stop() {
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.stop();
        }
    }

    public void setAutoStart(boolean autoStart) {
        this.mAutoStart = autoStart;
    }
}
