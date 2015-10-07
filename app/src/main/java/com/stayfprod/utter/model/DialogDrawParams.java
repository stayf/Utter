package com.stayfprod.utter.model;


import android.graphics.Rect;
import android.text.StaticLayout;

public class DialogDrawParams {

    public boolean isMute;
    public String unreadCountStr;
    public int layoutHeight;
    public int titleStartX;
    public float textStartX;
    public Rect groupRect = new Rect();

    public CharSequence[] drawTitle = new CharSequence[2];
    public float[] dateStartX = new float[2];
    public float[] counterStartX = new float[2];

    public Rect[] muteRect = {new Rect(), new Rect()};
    public Rect[] errorRect = {new Rect(), new Rect()};
    public Rect[] badgeRect = {new Rect(), new Rect()};
    public Rect[] clockRect = {new Rect(), new Rect()};
    public Rect[] cycleRect = {new Rect(), new Rect()};

    public StaticLayout[] staticTextLayout = new StaticLayout[2];
    public float[] staticTextLayoutStartY = new float[2];
}
