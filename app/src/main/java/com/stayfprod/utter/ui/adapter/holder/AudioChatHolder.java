package com.stayfprod.utter.ui.adapter.holder;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.AudioMsg;
import com.stayfprod.utter.ui.view.chat.AudioMsgView;
import com.stayfprod.utter.ui.view.chat.VoiceMsgView;

public class AudioChatHolder extends AbstractHolder<AudioMsg> {

    public AudioChatHolder(Context context) {
        super(new AudioMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(AudioMsg record, int i, Context context) {
        ((AudioMsgView) itemView).setValues(record, i, context, this);
    }
}