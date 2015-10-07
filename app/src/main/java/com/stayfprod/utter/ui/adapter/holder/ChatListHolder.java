package com.stayfprod.utter.ui.adapter.holder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ChatListManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.ui.view.DialogView;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class ChatListHolder extends AbstractHolder<ChatInfo> {

    private static final String LOG = ChatListHolder.class.getSimpleName();

    public static volatile boolean isClickOnItemBlocked = false;

    public ChatListHolder(final Context context, final List<ChatInfo> records, final View.OnTouchListener itemTouchListener) {
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
                if (itemTouchListener != null) {
                    boolean defaultResult = itemTouchListener.onTouch(v, evt);

                    if (defaultResult) {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);
                        return true;
                    }
                }

                switch (evt.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        handlerBackground.postDelayed(actionChangeBackground, 50);
                        needOpen = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);

                        int adapterPos = getAdapterPosition();
                        if (adapterPos != -1) {
                            //info блокировка нажатия везде!!!
                            if (needOpen && !isClickOnItemBlocked) {
                                isClickOnItemBlocked = true;

                                //Logs.e("1");

                                final ChatInfo chatInfo = records.get(adapterPos);
                                if ((chatInfo.isGroupChat && chatInfo.groupChatFull == null)) {
                                    //isClickOnItemBlocked = true;
                                    //Timer.start();
                                    //Logs.e("2");

                                    ChatListManager.getManager().getGroupChatFull((int) chatInfo.tgChatObject.id, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            //Timer.show(this);
                                            final List<CachedUser> botUsers = ChatListManager.getManager().processGroupChatFull(object, calledConstructor);
                                            if (chatInfo.groupChatFull == null) {
                                                isClickOnItemBlocked = false;
                                                AndroidUtil.showToastShort("try later...");
                                            } else {
                                                if (botUsers != null && !botUsers.isEmpty()) {
                                                    BotManager.getManager().getBotGroupInfo(botUsers, chatInfo, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //openActivity(chatInfo, context);
                                                        }
                                                    });
                                                    //Logs.e("3");
                                                    openActivity(chatInfo, context);
                                                } else {
                                                    //isClickOnItemBlocked = false;
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
                                                //openActivity(chatInfo, context);
                                            }
                                        });
                                        //Logs.e("4");
                                        openActivity(chatInfo, context);
                                    } else {
                                        openActivity(chatInfo, context);
                                    }
                                } else if (chatInfo.isBot) {
                                    if (BotManager.getManager().isEmptyBotInfo((int) chatInfo.tgChatObject.id)) {
                                        //isClickOnItemBlocked = true;
                                        UserManager.getManager().getUserFull((int) chatInfo.tgChatObject.id, new ResultController() {
                                            @Override
                                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                //isClickOnItemBlocked = false;
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
                                            /*case TdApi.Error.CONSTRUCTOR:
                                                TdApi.Error error = (TdApi.Error) object;
                                                if (error.text.contains("unknown chat id")) {
                                                    ChatManager.getManager().createPrivateChat((int) chatInfo.tgChatObject.id, new ResultController() {
                                                        @Override
                                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                            switch (object.getConstructor()) {
                                                                case TdApi.Chat.CONSTRUCTOR: {
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    });
                                                }*/
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
        /*if (!isClickOnItemBlocked) {
            isClickOnItemBlocked = true;*/
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
                //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                ChatManager.getManager().getChat(0, chatInfo.tgChatObject.id, chatInfo, true, true);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        isClickOnItemBlocked = false;
                    }
                }, 200);
            }
        });
        //}
    }

    @Override
    public void setValues(ChatInfo record, int i, final Context context) {
        ((DialogView) itemView).setValues(record, i, context);
    }
}
