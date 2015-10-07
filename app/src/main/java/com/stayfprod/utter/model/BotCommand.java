package com.stayfprod.utter.model;

import org.drinkless.td.libcore.telegram.TdApi;

public class BotCommand {
    public CachedUser cachedUser;
    public TdApi.BotCommand tgBotCommand;

    public BotCommand(CachedUser cachedUser, TdApi.BotCommand tgBotCommand) {
        this.cachedUser = cachedUser;
        this.tgBotCommand = tgBotCommand;
    }
}
