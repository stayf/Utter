package com.stayfprod.utter.manager;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.Connection;
import com.stayfprod.utter.service.ConnectionChangeListener;
import com.stayfprod.utter.service.IconFactory;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.view.DetermineProgressView;
import com.stayfprod.utter.ui.view.IconUpdatable;
import com.stayfprod.utter.ui.view.MediaView;
import com.stayfprod.utter.ui.view.StickerThumbView;
import com.stayfprod.utter.ui.view.chat.AbstractMsgView;
import com.stayfprod.utter.ui.view.chat.AudioMsgView;
import com.stayfprod.utter.ui.view.chat.ContactMsgView;
import com.stayfprod.utter.ui.view.chat.VoiceMsgView;
import com.stayfprod.utter.ui.view.chat.DocumentMsgView;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.ui.view.chat.PhotoMsgView;
import com.stayfprod.utter.ui.view.chat.StickerMsgView;
import com.stayfprod.utter.ui.view.chat.VideoMsgView;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.Logs;
import com.stayfprod.utter.util.TextUtil;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.ChatHelper;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class UpdateHandler extends ResultController {
    private static final String LOG = UpdateHandler.class.getSimpleName();

    private static volatile UpdateHandler updateHandler;

    @Override
    public boolean hasChanged() {
        return true;
    }

    public static void destroy() {
        updateHandler = null;
    }

    public static UpdateHandler getHandler() {
        if (updateHandler == null) {
            synchronized (ChatListManager.class) {
                if (updateHandler == null) {
                    updateHandler = new UpdateHandler();
                }
            }
        }
        return updateHandler;
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        try {
            Log.i("UpdateHandlerOnResult", object.getConstructor() + " - " + object.getClass());
            if (!(App.getAppContext() instanceof AppCompatActivity)) {
                return;
            }
            switch (object.getConstructor()) {
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChatPhoto = (TdApi.UpdateChatPhoto) object;
                    if (FileUtils.isTDFileEmpty(updateChatPhoto.photo.small)) {
                        FileManager.getManager().uploadFile(FileManager.TypeLoad.UPDATE_CHAT_PHOTO, new Object[]{updateChatPhoto}, updateChatPhoto.photo.small.id, -1, -1);
                    } else {
                        ChatListManager.getManager().updateChatPhoto(updateChatPhoto);
                        notifyObservers(new NotificationObject(NotificationObject.UPDATE_CHAT_PHOTO, updateChatPhoto));
                    }
                    break;
                }
                case TdApi.UpdateNewAuthorization.CONSTRUCTOR: {
                    //TODO залогинелся с другого девайса
                    TdApi.UpdateNewAuthorization updateNewAuthorization = (TdApi.UpdateNewAuthorization) object;
                    //updateNewAuthorization.
                    break;
                }
                case TdApi.UpdateUserBlocked.CONSTRUCTOR: {
                    //TODO можно обновить в чате и в профиле меню
                    //Is user blacklisted by current user
                    TdApi.UpdateUserBlocked updateUserBlocked = (TdApi.UpdateUserBlocked) object;
                    UserManager userManager = UserManager.getManager();
                    CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(updateUserBlocked.userId);
                    cachedUser.isBlocked = updateUserBlocked.isBlocked;
                    //info информация о блокировке юзера в UserFull
                    break;
                }
                case TdApi.UpdateUserAction.CONSTRUCTOR: {
                    TdApi.UpdateUserAction updateUserAction = (TdApi.UpdateUserAction) object;
                    //TODO вся активность в чате
                    switch (updateUserAction.action.getConstructor()) {
                        //@description User typing message
                        case TdApi.SendMessageTypingAction.CONSTRUCTOR:
                            TdApi.SendMessageTypingAction sendMessageTypingAction = (TdApi.SendMessageTypingAction) updateUserAction.action;

                            break;
                        //@description User cancels typing
                        case TdApi.SendMessageCancelAction.CONSTRUCTOR:

                            break;
                        //@description User records a video
                        case TdApi.SendMessageRecordVideoAction.CONSTRUCTOR:

                            break;
                        //@description User uploads a video
                        case TdApi.SendMessageUploadVideoAction.CONSTRUCTOR:

                            break;
                        //@description User records an audio
                        case TdApi.SendMessageRecordVoiceAction.CONSTRUCTOR:

                            break;
                        //@description User uploads an audio
                        case TdApi.SendMessageUploadVoiceAction.CONSTRUCTOR:

                            break;
                        //@description User uploads a photo
                        case TdApi.SendMessageUploadPhotoAction.CONSTRUCTOR:

                            break;
                        //@description User uploads a document
                        case TdApi.SendMessageUploadDocumentAction.CONSTRUCTOR:

                            break;
                        //@description User sends geolocation
                        case TdApi.SendMessageGeoLocationAction.CONSTRUCTOR:

                            break;
                        //@description User chooses contact to send
                        case TdApi.SendMessageChooseContactAction.CONSTRUCTOR:

                            break;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChatReplyMarkup = (TdApi.UpdateChatReplyMarkup) object;
                    BotManager.getManager().updateChatReplyMarkup(updateChatReplyMarkup);
                    break;
                }
                case TdApi.UpdateMessageSendFailed.CONSTRUCTOR: {
                    TdApi.UpdateMessageSendFailed updateMessageSendFailed = (TdApi.UpdateMessageSendFailed) object;
                    AndroidUtil.showToastLong(updateMessageSendFailed.errorCode + ":" + updateMessageSendFailed.errorDescription);
                    break;
                }
                case TdApi.UpdateNewMessage.CONSTRUCTOR: {
                    TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
                    boolean upUnread = ChatManager.getManager().addReceivedMsg(updateNewMessage.message);
                    ChatListManager.getManager().upAndChangeChat(updateNewMessage.message.chatId, updateNewMessage.message, upUnread);
                    break;
                }
                case TdApi.UpdateOption.CONSTRUCTOR: {
                    TdApi.UpdateOption update = (TdApi.UpdateOption) object;
                    Log.d("UpdateHandler.option", "name=" + update.name + "; value= " + update.value);
                    OptionManager.getManager().setUpdateOption(update);
                    switch (update.value.getConstructor()) {
                        case TdApi.OptionBoolean.CONSTRUCTOR:
                            if (update.name.equals(OptionManager.OptionType.network_unreachable.name())) {
                                TdApi.OptionBoolean optionBoolean = (TdApi.OptionBoolean) update.value;
                                if (optionBoolean.value) {
                                    Connection.currentState = Connection.State.Waiting_for_network;
                                    Connection.isConnected = false;
                                    notifyObservers(new NotificationObject(NotificationObject.CHANGE_CONNECTION, Connection.currentState.text));
                                }
                            }
                            break;
                        case TdApi.OptionEmpty.CONSTRUCTOR:
                            TdApi.OptionEmpty optionEmpty = (TdApi.OptionEmpty) update.value;
                            if (update.name.equals(OptionManager.OptionType.my_id.name())) {
                                AndroidUtil.runInUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            StickerManager.getManager().destroy();
                                            ChatListManager.getManager().destroy();
                                            new AuthManager(null, null).openAuthActivity((AppCompatActivity) App.getAppContext());
                                        } catch (Throwable e) {
                                            Crashlytics.logException(e);
                                        }
                                    }
                                });
                            }
                            break;
                        case TdApi.OptionInteger.CONSTRUCTOR:
                            TdApi.OptionInteger optionInteger = (TdApi.OptionInteger) update.value;
                            if (update.name.equals(OptionManager.OptionType.my_id.name())) {
                                UserManager manager = UserManager.getManager();
                                manager.setCurrUserId(optionInteger.value);
                            }
                            break;
                        case TdApi.OptionString.CONSTRUCTOR:
                            TdApi.OptionString optionString = (TdApi.OptionString) update.value;
                            if (update.name.equals(OptionManager.OptionType.connection_state.name())) {
                                Connection.State state = Connection.State.valueOf(optionString.value.trim().replaceAll("\\s", "_"));
                                Connection.currentState = state;
                                Connection.isConnected = state == Connection.State.Updating || state == Connection.State.Ready;
                                notifyObservers(new NotificationObject(NotificationObject.CHANGE_CONNECTION, Connection.currentState.text));

                                if (state == Connection.State.Connecting) {
                                    ConnectionChangeListener.checkConnectionByTimeout();
                                }
                            }
                            break;
                    }
                    break;
                }
                case TdApi.UpdateFileProgress.CONSTRUCTOR: {
                    TdApi.UpdateFileProgress updateFileProgress = (TdApi.UpdateFileProgress) object;
                    FileManager fileManager = FileManager.getManager();
                    FileManager.StorageObject storageObject = fileManager.getStorageObject(updateFileProgress.fileId);
                    List<FileManager.StorageObject> additionStorageObjectList = fileManager.getAdditionStorageObject(updateFileProgress.fileId);

                    if (storageObject != null) {
                        final int size = updateFileProgress.size;
                        if (size != 0) {
                            final int ready = updateFileProgress.ready;//в байтах
                            final int x = 100 * ready / size;

                            switch (storageObject.typeLoad) {
                                case DOCUMENT:
                                    updateProcessFileDocument(storageObject, x, ready, size);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x, ready, size);
                                    break;
                                case PHOTO:
                                    updateProcessFilePhoto(storageObject, x);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x);
                                    break;
                                case VIDEO:
                                    updateProcessFileVideo(storageObject, x);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x);
                                    break;
                                case VOICE:
                                    updateProcessFileVoice(storageObject, x);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x);
                                    break;
                                case AUDIO:
                                    updateProcessFileAudio(storageObject, x, ready, size);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x, ready, size);
                                    break;
                                case SHARED_AUDIO:
                                case SHARED_AUDIO_PLAYER:
                                    updateProcessFileSharedAudio(storageObject, x, ready, size);
                                    updateProcessAdditionStorage(storageObject.typeLoad, additionStorageObjectList, x, ready, size);
                                    break;
                            }
                        }
                    }
                    break;
                }
                case TdApi.UpdateFile.CONSTRUCTOR: {
                    TdApi.UpdateFile updateFile = (TdApi.UpdateFile) object;
                    //Log.d("UpdateFile.", "name=" + updateFile.fileId + "==" + updateFile.path);
                    FileManager fileManager = FileManager.getManager();
                    FileManager.StorageObject storageObject = fileManager.getAndRemoveStorageObject(updateFile.file.id);
                    List<FileManager.StorageObject> additionStorageObjectList = fileManager.getAndRemoveAdditionStorageObject(updateFile.file.id);


                    final TdApi.File fileLocal;
                    if (storageObject != null) {
                        fileLocal = updateFile.file;
                        switch (storageObject.typeLoad) {
                            case SHARED_AUDIO:
                            case SHARED_AUDIO_PLAYER:
                                updateFileSharedAudio(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case SHARED_PHOTO:
                                updateFileSharedPhoto(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case SHARED_PHOTO_THUMB:
                                updateFileSharedPhotoThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case SHARED_VIDEO_THUMB:
                                updateFileSharedVideoThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CONTACT_ICON:
                                updateContactIcon(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case UPDATE_CHAT_PHOTO:
                                TdApi.UpdateChatPhoto updateChatPhoto = (TdApi.UpdateChatPhoto) storageObject.obj[0];
                                updateChatPhoto.photo.small = fileLocal;
                                ChatListManager.getManager().updateChatPhoto(updateChatPhoto);
                                notifyObservers(new NotificationObject(NotificationObject.UPDATE_CHAT_PHOTO, updateChatPhoto));
                                break;
                            case BOT_COMMAND_ICON:
                                updateIconBot(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CHANGE_CHAT_TITLE_IMAGE:
                                updateChangeTitleImage(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CHAT_ICON:
                                updateChatIcon(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CHAT_LIST_ICON:
                                updateChatListIcon(storageObject, fileLocal);
                                break;
                            case USER_IMAGE:
                                int userId = (int) storageObject.obj[0];
                                UserManager userManager = UserManager.getManager();
                                CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                                cachedUser.tgUser.profilePhoto.small = fileLocal;
                                if (userId == userManager.getCurrUserId()) {
                                    notifyObservers(new NotificationObject(NotificationObject.USER_IMAGE_UPDATE, fileLocal));
                                } else {
                                    ChatListManager.getManager().refreshDialogByUserId(userId);
                                }
                                break;
                            case DOCUMENT:
                                updateFileDocument(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case DOCUMENT_THUMB:
                                updateFileDocumentThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case PHOTO:
                                updateFilePhoto(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case PHOTO_THUMB:
                                updateFilePhotoThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CHAT_STICKER:
                            case USER_STICKER:
                                updateFileSticker(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case CHAT_STICKER_THUMB:
                            case USER_STICKER_THUMB:
                                updateFileStickerThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case USER_STICKER_MICRO_THUMB:
                                updateFileStickerMicroThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case VIDEO_THUMB:
                                updateFileVideoThumb(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case VIDEO:
                                updateFileVideo(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case VOICE:
                                updateFileVoice(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                            case AUDIO:
                                updateFileAudio(storageObject, fileLocal);
                                updateAdditionStorage(storageObject.typeLoad, additionStorageObjectList, fileLocal);
                                break;
                        }
                        FileManager.getManager().removeIndexStorageAndAdditionMap(storageObject.msgId);
                    }
                    break;
                }
                case TdApi.UpdateStickers.CONSTRUCTOR: {
                    TdApi.UpdateStickers updateStickers = (TdApi.UpdateStickers) object;
                    StickerManager.getManager().getStickers(false);
                    break;
                }
                case TdApi.UpdateNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateNotificationSettings updateNotificationSettings = (TdApi.UpdateNotificationSettings) object;

                    switch (updateNotificationSettings.scope.getConstructor()) {
                        case TdApi.NotificationSettingsForAllChats.CONSTRUCTOR:
                            break;
                        case TdApi.NotificationSettingsForChat.CONSTRUCTOR:
                            TdApi.NotificationSettingsForChat notificationSettingsForChat = (TdApi.NotificationSettingsForChat) updateNotificationSettings.scope;
                            ChatListManager.getManager().updateNotificationSettingForChat(notificationSettingsForChat.chatId, updateNotificationSettings.notificationSettings);

                            notifyObservers(new NotificationObject(NotificationObject.CHANGE_CHAT_MUTE_STATUS,
                                    new Object[]{notificationSettingsForChat.chatId, updateNotificationSettings.notificationSettings.muteFor}));
                            break;
                        case TdApi.NotificationSettingsForGroupChats.CONSTRUCTOR:
                            break;
                        case TdApi.NotificationSettingsForPrivateChats.CONSTRUCTOR:
                            break;
                    }
                    break;
                }
                case TdApi.UpdateMessageId.CONSTRUCTOR: {
                    TdApi.UpdateMessageId updateMessageId = (TdApi.UpdateMessageId) object;
                    //breakUpdateNewMsgList.put(updateMessageId.newId, updateMessageId.newId);
                    ChatManager.getManager().updateMessageId(updateMessageId);
                    ChatListManager.getManager().updateMessageId(updateMessageId);
                    break;
                }
                case TdApi.UpdateMessageDate.CONSTRUCTOR: {
                    //info отрабатывает если сообщение отправлялось больше секунды
                    TdApi.UpdateMessageDate messageDate = (TdApi.UpdateMessageDate) object;
                    ChatManager.getManager().updateMessageDate(messageDate);
                    ChatListManager.getManager().updateMessageDate(messageDate);
                    break;
                }
                case TdApi.UpdateMessageContent.CONSTRUCTOR: {
                    TdApi.UpdateMessageContent mc = (TdApi.UpdateMessageContent) object;
                    //info Присылается если я отправлял контент не повторный(именно первичный)!
                    //не вижу смысла вообще это использывать(если раскоментить будет работать)
                    //ChatListManager.getManager().updateMsgContent(mc);
                    //ChatManager.getManager().updateMsgContent(mc);
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadInbox chatReadInbox = (TdApi.UpdateChatReadInbox) object;
                    ThreadService.runTaskBySchedule(new Runnable() {
                        @Override
                        public void run() {
                            ChatManager.getManager().updateChatReadInbox(chatReadInbox);
                            ChatListManager.getManager().updateChatReadInbox(chatReadInbox);
                        }
                    }, 200);
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadOutbox updateChatReadOutbox = (TdApi.UpdateChatReadOutbox) object;
                    ThreadService.runTaskBySchedule(new Runnable() {
                        @Override
                        public void run() {
                            ChatManager.getManager().updateChatReadOutbox(updateChatReadOutbox);
                            ChatListManager.getManager().updateChatReadOutbox(updateChatReadOutbox);
                        }
                    }, 200);
                    break;
                }
                case TdApi.UpdateDeleteMessages.CONSTRUCTOR: {
                    TdApi.UpdateDeleteMessages deleteMessages = (TdApi.UpdateDeleteMessages) object;
                    ChatManager.getManager().deleteMessageFromChat(deleteMessages.messages, deleteMessages.chatId);
                    break;
                }
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    UserManager userManager = UserManager.getManager();
                    CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(updateUserStatus.userId);
                    cachedUser.tgUser.status = updateUserStatus.status;
                    notifyObservers(new NotificationObject(NotificationObject.UPDATE_USER_STATUS, updateUserStatus));
                    break;
                }
                //отрабатывает только для изменения инфы об определенном юзере
                case TdApi.UpdateUser.CONSTRUCTOR:
                    //info если сменилось имя(UpdateUserName), телефон(UpdateUserPhoneNumber),фото юзера(UpdateUserProfilePhoto), UserLinks
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;

                    UserManager userManager = UserManager.getManager();
                    CachedUser cachedUser = userManager.insertUserInCache(updateUser.user);

                    if (userManager.getCurrUserId() != null && updateUser.user.id == userManager.getCurrUserId()) {
                        if (FileUtils.isTDFileEmpty(cachedUser.tgUser.profilePhoto.small)) {
                            if (cachedUser.tgUser.profilePhoto.small.id > 0) {
                                FileManager.getManager().uploadFile(FileManager.TypeLoad.USER_IMAGE, new Object[]{cachedUser.tgUser.id}, cachedUser.tgUser.profilePhoto.small.id, -1, -1);
                            }
                        }
                        notifyObservers(new NotificationObject(NotificationObject.USER_DATA_UPDATE, cachedUser));
                    } else {
                        //info если изменилось имя юзера не владельца, делаем перерасчет его чата(если такой имеется),
                        //внутри чата имя не меняем(дорогая операция, хотя индексы по юзерам есть)
                        ChatListManager.getManager().updateDialogTitleWithSingleUser(updateUser.user.id, cachedUser);
                        notifyObservers(new NotificationObject(NotificationObject.UPDATE_CHAT_TITLE_USER, cachedUser));
                    }
                    break;
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    //отработает только в случае изменения названия группового чата
                    TdApi.UpdateChatTitle updateChatTitle = (TdApi.UpdateChatTitle) object;
                    ChatListManager.getManager().updateDialogTitle(updateChatTitle);
                    notifyObservers(new NotificationObject(NotificationObject.UPDATE_CHAT_TITLE, updateChatTitle));
                    break;
                }
                case TdApi.UpdateChatParticipantsCount.CONSTRUCTOR: {
                    final TdApi.UpdateChatParticipantsCount updateChatParticipantsCount = (TdApi.UpdateChatParticipantsCount) object;
                    TdApi.GetGroupChatFull getGroupChatFull = new TdApi.GetGroupChatFull();
                    getGroupChatFull.groupChatId = (int) updateChatParticipantsCount.chatId;
                    client().send(getGroupChatFull, new ResultController() {
                        @Override
                        public void afterResult(final TdApi.TLObject object, int calledConstructor) {
                            ThreadService.runTaskBySchedule(new Runnable() {
                                @Override
                                public void run() {
                                    switch (object.getConstructor()) {
                                        case TdApi.GroupChatFull.CONSTRUCTOR: {
                                            TdApi.GroupChatFull groupChatFull = (TdApi.GroupChatFull) object;
                                            ChatListManager.getManager().updateMembersInGroupChat(updateChatParticipantsCount, groupChatFull);
                                            //info обязательно UpdateHandler.this
                                            UpdateHandler.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MEMBER_COUNT, updateChatParticipantsCount));
                                        }
                                        break;
                                    }
                                }
                            }, 0);
                        }
                    });
                    break;
                }
            }
        } catch (Throwable e) {
            Log.e(LOG, "onResult", e);
            Crashlytics.logException(e);
        }
    }

    private void displaySticker(final String path, final FileManager.TypeLoad type, final View stickerView, final String tag, final boolean animate, final int[] bounds) {
        if (WebpSupportManager.IS_NEED_NATIVE_LIB) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    WebpSupportManager.getManager().loadWebP(path, type, stickerView, tag, bounds, animate);
                }
            });
        } else {
            BitmapDrawable bitmapDrawable = FileManager.getManager().getStickerFromFile(path, type, bounds);
            //Logs.e("displayStickerIfVisible2=" + AndroidUtil.isItemViewVisible(stickerView, tag) + " = " + tag);


            StickerManager stickerManager = StickerManager.getManager();
            if (tag != null && tag.equals("0") && stickerView instanceof StickerThumbView) {
                View newStickerView = stickerManager.getStickerThumbGridView().getChildAt(Integer.valueOf(tag));

                if (AndroidUtil.isItemViewVisible(newStickerView, tag)) {
                    ((ImageUpdatable) newStickerView).setImageAndUpdateAsync(bitmapDrawable, animate);
                }
                //Logs.e("displayStickerIfVisible2=");
            } else {
                if (AndroidUtil.isItemViewVisible(stickerView, tag)) {
                    ((ImageUpdatable) stickerView).setImageAndUpdateAsync(bitmapDrawable, animate);
                }
            }


        }
    }

    private void displayStickerIfVisible(final String path, final FileManager.TypeLoad type, final View stickerView, final String tag, boolean animate, int[] bounds) {
        //Logs.e("displayStickerIfVisible=" + AndroidUtil.isItemViewVisible(stickerView, tag) + " = " + tag);
        if (AndroidUtil.isItemViewVisible(stickerView, tag))
            displaySticker(path, type, stickerView, tag, animate, bounds);
    }

    private void updateIconBot(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.User user = (TdApi.User) storageObject.obj[0];
        final ImageView icon = (ImageView) storageObject.obj[1];
        final View itemView = (View) storageObject.obj[2];
        final String tag = (String) storageObject.obj[3];
        user.profilePhoto.small = fileLocal;
        final IconDrawable iconDrawable = IconFactory.createBitmapIconForImageView(IconFactory.Type.BOT_COMMAND, fileLocal.path, itemView, icon, tag);
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (AndroidUtil.isItemViewVisible(itemView, tag) && iconDrawable != null) {
                    icon.setImageDrawable(iconDrawable);
                }
            }
        });
    }

    private void updateChangeTitleImage(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.Photo photo = (TdApi.Photo) storageObject.obj[0];
        final View itemView = (View) storageObject.obj[1];
        final ImageView icon = (ImageView) storageObject.obj[2];
        final String tag = (String) storageObject.obj[3];
        photo.photos[0].photo = fileLocal;

        final IconDrawable iconDrawable = IconFactory.createBitmapIconForImageView(IconFactory.Type.TITLE, fileLocal.path, itemView, icon, tag);
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (AndroidUtil.isItemViewVisible(itemView, tag) && iconDrawable != null) {
                    icon.setImageDrawable(iconDrawable);
                }
            }
        });
    }

    private void updateChatIcon(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        //больше ничего не делаем тк, если файл скачан, то нам сразу прийдет апдейт на него(была идея проходить по проиндексированным записям)
        TdApi.User user = (TdApi.User) storageObject.obj[0];
        AbstractMsgView abstractMsgView = (AbstractMsgView) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        Boolean forward = (Boolean) storageObject.obj[3];
        if (forward == null) {
            forward = false;
        }
        user.profilePhoto.small = fileLocal;
        IconDrawable iconDrawable = IconFactory.createBitmapIconForChat(IconFactory.Type.CHAT, fileLocal.path, abstractMsgView, tag, forward);

        if (AndroidUtil.isItemViewVisible(abstractMsgView, tag)) {
            abstractMsgView.setIconAsync(iconDrawable, forward);
        }
    }

    private void updateContactIcon(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        //больше ничего не делаем тк, если файл скачан, то нам сразу прийдет апдейт на него(была идея проходить по проиндексированным записям)
        TdApi.User user = (TdApi.User) storageObject.obj[0];
        ContactMsgView contactMsgView = (ContactMsgView) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        user.profilePhoto.small = fileLocal;
        IconDrawable iconDrawable = IconFactory.createBitmapIconForContact(IconFactory.Type.CHAT, fileLocal.path, contactMsgView, tag);

        if (AndroidUtil.isItemViewVisible(contactMsgView, tag)) {
            contactMsgView.setUserIconDrawableAndUpdateAsync(iconDrawable);
        }
    }

    private void updateChatListIcon(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        //additionStorageObjectList не нужен тк они не повторяются в списке
        TdApi.TLObject tlObject = (TdApi.TLObject) storageObject.obj[0];
        View view = (View) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];

        IconDrawable iconDrawable;
        //есть еще UnknownPrivateChatInfo, UnknownGroupChatInfo но нет смысла их тут проверять
        switch (tlObject.getConstructor()) {
            case TdApi.User.CONSTRUCTOR: {
                TdApi.User user = (TdApi.User) tlObject;
                user.profilePhoto.small = fileLocal;
                iconDrawable = IconFactory.createBitmapIcon(IconFactory.Type.CHAT_LIST, fileLocal.path);
                break;
            }
            case TdApi.GroupChat.CONSTRUCTOR: {
                TdApi.GroupChat groupChat = (TdApi.GroupChat) tlObject;
                groupChat.photo.small = fileLocal;
                iconDrawable = IconFactory.createBitmapIcon(IconFactory.Type.CHAT_LIST, fileLocal.path);
                break;
            }
            default:
                iconDrawable = null;
        }

        if (AndroidUtil.isItemViewVisible(view, tag)) {
            ((IconUpdatable) view).setIconAsync(iconDrawable, false);
        }
    }

    private void updateProcessFileSharedAudio(final FileManager.StorageObject storageObject, final int process, int... args) {
        final View itemView = (View) storageObject.obj[0];
        final TdApi.Audio audio = (TdApi.Audio) storageObject.obj[1];
        final String tag = (String) storageObject.obj[2];
        final TextView textView = (TextView) storageObject.obj[3];
        final DetermineProgressView progressView = (DetermineProgressView) storageObject.obj[4];
        storageObject.processLoad = process;
        if (storageObject.loadMsg[0] == null) {
            storageObject.loadMsg[0] = new StringBuffer();
        }
        storageObject.loadMsg[0].setLength(0);
        storageObject.loadMsg[0].append(ChatHelper.getFileDownloadingSize(args[0], args[1]));

        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (AndroidUtil.isItemViewVisible(itemView, tag)) {

                    if (textView != null) {
                        textView.setText(storageObject.loadMsg[0]);
                    }

                    progressView.getProgressDrawable().setProgressWithAnimationAsync(process);
                }
            }
        });
    }

    private void updateProcessFileAudio(FileManager.StorageObject storageObject, int process, int... args) {
        AudioMsgView audioMsgView = (AudioMsgView) storageObject.obj[0];
        TdApi.Audio audio = (TdApi.Audio) storageObject.obj[1];
        String audioTag = (String) storageObject.obj[2];
        TextUtil.ellipsizeTextForTwoOrientations(
                storageObject.loadMsg,
                audioMsgView.getMaxTextWidths(),
                DocumentMsgView.FILE_SIZE_PAINT,
                ChatHelper.getFileDownloadingSize(args[0], args[1]));
        storageObject.processLoad = process;
        if (AndroidUtil.isItemViewVisible(audioMsgView, audioTag)) {
            audioMsgView.setSubTitleSize(storageObject.loadMsg);
            audioMsgView.getProgressDrawable().setProgressWithAnimationAsync(process);
        }
    }

    private void updateProcessFileDocument(FileManager.StorageObject storageObject, int process, int... args) {
        DocumentMsgView documentMsgView = (DocumentMsgView) storageObject.obj[0];
        TdApi.Document document = (TdApi.Document) storageObject.obj[1];
        String docTag = (String) storageObject.obj[2];

        TextUtil.ellipsizeTextForTwoOrientations(
                storageObject.loadMsg,
                documentMsgView.getMaxTextWidths(),
                DocumentMsgView.FILE_SIZE_PAINT,
                ChatHelper.getFileDownloadingSize(args[0], args[1]));
        storageObject.processLoad = process;

        if (AndroidUtil.isItemViewVisible(documentMsgView, docTag)) {
            documentMsgView.setFileSize(storageObject.loadMsg);
            documentMsgView.getProgressDrawable().setProgressWithAnimationAsync(process);
        }
    }

    private void updateProcessFilePhoto(FileManager.StorageObject storageObject, int process) {
        PhotoMsgView photoMsgView = (PhotoMsgView) storageObject.obj[0];
        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[1];
        String photoTag = (String) storageObject.obj[2];
        storageObject.processLoad = process;
        if (AndroidUtil.isItemViewVisible(photoMsgView, photoTag) && photoMsgView.isLocalMsg()) {
            //info есть косяк в либе не всегда присылает UpdateFile или же пришлет его раньше чем UpdateFileProcess
            photoMsgView.getProgressDrawable().setProgressWithForceAnimationAsync(process, true);
        }
    }

    private void updateProcessFileVideo(FileManager.StorageObject storageObject, int process) {
        final VideoMsgView videoMsgView = (VideoMsgView) storageObject.obj[0];
        TdApi.Video video = (TdApi.Video) storageObject.obj[1];
        String videoTag = (String) storageObject.obj[2];
        storageObject.processLoad = process;
        if (AndroidUtil.isItemViewVisible(videoMsgView, videoTag))
            videoMsgView.getProgressDrawable().setProgressWithAnimationAsync(process);
    }

    private void updateProcessFileVoice(FileManager.StorageObject storageObject, int process) {
        VoiceMsgView voiceMsgView = (VoiceMsgView) storageObject.obj[0];
        TdApi.Voice audio = (TdApi.Voice) storageObject.obj[1];
        String audioTag = (String) storageObject.obj[2];
        storageObject.processLoad = process;
        if (AndroidUtil.isItemViewVisible(voiceMsgView, audioTag))
            voiceMsgView.getProgressDrawable().setProgressWithAnimationAsync(process);
    }

    private void updateFileAudio(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        AudioMsgView audioMsgView = (AudioMsgView) storageObject.obj[0];
        TdApi.Audio audio = (TdApi.Audio) storageObject.obj[1];
        audio.audio = fileLocal;
        String tag = (String) storageObject.obj[2];

        TextUtil.ellipsizeTextForTwoOrientations(
                storageObject.loadMsg,
                audioMsgView.getMaxTextWidths(),
                DocumentMsgView.FILE_SIZE_PAINT,
                audio.title);

        if (AndroidUtil.isItemViewVisible(audioMsgView, tag)) {
            audioMsgView.setSubTitleSize(storageObject.loadMsg);
            audioMsgView.getProgressDrawable().setProgressWithAnimationAsync(100, true);
        }
    }

    private void updateFileSharedAudio(final FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        final View itemView = (View) storageObject.obj[0];
        final TdApi.Audio audio = (TdApi.Audio) storageObject.obj[1];
        audio.audio = fileLocal;
        final String tag = (String) storageObject.obj[2];
        final TextView textView = (TextView) storageObject.obj[3];
        final DetermineProgressView progressView = (DetermineProgressView) storageObject.obj[4];


        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                    if (textView != null) {
                        textView.setText(audio.title);
                    } else {
                        UpdateHandler.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG, new Object[]{audio, storageObject.msgId, true}));
                    }
                    progressView.getProgressDrawable().setProgressWithAnimationAsync(100, true);
                }
            }
        });
    }

    private void updateFileDocument(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        DocumentMsgView documentMsgView = (DocumentMsgView) storageObject.obj[0];
        TdApi.Document document = (TdApi.Document) storageObject.obj[1];
        String docTag = (String) storageObject.obj[2];

        document.document = fileLocal;

        TextUtil.ellipsizeTextForTwoOrientations(
                storageObject.loadMsg,
                documentMsgView.getMaxTextWidths(),
                DocumentMsgView.FILE_SIZE_PAINT,
                ChatHelper.getFileSize(fileLocal.size));

        if (AndroidUtil.isItemViewVisible(documentMsgView, docTag)) {
            documentMsgView.setFileSize(storageObject.loadMsg);
            documentMsgView.getProgressDrawable().setProgressWithAnimationAsync(100, true);
        }
    }

    private void updateFileDocumentThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        DocumentMsgView documentMsgView = (DocumentMsgView) storageObject.obj[0];
        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];

        photoSize.photo = fileLocal;
        if (AndroidUtil.isItemViewVisible(documentMsgView, tag)) {
            documentMsgView.getProgressDrawable().setBackgroundImageAsync(FileManager.getManager().getNoResizeBitmapFromFile(fileLocal.path));
        }
    }

    private void updateFilePhoto(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        PhotoMsgView photoMsgView = (PhotoMsgView) storageObject.obj[0];
        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        photoSize.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(photoMsgView, tag))
            photoMsgView.setImageAndUpdateAsync(FileManager.getManager().getImageFromFile(fileLocal.path));
    }


    private void updateFileSharedPhoto(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        MediaView mediaView = (MediaView) storageObject.obj[0];
        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        photoSize.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(mediaView, tag))
            mediaView.setImageAndUpdateAsync(FileManager.getManager().getImageFromFile(fileLocal.path));
    }

    private void updateFilePhotoThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        PhotoMsgView photoMsgView = (PhotoMsgView) storageObject.obj[0];
        TdApi.PhotoSize photoSizeThumb = (TdApi.PhotoSize) storageObject.obj[1];
        String tag = (String) storageObject.obj[5];

        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[3];
        int fileId = (int) storageObject.obj[2];
        int pos = (int) storageObject.obj[4];

        photoSizeThumb.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(photoMsgView, tag)) {
            photoMsgView.setImageAndUpdateAsync(FileManager.getManager().getNoResizeBitmapFromFile(fileLocal.path));
            FileManager.getManager().uploadFile(FileManager.TypeLoad.PHOTO,
                    new Object[]{photoMsgView, photoSize, tag}, fileId, pos, photoMsgView.record.tgMessage.id);
        }
    }

    private void updateFileSharedPhotoThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        MediaView mediaView = (MediaView) storageObject.obj[0];
        TdApi.PhotoSize photoSizeThumb = (TdApi.PhotoSize) storageObject.obj[1];
        String tag = (String) storageObject.obj[5];

        TdApi.PhotoSize photoSize = (TdApi.PhotoSize) storageObject.obj[3];
        int fileId = (int) storageObject.obj[2];
        int pos = (int) storageObject.obj[4];

        photoSizeThumb.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(mediaView, tag)) {
            mediaView.setImageAndUpdateAsync(FileManager.getManager().getNoResizeBitmapFromFile(fileLocal.path));
            FileManager.getManager().uploadFile(FileManager.TypeLoad.SHARED_PHOTO,
                    new Object[]{mediaView, photoSize, tag}, fileId, pos, mediaView.sharedMedia.message.id);
        }
    }

    private void updateFileSticker(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.Sticker sticker = (TdApi.Sticker) storageObject.obj[0];
        View stickerView = (View) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        int bounds[] = {(int) storageObject.obj[3], (int) storageObject.obj[4]};
        sticker.sticker = fileLocal;

        //после подгрузки полного изображения, отправляем стикер!
        if (storageObject.typeLoad == FileManager.TypeLoad.USER_STICKER) {
            ChatManager manager = ChatManager.getManager();
            TdApi.InputMessageContent msg = manager.createStickerMsg(sticker.sticker.path);
            manager.sendMessage(msg);
            StickerRecentManager.getInstance().addRecentSticker(sticker);
            /*if (FileUtils.isTDFileLocal(sticker.thumb.photo)) {
                TdApi.File fileLocalThumb = sticker.thumb.photo;
                displayStickerIfVisible(fileLocalThumb.path, FileManager.TypeLoad.USER_STICKER_THUMB, stickerView, tag, false, bounds);
            }*/
        } else {
            displayStickerIfVisible(fileLocal.path, storageObject.typeLoad, stickerView, tag, false, bounds);
        }
    }

    private void updateFileStickerThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.Sticker sticker = (TdApi.Sticker) storageObject.obj[0];
        View stickerView = (View) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];
        int bounds[] = {(int) storageObject.obj[3], (int) storageObject.obj[4]};
        sticker.thumb.photo = fileLocal;

        if (storageObject.typeLoad == FileManager.TypeLoad.USER_STICKER_THUMB) {

            /*if (tag.equals("0")) {
                Logs.e("bbbbbbbb===" + AndroidUtil.isItemViewVisible(stickerView, tag));
            }*/

            if (AndroidUtil.isItemViewVisible(stickerView, tag)) {
                //Logs.e("bbbbbbbb===" + tag + " " + sticker.thumb.photo.path);
                /*FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.USER_STICKER,
                        (int) storageObject.obj[5], (int) storageObject.obj[6], -1, sticker, stickerView, tag, storageObject.obj[3], storageObject.obj[4]);*/
                displaySticker(sticker.thumb.photo.path, FileManager.TypeLoad.USER_STICKER_THUMB, stickerView, tag, false, bounds);
            }
        } else {
            //Logs.e("displaySticker00=" + AndroidUtil.isItemViewVisible(stickerView, tag) + " " + tag);
            if (AndroidUtil.isItemViewVisible(stickerView, tag)) {
                displaySticker(fileLocal.path, FileManager.TypeLoad.CHAT_STICKER_THUMB, stickerView, tag, false, bounds);
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_STICKER,
                        (int) storageObject.obj[5], (int) storageObject.obj[6], ((StickerMsgView) (stickerView)).record.tgMessage.id,
                        sticker, stickerView, tag, storageObject.obj[3], storageObject.obj[4]);
            }
        }
    }

    private void updateFileStickerMicroThumb(FileManager.StorageObject storageObject, final TdApi.File fileLocal) {
        final TdApi.Sticker sticker = (TdApi.Sticker) storageObject.obj[0];
        final ImageView imageView = (ImageView) storageObject.obj[1];
        final View itemView = (View) storageObject.obj[2];
        final String tag = (String) storageObject.obj[3];
        sticker.thumb.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
            if (WebpSupportManager.IS_NEED_NATIVE_LIB) {
                ThreadService.runTaskBackground(new Runnable() {
                    @Override
                    public void run() {
                        WebpSupportManager.getManager().loadWebP(fileLocal.path, FileManager.TypeLoad.USER_STICKER_MICRO_THUMB, itemView, tag, imageView, false);
                    }
                });
            } else {
                final BitmapDrawable bitmapDrawable = FileManager.getManager().getStickerFromFile(fileLocal.path, FileManager.TypeLoad.USER_STICKER_MICRO_THUMB);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            StickerManager stickerManager = StickerManager.getManager();
                            if (tag != null) {
                                ImageView newStickerView = (ImageView) ((RelativeLayout) stickerManager.getMicroStickerLinearLayoutManager()
                                        .findViewByPosition(Integer.valueOf(tag))).findViewById(R.id.image_micro_thumb);

                                if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                    newStickerView.setImageDrawable(bitmapDrawable);
                                }
                            } else {
                                if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                    imageView.setImageDrawable(bitmapDrawable);
                                }
                            }

                        } catch (Throwable e) {
                            Log.e(LOG, "displaySticker", e);
                        }
                    }
                });
            }
        }
    }

    private void updateFileVoice(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        VoiceMsgView voiceMsgView = (VoiceMsgView) storageObject.obj[0];
        TdApi.Voice audio = (TdApi.Voice) storageObject.obj[1];
        audio.voice = fileLocal;
        String tag = (String) storageObject.obj[2];

        if (AndroidUtil.isItemViewVisible(voiceMsgView, tag)) {
            voiceMsgView.setThumbVisible(true);
            voiceMsgView.getProgressDrawable().setProgressWithAnimationAsync(100, true);
        }
    }

    private void updateFileVideo(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        final VideoMsgView videoMsgView = (VideoMsgView) storageObject.obj[0];
        TdApi.Video video = (TdApi.Video) storageObject.obj[1];
        String videoTag = (String) storageObject.obj[2];

        video.video = fileLocal;
        if (AndroidUtil.isItemViewVisible(videoMsgView, videoTag))
            videoMsgView.getProgressDrawable().setProgressWithAnimationAsync(100, true);
    }

    private void updateFileSharedVideoThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.PhotoSize thumb = (TdApi.PhotoSize) storageObject.obj[0];
        MediaView mediaView = (MediaView) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];

        thumb.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(mediaView, tag))
            mediaView.setImageAndUpdateAsync(FileManager.getManager().getNoResizeBitmapFromFile(fileLocal.path));
    }

    private void updateFileVideoThumb(FileManager.StorageObject storageObject, TdApi.File fileLocal) {
        TdApi.PhotoSize thumb = (TdApi.PhotoSize) storageObject.obj[0];
        VideoMsgView videoMsgView = (VideoMsgView) storageObject.obj[1];
        String tag = (String) storageObject.obj[2];

        thumb.photo = fileLocal;

        if (AndroidUtil.isItemViewVisible(videoMsgView, tag))
            videoMsgView.setImageAndUpdateAsync(FileManager.getManager().getNoResizeBitmapFromFile(fileLocal.path));
    }

    private void updateAdditionStorage(FileManager.TypeLoad typeLoad, List<FileManager.StorageObject> additionStorageObjectList, TdApi.File fileLocal) {
        if (additionStorageObjectList != null && !additionStorageObjectList.isEmpty()) {
            for (int i = 0; i < additionStorageObjectList.size(); i++) {
                FileManager.StorageObject storageObjectAddition = additionStorageObjectList.get(i);
                //info typeload должен браться из дополнительных!!!
                switch (storageObjectAddition.typeLoad != null ? storageObjectAddition.typeLoad : typeLoad) {
                    case DOCUMENT_THUMB:
                        updateFileDocumentThumb(storageObjectAddition, fileLocal);
                        break;
                    case PHOTO:
                        updateFilePhoto(storageObjectAddition, fileLocal);
                        break;
                    case PHOTO_THUMB:
                        updateFilePhotoThumb(storageObjectAddition, fileLocal);
                        break;
                    case CHAT_STICKER:
                        updateFileSticker(storageObjectAddition, fileLocal);
                        break;
                    case CHAT_STICKER_THUMB:
                    case USER_STICKER_THUMB:
                        updateFileStickerThumb(storageObjectAddition, fileLocal);
                        break;
                    case VIDEO_THUMB:
                        updateFileVideoThumb(storageObjectAddition, fileLocal);
                        break;
                    case VIDEO:
                        updateFileVideo(storageObjectAddition, fileLocal);
                        break;
                    case VOICE:
                        updateFileVoice(storageObjectAddition, fileLocal);
                        break;
                    case AUDIO:
                        updateFileAudio(storageObjectAddition, fileLocal);
                        break;
                    case DOCUMENT:
                        updateFileDocument(storageObjectAddition, fileLocal);
                        break;
                    case CHAT_ICON:
                        updateChatIcon(storageObjectAddition, fileLocal);
                        break;
                    case BOT_COMMAND_ICON:
                        updateIconBot(storageObjectAddition, fileLocal);
                        break;
                    case USER_STICKER_MICRO_THUMB:
                        updateFileStickerMicroThumb(storageObjectAddition, fileLocal);
                        break;
                    case CONTACT_ICON:
                        updateContactIcon(storageObjectAddition, fileLocal);
                        break;
                    case SHARED_VIDEO_THUMB:
                        updateFileSharedVideoThumb(storageObjectAddition, fileLocal);
                        break;
                    case SHARED_PHOTO:
                        updateFileSharedPhoto(storageObjectAddition, fileLocal);
                        break;
                    case SHARED_PHOTO_THUMB:
                        updateFileSharedPhotoThumb(storageObjectAddition, fileLocal);
                        break;
                    case SHARED_AUDIO:
                    case SHARED_AUDIO_PLAYER:
                        updateFileSharedAudio(storageObjectAddition, fileLocal);
                        break;
                    case CHANGE_CHAT_TITLE_IMAGE:
                        updateChangeTitleImage(storageObjectAddition, fileLocal);
                        break;
                }

                FileManager.getManager().removeIndexStorageAndAdditionMap(storageObjectAddition.msgId);
            }
        }
    }

    private void updateProcessAdditionStorage(FileManager.TypeLoad typeLoad, List<FileManager.StorageObject> additionStorageObjectList, int process, int... args) {
        if (additionStorageObjectList != null && !additionStorageObjectList.isEmpty()) {
            for (int i = 0; i < additionStorageObjectList.size(); i++) {
                FileManager.StorageObject storageObjectAddition = additionStorageObjectList.get(i);
                switch (storageObjectAddition.typeLoad != null ? storageObjectAddition.typeLoad : typeLoad) {
                    case PHOTO:
                        updateProcessFilePhoto(storageObjectAddition, process);
                        break;
                    case VIDEO:
                        updateProcessFileVideo(storageObjectAddition, process);
                        break;
                    case VOICE:
                        updateProcessFileVoice(storageObjectAddition, process);
                        break;
                    case AUDIO:
                        updateProcessFileAudio(storageObjectAddition, process, args);
                        break;
                    case DOCUMENT:
                        updateProcessFileDocument(storageObjectAddition, process, args);
                        break;
                    case SHARED_AUDIO:
                    case SHARED_AUDIO_PLAYER:
                        updateProcessFileSharedAudio(storageObjectAddition, process, args);
                        break;
                }
            }
        }
    }
}
