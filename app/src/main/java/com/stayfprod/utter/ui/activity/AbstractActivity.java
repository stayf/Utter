package com.stayfprod.utter.ui.activity;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.stayfprod.emojicon.EmojIconsPopup;
import com.stayfprod.utter.manager.ResultController;

public class AbstractActivity extends AppCompatActivity {

    public static volatile int sWindowCurrentWidth;
    public static volatile int sWindowCurrentHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        sWindowCurrentWidth = size.x;
        sWindowCurrentHeight = size.y;
        EmojIconsPopup.sWindowWidth = size.x;
        ResultController.initTG();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        sWindowCurrentWidth = size.x;
        sWindowCurrentHeight = size.y;
        EmojIconsPopup.sWindowWidth = size.x;
    }

    public int getToolBarSize() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        return getResources().getDimensionPixelSize(tv.resourceId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    public final <E extends View> E findView(int id) {
        return (E) findViewById(id);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
