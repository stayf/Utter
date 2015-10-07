package com.stayfprod.utter.manager;


import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.NotificationObject;

import org.drinkless.td.libcore.telegram.TdApi;

public class ProfileManager extends ResultController {

    private static volatile ProfileManager profileManager;

    public static final int ADD_MEMBER_TYPE = 1;
    public static final int ADD_SHARED_MEDIA_TYPE = 2;
    public static final int ADD_USER_LIST_TYPE = 3;
    public static final int ADD_GROUP_TYPE = 4;
    public static final int INFO_NAME_TYPE = 5;
    public static final int INFO_ADDITION_TYPE = 6;
    public static final int ADD_USER_TOP_LIST_TYPE = 7;

    public enum FOR {
        USER,
        GROUP,
        BOT
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    private ChatInfo chatInfo;
    private ChatInfo oldChatInfo;

    public void revertOldChatInfo() {
        chatInfo = oldChatInfo;
        oldChatInfo = null;
    }

    public ChatInfo getOldChatInfo() {
        return oldChatInfo;
    }

    public void setOldChatInfo(ChatInfo oldChatInfo) {
        this.oldChatInfo = oldChatInfo;
    }

    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    public boolean isSameChatId(long chatId) {
        return chatInfo != null && chatInfo.tgChatObject != null && chatInfo.tgChatObject.id == chatId;
    }

    public boolean isSameOldChatId(long chatId) {
        return oldChatInfo != null && oldChatInfo.tgChatObject != null && oldChatInfo.tgChatObject.id == chatId;
    }

    public void forceClose() {
        notifyObservers(new NotificationObject(NotificationObject.FORCE_CLOSE_CHAT, null));
    }

    public void setChatInfo(ChatInfo chatInfo) {
        this.chatInfo = chatInfo;
    }

    public void clean() {
        chatInfo = null;
        oldChatInfo = null;
    }

    public static ProfileManager getManager() {
        if (profileManager == null) {
            synchronized (ProfileManager.class) {
                if (profileManager == null) {
                    profileManager = new ProfileManager();
                }
            }
        }
        return profileManager;
    }

    public void getChat(long chatId, ResultController resultController) {
        TdApi.GetChat getChat = new TdApi.GetChat();
        getChat.chatId = chatId;
        client().send(getChat, resultController);
    }

    public static int getItemType(int position, FOR mFor) {
        switch (mFor) {
            case BOT: {
                switch (position) {
                    case 0:
                        return INFO_NAME_TYPE;
                    case 1:
                        return INFO_ADDITION_TYPE;
                    case 2:
                        return ADD_GROUP_TYPE;
                    case 3:
                        return ADD_SHARED_MEDIA_TYPE;
                }
                break;
            }
            case GROUP: {
                switch (position) {
                    case 0:
                        return ADD_MEMBER_TYPE;
                    case 1:
                        return ADD_SHARED_MEDIA_TYPE;
                    case 2:
                        return ADD_USER_TOP_LIST_TYPE;
                    default:
                        return ADD_USER_LIST_TYPE;
                }
            }
            case USER:
            default: {
                switch (position) {
                    case 0:
                        return INFO_NAME_TYPE;
                    case 1:
                        return INFO_ADDITION_TYPE;
                    case 2:
                        return ADD_SHARED_MEDIA_TYPE;
                }
            }
        }
        return 0;
    }


    public void addUserToGroup(CachedUser cachedUser, ResultController resultController) {
        //обращение к апи, после ответа back на активити

        if (chatInfo != null) {
            TdApi.AddChatParticipant addChatParticipant = new TdApi.AddChatParticipant();
            addChatParticipant.forwardLimit = 50;
            addChatParticipant.chatId = chatInfo.tgChatObject.id;
            addChatParticipant.userId = cachedUser.tgUser.id;
            client().send(addChatParticipant, resultController);
        }
    }


    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

    }
}
