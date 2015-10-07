package com.stayfprod.utter.model;


import org.drinkless.td.libcore.telegram.TdApi;

public class SharedMedia {
    public boolean isVideo;
    public String videoTime;

    public boolean isDate;
    public String date;

    public TdApi.Message message;

    public int thumbIndex;
    public int photoIndex;

    public boolean isSelected;

    public int pos;

    public int totalCount;

    public boolean isDeleted;

    public SharedMedia(boolean isVideo, String videoTime, boolean isDate, String date, TdApi.Message message) {
        this.isVideo = isVideo;
        this.videoTime = videoTime;
        this.isDate = isDate;
        this.date = date;
        this.message = message;
    }

    public SharedMedia(boolean isDate, String date) {
        this.isDate = isDate;
        this.date = date;
    }

    public SharedMedia() {

    }

}
