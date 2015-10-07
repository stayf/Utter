package com.stayfprod.utter.ui.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.ColorUtil;
import com.stayfprod.utter.util.AndroidUtil;

public class IndeterminateProgressDrawable extends Drawable implements Animatable {

    private long mLastUpdateTime;
    private long mLastProgressStateTime;
    private long mLastRunStateTime;
    private int mProgressState;
    private static final int PROGRESS_STATE_HIDE = -1;
    private static final int PROGRESS_STATE_STRETCH = 0;
    private static final int PROGRESS_STATE_KEEP_STRETCH = 1;
    private static final int PROGRESS_STATE_SHRINK = 2;
    private static final int PROGRESS_STATE_KEEP_SHRINK = 3;
    private int mRunState;
    private static final int RUN_STATE_STOPPED = 0;
    private static final int RUN_STATE_STARTING = 1;
    private static final int RUN_STATE_STARTED = 2;
    private static final int RUN_STATE_RUNNING = 3;
    private static final int RUN_STATE_STOPPING = 4;
    private Paint mPaint;
    private RectF mRect;
    private float mStartAngle;
    private float mSweepAngle;
    private int mStrokeColorIndex;
    private int mPadding;
    private float mInitialAngle;
    private float mProgressPercent;
    private float mSecondaryProgressPercent;
    private float mMaxSweepAngle;
    private float mMinSweepAngle;
    private int mStrokeSize;
    private int[] mStrokeColors;
    private int mStrokeSecondaryColor;
    private boolean mReverse;
    private int mRotateDuration;
    private int mTransformDuration;
    private int mKeepDuration;
    private float mInStepPercent;
    private int[] mInColors;
    private int mInAnimationDuration;
    private int mOutAnimationDuration;
    private int mProgressMode;
    private Interpolator mTransformInterpolator;
    private final Runnable mUpdater;

