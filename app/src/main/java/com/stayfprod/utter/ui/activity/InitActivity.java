package com.stayfprod.utter.ui.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;

import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.App;

public class InitActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.appContext = this;
        super.onCreate(savedInstanceState);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            AndroidUtil.showToastLong("Utter required external storage!!");
            return;
        }

        new AuthManager(this, null).checkAuthState();
        getWindow().setWindowAnimations(0);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }
}
