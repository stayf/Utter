package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ContactListManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.activity.ProfileActivity;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

import org.drinkless.td.libcore.telegram.TdApi;

public class ContactView extends AbstractChatView implements IconUpdatable {

    private static final BitmapDrawable GROUP_USERS_DRAWABLE;
    private static final int PROFILE_TRANSLATE_X = AndroidUtil.dp(68);
    private static final TextPaint TEXT_PAINT_BLUE;

    static {
        GROUP_USERS_DRAWABLE = FileUtils.decodeImageResource(R.mipmap.ic_groupusers);
        TEXT_PAINT_BLUE = new TextPaint(DialogView.TEXT_PAINT);
        TEXT_PAINT_BLUE.setColor(0xFF569ACE);
    }

    public Contact record;
    private IconDrawable iconDrawable;

    public static int LAYOUT_HEIGHT = DialogView.LAYOUT_HEIGHT;
    private static final int TEXT_MARGIN_LEFT = Constant.DP_10;
    private static final int TEXT_START_X = IconDrawable.CHAT_LIST_MARGIN_LEFT + IconFactory.Type.CHAT_LIST.getHeight() + TEXT_MARGIN_LEFT;

    private boolean isFirstItem;
    private boolean isInProfile;

    public void setIsInProfile(boolean isInProfile) {
        this.isInProfile = isInProfile;
    }

    public void setIsFirstItem(boolean isFirstItem) {
        this.isFirstItem = isFirstItem;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int i = getOrientatedIndex();

        if (isInProfile) {
            if (isFirstItem) {
                GROUP_USERS_DRAWABLE.setBounds(Constant.DP_18, Constant.DP_28,
                        Constant.DP_18 + GROUP_USERS_DRAWABLE.getIntrinsicWidth(),
                        Constant.DP_28 + GROUP_USERS_DRAWABLE.getIntrinsicHeight());
                GROUP_USERS_DRAWABLE.draw(canvas);
            }

            canvas.translate(PROFILE_TRANSLATE_X, 0);
        }


        if (iconDrawable != null)
            iconDrawable.draw(canvas);

        if (record != null) {
            canvas.drawText(record.drawTitle[i], 0, record.drawTitle[i].length(), TEXT_START_X, DialogView.TITLE_START_Y, DialogView.TITLE_PAINT);
            canvas.drawText(record.drawLastSeen[i], 0, record.drawLastSeen[i].length(), TEXT_START_X, DialogView.TEXT_START_Y_ILL + DialogView.TEXT_MARGIN_METRICS,
                    !record.isOnline ? DialogView.TEXT_PAINT : TEXT_PAINT_BLUE);

            if (!isInProfile) {
                canvas.translate(TEXT_START_X, 0);
                canvas.drawRect(0, LAYOUT_HEIGHT - Constant.DP_1, getMeasureWidth(i), LAYOUT_HEIGHT, DialogView.LINE_PAINT);
            }
        }
    }

    public static void profileMeasure(Contact record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i) - PROFILE_TRANSLATE_X;

            int maxTextWidth = windowWidth - TEXT_START_X;

            record.drawTitle[i] = TextUtils.ellipsize(record.cachedUser.fullName, DialogView.TITLE_PAINT, maxTextWidth, TextUtils.TruncateAt.END);
            record.drawLastSeen[i] = TextUtils.ellipsize(record.lastSeen, DialogView.TEXT_PAINT, maxTextWidth, TextUtils.TruncateAt.END);

