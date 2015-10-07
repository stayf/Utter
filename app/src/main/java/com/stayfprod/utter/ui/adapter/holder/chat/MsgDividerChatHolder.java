package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.chat.MsgDivider;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.util.AndroidUtil;

public class MsgDividerChatHolder extends AbstractHolder<MsgDivider> {

    public TextView text;
    public ImageView image;

    public MsgDividerChatHolder(Context context) {
        super(new LinearLayout(context));

        LinearLayout layout = ((LinearLayout) itemView);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Constant.DP_28));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFFEBF2F7);

        text = new TextView(context);
        text.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        text.setTextColor(0xFF579bcf);
        text.setTextSize(14);

        layout.addView(text);

        LinearLayout.LayoutParams textLP = (LinearLayout.LayoutParams) text.getLayoutParams();
        textLP.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        textLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        image = new ImageView(context);
        image.setImageResource(R.mipmap.ic_small_arrow);
        layout.addView(image);

        LinearLayout.LayoutParams imageLP = (LinearLayout.LayoutParams) image.getLayoutParams();
        imageLP.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        imageLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        imageLP.setMargins(Constant.DP_5, 0, 0, 0);
    }

    @Override
    public void setValues(MsgDivider record, int i, Context context) {
        text.setText(record.msg_divider);
        itemView.setTag(i);
    }
}
