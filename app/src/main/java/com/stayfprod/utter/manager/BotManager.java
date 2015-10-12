package com.stayfprod.utter.manager;


import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojiconHandler;
import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.AbstractChatMsg;
import com.stayfprod.utter.model.BotCommand;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.chat.AbstractMainMsg;
import com.stayfprod.utter.model.chat.BotDescriptionMsg;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.adapter.BotCommandsAdapter;
import com.stayfprod.utter.ui.component.BotKeyboardPopup;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class BotManager extends ResultController {

    private static final String LOG = "BotManager";

    private static volatile BotManager sBotManager;

    public static BotManager getManager() {
        if (sBotManager == null) {
            synchronized (BotManager.class) {
                if (sBotManager == null) {
                    sBotManager = new BotManager();
                }
            }
        }
        return sBotManager;
    }

    private Pattern mCurrentBotPattern;
    private List<BotCommand> mBotCommandList = new ArrayList<>();
    private List<BotCommand> mBotCommandListSearch = new ArrayList<>();
    private BotKeyboardPopup mPopupBotKeyboard;
    private BotCommandsAdapter mBotCommandsAdapter;
    private RelativeLayout mMainLayout;
    private RelativeLayout mKeyboard;
    private volatile int mReplyMarkupMessageId;
    private volatile long mChatId = 0;
    private volatile boolean mIsNeedGetGroupCommandList;
    private volatile boolean mIsGroup = false;

    public void setIsGroup(boolean isGroup) {
        this.mIsGroup = isGroup;
    }

    public void setChatId(long chatId) {
        this.mChatId = chatId;
    }

    public void clean() {
        mChatId = 0;
        mReplyMarkupMessageId = 0;
        mKeyboard = null;
        mPopupBotKeyboard = null;
        mCurrentBotPattern = null;
        mMainLayout = null;
        mBotCommandsAdapter = null;
        mIsNeedGetGroupCommandList = false;
        mIsGroup = false;
    }

    public void setPopupBotKeyboard(BotKeyboardPopup popupBotKeyboard, RelativeLayout mainLayout) {
        this.mPopupBotKeyboard = popupBotKeyboard;
        this.mMainLayout = mainLayout;
    }

    public RelativeLayout getKeyboard() {
        return mKeyboard;
    }

    public void setAdapter(BotCommandsAdapter botCommandsAdapter) {
        this.mBotCommandsAdapter = botCommandsAdapter;
    }

    public List<BotCommand> getBotCommandListForSearch() {
        return mBotCommandListSearch;
    }

    public void findCommand(String text) {
        if (text.startsWith("/")) {
            mBotCommandListSearch.clear();
            for (int i = 0; i < mBotCommandList.size(); i++) {
                BotCommand botCommand = mBotCommandList.get(i);
                String textCommand = "/" + botCommand.tgBotCommand.command;
                if (textCommand.startsWith(text)) {
                    mBotCommandListSearch.add(botCommand);
                }
            }

            AndroidUtil.runInUI(new Runnable() {
                @Override
                public void run() {
                    if (mBotCommandsAdapter != null) {
                        mBotCommandsAdapter.notifyDataSetChanged();
                        if (mBotCommandListSearch.size() == 0) {
                            hideCommandList();
                        } else {
                            showCommandList();
                        }
                    }
                }
            });
        }
    }

    public boolean isEmptyBotInfo(int id) {
        return UserManager.getManager().getUserByIdWithRequestAsync(id).botInfo.getConstructor() == TdApi.BotInfoEmpty.CONSTRUCTOR;
    }

    public boolean isEmptyBotInfo(CachedUser cachedUser) {
        return cachedUser.botInfo.getConstructor() == TdApi.BotInfoEmpty.CONSTRUCTOR;
    }

    public void showCommandList() {
        notifyObservers(new NotificationObject(NotificationObject.BOT_SHOW_COMMAND_LIST, null));
    }

    public void hideCommandListAndCleanEditText() {
        notifyObservers(new NotificationObject(NotificationObject.BOT_HIDE_COMMAND_LIST_AND_CLEAN_EDIT, null));
    }

    public void hideCommandList() {
        notifyObservers(new NotificationObject(NotificationObject.BOT_HIDE_COMMAND_LIST, null));
    }

    public void initBot(CachedUser cachedUser, ChatInfo chatInfo) {
        mReplyMarkupMessageId = chatInfo.tgChatObject.replyMarkupMessageId;
        mChatId = chatInfo.tgChatObject.id;
        mBotCommandList.clear();
        mBotCommandListSearch.clear();
        processInit(cachedUser);
    }

    public void prepareGroupBot(ChatInfo chatInfo) {
        mIsGroup = true;
        mIsNeedGetGroupCommandList = true;

        mReplyMarkupMessageId = chatInfo.tgChatObject.replyMarkupMessageId;
        mChatId = chatInfo.tgChatObject.id;

        mBotCommandList.clear();
        mBotCommandListSearch.clear();
    }

    public void prepareGroupBotForUpdate() {
        mIsNeedGetGroupCommandList = true;
        mBotCommandList.clear();
        mBotCommandListSearch.clear();
    }

    public void initGroupBotStart(CachedUser cachedUser) {
        if (mIsNeedGetGroupCommandList) {
            processInit(cachedUser);
        }
    }

    public void getBotGroupInfoUpdate(final List<CachedUser> botUsers, final ChatInfo chatInfo) {
        if (chatInfo.tgChatObject.id == mChatId) {
            getBotGroupInfo(botUsers, chatInfo, null);
        }
    }

    public void getBotGroupInfo(final List<CachedUser> botUsers, final ChatInfo chatInfo, final Runnable after) {
        ThreadService.runTaskChatForegroundExecutor(new Runnable() {
            @Override
            public void run() {
                if (after == null) {
                    prepareGroupBotForUpdate();
                } else {
                    prepareGroupBot(chatInfo);
                }

                for (int i = 0; i < botUsers.size(); i++) {
                    CachedUser botUser = botUsers.get(i);
                    if (BotManager.getManager().isEmptyBotInfo(botUser)) {
                        final CountDownLatch countDownLatch = new CountDownLatch(1);
                        UserManager.getManager().getUserFull(botUser.tgUser.id, new ResultController() {
                            @Override
                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                switch (object.getConstructor()) {
                                    case TdApi.UserFull.CONSTRUCTOR: {
                                        TdApi.UserFull userFull = (TdApi.UserFull) object;
                                        CachedUser cachedUser = UserManager.getManager().insertUserInCache(userFull);
                                        initGroupBotStart(cachedUser);
                                        break;
                                    }
                                }
                                countDownLatch.countDown();
                            }
                        });
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            Log.e(LOG, "botUsers", e);
                        }
                    } else {
                        initGroupBotStart(botUser);
                    }
                }
                initGroupBotFinish();
                if (after != null) {
                    after.run();
                }
            }
        });
    }

    public void initGroupBotFinish() {
        mIsNeedGetGroupCommandList = false;
    }

    private void processInit(CachedUser cachedUser) {
        if (mKeyboard != null) {
            try {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        mKeyboard.removeAllViews();
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
            } catch (InterruptedException e) {
                Log.e(LOG, "", e);
            }
            mKeyboard = null;
        }

        switch (cachedUser.botInfo.getConstructor()) {
            case TdApi.BotInfoEmpty.CONSTRUCTOR:

                break;
            case TdApi.BotInfoGeneral.CONSTRUCTOR:
                TdApi.BotInfoGeneral botInfoGeneral = (TdApi.BotInfoGeneral) cachedUser.botInfo;
                createPatternAndCommandList(botInfoGeneral, cachedUser);
                break;
        }
    }

    public void createPatternAndCommandList(TdApi.BotInfoGeneral infoGeneral, CachedUser cachedUser) {
        if (mCurrentBotPattern == null) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("/start|");
            stringBuilder.append("/help|");
            stringBuilder.append("/settings");

            for (TdApi.BotCommand command : infoGeneral.commands) {
                stringBuilder.append("|/").append(command.command);
                mBotCommandList.add(new BotCommand(cachedUser, command));
            }
            mCurrentBotPattern = Pattern.compile(stringBuilder.toString());
        } else {
            StringBuilder stringBuilder = new StringBuilder(mCurrentBotPattern.pattern());
            for (TdApi.BotCommand command : infoGeneral.commands) {
                stringBuilder.append("|/").append(command.command);
                mBotCommandList.add(new BotCommand(cachedUser, command));
            }
            mCurrentBotPattern = Pattern.compile(stringBuilder.toString());
        }
        mBotCommandListSearch.addAll(mBotCommandList);
    }

    public SpannableStringBuilder linkifyDescription(TdApi.BotInfoGeneral infoGeneral) {
        SpannableStringBuilder builder = new SpannableStringBuilder(infoGeneral.description);
        ChatHelper.addLinks(builder, mCurrentBotPattern, "botLinks");
        return builder;
    }

    public void linkifyMsg(SpannableStringBuilder builder) {
        if (mCurrentBotPattern != null) {
            ChatHelper.addLinks(builder, Pattern.compile("(^|\\s)/\\w+"), "botHelpLinks");
            ChatHelper.addLinks(builder, mCurrentBotPattern, "botLinks");
            //todo добавить действия по клику
        }
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public void buildKeyBoard(final TdApi.Message tgMessage, boolean fromUpdate) {

        if (mChatId != 0) {
            if (mReplyMarkupMessageId == 0) {
                mReplyMarkupMessageId = -1;
                replyMarkUpNoneAsync();
            }

            if (mReplyMarkupMessageId == tgMessage.id) {
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        switch (tgMessage.replyMarkup.getConstructor()) {
                            case TdApi.ReplyMarkupShowKeyboard.CONSTRUCTOR:
                                final TdApi.ReplyMarkupShowKeyboard replyMarkupShowKeyboard = (TdApi.ReplyMarkupShowKeyboard) tgMessage.replyMarkup;

                                Context context = App.getAppContext();
                                if (mKeyboard == null) {
                                    RelativeLayout.LayoutParams keyboardLP = new RelativeLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.MATCH_PARENT,
                                            RelativeLayout.LayoutParams.MATCH_PARENT);
                                    mKeyboard = new RelativeLayout(context);
                                    mKeyboard.setBackgroundColor(0xFFF5F6F7);
                                    mKeyboard.setLayoutParams(keyboardLP);
                                } else {
                                    mKeyboard.removeAllViews();
                                }

                                TableLayout table = new TableLayout(context);

                                table.setStretchAllColumns(true);
                                table.setShrinkAllColumns(true);

                                mKeyboard.addView(table);
                                RelativeLayout.LayoutParams tableLP = (RelativeLayout.LayoutParams) table.getLayoutParams();
                                tableLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                                tableLP.height = RelativeLayout.LayoutParams.MATCH_PARENT;

                                TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        1.0f);

                                View.OnClickListener onClickListener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        TextView textView = (TextView) v;
                                        ChatManager manager = ChatManager.getManager();
                                        manager.sendMessage(manager.createTextMsg(/*"/" +*/ textView.getText().toString()));
                                        if (replyMarkupShowKeyboard.oneTime) {
                                            BotManager.this.deleteChatReplyMarkup();
                                        }
                                    }
                                };

                                for (int i = 0; i < replyMarkupShowKeyboard.rows.length; i++) {
                                    int columnsInRow = replyMarkupShowKeyboard.rows[i].length;
                                    TableRow row = new TableRow(context);
                                    for (int j = 0; j < replyMarkupShowKeyboard.rows[i].length; j++) {
                                        String val = replyMarkupShowKeyboard.rows[i][j];
                                        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder(val);
                                        final TextView textView = new TextView(context);
                                        textView.setTextColor(0xff36474f);
                                        textView.setTextSize(16);
                                        textView.setGravity(Gravity.CENTER);
                                        textView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                                        EmojiconHandler.addEmojis(context, stringBuilder, Constant.DP_20, 0, -1, false);
                                        textView.setText(stringBuilder);

                                        textView.setBackgroundResource(R.drawable.bot_button);

                                        textView.setOnClickListener(onClickListener);

                                        TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0,
                                                ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                                        cellLp.setMargins(Constant.DP_5, Constant.DP_5, Constant.DP_5, Constant.DP_5);

                                        if (i == 0) {
                                            cellLp.topMargin = Constant.DP_16;
                                        }

                                        if (i == replyMarkupShowKeyboard.rows.length - 1) {
                                            cellLp.bottomMargin = Constant.DP_16;
                                        }

                                        if (j == 0) {
                                            cellLp.leftMargin = Constant.DP_16;
                                        }

                                        if (j == columnsInRow - 1) {
                                            cellLp.rightMargin = Constant.DP_16;
                                        }

                                        row.addView(textView, cellLp);
                                    }
                                    table.addView(row, rowLp);
                                }
                                if (mKeyboard != null) {
                                    mKeyboard.invalidate();
                                    if (mPopupBotKeyboard != null) {
                                        if (!mIsGroup) {
                                            if (mPopupBotKeyboard.isShowing()) {
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            } else {
                                                showKeyboardAsync();
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            }
                                        } else {
                                            if (mPopupBotKeyboard.isShowing()) {
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            } else {
                                                updateIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                                            }
                                        }
                                    }
                                }
                                break;
                            case TdApi.ReplyMarkupForceReply.CONSTRUCTOR:
                                final TdApi.ReplyMarkupForceReply replyMarkupForceReply = (TdApi.ReplyMarkupForceReply) tgMessage.replyMarkup;
                                //todo как-то заставить юзера ответить на сообщение(походу принудительно показать клаву)

                                if (replyMarkupForceReply.personal) {
                                    showKeyboardAsync();
                                    updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                }

                                break;
                            case TdApi.ReplyMarkupHideKeyboard.CONSTRUCTOR:
                                final TdApi.ReplyMarkupHideKeyboard replyMarkupHideKeyboard = (TdApi.ReplyMarkupHideKeyboard) tgMessage.replyMarkup;

                                if (replyMarkupHideKeyboard.personal) {
                                    closeKeyboard();
                                }
                                updateIconsVisibility(View.GONE, View.VISIBLE, View.GONE);
                                break;
                            case TdApi.ReplyMarkupNone.CONSTRUCTOR:
                                replyMarkUpNone();
                                break;
                        }
                    }
                });
            }
        }
    }

    public void showKeyboardAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                try {
                    showKeyboard();
                } catch (Exception e) {
                    Log.e(LOG, "", e);
                    Crashlytics.logException(e);
                }
            }
        }, 150);
    }

    public void showKeyboard() {
        if (mKeyboard != null && !mPopupBotKeyboard.isShowing()) {
            if (mPopupBotKeyboard.isKeyBoardOpen()) {
                mPopupBotKeyboard.showAtBottom();
                mMainLayout.setPadding(0, 0, 0, 0);
            } else {
                mPopupBotKeyboard.showAtBottomFirstTime();
                mMainLayout.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
            }
        }
    }

    public void closeKeyboard() {
        if (mKeyboard != null && mPopupBotKeyboard.isShowing()) {
            mPopupBotKeyboard.dismiss();
            mMainLayout.setPadding(0, 0, 0, 0);
        }
    }

    public void replyMarkUpNoneAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                replyMarkUpNone();
            }
        });
    }

    public void replyMarkUpNone() {
        closeKeyboard();
        updateIconsVisibility(View.GONE, View.VISIBLE, View.GONE);
    }

    public void updateIconsVisibility(int command, int slash, int panelKb) {
        notifyObservers(new NotificationObject(NotificationObject.BOT_CHANGE_ICON_VISIBILITY, new int[]{command, slash, panelKb}));
    }

    public void updateChatReplyMarkup(TdApi.UpdateChatReplyMarkup updateChatReplyMarkup) {
        if (updateChatReplyMarkup.chatId == mChatId) {
            mReplyMarkupMessageId = updateChatReplyMarkup.replyMarkupMessageId;
        }

        ChatListManager.getManager().updateReplyMarkupMessageId(updateChatReplyMarkup);

        ChatManager chatManager = ChatManager.getManager();
        if (chatManager.isSameChatId(updateChatReplyMarkup.chatId)) {
            final Integer realPos = chatManager.getRealPos(updateChatReplyMarkup.replyMarkupMessageId);
            if (realPos != null) {
                final AbstractMainMsg message = (AbstractMainMsg) chatManager.getChatMessageList().get(realPos);
                //info необходимо выполнить этот блок после инициализации группы бота в runTaskChatBackground!!
                ThreadService.runTaskChatBackground(new Runnable() {
                    @Override
                    public void run() {
                        buildKeyBoard(message.tgMessage, true);
                    }
                });
            }
        }
    }

    public void deleteChatReplyMarkup() {
        TdApi.DeleteChatReplyMarkup deleteChatReplyMarkup = new TdApi.DeleteChatReplyMarkup();
        deleteChatReplyMarkup.chatId = mChatId;
        deleteChatReplyMarkup.messageId = mReplyMarkupMessageId;
        client().send(deleteChatReplyMarkup, this);
    }

    public void sendBotStartMessage(Long chatId, ResultController resultController) {
        TdApi.SendBotStartMessage sendBotStartMessage = new TdApi.SendBotStartMessage();
        if (chatId != null) {
            sendBotStartMessage.chatId = chatId;
            sendBotStartMessage.botUserId = chatId.intValue();
            sendBotStartMessage.parameter = "" + System.currentTimeMillis();
            client().send(sendBotStartMessage, resultController);
        }
    }

    public void sendBotStartMessage(Long chatId, int botUserId, ResultController resultController) {
        TdApi.SendBotStartMessage sendBotStartMessage = new TdApi.SendBotStartMessage();
        if (chatId != null) {
            sendBotStartMessage.chatId = chatId;
            sendBotStartMessage.botUserId = botUserId;
            sendBotStartMessage.parameter = "" + System.currentTimeMillis();
            client().send(sendBotStartMessage, resultController);
        }
    }

    public void sendBotStartMessage(Long chatId) {
        sendBotStartMessage(chatId, ChatManager.getManager());
    }

    public void sendBotStartMessage(Long chatId, Integer botUserId) {
        if (botUserId != null) {
            sendBotStartMessage(chatId, botUserId, ChatManager.getManager());
        }
    }

    public void changeSizeOfLastBotMsg(boolean val) {
        ChatManager chatManager = ChatManager.getManager();
        List<AbstractChatMsg> abstractChatMsgs = chatManager.getChatMessageList();
        if (abstractChatMsgs != null && !abstractChatMsgs.isEmpty()) {
            AbstractChatMsg abstractChatMsg = abstractChatMsgs.get(abstractChatMsgs.size() - 1);
            if (abstractChatMsg instanceof BotDescriptionMsg) {
                BotDescriptionMsg botDescriptionMsg = (BotDescriptionMsg) abstractChatMsg;
                botDescriptionMsg.isFullScreen = val;
                chatManager.notifySetDataChanged();
            }
        }
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

    }
}