            record.isOnline = record.drawLastSeen[i].equals(AndroidUtil.getResourceString(R.string.online));
            if (i == 0) {
                profileMeasure(record, Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    public static void measure(Contact record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);

            int maxTextWidth = windowWidth - TEXT_START_X;

            record.drawTitle[i] = TextUtils.ellipsize(record.cachedUser.fullName, DialogView.TITLE_PAINT, maxTextWidth, TextUtils.TruncateAt.END);
            record.drawLastSeen[i] = TextUtils.ellipsize(record.lastSeen, DialogView.TEXT_PAINT, maxTextWidth, TextUtils.TruncateAt.END);

            record.isOnline = record.drawLastSeen[i].equals(AndroidUtil.getResourceString(R.string.online));

            if (i == 0) {
                measure(record, Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    public ContactView(Context context, boolean isInProfile) {
        super(context);
        this.isInProfile = isInProfile;
    }

    private void openActivity(TdApi.TLObject object) {
        TdApi.Chat chat = (TdApi.Chat) object;
        //смысла нет кешировать это знакомы, которые уже в кеше
        //UserManager userManager = UserManager.getManager();
        //CachedUser cachedUser = userManager.insertUserInCache(((TdApi.PrivateChatInfo) chat.type).user);

        ChatInfo chatInfo = new ChatInfo();
        chatInfo.tgChatObject = chat;
        chatInfo.initials = record.cachedUser.initials;
        chatInfo.chatName = new SpannableString(record.cachedUser.fullName);

        ChatManager.getManager().getChat(0, chat.id, chatInfo, true, true);

        ContactListManager.getManager().clean();

        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isGroup", false);
                final Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
                ((AppCompatActivity) getContext()).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                ((AppCompatActivity) getContext()).supportFinishAfterTransition();
                isClickedInNonProfile = false;
            }
        });

    }

    private static volatile boolean isClickedInProfile;
    private static volatile boolean isClickedInNonProfile;

    public ContactView(Context context) {
        super(context);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInProfile) {
                    if (!isClickedInNonProfile) {
                        isClickedInNonProfile = true;
                        final ContactListManager contactListManager = ContactListManager.getManager();

                        if (contactListManager.getAction() == ContactListManager.ACTION_ADD_USER_TO_GROUP) {
                            ProfileManager profileManager = ProfileManager.getManager();

                            profileManager.addUserToGroup(record.cachedUser, new ResultController() {
                                @Override
                                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                    contactListManager.closeActivity();
                                }
                            });
                            isClickedInNonProfile = false;
                        } else {
                            isClickedInNonProfile = true;
                            final int userId = record.cachedUser.tgUser.id;
                            ContactListManager.getManager().getChatInfo(userId, new ResultController() {
                                @Override
                                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                    switch (object.getConstructor()) {
                                        case TdApi.Chat.CONSTRUCTOR: {
                                            openActivity(object);
                                            break;
                                        }
                                        case TdApi.Error.CONSTRUCTOR:
                                            TdApi.Error error = (TdApi.Error) object;
                                            String errorText = error.text.toLowerCase();
                                            if (errorText.contains("unknown chat id") || errorText.contains("chat not found")) {
                                                ChatManager.getManager().createPrivateChat(userId, new ResultController() {
                                                    @Override
                                                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                        switch (object.getConstructor()) {
                                                            case TdApi.Chat.CONSTRUCTOR: {
                                                                openActivity(object);
                                                                break;
                                                            }
                                                            default:
                                                                isClickedInNonProfile = false;
                                                                break;
                                                        }
                                                    }
                                                });
                                            }
                                        default:
                                            isClickedInNonProfile = false;
                                            break;
                                    }
                                }
                            });
                        }
                    }

                } else {

                    if (UserManager.getManager().getCurrUserId() == record.cachedUser.tgUser.id) {
                        return;
                    }

                    if (!isClickedInProfile) {
                        isClickedInProfile = true;
                        try {
                            UserManager userManager = UserManager.getManager();
                            final int userId = record.cachedUser.tgUser.id;
                            CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                            if (cachedUser.isHaveFullInfo) {
                                checkChatInfo(userId);
                            } else {
                                userManager.getUserFull(userId, new ResultController() {
                                    @Override
                                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                        switch (object.getConstructor()) {
                                            case TdApi.UserFull.CONSTRUCTOR: {
                                                TdApi.UserFull userFull = (TdApi.UserFull) object;
                                                CachedUser cachedUser = UserManager.getManager().insertUserInCache(userFull);
                                                checkChatInfo(userId);
                                                break;
                                            }
                                            default:
                                                isClickedInProfile = false;
                                                break;
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            isClickedInProfile = false;
                        }
                    }
                }
            }
        });
    }

    private void openProfileActivity(final int userId, TdApi.TLObject object) {
        TdApi.Chat chat = (TdApi.Chat) object;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGroup", false);
        bundle.putBoolean("isMuted", chat.notificationSettings.muteFor > 0);
        bundle.putBoolean("isSubProfile", true);
        //bundle.putBoolean("isFirstBotOpening",isFirstBotOpening);
        ProfileManager profileManager = ProfileManager.getManager();
        profileManager.setOldChatInfo(profileManager.getChatInfo());
        ChatInfo chatInfo = new ChatInfo();
        UserManager userManager = UserManager.getManager();
        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
        chatInfo.isBot = cachedUser.tgUser.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR;
        chatInfo.tgChatObject = chat;
        chatInfo.initials = cachedUser.initials;
        chatInfo.chatName = new SpannableString(cachedUser.fullName);
        profileManager.setChatInfo(chatInfo);
        final Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtras(bundle);

        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                getContext().startActivity(intent);
                ((AbstractActivity) getContext()).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                isClickedInProfile = false;
            }
        });
    }

    private void checkChatInfo(final int userId) {
        ContactListManager.getManager().getChatInfo(userId, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                switch (object.getConstructor()) {
                    case TdApi.Chat.CONSTRUCTOR: {
                        openProfileActivity(userId, object);
                        break;
                    }
                    case TdApi.Error.CONSTRUCTOR:
                        TdApi.Error error = (TdApi.Error) object;

                        String errorText = error.text.toLowerCase();
                        //Logs.e("errorText=" + errorText);
                        if (errorText.contains("unknown chat id") || errorText.contains("chat not found")) {
                            ChatManager.getManager().createPrivateChat(userId, new ResultController() {
                                @Override
                                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                    switch (object.getConstructor()) {
                                        case TdApi.Chat.CONSTRUCTOR: {
                                            openProfileActivity(userId, object);
                                            break;
                                        }
                                        default:
                                            isClickedInProfile = false;
                                            break;
                                    }

                                }
                            });
                        }
                    default:
                        isClickedInProfile = false;
                        break;
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setValues(Contact record, int i, Context context) {
        this.setTag(i);
        this.record = record;

        TdApi.User user = record.cachedUser.tgUser;
        TdApi.File file = user.profilePhoto.small;
        if (FileUtils.isTDFileEmpty(file)) {
            iconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT_LIST, user.id, record.cachedUser.initials);
            if (file.id > 0) {
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_LIST_ICON,
                        file.id, i, -1, user, this, getItemViewTag());
            }
        } else {
            iconDrawable = IconFactory.createBitmapIconForChat(IconFactory.Type.CHAT_LIST, file.path, this, getItemViewTag());
        }
        invalidate();
    }

    @Override
    public void setIconAsync(IconDrawable iconDrawable, boolean isForwardIcon, boolean... animated) {
        if (iconDrawable != null) {
            this.iconDrawable.emptyBitmap = iconDrawable.emptyBitmap;
            this.iconDrawable.mPaint = iconDrawable.mPaint;
            this.iconDrawable.text = iconDrawable.text;

            final Rect dirty = this.iconDrawable.getDirtyBounds();
            postInvalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
        }
    }
}
