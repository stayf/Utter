package com.stayfprod.utter.model;


import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.URLSpan;

public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}