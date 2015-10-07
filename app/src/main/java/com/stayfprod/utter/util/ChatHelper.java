package com.stayfprod.utter.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.InputMsgIconType;
import com.stayfprod.utter.model.OutputMsgIconType;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.URLSpanNoUnderline;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatHelper {

    public final static int LOCAL_MSG_ID_MIN = 1000000000;
    private static final int SYSTEM_MSG_COLOR = Color.parseColor("#6b9cc2");

    public static OutputMsgIconType getTypeOfOutputMsgIcon(TdApi.Chat tgChat, int msgId) {
        //верхнее исходящее, не прочитанное
        try{
            if (msgId < LOCAL_MSG_ID_MIN && msgId > tgChat.lastReadOutboxMessageId && msgId > tgChat.lastReadInboxMessageId && tgChat.unreadCount <= 0) {
                return OutputMsgIconType.ACCEPT_NOT_RED;
            }
            if (msgId >= LOCAL_MSG_ID_MIN) {
                //иначе ставим часики
                return OutputMsgIconType.NOT_SEND;
            }
        }catch (Exception e){
            Crashlytics.logException(e);
        }

        return OutputMsgIconType.NONE;
    }

    public static boolean isChatMuted(ChatInfo chatInfo) {
        return chatInfo.tgChatObject.notificationSettings.muteFor > 0;
    }

    public static String getUserOnlyName(TdApi.User user) {
        if (TextUtil.isNotBlank(user.firstName)) {
            return user.firstName + ": ";
        }

        if (TextUtil.isNotBlank(user.lastName)) {
            return user.lastName + ": ";
        }
        return "";
    }

    public static InputMsgIconType getTypeOfInputMsgIcon(TdApi.Chat tgChat) {
        //входящее
        //todo не понятно если ошибка, то какой параметр это показывает!!
        if (tgChat.unreadCount > 0) {
            return InputMsgIconType.NEW_MSG;
        } else {
            return InputMsgIconType.NONE;
        }
    }

    public static boolean isUserOnline(TdApi.UserStatus userStatus, int userId) {
        Integer currUserId = UserManager.getManager().getCurrUserId();
        if (currUserId != null && userId == currUserId) {
            return true;
        }

        boolean returned = false;
        if (userStatus != null) {
            switch (userStatus.getConstructor()) {
                case TdApi.UserStatusOnline.CONSTRUCTOR:
                    returned = true;
                    break;
                case TdApi.UserStatusOffline.CONSTRUCTOR:
                    TdApi.UserStatusOffline userStatusOffline = (TdApi.UserStatusOffline) userStatus;
                    returned = DateUtil.isOnline(userStatusOffline.wasOnline);
                    break;
                case TdApi.UserStatusRecently.CONSTRUCTOR:
                    returned = true;
                    break;
            }
        }

        return returned;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public static Spannable getSpanSystemText(CharSequence text, int... color) {
        Spannable spanText = new SpannableString(text);
        spanText.setSpan(new ForegroundColorSpan(color.length == 0 ? SYSTEM_MSG_COLOR : color[0]), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanText;
    }

    public static Spannable getSpanSystemText(CharSequence prefix, CharSequence text) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (TextUtil.isNotBlank(prefix))
            spannableStringBuilder.append(getSpanSystemText(prefix));
        if (TextUtil.isNotBlank(text))
            spannableStringBuilder.append(getSpanSystemText(text));
        return spannableStringBuilder;
    }

    public static Spannable getSpanSystemText(CharSequence text1, CharSequence text2, CharSequence text3) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (TextUtil.isNotBlank(text1))
            spannableStringBuilder.append(getSpanSystemText(text1));
        if (TextUtil.isNotBlank(text2))
            spannableStringBuilder.append(getSpanSystemText(text2, Color.BLACK));
        if (TextUtil.isNotBlank(text3))
            spannableStringBuilder.append(getSpanSystemText(text3));
        return spannableStringBuilder;
    }

    public static void openFile(String path, String type, Context context) {
        try {
            Intent i = new Intent();
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(new File(path)), type);
            context.startActivity(i);
        } catch (Exception e) {
            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.can_not_open_file));
        }
    }

    public static String getFileName(String text) {
        if (text == null || text.length() == 0) {
            return AndroidUtil.getResourceString(R.string.no_file_name);
        }
        return text;
    }

    public static String getDurationString(int totalSecs, int max) {
        int maxHours = max / 3600;
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;

        String timeString;
        if (maxHours == 0)
            timeString = String.format("%02d:%02d", minutes, seconds);
        else
            timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public static String getDurationString(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;

        String timeString;
        if (hours == 0)
            timeString = String.format("%02d:%02d", minutes, seconds);
        else
            timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public static String getFileSize(long size) {
        return humanReadableByteCount(size, true);
    }

    public static String getFileDownloadingSize(long loaded, long fileSize) {
        return AndroidUtil.getResourceString(R.string.downloading) + humanReadableByteCount(loaded, true)
                + AndroidUtil.getResourceString(R.string.of) + humanReadableByteCount(fileSize, true);
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String lastSeenUser(TdApi.UserStatus userStatus) {
        String strStatus = "";
        switch (userStatus.getConstructor()) {
            case TdApi.UserStatusEmpty.CONSTRUCTOR:
                TdApi.UserStatusEmpty userStatusEmpty = (TdApi.UserStatusEmpty) userStatus;
                strStatus = "";
                break;
            case TdApi.UserStatusOnline.CONSTRUCTOR:
                TdApi.UserStatusOnline userStatusOnline = (TdApi.UserStatusOnline) userStatus;
                strStatus = AndroidUtil.getResourceString(R.string.online);
                //userStatusOnline.expires
                break;
            case TdApi.UserStatusOffline.CONSTRUCTOR:
                TdApi.UserStatusOffline userStatusOffline = (TdApi.UserStatusOffline) userStatus;
                strStatus = DateUtil.calculateLastSeen(userStatusOffline.wasOnline);
                break;
            case TdApi.UserStatusRecently.CONSTRUCTOR:
                TdApi.UserStatusRecently userStatusRecently = (TdApi.UserStatusRecently) userStatus;
                strStatus = AndroidUtil.getResourceString(R.string.online);
                break;
            case TdApi.UserStatusLastWeek.CONSTRUCTOR:
                TdApi.UserStatusLastWeek userStatusLastWeek = (TdApi.UserStatusLastWeek) userStatus;
                strStatus = AndroidUtil.getResourceString(R.string.last_seen_week_ago);
                break;
            case TdApi.UserStatusLastMonth.CONSTRUCTOR:
                TdApi.UserStatusLastMonth userStatusLastMonth = (TdApi.UserStatusLastMonth) userStatus;
                strStatus = AndroidUtil.getResourceString(R.string.last_seen_month_ago);
                break;
        }
        return strStatus;
    }

    public static Object[] calculateOnlineUsersInGroupChat(TdApi.GroupChatFull groupChatFull) {
        int count = 0;
        List<CachedUser> botPosList = new ArrayList<CachedUser>();
        for (int i = 0; i < groupChatFull.participants.length; i++) {
            TdApi.ChatParticipant participant = groupChatFull.participants[i];
            CachedUser cachedUser = UserManager.getManager().insertUserInCache(participant.user);
            if (ChatHelper.isUserOnline(participant.user.status, participant.user.id)) {
                count++;
            }

            if (cachedUser.tgUser.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR) {
                botPosList.add(cachedUser);
            }

        }
        return new Object[]{count, botPosList};
    }

    public static boolean addLinks(Spannable text, Pattern pattern, String scheme) {
        return addLinks(text, pattern, scheme, null, null);
    }

    public static boolean addLinks(Spannable s, Pattern p,
                                   String scheme, Linkify.MatchFilter matchFilter,
                                   Linkify.TransformFilter transformFilter) {
        boolean hasMatches = false;
        String prefix = (scheme == null) ? "" : scheme.toLowerCase(Locale.ROOT);
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (matchFilter != null) {
                allowed = matchFilter.acceptMatch(s, start, end);
            }

            if (allowed) {
                String url = makeUrl(m.group(0), new String[]{prefix},
                        m, transformFilter);

                applyLink(url, start, end, s);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static void applyLink(String url, int start, int end, Spannable text) {
        URLSpanNoUnderline span = new URLSpanNoUnderline(url);
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String makeUrl(String url, String[] prefixes,
                                  Matcher m, Linkify.TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(m, url);
        }

        boolean hasPrefix = false;

        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0,
                    prefixes[i].length())) {
                hasPrefix = true;

                // Fix capitalization if necessary
                if (!url.regionMatches(false, 0, prefixes[i], 0,
                        prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }

                break;
            }
        }

        if (!hasPrefix) {
            url = prefixes[0] + url;
        }

        return url;
    }
}
