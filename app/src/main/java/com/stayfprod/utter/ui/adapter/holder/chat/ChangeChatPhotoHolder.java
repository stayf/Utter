package com.stayfprod.utter.ui.adapter.holder.chat;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.chat.ChangeIconTitleMsg;
import com.stayfprod.utter.service.IconFactory;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

import org.drinkless.td.libcore.telegram.TdApi;

public class ChangeChatPhotoHolder extends AbstractHolder<ChangeIconTitleMsg> {
    public TextView msg;
    public ImageView imageView;

    public ChangeChatPhotoHolder(Context context) {
        super(new LinearLayout(context));

        LinearLayout layout = ((LinearLayout) itemView);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
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

        imageView = new ImageView(context);

        layout.addView(imageView);

        LinearLayout.LayoutParams imageViewLP = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        imageViewLP.width = Constant.DP_45;
        imageViewLP.height = Constant.DP_45;
        imageView.setPadding(Constant.DP_4, Constant.DP_4, Constant.DP_4, Constant.DP_4);
    }

    @Override
    public void setValues(ChangeIconTitleMsg record, int i, Context context) {
        msg.setText(record.sys_msg);
        itemView.setTag(i);

        TdApi.Photo photo = record.photo;

        if (photo.photos.length > 0) {
            TdApi.PhotoSize photoSize = photo.photos[0];

            if (FileUtils.isTDFileEmpty(photoSize.photo)) {
                //AndroidUtil.setImageDrawable(imageView, IconFactory.createEmptyIcon(IconFactory.Type.TITLE, record.tgMessage.id, ""));
                imageView.setImageBitmap(null);

                if (photoSize.photo.id > 0)
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHANGE_CHAT_TITLE_IMAGE,
                            photoSize.photo.id, i, record.tgMessage.id, photo, itemView, imageView, getItemViewTag());
            } else {
                AndroidUtil.setImageDrawable(imageView, IconFactory.createBitmapIconForImageView(IconFactory.Type.TITLE, photoSize.photo.path, itemView, imageView, getItemViewTag()));
            }
        } else {
            imageView.setImageBitmap(null);
        }
    }
}
