package com.stayfprod.utter.ui.drawable;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;

public class DeterminateProgressDrawable extends Drawable implements Drawable.Callback {

    public enum LoadStatus {
        NO_LOAD,
        PAUSE,
        PROCEED_LOAD,
        LOADED
    }

    public enum PlayStatus {
        PLAY,
        PAUSE
    }

    public enum ColorRange {
        BLUE,
        BLACK
    }

    public static final int PROGRESS_BAR_IMAGE_SIZE = Constant.DP_82;
    public static final int CIRCLE_SIZE = Constant.DP_45;
    private static final int STROKE_WIDTH = Constant.DP_2;
    private static final float BACK_IMAGE_PADDING = Constant.DP_19;
    private static final int BACK_IMAGE_WIDTH = (int) BACK_IMAGE_PADDING << 1;
    private static final float PAD_STROKE = STROKE_WIDTH >> 1;
    private static final float DIF = CIRCLE_SIZE - PAD_STROKE;
    private static final float DIF_WITH_BACKGROUND = DIF + BACK_IMAGE_PADDING;
    private static final float BACK_IMAGE_PADDING_AND_PAD_STROKE = BACK_IMAGE_PADDING + PAD_STROKE;

    private static final BitmapDrawable
            IC_DOWNLOAD = FileUtil.decodeImageResource(R.mipmap.ic_download),
            IC_PAUSE = FileUtil.decodeImageResource(R.mipmap.ic_pause),
            IC_DOWNLOAD_BLUE = FileUtil.decodeImageResource(R.mipmap.ic_download_blue),
            IC_FILE_PAUSE_BLUE = FileUtil.decodeImageResource(R.mipmap.ic_file_pause_blue),
            IC_FILE = FileUtil.decodeImageResource(R.mipmap.ic_file),
            IC_PLAY = FileUtil.decodeImageResource(R.mipmap.ic_play);


    public static final int ANIM_DURATION = 300;

    private static final int START_ANGLE = -90;
    private static final Paint CIRCLE_BACK_PAINT_BLUE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint CIRCLE_BACK_PAINT_LIGHT_BLUE;
    private static final Paint CIRCLE_BACK_PAINT_BLACK;

    private static final Paint ARC_PAINT_WHITE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint ARC_PAINT_BLUE;

    private static final Paint BACKGROUND_IMAGE_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    static {
        CIRCLE_BACK_PAINT_BLUE.setColor(0xFF68ade1);
        CIRCLE_BACK_PAINT_BLUE.setStyle(Paint.Style.FILL_AND_STROKE);
        CIRCLE_BACK_PAINT_BLUE.setStrokeWidth(STROKE_WIDTH);

        CIRCLE_BACK_PAINT_LIGHT_BLUE = new Paint(CIRCLE_BACK_PAINT_BLUE);
        CIRCLE_BACK_PAINT_LIGHT_BLUE.setColor(0xFFF0F6FA);

        CIRCLE_BACK_PAINT_BLACK = new Paint(CIRCLE_BACK_PAINT_BLUE);
        CIRCLE_BACK_PAINT_BLACK.setColor(0x4D000000);

        ARC_PAINT_WHITE.setColor(Color.WHITE);
        ARC_PAINT_WHITE.setStyle(Paint.Style.STROKE);
        ARC_PAINT_WHITE.setStrokeWidth(STROKE_WIDTH);

        ARC_PAINT_BLUE = new Paint(ARC_PAINT_WHITE);
        ARC_PAINT_BLUE.setColor(0xFF68ADE1);
    }

    private RectF mOvalRectF = new RectF();
    private Rect mBackgroundImageRect = new Rect();
    private boolean mIsDrawCenter = true;
    private int mMin = 0;
    private int mMax = 100;

    private volatile float mProgress = 0;
    private volatile LoadStatus mLoadStatus;
    private volatile PlayStatus mPlayStatus;
    private volatile ColorRange mColorRange;

    private volatile LoadingContentType mContentType;
    private volatile boolean mIsAvailablePause = true;

    private volatile boolean mIsHaveBackground;
    private volatile BitmapDrawable mBackgroundImage;
    private volatile Bitmap mCenterImage;
    private volatile int mCx, mCy;
    private volatile boolean mIsVisible = true;
    private volatile boolean mIsRunAnimation = false;

    public DeterminateProgressDrawable() {
        this.setCallback(this);
    }

    public void setMainSettings(LoadStatus loadStatus, ColorRange colorRange, LoadingContentType contentType, boolean... isAvailablePause) {
        this.mProgress = 0;
        this.mLoadStatus = loadStatus;
        this.mPlayStatus = null;
        this.mColorRange = colorRange;
        this.mContentType = contentType;
        this.mIsAvailablePause = (isAvailablePause.length <= 0) || isAvailablePause[0];
        chooseCenterImage();
    }

