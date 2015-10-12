package com.stayfprod.utter.model;


public class NotificationObject {

    public static final int USER_IMAGE_UPDATE = 1;
    public static final int USER_DATA_UPDATE = 2;
    public static final int GALLERY_UPDATE_DATA = 3;
    public static final int LEFT_CHAT = 4;

    public static final int CHANGE_CONNECTION = 5;

    public static final int UPDATE_USER_STATUS = 110;
    public static final int UPDATE_MEMBER_COUNT = 111;

    public static final int UPDATE_CHAT_TITLE = 70;
    public static final int UPDATE_CHAT_PHOTO = 71;
    public static final int UPDATE_CHAT_TITLE_USER = 72;

    public static final int CHANGE_CHAT_MUTE_STATUS = 13;

    public static final int BOT_HIDE_COMMAND_LIST_AND_CLEAN_EDIT = 20;
    public static final int BOT_HIDE_COMMAND_LIST = 21;
    public static final int BOT_SHOW_COMMAND_LIST = 22;
    public static final int BOT_CHANGE_ICON_VISIBILITY = 23;
    public static final int BOT_SHOW_START = 24;

    public static final int FORCE_CLOSE_CHAT = 30;
    public static final int FORCE_CLOSE_SHARED_ACTIVITY = 31;

    public static final int UPDATE_RECORD_VOICE_STATE = 40;

    public static final int UPDATE_MUSIC_PLAYER = 50;

    public static final int CLOSE_CONTACT_ACTIVITY = 60;

    public static final int UPDATE_SHARED_MEDIA_LIST = 80;
    public static final int UPDATE_JUST_SHARED_MEDIA_LIST = 82;
    public static final int UPDATE_SHARED_AUDIO_LIST = 81;

    public static final int UPDATE_SHARED_MEDIA_PAGE = 90;
    public static final int UPDATE_MUSIC_PHOTO_AND_TAG = 100;


    private int messageCode;
    private Object what;

    public NotificationObject(int messageCode, Object what) {
        this.messageCode = messageCode;
        this.what = what;
    }

    public int getMessageCode() {
        return this.messageCode;
    }

    public Object getWhat() {
        return this.what;
    }

}