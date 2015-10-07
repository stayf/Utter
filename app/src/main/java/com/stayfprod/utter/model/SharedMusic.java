package com.stayfprod.utter.model;


import org.drinkless.td.libcore.telegram.TdApi;

public class SharedMusic {
    public String performer;
    public String name;

    public String date;
    public boolean isDivider;

    public TdApi.Message message;

    public boolean isSelected;

    public int pos;
    public boolean isDeleted;

    public SharedMusic(String performer, String name, String date, boolean isDivider) {
        this.performer = performer;
        this.name = name;
        this.date = date;
        this.isDivider = isDivider;
    }

    public SharedMusic(String date, boolean isDivider) {
        this.isDivider = isDivider;
        this.date = date;
    }

    public SharedMusic() {

    }
}
