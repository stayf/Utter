package com.stayfprod.utter.ui.view;


import android.graphics.drawable.BitmapDrawable;

public interface ImageUpdatable {
    void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated);
}
