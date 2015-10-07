package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.chat.SystemMsg;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.util.AndroidUtil;

public class SystemMsgChatHolder extends AbstractHolder<SystemMsg> {

    public TextView msg;

    public SystemMsgChatHolder(Context context) {
        super(new LinearLayout(context));

        LinearLayout layout = ((LinearLayout) itemView);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(Constant.DP_12, 0, Constant.DP_12, 0);

        msg = new TextView(context);
        msg.setId(R.id.i_chat_sys_msg_1);
        msg.setTypeface(AndroidUtil.TF_ROBOTO_BOLD);
        msg.setTextColor(0xFF569ace);
        msg.setTextSize(15);

        layout.addView(msg);

        LinearLayout.LayoutParams msg1LP = (LinearLayout.LayoutParams) msg.getLayoutParams();
        msg1LP.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        msg1LP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        msg1LP.topMargin = Constant.DP_7;
        msg1LP.bottomMargin = Constant.DP_7;
    }

    @Override
    public void setValues(SystemMsg record, int i, Context context) {
        msg.setText(record.sys_msg);
        itemView.setTag(i);
    }
}
