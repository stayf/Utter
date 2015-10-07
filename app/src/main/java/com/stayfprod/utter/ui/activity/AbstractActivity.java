package com.stayfprod.utter.ui.activity;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;

import com.stayfprod.emojicon.EmojiconsPopup;
import com.stayfprod.utter.App;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.Logs;

public class AbstractActivity extends AppCompatActivity {

    public static volatile int WINDOW_CURRENT_WIDTH;
    public static volatile int WINDOW_CURRENT_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        WINDOW_CURRENT_WIDTH = size.x;
        WINDOW_CURRENT_HEIGHT = size.y;
        EmojiconsPopup.WINDOW_WIDTH = size.x;
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
        WINDOW_CURRENT_WIDTH = size.x;
        WINDOW_CURRENT_HEIGHT = size.y;
        EmojiconsPopup.WINDOW_WIDTH = size.x;
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
