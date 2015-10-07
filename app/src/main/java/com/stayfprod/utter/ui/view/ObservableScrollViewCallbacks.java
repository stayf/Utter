package com.stayfprod.utter.ui.view;

import com.stayfprod.utter.model.ScrollState;

public interface ObservableScrollViewCallbacks {

    void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging);

    void onDownMotionEvent();

    void onUpOrCancelMotionEvent(ScrollState scrollState);
}