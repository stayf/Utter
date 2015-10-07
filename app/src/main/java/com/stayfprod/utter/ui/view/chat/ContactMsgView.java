package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.chat.ContactMsg;
import com.stayfprod.utter.service.IconFactory;
import com.stayfprod.utter.service.StaticLayoutFactory;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.activity.ProfileActivity;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.Logs;

import org.drinkless.td.libcore.telegram.TdApi;

public class ContactMsgView extends AbstractMsgView<ContactMsg> {


    private IconDrawable userIconDrawable;

    public ContactMsgView(Context context) {
        super(context);
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        //открываем профиль
        /*
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGroup", isGroup);
        bundle.putBoolean("isMuted", isMuted);

        ProfileManager.getManager().setChatInfo(ChatManager.getCurrentChatInfo());

        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtras(bundle);

        getContext().startActivity(intent);
        ((AbstractActivity) getContext()).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);*/
        return false;
    }

    @Override
    public void setValues(final ContactMsg record, int i, Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);
        TdApi.MessageContact messageContact = (TdApi.MessageContact) record.tgMessage.message;

        if (UserManager.isEmptyUser(record.contact)) {
            userIconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT, record.contact.tgUser.id, record.contact.initials);

            final UserManager userManager = UserManager.getManager();
            final String tag = getItemViewTag();
            final int intTag = getItemViewIntTag();
            final int msgId = record.tgMessage.id;
            userManager.getUser(record.contact.tgUser.id, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                    switch (object.getConstructor()) {
                        case TdApi.User.CONSTRUCTOR: {
                            TdApi.User user = (TdApi.User) object;
                            final CachedUser cachedUser = userManager.insertUserInCache(user);
                            if (AndroidUtil.isItemViewVisible(ContactMsgView.this, tag)) {
                                if (FileUtils.isTDFileEmpty(cachedUser.tgUser.profilePhoto.small)) {
                                    if (cachedUser.tgUser.profilePhoto.small.id > 0)
                                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CONTACT_ICON,
                                                cachedUser.tgUser.profilePhoto.small.id, intTag, msgId, cachedUser.tgUser, ContactMsgView.this, tag);
                                } else {
                                    userIconDrawable = IconFactory.createBitmapIconForContact(IconFactory.Type.CHAT, cachedUser.tgUser.profilePhoto.small.path, ContactMsgView.this, tag);
                                }
                            }
                            break;
                        }
                    }
                }
            });
        } else {
            if (FileUtils.isTDFileEmpty(record.contact.tgUser.profilePhoto.small)) {
                if (record.contact.tgUser.profilePhoto.small.id > 0)
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CONTACT_ICON,
                            record.contact.tgUser.profilePhoto.small.id, i, record.tgMessage.id, record.contact.tgUser, ContactMsgView.this, getItemViewTag(), false);
                else {
                    userIconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT, record.contact.tgUser.id, record.contact.initials);
                }
            } else {
                userIconDrawable = IconFactory.createBitmapIconForContact(IconFactory.Type.CHAT, record.contact.tgUser.profilePhoto.small.path, ContactMsgView.this, getItemViewTag());
            }
        }
        invalidate();
    }

    public static void measureByOrientation(ContactMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            int subContentWidth = windowWidth - getSubContainerMarginLeft(record) - SUB_CONTAINER_MARGIN_RIGHT - SUB_CONTAINER_MARGIN_LEFT;
            record.staticTextLayout[i] = StaticLayoutFactory.createSimpleLayout(record.text, TextMsgView.TEXT_PAINT, subContentWidth, null);

            record.staticTextLayoutStartY[i] = (record.staticTextLayout[i].getLineAscent(0) - TextMsgView.TEXT_PAINT.ascent())
                    + (record.staticTextLayout[i].getLineDescent(0) == 0 ? TextMsgView.TEXT_PAINT.descent() : 0) + Constant.DP_4;

            int bidderHeight;
            if (record.staticTextLayout[i].getHeight() + Constant.DP_4 > IconFactory.Type.CHAT.getHeight()) {
                bidderHeight = (int) (record.staticTextLayout[i].getHeight() + getSubContainerMarginTop(record)) + Constant.DP_4;
            } else {
                bidderHeight = (int) (IconFactory.Type.CHAT.getHeight() + Constant.DP_6 + getSubContainerMarginTop(record));
            }

            measureLayoutHeight(bidderHeight, i, record);
            if (i == 0)
                measureByOrientation(record, Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, record.layoutHeight[getOrientatedIndex()]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = getOrientatedIndex();
        canvas.save();

        if (userIconDrawable != null) {
            canvas.translate(0, Constant.DP_1);
            userIconDrawable.draw(canvas);
        }

        canvas.restore();

        if (record.staticTextLayout[i] != null) {
            float startY = record.staticTextLayoutStartY[i];
            canvas.translate(SUB_CONTAINER_MARGIN_LEFT, startY);
            record.staticTextLayout[i].draw(canvas);
        }
    }

    public static void measure(ContactMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageContact messageContact = (TdApi.MessageContact) message;
        chatMessage.text = new SpannableString(messageContact.firstName + " " + messageContact.lastName + "\n\r +" + messageContact.phoneNumber);
        UserManager userManager = UserManager.getManager();
        chatMessage.contact = userManager.getUserByIdNoRequest(messageContact.userId, messageContact);
        //пересланный контакт может быть у юзера под другим именем
        //Logs.e("xxx=" + chatMessage.contact.initials      + " " + chatMessage.contact.fullName + " " + chatMessage.contact.tgUser.id + " " + UserManager.getManager().getCurrUserId());

        measureByOrientation(chatMessage);
    }

    public void setUserIconDrawableAndUpdateAsync(IconDrawable userIconDrawable) {
        this.userIconDrawable = userIconDrawable;
        postInvalidate();
    }
}
