package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.model.chat.ContactMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.ContactMsgView;

public class ContactChatHolder extends AbstractHolder<ContactMsg> {
    public TextView text;

    public ContactChatHolder(Context context) {
        super(new ContactMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(ContactMsg record, int i, Context context) {
        ((ContactMsgView) itemView).setValues(record, i, context, this);
    }
}