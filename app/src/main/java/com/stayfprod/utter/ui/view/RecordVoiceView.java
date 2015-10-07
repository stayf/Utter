package com.stayfprod.utter.ui.view;


import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

public class RecordVoiceView extends View {

    private Paint mPaintButton = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintVoice = new Paint(Paint.ANTI_ALIAS_FLAG);

    private BitmapDrawable mMicBlack;
    private BitmapDrawable mMicWhite;
    private Rect mClickBounds = new Rect();
    private boolean isPressed;
    public boolean canTouch;
    private View.OnTouchListener afterTouchListener;

    public static final int MIN_PRESSED_BUTTON_RADIUS = AndroidUtil.dp(24);
    public static final int MAX_PRESSED_BUTTON_RADIUS = AndroidUtil.dp(40);
    public static final int MAX_VOICE_RADIUS = AndroidUtil.dp(58);

    private volatile int currRadius;
    private volatile int currVoiceRadius;
    private volatile int dx;

    public int getCurrVoiceRadius() {
        return currVoiceRadius;
    }

    public void setCurrVoiceRadius(int voiceAmplitude) {
        //от 0 до 255
        this.currVoiceRadius = currRadius + (voiceAmplitude * (MAX_VOICE_RADIUS - MAX_PRESSED_BUTTON_RADIUS)) / Short.MAX_VALUE;
        invalidate();
    }

    public int getCurrRadius() {
        return currRadius;
    }

    public void setCurrRadius(int currRadius) {
        this.currRadius = currRadius;
        invalidate();
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
        invalidate();
    }

    public void onMove(int dx) {
        this.dx += dx;
        invalidate();
    }

    public void onCancel() {
        isPressed = false;
        invalidate();
    }

    public void onPressed() {
        currRadius = MIN_PRESSED_BUTTON_RADIUS;
        currVoiceRadius = 0;
        isPressed = true;
        canTouch = true;
        invalidate();
    }

    public void onStartRecording() {
        ObjectAnimator animButtonRadius = ObjectAnimator.ofInt(this, "currRadius", currRadius, MAX_PRESSED_BUTTON_RADIUS);
        animButtonRadius.setDuration(200);
        animButtonRadius.start();
    }

    public void setAfterTouchListener(OnTouchListener afterTouchListener) {
        this.afterTouchListener = afterTouchListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (((event.getX() >= mClickBounds.left -  Constant.DP_12
                && event.getX() <= mClickBounds.right +  Constant.DP_12
                && event.getY() >= mClickBounds.top -  Constant.DP_12
                && event.getY() <= mClickBounds.bottom +  Constant.DP_12) && event.getAction() == MotionEvent.ACTION_DOWN) || canTouch) {

            afterTouchListener.onTouch(this, event);
            return true;
        }
        return false;
    }


    public RecordVoiceView(Context context) {
        super(context);
        init();
    }

    public RecordVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecordVoiceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mMicBlack = FileUtils.decodeImageResource(R.mipmap.ic_mic);
        mMicWhite = FileUtils.decodeImageResource(R.mipmap.ic_mic_white);
        mPaintButton.setColor(0xFFFF3B36);
        mPaintButton.setStyle(Paint.Style.FILL);
        mPaintButton.setShadowLayer(4.0f, 0.0f, 1.5f, Color.argb(100, 0, 0, 0));

        mPaintVoice.setColor(0x0D000000);
        mPaintVoice.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int endX = getWidth() - AndroidUtil.dp(18) - dx;
        int startX = endX - mMicWhite.getIntrinsicWidth();
        int startY = getHeight() - AndroidUtil.dp(36);
        int endY = startY + mMicWhite.getIntrinsicHeight();

        if (dx == 0) {
            mClickBounds.set(startX, startY, endX, endY);
        }

        if (isPressed) {
            int halfImgWidth = (mMicWhite.getIntrinsicWidth() >> 1);
            int halfImgHeight = (mMicWhite.getIntrinsicHeight() >> 1);
            float dx = startX + halfImgWidth - currRadius;
            float dy = startY + halfImgHeight - currRadius;

            canvas.translate(dx, dy);
            int cx = startX + halfImgWidth;
            int cy = startY + halfImgHeight;

            canvas.restore();
            canvas.drawCircle(cx, cy, currVoiceRadius, mPaintVoice);
            canvas.drawCircle(cx, cy, (float) (currRadius), mPaintButton);

            mMicWhite.setBounds(startX, startY, endX, endY);
            mMicWhite.draw(canvas);
        } else {
            mMicBlack.setBounds(startX, startY, endX, endY);
            mMicBlack.draw(canvas);
        }
    }
}
