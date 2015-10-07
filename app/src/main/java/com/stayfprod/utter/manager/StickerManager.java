package com.stayfprod.utter.manager;

import android.support.v7.widget.LinearLayoutManager;
import android.widget.GridView;

import com.stayfprod.emojicon.EmojiConstants;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.StickerMicroThumb;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.util.Logs;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StickerManager extends ResultController {

    private static volatile StickerManager userStickerManager;
    private List<TdApi.Sticker> cachedStickers = Collections.synchronizedList(new ArrayList<TdApi.Sticker>(200));
    private List<StickerMicroThumb> cachedTitleStickers = Collections.synchronizedList(new ArrayList<StickerMicroThumb>(20));
    private Long currentSet = -1L;

    private boolean isNeedUpdate;

    public Long getCurrentSet() {
        return currentSet;
    }

    public void setCurrentSet(Long currentSet) {
        this.currentSet = currentSet;
    }

    public List<StickerMicroThumb> getCachedTitleStickers() {
        return cachedTitleStickers;
    }

    public List<TdApi.Sticker> getCachedStickers() {
        return cachedStickers;
    }


    private GridView stickerThumbGridView;

    public LinearLayoutManager getMicroStickerLinearLayoutManager() {
        return microStickerLinearLayoutManager;
    }

    public void setMicroStickerLinearLayoutManager(LinearLayoutManager microStickerLinearLayoutManager) {
        this.microStickerLinearLayoutManager = microStickerLinearLayoutManager;
    }

    public GridView getStickerThumbGridView() {
        return stickerThumbGridView;
    }

    public void setStickerThumbGridView(GridView stickerThumbGridView) {
        this.stickerThumbGridView = stickerThumbGridView;
    }

    private LinearLayoutManager microStickerLinearLayoutManager;

    public void cleanStickerViews(){
        stickerThumbGridView = null;
        microStickerLinearLayoutManager = null;
    }

    public static StickerManager getManager() {
        if (userStickerManager == null) {
            synchronized (StickerManager.class) {
                if (userStickerManager == null) {
                    userStickerManager = new StickerManager();
                }
            }
        }
        return userStickerManager;
    }

    public TdApi.Sticker findStickerByPos(int pos) {
        return cachedStickers.get(pos);
    }

    public void destroy() {
        cachedStickers.clear();
        cachedTitleStickers.clear();
        userStickerManager = null;
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                try{
                    StickerRecentManager.getInstance().deleteRecentStickers();
                }catch (Exception e){
                    //
                }
            }
        });
    }

    public boolean isNeedRebuild() {
        return cachedStickers.isEmpty() || isNeedUpdate;
    }

    /*   public void getStickers() {
           TdApi.GetStickers func = new TdApi.GetStickers("");
           client().send(func, getManager());
       }
   */
    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        /*switch (object.getConstructor()) {
            case TdApi.Stickers.CONSTRUCTOR:
                TdApi.Sticker[] stickers = ((TdApi.Stickers) object).stickers;
                cachedStickers.clear();
                cachedStickers.addAll(Arrays.asList(stickers));
                break;
        }*/
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

    private AtomicInteger counter = new AtomicInteger(0);

    public void getStickers(boolean rebuild) {

        //если прийдет апдейт то запросить стикеры когда юзер выйдет из чата
        if (!cachedStickers.isEmpty() && !cachedTitleStickers.isEmpty() && !rebuild) {
            isNeedUpdate = true;
            return;
        }

        counter.set(0);
        TdApi.GetStickerSets getStickerSets = new TdApi.GetStickerSets();

        client().send(getStickerSets, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                switch (object.getConstructor()) {
                    case TdApi.StickerSets.CONSTRUCTOR: {
                        TdApi.StickerSets stickerSets = (TdApi.StickerSets) object;

                        final TdApi.StickerSetInfo[] sets = stickerSets.sets;
                        cachedStickers.clear();
                        cachedTitleStickers.clear();

                        StickerRecentManager.getInstance().loadRecents();
                        final int columnMaxCount = AbstractActivity.WINDOW_CURRENT_WIDTH / EmojiConstants.STICKER_THUMB_WIDTH;
                        final int rowsCount = (int) Math.ceil((double) cachedStickers.size() / columnMaxCount);
                        final int maxStickers = rowsCount * columnMaxCount;
                        final int dif = maxStickers - cachedStickers.size();
                        addGags(dif, cachedStickers);

                        List<StickerMicroThumb> microThumbList = new ArrayList<StickerMicroThumb>(2);
                        StickerMicroThumb smile = new StickerMicroThumb();
                        smile.resourceId = R.drawable.ic_smiles_smile;
                        microThumbList.add(smile);

                        StickerMicroThumb recent = new StickerMicroThumb();
                        recent.resourceId = R.drawable.ic_smiles_recent;
                        microThumbList.add(recent);

                        cachedTitleStickers.addAll(microThumbList);

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
                                                stickerMicroThumb.startPos = cachedStickers.size();
                                                cachedStickers.addAll(Arrays.asList(stickers));
                                                cachedTitleStickers.add(stickerMicroThumb);
                                                addGags(dif, cachedStickers);
                                            } else {
                                                //официальные стикеры
                                                StickerMicroThumb stickerMicroThumb = new StickerMicroThumb();
                                                stickerMicroThumb.sticker = stickers[0];
                                                stickerMicroThumb.startPos = cachedStickers.size();
                                                cachedStickers.addAll(Arrays.asList(stickers));
                                                cachedTitleStickers.add(stickerMicroThumb);
                                                addGags(dif, cachedStickers);
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
