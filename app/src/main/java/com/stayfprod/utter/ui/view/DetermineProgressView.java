package com.stayfprod.utter.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;

public class DetermineProgressView extends View {

    private DeterminateProgressDrawable progressDrawable;

    public DetermineProgressView(Context context) {
        super(context);
        init();
    }

    public DetermineProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetermineProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetermineProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return progressDrawable;
    }

    private void init() {
        progressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                DetermineProgressView.this.invalidate();
            }
        };

        progressDrawable.setMainSettings(
                null, DeterminateProgressDrawable.PlayStatus.PAUSE,
                DeterminateProgressDrawable.ColorRange.BLUE,
                LoadingContentType.AUDIO, true, false);
        progressDrawable.setBounds(0, 0);
        progressDrawable.setVisibility(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        progressDrawable.setBounds(0, 0, getWidth(), getHeight());
        progressDrawable.draw(canvas);
    }
}