    public void setMainSettings(PlayStatus playStatus, ColorRange colorRange, LoadingContentType contentType, boolean... isAvailablePause) {
        this.mProgress = 0;
        this.mLoadStatus = null;
        this.mPlayStatus = playStatus;
        this.mColorRange = colorRange;
        this.mContentType = contentType;
        this.mIsAvailablePause = (isAvailablePause.length <= 0) || isAvailablePause[0];
        chooseCenterImage();
    }

    public void setMainSettings(LoadStatus loadStatus, PlayStatus playStatus, ColorRange colorRange, LoadingContentType contentType, boolean isAvailablePause, boolean isHaveBackground) {
        if (loadStatus != null) {
            setMainSettings(loadStatus, colorRange, contentType, isAvailablePause, isHaveBackground);
        } else {
            setMainSettings(playStatus, colorRange, contentType, isAvailablePause, isHaveBackground);
        }
    }

    public void setMainSettings(LoadStatus loadStatus, ColorRange colorRange, LoadingContentType contentType, boolean isAvailablePause, boolean isHaveBackground) {
        this.mIsHaveBackground = isHaveBackground;
        setMainSettings(loadStatus, colorRange, contentType, isAvailablePause);
    }


    public void setMainSettings(PlayStatus playStatus, ColorRange colorRange, LoadingContentType contentType, boolean isAvailablePause, boolean isHaveBackground) {
        this.mIsHaveBackground = isHaveBackground;
        setMainSettings(playStatus, colorRange, contentType, isAvailablePause);
    }

    public void clean() {
        mCenterImage = null;
        mBackgroundImage = null;
        mContentType = null;
        mColorRange = null;
        mPlayStatus = null;
        mLoadStatus = null;
    }

    public boolean isHaveBackground() {
        return mIsHaveBackground;
    }

    public LoadingContentType getContentType() {
        return mContentType;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setBounds(int x, int y) {
        if (mIsHaveBackground)
            this.setBounds(x, y, x + CIRCLE_SIZE + BACK_IMAGE_WIDTH, y + CIRCLE_SIZE + BACK_IMAGE_WIDTH);
        else
            this.setBounds(x, y, x + CIRCLE_SIZE, y + CIRCLE_SIZE);
    }

    public void setVisibility(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    private void chooseCenterImage() {
        if (mLoadStatus != null) {
            if (mColorRange == ColorRange.BLACK) {
                switch (mLoadStatus) {
                    case NO_LOAD:
                    case PAUSE:
                        mIsDrawCenter = true;
                        mCenterImage = IC_DOWNLOAD.getBitmap();
                        break;
                    case PROCEED_LOAD:
                        mIsDrawCenter = mIsAvailablePause;
                        mCenterImage = IC_PAUSE.getBitmap();
                        break;
                    case LOADED:
                        mIsDrawCenter = false;
                        mCenterImage = null;
                        break;
                }
            } else {
                mIsDrawCenter = true;
                switch (mLoadStatus) {
                    case NO_LOAD:
                    case PAUSE:
                        mCenterImage = IC_DOWNLOAD_BLUE.getBitmap();
                        break;
                    case PROCEED_LOAD:
                        mCenterImage = IC_FILE_PAUSE_BLUE.getBitmap();
                        break;
                    case LOADED:
                        mCenterImage = IC_FILE.getBitmap();
                        break;
                }
            }
        }

        if (mPlayStatus != null) {
            mIsDrawCenter = true;
            switch (mPlayStatus) {
                case PLAY:
                    mCenterImage = IC_PLAY.getBitmap();
                    break;
                case PAUSE:
                    mCenterImage = IC_PAUSE.getBitmap();
                    break;
            }
        }
        initCenterImageCoordinates();
    }

    private void initCenterImageCoordinates() {
        int lw = CIRCLE_SIZE;
        int lh = CIRCLE_SIZE;

        if (mIsHaveBackground) {
            lw = CIRCLE_SIZE + BACK_IMAGE_WIDTH;
            lh = CIRCLE_SIZE + BACK_IMAGE_WIDTH;
        }

        if (mCenterImage != null) {
            if (mPlayStatus == PlayStatus.PLAY) {
                int imgWidth = mCenterImage.getWidth();
                mCx = (int) (((lw - imgWidth + STROKE_WIDTH) >> 1) + Math.ceil((imgWidth * 0.577350269 - (imgWidth >> 1))));
            } else {
                mCx = (lw - mCenterImage.getWidth() + STROKE_WIDTH) >> 1;
            }
            mCy = (lh - mCenterImage.getHeight() + STROKE_WIDTH) >> 1;
        }
    }

    public void setBackgroundImage(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                this.mBackgroundImage = bitmapDrawable;
            }
        }
    }

