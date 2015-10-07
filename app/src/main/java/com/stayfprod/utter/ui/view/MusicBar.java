package com.stayfprod.utter.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.Logs;

public class MusicBar extends View {
    private static final Paint SHADOW_LINE_PAINT = new Paint();
    private static final Paint PROGRESS_PAINT = new Paint();
    private static final TextPaint TEXT_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private static final BitmapDrawable PAUSE_DRAWABLE;
    private static final BitmapDrawable PLAY_DRAWABLE;
    private static final BitmapDrawable CLOSE_DRAWABLE;

    private static final int PLAY_MARGIN_LEFT = Constant.DP_24;
    private static final int CLOSE_MARGIN_RIGHT = Constant.DP_23;

    private static final int NAME_MARGIN_RIGHT = Constant.DP_18;
    private static final int NAME_MARGIN_LEFT = Constant.DP_24;

    private static final float NAME_HEIGHT;

    static {
        SHADOW_LINE_PAINT.setColor(Color.WHITE);
        SHADOW_LINE_PAINT.setStyle(Paint.Style.FILL);
        SHADOW_LINE_PAINT.setShadowLayer(2.0f, 0.0f, 0f, Color.argb(100, 0, 0, 0));

        PROGRESS_PAINT.setColor(0xFF68ADE1);
        PROGRESS_PAINT.setStyle(Paint.Style.FILL_AND_STROKE);

        TEXT_PAINT.setColor(0xFF333333);
        TEXT_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        TEXT_PAINT.setTextSize(Constant.DP_16);

        PAUSE_DRAWABLE = FileUtils.decodeImageResource(R.mipmap.ic_pausepl);
        PLAY_DRAWABLE = FileUtils.decodeImageResource(R.mipmap.ic_playpl);
        CLOSE_DRAWABLE = FileUtils.decodeImageResource(R.mipmap.ic_closeplayer);

        Paint.FontMetrics metrics = TEXT_PAINT.getFontMetrics();
        NAME_HEIGHT = (metrics.descent - metrics.ascent + metrics.leading);
    }

    private StringBuilder originalName = new StringBuilder();
    private StringBuilder drawName = new StringBuilder();
    private int maxTextWidth = -1;
    private boolean isPlaying = true;
    private volatile float progress = 0;

    private View.OnClickListener onCloseClickListener;
    private View.OnClickListener onPlayClickListener;

    public void setOnPlayClickListener(OnClickListener onPlayClickListener) {
        this.onPlayClickListener = onPlayClickListener;
    }

    public void setOnCloseClickListener(OnClickListener onCloseClickListener) {
        this.onCloseClickListener = onCloseClickListener;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        //invalidateUI();
    }

    public MusicBar(Context context) {
        super(context);
        init();
    }

    public MusicBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MusicBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MusicBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (((event.getX() >= 2 && event.getX() <= PLAY_MARGIN_LEFT + PAUSE_DRAWABLE.getIntrinsicWidth() + NAME_MARGIN_LEFT))) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (onPlayClickListener != null)
                    onPlayClickListener.onClick(this);
            }
            return true;
        }

        if (((event.getX() >= getWidth() - CLOSE_MARGIN_RIGHT - getHeight()
                && event.getX() <= getWidth()))) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (onCloseClickListener != null)
                    onCloseClickListener.onClick(this);
            }
            return true;
        }
        return false;
    }

    public void init() {
        //setWillNotDraw(false);
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        originalName.append("");
        progress = 0;
    }

    public void setName(String name) {
        originalName.setLength(0);
        originalName.append(name);
        measureName();
    }

    public boolean isEmptyDrawName() {
        return drawName.length() == 0;
    }

    private void measureName() {
        if (maxTextWidth != -1) {
            drawName.setLength(0);
            drawName.append(TextUtils.ellipsize(originalName.toString(), TEXT_PAINT, maxTextWidth, TextUtils.TruncateAt.END));
        } else {
            AndroidUtil.runInUI(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
    }

    public void play() {
        isPlaying = true;
        //invalidateUI();
    }

    public void stop() {
        isPlaying = false;
        //invalidateUI();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isPlaying) {
            int startY = (getHeight() - PLAY_DRAWABLE.getIntrinsicHeight()) >> 1;
            PLAY_DRAWABLE.setBounds(PLAY_MARGIN_LEFT, startY, PLAY_MARGIN_LEFT + PLAY_DRAWABLE.getIntrinsicWidth(), startY + PLAY_DRAWABLE.getIntrinsicHeight());
            PLAY_DRAWABLE.draw(canvas);
        } else {
            int startY = (getHeight() - PAUSE_DRAWABLE.getIntrinsicHeight()) >> 1;
            PAUSE_DRAWABLE.setBounds(PLAY_MARGIN_LEFT, startY, PLAY_MARGIN_LEFT + PAUSE_DRAWABLE.getIntrinsicWidth(), startY + PAUSE_DRAWABLE.getIntrinsicHeight());
            PAUSE_DRAWABLE.draw(canvas);
        }

        int startXClose = getWidth() - CLOSE_DRAWABLE.getIntrinsicWidth() - CLOSE_MARGIN_RIGHT;
        int startYClose = (getHeight() - CLOSE_DRAWABLE.getIntrinsicHeight()) >> 1;
        CLOSE_DRAWABLE.setBounds(startXClose, startYClose, startXClose + CLOSE_DRAWABLE.getIntrinsicWidth(), startYClose + CLOSE_DRAWABLE.getIntrinsicHeight());
        CLOSE_DRAWABLE.draw(canvas);

        //canvas.drawRect(0, getHeight() - Constant.DP_2, getWidth(), getHeight(), SHADOW_LINE_PAINT);
        canvas.drawRect(0, getHeight() - Constant.DP_2, (progress * getWidth()) / 100, getHeight(), PROGRESS_PAINT);

        float startYName = ((getHeight() - NAME_HEIGHT) / 2) + Constant.DP_15;
        canvas.drawText(drawName.toString(), PLAY_MARGIN_LEFT + PAUSE_DRAWABLE.getIntrinsicWidth() + NAME_MARGIN_LEFT, startYName, TEXT_PAINT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        recalculateMaxTextWidth();
        measureName();
    }

    private void recalculateMaxTextWidth() {
        maxTextWidth = AbstractActivity.WINDOW_CURRENT_WIDTH - PLAY_MARGIN_LEFT - PAUSE_DRAWABLE.getIntrinsicWidth()
                - NAME_MARGIN_LEFT - NAME_MARGIN_RIGHT
                - CLOSE_DRAWABLE.getIntrinsicWidth() - CLOSE_MARGIN_RIGHT;
    }

    public void invalidateUI() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
