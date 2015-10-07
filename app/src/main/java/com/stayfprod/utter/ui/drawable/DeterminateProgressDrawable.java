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
import com.stayfprod.utter.util.FileUtils;
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
            IC_DOWNLOAD = FileUtils.decodeImageResource(R.mipmap.ic_download),
            IC_PAUSE = FileUtils.decodeImageResource(R.mipmap.ic_pause),
            IC_DOWNLOAD_BLUE = FileUtils.decodeImageResource(R.mipmap.ic_download_blue),
            IC_FILE_PAUSE_BLUE = FileUtils.decodeImageResource(R.mipmap.ic_file_pause_blue),
            IC_FILE = FileUtils.decodeImageResource(R.mipmap.ic_file),
            IC_PLAY = FileUtils.decodeImageResource(R.mipmap.ic_play);


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

    private RectF ovalRectF = new RectF();
    private Rect backgroundImageRect = new Rect();
    private boolean isDrawCenter = true;
    private int min = 0;
    private int max = 100;

    private volatile float progress = 0;
    private volatile LoadStatus loadStatus;
    private volatile PlayStatus playStatus;
    private volatile ColorRange colorRange;

    private volatile LoadingContentType contentType;
    private volatile boolean isAvailablePause = true;

    private volatile boolean isHaveBackground;
    private volatile BitmapDrawable backgroundImage;
    private volatile Bitmap centerImage;
    private volatile int cx, cy;
    private volatile boolean isVisible = true;
    private volatile boolean isRunAnimation = false;


    public DeterminateProgressDrawable() {
        this.setCallback(this);
    }

    public void setMainSettings(LoadStatus loadStatus, ColorRange colorRange, LoadingContentType contentType, boolean... isAvailablePause) {
        this.progress = 0;
        this.loadStatus = loadStatus;
        this.playStatus = null;
        this.colorRange = colorRange;
        this.contentType = contentType;
        this.isAvailablePause = (isAvailablePause.length <= 0) || isAvailablePause[0];
        chooseCenterImage();
    }

    public void setMainSettings(PlayStatus playStatus, ColorRange colorRange, LoadingContentType contentType, boolean... isAvailablePause) {
        this.progress = 0;
        this.loadStatus = null;
        this.playStatus = playStatus;
        this.colorRange = colorRange;
        this.contentType = contentType;
        this.isAvailablePause = (isAvailablePause.length <= 0) || isAvailablePause[0];
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
        this.isHaveBackground = isHaveBackground;
        setMainSettings(loadStatus, colorRange, contentType, isAvailablePause);
    }


    public void setMainSettings(PlayStatus playStatus, ColorRange colorRange, LoadingContentType contentType, boolean isAvailablePause, boolean isHaveBackground) {
        this.isHaveBackground = isHaveBackground;
        setMainSettings(playStatus, colorRange, contentType, isAvailablePause);
    }

    public void clean() {
        centerImage = null;
        backgroundImage = null;
        contentType = null;
        colorRange = null;
        playStatus = null;
        loadStatus = null;
    }

    public boolean isHaveBackground() {
        return isHaveBackground;
    }

    public LoadingContentType getContentType() {
        return contentType;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setBounds(int x, int y) {
        if (isHaveBackground)
            this.setBounds(x, y, x + CIRCLE_SIZE + BACK_IMAGE_WIDTH, y + CIRCLE_SIZE + BACK_IMAGE_WIDTH);
        else
            this.setBounds(x, y, x + CIRCLE_SIZE, y + CIRCLE_SIZE);
    }

    public void setVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }

    private void chooseCenterImage() {
        if (loadStatus != null) {
            if (colorRange == ColorRange.BLACK) {
                switch (loadStatus) {
                    case NO_LOAD:
                    case PAUSE:
                        isDrawCenter = true;
                        centerImage = IC_DOWNLOAD.getBitmap();
                        break;
                    case PROCEED_LOAD:
                        isDrawCenter = isAvailablePause;
                        centerImage = IC_PAUSE.getBitmap();
                        break;
                    case LOADED:
                        isDrawCenter = false;
                        centerImage = null;
                        break;
                }
            } else {
                isDrawCenter = true;
                switch (loadStatus) {
                    case NO_LOAD:
                    case PAUSE:
                        centerImage = IC_DOWNLOAD_BLUE.getBitmap();
                        break;
                    case PROCEED_LOAD:
                        centerImage = IC_FILE_PAUSE_BLUE.getBitmap();
                        break;
                    case LOADED:
                        centerImage = IC_FILE.getBitmap();
                        break;
                }
            }
        }

        if (playStatus != null) {
            isDrawCenter = true;
            switch (playStatus) {
                case PLAY:
                    centerImage = IC_PLAY.getBitmap();
                    break;
                case PAUSE:
                    centerImage = IC_PAUSE.getBitmap();
                    break;
            }
        }
        initCenterImageCoordinates();
    }

    private void initCenterImageCoordinates() {
        int lw = CIRCLE_SIZE;
        int lh = CIRCLE_SIZE;

        if (isHaveBackground) {
            lw = CIRCLE_SIZE + BACK_IMAGE_WIDTH;
            lh = CIRCLE_SIZE + BACK_IMAGE_WIDTH;
        }

        if (centerImage != null) {
            if (playStatus == PlayStatus.PLAY) {
                int imgWidth = centerImage.getWidth();
                cx = (int) (((lw - imgWidth + STROKE_WIDTH) >> 1) + Math.ceil((imgWidth * 0.577350269 - (imgWidth >> 1))));
            } else {
                cx = (lw - centerImage.getWidth() + STROKE_WIDTH) >> 1;
            }
            cy = (lh - centerImage.getHeight() + STROKE_WIDTH) >> 1;
        }
    }

    public void setBackgroundImage(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                this.backgroundImage = bitmapDrawable;
            }
        }
    }

    public void setBackgroundImageAsync(BitmapDrawable bitmapDrawable) {
        setBackgroundImage(bitmapDrawable);
        invalidateAsync();
    }

    @Override
    public void draw(Canvas canvas) {
        if (isVisible) {
            Rect bounds = getBounds();
            canvas.translate(bounds.left, bounds.top);
            canvas.save();
            if (backgroundImage != null) {
                //тут наверное лучше bitmapDrawable и задавть границы
                //AndroidUtil.setCropBounds(backgroundImage, Constant.DP_64);
                //backgroundImage.draw(canvas);
                canvas.drawBitmap(backgroundImage.getBitmap(), null, backgroundImageRect, BACKGROUND_IMAGE_PAINT);
            }

            if (centerImage != null) {
                canvas.restore();
                Paint ovalPaint;
                if (playStatus == null) {
                    ovalPaint = colorRange == ColorRange.BLACK ? CIRCLE_BACK_PAINT_BLACK : CIRCLE_BACK_PAINT_LIGHT_BLUE;
                    canvas.drawOval(ovalRectF, ovalPaint);
                    Paint arcPaint = colorRange == ColorRange.BLACK ? ARC_PAINT_WHITE : ARC_PAINT_BLUE;
                    canvas.drawArc(ovalRectF, START_ANGLE, 360 * progress / max, false, arcPaint);
                } else {
                    ovalPaint = colorRange == ColorRange.BLACK ? CIRCLE_BACK_PAINT_BLACK : CIRCLE_BACK_PAINT_BLUE;
                    canvas.drawOval(ovalRectF, ovalPaint);
                }

                if (isDrawCenter) {
                    canvas.drawBitmap(centerImage, cx, cy, null);
                }
            }
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (isHaveBackground) {
            ovalRectF.set(BACK_IMAGE_PADDING_AND_PAD_STROKE, BACK_IMAGE_PADDING_AND_PAD_STROKE, DIF_WITH_BACKGROUND, DIF_WITH_BACKGROUND);
        } else
            ovalRectF.set(PAD_STROKE, PAD_STROKE, DIF, DIF);
        //todo не по центру обезается
        backgroundImageRect.set(0, 0, bounds.width(), bounds.height());
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
        this.loadStatus = status;
        this.playStatus = null;
        chooseCenterImage();
        if (status == LoadStatus.LOADED) {
            progress = 0;
        }
    }

    public PlayStatus getPlayStatus() {
        return playStatus;
    }

    public LoadStatus getLoadStatus() {
        return loadStatus;
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
        this.playStatus = status;
        this.loadStatus = null;
        chooseCenterImage();
    }

    public void invalidate() {
        //invalidateSelf();
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
        isVisible = true;
        if (loadStatus == LoadStatus.NO_LOAD) {
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
        isVisible = true;
        boolean finish = isFinish.length > 0 && isFinish[0];
        if (loadStatus == DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD || loadStatus == DeterminateProgressDrawable.LoadStatus.PAUSE) {
            if ((int) progress < max || finish) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
                objectAnimator.setDuration(DeterminateProgressDrawable.ANIM_DURATION);
                objectAnimator.setInterpolator(new DecelerateInterpolator());

                if ((int) progress >= max && finish) {
                    objectAnimator.addListener(new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (contentType == LoadingContentType.VIDEO || contentType == LoadingContentType.AUDIO) {
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
