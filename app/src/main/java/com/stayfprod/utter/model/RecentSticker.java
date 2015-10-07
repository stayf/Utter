package com.stayfprod.utter.model;


import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Serializable;

public class RecentSticker implements Serializable {

    public TdApi.Sticker sticker;

    public RecentSticker() {

    }

    public RecentSticker(TdApi.Sticker sticker) {
        this.sticker = sticker;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(sticker.setId).hashCode();
        result = prime * result + sticker.sticker.id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RecentSticker other = (RecentSticker) obj;
        return sticker.setId == other.sticker.setId && sticker.sticker.id == other.sticker.sticker.id;
    }
}
