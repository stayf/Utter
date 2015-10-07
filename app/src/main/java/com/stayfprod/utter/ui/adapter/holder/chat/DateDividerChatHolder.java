package com.stayfprod.utter.ui.adapter.holder.chat;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.model.chat.DateDivider;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.util.AndroidUtil;

public class DateDividerChatHolder extends AbstractHolder<DateDivider> {

    public TextView divider;

    public DateDividerChatHolder(Context context) {
        super(new LinearLayout(context));
        LinearLayout layout = ((LinearLayout) itemView);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        divider = new TextView(context);
        divider.setGravity(Gravity.CENTER);
        divider.setTypeface(AndroidUtil.TF_ROBOTO_BOLD);
        divider.setTextColor(0xFF333333);
        divider.setTextSize(15);

        layout.addView(divider);

        LinearLayout.LayoutParams dividerLP = (LinearLayout.LayoutParams) divider.getLayoutParams();
        dividerLP.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        dividerLP.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        dividerLP.gravity = Gravity.CENTER;
        dividerLP.topMargin = Constant.DP_7;
        dividerLP.bottomMargin = Constant.DP_7;
    }

    @Override
    public void setValues(DateDivider record, int i, Context context) {
        divider.setText(record.date_divider);
        itemView.setTag(i);
    }
}
