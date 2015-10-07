package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.StickerMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.StickerMsgView;

public class StickerChatHolder extends AbstractHolder<StickerMsg> {

    public StickerChatHolder(Context context) {
        super(new StickerMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(StickerMsg record, int i, Context context) {
        ((StickerMsgView) itemView).setValues(record, i, context,this);
    }
}
