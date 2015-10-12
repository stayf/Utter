package com.stayfprod.utter.manager;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.PushNotification;
import com.stayfprod.utter.ui.activity.ChatListActivity;
import com.stayfprod.utter.util.TextUtil;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager extends ResultController {
    public static final int NOTIFICATION_SETTINGS_FOR_ALL_CHATS = 1;
    public static final int NOTIFICATION_SETTINGS_FOR_CHAT = 2;
    public static final int NOTIFICATION_SETTINGS_FOR_GROUP_CHATS = 3;
    public static final int NOTIFICATION_SETTINGS_FOR_PRIVATE_CHATS = 4;

    public static final int UNMUTE = 0;
    public static final int MUTE_FOR_HOUR = 60 * 60;
    public static final int MUTE_FOR_8_HOURS = 8 * MUTE_FOR_HOUR;
    public static final int MUTE_FOR_2_DAYS = 2 * 3 * MUTE_FOR_8_HOURS;
    public static final int MUTE_DISABLE = Integer.MAX_VALUE;

    private static final String LOG = "NotificationManager";
    private static final String GROUP_KEY = "GROUP_KEY";
    private static final int NOTIFICATION_ID = 7788911;

    private static volatile NotificationManager sManager;
    private static BitmapDrawable sBigIcon;

    public static NotificationManager getManager() {
        if (sManager == null) {
            synchronized (NotificationManager.class) {
                if (sManager == null) {
                    sManager = new NotificationManager();
                }
            }
        }
        return sManager;
    }

    private List<PushNotification> sPushNotifications = new ArrayList<PushNotification>();
    private LongSparseArray mChatIdList = new LongSparseArray();

    @Override
    public boolean hasChanged() {
        return true;
    }

    public void setMuteForChat(final int muteFor, final Long chatId) {
        if (chatId != null) {
            TdApi.SetNotificationSettings setNotificationSettings = new TdApi.SetNotificationSettings();
            setNotificationSettings.notificationSettings = new TdApi.NotificationSettings();
            setNotificationSettings.notificationSettings.muteFor = muteFor;
            setNotificationSettings.scope = new TdApi.NotificationSettingsForChat(chatId);
            //info не отрабатывает в блоке TdApi.Ok
            notifyObservers(new NotificationObject(NotificationObject.CHANGE_CHAT_MUTE_STATUS, new Object[]{chatId, muteFor}));
            client().send(setNotificationSettings, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                    switch (object.getConstructor()) {
                        case TdApi.Ok.CONSTRUCTOR:
                            if (calledConstructor == TdApi.SetNotificationSettings.CONSTRUCTOR) {
                                ChatListManager.getManager().updateMuteForChat(chatId, muteFor);
                            }
                            break;
                    }
                }
            });
        }
    }

    public void getSetting(int type, int... chatId) {
        TdApi.GetNotificationSettings getNotificationSettings = new TdApi.GetNotificationSettings();
        switch (type) {
            case NOTIFICATION_SETTINGS_FOR_ALL_CHATS:
                getNotificationSettings.scope = new TdApi.NotificationSettingsForAllChats();
                break;
            case NOTIFICATION_SETTINGS_FOR_CHAT:
                getNotificationSettings.scope = new TdApi.NotificationSettingsForChat(chatId[0]);
                break;
            case NOTIFICATION_SETTINGS_FOR_GROUP_CHATS:
                getNotificationSettings.scope = new TdApi.NotificationSettingsForGroupChats();
                break;
            case NOTIFICATION_SETTINGS_FOR_PRIVATE_CHATS:
                getNotificationSettings.scope = new TdApi.NotificationSettingsForPrivateChats();
                break;
        }
        client().send(getNotificationSettings, getManager());
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

    }

    public void cleanStorage() {
        sPushNotifications.clear();
        mChatIdList.clear();
    }

    public void cancelNotification(Context context) {
        if (sPushNotifications.size() > 0) {
            android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
            cleanStorage();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification(Context context, PushNotification notification) {
        sPushNotifications.add(0, notification);
        if (mChatIdList.get(notification.chatId) == null) {
            mChatIdList.put(notification.chatId, notification.chatId);
        }

        try {
            android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //todo сделать переходы в чаты
            Intent notificationIntent = new Intent(context, ChatListActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

            mBuilder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentTitle(AndroidUtil.getApplicationName(context));
            if (TextUtil.isNotBlank(notification.who) && !notification.isGroup) {
                mBuilder.setContentText(notification.who + ": " + notification.msg);
            } else {
                mBuilder.setContentText(notification.msg);
            }

            if (App.CURRENT_VERSION_SDK >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.mipmap.ic_logo);
                mBuilder.setColor(0xFF5B95C2);
            } else {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            }

            if (sBigIcon != null && sBigIcon.getBitmap() != null) {
                mBuilder.setLargeIcon(sBigIcon.getBitmap());
            }

            if (TextUtil.isNotBlank(notification.who)) {
                mBuilder.setTicker(notification.who + ": " + notification.msg);
            } else {
                mBuilder.setTicker(notification.msg);
            }

            mBuilder.setContentInfo(String.valueOf(sPushNotifications.size()));
            if (App.CURRENT_VERSION_SDK >= 16) {
                mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            }

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            if (sPushNotifications.size() > 1) {
                inboxStyle.setSummaryText(sPushNotifications.size() + " new messages from " + mChatIdList.size() + " chats");
            } else {
                inboxStyle.setSummaryText(sPushNotifications.size() + " new message");
            }

            mBuilder.setStyle(inboxStyle);

            mBuilder.setGroup(GROUP_KEY);
            mBuilder.setGroupSummary(true);
            mBuilder.setNumber(sPushNotifications.size());

            for (int i = 0; i < sPushNotifications.size(); i++) {
                PushNotification pushNotification = sPushNotifications.get(i);
                if (TextUtil.isNotBlank(pushNotification.who) && !pushNotification.isGroup) {
                    inboxStyle.addLine(pushNotification.who + ": " + pushNotification.msg);
                } else {
                    inboxStyle.addLine(pushNotification.msg);
                }
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            Log.e(LOG, "sendNotification", e);
            Crashlytics.logException(e);
        }
    }


}
