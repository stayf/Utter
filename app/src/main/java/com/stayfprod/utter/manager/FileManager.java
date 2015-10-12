package com.stayfprod.utter.manager;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.stayfprod.utter.model.AbstractChatMsg;
import com.stayfprod.utter.service.CacheService;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.ui.view.chat.PhotoMsgView;
import com.stayfprod.utter.ui.view.chat.StickerMsgView;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.App;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager extends ResultController {
    private static final String LOG = FileManager.class.getSimpleName();

    public enum TypeLoad {
        USER_IMAGE,
        DOCUMENT_THUMB,
        DOCUMENT,
        PHOTO,
        PHOTO_THUMB,
        USER_STICKER_MICRO_THUMB,
        USER_STICKER_THUMB,
        USER_STICKER,
        CHAT_STICKER,
        CHAT_STICKER_THUMB,
        VIDEO_THUMB,
        VIDEO,
        VOICE,
        AUDIO,
        CHAT_LIST_ICON,
        CHAT_ICON,
        BOT_COMMAND_ICON,
        UPDATE_CHAT_PHOTO,
        CONTACT_ICON,
        SHARED_VIDEO_THUMB,
        SHARED_PHOTO,
        SHARED_PHOTO_THUMB,
        SHARED_AUDIO,
        SHARED_AUDIO_PLAYER,
        CHANGE_CHAT_TITLE_IMAGE
    }

    public static class StorageObject {
        public TypeLoad typeLoad;
        public Object[] obj;
        public StringBuffer loadMsg[] = new StringBuffer[2];
        public int processLoad = -1;
        public boolean isCanceled = false;
        public int pos;
        public int msgId = -1;

        public StorageObject(TypeLoad typeLoad, Object[] obj, int pos, int msgId) {
            this.typeLoad = typeLoad;
            this.obj = obj;
            this.pos = pos;
            this.msgId = msgId;
        }
    }

    public static volatile boolean sCanDownloadFile = true;
    private static volatile FileManager sFileManager;

    //Ключ: file_id
    private final ConcurrentHashMap<Integer, StorageObject> mStorageMap = new ConcurrentHashMap<Integer, StorageObject>(40);
    //Если по одному и тому же id имется элементы на разных позициях
    private final ConcurrentHashMap<Integer, List<StorageObject>> mStorageAdditionMap = new ConcurrentHashMap<Integer, List<StorageObject>>(40);
    //info Индекс по msgId. Все что добавляется в mStorageMap и mStorageAdditionMap индесируется им
    private final ConcurrentHashMap<Integer, StorageObject> mIndexStorageAndAdditionMap = new ConcurrentHashMap<Integer, StorageObject>(40);

    //Временно хранилище документов на закачку
    private final ConcurrentHashMap<Integer, TempStorageObject> mTempStorageMap = new ConcurrentHashMap<Integer, TempStorageObject>(40);

    public static FileManager getManager() {
        if (sFileManager == null) {
            synchronized (ChatListManager.class) {
                if (sFileManager == null) {
                    sFileManager = new FileManager();
                }
            }
        }
        return sFileManager;
    }

    public StorageObject getAndRemoveStorageObject(int key) {
        synchronized (mStorageMap) {
            return mStorageMap.remove(key);
        }
    }

    //нужен для элементов с прогресс барами
    public StorageObject getStorageObject(int fileId) {
        return mStorageMap.get(fileId);
    }

    public List<FileManager.StorageObject> getAdditionStorageObject(int fileId) {
        return mStorageAdditionMap.get(fileId);
    }

    public boolean isHaveStorageObjectByFileID(int fileId, int msgId) {
        return mStorageMap.get(fileId) != null;
    }

    //стикеры, изображения, фамбы
    public List<StorageObject> getAndRemoveAdditionStorageObject(int key) {
        synchronized (mStorageMap) {
            return mStorageAdditionMap.remove(key);
        }
    }

    public void removeIndexStorageAndAdditionMap(Integer msgId) {
        if (msgId != null) {
            mIndexStorageAndAdditionMap.remove(msgId);
        }
    }

    public StorageObject updateStorageObjectAsync(final TypeLoad typeLoad, final int fileId, final int msgId, final Object... obj) {
        StorageObject storageObject = mIndexStorageAndAdditionMap.get(msgId);

        if (storageObject != null && storageObject.typeLoad != typeLoad) {
            return null;
        }

        if (storageObject != null) {
            storageObject.obj = obj;
        } else {
            StorageObject storageObjectMap = mStorageMap.get(fileId);
            if (storageObjectMap != null) {
                uploadFileAsync(typeLoad, fileId, -1, msgId, obj);
                return storageObjectMap;
            } else {
                return null;
            }
        }
        return storageObject;
    }

    private boolean addToStorageObject(TypeLoad typeLoad, Object[] obj, int fileId, int pos, int msgId) {
        if (mStorageMap.get(fileId) == null) {
            StorageObject storageObject = new StorageObject(typeLoad, obj, pos, msgId);
            mStorageMap.put(fileId, storageObject);
            if (msgId > 0)
                mIndexStorageAndAdditionMap.put(msgId, storageObject);
            return true;
        }
        return false;
    }

    private void addToAdditionStorageObject(TypeLoad typeLoad, Object[] obj, int fileId, int pos, int msgId) {
        StorageObject storageObject = new StorageObject(typeLoad, obj, pos, msgId);
        if (mStorageAdditionMap.get(fileId) == null) {
            List<StorageObject> objectList = new ArrayList<StorageObject>();
            objectList.add(storageObject);
            mStorageAdditionMap.put(fileId, objectList);
            if (msgId > 0)
                mIndexStorageAndAdditionMap.put(msgId, storageObject);
        } else {
            List<StorageObject> objectList = mStorageAdditionMap.get(fileId);
            boolean insert = true;
            //повторения не нужны
            for (int i = 0; i < objectList.size(); i++) {
                StorageObject o = objectList.get(i);
                //info загружаемый контент не из списка сообщений
                if (storageObject.msgId == -1) {
                    if (o.pos == storageObject.pos && o.typeLoad == storageObject.typeLoad) {
                        insert = false;
                        //подмена ссылки
                        storageObject.obj = obj;
                        break;
                    }
                } else {
                    if (o.msgId == storageObject.msgId && o.typeLoad == storageObject.typeLoad) {
                        insert = false;
                        //подмена ссылки
                        storageObject.obj = obj;
                        break;
                    }
                }
            }
            if (insert) {
                objectList.add(storageObject);
                if (msgId > 0)
                    mIndexStorageAndAdditionMap.put(msgId, storageObject);
            }
        }

    }

    //todo если удалили запись не успев загрузить нужно очистить хранилища с этой итерацией + отменить загрузку
    public void uploadFile(TypeLoad typeLoad, Object[] obj, int fileId, int pos, int msgId) {
        synchronized (mStorageMap) {
            //info pos == -1  если юзер нажал на кнопку загрузки
            if (sCanDownloadFile || pos == -1) {
                if (addToStorageObject(typeLoad, obj, fileId, pos, msgId)) {
                    TdApi.DownloadFile downloadFile = new TdApi.DownloadFile();
                    downloadFile.fileId = fileId;
                    client().send(downloadFile, getManager());
                } else {
                    //точно ли не тот же самый элемент ломится
                    StorageObject storageObject = mStorageMap.get(fileId);
                    if (storageObject != null) {
                        //info исправили на сравнение Id-шника сообщения вместо позиции
                        //если это не контент из списка сообщений
                        if (msgId == -1) {
                            //info даже сесли позиции совпали все равно обработает как надо
                            if (storageObject.pos != pos || storageObject.typeLoad != typeLoad) {
                                addToAdditionStorageObject(typeLoad, obj, fileId, pos, msgId);
                            } else {
                                //подменить ссылку
                                storageObject.obj = obj;
                            }
                        } else {
                            if (storageObject.msgId != msgId || storageObject.typeLoad != typeLoad) {
                                addToAdditionStorageObject(typeLoad, obj, fileId, pos, msgId);
                            } else {
                                //подменить ссылку
                                storageObject.obj = obj;
                            }
                        }
                    }
                }
            } else {
                //info На данный момент pos == -2 нигде нет
                if (pos != -2) {
                    this.mTempStorageMap.put(msgId, new TempStorageObject(typeLoad, obj, fileId, pos, msgId));
                }
            }
        }
    }

    public void uploadFileAsync(final TypeLoad typeLoad, final int fileId, final int pos, final int msgId, final Object... obj) {
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                uploadFile(typeLoad, obj, fileId, pos, msgId);
            }
        });
    }

    public static class TempStorageObject extends StorageObject {
        public int fileId;

        public TempStorageObject(TypeLoad typeLoad, Object[] obj, int fileId, int pos, int msgId) {
            super(typeLoad, obj, pos, msgId);
            this.fileId = fileId;
        }
    }

    public void proceedLoad(int fileId, int msgId, boolean isSendRequest) {
        if (mIndexStorageAndAdditionMap.containsKey(msgId)) {
            StorageObject storageObject = mIndexStorageAndAdditionMap.get(msgId);
            storageObject.isCanceled = false;
            TdApi.DownloadFile downloadFile = new TdApi.DownloadFile();
            downloadFile.fileId = fileId;
            if (isSendRequest) {
                client().send(downloadFile, getManager());
            }
        }
    }

    public void cancelDownloadFile(final int fileId, final int msgId, boolean isSendRequest) {
        if (mIndexStorageAndAdditionMap.containsKey(msgId)) {
            TdApi.CancelDownloadFile downloadFile = new TdApi.CancelDownloadFile();
            downloadFile.fileId = fileId;
            if (isSendRequest) {
                client().send(downloadFile, new ResultController() {
                    @Override
                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                        if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                            if (mIndexStorageAndAdditionMap.containsKey(msgId)) {
                                StorageObject storageObject = mIndexStorageAndAdditionMap.get(msgId);
                                storageObject.isCanceled = true;
                            }
                        }
                    }
                });
            } else {
                StorageObject storageObject = mIndexStorageAndAdditionMap.get(msgId);
                storageObject.isCanceled = true;
            }
        }
    }

    public void tryToLoad(final int firstVisible, final int lastVisible) {
        ThreadService.runSingleTaskWithLowestPriority(new Runnable() {
            @Override
            public void run() {
                //смотрим на текуще видимые элементы
                if (!mTempStorageMap.isEmpty()) {
                    ChatManager manager = ChatManager.getManager();
                    for (int i = firstVisible - 2; i <= lastVisible + 2; i++) {
                        AbstractChatMsg chatMsg = manager.getChatMsgByPos(i);

                        if (chatMsg != null && chatMsg.tgMessage != null && mTempStorageMap.containsKey(chatMsg.tgMessage.id)) {
                            TempStorageObject tempStorageObject = mTempStorageMap.remove(chatMsg.tgMessage.id);
                            uploadFileAsync(tempStorageObject.typeLoad, tempStorageObject.fileId, i, tempStorageObject.msgId, tempStorageObject.obj);
                        }
                    }
                }
            }
        });
    }

    public void cleanTempStorage() {
        mStorageMap.clear();
        mTempStorageMap.clear();
        mStorageAdditionMap.clear();
        mIndexStorageAndAdditionMap.clear();
    }

    @Deprecated
    public BitmapDrawable getBitmapIconFromFile(String path) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(path, FileUtil.prepareOptions(path, IconFactory.MAX_ICON_HEIGHT, FileUtil.CalculateType.BOTH));
            CacheService.getManager().addBitmapToMemoryCache(path, bitmapDrawable);
        }
        return bitmapDrawable;
    }

    public BitmapDrawable getBitmapFromGallery(final String path, final int id, final ImageView imageView, final Context context, final String tag) {
        BitmapDrawable b = CacheService.getManager().getBitmapDrawable(path + id);

        if (b == null) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (AndroidUtil.isItemViewVisible(imageView, tag)) {
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(App.getAppResources(), MediaStore.Images.Thumbnails.getThumbnail(
                                    context.getContentResolver(), id,
                                    MediaStore.Images.Thumbnails.MINI_KIND, null));
                            AndroidUtil.setImageAsyncWithAnim(imageView, bitmapDrawable, imageView, tag);
                            CacheService.getManager().addBitmapToMemoryCache(path + id, bitmapDrawable);
                        }
                    } catch (Throwable e) {
                        //много открытых курсоров?
                    }
                }
            });
        }
        return b;
    }

    public BitmapDrawable getBitmapFullScreen(File f) {
        String filePath = f.getAbsolutePath();
        return FileUtil.decodeFileInBitmapDrawable(filePath, FileUtil.prepareOptions(filePath, AndroidUtil.WINDOW_PORTRAIT_WIDTH, FileUtil.CalculateType.BY_WIDTH));
    }

    private BitmapDrawable processSticker(String path, TypeLoad type, boolean... async) {
        //защита от повторений
        if (async.length > 0 && async[0]) {
            BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path + type.name());
            if (bitmapDrawable != null) {
                return bitmapDrawable;
            }
        }
        int maxHeight = StickerMsgView.getStickerMaxHeight(type);
        return FileUtil.decodeFileInBitmapDrawable(path, maxHeight != 0 ? FileUtil.prepareOptions(path, maxHeight, FileUtil.CalculateType.BY_HEIGHT) : FileUtil.superBitmapOptions());
    }

    //для микро фамбов
    public BitmapDrawable getStickerFromFile(final String path, final TypeLoad type) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path + type.name());
        if (bitmapDrawable == null) {
            bitmapDrawable = processSticker(path, type);
            CacheService.getManager().addBitmapToMemoryCache(path + type.name(), bitmapDrawable);
        }
        return bitmapDrawable;
    }

    public BitmapDrawable getStickerFromFile(final String path, final TypeLoad type, int[] bounds) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path + type.name());
        if (bitmapDrawable == null) {
            bitmapDrawable = processSticker(path, type);
            StickerMsgView.setStickerBounds(bitmapDrawable, type, bounds);
            CacheService.getManager().addBitmapToMemoryCache(path + type.name(), bitmapDrawable);
        }
        return bitmapDrawable;
    }

    public BitmapDrawable getStickerFromFile(final String path, final TypeLoad type, final View itemView, final String tag, final ImageView imageView, final int... bounds) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path + type.name());
        if (bitmapDrawable == null) {
            if (WebpSupportManager.IS_NEED_NATIVE_LIB) {
                ThreadService.runTaskBackground(new Runnable() {
                    @Override
                    public void run() {
                        final BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path + type.name());
                        if (bitmapDrawable != null) {
                            if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                if (type == TypeLoad.USER_STICKER_MICRO_THUMB) {
                                    AndroidUtil.runInUI(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                                imageView.setImageDrawable(bitmapDrawable);
                                            }
                                        }
                                    });
                                } else {
                                    ((ImageUpdatable) itemView).setImageAndUpdateAsync(bitmapDrawable, true);
                                }
                            }
                        } else {
                            if (type == TypeLoad.USER_STICKER_MICRO_THUMB) {
                                WebpSupportManager.getManager().loadWebP(path, type, itemView, tag, imageView);
                            } else {
                                WebpSupportManager.getManager().loadWebP(path, type, itemView, tag, bounds);
                            }
                        }
                    }
                });
            } else {
                ThreadService.runTaskBackground(new Runnable() {
                    @Override
                    public void run() {
                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                            final BitmapDrawable bitmapDrawable = processSticker(path, type, true);
                            if (type == TypeLoad.USER_STICKER_MICRO_THUMB) {
                                AndroidUtil.runInUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                            imageView.setImageDrawable(bitmapDrawable);
                                        }
                                    }
                                });
                            } else {
                                StickerMsgView.setStickerBounds(bitmapDrawable, type, bounds);
                                if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                    ((ImageUpdatable) itemView).setImageAndUpdateAsync(bitmapDrawable, true);
                                }
                            }
                            CacheService.getManager().addBitmapToMemoryCache(path + type.name(), bitmapDrawable);
                        }
                    }
                });
            }
        }
        return bitmapDrawable;
    }

    /*
     * Асинхронное получение не скалированного изображения для CircleProgressBar
     * */
    public BitmapDrawable getNoResizeBitmapCircleProgressBar(final String path, final View itemView, final String tag) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        BitmapDrawable bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(path);
                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                            ((ImageUpdatable) itemView).setImageAndUpdateAsync(bitmapDrawable);
                        }
                        CacheService.getManager().addBitmapToMemoryCache(path, bitmapDrawable);
                    }
                }
            });
        }
        return bitmapDrawable;
    }

    /*
     * Не асинхронное получение не скалированного изображения(чаще всего блюр первью картинки, видео фамба, фамба на стикер) для UpdateHandler
     * */
    public BitmapDrawable getNoResizeBitmapFromFile(String path) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(path);
            CacheService.getManager().addBitmapToMemoryCache(path, bitmapDrawable);
        }
        return bitmapDrawable;
    }

    /*
     * Асинхронное получение не скалированного изображения(чаще всего блюр первью картинки, видео фамба, но не фамба на стикер)
     * */
    public BitmapDrawable getNoResizeBitmapFromFile(final String path, final View itemView, final String tag) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        BitmapDrawable bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(path);
                        if (AndroidUtil.isItemViewVisible(itemView, tag))
                            ((ImageUpdatable) itemView).setImageAndUpdateAsync(bitmapDrawable);

                        CacheService.getManager().addBitmapToMemoryCache(path, bitmapDrawable);
                    }
                }
            });
        }
        return bitmapDrawable;
    }

    /*
    * Не асинхронное получение картинке в чате для UpdateHandler
    * */
    public BitmapDrawable getImageFromFile(String path) {
        BitmapDrawable bitmapDrawable = CacheService.getManager().getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(path, FileUtil.prepareOptions(path, PhotoMsgView.MAX_IMAGE_CHAT_WIDTH, FileUtil.CalculateType.BOTH));
            CacheService.getManager().addBitmapToMemoryCache(path, bitmapDrawable);
        }
        return bitmapDrawable;
    }

    /*
    * Асинхронное получение картинки в чате
    * */
    public BitmapDrawable getImageFromFile(final String path, final View itemView, final String tag) {
        CacheService cacheService = CacheService.getManager();
        BitmapDrawable bitmapDrawable = cacheService.getBitmapDrawable(path);
        if (bitmapDrawable == null) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        BitmapDrawable drawable = FileUtil.decodeFileInBitmapDrawable(path, FileUtil.prepareOptions(path, PhotoMsgView.MAX_IMAGE_CHAT_WIDTH, FileUtil.CalculateType.BOTH));
                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                            ((ImageUpdatable) itemView).setImageAndUpdateAsync(drawable);
                        }
                        CacheService.getManager().addBitmapToMemoryCache(path, drawable);
                    }
                }
            });
        }
        return bitmapDrawable;
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Ok.CONSTRUCTOR: {

                break;
            }
            case TdApi.Error.CONSTRUCTOR: {
                TdApi.Error error = (TdApi.Error) object;
                Log.e(LOG, "Error Result:" + error.code + "," + error.text);
                break;
            }
        }
    }
}
