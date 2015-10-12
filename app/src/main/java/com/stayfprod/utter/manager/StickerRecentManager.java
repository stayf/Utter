package com.stayfprod.utter.manager;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.utter.App;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StickerRecentManager {

    private static String LOG = StickerRecentManager.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static StickerRecentManager sInstance;

    private List<TdApi.Sticker> mTempStickerSet;//используется для сохранения

    public int loadRecentCollectionSize;
    public int currentRecentCollectionSize;

    public static StickerRecentManager getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new StickerRecentManager();
                }
            }
        }
        return sInstance;
    }

    public void loadRecents() {
        try {
            File file = FileUtil.createRecentStickersFile();
            mTempStickerSet = FileUtil.loadObject(file);
            if (mTempStickerSet == null) {
                loadRecentCollectionSize = 0;
                mTempStickerSet = new ArrayList<>(10);
            } else {
                loadRecentCollectionSize = mTempStickerSet.size();
            }
            currentRecentCollectionSize = loadRecentCollectionSize;
            List<TdApi.Sticker> cachedStickers = StickerManager.getManager().getCachedStickers();
            cachedStickers.clear();
            cachedStickers.addAll(mTempStickerSet);
        } catch (Exception e) {
            Log.w(LOG, "", e);
        }
    }

    public void deleteRecentStickers() {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), App.STG_RECENT_STICKERS);
            FileUtil.cleanDirectory(file);
        } catch (Exception e) {
            //
        }
    }

    @SuppressWarnings("RedundantStringConstructorCall")
    public void addRecentSticker(TdApi.Sticker sticker) {

        TdApi.Sticker recentSticker = new TdApi.Sticker();
        recentSticker.isRecent = true;
        recentSticker.height = sticker.height;
        recentSticker.width = sticker.width;
        recentSticker.setId = sticker.setId;
        recentSticker.emoji = new String(sticker.emoji);

        TdApi.File stickerFile = new TdApi.File();
        stickerFile.id = sticker.sticker.id;
        stickerFile.path = new String(sticker.sticker.path);
        stickerFile.persistentId = sticker.sticker.persistentId;
        stickerFile.size = sticker.sticker.size;
        recentSticker.sticker = stickerFile;

        recentSticker.thumb = new TdApi.PhotoSize();
        recentSticker.thumb.height = sticker.thumb.height;
        recentSticker.thumb.width = sticker.thumb.width;
        recentSticker.thumb.type = sticker.thumb.type;

        TdApi.File stickerThumbPhoto = new TdApi.File();
        stickerThumbPhoto.id = sticker.thumb.photo.id;
        stickerThumbPhoto.size = sticker.thumb.photo.size;
        stickerThumbPhoto.path = new String(sticker.thumb.photo.path);
        stickerThumbPhoto.persistentId = sticker.thumb.photo.persistentId;

        recentSticker.thumb.photo = stickerThumbPhoto;

        Iterator<TdApi.Sticker> iterator = mTempStickerSet.iterator();
        while (iterator.hasNext()) {
            TdApi.Sticker stickerTmp = iterator.next();
            //info удаляем gags и копии того же самого стикера!!
            if (stickerTmp.sticker != null) {
                if (stickerTmp.setId == recentSticker.setId &&
                        (stickerTmp.sticker.id == recentSticker.sticker.id
                                || stickerTmp.sticker.path.equals(recentSticker.sticker.path))) {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }

        mTempStickerSet.add(0, recentSticker);

        final int columnMaxCount = AbstractActivity.sWindowCurrentWidth / EmojConstant.sStickerThumbWidth;
        final int rowsCount = (int) Math.ceil((double) mTempStickerSet.size() / columnMaxCount);
        final int maxStickers = rowsCount * columnMaxCount;

        final int dif = maxStickers - mTempStickerSet.size();

        StickerManager.getManager().addGags(dif, mTempStickerSet);
    }

    @SuppressLint("CommitPrefEdits")
    public void saveRecents() {
        try {
            File file = FileUtil.createRecentStickersFile();
            FileUtil.saveObject(file, mTempStickerSet);
            List<TdApi.Sticker> cachedStickers = StickerManager.getManager().getCachedStickers();

            Iterator<TdApi.Sticker> iterator = cachedStickers.iterator();
            //info удаление всех ресентов и gags, после просто заного всю коллекцию добавляем!!
            while (iterator.hasNext()) {
                TdApi.Sticker sticker = iterator.next();
                if (sticker.isRecent || sticker.sticker == null) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            currentRecentCollectionSize = mTempStickerSet.size();
            cachedStickers.addAll(0, mTempStickerSet);
        } catch (Exception e) {
            Log.e(LOG, "", e);
            Crashlytics.logException(e);
        }
    }
}
