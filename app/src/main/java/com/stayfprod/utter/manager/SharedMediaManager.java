package com.stayfprod.utter.manager;


import android.util.SparseArray;
import android.util.SparseIntArray;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.SharedMedia;
import com.stayfprod.utter.model.SharedMusic;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.SharedMediaActivity;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.DateUtil;
import com.stayfprod.utter.util.Logs;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SharedMediaManager extends ResultController {
    //SearchMessages, SearchMessagesFilterUrl, SearchMessagesFilterVoice,
    // SearchMessagesFilterPhotoAndVideo, SearchMessagesFilterVoice, SearchMessagesFilterVideo,
    // SearchMessagesFilterPhoto, SearchMessagesFilterDocument, SearchMessagesFilterAudio, SearchMessagesFilterEmpty

    private static volatile SharedMediaManager sharedMediaManager;

    private List<SharedMusic> audioMessages = new ArrayList<SharedMusic>(100);
    private List<SharedMedia> photoAndVideoProfileMessages = new ArrayList<SharedMedia>(50);
    private List<SharedMedia> photoAndVideoProfileMessagesSecond = new ArrayList<SharedMedia>(50);
    private List<SharedMedia> photoAndVideoSharedMessages = new ArrayList<SharedMedia>(100);

    private SparseArray<SharedMusic> selectedMusicList = new SparseArray<SharedMusic>(100);
    private SparseArray<SharedMedia> selectedMediaList = new SparseArray<SharedMedia>(100);

    public SparseArray<SharedMedia> getSelectedMediaList() {
        return selectedMediaList;
    }

    public SparseArray<SharedMusic> getSelectedMusicList() {
        return selectedMusicList;
    }

    public void cleanSelectedMusic() {
        for (int i = 0; i < selectedMusicList.size(); i++) {
            int key = selectedMusicList.keyAt(i);
            SharedMusic obj = selectedMusicList.get(key);
            obj.isSelected = false;
        }
        selectedMusicList.clear();
    }

    public void cleanSelectedMedia() {
        for (int i = 0; i < selectedMediaList.size(); i++) {
            int key = selectedMediaList.keyAt(i);
            SharedMedia obj = selectedMediaList.get(key);
            obj.isSelected = false;
        }
        selectedMediaList.clear();
    }

    public void deleteSelectedMusicList(final Long chatId) {
        if (chatId != null) {
            final int[] msgs = new int[selectedMusicList.size()];

            for (int i = 0; i < selectedMusicList.size(); i++) {
                int key = selectedMusicList.keyAt(i);
                SharedMusic sharedMusic = selectedMusicList.get(key);
                sharedMusic.isDeleted = true;
                msgs[i] = sharedMusic.message.id;
            }

            selectedMusicList.clear();
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
                                    for (Iterator<SharedMusic> it2 = audioMessages.iterator(); it2.hasNext(); ) {
                                        SharedMusic sharedMusic = it2.next();
                                        if (sharedMusic.isDeleted) {
                                            it2.remove();
                                        }
                                    }

                                    SharedMediaManager.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_AUDIO_LIST, null));

                                    //todo удаление в чате
                                    //cleanSearchAudio();
                                    //searchAudio(chatId, UserManager.getManager().getCurrUserId());
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
            final int[] msgs = new int[selectedMediaList.size()];

            for (int i = 0; i < selectedMediaList.size(); i++) {
                int key = selectedMediaList.keyAt(i);
                SharedMedia sharedMedia = selectedMediaList.get(key);
                sharedMedia.isDeleted = true;
                msgs[i] = sharedMedia.message.id;
            }

            selectedMediaList.clear();
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
                                    /*List<SharedMedia> profList = getRequiredForDeleteList(true, isOldChatId);
                                    for (Iterator<SharedMedia> it = profList.iterator(); it.hasNext(); ) {
                                        SharedMedia sharedMedia = it.next();
                                        if (sharedMedia.isDeleted) {
                                            it.remove();
                                        } else {
                                            sharedMedia.totalCount = sharedMedia.totalCount - msgs.length;
                                        }
                                    }*/

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
                                    //todo удаление в чате
                                    /*cleanSearchMedia(true, isOldChatId);
                                    cleanSearchMedia(false, isOldChatId);*/
                                    //searchMedia(chatId, UserManager.getManager().getCurrUserId(), false, isOldChatId);
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
        return photoAndVideoSharedMessages;
    }

    public List<SharedMedia> getPhotoAndVideoProfileMessages(boolean isOldChatId) {
        if (isOldChatId) {
            return photoAndVideoProfileMessages;
        } else {
            return photoAndVideoProfileMessagesSecond;
        }
    }

    public String getPhotoAndVideoMessagesStringSize(boolean isOldChatId) {
        if (isOldChatId) {
            if (photoAndVideoProfileMessages != null) {
                if (photoAndVideoProfileMessages.size() > 0) {
                    return String.valueOf(photoAndVideoProfileMessages.get(0).totalCount);
                }
            }
        } else {
            if (photoAndVideoProfileMessagesSecond != null) {
                if (photoAndVideoProfileMessagesSecond.size() > 0) {
                    return String.valueOf(photoAndVideoProfileMessagesSecond.get(0).totalCount);
                }
            }
        }
        return "0";
    }

    public void setCurrentPage(int page) {
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_SHARED_MEDIA_PAGE, page));
    }

    public List<SharedMusic> getAudioMessages() {
        return audioMessages;
    }

    public static SharedMediaManager getManager() {
        if (sharedMediaManager == null) {
            synchronized (SharedMediaManager.class) {
                if (sharedMediaManager == null) {
                    sharedMediaManager = new SharedMediaManager();
                }
            }
        }
        return sharedMediaManager;
    }

    public void forceClose() {
        notifyObservers(new NotificationObject(NotificationObject.FORCE_CLOSE_SHARED_ACTIVITY, null));
    }

    public void cleanSearchMedia(boolean forProfile, boolean isOldChatId) {
        if (forProfile) {
            if (isOldChatId) {
                photoAndVideoProfileMessages.clear();
            } else {
                photoAndVideoProfileMessagesSecond.clear();
            }
        } else
            photoAndVideoSharedMessages.clear();
    }

    private List<SharedMedia> getRequiredForDeleteList(boolean forProfile, boolean isOldChatId) {
        if (forProfile) {
            if (isOldChatId) {
                return photoAndVideoProfileMessages;
            } else {
                return photoAndVideoProfileMessagesSecond;
            }
        } else
            return photoAndVideoSharedMessages;
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
                                        photoAndVideoSharedMessages.add(new SharedMedia(true, DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA)));
                                        processMedia(message, false, isOldChatId, messages.totalCount);
                                    } else {
                                        if (DateUtil.isDifMonths(remDate, message.date)) {
                                            photoAndVideoSharedMessages.add(new SharedMedia(true, DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA)));
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
                photoAndVideoProfileMessages.add(sharedMedia);
            } else {
                photoAndVideoProfileMessagesSecond.add(sharedMedia);
            }
        } else
            photoAndVideoSharedMessages.add(sharedMedia);
    }

    public void cleanSearchAudio() {
        audioMessages.clear();
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
                                    audioMessages.add(new SharedMusic(DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA), true));
                                    processAudio(message);
                                } else {
                                    if (DateUtil.isDifMonths(remDate, message.date)) {
                                        audioMessages.add(new SharedMusic(DateUtil.getDateForChat(message.date, DateUtil.DateType.SHARED_MEDIA), true));
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
                audioMessages.add(sharedMusic);
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
