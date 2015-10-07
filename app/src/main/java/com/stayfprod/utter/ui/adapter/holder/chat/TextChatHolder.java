package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.model.chat.TextMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.TextMsgView;

public class TextChatHolder extends AbstractHolder<TextMsg> {
    public TextView text;

    public TextChatHolder(Context context) {
        super(new TextMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(TextMsg record, int i, Context context) {
        ((TextMsgView) itemView).setValues(record, i, context,this);
    }
}
