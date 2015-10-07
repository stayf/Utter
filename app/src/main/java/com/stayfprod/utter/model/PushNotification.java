package com.stayfprod.utter.model;


public class PushNotification {
    public CharSequence who;
    public CharSequence msg;
    public long chatId;
    public boolean isGroup;

    public PushNotification(CharSequence who, CharSequence msg, long chatId, boolean isGroup) {
        this.who = who;
        this.msg = msg;
        this.chatId = chatId;
        this.isGroup = isGroup;
    }
}
