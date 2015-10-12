package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.stayfprod.utter.model.AbstractChatMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.adapter.holder.AudioChatHolder;
import com.stayfprod.utter.ui.adapter.holder.LoadingHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.ChangeChatPhotoHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.ContactChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.VoiceChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.BotDescriptionHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.DateDividerChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.DocumentChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.GeoChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.MsgDividerChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.PhotoChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.StickerChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.SystemMsgChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.TextChatHolder;
import com.stayfprod.utter.ui.adapter.holder.chat.VideoChatHolder;

import java.util.List;

public class ChatAdapter extends AbstractLoadingAdapter<AbstractHolder> {

    private List<AbstractChatMsg> mRecords;
    private Context mContext;

    public ChatAdapter(List<AbstractChatMsg> records, Context context) {
        this.mRecords = records;
        this.mContext = context;
    }

    @Override
    public AbstractHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return new LoadingHolder(mContext);
    }

    @Override
    public void onBindFooterView(AbstractHolder holder, int position) {

    }

    @SuppressWarnings("ALL")
    @Override
    public AbstractHolder onCreateBasicItemViewHolder(ViewGroup parent, int viewType) {
        AbstractChatMsg.Type type = AbstractChatMsg.Type.fromInteger(viewType);
        Context context = parent.getContext();
        switch (type) {
            case SYSTEM_MSG:
                return new SystemMsgChatHolder(context);
            case SYSTEM_CHANGE_TITLE_MSG:
                return new ChangeChatPhotoHolder(context);

            case DATE_DIVIDER:
                return new DateDividerChatHolder(context);
            case NEW_MSG_DIVIDER:
                return new MsgDividerChatHolder(context);

            case MSG_TEXT:
                return new TextChatHolder(context);
            case MSG_VOICE:
                return new VoiceChatHolder(context);
            case MSG_AUDIO:
                return new AudioChatHolder(context);
            case MSG_DOCUMENT:
                return new DocumentChatHolder(context);
            case MSG_GEO:
                return new GeoChatHolder(context);
            case MSG_STICKER:
                return new StickerChatHolder(context);
            case MSG_PHOTO:
                return new PhotoChatHolder(context);
            case MSG_VIDEO:
                return new VideoChatHolder(context);
            case MSG_CONTACT:
                return new ContactChatHolder(context);

            case MSG_FORWARD_TEXT:
                return new TextChatHolder(context);
            case MSG_FORWARD_VOICE:
                return new VoiceChatHolder(context);
            case MSG_FORWARD_AUDIO:
                return new AudioChatHolder(context);
            case MSG_FORWARD_DOCUMENT:
                return new DocumentChatHolder(context);
            case MSG_FORWARD_GEO:
                return new GeoChatHolder(context);
            case MSG_FORWARD_STICKER:
                return new StickerChatHolder(context);
            case MSG_FORWARD_PHOTO:
                return new PhotoChatHolder(context);
            case MSG_FORWARD_VIDEO:
                return new VideoChatHolder(context);
            case MSG_CONTACT_FORWARD:
                return new ContactChatHolder(context);

            case MSG_BOT_DESCRIPTION:
                return new BotDescriptionHolder(context);
            default: {
                return new TextChatHolder(context);
            }
        }
    }

    @SuppressWarnings("ALL")
    @Override
    public void onBindBasicItemView(AbstractHolder holder, int position) {
        holder.setValues(mRecords.get(position), position, mContext);
    }

    @Override
    public int getBasicItemCount() {
        return mRecords.size();
    }

    @Override
    public int getBasicItemType(int position) {
        return mRecords.get(position).type.getValue();
    }

}

