package com.stayfprod.utter.ui.adapter.holder;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.IntermediateManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.SharedMedia;
import com.stayfprod.utter.model.SharedMusic;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.ui.activity.IntermediateActivity;
import com.stayfprod.utter.ui.view.DialogView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class IntermediateHolder extends AbstractHolder<ChatInfo> {

    private static final String LOG = ChatListHolder.class.getSimpleName();

    public static volatile boolean isClickOnItemBlocked = false;

    public IntermediateHolder(final Context context, final List<ChatInfo> records) {
        super(new DialogView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                DialogView.LAYOUT_HEIGHT));

        itemView.setOnTouchListener(new View.OnTouchListener() {
            private Handler handlerBackground = new Handler();

            Runnable actionChangeBackground = new Runnable() {
                @Override
                public void run() {
                    itemView.setBackgroundColor(0x1A000000);
                }
            };

            private boolean needOpen;

            @Override
            public boolean onTouch(View v, MotionEvent evt) {

                switch (evt.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        handlerBackground.postDelayed(actionChangeBackground, 50);
                        needOpen = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);

                        //info блокировка нажатия везде!!!
                        if (needOpen && !isClickOnItemBlocked) {
                            isClickOnItemBlocked = true;
                            final ChatInfo chatInfo = records.get(getPosition());
                            if ((chatInfo.isGroupChat && chatInfo.groupChatFull == null)) {
                                IntermediateManager.getManager().getGroupChatFull((int) chatInfo.tgChatObject.id, new ResultController() {
                                    @Override
                                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                        final List<CachedUser> botUsers = IntermediateManager.getManager().processGroupChatFull(object, calledConstructor, chatInfo);
                                        if (chatInfo.groupChatFull == null) {
                                            isClickOnItemBlocked = false;
                                            AndroidUtil.showToastShort("try later...");
                                        } else {
                                            if (botUsers != null && !botUsers.isEmpty()) {
                                                BotManager.getManager().getBotGroupInfo(botUsers, chatInfo, new Runnable() {
                                                    @Override
                                                    public void run() {

                                                    }
                                                });
                                                openActivity(chatInfo, context);
                                            } else {
                                                openActivity(chatInfo, context);
                                            }
                                        }
                                    }
                                });
                                break;
                            } else if (chatInfo.isGroupChat) {
                                Object[] objects = ChatHelper.calculateOnlineUsersInGroupChat(chatInfo.groupChatFull);
                                chatInfo.groupMembersOnline = (int) objects[0];
                                final List<CachedUser> botUsers = (List<CachedUser>) objects[1];
                                if (botUsers != null && !botUsers.isEmpty()) {
                                    BotManager.getManager().getBotGroupInfo(botUsers, chatInfo, new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                    openActivity(chatInfo, context);
                                } else {
                                    openActivity(chatInfo, context);
                                }
                            } else if (chatInfo.isBot) {
                                if (BotManager.getManager().isEmptyBotInfo((int) chatInfo.tgChatObject.id)) {
                                    UserManager.getManager().getUserFull((int) chatInfo.tgChatObject.id, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            switch (object.getConstructor()) {
                                                case TdApi.UserFull.CONSTRUCTOR: {
                                                    TdApi.UserFull userFull = (TdApi.UserFull) object;
                                                    CachedUser cachedUser = UserManager.getManager().insertUserInCache(userFull);
                                                    BotManager.getManager().initBot(cachedUser, chatInfo);
                                                    openActivity(chatInfo, context);
                                                    break;
                                                }
                                                default: {
                                                    isClickOnItemBlocked = false;
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    BotManager.getManager().initBot(UserManager.getManager().getUserByIdWithRequestAsync(chatInfo.tgChatObject.id), chatInfo);
                                    openActivity(chatInfo, context);
                                }
                            } else {
                                openActivity(chatInfo, context);
                            }
                        }
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE: {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);
                        needOpen = false;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        //todo идея в том, если сменились координаты по x, то отменять клик и возвращать цвет(прозрачность)
                        break;
                    }
                }
                return true;
            }
        });
    }

    public void openActivity(final ChatInfo chatInfo, final Context context) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                Boolean isGroup = chatInfo.isGroupChat;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isGroup", isGroup);
                bundle.putBoolean("isMuted", ChatHelper.isChatMuted(chatInfo));

                if (chatInfo.tgChatObject.type.getConstructor() == TdApi.GroupChatInfo.CONSTRUCTOR) {
                    Boolean isLeave = ((TdApi.GroupChatInfo) chatInfo.tgChatObject.type).groupChat.left;
                    bundle.putBoolean("isLeave", isLeave);
                }

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtras(bundle);

                ChatManager.getManager().forceClose();
                ProfileManager.getManager().forceClose();

                ChatManager.getManager().getChat(0, chatInfo.tgChatObject.id, chatInfo, true, true);

                final IntermediateManager intermediateManager = IntermediateManager.getManager();
                IntermediateActivity.Action action = IntermediateManager.getManager().action;
                if (action != null) {
                    final Long chatId = chatInfo.tgChatObject.id;
                    switch (action) {
                        case ADD_BOT:
                            final Integer botId = IntermediateManager.getManager().botId;
                            ChatManager.getManager().setAfterInitChatAction(new Runnable() {
                                @Override
                                public void run() {
                                    BotManager botManager = BotManager.getManager();
                                    botManager.setChatId(chatId);
                                    botManager.setIsGroup(true);
                                    botManager.sendBotStartMessage(chatId, botId);
                                }
                            });
                            break;
                        case FORWARD_MSGES:
                            SharedMediaManager.getManager().forceClose();
                            ChatManager.getManager().setAfterInitChatAction(new Runnable() {
                                @Override
                                public void run() {
                                    ThreadService.runTaskBackground(new Runnable() {
                                        @Override
                                        public void run() {
                                            SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                            ChatManager chatManager = ChatManager.getManager();
                                            int msgId[];
                                            long fromChatId = 0;
                                            if (sharedMediaManager.getSelectedMediaList().size() > 0) {
                                                SparseArray<SharedMedia> sharedMediaSparseArray = sharedMediaManager.getSelectedMediaList();
                                                msgId = new int[sharedMediaSparseArray.size()];
                                                for (int i = 0; i < sharedMediaSparseArray.size(); i++) {
                                                    int key = sharedMediaSparseArray.keyAt(i);
                                                    SharedMedia obj = sharedMediaSparseArray.get(key);
                                                    msgId[i] = obj.message.id;
                                                    if (i == 0) {
                                                        fromChatId = obj.message.chatId;
                                                    }
                                                    //chatManager.sendMessage(chatManager.createForwardMsg(obj.message.id));
                                                }
                                                chatManager.forwardMessages(msgId, fromChatId);
                                                sharedMediaSparseArray.clear();
                                            } else if (sharedMediaManager.getSelectedMusicList().size() > 0) {
                                                SparseArray<SharedMusic> sharedMusicSparseArray = sharedMediaManager.getSelectedMusicList();
                                                msgId = new int[sharedMusicSparseArray.size()];
                                                for (int i = 0; i < sharedMusicSparseArray.size(); i++) {
                                                    int key = sharedMusicSparseArray.keyAt(i);
                                                    SharedMusic obj = sharedMusicSparseArray.get(key);
                                                    msgId[i] = obj.message.id;
                                                    if (i == 0) {
                                                        fromChatId = obj.message.chatId;
                                                    }
                                                    //chatManager.sendMessage(chatManager.createForwardMsg(obj.message.id));
                                                }

                                                chatManager.forwardMessages(msgId, fromChatId);
                                                sharedMusicSparseArray.clear();
                                            }
                                        }
                                    });
                                }
                            });
                            break;
                        case SHARED_CONTACT:
                            final Integer userId = intermediateManager.userId;
                            final ChatManager chatManager = ChatManager.getManager();
                            chatManager.setAfterInitChatAction(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        CachedUser cachedUser = UserManager.getManager().getUserByIdWithRequestAsync(userId);
                                        chatManager.sendMessage(chatManager.createContactMsg(cachedUser.tgUser.firstName,
                                                cachedUser.tgUser.lastName, cachedUser.tgUser.phoneNumber, cachedUser.tgUser.id));
                                    } catch (Exception e) {
                                        Log.e(LOG, "SHARED_CONTACT", e);
                                        Crashlytics.logException(e);
                                    }
                                }
                            });
                            break;
                    }
                }

                context.startActivity(intent);
                ((IntermediateActivity) context).forceFinish();
                ((IntermediateActivity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        isClickOnItemBlocked = false;
                    }
                }, 200);
            }
        });
    }

    @Override
    public void setValues(ChatInfo record, int i, final Context context) {
        ((DialogView) itemView).setValues(record, i, context);
    }
}