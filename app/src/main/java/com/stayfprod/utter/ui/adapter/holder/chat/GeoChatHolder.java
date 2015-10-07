package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.GeoMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.GeoMsgView;

public class GeoChatHolder extends AbstractHolder<GeoMsg> {

    public GeoChatHolder(Context context) {
        super(new GeoMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(GeoMsg record, int i, Context context) {
        ((GeoMsgView) itemView).setValues(record, i, context, this);
    }
}

