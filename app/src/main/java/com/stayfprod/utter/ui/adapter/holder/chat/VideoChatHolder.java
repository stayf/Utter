package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.model.chat.VideoMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.view.chat.VideoMsgView;

public class VideoChatHolder extends AbstractHolder<VideoMsg> {

    public VideoChatHolder(Context context) {
        super(new VideoMsgView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setValues(VideoMsg record, int i, final Context context) {
        ((VideoMsgView) itemView).setValues(record, i, context,this);
    }
}
