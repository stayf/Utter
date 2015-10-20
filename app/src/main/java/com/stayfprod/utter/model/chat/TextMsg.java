package com.stayfprod.utter.model.chat;


import android.text.Spannable;
import android.text.StaticLayout;

public class TextMsg extends AbstractMainMsg {
    public Spannable text;
    final public StaticLayout[] staticTextLayout = new StaticLayout[2];
    final public float[] staticTextLayoutStartY = new float[2];
}
