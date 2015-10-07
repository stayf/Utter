package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.DocumentMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.DocumentMsgView;

public class DocumentChatHolder extends AbstractHolder<DocumentMsg> {

    public DocumentChatHolder(Context context) {
        super(new DocumentMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(DocumentMsg record, int i, final Context context) {
        ((DocumentMsgView) itemView).setValues(record, i, context, this);
    }
}
