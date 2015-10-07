package com.stayfprod.utter.model;

import android.text.Spannable;
import org.drinkless.td.libcore.telegram.TdApi;

public class ChatInfo {

    public TdApi.Chat tgChatObject;

    public Spannable chatName;
    public Spannable text;
    public String date;
    public InputMsgIconType inputMsgIcon;
    public OutputMsgIconType outputMsgIcon;
    public String initials;

    public boolean isGroupChat;
    public boolean isBot;
    public TdApi.GroupChatFull groupChatFull;
    public int groupMembersOnline;

    public DialogDrawParams drawParams = new DialogDrawParams();

    public int currentPosInList;
}
