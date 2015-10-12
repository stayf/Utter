package com.stayfprod.utter.ui.view;


import android.content.Context;
import android.widget.GridView;

import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.utter.ui.adapter.StickerAdapter;
import com.stayfprod.utter.manager.StickerManager;

import com.stayfprod.emojicon.EmojiconGridView;

public class StickerGridView extends EmojiconGridView {

    public StickerGridView(Context context) {

        gridView = new GridView(context);
        gridView.setColumnWidth(EmojConstant.sStickerThumbWidth);
        gridView.setHorizontalSpacing(0);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setVerticalSpacing(EmojConstant.dp(12));
        gridView.setVerticalScrollBarEnabled(true);
        gridView.setPadding(0, EmojConstant.dp(12), 0, EmojConstant.dp(12));
        gridView.setClipToPadding(false);

        GridView.LayoutParams layoutParams = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);

        gridView.setLayoutParams(layoutParams);

        StickerAdapter stickerAdapter = new StickerAdapter(context, StickerManager.getManager().getCachedStickers());
        gridView.setAdapter(stickerAdapter);

        StickerManager stickerManager = StickerManager.getManager();
        stickerManager.setStickerThumbGridView(gridView);
    }

}