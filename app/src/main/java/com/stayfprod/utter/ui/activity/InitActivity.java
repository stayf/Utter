package com.stayfprod.utter.ui.activity;

import android.graphics.Color;
import android.os.Bundle;

import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.App;

public class InitActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.setAppContext(this);
        super.onCreate(savedInstanceState);

        new AuthManager(this, null).checkAuthState();
        getWindow().setWindowAnimations(0);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }
}
