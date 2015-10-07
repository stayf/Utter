package com.stayfprod.utter.ui.view;


import android.content.Context;
import android.widget.GridView;

import com.stayfprod.emojicon.EmojiConstants;
import com.stayfprod.utter.ui.adapter.StickerAdapter;
import com.stayfprod.utter.manager.StickerManager;

import com.stayfprod.emojicon.EmojiconGridView;

public class StickerGridView extends EmojiconGridView {

    public StickerGridView(Context context) {

        gridView = new GridView(context);
        gridView.setColumnWidth(EmojiConstants.STICKER_THUMB_WIDTH);
        gridView.setHorizontalSpacing(0);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setVerticalSpacing(EmojiConstants.dp(12));
        gridView.setVerticalScrollBarEnabled(true);
        gridView.setPadding(0, EmojiConstants.dp(12), 0, EmojiConstants.dp(12));
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