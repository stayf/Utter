package com.stayfprod.utter.ui.view;


import android.view.ViewGroup;


public interface Scrollable {

    void setScrollViewCallbacks(ObservableScrollViewCallbacks listener);

    void scrollVerticallyTo(int y);

    int getCurrentScrollY();

    void setTouchInterceptionViewGroup(ViewGroup viewGroup);
}