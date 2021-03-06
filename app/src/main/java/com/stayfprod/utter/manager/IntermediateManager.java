package com.stayfprod.utter.manager;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.activity.IntermediateActivity;
import com.stayfprod.utter.ui.adapter.IntermediateAdapter;
import com.stayfprod.utter.ui.view.DialogView;
import com.stayfprod.utter.ui.view.SimpleRecyclerView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class IntermediateManager extends ResultController {

    private final static String LOG = IntermediateManager.class.getSimpleName();
    private static volatile IntermediateManager intermediateManager;

    private volatile boolean mIsFirstInit = true;
    private volatile boolean mNeedDownloadMore = true;
    private volatile int mChatOffset = 0;

    private int mChatLimit = 10;
    private IntermediateAdapter mChatListAdapter;
    private LinearLayoutManager mChatListLayoutManager;
    private int mFirstVisibleItemPosition;
    private final List<ChatInfo> mChatList = Collections.synchronizedList(new ArrayList<ChatInfo>());
    private IntermediateActivity.TypeList mTypeList;

    public IntermediateActivity.Action action;
    public Integer botId;
    public Integer userId;

    @Override
    public boolean hasChanged() {
        return true;
    }

    public static IntermediateManager getManager() {
        if (intermediateManager == null) {
            synchronized (IntermediateManager.class) {
                if (intermediateManager == null) {
                    intermediateManager = new IntermediateManager();
                }
            }
        }
        return intermediateManager;
    }

    public void getGroupChatFull(int chatId, Client.ResultHandler resultHandler) {
        TdApi.GetGroupChatFull groupChatFull = new TdApi.GetGroupChatFull();
        groupChatFull.groupChatId = chatId;
        client().send(groupChatFull, resultHandler);
    }

    @SuppressWarnings("unchecked")
    public List<CachedUser> processGroupChatFull(TdApi.TLObject object, int calledConstructor, ChatInfo chatInfo) {
        switch (object.getConstructor()) {
            case TdApi.GroupChatFull.CONSTRUCTOR: {
                TdApi.GroupChatFull groupChatFull = (TdApi.GroupChatFull) object;
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

    public void initRecycleView(Context context, IntermediateActivity.TypeList typeList, IntermediateActivity.Action action, Integer botId, Integer userId) {
        this.mTypeList = typeList;
        this.action = action;
        this.botId = botId;
        this.userId = userId;
        final SimpleRecyclerView recyclerView = ((AbstractActivity) context).findView(R.id.a_intermediate_chat_list);
        recyclerView.setHasFixedSize(true);
        mChatListLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new IntermediateAdapter(mChatList, context);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int visibleItemCount;
            int totalItemCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //fixme очень не понятное место!!!
                try {
                    super.onScrolled(recyclerView, dx, dy);
                } catch (Exception e) {
                    Log.e(LOG, "onScrolled", e);
                }

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

    public void getChats(int limit, int offset, boolean isFirstGetChats) {
        this.mChatOffset = offset;
        this.mChatLimit = limit;
        getChats(isFirstGetChats);
    }

    public void getChats(boolean isFirstGetChats) {
        TdApi.GetChats func = new TdApi.GetChats();
        if (!isFirstGetChats) {
            func.limit = mChatLimit;
        } else {
            func.limit = AndroidUtil.WINDOW_PORTRAIT_HEIGHT / DialogView.LAYOUT_HEIGHT + 5;
        }

        func.offset = mChatOffset;
        if (func.offset == 0) {
            this.mChatList.clear();
        }

        mChatListAdapter.setLoadingData();
        client().send(func, getManager());
    }

    @Override
    public void afterResult(final TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Chats.CONSTRUCTOR: {
                //info обработа в другом потоке что бы успеть забрать информацию по юзеру который отправил сообщение последним в групповом чате
                ThreadService.runTaskIntermediateBackground(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int remChatOffset = mChatOffset;
                            final TdApi.Chat[] chatArr = ((TdApi.Chats) object).chats;

                            mNeedDownloadMore = chatArr.length != 0;
                            mChatOffset += chatArr.length;

                            final List<ChatInfo> tempChatList = new ArrayList<>(50);

                            for (int i = 0; i < chatArr.length; i++) {
                                TdApi.Chat chat = chatArr[i];
                                if (ChatListManager.DrawPreparer.isDisplayingDialog(chat)) {
                                    ChatInfo chatInfo = ChatListManager.DrawPreparer.prepareChatInfo(chat, -1);

                                    switch (mTypeList) {
                                        case USERS_ONLY:
                                            if (!chatInfo.isGroupChat) {
                                                tempChatList.add(chatInfo);
                                            }
                                            break;
                                        case GROUPS_ONLY:
                                            if (chatInfo.isGroupChat) {
                                                Boolean isLeave = ((TdApi.GroupChatInfo) chatInfo.tgChatObject.type).groupChat.left;
                                                if (!isLeave) {
                                                    tempChatList.add(chatInfo);
                                                }
                                            }
                                            break;
                                        case ALL:
                                            tempChatList.add(chatInfo);
                                            break;
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
        mChatList.clear();
        mChatListLayoutManager = null;
        intermediateManager = null;
    }
}
