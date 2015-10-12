package com.stayfprod.emojicon;

import com.stayfprod.emojicon.emoji.Emojicon;
import com.stayfprod.emojicon.emoji.People;

import java.util.Arrays;

import android.content.Context;
import android.widget.GridView;

public class EmojiconGridView {

    public GridView gridView;
    protected EmojIconsPopup mEmojiconPopup;
    private EmojiconRecents mRecents;
    private Emojicon[] mData;

    public EmojiconGridView() {

    }

    public EmojiconGridView(final Context context, Emojicon[] emojIcons, EmojiconRecents recents, EmojIconsPopup emojiconPopup) {
        mEmojiconPopup = emojiconPopup;

        gridView = new GridView(context);
        gridView.setColumnWidth(EmojConstant.sEmojGridWidth);
        gridView.setHorizontalSpacing(0);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setVerticalSpacing(0);

        GridView.LayoutParams layoutParams = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(layoutParams);

        setRecents(recents);
        if (emojIcons == null) {
            mData = People.DATA;
        } else {
            Object[] objects = (Object[]) emojIcons;
            mData = Arrays.asList(objects).toArray(new Emojicon[objects.length]);
        }
        EmojiAdapter mAdapter = new EmojiAdapter(context, mData);
        mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (mEmojiconPopup.onEmojiconClickedListener != null) {
                    mEmojiconPopup.onEmojiconClickedListener.onEmojiconClicked(emojicon);
                }
                if (mRecents != null) {
                    mRecents.addRecentEmoji(context, emojicon);
                }
            }
        });
        gridView.setAdapter(mAdapter);
    }

    protected void setRecents(EmojiconRecents recents) {
        mRecents = recents;
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }

}