    public void setBackgroundImageAsync(BitmapDrawable bitmapDrawable) {
        setBackgroundImage(bitmapDrawable);
        invalidateAsync();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mIsVisible) {
            Rect bounds = getBounds();
            canvas.translate(bounds.left, bounds.top);
            canvas.save();
            if (mBackgroundImage != null) {
                canvas.drawBitmap(mBackgroundImage.getBitmap(), null, mBackgroundImageRect, BACKGROUND_IMAGE_PAINT);
            }

            if (mCenterImage != null) {
                canvas.restore();
                Paint ovalPaint;
                if (mPlayStatus == null) {
                    ovalPaint = mColorRange == ColorRange.BLACK ? CIRCLE_BACK_PAINT_BLACK : CIRCLE_BACK_PAINT_LIGHT_BLUE;
                    canvas.drawOval(mOvalRectF, ovalPaint);
                    Paint arcPaint = mColorRange == ColorRange.BLACK ? ARC_PAINT_WHITE : ARC_PAINT_BLUE;
                    canvas.drawArc(mOvalRectF, START_ANGLE, 360 * mProgress / mMax, false, arcPaint);
                } else {
                    ovalPaint = mColorRange == ColorRange.BLACK ? CIRCLE_BACK_PAINT_BLACK : CIRCLE_BACK_PAINT_BLUE;
                    canvas.drawOval(mOvalRectF, ovalPaint);
                }

                if (mIsDrawCenter) {
                    canvas.drawBitmap(mCenterImage, mCx, mCy, null);
                }
            }
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mIsHaveBackground) {
            mOvalRectF.set(BACK_IMAGE_PADDING_AND_PAD_STROKE, BACK_IMAGE_PADDING_AND_PAD_STROKE, DIF_WITH_BACKGROUND, DIF_WITH_BACKGROUND);
        } else
            mOvalRectF.set(PAD_STROKE, PAD_STROKE, DIF, DIF);
        //todo не по центру обезается
        mBackgroundImageRect.set(0, 0, bounds.width(), bounds.height());
    }

    public void changeLoadStatusAsyncAndUpdate(LoadStatus status) {
        changeLoadStatus(status);
        invalidateAsync();
    }

    private void invalidateAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void changeLoadStatus(LoadStatus status) {
        this.mLoadStatus = status;
        this.mPlayStatus = null;
        chooseCenterImage();
        if (status == LoadStatus.LOADED) {
            mProgress = 0;
        }
    }

    public PlayStatus getPlayStatus() {
        return mPlayStatus;
    }

    public LoadStatus getLoadStatus() {
        return mLoadStatus;
    }

    public void changeLoadStatusAndUpdate(LoadStatus status) {
        changeLoadStatus(status);
        invalidate();
    }

    public void changePlayStatusAsyncAndUpdate(PlayStatus status) {
        changePlayStatus(status);
        invalidateAsync();
    }

    public void changePlayStatus(PlayStatus status) {
        this.mPlayStatus = status;
        this.mLoadStatus = null;
        chooseCenterImage();
    }

    public void invalidate() {

    }

    public void changePlayStatusAndUpdate(PlayStatus status) {
        changePlayStatus(status);
        invalidate();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void invalidateDrawable(Drawable who) {

    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {

    }

    public void setProgressWithForceAnimationAsync(final float progress, final boolean... isFinish) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                setProgressWithForceAnimation(progress, isFinish);
            }
        });
    }

    public void setProgressWithForceAnimation(final float progress, final boolean... isFinish) {
        mIsVisible = true;
        if (mLoadStatus == LoadStatus.NO_LOAD) {
            changeLoadStatus(LoadStatus.PROCEED_LOAD);
        }
        setProgressWithAnimation(progress, isFinish);
    }

    public void setProgressWithAnimationAsync(final float progress, final boolean... isFinish) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                setProgressWithAnimation(progress, isFinish);
            }
        });
    }

    //info в процентах
    public void setProgressWithAnimation(final float progress, boolean... isFinish) {
        mIsVisible = true;
        boolean finish = isFinish.length > 0 && isFinish[0];
        if (mLoadStatus == DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD || mLoadStatus == DeterminateProgressDrawable.LoadStatus.PAUSE) {
            if ((int) progress < mMax || finish) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
                objectAnimator.setDuration(DeterminateProgressDrawable.ANIM_DURATION);
                objectAnimator.setInterpolator(new DecelerateInterpolator());

                if ((int) progress >= mMax && finish) {
                    objectAnimator.addListener(new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mContentType == LoadingContentType.VIDEO || mContentType == LoadingContentType.AUDIO) {
                                changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                            } else
                                changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.LOADED);
                        }
                    });
                }
                objectAnimator.start();
            }
        }
    }
}
