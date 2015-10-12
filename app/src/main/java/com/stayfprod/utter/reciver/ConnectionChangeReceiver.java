package com.stayfprod.utter.reciver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.stayfprod.utter.manager.OptionManager;
import com.stayfprod.utter.service.ThreadService;

import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private final static String LOG = "ConChangeReceiver";
    private static volatile boolean isTaskRun = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        checkConnection(context);
    }

    public static void checkConnection(Context context) {
        if (isHaveConnection(context)) {
            checkConnectionByTimeout();
        } else {
            OptionManager.getManager().setNetworkUnreachableOption(true);
        }
    }

    public static void checkConnectionByTimeout() {
        ThreadService.runConnectionCheckTask(new Runnable() {
            @Override
            public void run() {
                isTaskRun = true;
                while (isTaskRun) {
                    if (isReallyHaveInternet()) {
                        isTaskRun = false;
                        OptionManager.getManager().setNetworkUnreachableOption(false);
                    } else {
                        OptionManager.getManager().setNetworkUnreachableOption(true);
                    }
                }
            }
        });
    }

    public static boolean isHaveConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isReallyHaveInternet() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection urlc = (HttpURLConnection) (url.openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000);
            urlc.connect();//info NetworkOnMainThreadException нельзя в ui запускать
            return urlc.getResponseCode() == 200;
        } catch (Throwable e) {
            Log.w(LOG, "Error checking internet connection" + e.getMessage() + "---", e);
        }
        return false;
    }
}
