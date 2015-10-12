package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.chat.BotDescriptionMsg;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.util.AndroidUtil;

public class BotDescriptionHolder extends AbstractHolder<BotDescriptionMsg> {

    public TextView description;
    public TextView title;
    public RelativeLayout subLayout;

    public BotDescriptionHolder(Context context) {
        super(new RelativeLayout(context));

        RelativeLayout layout = ((RelativeLayout) itemView);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        subLayout = new RelativeLayout(context);
        layout.addView(subLayout);

        RelativeLayout.LayoutParams subLayoutLP = (RelativeLayout.LayoutParams) subLayout.getLayoutParams();
        subLayoutLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        subLayoutLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;

        //иконка
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.mipmap.ic_help);
        imageView.setId(AndroidUtil.generateViewId());
        subLayout.addView(imageView);

        RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        imageViewLP.topMargin = Constant.DP_7;
        imageViewLP.leftMargin = Constant.DP_18;
        imageViewLP.rightMargin = Constant.DP_18;
        //иконка end

        //заголовок
        title = new TextView(context);
        title.setId(AndroidUtil.generateViewId());
        title.setText(AndroidUtil.getResourceString(R.string.what_this_bot_can_do));
        title.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        title.setTextColor(0xFF333333);
        title.setTextSize(15);
        subLayout.addView(title);

        RelativeLayout.LayoutParams titleLP = (RelativeLayout.LayoutParams) title.getLayoutParams();
        titleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        titleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        titleLP.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
        //заголовок end

        //текст
        description = new TextView(context);
        description.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        description.setTextColor(0xFF333333);
        description.setTextSize(15);
        description.setLinkTextColor(0xFF569ACE);

        subLayout.addView(description);

        RelativeLayout.LayoutParams descriptionLP = (RelativeLayout.LayoutParams) description.getLayoutParams();
        descriptionLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        descriptionLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        descriptionLP.topMargin = Constant.DP_7;
        descriptionLP.bottomMargin = Constant.DP_7;
        descriptionLP.rightMargin = Constant.DP_18;
        descriptionLP.addRule(RelativeLayout.BELOW, title.getId());
        descriptionLP.addRule(RelativeLayout.ALIGN_LEFT, title.getId());
        //текст end
    }

    @Override
    public void setValues(BotDescriptionMsg record, int i, Context context) {
        description.setText(record.description);
        if (record.isFullScreen) {
            itemView.setMinimumHeight(ChatActivity.sWindowCurrentHeight - AndroidUtil.dp(160));
            ((RelativeLayout.LayoutParams) subLayout.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL);
        } else {
            itemView.setMinimumHeight(0);
            RelativeLayout.LayoutParams layoutParams = ((RelativeLayout.LayoutParams) subLayout.getLayoutParams());
            layoutParams.topMargin = Constant.DP_18;
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
        }
        itemView.setTag(i);
    }
}
