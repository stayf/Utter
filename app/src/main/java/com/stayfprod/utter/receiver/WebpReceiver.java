package com.stayfprod.utter.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stayfprod.utter.manager.WebpSupportManager;
import com.stayfprod.utter.service.WebpSupportService;

public class WebpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && WebpSupportService.ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            int key = intent.getIntExtra("key", 0);
            String tmpFile = intent.getStringExtra("tmpFile");
            String path = intent.getStringExtra("path");
            WebpSupportManager.getManager().handleReceived(key, tmpFile, path);
        }
    }
}
