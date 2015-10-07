package com.stayfprod.utter.ui.listener;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public abstract class AppStatusListener implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
