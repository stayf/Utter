package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.PhotoMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.PhotoMsgView;

public class PhotoChatHolder extends AbstractHolder<PhotoMsg> {

    public PhotoChatHolder(Context context) {
        super(new PhotoMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(PhotoMsg record, int i, Context context) {
        ((PhotoMsgView) itemView).setValues(record, i, context, this);
    }
}
