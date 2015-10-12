package com.stayfprod.utter.manager;

import android.support.v7.widget.LinearLayoutManager;
import android.widget.GridView;

import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.StickerMicroThumb;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.AbstractActivity;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StickerManager extends ResultController {

    private static volatile StickerManager sUserStickerManager;

    public static StickerManager getManager() {
        if (sUserStickerManager == null) {
            synchronized (StickerManager.class) {
                if (sUserStickerManager == null) {
                    sUserStickerManager = new StickerManager();
                }
            }
        }
        return sUserStickerManager;
    }

    private List<TdApi.Sticker> mCachedStickers = Collections.synchronizedList(new ArrayList<TdApi.Sticker>(200));
    private List<StickerMicroThumb> mCachedTitleStickers = Collections.synchronizedList(new ArrayList<StickerMicroThumb>(20));
    private Long mCurrentSet = -1L;
    private boolean mIsNeedUpdate;
    private GridView mStickerThumbGridView;
    private AtomicInteger mCounter = new AtomicInteger(0);

    public Long getCurrentSet() {
        return mCurrentSet;
    }

    public void setCurrentSet(Long currentSet) {
        this.mCurrentSet = currentSet;
    }

    public List<StickerMicroThumb> getCachedTitleStickers() {
        return mCachedTitleStickers;
    }

    public List<TdApi.Sticker> getCachedStickers() {
        return mCachedStickers;
    }

    public LinearLayoutManager getMicroStickerLinearLayoutManager() {
        return microStickerLinearLayoutManager;
    }

    public void setMicroStickerLinearLayoutManager(LinearLayoutManager microStickerLinearLayoutManager) {
        this.microStickerLinearLayoutManager = microStickerLinearLayoutManager;
    }

    public GridView getStickerThumbGridView() {
        return mStickerThumbGridView;
    }

    public void setStickerThumbGridView(GridView stickerThumbGridView) {
        this.mStickerThumbGridView = stickerThumbGridView;
    }

    private LinearLayoutManager microStickerLinearLayoutManager;

    public void cleanStickerViews() {
        mStickerThumbGridView = null;
        microStickerLinearLayoutManager = null;
    }

    public TdApi.Sticker findStickerByPos(int pos) {
        return mCachedStickers.get(pos);
    }

    public void destroy() {
        mCachedStickers.clear();
        mCachedTitleStickers.clear();
        sUserStickerManager = null;
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                StickerRecentManager.getInstance().deleteRecentStickers();
            }
        });
    }

    public boolean isNeedRebuild() {
        return mCachedStickers.isEmpty() || mIsNeedUpdate;
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {

    }

    public void addGags(int dif, List<TdApi.Sticker> stickers) {
        for (int i = 0; i < dif; i++) {
            stickers.add(new TdApi.Sticker());
        }
    }

    public void bubbleSortStickerSets(TdApi.StickerSetInfo[] sets) {
        boolean swapped = true;
        int j = 0;
        TdApi.StickerSetInfo tmp;
        while (swapped) {
            swapped = false;
            j++;
            for (int i = 0; i < sets.length - j; i++) {
                if (sets[i].rating > sets[i + 1].rating) {
                    tmp = sets[i];
                    sets[i] = sets[i + 1];
                    sets[i + 1] = tmp;
                    swapped = true;
                }
            }
        }
    }

    public void getStickers(boolean rebuild) {
        //если прийдет апдейт то запросить стикеры когда юзер выйдет из чата
        if (!mCachedStickers.isEmpty() && !mCachedTitleStickers.isEmpty() && !rebuild) {
            mIsNeedUpdate = true;
            return;
        }

        mCounter.set(0);
        TdApi.GetStickerSets getStickerSets = new TdApi.GetStickerSets();

        client().send(getStickerSets, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                switch (object.getConstructor()) {
                    case TdApi.StickerSets.CONSTRUCTOR: {
                        TdApi.StickerSets stickerSets = (TdApi.StickerSets) object;

                        final TdApi.StickerSetInfo[] sets = stickerSets.sets;
                        mCachedStickers.clear();
                        mCachedTitleStickers.clear();

                        StickerRecentManager.getInstance().loadRecents();
                        final int columnMaxCount = AbstractActivity.sWindowCurrentWidth / EmojConstant.sStickerThumbWidth;
                        final int rowsCount = (int) Math.ceil((double) mCachedStickers.size() / columnMaxCount);
                        final int maxStickers = rowsCount * columnMaxCount;
                        final int dif = maxStickers - mCachedStickers.size();
                        addGags(dif, mCachedStickers);

                        List<StickerMicroThumb> microThumbList = new ArrayList<StickerMicroThumb>(2);
                        StickerMicroThumb smile = new StickerMicroThumb();
                        smile.resourceId = R.drawable.ic_smiles_smile;
                        microThumbList.add(smile);

                        StickerMicroThumb recent = new StickerMicroThumb();
                        recent.resourceId = R.drawable.ic_smiles_recent;
                        microThumbList.add(recent);

                        mCachedTitleStickers.addAll(microThumbList);

                        bubbleSortStickerSets(sets);

                        for (int i = 0; i < sets.length; i++) {
                            TdApi.StickerSetInfo setInfo = sets[i];

                            TdApi.GetStickerSet getStickerSet = new TdApi.GetStickerSet();
                            getStickerSet.setId = setInfo.id;
                            client().send(getStickerSet, new ResultController() {
                                @Override
                                public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                    switch (object.getConstructor()) {
                                        case TdApi.StickerSet.CONSTRUCTOR: {
                                            TdApi.StickerSet stickerSet = (TdApi.StickerSet) object;


                                            TdApi.Sticker[] stickers = stickerSet.stickers;
                                            final int rowsCount = (int) Math.ceil((double) stickers.length / columnMaxCount);
                                            final int maxStickers = rowsCount * columnMaxCount;

                                            final int dif = maxStickers - stickers.length;

                                            if (!stickerSet.isOfficial) {
                                                StickerMicroThumb stickerMicroThumb = new StickerMicroThumb();
                                                stickerMicroThumb.sticker = stickers[0];
                                                stickerMicroThumb.startPos = mCachedStickers.size();
                                                mCachedStickers.addAll(Arrays.asList(stickers));
                                                mCachedTitleStickers.add(stickerMicroThumb);
                                                addGags(dif, mCachedStickers);
                                            } else {
                                                //официальные стикеры
                                                StickerMicroThumb stickerMicroThumb = new StickerMicroThumb();
                                                stickerMicroThumb.sticker = stickers[0];
                                                stickerMicroThumb.startPos = mCachedStickers.size();
                                                mCachedStickers.addAll(Arrays.asList(stickers));
                                                mCachedTitleStickers.add(stickerMicroThumb);
                                                addGags(dif, mCachedStickers);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        break;
                    }
                }
            }
        });
    }
}
