package com.stayfprod.utter.ui.view;


import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.util.LockUtils;

public class LockPatternView extends View {

    public static class Cell implements Parcelable {

        public final int row;
        public final int column;

        static Cell[][] sCells = new Cell[MATRIX_WIDTH][MATRIX_WIDTH];

        static {
            for (int i = 0; i < MATRIX_WIDTH; i++) {
                for (int j = 0; j < MATRIX_WIDTH; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }


        public int getId() {
            return row * MATRIX_WIDTH + column;
        }

        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }


        public static synchronized Cell of(int id) {
            return of(id / MATRIX_WIDTH, id % MATRIX_WIDTH);
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("row must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
            if (column < 0 || column > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("column must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
        }

        @Override
        public String toString() {
            return "(row=" + row + ",column=" + column + ")";
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Cell)
                return column == ((Cell) object).column
                        && row == ((Cell) object).row;
            return super.equals(object);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(column);
            dest.writeInt(row);
        }

        public static final Parcelable.Creator<Cell> CREATOR = new Parcelable.Creator<Cell>() {

            public Cell createFromParcel(Parcel in) {
                return new Cell(in);
            }

            public Cell[] newArray(int size) {
                return new Cell[size];
            }
        };

        private Cell(Parcel in) {
            column = in.readInt();
            row = in.readInt();
        }
    }

    public enum DisplayMode {
        Correct,
        Animate,
        Wrong
    }

    public interface OnPatternListener {
        void onPatternStart();

        void onPatternCleared();

        void onPatternCellAdded(List<Cell> pattern);

        void onPatternDetected(List<Cell> pattern);
    }

    private static final int ASPECT_SQUARE = 0;
    private static final int ASPECT_LOCK_WIDTH = 1;
    private static final int ASPECT_LOCK_HEIGHT = 2;

    public static final int MATRIX_WIDTH = 3;

    public static final int MATRIX_SIZE = MATRIX_WIDTH * MATRIX_WIDTH;

    private static final boolean PROFILE_DRAWING = false;
    private final CellState[][] mCellStates;

    private final int mDotSize;
    private final int mDotSizeActivated;
    private final int mPathWidth;

    private boolean mDrawingProfilingStarted = false;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;

    private static final float DRAG_THRESHHOLD = 0.0f;

    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(MATRIX_SIZE);

    private boolean[][] mPatternDrawLookup = new boolean[MATRIX_WIDTH][MATRIX_WIDTH];

    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private long mAnimatingPeriodStart;

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;

    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private final Rect mTmpInvalidateRect = new Rect();

    private int mAspect;
    private int mRegularColor;
    private int mErrorColor;
    private int mSuccessColor;

    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;

    public static class CellState {
        public float scale = 1.0f;
        public float translateY = 0.0f;
        public float alpha = 1.0f;
        public float size;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        public ValueAnimator lineAnimator;
    }

    public LockPatternView(Context context) {
        this(context, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final String aspect = "square";

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

        mRegularColor = Color.WHITE;
        mErrorColor = Color.RED;
        mSuccessColor = Color.WHITE;

        int pathColor = mRegularColor;

        mPathPaint.setColor(pathColor);

        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mPathWidth = Constant.DP_4;
        mPathPaint.setStrokeWidth(mPathWidth);

        mDotSize = Constant.DP_14;
        mDotSizeActivated = Constant.DP_28;

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mCellStates = new CellState[MATRIX_WIDTH][MATRIX_WIDTH];
        for (int i = 0; i < MATRIX_WIDTH; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                mCellStates[i][j] = new CellState();
                mCellStates[i][j].size = mDotSize;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !isInEditMode()) {
            mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                    context, android.R.interpolator.fast_out_slow_in);
            mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                    context, android.R.interpolator.linear_out_slow_in);
        }
    }

    public CellState[][] getCellStates() {
        return mCellStates;
    }

    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    @SuppressWarnings("unchecked")
    public List<Cell> getPattern() {
        return (List<Cell>) mPattern.clone();
    }

    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.row][cell.column] = true;
        }

