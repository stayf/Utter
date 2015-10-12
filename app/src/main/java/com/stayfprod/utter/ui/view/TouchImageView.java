package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    private Matrix mMatrix;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int CLICK = 3;

    private int mMode = NONE;

    private PointF mLast = new PointF();
    private PointF mStart = new PointF();
    private float mMinScale = 1f;
    private float mMaxScale = 3f;
    private float[] mMatrixValues;
    private int mViewWidth, mViewHeight;
    private float mSaveScale = 1f;
    private float mOrigWidth, mOrigHeight;
    private int mOldMeasuredWidth, mOldMeasuredHeight;
    private ScaleGestureDetector mScaleDetector;
    private Context mContext;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mMatrixValues = new float[9];
        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLast.set(curr);
                        mStart.set(mLast);
                        mMode = DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mMode == DRAG) {
                            float deltaX = curr.x - mLast.x;
                            float deltaY = curr.y - mLast.y;
                            float fixTransX = getFixDragTrans(deltaX, mViewWidth, mOrigWidth * mSaveScale);
                            float fixTransY = getFixDragTrans(deltaY, mViewHeight, mOrigHeight * mSaveScale);
                            mMatrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            mLast.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        mMode = NONE;
                        int xDiff = (int) Math.abs(curr.x - mStart.x);
                        int yDiff = (int) Math.abs(curr.y - mStart.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mMode = NONE;
                        break;

                }

                setImageMatrix(mMatrix);
                invalidate();
                return true;
            }

        });
    }

    public void setMaxZoom(float x) {
        mMaxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = mSaveScale;
            mSaveScale *= mScaleFactor;
            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                mScaleFactor = mMaxScale / origScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                mScaleFactor = mMinScale / origScale;
            }

            if (mOrigWidth * mSaveScale <= mViewWidth || mOrigHeight * mSaveScale <= mViewHeight)
                mMatrix.postScale(mScaleFactor, mScaleFactor, mViewWidth / 2, mViewHeight / 2);
            else
                mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        mMatrix.getValues(mMatrixValues);
        float transX = mMatrixValues[Matrix.MTRANS_X];
        float transY = mMatrixValues[Matrix.MTRANS_Y];
        float fixTransX = getFixTrans(transX, mViewWidth, mOrigWidth * mSaveScale);
        float fixTransY = getFixTrans(transY, mViewHeight, mOrigHeight * mSaveScale);
        if (fixTransX != 0 || fixTransY != 0)
            mMatrix.postTranslate(fixTransX, fixTransY);

    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (mOldMeasuredHeight == mViewWidth && mOldMeasuredHeight == mViewHeight || mViewWidth == 0 || mViewHeight == 0)
            return;
        mOldMeasuredHeight = mViewHeight;
        mOldMeasuredWidth = mViewWidth;

        if (mSaveScale == 1) {
            float scale;
            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;

            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();
            float scaleX = (float) mViewWidth / (float) bmWidth;
            float scaleY = (float) mViewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            mMatrix.setScale(scale, scale);

            float redundantYSpace = (float) mViewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) mViewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            mMatrix.postTranslate(redundantXSpace, redundantYSpace);
            mOrigWidth = mViewWidth - 2 * redundantXSpace;
            mOrigHeight = mViewHeight - 2 * redundantYSpace;
            setImageMatrix(mMatrix);
        }
        fixTrans();
    }
}