package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.stayfprod.utter.model.ScrollState;

public class ObservableScrollView extends ScrollView implements Scrollable {

    private int mPrevScrollY;
    private int mScrollY;

    private ObservableScrollViewCallbacks mCallbacks;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        mPrevScrollY = ss.prevScrollY;
        mScrollY = ss.scrollY;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.prevScrollY = mPrevScrollY;
        ss.scrollY = mScrollY;
        return ss;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mCallbacks != null) {
            mScrollY = t;

            mCallbacks.onScrollChanged(t, mFirstScroll, mDragging);
            if (mFirstScroll) {
                mFirstScroll = false;
            }

            if (mPrevScrollY < t) {
                mScrollState = ScrollState.UP;
            } else if (t < mPrevScrollY) {
                mScrollState = ScrollState.DOWN;
            }
            mPrevScrollY = t;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mFirstScroll = mDragging = true;
                    mCallbacks.onDownMotionEvent();
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIntercepted = false;
                    mDragging = false;
                    mCallbacks.onUpOrCancelMotionEvent(mScrollState);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mPrevMoveEvent == null) {
                        mPrevMoveEvent = ev;
                    }
                    float diffY = ev.getY() - mPrevMoveEvent.getY();
                    mPrevMoveEvent = MotionEvent.obtainNoHistory(ev);
                    if (getCurrentScrollY() - diffY <= 0) {

                        if (mIntercepted) {
                            return false;
                        }

                        final ViewGroup parent;
                        if (mTouchInterceptionViewGroup == null) {
                            parent = (ViewGroup) getParent();
                        } else {
                            parent = mTouchInterceptionViewGroup;
                        }

                        float offsetX = 0;
                        float offsetY = 0;
                        for (View v = this; v != null && v != parent; v = (View) v.getParent()) {
                            offsetX += v.getLeft() - v.getScrollX();
                            offsetY += v.getTop() - v.getScrollY();
                        }
                        final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                        event.offsetLocation(offsetX, offsetY);

                        if (parent.onInterceptTouchEvent(event)) {
                            mIntercepted = true;

                            event.setAction(MotionEvent.ACTION_DOWN);

                            post(new Runnable() {
                                @Override
                                public void run() {
                                    parent.dispatchTouchEvent(event);
                                }
                            });
                            return false;
                        }
                        return super.onTouchEvent(ev);
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        mCallbacks = listener;
    }

    @Override
    public void setTouchInterceptionViewGroup(ViewGroup viewGroup) {
        mTouchInterceptionViewGroup = viewGroup;
    }

    @Override
    public void scrollVerticallyTo(int y) {
        scrollTo(0, y);
    }

    @Override
    public int getCurrentScrollY() {
        return mScrollY;
    }

    static class SavedState extends BaseSavedState {
        int prevScrollY;
        int scrollY;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            prevScrollY = in.readInt();
            scrollY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(prevScrollY);
            out.writeInt(scrollY);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
