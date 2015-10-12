package com.stayfprod.utter.manager;


import android.util.SparseArray;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.SharedMedia;
import com.stayfprod.utter.model.SharedMusic;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.DateUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SharedMediaManager extends ResultController {

    private static volatile SharedMediaManager sSharedMediaManager;

    public static SharedMediaManager getManager() {
        if (sSharedMediaManager == null) {
            synchronized (SharedMediaManager.class) {
                if (sSharedMediaManager == null) {
                    sSharedMediaManager = new SharedMediaManager();
                }
            }
        }
        return sSharedMediaManager;
    }

    private List<SharedMusic> mAudioMessages = new ArrayList<SharedMusic>(100);
    private List<SharedMedia> mPhotoAndVideoProfileMessages = new ArrayList<SharedMedia>(50);
    private List<SharedMedia> mPhotoAndVideoProfileMessagesSecond = new ArrayList<SharedMedia>(50);
    private List<SharedMedia> mPhotoAndVideoSharedMessages = new ArrayList<SharedMedia>(100);

    private SparseArray<SharedMusic> mSelectedMusicList = new SparseArray<SharedMusic>(100);
    private SparseArray<SharedMedia> mSelectedMediaList = new SparseArray<SharedMedia>(100);

    public SparseArray<SharedMedia> getSelectedMediaList() {
        return mSelectedMediaList;
    }

    public SparseArray<SharedMusic> getSelectedMusicList() {
        return mSelectedMusicList;
    }

    public void cleanSelectedMusic() {
        for (int i = 0; i < mSelectedMusicList.size(); i++) {
            int key = mSelectedMusicList.keyAt(i);
            SharedMusic obj = mSelectedMusicList.get(key);
            obj.isSelected = false;
        }
        mSelectedMusicList.clear();
    }

    public void cleanSelectedMedia() {
        for (int i = 0; i < mSelectedMediaList.size(); i++) {
            int key = mSelectedMediaList.keyAt(i);
            SharedMedia obj = mSelectedMediaList.get(key);
            obj.isSelected = false;
        }
        mSelectedMediaList.clear();
    }

    public void deleteSelectedMusicList(final Long chatId) {
        if (chatId != null) {
            final int[] msgs = new int[mSelectedMusicList.size()];

            for (int i = 0; i < mSelectedMusicList.size(); i++) {
                int key = mSelectedMusicList.keyAt(i);
                SharedMusic sharedMusic = mSelectedMusicList.get(key);
                sharedMusic.isDeleted = true;
                msgs[i] = sharedMusic.message.id;
            }

            mSelectedMusicList.clear();
            final TdApi.DeleteMessages func = new TdApi.DeleteMessages();
            func.chatId = chatId;
            func.messageIds = msgs;
            client().send(func, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                    switch (object.getConstructor()) {
                        case TdApi.Ok.CONSTRUCTOR:
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    for (Iterator<SharedMusic> it2 = mAudioMessages.iterator(); it2.hasNext(); ) {
                                        SharedMusic sharedMusic = it2.next();
                                        if (sharedMusic.isDeleted) {
                                            it2.remove();
                                        }
                                    }
                                    SharedMediaManager.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_AUDIO_LIST, null));
                                }
                            });

                            break;
                    }
                }
            });
        }
    }

    public void deleteSelectedMediaList(final Long chatId, final boolean isOldChatId) {
        if (chatId != null) {
            final int[] msgs = new int[mSelectedMediaList.size()];

            for (int i = 0; i < mSelectedMediaList.size(); i++) {
                int key = mSelectedMediaList.keyAt(i);
                SharedMedia sharedMedia = mSelectedMediaList.get(key);
                sharedMedia.isDeleted = true;
                msgs[i] = sharedMedia.message.id;
            }

            mSelectedMediaList.clear();
            final TdApi.DeleteMessages func = new TdApi.DeleteMessages();
            func.chatId = chatId;
            func.messageIds = msgs;
            client().send(func, new ResultController() {
                @Override
                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                    switch (object.getConstructor()) {
                        case TdApi.Ok.CONSTRUCTOR:
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    List<SharedMedia> sharedList = getRequiredForDeleteList(false, isOldChatId);
                                    for (Iterator<SharedMedia> it2 = sharedList.iterator(); it2.hasNext(); ) {
                                        SharedMedia sharedMedia = it2.next();
                                        if (sharedMedia.isDeleted) {
                                            it2.remove();
                                        } else {
                                            sharedMedia.totalCount = sharedMedia.totalCount - msgs.length;
                                        }
                                    }
                                    SharedMediaManager.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_JUST_SHARED_MEDIA_LIST, null));

                                    cleanSearchMedia(true, isOldChatId);
                                    searchMedia(chatId, UserManager.getManager().getCurrUserId(), true, isOldChatId);
                                }
                            });
                            break;
                    }
                }
            });
        }
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public List<SharedMedia> getPhotoAndVideoSharedMessages() {
        return mPhotoAndVideoSharedMessages;
    }

    public List<SharedMedia> getPhotoAndVideoProfileMessages(boolean isOldChatId) {
        if (isOldChatId) {
            return mPhotoAndVideoProfileMessages;
        } else {
            return mPhotoAndVideoProfileMessagesSecond;
        }
    }

    public String getPhotoAndVideoMessagesStringSize(boolean isOldChatId) {
        if (isOldChatId) {
            if (mPhotoAndVideoProfileMessages != null) {
                if (mPhotoAndVideoProfileMessages.size() > 0) {
                    return String.valueOf(mPhotoAndVideoProfileMessages.get(0).totalCount);
                }
            }
        } else {
            if (mPhotoAndVideoProfileMessagesSecond != null) {
                if (mPhotoAndVideoProfileMessagesSecond.size() > 0) {
                    return String.valueOf(mPhotoAndVideoProfileMessagesSecond.get(0).totalCount);
                }
            }
        }
        return "0";
    }

    public void setCurrentPage(int page) {
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_MEDIA_PAGE, page));
    }

    public List<SharedMusic> getAudioMessages() {
        return mAudioMessages;
    }

    public void forceClose() {
        notifyObservers(new NotificationObject(NotificationObject.FORCE_CLOSE_SHARED_ACTIVITY, null));
    }

    public void cleanSearchMedia(boolean forProfile, boolean isOldChatId) {
        if (forProfile) {
            if (isOldChatId) {
                mPhotoAndVideoProfileMessages.clear();
            } else {
                mPhotoAndVideoProfileMessagesSecond.clear();
            }
        } else
            mPhotoAndVideoSharedMessages.clear();
    }

    private List<SharedMedia> getRequiredForDeleteList(boolean forProfile, boolean isOldChatId) {
        if (forProfile) {
            if (isOldChatId) {
                return mPhotoAndVideoProfileMessages;
            } else {
                return mPhotoAndVideoProfileMessagesSecond;
            }
        } else
            return mPhotoAndVideoSharedMessages;
    }

    public void searchMedia(long chatId, int formId, final boolean forProfile, final boolean isOldChatId) {
        //фото и видео
        TdApi.SearchMessages searchMessages = new TdApi.SearchMessages();
        searchMessages.chatId = chatId;
        searchMessages.limit = forProfile ? 50 : 100;
        searchMessages.fromId = formId;
        searchMessages.filter = new TdApi.SearchMessagesFilterPhotoAndVideo();
        searchMessages.query = "*/*";

        client().send(searchMessages, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                if (object.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                    final TdApi.Messages messages = (TdApi.Messages) object;

                    ThreadService.runTaskBackground(new Runnable() {
                        @Override
                        public void run() {
                            int remDate = 0;
                            for (int i = 0; i < messages.messages.length; i++) {
                                TdApi.Message message = messages.messages[i];
                                if (!forProfile) {
                                    //проверять дату
                                    if (remDate == 0) {
                                        remDate = message.date;
                                        mPhotoAndVideoSharedMessages.add(new SharedMedia(true, DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA)));
                                        processMedia(message, false, isOldChatId, messages.totalCount);
                                    } else {
                                        if (DateUtil.isDifMonths(remDate, message.date)) {
                                            mPhotoAndVideoSharedMessages.add(new SharedMedia(true, DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA)));
                                            processMedia(message, false, isOldChatId, messages.totalCount);
                                        } else {
                                            processMedia(message, false, isOldChatId, messages.totalCount);
                                        }
                                        remDate = message.date;
                                    }
                                } else {
                                    processMedia(message, true, isOldChatId, messages.totalCount);
                                }
                            }

                            SharedMediaManager.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_MEDIA_LIST, null));
                        }
                    });

                }
            }
        });
    }

    private void processMedia(TdApi.Message message, boolean forProfile, boolean isOldChatId, int totalCount) {
        SharedMedia sharedMedia = new SharedMedia();
        sharedMedia.message = message;
        sharedMedia.isDate = false;
        sharedMedia.totalCount = totalCount;

        switch (message.message.getConstructor()) {
            case TdApi.MessageVideo.CONSTRUCTOR:
                TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) message.message;
                sharedMedia.isVideo = true;
                sharedMedia.videoTime = ChatHelper.getDurationString(messageVideo.video.duration);
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                sharedMedia.isVideo = false;

                TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message.message;

                TdApi.PhotoSize[] photoSizes = messagePhoto.photo.photos;
                //ищим s либо a
                int a = -1;
                int s = -1;
                int prevIteration = 0;
                for (int j = 0; j < photoSizes.length; j++) {
                    TdApi.PhotoSize photoSize = photoSizes[j];
                    if (photoSize.type != null && photoSize.type.equals("s")) {
                        s = j;
                    }
                    if (photoSize.type != null && photoSize.type.equals("a")) {
                        a = j;
                    }
                    if (photoSize.width > AndroidUtil.WINDOW_PORTRAIT_WIDTH) {
                        break;
                    }
                    prevIteration = j;
                }
                sharedMedia.thumbIndex = a != -1 ? a : s;
                sharedMedia.photoIndex = prevIteration;
                break;
        }
        if (forProfile) {
            if (isOldChatId) {
                mPhotoAndVideoProfileMessages.add(sharedMedia);
            } else {
                mPhotoAndVideoProfileMessagesSecond.add(sharedMedia);
            }
        } else
            mPhotoAndVideoSharedMessages.add(sharedMedia);
    }

    public void cleanSearchAudio() {
        mAudioMessages.clear();
    }

    public void searchAudio(long chatId, int formId) {
        TdApi.SearchMessages searchMessages = new TdApi.SearchMessages();
        searchMessages.chatId = chatId;
        searchMessages.limit = 100;
        searchMessages.fromId = formId;
        searchMessages.filter = new TdApi.SearchMessagesFilterAudio();
        searchMessages.query = "*/*";

        client().send(searchMessages, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                if (object.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                    final TdApi.Messages messages = (TdApi.Messages) object;

                    ThreadService.runTaskBackground(new Runnable() {
                        @Override
                        public void run() {
                            int remDate = 0;
                            for (int i = 0; i < messages.messages.length; i++) {
                                TdApi.Message message = messages.messages[i];
                                //проверять дату
                                if (remDate == 0) {
                                    remDate = message.date;
                                    mAudioMessages.add(new SharedMusic(DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA), true));
                                    processAudio(message);
                                } else {
                                    if (DateUtil.isDifMonths(remDate, message.date)) {
                                        mAudioMessages.add(new SharedMusic(DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA), true));
                                        processAudio(message);
                                    } else {
                                        processAudio(message);
                                    }
                                    remDate = message.date;
                                }
                            }

                            SharedMediaManager.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_AUDIO_LIST, null));
                        }
                    });
                }
            }
        });
    }


    private void processAudio(TdApi.Message message) {
        switch (message.message.getConstructor()) {
            case TdApi.MessageAudio.CONSTRUCTOR:
                TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) message.message;
                SharedMusic sharedMusic = new SharedMusic();
                sharedMusic.isDivider = false;

                if (TextUtil.isBlank(messageAudio.audio.performer)) {
                    messageAudio.audio.performer = AndroidUtil.getResourceString(R.string.unknown);
                }

                if (TextUtil.isBlank(messageAudio.audio.title)) {
                    messageAudio.audio.title = AndroidUtil.getResourceString(R.string.unknown);
                }

                sharedMusic.performer = messageAudio.audio.performer;
                sharedMusic.name = messageAudio.audio.title;

                sharedMusic.message = message;
                mAudioMessages.add(sharedMusic);
                break;
        }
    }


    public void searchAudioPlayer(long chatId, final List<TdApi.Message> messageList) {
        final AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        TdApi.SearchMessages searchMessages = new TdApi.SearchMessages();
        searchMessages.chatId = chatId;
        searchMessages.limit = 100;
        searchMessages.fromId = UserManager.getManager().getCurrUserId();
        searchMessages.filter = new TdApi.SearchMessagesFilterAudio();
        searchMessages.query = "*/*";
        client().send(searchMessages, new ResultController() {
            @Override
            public void afterResult(final TdApi.TLObject object, int calledConstructor) {
                ThreadService.runTaskBackground(new Runnable() {
                    @Override
                    public void run() {
                        messageList.clear();
                        if (object.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                            TdApi.Messages messages = (TdApi.Messages) object;
                            final List<TdApi.Message> tempList = Arrays.asList(messages.messages);

                            if (audioPlayer.getMessage() != null) {
                                int id = audioPlayer.getMsgId();
                                for (int i = 0; i < tempList.size(); i++) {
                                    if (id == tempList.get(i).id) {
                                        audioPlayer.setCurrentTrackPosition(i);
                                        break;
                                    }

                                    if (i == tempList.size() - 1) {
                                        audioPlayer.setCurrentTrackPosition(-1);
                                    }
                                }
                            } else {
                                audioPlayer.setCurrentTrackPosition(-1);
                            }
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    messageList.addAll(tempList);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

    }
}
