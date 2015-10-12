package com.stayfprod.utter.manager;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.support.v4.util.LongSparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojiconHandler;
import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.model.PushNotification;
import com.stayfprod.utter.ui.adapter.ChatListAdapter;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.component.MenuDrawerLayout;
import com.stayfprod.utter.ui.listener.DrawerClosedListener;
import com.stayfprod.utter.ui.view.SimpleRecyclerView;
import com.stayfprod.utter.ui.view.DialogView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.DateUtil;
import com.stayfprod.utter.util.TextUtil;
import com.stayfprod.utter.R;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ChatListManager extends ResultController {

    private final static String LOG = ChatListManager.class.getSimpleName();
    private static volatile ChatListManager sChatListManager;

    public static ChatListManager getManager() {
        if (sChatListManager == null) {
            synchronized (ChatListManager.class) {
                if (sChatListManager == null) {
                    sChatListManager = new ChatListManager();
                }
            }
        }
        return sChatListManager;
    }

    private final SparseIntArray mIndexPosUserMap = new SparseIntArray();//userID, pos in chat
    private final LongSparseArray<Integer> mIndexPosChatList = new LongSparseArray<Integer>();//chatId, pos in chat
    private final List<ChatInfo> mChatList = Collections.synchronizedList(new ArrayList<ChatInfo>());

    private volatile boolean mIsFirstInit = true;
    private volatile boolean mNeedDownloadMore = true;

    private ChatListAdapter mChatListAdapter;
    private LinearLayoutManager mChatListLayoutManager;
    private volatile int mChatOffset = 0;
    private int mChatLimit = 10;
    private boolean mIsFirstGetChats = true;
    private int mFirstVisibleItemPosition;

    public void initRecycleView(Context context, final MenuDrawerLayout drawerLayout) {
        final SimpleRecyclerView recyclerView = (SimpleRecyclerView) ((Activity) context).findViewById(R.id.a_chat_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        mChatListLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(mChatList, context);

        drawerLayout.setDrawerListener(new DrawerClosedListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                //info обязательно нужно сделать клик иначе чат первый раз не нажмется после закрытия дровера
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;
                float x = 0.0f;
                float y = 0.0f;
                int metaState = 0;
                MotionEvent motionEvent = MotionEvent.obtain(
                        downTime, eventTime,
                        MotionEvent.ACTION_CANCEL, x, y,
                        metaState
                );
                recyclerView.onTouchEvent(motionEvent);
            }
        });
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return drawerLayout.onTouchEvent(event);
            }
        };

        recyclerView.setOnTouchListener(onTouchListener);
        mChatListAdapter.setOnItemTouchListener(onTouchListener);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int visibleItemCount;
            int totalItemCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mNeedDownloadMore) {
                    visibleItemCount = mChatListLayoutManager.getChildCount();
                    totalItemCount = mChatListLayoutManager.getItemCount();
                    mFirstVisibleItemPosition = mChatListLayoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + mFirstVisibleItemPosition) >= totalItemCount * 0.8) {
                        mNeedDownloadMore = false;
                        getChats(false);
                    }
                }
            }
        });

        recyclerView.setAdapter(mChatListAdapter);
        if (mIsFirstInit) {
            mIsFirstInit = false;
            getManager().getChats(10, 0, true);
        }
    }

    public void getGroupChatFull(int chatId, Client.ResultHandler resultHandler) {
        TdApi.GetGroupChatFull groupChatFull = new TdApi.GetGroupChatFull();
        groupChatFull.groupChatId = chatId;
        client().send(groupChatFull, resultHandler);
    }

    public void getChats(int limit, int offset, boolean isFirstGetChats) {
        this.mChatOffset = offset;
        this.mChatLimit = limit;
        getChats(isFirstGetChats);
    }

    public void getChats(boolean isFirstGetChats) {
        this.mIsFirstGetChats = isFirstGetChats;
        TdApi.GetChats func = new TdApi.GetChats();
        if (!this.mIsFirstGetChats) {
            func.limit = mChatLimit;
        } else {
            func.limit = AndroidUtil.WINDOW_PORTRAIT_HEIGHT / DialogView.LAYOUT_HEIGHT + 5;
        }

        func.offset = mChatOffset;
        if (func.offset == 0) {
            this.mIndexPosUserMap.clear();
            this.mChatList.clear();
        }

        mChatListAdapter.setLoadingData();
        client().send(func, getManager());
    }

    public void addToIndexPosUserMap(int userId, int pos) {
        synchronized (mIndexPosChatList) {
            mIndexPosUserMap.put(userId, pos);
        }
    }

    public int getIndexPosByUserId(int userId) {
        synchronized (mIndexPosChatList) {
            return mIndexPosUserMap.get(userId);
        }
    }

    //info самые правильные блоки
    public void updateMembersInGroupChat(TdApi.UpdateChatParticipantsCount updateChatParticipantsCount, TdApi.GroupChatFull groupChatFull) {
        synchronized (mIndexPosChatList) {
            final ChatInfo chatInfo = getChatInfoByChatId(updateChatParticipantsCount.chatId);
            if (chatInfo != null) {
                TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                groupChatInfo.groupChat.participantsCount = updateChatParticipantsCount.participantsCount;
                if (chatInfo.groupChatFull != null) {
                    chatInfo.groupChatFull.adminId = groupChatFull.adminId;
                    chatInfo.groupChatFull.participants = groupChatFull.participants;
                    chatInfo.groupChatFull.groupChat = groupChatFull.groupChat;
                    chatInfo.groupChatFull.inviteLink = groupChatFull.inviteLink;
                } else {
                    chatInfo.groupChatFull = groupChatFull;
                }

                Object[] objects = ChatHelper.calculateOnlineUsersInGroupChat(groupChatFull);
                chatInfo.groupMembersOnline = (int) objects[0];

                //info странно но этот код обязателен ибо после добавления в чат бота его не видно по нажатию на шапку
                ChatInfo chatInfoForChat = ChatManager.getCurrentChatInfo();
                if (chatInfoForChat != null && ChatManager.getManager().isSameChatId(updateChatParticipantsCount.chatId)) {
                    TdApi.GroupChatInfo groupChatInfoForChat = (TdApi.GroupChatInfo) chatInfoForChat.tgChatObject.type;
                    groupChatInfoForChat.groupChat.participantsCount = updateChatParticipantsCount.participantsCount;
                    if (chatInfoForChat.groupChatFull != null) {
                        chatInfoForChat.groupChatFull.adminId = groupChatFull.adminId;
                        chatInfoForChat.groupChatFull.participants = groupChatFull.participants;
                        chatInfoForChat.groupChatFull.groupChat = groupChatFull.groupChat;
                        chatInfoForChat.groupChatFull.inviteLink = groupChatFull.inviteLink;
                    } else {
                        chatInfoForChat.groupChatFull = groupChatFull;
                    }
                    chatInfoForChat.groupMembersOnline = chatInfo.groupMembersOnline;
                }

                BotManager.getManager().getBotGroupInfoUpdate((List<CachedUser>) objects[1], chatInfo);
            }
        }
    }

    public void updateChatPhoto(TdApi.UpdateChatPhoto updateChatPhoto) {
        synchronized (mIndexPosChatList) {
            final ChatInfo chatInfo = getChatInfoByChatId(updateChatPhoto.chatId);
            if (chatInfo != null) {
                if (!chatInfo.isGroupChat) {
                    TdApi.PrivateChatInfo privateChatInfo = (TdApi.PrivateChatInfo) chatInfo.tgChatObject.type;
                    TdApi.ProfilePhoto profilePhoto = privateChatInfo.user.profilePhoto;
                    profilePhoto.id = updateChatPhoto.photo.id;
                    profilePhoto.small = updateChatPhoto.photo.small;
                    profilePhoto.big = updateChatPhoto.photo.big;
                } else {
                    TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                    TdApi.ProfilePhoto profilePhoto = groupChatInfo.groupChat.photo;
                    profilePhoto.id = updateChatPhoto.photo.id;
                    profilePhoto.small = updateChatPhoto.photo.small;
                    profilePhoto.big = updateChatPhoto.photo.big;
                }

                notifyDataSetChangedUI();
            }
        }
    }

    public void removeChat(final Long chatId) {
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                synchronized (mIndexPosChatList) {
                    final ChatInfo chatInfo = getChatInfoByChatId(chatId);
                    if (chatInfo != null) {
                        final CountDownLatch countDownLatch = new CountDownLatch(1);
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (mChatListAdapter != null && mChatList.size() > chatInfo.currentPosInList) {
                                    mChatList.remove(chatInfo.currentPosInList);
                                    mChatListAdapter.notifyItemRemoved(chatInfo.currentPosInList);
                                }
                                countDownLatch.countDown();
                            }
                        });
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            //
                        }
                        rebuildIndex();
                    }
                }
            }
        });
    }


    public void updateMessageId(final TdApi.UpdateMessageId updateMessageId) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(updateMessageId.chatId);
            if (chatInfo != null) {
                chatInfo.tgChatObject.topMessage.id = updateMessageId.newId;
                chatInfo.outputMsgIcon = ChatHelper.getTypeOfOutputMsgIcon(chatInfo.tgChatObject, updateMessageId.newId);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void updateMuteForChat(Long chatId, int muteFor) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(chatId);
            if (chatInfo != null) {
                TdApi.NotificationSettings currentSettings = chatInfo.tgChatObject.notificationSettings;
                currentSettings.muteFor = muteFor;
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void updateNotificationSettingForChat(long chatId, TdApi.NotificationSettings notificationSettings) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(chatId);
            if (chatInfo != null) {
                TdApi.NotificationSettings currentSettings = chatInfo.tgChatObject.notificationSettings;
                currentSettings.muteFor = notificationSettings.muteFor;
                currentSettings.sound = notificationSettings.sound;
                currentSettings.showPreviews = notificationSettings.showPreviews;
                currentSettings.eventsMask = notificationSettings.eventsMask;
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void updateDialogTitle(TdApi.UpdateChatTitle updateChatTitle) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(updateChatTitle.chatId);
            if (chatInfo != null) {
                chatInfo.chatName = new SpannableString(updateChatTitle.title);
                chatInfo.initials = TextUtil.createGroupChatInitials(updateChatTitle.title);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void updateDialogTitleWithSingleUser(int userId, CachedUser cachedUser) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByUserId(userId);
            if (chatInfo != null) {
                chatInfo.chatName = new SpannableString(cachedUser.fullName);
                chatInfo.initials = TextUtil.createGroupChatInitials(cachedUser.fullName);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void refreshDialogByUserId(int userId) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByUserId(userId);
            if (chatInfo != null) {
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    private LongSparseArray<TdApi.UpdateChatReadOutbox> updateChatReadOutboxArray = new LongSparseArray<TdApi.UpdateChatReadOutbox>();

    public void updateChatReadOutbox(TdApi.UpdateChatReadOutbox chatReadOutbox) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(chatReadOutbox.chatId);
            if (chatInfo != null) {
                chatInfo.tgChatObject.lastReadOutboxMessageId = chatReadOutbox.lastReadOutboxMessageId;
                chatInfo.outputMsgIcon = ChatHelper.getTypeOfOutputMsgIcon(chatInfo.tgChatObject, chatInfo.tgChatObject.topMessage.id);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            } else {
                updateChatReadOutboxArray.put(chatReadOutbox.chatId, chatReadOutbox);
            }
        }
    }

    private LongSparseArray<TdApi.UpdateChatReadInbox> updateChatReadInboxArray = new LongSparseArray<TdApi.UpdateChatReadInbox>();

    public void updateChatReadInbox(TdApi.UpdateChatReadInbox chatReadInbox) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(chatReadInbox.chatId);
            if (chatInfo != null) {
                chatInfo.tgChatObject.lastReadInboxMessageId = chatReadInbox.lastReadInboxMessageId;
                //BUGG(не приходило нормальное кол-во сообщений)
                chatInfo.tgChatObject.unreadCount = chatReadInbox.unreadCount;
                chatInfo.inputMsgIcon = ChatHelper.getTypeOfInputMsgIcon(chatInfo.tgChatObject);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            } else {
                updateChatReadInboxArray.put(chatReadInbox.chatId, chatReadInbox);
            }
        }
    }

    public void updateMessageDate(final TdApi.UpdateMessageDate updateMessageDate) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(updateMessageDate.chatId);
            if (chatInfo != null) {
                chatInfo.tgChatObject.topMessage.date = updateMessageDate.newDate;
                chatInfo.date = DrawPreparer.generateDate(updateMessageDate.newDate);
                DialogView.measure(chatInfo);
                notifyItemChangedByPosUI(chatInfo.currentPosInList);
            }
        }
    }

    public void readHistory(final long chatId) {
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                synchronized (mIndexPosChatList) {
                    ChatInfo chatInfo = getChatInfoByChatId(chatId);
                    if (chatInfo != null) {
                        chatInfo.inputMsgIcon = ChatHelper.getTypeOfInputMsgIcon(chatInfo.tgChatObject);
                        chatInfo.tgChatObject.unreadCount = 0;
                        DialogView.measure(chatInfo);
                        notifyItemChangedByPosUI(chatInfo.currentPosInList);
                    }
                }
            }
        });
    }

    public void updateMsgContent(final TdApi.UpdateMessageContent updateMessageContent) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(updateMessageContent.chatId);
            if (chatInfo != null) {
                if (chatInfo.tgChatObject.topMessage.id == updateMessageContent.messageId) {
                    chatInfo.tgChatObject.topMessage.message = updateMessageContent.newContent;
                    updateAllContent(chatInfo);
                    notifyItemChangedByPosUI(chatInfo.currentPosInList);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<CachedUser> processGroupChatFull(TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.GroupChatFull.CONSTRUCTOR: {
                TdApi.GroupChatFull groupChatFull = (TdApi.GroupChatFull) object;
                ChatInfo chatInfo = getChatInfoByChatId((long) groupChatFull.groupChat.id);
                if (chatInfo != null) {
                    chatInfo.groupChatFull = groupChatFull;

                    Object[] objects = ChatHelper.calculateOnlineUsersInGroupChat(groupChatFull);
                    chatInfo.groupMembersOnline = (int) objects[0];
                    return (List<CachedUser>) objects[1];
                }
            }
            break;
        }
        return null;
    }

    public void notifyItemChangedByPosUI(final int pos) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (mChatListAdapter != null && mChatList.size() > pos) {
                    mChatListAdapter.notifyItemChanged(pos);
                }
            }
        });
    }

    public void notifyDataSetChangedUI() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (mChatListAdapter != null && mChatList.size() > 0) {
                    mChatListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private ChatInfo getChatInfoByChatId(Long chatId) {
        if (mIndexPosChatList.size() > 0 && chatId != null) {
            final Integer pos = mIndexPosChatList.get(chatId);
            if (pos != null) {
                ChatInfo chatInfo = mChatList.get(pos);
                if (chatInfo != null)
                    chatInfo.currentPosInList = pos;
                return chatInfo;
            }
        }
        return null;
    }

    private ChatInfo getChatInfoByUserId(int userId) {
        if (mIndexPosUserMap.indexOfKey(userId) >= 0) {
            int pos = mIndexPosUserMap.get(userId);
            ChatInfo chatInfo = mChatList.get(pos);
            if (chatInfo != null) {
                chatInfo.currentPosInList = pos;
            }
            return chatInfo;
        }
        return null;
    }

    private void updateAllContent(ChatInfo chatInfo) {
        if (chatInfo != null) {
            chatInfo.date = DrawPreparer.generateDate(chatInfo.tgChatObject.topMessage.date);
            chatInfo.text = DrawPreparer.generateTopMsgText(chatInfo);
            chatInfo.inputMsgIcon = ChatHelper.getTypeOfInputMsgIcon(chatInfo.tgChatObject);
            chatInfo.outputMsgIcon = ChatHelper.getTypeOfOutputMsgIcon(chatInfo.tgChatObject, chatInfo.tgChatObject.topMessage.id);
            DialogView.measure(chatInfo);
        }
    }

    /*
    * Обновление итема без подъема наверх
    * */
    public void justUpdateChat(final Long chatId, final TdApi.Message message) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(chatId);
            if (chatInfo != null && message != null) {
                chatInfo.tgChatObject.topMessage = message;
                updateAllContent(chatInfo);
                notifyItemChangedByPosUI(0);
            }
        }
    }

    public void updateReplyMarkupMessageId(TdApi.UpdateChatReplyMarkup updateChatReplyMarkup) {
        synchronized (mIndexPosChatList) {
            ChatInfo chatInfo = getChatInfoByChatId(updateChatReplyMarkup.chatId);
            if (chatInfo != null) {
                chatInfo.tgChatObject.replyMarkupMessageId = updateChatReplyMarkup.replyMarkupMessageId;
            }
        }
    }

    //info самые правильны блоки end

    //private final ReentrantLock upAndChangeChatLocker = new ReentrantLock();

    public void upAndChangeChat(final long chatId, final TdApi.Message message, final boolean upUnread) {
        ThreadService.runTaskChatBackground(new Runnable() {
            @Override
            public void run() {
                synchronized (mIndexPosChatList) {
                    final CountDownLatch latchMain = new CountDownLatch(1);
                    if (mIndexPosChatList.size() > 0) {
                        try {
                            final Integer pos = mIndexPosChatList.get(chatId);
                            if (pos != null) {
                                final ChatInfo chatInfo = mChatList.get(pos);
                                chatInfo.tgChatObject.topMessage = message;
                                if (upUnread) {
                                    chatInfo.tgChatObject.unreadCount++;
                                }
                                updateAllContent(chatInfo);
                                if (pos > 0) {
                                    final CountDownLatch latchOne = new CountDownLatch(1);
                                    AndroidUtil.runInUI(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mChatListAdapter != null && mChatList.size() > pos) {
                                                mChatList.remove((int) pos);
                                                //mChatListAdapter.notifyItemRemoved(pos);
                                                mChatList.add(0, chatInfo);
                                                //mChatListAdapter.notifyItemInserted(0);
                                                mChatListAdapter.notifyDataSetChanged();
                                                if (mFirstVisibleItemPosition == 0 || mFirstVisibleItemPosition == 1) {
                                                    mChatListLayoutManager.scrollToPosition(0);
                                                }
                                                /*mChatListAdapter.notifyItemMoved(pos, 0);
                                                mChatListAdapter.notifyItemChanged(0);*/
                                            }
                                            latchOne.countDown();
                                        }
                                    });
                                    latchOne.await();
                                } else {
                                    final CountDownLatch latchTwo = new CountDownLatch(1);
                                    AndroidUtil.runInUI(new Runnable() {
                                        @Override
                                        public void run() {
                                            mChatListAdapter.notifyItemChanged(0);
                                            latchTwo.countDown();
                                        }
                                    });
                                    latchTwo.await();
                                }

                                if (!App.isAppInForeground() && !ChatHelper.isChatMuted(chatInfo)) {
                                    NotificationManager.getManager().
                                            sendNotification(App.getAppContext(),
                                                    new PushNotification(
                                                            UserManager.getManager().getUserByIdWithRequestAsync(message.fromId).tgUser.firstName,
                                                            chatInfo.text, message.chatId, chatInfo.isGroupChat));
                                }
                                rebuildIndex();
                            } else {
                                TdApi.GetChat getChat = new TdApi.GetChat();
                                getChat.chatId = chatId;
                                client().send(getChat, new ResultController() {
                                    @Override
                                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                        try {
                                            switch (object.getConstructor()) {
                                                case TdApi.Chat.CONSTRUCTOR:
                                                    TdApi.Chat chat = (TdApi.Chat) object;
                                                    mChatOffset++;
                                                    if (DrawPreparer.isDisplayingDialog(chat)) {
                                                        final ChatInfo chatInfo = DrawPreparer.prepareChatInfo(chat, -1);
                                                        final CountDownLatch latchThree = new CountDownLatch(1);
                                                        AndroidUtil.runInUI(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (mChatListAdapter != null) {
                                                                    mChatList.add(0, chatInfo);
                                                                    //mChatListAdapter.notifyItemInserted(0);
                                                                    mChatListAdapter.notifyDataSetChanged();
                                                                    if (mFirstVisibleItemPosition == 0 || mFirstVisibleItemPosition == 1) {
                                                                        mChatListLayoutManager.scrollToPosition(0);
                                                                    }
                                                                }
                                                                latchThree.countDown();
                                                            }
                                                        });
                                                        latchThree.await();
                                                        rebuildIndex();
                                                        if (!App.isAppInForeground() && !ChatHelper.isChatMuted(chatInfo)) {
                                                            NotificationManager.getManager().
                                                                    sendNotification(App.getAppContext(),
                                                                            new PushNotification(
                                                                                    UserManager.getManager().getUserByIdWithRequestAsync(message.fromId).tgUser.firstName,
                                                                                    chatInfo.text, message.chatId, chatInfo.isGroupChat));
                                                        }
                                                    }
                                                    break;
                                            }
                                        } catch (Throwable ex) {
                                            Log.e(LOG, "upAndChangeChat, afterResult", ex);
                                            Crashlytics.logException(ex);
                                        }
                                        //info блокировка нужна что бы избежать повторяющихся чатов(когда при старте куча апдейтов)
                                        latchMain.countDown();
                                    }
                                });
                                latchMain.await();
                            }
                        } catch (Throwable e) {
                            Log.e(LOG, "upAndChangeChat", e);
                            Crashlytics.logException(e);
                        }
                    }
                }
            }
        });
    }

    private void rebuildIndex() {
        mIndexPosChatList.clear();
        mIndexPosUserMap.clear();
        for (int i = 0; i < mChatList.size(); i++) {
            ChatInfo info = mChatList.get(i);
            mIndexPosChatList.put(info.tgChatObject.id, i);
            switch (info.tgChatObject.type.getConstructor()) {
                case TdApi.PrivateChatInfo.CONSTRUCTOR:
                    TdApi.User user = ((TdApi.PrivateChatInfo) info.tgChatObject.type).user;
                    mIndexPosUserMap.put(user.id, i);
                    break;
            }
        }
    }

    @Override
    public void afterResult(final TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Chats.CONSTRUCTOR: {
                //info обработа в другом потоке что бы успеть забрать информацию по юзеру который отправил сообщение последним в групповом чате
                ThreadService.runTaskChatBackground(new Runnable() {
                    @Override
                    public void run() {
                        UserManager.getManager().getInfoMeIfNeed();//.getContacts();
                        try {
                            synchronized (mIndexPosChatList) {
                                final int remChatOffset = mChatOffset;
                                final TdApi.Chat[] chatArr = ((TdApi.Chats) object).chats;

                                mNeedDownloadMore = chatArr.length != 0;
                                mChatOffset += chatArr.length;

                                final List<ChatInfo> tempChatList = new ArrayList<>(50);

                                int pos = 0;
                                for (int i = 0; i < chatArr.length; i++) {
                                    TdApi.Chat chat = chatArr[i];

                                    if (DrawPreparer.isDisplayingDialog(chat)) {
                                        //info это на тот случай если чаты пришли в апдейтах
                                        if (mIndexPosChatList.indexOfKey(chat.id) < 0) {

                                            TdApi.UpdateChatReadOutbox updateChatReadOutbox = updateChatReadOutboxArray.get(chat.id);
                                            TdApi.UpdateChatReadInbox updateChatReadInbox = updateChatReadInboxArray.get(chat.id);

                                            if (updateChatReadOutbox != null) {
                                                updateChatReadOutboxArray.remove(chat.id);
                                                chat.lastReadOutboxMessageId = updateChatReadOutbox.lastReadOutboxMessageId;
                                            }

                                            if (updateChatReadInbox != null) {
                                                updateChatReadInboxArray.remove(chat.id);
                                                chat.lastReadInboxMessageId = updateChatReadInbox.lastReadInboxMessageId;
                                                chat.unreadCount = updateChatReadInbox.unreadCount;
                                            }

                                            tempChatList.add(DrawPreparer.prepareChatInfo(chat, pos));
                                            mIndexPosChatList.put(chat.id, remChatOffset + pos);
                                            pos++;
                                        }
                                    }
                                }
                                final CountDownLatch latch = new CountDownLatch(1);
                                AndroidUtil.runInUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        mChatList.addAll(tempChatList);
                                        if (mChatListAdapter != null)
                                            mChatListAdapter.updateDataAfterLoading();
                                        latch.countDown();
                                    }
                                });
                                latch.await();
                            }
                        } catch (Throwable e) {
                            Log.e(LOG, "afterResult", e);
                            Crashlytics.logException(e);
                        }
                    }
                });
                break;
            }
        }
    }

    public void destroy() {
        mChatListAdapter = null;
        mIndexPosUserMap.clear();
        mChatList.clear();
        mIndexPosChatList.clear();
        sChatListManager = null;
    }

    //############Подготовка перед отрисовкой############
    public static class DrawPreparer {

        public static boolean isDisplayingDialog(TdApi.Chat tgChat) {
            //info если только id значит это unknownPrivateChatInfo или unknownGroupChatInfo
            return !(tgChat.topMessage == null || tgChat.type == null || tgChat.notificationSettings == null);
        }

        /*
        * Если pos == -1 то не добавится в индекс по юзеру,
        * необходимо если идет блокировка по mIndexPosChatList
        * */
        public static ChatInfo prepareChatInfo(TdApi.Chat tgChat, int pos) {
            ChatInfo chatInfo = new ChatInfo();
            chatInfo.tgChatObject = tgChat;
            buildUniqueInfo(tgChat.type, chatInfo, pos);
            chatInfo.date = generateDate(tgChat.topMessage.date);
            chatInfo.text = generateTopMsgText(chatInfo);
            chatInfo.inputMsgIcon = ChatHelper.getTypeOfInputMsgIcon(tgChat);
            chatInfo.outputMsgIcon = ChatHelper.getTypeOfOutputMsgIcon(tgChat, tgChat.topMessage.id);

            DialogView.measure(chatInfo);
            //IconFactory.putLocalIconInCache(IconFactory.Type.CHAT_LIST, chatInfo.tgChatObject.type);
            return chatInfo;
        }

        private static void buildUniqueInfo(TdApi.ChatInfo type, ChatInfo chatInfo, int pos) {
            switch (type.getConstructor()) {
                case TdApi.PrivateChatInfo.CONSTRUCTOR: {
                    UserManager userManager = UserManager.getManager();
                    CachedUser cachedUser = userManager.insertUserInCache(((TdApi.PrivateChatInfo) type).user);

                    if (pos != -1)
                        getManager().addToIndexPosUserMap(cachedUser.tgUser.id, pos);

                    chatInfo.chatName = new SpannableString(cachedUser.fullName);
                    chatInfo.initials = cachedUser.initials;
                    chatInfo.isGroupChat = false;
                    chatInfo.isBot = cachedUser.tgUser.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR;
                    break;
                }
                case TdApi.GroupChatInfo.CONSTRUCTOR: {
                    TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) type;
                    TdApi.GroupChat groupChat = groupChatInfo.groupChat;
                    chatInfo.chatName = new SpannableString(groupChat.title);
                    chatInfo.initials = TextUtil.createGroupChatInitials(groupChat.title);
                    chatInfo.isGroupChat = true;
                    chatInfo.groupChatFull = null;
                    break;
                }
            }
        }

        public static String generateDate(int date) {
            return DateUtil.getDateForChat(date, DateUtil.DateType.CHAT_LIST);
        }

        private static Spannable generateTopMsgText(ChatInfo chatInfo) {
            TdApi.Message topMessage = chatInfo.tgChatObject.topMessage;
            Spannable spanText = null;
            String userGroupName = "";

            if (chatInfo.isGroupChat) {
                UserManager userManager = UserManager.getManager();

                if (userManager.getCurrUserId() != topMessage.fromId) {
                    CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(topMessage.fromId);
                    userGroupName = ChatHelper.getUserOnlyName(cachedUser.tgUser);
                }
            }
            switch (topMessage.message.getConstructor()) {
                case TdApi.MessageText.CONSTRUCTOR: {
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(ChatHelper.getSpanSystemText(userGroupName));
                    builder.append(TextUtil.replaceNewRowSymbols(((TdApi.MessageText) topMessage.message).text));
                    EmojiconHandler.addEmojis(App.getAppContext(), builder, Constant.DP_20, 0, -1, false);
                    spanText = builder;
                    break;
                }
                case TdApi.MessageAudio.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.audio));
                    break;
                }
                case TdApi.MessageVoice.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.voice));
                    break;
                }
                case TdApi.MessageDocument.CONSTRUCTOR: {
                    TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(userGroupName, messageDocument.document.fileName);
                    break;
                }
                case TdApi.MessageSticker.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.sticker));
                    break;
                }
                case TdApi.MessageChatJoinByLink.CONSTRUCTOR: {
                    TdApi.MessageChatJoinByLink messageChatJoinByLink = (TdApi.MessageChatJoinByLink) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.chat_join_by_link));
                    break;
                }
                case TdApi.MessageVenue.CONSTRUCTOR: {
                    TdApi.MessageVenue messageVenue = (TdApi.MessageVenue) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.venue));
                    break;
                }
                case TdApi.MessageWebPage.CONSTRUCTOR: {
                    TdApi.MessageWebPage messageWebPage = (TdApi.MessageWebPage) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.web_page));
                    break;
                }
                case TdApi.MessagePhoto.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.photo));
                    break;
                }
                case TdApi.MessageVideo.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.video));
                    break;
                }
                case TdApi.MessageLocation.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.location));
                    break;
                }
                case TdApi.MessageContact.CONSTRUCTOR: {
                    TdApi.MessageContact messageContact = (TdApi.MessageContact) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(userGroupName, AndroidUtil.getResourceString(R.string.contact));
                    break;
                }
                case TdApi.MessageChatDeleteParticipant.CONSTRUCTOR:
                    TdApi.MessageChatDeleteParticipant deleteParticipant = (TdApi.MessageChatDeleteParticipant) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(deleteParticipant.user.firstName + " "
                            + deleteParticipant.user.lastName + AndroidUtil.getResourceString(R.string.left_group));
                    break;
                case TdApi.MessageChatChangePhoto.CONSTRUCTOR:
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.chat_photo_was_changed));
                    break;
                case TdApi.MessageChatAddParticipant.CONSTRUCTOR:
                    TdApi.MessageChatAddParticipant chatAddParticipant = (TdApi.MessageChatAddParticipant) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.added_new_user)
                            + chatAddParticipant.user.firstName + " " + chatAddParticipant.user.lastName);
                    break;
                case TdApi.MessageChatDeletePhoto.CONSTRUCTOR:
                    TdApi.MessageChatDeletePhoto messageChatDeletePhoto = (TdApi.MessageChatDeletePhoto) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.chat_photo_was_deleted));
                    break;
                case TdApi.MessageChatChangeTitle.CONSTRUCTOR:
                    TdApi.MessageChatChangeTitle messageChatChangeTitle = (TdApi.MessageChatChangeTitle) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.new_group_name) + messageChatChangeTitle.title);
                    break;
                case TdApi.MessageGroupChatCreate.CONSTRUCTOR: {
                    TdApi.MessageGroupChatCreate messageGroupChatCreate = (TdApi.MessageGroupChatCreate) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.created_the_group_) + messageGroupChatCreate.title);
                    break;
                }
                case TdApi.MessageDeleted.CONSTRUCTOR: {
                    TdApi.MessageDeleted messageDeleted = (TdApi.MessageDeleted) topMessage.message;
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.message_was_deleted));
                    break;
                }
                case TdApi.MessageUnsupported.CONSTRUCTOR: {
                    spanText = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.unsupported_message));
                    break;
                }
            }
            return spanText;
        }
    }
}