    private IndeterminateProgressDrawable(int padding, float initialAngle, float progressPercent, float secondaryProgressPercent,
                                          float maxSweepAngle, float minSweepAngle, int strokeSize, int[] strokeColors, int strokeSecondaryColor,
                                          boolean reverse, int rotateDuration, int transformDuration, int keepDuration, Interpolator transformInterpolator,
                                          int progressMode, int inAnimDuration, float inStepPercent, int[] inStepColors, int outAnimDuration) {
        this.mRunState = 0;
        this.mUpdater = new Runnable() {
            public void run() {
                IndeterminateProgressDrawable.this.update();
            }
        };
        this.mPadding = padding;
        this.mInitialAngle = initialAngle;
        this.setProgress(progressPercent);
        this.setSecondaryProgress(secondaryProgressPercent);
        this.mMaxSweepAngle = maxSweepAngle;
        this.mMinSweepAngle = minSweepAngle;
        this.mStrokeSize = strokeSize;
        this.mStrokeColors = strokeColors;
        this.mStrokeSecondaryColor = strokeSecondaryColor;
        this.mReverse = reverse;
        this.mRotateDuration = rotateDuration;
        this.mTransformDuration = transformDuration;
        this.mKeepDuration = keepDuration;
        this.mTransformInterpolator = transformInterpolator;
        this.mProgressMode = progressMode;
        this.mInAnimationDuration = inAnimDuration;
        this.mInStepPercent = inStepPercent;
        this.mInColors = inStepColors;
        this.mOutAnimationDuration = outAnimDuration;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeCap(Cap.ROUND);
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mRect = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {
        switch (this.mProgressMode) {
            case 0:
                this.drawDeterminate(canvas);
                break;
            case 1:
                this.drawIndeterminate(canvas);
        }
    }

    private void drawDeterminate(Canvas canvas) {
        Rect bounds = this.getBounds();
        float radius = 0.0F;
        float size = 0.0F;
        if (this.mRunState == RUN_STATE_STARTING) {
            size = (float) this.mStrokeSize * (float) Math.min((long) this.mInAnimationDuration, SystemClock.uptimeMillis() - this.mLastRunStateTime) / (float) this.mInAnimationDuration;
            if (size > 0.0F) {
                radius = ((float) (Math.min(bounds.width(), bounds.height()) - this.mPadding * 2 - this.mStrokeSize * 2) + size) / 2.0F;
            }
        } else if (this.mRunState == 4) {
            size = (float) this.mStrokeSize * (float) Math.max(0L, (long) this.mOutAnimationDuration - SystemClock.uptimeMillis() + this.mLastRunStateTime) / (float) this.mOutAnimationDuration;
            if (size > 0.0F) {
                radius = ((float) (Math.min(bounds.width(), bounds.height()) - this.mPadding * 2 - this.mStrokeSize * 2) + size) / 2.0F;
            }
        } else if (this.mRunState != 0) {
            size = (float) this.mStrokeSize;
            radius = (float) (Math.min(bounds.width(), bounds.height()) - this.mPadding * 2 - this.mStrokeSize) / 2.0F;
        }

        if (radius > 0.0F) {
            float x = (float) (bounds.left + bounds.right) / 2.0F;
            float y = (float) (bounds.top + bounds.bottom) / 2.0F;
            this.mPaint.setStrokeWidth(size);
            this.mPaint.setStyle(Style.STROKE);
            if (this.mProgressPercent == 1.0F) {
                this.mPaint.setColor(this.mStrokeColors[0]);
                canvas.drawCircle(x, y, radius, this.mPaint);
            } else if (this.mProgressPercent == 0.0F) {
                this.mPaint.setColor(this.mStrokeSecondaryColor);
                canvas.drawCircle(x, y, radius, this.mPaint);
            } else {
                float sweepAngle = (float) (this.mReverse ? -360 : 360) * this.mProgressPercent;
                this.mRect.set(x - radius, y - radius, x + radius, y + radius);
                this.mPaint.setColor(this.mStrokeSecondaryColor);
                canvas.drawArc(this.mRect, this.mInitialAngle + sweepAngle, (float) (this.mReverse ? -360 : 360) - sweepAngle, false, this.mPaint);
                this.mPaint.setColor(this.mStrokeColors[0]);
                canvas.drawArc(this.mRect, this.mInitialAngle, sweepAngle, false, this.mPaint);
            }
        }

    }

    private int getIndeterminateStrokeColor() {
        if (this.mProgressState == 3 && this.mStrokeColors.length != 1) {
            float value = Math.max(0.0F, Math.min(1.0F, (float) (SystemClock.uptimeMillis() - this.mLastProgressStateTime) / (float) this.mKeepDuration));
            int prev_index = this.mStrokeColorIndex == 0 ? this.mStrokeColors.length - 1 : this.mStrokeColorIndex - 1;
            return ColorUtil.getMiddleColor(this.mStrokeColors[prev_index], this.mStrokeColors[this.mStrokeColorIndex], value);
        } else {
            return this.mStrokeColors[this.mStrokeColorIndex];
        }
    }

    private int c = 0;

    private void drawIndeterminate(Canvas canvas) {
        Rect bounds;
        float radius;
        float x;
        float y;
        float y1;
        if (this.mRunState == RUN_STATE_STARTING) {
            bounds = this.getBounds();
            radius = (float) (bounds.left + bounds.right) / 2.0F;
            x = (float) (bounds.top + bounds.bottom) / 2.0F;
            y = (float) (Math.min(bounds.width(), bounds.height()) - this.mPadding * 2) / 2.0F;
            y1 = 1.0F / (this.mInStepPercent * (float) (this.mInColors.length + 2) + 1.0F);
            float time = (float) (SystemClock.uptimeMillis() - this.mLastRunStateTime) / (float) this.mInAnimationDuration;
            float steps = time / y1;
            float outerRadius = 0.0F;
            float innerRadius = 0.0F;

            for (int radius1 = (int) Math.floor((double) steps); radius1 >= 0; --radius1) {
                innerRadius = outerRadius;
                outerRadius = Math.min(1.0F, (steps - (float) radius1) * this.mInStepPercent) * y;
                if (radius1 < this.mInColors.length) {
                    if (innerRadius == 0.0F) {
                        this.mPaint.setColor(this.mInColors[radius1]);
                        this.mPaint.setStyle(Style.FILL);
                        canvas.drawCircle(radius, x, outerRadius, this.mPaint);
                    } else {
                        if (outerRadius <= innerRadius) {
                            break;
                        }

                        float radius2 = (innerRadius + outerRadius) / 2.0F;
                        this.mRect.set(radius - radius2, x - radius2, radius + radius2, x + radius2);
                        this.mPaint.setStrokeWidth(outerRadius - innerRadius);
                        this.mPaint.setStyle(Style.STROKE);
                        this.mPaint.setColor(this.mInColors[radius1]);
                        canvas.drawCircle(radius, x, radius2, this.mPaint);
                    }
                }
            }

            if (this.mProgressState == PROGRESS_STATE_HIDE) {
                if (steps >= 1.0F / this.mInStepPercent || time >= 1.0F) {
                    this.resetAnimation();
                }
            } else {
                float var15 = y - (float) this.mStrokeSize / 2.0F;
                this.mRect.set(radius - var15, x - var15, radius + var15, x + var15);
                this.mPaint.setStrokeWidth((float) this.mStrokeSize);
                this.mPaint.setStyle(Style.STROKE);
                this.mPaint.setColor(this.getIndeterminateStrokeColor());
                canvas.drawArc(this.mRect, this.mStartAngle, this.mSweepAngle, false, this.mPaint);
            }
        } else if (this.mRunState == RUN_STATE_STOPPING) {
            float var13 = (float) this.mStrokeSize * (float) Math.max(0L, (long) this.mOutAnimationDuration - SystemClock.uptimeMillis() + this.mLastRunStateTime) / (float) this.mOutAnimationDuration;
            if (var13 > 0.0F) {
                Rect var14 = this.getBounds();
                x = ((float) (Math.min(var14.width(), var14.height()) - this.mPadding * 2 - this.mStrokeSize * 2) + var13) / 2.0F;
                y = (float) (var14.left + var14.right) / 2.0F;
                y1 = (float) (var14.top + var14.bottom) / 2.0F;
                this.mRect.set(y - x, y1 - x, y + x, y1 + x);
                this.mPaint.setStrokeWidth(var13);
                this.mPaint.setStyle(Style.STROKE);
                this.mPaint.setColor(this.getIndeterminateStrokeColor());
                canvas.drawArc(this.mRect, this.mStartAngle, this.mSweepAngle, false, this.mPaint);
            }
        } else if (this.mRunState != RUN_STATE_STOPPED) {
            bounds = this.getBounds();
            radius = (float) (Math.min(bounds.width(), bounds.height()) - this.mPadding * 2 - this.mStrokeSize) / 2.0F;
            x = (float) (bounds.left + bounds.right) / 2.0F;
            y = (float) (bounds.top + bounds.bottom) / 2.0F;
            this.mRect.set(x - radius, y - radius, x + radius, y + radius);
            this.mPaint.setStrokeWidth((float) this.mStrokeSize);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setColor(this.getIndeterminateStrokeColor());
            canvas.drawArc(this.mRect, this.mStartAngle, this.mSweepAngle, false, this.mPaint);
        }

    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -3;
    }

    public int getProgressMode() {
        return this.mProgressMode;
    }

    public void setProgressMode(int mode) {
        if (this.mProgressMode != mode) {
            this.mProgressMode = mode;
            this.invalidateSelf();
        }

    }

    public float getProgress() {
        return this.mProgressPercent;
    }

    public float getSecondaryProgress() {
        return this.mSecondaryProgressPercent;
    }

    public void setProgress(float percent) {
        percent = Math.min(1.0F, Math.max(0.0F, percent));
        if (this.mProgressPercent != percent) {
            this.mProgressPercent = percent;
            if (this.isRunning()) {
                this.invalidateSelf();
            } else if (this.mProgressPercent != 0.0F) {
                this.start();
            }
        }
    }

    public void setSecondaryProgress(float percent) {
        percent = Math.min(1.0F, Math.max(0.0F, percent));
        if (this.mSecondaryProgressPercent != percent) {
            this.mSecondaryProgressPercent = percent;
            if (this.isRunning()) {
                this.invalidateSelf();
            } else if (this.mSecondaryProgressPercent != 0.0F) {
                this.start();
            }
        }
    }

    private void resetAnimation() {
        this.mLastUpdateTime = SystemClock.uptimeMillis();
        this.mLastProgressStateTime = this.mLastUpdateTime;
        this.mStartAngle = this.mInitialAngle;
        this.mStrokeColorIndex = 0;
        this.mSweepAngle = this.mReverse ? -this.mMinSweepAngle : this.mMinSweepAngle;
        this.mProgressState = 0;
    }

    public void start() {
        this.start(this.mInAnimationDuration > 0);
    }

    public void stop() {
        this.stop(this.mOutAnimationDuration > 0);
    }

    private void start(boolean withAnimation) {
        if (!this.isRunning()) {
            if (withAnimation) {
                this.mRunState = 1;
                this.mLastRunStateTime = SystemClock.uptimeMillis();
                this.mProgressState = PROGRESS_STATE_HIDE;
            } else {
                this.resetAnimation();
            }

            this.scheduleSelf(this.mUpdater, SystemClock.uptimeMillis() + 16L);
            this.invalidateSelf();
        }
    }

    private void stop(boolean withAnimation) {
        if (this.isRunning()) {
            if (withAnimation) {
                this.mLastRunStateTime = SystemClock.uptimeMillis();
                if (this.mRunState == 2) {
                    this.scheduleSelf(this.mUpdater, SystemClock.uptimeMillis() + 16L);
                    this.invalidateSelf();
                }

                this.mRunState = 4;
            } else {
                this.mRunState = 0;
                this.unscheduleSelf(this.mUpdater);
                this.invalidateSelf();
            }

        }
    }

    public boolean isRunning() {
        return this.mRunState != 0;
    }

    public void scheduleSelf(Runnable what, long when) {
        if (this.mRunState == 0) {
            this.mRunState = this.mInAnimationDuration > 0 ? 1 : 3;
        }

        super.scheduleSelf(what, when);
    }

    private void update() {
        switch (this.mProgressMode) {
            case 0:
                this.updateDeterminate();
                break;
            case 1:
                this.updateIndeterminate();
        }

    }

    private void updateDeterminate() {
        long curTime = SystemClock.uptimeMillis();
        if (this.mRunState == 1) {
            if (curTime - this.mLastRunStateTime > (long) this.mInAnimationDuration) {
                this.mRunState = 2;
                return;
            }
        } else if (this.mRunState == 4 && curTime - this.mLastRunStateTime > (long) this.mOutAnimationDuration) {
            this.stop(false);
            return;
        }

        if (this.isRunning()) {
            this.scheduleSelf(this.mUpdater, SystemClock.uptimeMillis() + 16L);
        }

        this.invalidateSelf();
    }

    private void updateIndeterminate() {
        long curTime = SystemClock.uptimeMillis();
        float rotateOffset = (float) (curTime - this.mLastUpdateTime) * 360.0F / (float) this.mRotateDuration;
        if (this.mReverse) {
            rotateOffset = -rotateOffset;
        }

        this.mLastUpdateTime = curTime;
        float value;
        float maxAngle;
        float minAngle;
        switch (this.mProgressState) {
            case 0:
                if (this.mTransformDuration <= 0) {
                    this.mSweepAngle = this.mReverse ? -this.mMinSweepAngle : this.mMinSweepAngle;
                    this.mProgressState = 1;
                    this.mStartAngle += rotateOffset;
                    this.mLastProgressStateTime = curTime;
                } else {
                    value = (float) (curTime - this.mLastProgressStateTime) / (float) this.mTransformDuration;
                    maxAngle = this.mReverse ? -this.mMaxSweepAngle : this.mMaxSweepAngle;
                    minAngle = this.mReverse ? -this.mMinSweepAngle : this.mMinSweepAngle;
                    this.mStartAngle += rotateOffset;
                    this.mSweepAngle = this.mTransformInterpolator.getInterpolation(value) * (maxAngle - minAngle) + minAngle;
                    if (value > 1.0F) {
                        this.mSweepAngle = maxAngle;
                        this.mProgressState = 1;
                        this.mLastProgressStateTime = curTime;
                    }
                }
                break;
            case 1:
                this.mStartAngle += rotateOffset;
                if (curTime - this.mLastProgressStateTime > (long) this.mKeepDuration) {
                    this.mProgressState = 2;
                    this.mLastProgressStateTime = curTime;
                }
                break;
            case 2:
                if (this.mTransformDuration <= 0) {
                    this.mSweepAngle = this.mReverse ? -this.mMinSweepAngle : this.mMinSweepAngle;
                    this.mProgressState = 3;
                    this.mStartAngle += rotateOffset;
                    this.mLastProgressStateTime = curTime;
                    this.mStrokeColorIndex = (this.mStrokeColorIndex + 1) % this.mStrokeColors.length;
                } else {
                    value = (float) (curTime - this.mLastProgressStateTime) / (float) this.mTransformDuration;
                    maxAngle = this.mReverse ? -this.mMaxSweepAngle : this.mMaxSweepAngle;
                    minAngle = this.mReverse ? -this.mMinSweepAngle : this.mMinSweepAngle;
                    float newSweepAngle = (1.0F - this.mTransformInterpolator.getInterpolation(value)) * (maxAngle - minAngle) + minAngle;
                    this.mStartAngle += rotateOffset + this.mSweepAngle - newSweepAngle;
                    this.mSweepAngle = newSweepAngle;
                    if (value > 1.0F) {
                        this.mSweepAngle = minAngle;
                        this.mProgressState = 3;
                        this.mLastProgressStateTime = curTime;
                        this.mStrokeColorIndex = (this.mStrokeColorIndex + 1) % this.mStrokeColors.length;
                    }
                }
                break;
            case 3:
                this.mStartAngle += rotateOffset;
                if (curTime - this.mLastProgressStateTime > (long) this.mKeepDuration) {
                    this.mProgressState = 0;
                    this.mLastProgressStateTime = curTime;
                }
        }

        if (this.mRunState == 1) {
            if (curTime - this.mLastRunStateTime > (long) this.mInAnimationDuration) {
                this.mRunState = 3;
                if (this.mProgressState == PROGRESS_STATE_HIDE) {
                    this.resetAnimation();
                }
            }
        } else if (this.mRunState == 4 && curTime - this.mLastRunStateTime > (long) this.mOutAnimationDuration) {
            this.stop(false);
            return;
        }

        if (this.isRunning()) {
            this.scheduleSelf(this.mUpdater, SystemClock.uptimeMillis() + 16L);
        }

        this.invalidateSelf();
    }

    public static class Builder {
        private int mPadding;
        private float mInitialAngle;
        private float mProgressPercent;
        private float mSecondaryProgressPercent;
        private float mMaxSweepAngle;
        private float mMinSweepAngle;
        private int mStrokeSize;
        private int[] mStrokeColors;
        private int mStrokeSecondaryColor;
        private boolean mReverse;
        private int mRotateDuration;
        private int mTransformDuration;
        private int mKeepDuration;
        private Interpolator mTransformInterpolator;
        private int mProgressMode;
        private float mInStepPercent;
        private int[] mInColors;
        private int mInAnimationDuration;
        private int mOutAnimationDuration;

        public Builder(int what) {
            if (what == CircleProgressView.FOR_LIST) {
                this.mInAnimationDuration = 400;
                //цвета при длительном продолжении
                this.mStrokeColors = new int[]{0xFF5B95C2};
                //не обязательное(это начальные цвета при появлении)
                this.mInColors = new int[]{0xFFDAE7F3};
            } else {
                this.mInAnimationDuration = 800;
                this.mStrokeColors = new int[]{Color.WHITE};
                this.mInColors = new int[]{0xFFDAE7F3};
            }
            this.mPadding = AndroidUtil.dp(2);
            this.mInitialAngle = 0;
            this.mProgressPercent = 0.0f;
            this.mSecondaryProgressPercent = 0.0f;
            this.mMaxSweepAngle = 270;
            this.mMinSweepAngle = 1;
            this.mStrokeSize = AndroidUtil.dp(3);

            this.mStrokeSecondaryColor = 0;
            this.mReverse = false;
            this.mRotateDuration = 1000;
            this.mTransformDuration = 600;
            this.mKeepDuration = 300;
            this.mProgressMode = 1;

            this.mInStepPercent = 0.5F;
            this.mOutAnimationDuration = 400;
        }

        public IndeterminateProgressDrawable build() {
            if (this.mTransformInterpolator == null) {
                this.mTransformInterpolator = new DecelerateInterpolator();
            }

            return new IndeterminateProgressDrawable(this.mPadding, this.mInitialAngle,
                    this.mProgressPercent, this.mSecondaryProgressPercent, this.mMaxSweepAngle,
                    this.mMinSweepAngle, this.mStrokeSize, this.mStrokeColors,
                    this.mStrokeSecondaryColor, this.mReverse,
                    this.mRotateDuration, this.mTransformDuration,
                    this.mKeepDuration, this.mTransformInterpolator,
                    this.mProgressMode, this.mInAnimationDuration,
                    this.mInStepPercent, this.mInColors, this.mOutAnimationDuration);
        }
    }
}