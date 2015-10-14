package com.stayfprod.utter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class SMSMonitor extends BroadcastReceiver {
    public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            if (pduArray != null) {
                for (int i = 0; i < pduArray.length; i++) {
                    String msgBody = SmsMessage.createFromPdu((byte[]) pduArray[i]).getMessageBody();
                    if (msgBody.contains("Telegram code")) {
                        Pattern p = Pattern.compile("(\\d)+");
                        Matcher m = p.matcher(msgBody);
                        if (m.find()) {
                            afterReceiveMsg(m.group());
                            break;
                        }
                    }
                }
            }
        }
    }

    protected abstract void afterReceiveMsg(String code);
}
