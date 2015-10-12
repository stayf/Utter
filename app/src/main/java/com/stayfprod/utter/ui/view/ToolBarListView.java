package com.stayfprod.utter.ui.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.ui.adapter.ToolBarAdapter;

import java.lang.reflect.Method;

public class ToolBarListView {
    private final static String LOG = ToolBarListView.class.getSimpleName();

    private ListView mView;
    private Method mMethod;

    public ToolBarListView(ListView view, final ToolBarAdapter toolBarAdapter) {
        mView = view;
        mView.setClickable(false);
        mView.setScrollContainer(false);
        mView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mView.setAdapter(toolBarAdapter);

        mView.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler = new Handler();

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    toolBarAdapter.getItem(1).setBackgroundColor(0xff4a87b5);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent evt) {
                switch (evt.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mHandler.postDelayed(mAction, 120);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_OUTSIDE: {
                        mHandler.removeCallbacks(mAction);
                        toolBarAdapter.getItem(1).setBackgroundColor(Color.TRANSPARENT);
                        break;
                    }
                }
                return true;
            }
        });

        try {
            mMethod = AbsListView.class.getDeclaredMethod("trackMotionScroll", int.class, int.class);
            mMethod.setAccessible(true);
        } catch (Exception ex) {
            Log.e(LOG, "trackMotionScroll", ex);
            Crashlytics.logException(ex);
        }
    }

    public void scrollListBy(int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mView.scrollListBy(y);
        } else {
            trackMotionScroll(-y, -y);
        }
    }

    private void trackMotionScroll(int deltaY, int incrementalDeltaY) {
        try {
            mMethod.invoke(mView, deltaY, incrementalDeltaY);
        } catch (Exception ex) {
            Log.e(LOG, "trackMotionScroll", ex);
            Crashlytics.logException(ex);
        }
    }
}
