package com.stayfprod.emojicon;

import com.stayfprod.emojicon.emoji.Emojicon;

import android.content.Context;
import android.widget.GridView;

public class EmojiconRecentsGridView extends EmojiconGridView implements EmojiconRecents {

    private EmojiAdapter mAdapter;

    public EmojiconRecentsGridView(Context context, EmojIconsPopup emojiconsPopup) {
        gridView = new GridView(context);
        gridView.setBackgroundResource(android.R.color.transparent);
        gridView.setColumnWidth(EmojConstant.sEmojGridWidth);
        gridView.setHorizontalSpacing(0);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setVerticalSpacing(0);

        GridView.LayoutParams layoutParams = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(layoutParams);

        mEmojiconPopup = emojiconsPopup;

        EmojiconRecentsManager recents = EmojiconRecentsManager.getInstance(context);
        mAdapter = new EmojiAdapter(context, recents);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (mEmojiconPopup.onEmojiconClickedListener != null) {
                    mEmojiconPopup.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
            }
        });
        gridView.setAdapter(mAdapter);
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentsManager recents = EmojiconRecentsManager.getInstance(context);
        recents.push(emojicon);
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

}
