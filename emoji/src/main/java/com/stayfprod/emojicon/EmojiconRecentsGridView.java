package com.stayfprod.emojicon;

import com.stayfprod.emojicon.emoji.Emojicon;
import com.stayfprod.emojicon.emoji.People;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.GridView;

import java.util.Arrays;


public class EmojiconRecentsGridView extends EmojiconGridView implements EmojiconRecents {
    EmojiAdapter mAdapter;

    public EmojiconRecentsGridView(Context context, Emojicon[] emojicons,
                                   EmojiconRecents recents, EmojiconsPopup emojiconsPopup) {
        //super(context, emojicons, recents, emojiconsPopup,0);

        gridView = new GridView(context);
        gridView.setBackgroundResource(android.R.color.transparent);
        gridView.setColumnWidth(EmojiConstants.EMOJI_GRID_VIEW_WIDTH);
        gridView.setHorizontalSpacing(0);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setVerticalSpacing(0);

        GridView.LayoutParams layoutParams = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(layoutParams);

        mEmojiconPopup = emojiconsPopup;

        EmojiconRecentsManager recents1 = EmojiconRecentsManager.getInstance(context);
        mAdapter = new EmojiAdapter(context, recents1);
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
