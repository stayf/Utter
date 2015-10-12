package com.stayfprod.utter.ui.component;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class ProfileUserListDialog {
    private static final String LOG = ProfileUserListDialog.class.getSimpleName();

    private Contact mContact;

    private void showDialog(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String[] names = {AndroidUtil.getResourceString(R.string.remove_user)};

        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (mContact != null) {
                    try {
                        ChatInfo chatInfo = ProfileManager.getManager().getChatInfo();
                        ChatManager chatManager = ChatManager.getManager();
                        chatManager.deleteChatParticipant(chatInfo.tgChatObject.id, mContact.chatParticipant.user.id, new ResultController() {
                            @Override
                            public void afterResult(TdApi.TLObject object, int calledConstructor) {

                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG, "onClick", e);
                        Crashlytics.logException(e);
                    }
                }
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    }

    public boolean processLongClick(Context context, Contact contact) {
        try {
            ChatInfo chatInfo = ProfileManager.getManager().getChatInfo();
            UserManager userManager = UserManager.getManager();

            if (contact.chatParticipant.user.id == userManager.getCurrUserId()) {
                return false;
            }

            if (contact.chatParticipant.inviterId == userManager.getCurrUserId() || chatInfo.groupChatFull.adminId == userManager.getCurrUserId()) {
                showDialog(context);
                this.mContact = contact;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(LOG, "processLongClick", e);
            Crashlytics.logException(e);
            return false;
        }
    }
}
