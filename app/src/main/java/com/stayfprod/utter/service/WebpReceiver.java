package com.stayfprod.utter.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stayfprod.utter.manager.WebpSupportManager;

public class WebpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        if (intent != null && intent.getAction() != null && WebpSupportService.ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            int key = intent.getIntExtra("key", 0);
            String tmpFile = intent.getStringExtra("tmpFile");
            String path = intent.getStringExtra("path");
            WebpSupportManager.getManager().handleReceived(key, tmpFile, path);
        }
    }
}
