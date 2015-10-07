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

    private static volatile BotManager botManager;
    private Pattern currentBotPattern;
    private List<BotCommand> botCommandList = new ArrayList<>();
    private List<BotCommand> botCommandListSearch = new ArrayList<>();
    private BotKeyboardPopup popupBotKeyboard;
    private BotCommandsAdapter botCommandsAdapter;
    private RelativeLayout mainLayout;
    private RelativeLayout keyboard;
    private volatile int replyMarkupMessageId;
    private volatile long chatId = 0;
    private volatile boolean isNeedGetGroupCommandList;
    private volatile boolean isGroup = false;

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void clean() {
        chatId = 0;
        replyMarkupMessageId = 0;
        keyboard = null;
        popupBotKeyboard = null;
        currentBotPattern = null;
        mainLayout = null;
        botCommandsAdapter = null;
        isNeedGetGroupCommandList = false;
        isGroup = false;
    }

    public void setPopupBotKeyboard(BotKeyboardPopup popupBotKeyboard, RelativeLayout mainLayout) {
        this.popupBotKeyboard = popupBotKeyboard;
        this.mainLayout = mainLayout;
    }

    public RelativeLayout getKeyboard() {
        return keyboard;
    }

    public static BotManager getManager() {
        if (botManager == null) {
            synchronized (BotManager.class) {
                if (botManager == null) {
                    botManager = new BotManager();
                }
            }
        }
        return botManager;
    }

    public void setAdapter(BotCommandsAdapter botCommandsAdapter) {
        this.botCommandsAdapter = botCommandsAdapter;
    }

    public List<BotCommand> getBotCommandListForSearch() {
        return botCommandListSearch;
    }

    public void findCommand(String text) {
        if (text.startsWith("/")) {
            botCommandListSearch.clear();
            for (int i = 0; i < botCommandList.size(); i++) {
                BotCommand botCommand = botCommandList.get(i);
                String textCommand = "/" + botCommand.tgBotCommand.command;
                if (textCommand.startsWith(text)) {
                    botCommandListSearch.add(botCommand);
                }
            }

            AndroidUtil.runInUI(new Runnable() {
                @Override
                public void run() {
                    if (botCommandsAdapter != null) {
                        botCommandsAdapter.notifyDataSetChanged();
                        if (botCommandListSearch.size() == 0) {
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
        replyMarkupMessageId = chatInfo.tgChatObject.replyMarkupMessageId;
        chatId = chatInfo.tgChatObject.id;
        botCommandList.clear();
        botCommandListSearch.clear();
        processInit(cachedUser);
    }

    public void prepareGroupBot(ChatInfo chatInfo) {
        isGroup = true;
        isNeedGetGroupCommandList = true;

        replyMarkupMessageId = chatInfo.tgChatObject.replyMarkupMessageId;
        chatId = chatInfo.tgChatObject.id;

        botCommandList.clear();
        botCommandListSearch.clear();
    }

    public void prepareGroupBotForUpdate() {
        isNeedGetGroupCommandList = true;
        botCommandList.clear();
        botCommandListSearch.clear();
    }

    public void initGroupBotStart(CachedUser cachedUser) {
        if (isNeedGetGroupCommandList) {
            processInit(cachedUser);
        }
    }

    public void getBotGroupInfoUpdate(final List<CachedUser> botUsers, final ChatInfo chatInfo) {
        if (chatInfo.tgChatObject.id == chatId) {
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
        isNeedGetGroupCommandList = false;
    }

    private void processInit(CachedUser cachedUser) {
        if (keyboard != null) {
            try {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        keyboard.removeAllViews();
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
            } catch (InterruptedException e) {
                Log.e(LOG, "", e);
            }
            keyboard = null;
        }

        //@class BotInfo @description Provides information about bot and command supported by him
        switch (cachedUser.botInfo.getConstructor()) {
            case TdApi.BotInfoEmpty.CONSTRUCTOR:
                //@description User is not a bot

                break;
            case TdApi.BotInfoGeneral.CONSTRUCTOR:
                //@description User is a bot @share_text Small bot description shown when sharing bot
                // @param_description Big description shown in user info page @commands List of commands cupported by bot
                TdApi.BotInfoGeneral botInfoGeneral = (TdApi.BotInfoGeneral) cachedUser.botInfo;
                createPatternAndCommandList(botInfoGeneral, cachedUser);
                break;
        }
    }

    public void createPatternAndCommandList(TdApi.BotInfoGeneral infoGeneral, CachedUser cachedUser) {
        if (currentBotPattern == null) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("/start|");
            stringBuilder.append("/help|");
            stringBuilder.append("/settings");

            for (TdApi.BotCommand command : infoGeneral.commands) {
                stringBuilder.append("|/").append(command.command);
                botCommandList.add(new BotCommand(cachedUser, command));
            }
            currentBotPattern = Pattern.compile(stringBuilder.toString());
        } else {
            StringBuilder stringBuilder = new StringBuilder(currentBotPattern.pattern());
            for (TdApi.BotCommand command : infoGeneral.commands) {
                stringBuilder.append("|/").append(command.command);
                botCommandList.add(new BotCommand(cachedUser, command));
            }
            currentBotPattern = Pattern.compile(stringBuilder.toString());
        }
        botCommandListSearch.addAll(botCommandList);
    }

    public SpannableStringBuilder linkifyDescription(TdApi.BotInfoGeneral infoGeneral) {
        SpannableStringBuilder builder = new SpannableStringBuilder(infoGeneral.description);
        ChatHelper.addLinks(builder, currentBotPattern, "botLinks");
        return builder;
    }

    public void linkifyMsg(SpannableStringBuilder builder) {
        if (currentBotPattern != null) {
            ChatHelper.addLinks(builder, Pattern.compile("(^|\\s)/\\w+"), "botHelpLinks");
            ChatHelper.addLinks(builder, currentBotPattern, "botLinks");
            //todo добавить действия по клику
        }
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public void buildKeyBoard(final TdApi.Message tgMessage, boolean fromUpdate) {

        if (chatId != 0) {
            if (replyMarkupMessageId == 0) {
                replyMarkupMessageId = -1;
                replyMarkUpNoneAsync();
            }

            if (replyMarkupMessageId == tgMessage.id) {
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        switch (tgMessage.replyMarkup.getConstructor()) {
                            case TdApi.ReplyMarkupShowKeyboard.CONSTRUCTOR:
                                //@description Contains custom keyboard layout for fast reply to bot @rows List of rows of bot commands
                                //@resize_keyboard Do clients need to resize keyboard
                                //@one_time Do clients need to hide keyboard after use
                                //@personal Keyboard is showed automatically only for mentioned users or replied to chat user,
                                // for incoming messages it is true if and only if keyboard needs to be automatically showed to current user
                                final TdApi.ReplyMarkupShowKeyboard replyMarkupShowKeyboard = (TdApi.ReplyMarkupShowKeyboard) tgMessage.replyMarkup;

                                Context context = App.getAppContext();
                                if (keyboard == null) {
                                    RelativeLayout.LayoutParams keyboardLP = new RelativeLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.MATCH_PARENT,
                                            RelativeLayout.LayoutParams.MATCH_PARENT);
                                    keyboard = new RelativeLayout(context);
                                    keyboard.setBackgroundColor(0xFFF5F6F7);
                                    keyboard.setLayoutParams(keyboardLP);
                                } else {
                                    keyboard.removeAllViews();
                                }

                                TableLayout table = new TableLayout(context);

                                table.setStretchAllColumns(true);
                                table.setShrinkAllColumns(true);

                                keyboard.addView(table);
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
                                if (keyboard != null) {
                                    keyboard.invalidate();
                                    if (popupBotKeyboard != null) {
                                        if (!isGroup) {
                                            if (popupBotKeyboard.isShowing()) {
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            } else {
                                                showKeyboardAsync();
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            }
                                        } else {
                                            if (popupBotKeyboard.isShowing()) {
                                                updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                            } else {
                                                updateIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                                            }
                                        }
                                    }
                                }
                                break;
                            case TdApi.ReplyMarkupForceReply.CONSTRUCTOR:
                                //@description Instruct clients to force reply to this message @personal Keyboard is showed automatically only for mentioned
                                // users or replied to chat user, for incoming messages it is true if and only if keyboard needs to be automatically showed to current user
                                final TdApi.ReplyMarkupForceReply replyMarkupForceReply = (TdApi.ReplyMarkupForceReply) tgMessage.replyMarkup;
                                //todo как-то заставить юзера ответить на сообщение(походу принудительно показать клаву)

                                if (replyMarkupForceReply.personal) {
                                    showKeyboardAsync();
                                    updateIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
                                }

                                break;
                            case TdApi.ReplyMarkupHideKeyboard.CONSTRUCTOR:
                                //@description Instruct clients to hide keyboard after receiving this message. This kind of keyboard can't be received.
                                // Instead UpdateChatReplyMarkup with message_id == 0 will be send
                                //@personal Keyboard is showed automatically only for mentioned users or replied to chat user,
                                // for incoming messages it is true if and only if keyboard needs to be automatically showed to current user
                                final TdApi.ReplyMarkupHideKeyboard replyMarkupHideKeyboard = (TdApi.ReplyMarkupHideKeyboard) tgMessage.replyMarkup;
                                //replyMarkupHideKeyboard.personal

                                if (replyMarkupHideKeyboard.personal) {
                                    closeKeyboard();
                                }
                                updateIconsVisibility(View.GONE, View.VISIBLE, View.GONE);
                                break;
                            case TdApi.ReplyMarkupNone.CONSTRUCTOR:
                                //@description Absent reply markup
                                replyMarkUpNone();
                                break;
                        }
                    }
                });
            } /*else if (fromUpdate) {
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        replyMarkUpNone();
                    }
                });

            }*/
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
        if (keyboard != null && !popupBotKeyboard.isShowing()) {
            if (popupBotKeyboard.isKeyBoardOpen()) {
                popupBotKeyboard.showAtBottom();
                mainLayout.setPadding(0, 0, 0, 0);
            } else {
                popupBotKeyboard.showAtBottomFirstTime();
                mainLayout.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
            }
        }
    }

    public void closeKeyboard() {
        if (keyboard != null && popupBotKeyboard.isShowing()) {
            popupBotKeyboard.dismiss();
            mainLayout.setPadding(0, 0, 0, 0);
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

    //@description Default chat reply markup has changed. It can happen because new message with reply markup has come or old reply markup was hidden by user
    //@chat_id Chat identifier @reply_markup_message_id Identifier of message from which reply markup need to be used or 0 if there is no default custom reply markup in the chat
    public void updateChatReplyMarkup(TdApi.UpdateChatReplyMarkup updateChatReplyMarkup) {
        //Logs.e("replyMarkupMessageIdИИИ=" + updateChatReplyMarkup.chatId + " " + chatId);
        //Logs.e("updateChatReplyMarkup=" + updateChatReplyMarkup.chatId + " == " + chatId);
        if (updateChatReplyMarkup.chatId == chatId) {
            replyMarkupMessageId = updateChatReplyMarkup.replyMarkupMessageId;
        }
        //Logs.e("updateChatReplyMarkup" + " " + updateChatReplyMarkup.replyMarkupMessageId);
        ChatListManager.getManager().updateReplyMarkupMessageId(updateChatReplyMarkup);

        ChatManager chatManager = ChatManager.getManager();
        if (chatManager.isSameChatId(updateChatReplyMarkup.chatId)) {
            final Integer realPos = chatManager.getRealPos(updateChatReplyMarkup.replyMarkupMessageId);
            if (realPos != null) {
                final AbstractMainMsg message = (AbstractMainMsg) chatManager.getChatMessageList().get(realPos);
                //Logs.e("replyMarkupMessageIdXXX" + updateChatReplyMarkup.replyMarkupMessageId + " " + message.tgMessage.id + " " + replyMarkupMessageId);
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

    //@description Deletes default reply markup from chat. This method needs to be called after one-time keyboard
    // or ForceReply reply markup has been used. UpdateChatReplyMarkup will be send if reply markup will be changed @chat_id Chat identifier
    //@message_id Message identifier of used keyboard
    public void deleteChatReplyMarkup() {
        //если был флаг one-time
        TdApi.DeleteChatReplyMarkup deleteChatReplyMarkup = new TdApi.DeleteChatReplyMarkup();
        deleteChatReplyMarkup.chatId = chatId;
        deleteChatReplyMarkup.messageId = replyMarkupMessageId;
        client().send(deleteChatReplyMarkup, this);
    }

    //@description Invites bot to a chat (if it is not in the chat sends start) and message to it.
    // Bot can't be invited in private chat other than chat with a bot @bot_user_id Identifier of the bot @chat_id Identifier of the chat @
    // parameter Hidden parameter sent to bot for deep linking (https://api.telegram.org/bots#deep-linking)
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
