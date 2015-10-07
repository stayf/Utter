package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.VoiceMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.VoiceMsgView;

public class VoiceChatHolder extends AbstractHolder<VoiceMsg> {

    public VoiceChatHolder(Context context) {
        super(new VoiceMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(VoiceMsg record, int i, Context context) {
        ((VoiceMsgView) itemView).setValues(record, i, context, this);
    }
}
