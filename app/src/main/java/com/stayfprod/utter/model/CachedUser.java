package com.stayfprod.utter.model;

import org.drinkless.td.libcore.telegram.TdApi;

public class CachedUser {
    public TdApi.User tgUser;
    public boolean isBlocked;
    public TdApi.BotInfo botInfo;
    public String initials;
    public String fullName;

    public boolean isHaveFullInfo;

    public CachedUser(TdApi.User tgUser, boolean isBlocked, TdApi.BotInfo botInfo, String initials, String fullName) {
        this.tgUser = tgUser;
        this.isBlocked = isBlocked;
        this.botInfo = botInfo;
        this.initials = initials;
        this.fullName = fullName;
    }

    public CachedUser(TdApi.User tgUser, String initials, String fullName) {
        this.tgUser = tgUser;
        this.initials = initials;
        this.fullName = fullName;
    }
}
