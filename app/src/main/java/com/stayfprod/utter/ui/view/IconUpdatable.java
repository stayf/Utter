package com.stayfprod.utter.ui.view;


import com.stayfprod.utter.ui.drawable.IconDrawable;

public interface IconUpdatable {
    void setIconAsync(IconDrawable iconDrawable, boolean isForwardIcon, boolean... animated);
}