        setDisplayMode(displayMode);
    }

    public DisplayMode getDisplayMode() {
        return mPatternDisplayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException(
                        "you must have a pattern to "
                                + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.column);
            mInProgressY = getCenterYForRow(first.row);
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private void notifyCellAdded() {
        sendAccessEvent("Cell added");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }

    private void notifyPatternStarted() {
        sendAccessEvent("Pattern started");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent("Pattern completed");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent("Pattern cleared");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    private void clearPatternDrawLookup() {
        for (int i = 0; i < MATRIX_WIDTH; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    public void disableInput() {
        mInputEnabled = false;
    }

    public void enableInput() {
        mInputEnabled = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - getPaddingLeft() - getPaddingRight();
        mSquareWidth = width / (float) MATRIX_WIDTH;

        final int height = h - getPaddingTop() - getPaddingBottom();
        mSquareHeight = height / (float) MATRIX_WIDTH;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null
                    && !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
                    performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                    | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }

    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.row][newCell.column] = true;
        mPattern.add(newCell);
        if (!mInStealthMode) {
            startCellActivatedAnimation(newCell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(Cell cell) {
        final CellState cellState = mCellStates[cell.row][cell.column];
        startSizeAnimation(mDotSize, mDotSizeActivated, 96,
                mLinearOutSlowInInterpolator, cellState, new Runnable() {

                    @Override
                    public void run() {
                        startSizeAnimation(mDotSizeActivated, mDotSize, 192,
                                mFastOutSlowInInterpolator, cellState, null);
                    }
                });
        startLineEndAnimation(cellState, mInProgressX, mInProgressY,
                getCenterXForColumn(cell.column), getCenterYForRow(cell.row));
    }

    private void startLineEndAnimation(final CellState state,
                                       final float startX, final float startY, final float targetX,
                                       final float targetY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float t = (Float) animation.getAnimatedValue();
                        state.lineEndX = (1 - t) * startX + t * targetX;
                        state.lineEndY = (1 - t) * startY + t * targetY;
                        invalidate();
                    }
                });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state.lineAnimator = null;
            }
        });
        valueAnimator.setInterpolator(mFastOutSlowInInterpolator);
        valueAnimator.setDuration(100);
        valueAnimator.start();
        state.lineAnimator = valueAnimator;
    }

    private void startSizeAnimation(float start, float end, long duration,
                                    Interpolator interpolator, final CellState state,
                                    final Runnable endRunnable) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        state.size = (Float) animation.getAnimatedValue();
                        invalidate();
                    }

                });
        if (endRunnable != null) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (endRunnable != null)
                        endRunnable.run();
                }
            });
        }
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = getPaddingTop() + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onHoverEvent(@NonNull MotionEvent event) {
        if (((AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE)).isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                mPatternInProgress = false;
                resetPattern();
                notifyPatternCleared();

                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        final float radius = mPathWidth;
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event
                    .getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event
                    .getY();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx > DRAG_THRESHHOLD || dy > DRAG_THRESHHOLD) {
                invalidateNow = true;
            }

            if (mPatternInProgress && patternSize > 0) {
                final ArrayList<Cell> pattern = mPattern;
                final Cell lastCell = pattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.column);
                float lastCellCenterY = getCenterYForRow(lastCell.row);

                float left = Math.min(lastCellCenterX, x) - radius;
                float right = Math.max(lastCellCenterX, x) + radius;
                float top = Math.min(lastCellCenterY, y) - radius;
                float bottom = Math.max(lastCellCenterY, y) + radius;

                if (hitCell != null) {
                    final float width = mSquareWidth * 0.5f;
                    final float height = mSquareHeight * 0.5f;
                    final float hitCellCenterX = getCenterXForColumn(hitCell.column);
                    final float hitCellCenterY = getCenterYForRow(hitCell.row);

                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(hitCellCenterY - height, top);
                    bottom = Math.max(hitCellCenterY + height, bottom);
                }

                mTmpInvalidateRect.union(Math.round(left), Math.round(top),
                        Math.round(right), Math.round(bottom));
            }
        }
        mInProgressX = event.getX();
        mInProgressY = event.getY();

        if (invalidateNow) {
            mInvalidate.union(mTmpInvalidateRect);
            invalidate(mInvalidate);
            mInvalidate.set(mTmpInvalidateRect);
        }
    }

    private void sendAccessEvent(int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setContentDescription(getContext().getString(resId));
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            setContentDescription(null);
        } else
            announceForAccessibility(getContext().getString(resId));
    }

    private void sendAccessEvent(String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setContentDescription(text);
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            setContentDescription(null);
        } else
            announceForAccessibility(text);
    }

    private void handleActionUp(MotionEvent event) {
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void cancelLineAnimations() {
        for (int i = 0; i < MATRIX_WIDTH; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                CellState state = mCellStates[i][j];
                if (state.lineAnimator != null) {
                    state.lineAnimator.cancel();
                    state.lineEndX = Float.MIN_VALUE;
                    state.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset),
                    (int) (startY - heightOffset),
                    (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private float getCenterXForColumn(int column) {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.row][cell.column] = true;
            }

            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
                        / MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.column) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }
            invalidate();
        }

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        for (int i = 0; i < MATRIX_WIDTH; i++) {
            float centerY = getCenterYForRow(i);
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                CellState cellState = mCellStates[i][j];
                float centerX = getCenterXForColumn(j);
                float size = cellState.size * cellState.scale;
                float translationY = cellState.translateY;
                drawCircle(canvas, (int) centerX, (int) centerY + translationY,
                        size, drawLookup[i][j], cellState.alpha);
            }
        }

        final boolean drawPath = !mInStealthMode;

        if (drawPath) {
            mPathPaint.setColor(getCurrentColor(true));

            boolean anyCircles = false;
            float lastX = 0f;
            float lastY = 0f;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i != 0) {
                    CellState state = mCellStates[cell.row][cell.column];
                    currentPath.rewind();
                    currentPath.moveTo(lastX, lastY);
                    if (state.lineEndX != Float.MIN_VALUE
                            && state.lineEndY != Float.MIN_VALUE) {
                        currentPath.lineTo(state.lineEndX, state.lineEndY);
                    } else {
                        currentPath.lineTo(centerX, centerY);
                    }
                    canvas.drawPath(currentPath, mPathPaint);
                }
                lastX = centerX;
                lastY = centerY;
            }

            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles) {
                currentPath.rewind();
                currentPath.moveTo(lastX, lastY);
                currentPath.lineTo(mInProgressX, mInProgressY);

                mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(
                        mInProgressX, mInProgressY, lastX, lastY) * 255f));
                canvas.drawPath(currentPath, mPathPaint);
            }
        }
    }

    private float calculateLastSegmentAlpha(float x, float y, float lastX,
                                            float lastY) {
        float diffX = x - lastX;
        float diffY = y - lastY;
        float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        float frac = dist / mSquareWidth;
        return Math.min(1f, Math.max(0f, (frac - 0.3f) * 4f));
    }

    private int getCurrentColor(boolean partOfPattern) {
        if (!partOfPattern || mInStealthMode || mPatternInProgress) {
            return mRegularColor;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            return mErrorColor;
        } else if (mPatternDisplayMode == DisplayMode.Correct
                || mPatternDisplayMode == DisplayMode.Animate) {
            return mSuccessColor;
        } else {
            throw new IllegalStateException("unknown display mode " + mPatternDisplayMode);
        }
    }

    private void drawCircle(Canvas canvas, float centerX, float centerY,
                            float size, boolean partOfPattern, float alpha) {
        mPaint.setColor(getCurrentColor(partOfPattern));
        mPaint.setAlpha((int) (alpha * 255));
        canvas.drawCircle(centerX, centerY, size / 2, mPaint);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,
                LockUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(), mInputEnabled, mInStealthMode,
                mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(DisplayMode.Correct,
                LockUtils.stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    private static class SavedState extends BaseSavedState {

        private final String mSerializedPattern;
        private final int mDisplayMode;
        private final boolean mInputEnabled;
        private final boolean mInStealthMode;
        private final boolean mTactileFeedbackEnabled;

        private SavedState(Parcelable superState, String serializedPattern,
                           int displayMode, boolean inputEnabled, boolean inStealthMode,
                           boolean tactileFeedbackEnabled) {
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        private SavedState(Parcel in) {
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean) in.readValue(null);
            mInStealthMode = (Boolean) in.readValue(null);
            mTactileFeedbackEnabled = (Boolean) in.readValue(null);
        }

        public String getSerializedPattern() {
            return mSerializedPattern;
        }

        public int getDisplayMode() {
            return mDisplayMode;
        }

        public boolean isInputEnabled() {
            return mInputEnabled;
        }

        public boolean isInStealthMode() {
            return mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}