package com.stayfprod.utter.manager;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.chat.AbstractMainMsg;
import com.stayfprod.utter.model.chat.AudioMsg;
import com.stayfprod.utter.model.chat.BotDescriptionMsg;
import com.stayfprod.utter.model.chat.ChangeIconTitleMsg;
import com.stayfprod.utter.model.chat.ContactMsg;
import com.stayfprod.utter.model.chat.VoiceMsg;
import com.stayfprod.utter.model.chat.DateDivider;
import com.stayfprod.utter.model.chat.DocumentMsg;
import com.stayfprod.utter.model.chat.GeoMsg;
import com.stayfprod.utter.model.chat.MsgDivider;
import com.stayfprod.utter.model.chat.PhotoMsg;
import com.stayfprod.utter.model.chat.StickerMsg;
import com.stayfprod.utter.model.chat.SystemMsg;
import com.stayfprod.utter.model.chat.TextMsg;
import com.stayfprod.utter.model.chat.VideoMsg;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.ui.adapter.ChatAdapter;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.AbstractChatMsg;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.OutputMsgIconType;
import com.stayfprod.utter.ui.listener.RecyclerItemClickListener;
import com.stayfprod.utter.ui.view.SimpleRecyclerView;
import com.stayfprod.utter.ui.view.chat.AbstractMsgView;
import com.stayfprod.utter.ui.view.chat.AudioMsgView;
import com.stayfprod.utter.ui.view.chat.ContactMsgView;
import com.stayfprod.utter.ui.view.chat.VoiceMsgView;
import com.stayfprod.utter.ui.view.chat.DocumentMsgView;
import com.stayfprod.utter.ui.view.chat.GeoMsgView;
import com.stayfprod.utter.ui.view.chat.PhotoMsgView;
import com.stayfprod.utter.ui.view.chat.StickerMsgView;
import com.stayfprod.utter.ui.view.chat.TextMsgView;
import com.stayfprod.utter.ui.view.chat.VideoMsgView;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.DateUtil;
import com.stayfprod.utter.ui.activity.PhotoActivity;
import com.stayfprod.utter.R;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatManager extends ResultController {

    private final static String LOG = ChatManager.class.getSimpleName();
    private static volatile ChatManager sChatManager;

    public static volatile boolean sIsNeedRemoveChat = false;

    public static ChatManager getManager() {
        if (sChatManager == null) {
            synchronized (ChatManager.class) {
                if (sChatManager == null) {
                    sChatManager = new ChatManager();
                }
            }
        }
        return sChatManager;
    }

    public volatile int lastVisibleItem;
    public volatile int firstVisibleItem;

    private ChatAdapter mChatAdapter;
    private final Integer mChatLimit = 10;
    private volatile Integer mChatOffset;
    private volatile Long mChatId;
    private volatile Integer mChatFromId;
    private volatile ChatInfo mChatInfo;
    private volatile boolean mIsFirstGetChat;

    private SimpleRecyclerView mRecyclerView;
    private LinearLayoutManager mGridLayoutManager;
    private volatile boolean mNeedDownloadMore = true;
    private volatile List<AbstractChatMsg> mChatMessageList = Collections.synchronizedList(new ArrayList<AbstractChatMsg>(100));

    //хранит дату последнего сообщения в чате
    private volatile int mRemDate = 0;

    //для индексов, что бы не сбились, счетчит разделителей(они не в индексацию)
    private volatile int mDividersCounter = 0;
    private volatile int mTempUnreadCount = 0;

    private ImageView mImageScrollView;

    private final SparseArray<List<Integer>> mIndexPosListUserArray = new SparseArray<List<Integer>>(); //userID, pos
    private final SparseIntArray mIndexPosIdMsgArray = new SparseIntArray(); //msgId,pos

    //отклоненние для отправленныйх мной сообщений(не перестраиваем индекс если мое собщение) Всегда отрицательный
    private AtomicInteger mIndexNewMsgDeviation = new AtomicInteger(0);

    public static ChatInfo getCurrentChatInfo() {
        return getManager().mChatInfo;
    }

    public static boolean isHaveChatInfo() {
        return getManager().mChatInfo != null;
    }

    public static boolean isHaveChatId() {
        return getManager().mChatId != null;
    }

    public static Long getCurrentChatId() {
        return getManager().mChatId;
    }

    public boolean isSameChatId(long chatId) {
        return this.mChatId != null && this.mChatId == chatId;
    }

    public void stopScroll() {
        if (mRecyclerView != null) {
            mRecyclerView.stopScroll();
        }
    }

    public void clean() {
        mChatAdapter = null;
        mChatInfo = null;//по нему смотрим делать ли в чате что-то
        mChatId = null;
        mIndexPosIdMsgArray.clear();
        mIndexPosListUserArray.clear();
        mChatMessageList.clear();
        firstVisibleItem = 0;
        lastVisibleItem = 0;
        mTempUnreadCount = 0;
        mDividersCounter = 0;
        mRemDate = 0;
        mImageScrollView = null;
        mRecyclerView = null;
        //scrollToPos = 0;
        sChatManager = null;
    }

    private void setVisibilityNoMsges(final int visibility) {
        try {
            final TextView chatNoMsges = (TextView) ((View) mRecyclerView.getParent()).findViewById(R.id.a_chat_no_msges);
            if (chatNoMsges.getVisibility() != visibility) {
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        chatNoMsges.setVisibility(visibility);
                    }
                });
            }
        } catch (Exception e) {
            Log.w(LOG, "setVisibilityNoMsges", e);
        }
    }

    public void cleanHistory() {
        this.mChatOffset = 0;
        final int size = mChatMessageList.size();
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                mIndexNewMsgDeviation.set(0);
                if (mChatInfo.isBot) {
                    try {
                        AbstractChatMsg abstractChatMsg = mChatMessageList.get(size - 1);
                        mChatMessageList.clear();

                        if (abstractChatMsg instanceof BotDescriptionMsg) {
                            mChatMessageList.add(abstractChatMsg);
                        } else {
                            BotDescriptionMsg botDescription = new BotDescriptionMsg();
                            botDescription.type = AbstractChatMsg.Type.MSG_BOT_DESCRIPTION;
                            CachedUser cachedUser = UserManager.getManager().getUserByIdWithRequestAsync(getCurrentChatId());
                            if (cachedUser != null && cachedUser.tgUser != null) {
                                botDescription.description = BotManager.getManager().linkifyDescription(((TdApi.BotInfoGeneral) cachedUser.botInfo));
                            } else {
                                botDescription.description = new SpannableString("");
                            }
                            botDescription.isFullScreen = true;
                            mChatMessageList.add(botDescription);
                        }

                        mChatAdapter.notifyDataSetChanged();
                        notifyObservers(new NotificationObject(NotificationObject.BOT_SHOW_START, null));
                        mDividersCounter = 1;
                    } catch (Exception e) {
                        Log.w(LOG, "cleanHistory", e);
                    }
                } else {
                    mDividersCounter = 0;
                    mChatMessageList.clear();
                    mChatAdapter.notifyItemRangeRemoved(0, size);
                }
            }
        });
        sIsNeedRemoveChat = true;
        this.mIndexPosIdMsgArray.clear();
        this.mIndexPosListUserArray.clear();
        if (!mChatInfo.isBot)
            setVisibilityNoMsges(View.VISIBLE);
    }

    public void changeChatPhoto(String file, long chatId) {
        TdApi.ChangeChatPhoto changeChatPhoto = new TdApi.ChangeChatPhoto();
        changeChatPhoto.photo = new TdApi.InputFileLocal(file);
        changeChatPhoto.chatId = chatId;
        client().send(changeChatPhoto, this);
    }

    public void forceClose() {
        notifyObservers(new NotificationObject(NotificationObject.FORCE_CLOSE_CHAT, null));
    }

    private Runnable afterInitChatAction;

    public void setAfterInitChatAction(Runnable afterInitChatAction) {
        this.afterInitChatAction = afterInitChatAction;
    }

    public void callAfterInitChatAction() {
        if (afterInitChatAction != null) {
            afterInitChatAction.run();
            afterInitChatAction = null;
        }
    }

    public void readChatHistory() {
        if (isHaveChatInfo() && mChatInfo.tgChatObject.unreadCount != 0) {
            mChatInfo.tgChatObject.unreadCount = 0;
            TdApi.GetChatHistory func = new TdApi.GetChatHistory();
            func.limit = 1;
            func.offset = 0;
            func.chatId = this.mChatId;
            func.fromId = this.mChatFromId;
            ChatListManager.getManager().readHistory(mChatInfo.tgChatObject.id);
            client().send(func, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {

                }
            });
        }
    }

    public AbstractChatMsg getChatMsgByPos(int pos) {
        if (pos < 0 || pos >= mChatMessageList.size()) {
            return null;
        }
        synchronized (mIndexPosIdMsgArray) {
            return mChatMessageList.get(pos);
        }
    }

    public void deleteMessage(final Long chatId, int msgId) {
        if (chatId != null) {
            final int[] msgs = new int[]{msgId};
            final TdApi.DeleteMessages func = new TdApi.DeleteMessages();
            func.chatId = chatId;
            func.messageIds = msgs;
            client().send(func, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                    switch (object.getConstructor()) {
                        case TdApi.Ok.CONSTRUCTOR:
                            //может ответить синхронно??
                            ThreadService.runTaskBackground(new Runnable() {
                                @Override
                                public void run() {
                                    deleteMessageFromChat(msgs, chatId);
                                }
                            });

                            break;
                    }
                }
            });
        }
    }

    public void rebuildIndexes() {
        synchronized (mIndexPosIdMsgArray) {
            if (isHaveChatId()) {
                mIndexNewMsgDeviation.set(0);
                mIndexPosIdMsgArray.clear();
                mIndexPosListUserArray.clear();

                for (int i = 0; i < mChatMessageList.size(); i++) {
                    AbstractChatMsg abstractChatMessage = mChatMessageList.get(i);
                    //info в индекс попадают все сообщения получаемы от телеграмма(без разделитель даты и кол-ва новых сообщений и информации о боте)
                    if (abstractChatMessage != null && abstractChatMessage.tgMessage != null) {
                        if (abstractChatMessage instanceof AbstractMainMsg) {
                            AbstractMainMsg chatMessage = (AbstractMainMsg) abstractChatMessage;
                            mIndexPosIdMsgArray.put(chatMessage.tgMessage.id, i);
                            addToIndexPosListUserArray(chatMessage.tgMessage.fromId, i);
                        }
                    }
                }
            }
        }
    }

    public synchronized void deleteMessageFromChat(int msg[], long cid) {
        if (isHaveChatId() && cid == mChatId) {
            try {
                for (int i = 0; i < msg.length; i++) {
                    int id = msg[i];
                    //за синхронизацию можно не бояться тут остановится
                    final Integer pos = getRealPos(id);
                    if (pos != null) {
                        final CountDownLatch deleteLatchOne = new CountDownLatch(1);
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (isHaveChatId()) {
                                    try {
                                        mChatMessageList.remove((int) pos);
                                        mChatAdapter.notifyItemRemoved(pos);
                                    } catch (Exception e) {
                                        Log.e(LOG, "deleteMessageFromChat", e);
                                        Crashlytics.logException(e);
                                    } finally {
                                        deleteLatchOne.countDown();
                                    }
                                }
                            }
                        });
                        deleteLatchOne.await();
                        //первый
                        if (pos == 0) {
                            //смотрим только на верхний элемент(pos верный)
                            if (mChatMessageList.size() > 0) {
                                AbstractChatMsg.Type type = mChatMessageList.get(pos).type;
                                if (type == AbstractChatMsg.Type.DATE_DIVIDER || type == AbstractChatMsg.Type.NEW_MSG_DIVIDER) {
                                    mDividersCounter--;
                                    final CountDownLatch deleteLatchTwo = new CountDownLatch(1);
                                    AndroidUtil.runInUI(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isHaveChatId()) {
                                                mChatMessageList.remove((int) pos);
                                                mChatAdapter.notifyItemRemoved(pos);
                                                deleteLatchTwo.countDown();
                                            }
                                        }
                                    });
                                    deleteLatchTwo.await();
                                    //выше даты может быть разделитель новых сообщений
                                    try {
                                        if (mChatMessageList.get(pos).type == AbstractChatMsg.Type.NEW_MSG_DIVIDER) {
                                            mDividersCounter--;
                                            final CountDownLatch deleteLatchThree = new CountDownLatch(1);
                                            AndroidUtil.runInUI(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isHaveChatId()) {
                                                        mChatMessageList.remove((int) pos);
                                                        mChatAdapter.notifyItemRemoved(pos);
                                                        deleteLatchThree.countDown();
                                                    }
                                                }
                                            });
                                            deleteLatchThree.await();
                                        }
                                    } catch (Exception e) {
                                        //игнор
                                    }
                                }
                                //сменить в списке чатов
                                if (mChatMessageList.size() != 0) {
                                    //todo внутри тоже сделать countDown??
                                    //Logs.e(mChatMessageList.size());
                                    ChatListManager.getManager().justUpdateChat(mChatId, mChatMessageList.get(0).tgMessage);
                                }
                            }

                        } else {
                            //последний
                            if (pos == mChatMessageList.size() - 1) {
                                //не может быть последняя всегда дата
                            } else {
                                //в серединке
                                //смотрим выше, не дата ли стоит(тут именно pos тк сообщение мы удалили раньше)
                                if (mChatMessageList.size() > 0) {
                                    if (mChatMessageList.get(pos).type == AbstractChatMsg.Type.DATE_DIVIDER) {
                                        //смотрим ниже
                                        if (mChatMessageList.get(pos - 1).type == AbstractChatMsg.Type.DATE_DIVIDER) {
                                            //сверху и с низу сообщения даты, удаляем верхнюю дату
                                            mDividersCounter--;
                                            final CountDownLatch deleteLatchFour = new CountDownLatch(1);
                                            AndroidUtil.runInUI(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isHaveChatId()) {
                                                        mChatMessageList.remove(pos - 1);
                                                        mChatAdapter.notifyItemRemoved(pos - 1);
                                                        deleteLatchFour.countDown();
                                                    }
                                                }
                                            });
                                            deleteLatchFour.await();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        if (mChatMessageList.size() == 0) {
                            sIsNeedRemoveChat = true;
                            setVisibilityNoMsges(View.VISIBLE);
                        }

                        if (mChatInfo.isBot && mChatMessageList.size() == 1) {
                            sIsNeedRemoveChat = true;
                            notifyObservers(new NotificationObject(NotificationObject.BOT_SHOW_START, null));
                        }
                    }
                });

                rebuildIndexes();
            } catch (Throwable e) {
                Log.e(LOG, "deleteMessageFromChat", e);
                Crashlytics.logException(e);
            }
        }
    }

    public void deleteChatHistory() {
        TdApi.DeleteChatHistory func = new TdApi.DeleteChatHistory();
        func.chatId = mChatId;
        client().send(func, getManager());
    }

    public void deleteChatParticipant() {
        TdApi.DeleteChatParticipant func = new TdApi.DeleteChatParticipant();
        func.chatId = mChatId;
        func.userId = mChatFromId;
        client().send(func, getManager());
    }

    public void deleteChatParticipant(long chatId, int userId, ResultController resultController) {
        TdApi.DeleteChatParticipant func = new TdApi.DeleteChatParticipant();
        func.chatId = chatId;
        func.userId = userId;
        client().send(func, resultController);
    }

    public void deleteAndLeaveSelf(final ResultController after) {
        TdApi.DeleteChatHistory func = new TdApi.DeleteChatHistory();
        func.chatId = mChatId;
        client().send(func, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                TdApi.DeleteChatParticipant func = new TdApi.DeleteChatParticipant();
                func.chatId = mChatId;
                func.userId = mChatFromId;
                client().send(func, after);
            }
        });
    }

    public void sendMessage(TdApi.InputMessageContent messageContent) {
        readChatHistory();
        TdApi.SendMessage func = new TdApi.SendMessage();
        func.chatId = mChatId;
        func.message = messageContent;
        client().send(func, getManager());
    }

    public TdApi.InputMessageContent createTextMsg(String text) {
        TdApi.InputMessageText messageText = new TdApi.InputMessageText();
        messageText.text = text;
        return messageText;
    }

    public TdApi.InputMessageVoice createVoiceMsg(String filePath, int duration) {
        TdApi.InputMessageVoice inputMessageVoice = new TdApi.InputMessageVoice();
        inputMessageVoice.voice = new TdApi.InputFileLocal(filePath);
        inputMessageVoice.duration = duration;
        return inputMessageVoice;
    }

    public TdApi.InputMessageContent createPhotoMsg(String filePath) {
        TdApi.InputMessagePhoto messagePhoto = new TdApi.InputMessagePhoto();
        messagePhoto.photo = new TdApi.InputFileLocal(filePath);
        return messagePhoto;
    }


    public TdApi.InputMessageContent createContactMsg(String firstName, String lastName, String phone, int userId) {
        TdApi.InputMessageContact messageContact = new TdApi.InputMessageContact();
        messageContact.firstName = firstName;
        messageContact.lastName = lastName;
        messageContact.phoneNumber = phone;
        messageContact.userId = userId;
        return messageContact;
    }


    public TdApi.InputMessageContent createStickerMsg(String filePath) {
        TdApi.InputMessageSticker messageSticker = new TdApi.InputMessageSticker();
        messageSticker.sticker = new TdApi.InputFileLocal(filePath);
        return messageSticker;
    }

    public TdApi.InputMessageContent createForwardMsg(int msgId) {
        TdApi.InputMessageForwarded messageForwarded = new TdApi.InputMessageForwarded();
        messageForwarded.fromChatId = mChatId;
        messageForwarded.messageId = msgId;
        return messageForwarded;
    }

    public void forwardMessages(int msgId[], long fromChatId) {
        //Logs.e(msgId + " " + fromChatId + " " + mChatId);
        TdApi.ForwardMessages forwardMessages = new TdApi.ForwardMessages();
        forwardMessages.fromChatId = fromChatId;
        forwardMessages.chatId = mChatId;
        forwardMessages.messageIds = msgId;
        client().send(forwardMessages, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                switch (object.getConstructor()) {
                    case TdApi.Messages.CONSTRUCTOR:
                        TdApi.Messages messages = (TdApi.Messages) object;

                        for (int i = 0; i < messages.messages.length; i++) {
                            TdApi.Message message = messages.messages[i];
                            addMyMsg(message);
                            if (i == messages.messages.length - 1) {
                                ChatListManager.getManager().upAndChangeChat(message.chatId, message, false);
                            }
                        }

                        break;
                }
            }
        });
    }

    private void showDialog(final AbstractMainMsg chatMessage, Context context) {
        String list[] = {AndroidUtil.getResourceString(R.string.forward), AndroidUtil.getResourceString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(AndroidUtil.getResourceString(R.string.message))
                .setItems(list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ChatManager chatManager = ChatManager.getManager();
                        try {
                            if (ChatManager.isHaveChatInfo()) {
                                Integer pos = chatManager.getRealPos(chatMessage.tgMessage.id);
                                AbstractChatMsg message = mChatMessageList.get(pos);
                                if (message != null) {
                                    ChatManager manager = ChatManager.getManager();
                                    if (which == 0) {
                                        manager.sendMessage(manager.createForwardMsg(message.tgMessage.id));
                                    }
                                    if (which == 1) {
                                        manager.deleteMessage(ChatManager.getCurrentChatId(), message.tgMessage.id);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(LOG, "showDialog", e);
                            Crashlytics.logException(e);
                        }
                    }
                }).setNegativeButton(AndroidUtil.getResourceString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void initRecycleView(final Context context, Boolean isLeave) {
        mRecyclerView = (SimpleRecyclerView) ((Activity) context).findViewById(R.id.a_chat_recycler_view);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mGridLayoutManager = new LinearLayoutManager(context);
        mGridLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mImageScrollView = (ImageView) ((Activity) context).findViewById(R.id.a_chat_ic_scroll);

        if (isLeave != null && isLeave) {
            ((RelativeLayout.LayoutParams) mImageScrollView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        mImageScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
                checkImageScroll(mImageScrollView, 0);
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (view instanceof PhotoMsgView) {
                    try {
                        PhotoMsg chatMessage = (PhotoMsg) mChatMessageList.get(position);
                        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) chatMessage.tgMessage.message;
                        TdApi.PhotoSize photoSize = messagePhoto.photo.photos[chatMessage.photoIndex];
                        if (FileUtil.isTDFileLocal(photoSize.photo)) {
                            Intent intent = new Intent(context, PhotoActivity.class);
                            intent.putExtra("filePath", photoSize.photo.path);
                            intent.putExtra("imageView", true);
                            intent.putExtra("msgId", chatMessage.tgMessage.id);
                            ((AppCompatActivity) context).startActivityForResult(intent, ChatActivity.OPEN_PHOTO);
                        }
                    } catch (Exception e) {
                        Log.e(LOG, "onItemClick", e);
                        Crashlytics.logException(e);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                try {
                    AbstractChatMsg abstractChatMessage = mChatMessageList.get(position);
                    if (abstractChatMessage instanceof AbstractMainMsg) {
                        AbstractMainMsg chatMessage = (AbstractMainMsg) abstractChatMessage;
                        if (AbstractChatMsg.Type.isUserMsg(chatMessage.type) && chatMessage.msgIcon != OutputMsgIconType.NOT_SEND) {
                            showDialog(chatMessage, context);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG, "onItemLongClick", e);
                    Crashlytics.logException(e);
                }
            }
        }));

        //mRecyclerView.scrollToPosition(scrollToPos);

        mChatAdapter = new ChatAdapter(getChatMessageList(), context);
        mRecyclerView.setAdapter(mChatAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int visibleItemCount;
            int totalItemCount;
            boolean isOnce = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        FileManager.sCanDownloadFile = true;
                        FileManager.getManager().tryToLoad(firstVisibleItem, lastVisibleItem);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
                readChatHistory();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int absDy = Math.abs(dy);

                if (absDy > 30) {
                    isOnce = true;
                    FileManager.sCanDownloadFile = false;
                } else {
                    FileManager.sCanDownloadFile = true;
                }

                firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();

                if (mNeedDownloadMore) {
                    visibleItemCount = mGridLayoutManager.getChildCount();
                    totalItemCount = mGridLayoutManager.getItemCount();
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount * 0.8) {
                        mNeedDownloadMore = false;
                        getChat(false);
                    }
                }

                if (FileManager.sCanDownloadFile && isOnce) {
                    isOnce = false;
                    //FileManager.getManager().tryToLoad(firstVisibleItem, lastVisibleItem);
                }

                checkImageScroll(mImageScrollView, firstVisibleItem);
            }
        });
    }

    public void checkImageScroll(final ImageView imageScroll, int firstVisibleItem) {
        if (isHaveChatId()) {
            if (firstVisibleItem > 10) {
                if (imageScroll.getVisibility() == View.GONE) {
                    imageScroll.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(imageScroll, View.ALPHA, 0.2f, 1.0f).setDuration(400).start();
                }
            } else {
                if (imageScroll.getVisibility() == View.VISIBLE) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(imageScroll, View.ALPHA, 1.0f, 0.2f);
                    animator.addListener(new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            imageScroll.setVisibility(View.GONE);
                        }
                    });
                    animator.setDuration(400).start();
                }
            }
        }
    }

    public List<AbstractChatMsg> getChatMessageList() {
        return mChatMessageList;
    }

    public void getChat(int offset, long chatId, ChatInfo currChatInfo, boolean isFirstGetChat, boolean... isNotSendRequest) {
        this.mChatOffset = offset;
        this.mChatId = chatId;
        this.mChatFromId = UserManager.getManager().getCurrUserId();
        this.mChatInfo = currChatInfo;
        this.mTempUnreadCount = currChatInfo.tgChatObject != null ? currChatInfo.tgChatObject.unreadCount : 0;
        getChat(isFirstGetChat, isNotSendRequest);
    }

    public void getChat(boolean isFirstGetChat, boolean... isNotSendRequest) {
        if (mChatOffset != null && this.mChatId != null) {
            this.mIsFirstGetChat = isFirstGetChat;
            TdApi.GetChatHistory func = new TdApi.GetChatHistory();
            if (this.mIsFirstGetChat) {
                func.limit = AndroidUtil.WINDOW_PORTRAIT_HEIGHT / AbstractMsgView.MIN_MSG_HEIGHT + 5;
            } else {
                func.limit = mChatLimit;
            }

            func.offset = this.mChatOffset;
            func.chatId = this.mChatId;
            func.fromId = this.mChatFromId;

            if (func.offset == 0) {
                this.mRemDate = 0;
                this.mChatMessageList.clear();
                this.mIndexPosIdMsgArray.clear();
                this.mIndexPosListUserArray.clear();
                this.mIndexNewMsgDeviation.set(0);
            }

            if (isNotSendRequest.length == 0 || !isNotSendRequest[0]) {
                mChatAdapter.setLoadingData();
                client().send(func, getManager());
            }
        }
    }

    public void createPrivateChat(int userId, ResultController resultController) {
        TdApi.CreatePrivateChat createPrivateChat = new TdApi.CreatePrivateChat();
        createPrivateChat.userId = userId;
        client().send(createPrivateChat, resultController);
    }

    public Integer getRealPos(int messageId) {
        Integer pos = getIndexPosIdByMsgId(messageId);
        if (pos != null) {
            int dev = mIndexNewMsgDeviation.get();
            return pos + dev * (-1);
        }
        return null;
    }

    public void notifyItemChangedByPosUI(final int pos) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (isHaveChatId() && mChatMessageList.size() > pos && mChatAdapter != null) {
                    mChatAdapter.notifyItemChanged(pos);
                }
            }
        });
    }

    public void updateMessageId(TdApi.UpdateMessageId updateMessageId) {
        if (updateMessageId != null && isSameChatId(updateMessageId.chatId)) {
            final Integer realPos = getRealPos(updateMessageId.oldId);
            if (realPos != null) {
                final AbstractMainMsg message = (AbstractMainMsg) mChatMessageList.get(realPos);
                message.tgMessage.id = updateMessageId.newId;

                int notRealPos = realPos - mIndexNewMsgDeviation.get() * (-1);
                //индексируем по новому Id
                addToIndexPosIdMsgArray(updateMessageId.newId, notRealPos);

                message.msgIcon = ChatHelper.getTypeOfOutputMsgIcon(mChatInfo.tgChatObject, updateMessageId.newId);
                AbstractMsgView.measureByOrientation(message);
                notifyItemChangedByPosUI(realPos);
            }
        }
    }

    public void updateMessageDate(TdApi.UpdateMessageDate updateMessageDate) {
        if (updateMessageDate != null && isSameChatId(updateMessageDate.chatId)) {
            final Integer realPos = getRealPos(updateMessageDate.messageId);
            if (realPos != null) {
                final AbstractMainMsg message = (AbstractMainMsg) mChatMessageList.get(realPos);
                message.tgMessage.date = updateMessageDate.newDate;
                message.date = generateDate(updateMessageDate.newDate);
                AbstractMsgView.measureByOrientation(message);
                notifyItemChangedByPosUI(realPos);
            }
        }
    }

    public void updateChatReadOutbox(TdApi.UpdateChatReadOutbox chatReadOutbox) {
        if (chatReadOutbox != null && isSameChatId(chatReadOutbox.chatId)) {
            //если последние сообщения оказались входящими непрочитанными
            if (chatReadOutbox.lastReadOutboxMessageId == 0) {
                return;
            }
            int dif = (chatReadOutbox.lastReadOutboxMessageId - mChatInfo.tgChatObject.lastReadOutboxMessageId);
            mChatInfo.tgChatObject.lastReadOutboxMessageId = chatReadOutbox.lastReadOutboxMessageId;
            try {
                if (dif >= mChatMessageList.size()) {
                    dif = mChatMessageList.size() - 1;
                }
                for (int i = 0; i < dif; i++) {
                    //todo небольшая оптимизация msgIcon у всех будет одинаковый + не оябзательно пробегаться по всем
                    AbstractChatMsg abstractChatMsg = mChatMessageList.get(i);
                    if (abstractChatMsg != null) {
                        if (abstractChatMsg instanceof AbstractMainMsg) {
                            AbstractMainMsg message = (AbstractMainMsg) abstractChatMsg;
                            message.msgIcon = ChatHelper.getTypeOfOutputMsgIcon(mChatInfo.tgChatObject, message.tgMessage.id);
                        }
                    }
                }
                notifySetDataChangedAsync();
            } catch (Exception e) {
                Log.e(LOG, "updateChatReadOutbox", e);
                Crashlytics.logException(e);
            }
        }
    }

    public void updateChatReadInbox(TdApi.UpdateChatReadInbox chatReadInbox) {
        if (chatReadInbox != null && isSameChatId(chatReadInbox.chatId)) {

            int dif = (chatReadInbox.lastReadInboxMessageId - mChatInfo.tgChatObject.lastReadInboxMessageId);
            mChatInfo.tgChatObject.lastReadInboxMessageId = chatReadInbox.lastReadInboxMessageId;
            //todo BUGG протестить
            if (mChatInfo.tgChatObject.lastReadInboxMessageId != 0
                    && mChatInfo.tgChatObject.lastReadOutboxMessageId != 0
                    && mChatInfo.tgChatObject.lastReadInboxMessageId >= mChatInfo.tgChatObject.lastReadOutboxMessageId) {
                mChatInfo.tgChatObject.unreadCount = mChatInfo.tgChatObject.lastReadInboxMessageId - mChatInfo.tgChatObject.lastReadOutboxMessageId;
            } else {
                if (mChatInfo.tgChatObject.lastReadInboxMessageId == 0 || mChatInfo.tgChatObject.lastReadOutboxMessageId == 0) {

                } else {
                    mChatInfo.tgChatObject.unreadCount = chatReadInbox.unreadCount;
                    dif = chatReadInbox.unreadCount;
                }
            }

            if (chatReadInbox.lastReadInboxMessageId == 0) {
                return;
            }
            try {
                if (dif >= mChatMessageList.size()) {
                    dif = mChatMessageList.size() - 1;
                }

                for (int i = 0; i < dif; i++) {
                    AbstractChatMsg abstractChatMsg = mChatMessageList.get(i);
                    if (abstractChatMsg != null) {
                        if (abstractChatMsg instanceof AbstractMainMsg) {
                            AbstractMainMsg message = (AbstractMainMsg) abstractChatMsg;
                            message.msgIcon = ChatHelper.getTypeOfOutputMsgIcon(mChatInfo.tgChatObject, message.tgMessage.id);
                        }
                    }
                }
                notifySetDataChangedAsync();
            } catch (Exception e) {
                Log.e(LOG, "updateChatReadInbox", e);
                Crashlytics.logException(e);
            }
        }
    }

    public void addToIndexPosIdMsgArray(int msgId, int pos) {
        synchronized (this.mIndexPosIdMsgArray) {
            this.mIndexPosIdMsgArray.put(msgId, pos);
        }
    }

    public void addToIndexPosListUserArray(int userId, int pos) {
        synchronized (mIndexPosListUserArray) {
            List<Integer> posList = mIndexPosListUserArray.get(userId);
            if (posList == null) {
                posList = new ArrayList<Integer>();
            }
            posList.add(pos);
            mIndexPosListUserArray.put(userId, posList);
        }
    }

    public Integer getIndexPosIdByMsgId(int msgId) {
        synchronized (mIndexPosIdMsgArray) {
            if (mIndexPosIdMsgArray.indexOfKey(msgId) >= 0) {
                return mIndexPosIdMsgArray.get(msgId);
            } else {
                return null;
            }
        }
    }

    public List<Integer> getIndexPosListByUserId(int userId) {
        synchronized (this.mIndexPosListUserArray) {
            return this.mIndexPosListUserArray.get(userId);
        }
    }

    private static String generateDate(int date) {
        return DateUtil.getDateForChat(date, DateUtil.DateType.CHAT_MSG);
    }

    public void notifySetDataChanged() {
        if (isHaveChatId() && mChatAdapter != null)
            mChatAdapter.notifyDataSetChanged();
    }

    public void notifySetDataChangedAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                notifySetDataChanged();
            }
        });
    }

    public void changeChatTitle(String chatName, final ResultController resultController) {
        TdApi.ChangeChatTitle changeChatTitle = new TdApi.ChangeChatTitle();
        changeChatTitle.chatId = mChatId;
        changeChatTitle.title = chatName;
        client().send(changeChatTitle, resultController);
    }

    public void pressOnSameFiles(TdApi.MessageContent inputContent, int inputFileId, int inputPos) {
        for (int i = firstVisibleItem - 2; i <= lastVisibleItem + 2; i++) {
            if (i == inputPos) {
                continue;
            }
            View view = mGridLayoutManager.findViewByPosition(i);
            if (view instanceof AbstractMsgView) {

                AbstractMsgView abstractMsgView = (AbstractMsgView) view;
                TdApi.MessageContent content = abstractMsgView.record.tgMessage.message;

                Integer fileId = null;

                if (inputContent.getConstructor() == content.getConstructor()) {
                    switch (content.getConstructor()) {
                        case TdApi.MessageVoice.CONSTRUCTOR:
                            TdApi.MessageVoice messageVoice = (TdApi.MessageVoice) content;
                            fileId = messageVoice.voice.voice.id;
                            break;
                        case TdApi.MessageVideo.CONSTRUCTOR:
                            TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) content;
                            fileId = messageVideo.video.video.id;
                            break;
                        case TdApi.MessageDocument.CONSTRUCTOR:
                            TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) content;
                            fileId = messageDocument.document.document.id;
                            break;
                        case TdApi.MessageAudio.CONSTRUCTOR:
                            TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) content;
                            fileId = messageAudio.audio.audio.id;
                            break;
                    }
                }

                if (fileId != null && fileId == inputFileId) {
                    abstractMsgView.onViewClick(null, null, true);
                }
            }
        }
    }

    private AbstractChatMsg handleMsg(TdApi.Message message, int offset) {
        AbstractChatMsg abstractChatMessage = AbstractChatMsg.createEntry(message);
        abstractChatMessage.tgMessage = message;
        if (abstractChatMessage instanceof AbstractMainMsg) {
            AbstractMainMsg chatMessage = (AbstractMainMsg) abstractChatMessage;
            chatMessage.date = generateDate(message.date);
            //chatMessage.tgMessage = message;
            UserManager userManager = UserManager.getManager();
            chatMessage.cachedUser = userManager.getUserByIdWithRequestAsync(message.fromId);
            chatMessage.msgIcon = ChatHelper.getTypeOfOutputMsgIcon(mChatInfo.tgChatObject, message.id);
            chatMessage.isForward = !(message.forwardFromId <= 0);

            if (chatMessage.isForward) {
                chatMessage.forwardDate = generateDate(message.forwardDate);
                chatMessage.cachedForwardUser = userManager.getUserByIdWithRequestAsync(message.forwardFromId);
            }
        }
        //индексация
        addToIndexPosIdMsgArray(message.id, offset);
        addToIndexPosListUserArray(message.fromId, offset);
        initContent(message.message, abstractChatMessage);
        return abstractChatMessage;
    }

    private void initContent(TdApi.MessageContent message, AbstractChatMsg abstractChatMessage) {

        BotManager.getManager().buildKeyBoard(abstractChatMessage.tgMessage, false);
        UserManager userManager = UserManager.getManager();
        switch (message.getConstructor()) {
            case TdApi.MessageText.CONSTRUCTOR: {
                TextMsg chatMessage = (TextMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_TEXT;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_TEXT;

                TextMsgView.measureText(chatMessage, message);
                break;
            }
            case TdApi.MessageVoice.CONSTRUCTOR: {
                VoiceMsg chatMessage = (VoiceMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.text = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.voice));
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_VOICE;
                    //TextMsgView.measureMedia(chatMessage, message);
                    VoiceMsgView.measure(chatMessage, message);
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_VOICE;
                    VoiceMsgView.measure(chatMessage, message);
                }
                break;
            }
            case TdApi.MessageAudio.CONSTRUCTOR: {
                AudioMsg chatMessage = (AudioMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_AUDIO;
                    AudioMsgView.measure(chatMessage, message);
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_AUDIO;
                    AudioMsgView.measure(chatMessage, message);
                }
                break;
            }
            case TdApi.MessageDocument.CONSTRUCTOR: {
                DocumentMsg chatMessage = (DocumentMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_DOCUMENT;
                    TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) message;
                    chatMessage.text = new SpannableString(ChatHelper.getFileName(messageDocument.document.fileName));
                    //TextMsgView.measureMedia(chatMessage, message);
                    DocumentMsgView.measure(chatMessage, message);
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_DOCUMENT;
                    DocumentMsgView.measure(chatMessage, message);
                }
                break;
            }
            case TdApi.MessageSticker.CONSTRUCTOR: {
                StickerMsg chatMessage = (StickerMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_STICKER;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_STICKER;
                StickerMsgView.measure(chatMessage, message);
                break;
            }
            case TdApi.MessagePhoto.CONSTRUCTOR: {
                PhotoMsg chatMessage = (PhotoMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_PHOTO;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_PHOTO;
                PhotoMsgView.measure(chatMessage, message);
                break;
            }
            case TdApi.MessageVideo.CONSTRUCTOR: {
                VideoMsg chatMessage = (VideoMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.text = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.video));
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_VIDEO;
                    //TextMsgView.measureMedia(chatMessage, message);
                    VideoMsgView.measure(chatMessage, message);
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_VIDEO;
                    VideoMsgView.measure(chatMessage, message);
                }
                break;
            }
            case TdApi.MessageLocation.CONSTRUCTOR: {
                GeoMsg chatMessage = (GeoMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_GEO;
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_GEO;
                }
                GeoMsgView.measure(chatMessage);
                break;
            }
            case TdApi.MessageVenue.CONSTRUCTOR: {
                //TODO новый тип сообщений(пока будет как geo)
                /*TextMsg chatMessage = (TextMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_TEXT;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_TEXT;

                TextMsgView.measureVenue(chatMessage, message);*/

                GeoMsg chatMessage = (GeoMsg) abstractChatMessage;
                if (chatMessage.isForward) {
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_GEO;
                } else {
                    chatMessage.type = AbstractChatMsg.Type.MSG_GEO;
                }
                GeoMsgView.measure(chatMessage);
                break;
            }
            case TdApi.MessageWebPage.CONSTRUCTOR: {
                //TODO новый тип сообщений(Пока будет как текст)
                TextMsg chatMessage = (TextMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_FORWARD_TEXT;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_TEXT;

                TextMsgView.measureWebPage(chatMessage, message);
                break;
            }

            case TdApi.MessageContact.CONSTRUCTOR: {
                ContactMsg chatMessage = (ContactMsg) abstractChatMessage;
                if (chatMessage.isForward)
                    chatMessage.type = AbstractChatMsg.Type.MSG_CONTACT_FORWARD;
                else
                    chatMessage.type = AbstractChatMsg.Type.MSG_CONTACT;
                // TextMsgView.measureContact(chatMessage, message);
                ContactMsgView.measure(chatMessage, message);
                break;
            }
            case TdApi.MessageGroupChatCreate.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageGroupChatCreate messageGroupChatCreate = (TdApi.MessageGroupChatCreate) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;
                try {
                    CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                    String who = "";

                    if (cachedUser.tgUser.id == userManager.getCurrUserId()) {
                        who += AndroidUtil.getResourceString(R.string.you);
                    } else {
                        who += cachedUser.fullName;
                    }

                    if (TextUtil.isNotBlank(who)) {
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(who, AndroidUtil.getResourceString(R.string._created_the_group_), messageGroupChatCreate.title);
                    } else {
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.created_the_group_), messageGroupChatCreate.title);
                    }

                } catch (Exception e) {
                    Log.e(LOG, "MessageGroupChatCreate", e);
                    Crashlytics.logException(e);
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.created_the_group_), messageGroupChatCreate.title);
                }

                break;
            }

            case TdApi.MessageChatJoinByLink.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageChatJoinByLink messageChatJoinByLink = (TdApi.MessageChatJoinByLink) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;
                //info Сообщения о вступлении в группу по ссылке теперь являются отдельным типом сообщений.

                try {
                    CachedUser inviter = userManager.getUserByIdWithRequestAsync(messageChatJoinByLink.inviterId);
                    CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(inviter.fullName, AndroidUtil.getResourceString(R.string._invite_), who.fullName);
                } catch (Exception e) {
                    Log.e(LOG, "MessageChatJoinByLink", e);
                    Crashlytics.logException(e);
                    chatMessage.sys_msg = new SpannableString(AndroidUtil.getResourceString(R.string.new_user_added_in_chat));
                }

                break;
            }
            case TdApi.MessageChatChangeTitle.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageChatChangeTitle chatChangeTitle = (TdApi.MessageChatChangeTitle) message;
                CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;

                if (TextUtil.isNotBlank(who.fullName)) {
                    Integer currId = userManager.getCurrUserId();
                    //я изменил
                    if (currId != null && who.tgUser.id == currId)
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                AndroidUtil.getResourceString(R.string._changed_group_name_to_), chatChangeTitle.title);
                    else
                        //кто-то изменил
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(who.fullName,
                                AndroidUtil.getResourceString(R.string._changed_group_name_to_), chatChangeTitle.title);
                } else
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.new_group_name), chatChangeTitle.title);
                break;
            }
            case TdApi.MessageChatDeletePhoto.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                TdApi.MessageChatDeletePhoto chatDeletePhoto = (TdApi.MessageChatDeletePhoto) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;

                if (TextUtil.isNotBlank(who.fullName)) {
                    Integer currId = userManager.getCurrUserId();
                    //я удалил
                    if (currId != null && who.tgUser.id == currId)
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                AndroidUtil.getResourceString(R.string.deleted_chat_photo), null);
                    else
                        //кто-то удалил
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(who.fullName,
                                AndroidUtil.getResourceString(R.string.deleted_chat_photo), null);
                } else
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.chat_photo_was_deleted), null);
                break;
            }
            case TdApi.MessageChatAddParticipant.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageChatAddParticipant chatAddParticipant = (TdApi.MessageChatAddParticipant) message;
                CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;

                if (TextUtil.isNotBlank(who.fullName)) {
                    Integer currId = userManager.getCurrUserId();
                    //я пригласил
                    if (currId != null && who.tgUser.id == currId)
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                AndroidUtil.getResourceString(R.string._added_new_user),
                                chatAddParticipant.user.firstName + " " + chatAddParticipant.user.lastName);
                    else
                        //кто-то пригласил
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(who.fullName,
                                AndroidUtil.getResourceString(R.string._added_new_user),
                                chatAddParticipant.user.firstName + " " + chatAddParticipant.user.lastName);
                } else
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.added_new_user),
                            chatAddParticipant.user.firstName + " " + chatAddParticipant.user.lastName);

                UserManager.getManager().insertUserInCache(chatAddParticipant.user);
                break;
            }
            case TdApi.MessageChatDeleteParticipant.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                TdApi.MessageChatDeleteParticipant deleteParticipant = (TdApi.MessageChatDeleteParticipant) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;

                if (TextUtil.isNotBlank(who.fullName)) {
                    //сам ушел
                    if (who.tgUser.id == deleteParticipant.user.id) {
                        Integer currId = userManager.getCurrUserId();
                        //я ушел
                        if (currId != null && who.tgUser.id == currId)
                            chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                    AndroidUtil.getResourceString(R.string.left_group), null);
                        else
                            chatMessage.sys_msg = ChatHelper.getSpanSystemText(
                                    deleteParticipant.user.firstName + " " + deleteParticipant.user.lastName,
                                    AndroidUtil.getResourceString(R.string.left_group), null);
                    } else {
                        //помогли уйти
                        Integer currId = userManager.getCurrUserId();
                        if (currId != null && who.tgUser.id == currId)
                            chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                    AndroidUtil.getResourceString(R.string.removed), deleteParticipant.user.firstName + " " + deleteParticipant.user.lastName);
                        else
                            chatMessage.sys_msg = ChatHelper.getSpanSystemText(who.fullName,
                                    AndroidUtil.getResourceString(R.string.removed), deleteParticipant.user.firstName + " " + deleteParticipant.user.lastName);
                    }
                } else
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(
                            deleteParticipant.user.firstName + " " + deleteParticipant.user.lastName,
                            AndroidUtil.getResourceString(R.string.left_group), null);
                break;
            }
            case TdApi.MessageDeleted.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageDeleted messageDeleted = (TdApi.MessageDeleted) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;
                chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.message_was_deleted), null);
                break;
            }
            case TdApi.MessageUnsupported.CONSTRUCTOR: {
                SystemMsg chatMessage = (SystemMsg) abstractChatMessage;
                TdApi.MessageUnsupported messageUnsupported = (TdApi.MessageUnsupported) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_MSG;
                chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.unsupported_message), null);
                break;
            }
            case TdApi.MessageChatChangePhoto.CONSTRUCTOR: {
                ChangeIconTitleMsg chatMessage = (ChangeIconTitleMsg) abstractChatMessage;
                CachedUser who = userManager.getUserByIdWithRequestAsync(chatMessage.tgMessage.fromId);
                TdApi.MessageChatChangePhoto chatChangePhoto = (TdApi.MessageChatChangePhoto) message;
                chatMessage.type = AbstractChatMsg.Type.SYSTEM_CHANGE_TITLE_MSG;
                chatMessage.photo = chatChangePhoto.photo;

                if (TextUtil.isNotBlank(who.fullName)) {
                    Integer currId = userManager.getCurrUserId();
                    if (currId != null && who.tgUser.id == currId)
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(AndroidUtil.getResourceString(R.string.you),
                                AndroidUtil.getResourceString(R.string._changed_chat_photo), null);
                    else
                        chatMessage.sys_msg = ChatHelper.getSpanSystemText(who.fullName,
                                AndroidUtil.getResourceString(R.string._changed_chat_photo), null);
                } else
                    chatMessage.sys_msg = ChatHelper.getSpanSystemText(null, AndroidUtil.getResourceString(R.string.chat_photo_was_changed), null);
                break;
            }
        }
    }

    public void updateMsgContent(TdApi.UpdateMessageContent mc) {
        //находим сообщение и заменяем
        if (isHaveChatId()) {
            final Integer pos = getRealPos(mc.messageId);
            if (pos != null) {
                final AbstractChatMsg chatMessage = mChatMessageList.get(pos);
                /*if (chatMessage instanceof PhotoMsg) {
                    PhotoMsg photoMsg = (PhotoMsg) chatMessage;
                    TdApi.MessagePhoto messagePhoto = ((TdApi.MessagePhoto) photoMsg.tgMessage.message);
                    //info не обновляем а то будет прыгать либо качнуть картинку сначало
                } else {*/
                chatMessage.tgMessage.message = mc.newContent;
                initContent(chatMessage.tgMessage.message, chatMessage);
                notifyItemChangedByPosUI(pos);
            }
        }
    }

    public synchronized void addMyMsg(final TdApi.Message message) {
        if (message != null && isSameChatId(message.chatId)) {
            setVisibilityNoMsges(View.GONE);
            sIsNeedRemoveChat = false;

            final DateDivider dateDivider;
            TdApi.Message tgMessage = null;
            if (!mChatMessageList.isEmpty()) {
                tgMessage = mChatMessageList.get(0).tgMessage;
            }

            if (mChatMessageList.isEmpty() || tgMessage == null || DateUtil.isDifDates(tgMessage.date, message.date)) {
                dateDivider = new DateDivider();
                dateDivider.type = AbstractChatMsg.Type.DATE_DIVIDER;
                dateDivider.date_divider = DateUtil.getDateForChat(message.date, DateUtil.DateType.CHAT_SEPARATOR);
                mIndexNewMsgDeviation.decrementAndGet();
            } else dateDivider = null;
            this.mChatOffset += 1;

            final AbstractChatMsg newChatMessage = handleMsg(message, mIndexNewMsgDeviation.decrementAndGet());

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            AndroidUtil.runInUI(new Runnable() {
                @Override
                public void run() {
                    if (isHaveChatId()) {
                        if (dateDivider != null) {
                            mChatMessageList.add(0, dateDivider);
                            mChatMessageList.add(0, newChatMessage);
                            mChatAdapter.notifyItemRangeInserted(0, 2);
                        } else {
                            mChatMessageList.add(0, newChatMessage);
                            mChatAdapter.notifyItemInserted(0);
                        }

                        if (firstVisibleItem <= 1) {
                            mRecyclerView.scrollToPosition(0);
                            checkImageScroll(mImageScrollView, 0);
                        }
                    }
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public boolean addReceivedMsg(final TdApi.Message message) {
        boolean returned = true;
        if (this.mChatInfo != null) {
            returned = false;
            this.mChatInfo.tgChatObject.unreadCount++;
            addMyMsg(message);
        }
        return returned;
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public boolean isEmptyMessageList() {
        return mChatMessageList.size() == 0;
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

        switch (object.getConstructor()) {
            case TdApi.Ok.CONSTRUCTOR:
                if (TdApi.DeleteChatHistory.CONSTRUCTOR == calledConstructor) {
                    cleanHistory();
                }
                if (TdApi.DeleteChatParticipant.CONSTRUCTOR == calledConstructor) {
                    TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) mChatInfo.tgChatObject.type;
                    groupChatInfo.groupChat.left = true;
                    notifyObservers(new NotificationObject(NotificationObject.LEFT_CHAT, null));
                }
                break;

            case TdApi.Message.CONSTRUCTOR: {
                TdApi.Message message = (TdApi.Message) object;
                addMyMsg(message);
                ChatListManager.getManager().upAndChangeChat(message.chatId, message, false);
                break;
            }
            case TdApi.Messages.CONSTRUCTOR: {

                final int remChatOffset = this.mChatOffset;
                final TdApi.Messages messages = (TdApi.Messages) object;
                mNeedDownloadMore = messages.messages.length != 0;
                this.mChatOffset += messages.messages.length;
                final List<AbstractChatMsg> tempMessageList = new ArrayList<>(50);
                int counterNewDividers = 0;

                for (int i = 0; i < messages.messages.length; i++) {
                    TdApi.Message message = messages.messages[i];

                    if (mRemDate != 0 && DateUtil.isDifDates(mRemDate, message.date)) {
                        DateDivider dateDivider = new DateDivider();
                        dateDivider.type = AbstractChatMsg.Type.DATE_DIVIDER;
                        dateDivider.date_divider = DateUtil.getDateForChat(mRemDate, DateUtil.DateType.CHAT_SEPARATOR);
                        tempMessageList.add(dateDivider);
                        mDividersCounter++;
                        counterNewDividers++;
                    }

                    if (mTempUnreadCount > 5 && ((remChatOffset + i) == mTempUnreadCount)) {
                        MsgDivider msgDivider = new MsgDivider();
                        msgDivider.type = AbstractChatMsg.Type.NEW_MSG_DIVIDER;
                        msgDivider.msg_divider = mTempUnreadCount + AndroidUtil.getResourceString(R.string._new_messages);
                        tempMessageList.add(msgDivider);
                        mDividersCounter++;
                        counterNewDividers++;
                    }

                    mRemDate = message.date;
                    tempMessageList.add(handleMsg(message, remChatOffset + i + mDividersCounter));
                }

                //последнее сообщение в чате ставим дату
                if (messages.messages.length == 0 && mRemDate != 0) {
                    DateDivider dateDivider = new DateDivider();
                    dateDivider.type = AbstractChatMsg.Type.DATE_DIVIDER;
                    dateDivider.date_divider = DateUtil.getDateForChat(mRemDate, DateUtil.DateType.CHAT_SEPARATOR);
                    tempMessageList.add(dateDivider);
                    mDividersCounter++;
                    counterNewDividers++;
                    mRemDate = 0;
                }

                //если бот то самое последние сообщение - инфа о нем
                if (mChatInfo != null && mChatInfo.isBot && messages.messages.length == 0) {
                    BotDescriptionMsg botDescription = new BotDescriptionMsg();
                    botDescription.type = AbstractChatMsg.Type.MSG_BOT_DESCRIPTION;
                    CachedUser cachedUser = UserManager.getManager().getUserByIdWithRequestAsync(getCurrentChatId());
                    if (cachedUser != null && cachedUser.tgUser != null) {
                        botDescription.description = BotManager.getManager().linkifyDescription(((TdApi.BotInfoGeneral) cachedUser.botInfo));
                    } else {
                        botDescription.description = new SpannableString("");
                    }

                    if (mChatMessageList.size() == 0) {
                        botDescription.isFullScreen = true;
                    }

                    tempMessageList.add(botDescription);
                    mDividersCounter++;
                    counterNewDividers++;
                }

                if (calledConstructor == TdApi.GetChatHistory.CONSTRUCTOR) {
                    if (this.mIsFirstGetChat && messages.messages.length == 0 && mChatMessageList.size() == 0 && tempMessageList.size() == 0)
                        setVisibilityNoMsges(View.VISIBLE);


                    //final int newDividers = counterNewDividers;
                    final CountDownLatch countDownLatch = new CountDownLatch(1);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mChatAdapter != null)
                                mChatMessageList.addAll(tempMessageList);
                            if (mIsFirstGetChat) {
                                readChatHistory();
                            }
                            if (mChatAdapter != null)
                                mChatAdapter.updateDataAfterLoading();

                            if (mIsFirstGetChat) {
                                if (mChatInfo != null && mChatInfo.isBot && mChatMessageList.size() == 1)
                                    notifyObservers(new NotificationObject(NotificationObject.BOT_SHOW_START, null));
                                ChatManager.getManager().callAfterInitChatAction();
                            }

                            countDownLatch.countDown();
                        }
                    });
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        //
                    }
                }
                break;
            }
            case TdApi.Error.CONSTRUCTOR:
                if (mChatAdapter != null) {
                    mChatAdapter.updateDataAfterLoadingAsync();
                }
                break;
        }
    }
}
