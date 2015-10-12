package com.stayfprod.emojicon;

import com.stayfprod.emojicon.emoji.Emojicon;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class EmojiAdapter extends BaseAdapter {

    EmojiconGridView.OnEmojiconClickedListener emojiClickListener;

    private List<Emojicon> mData;
    private Context mContext;

    public EmojiAdapter(Context context, List<Emojicon> data) {
        this.mData = data;
        this.mContext = context;
    }

    public EmojiAdapter(Context context, Emojicon[] data) {
        this.mData = Arrays.asList(data);
        this.mContext = context;
    }

    public void setEmojiClickListener(EmojiconGridView.OnEmojiconClickedListener listener) {
        this.emojiClickListener = listener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Emojicon getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        EmojiView v = (EmojiView) convertView;
        if (v == null) {
            v = new EmojiView(mContext);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            v.setLayoutParams(new AbsListView.LayoutParams(
                    EmojConstant.sEmojDpLayoutSize, EmojConstant.sEmojDpLayoutSize));
        }

        Emojicon emoji = getItem(position);
        v.setEmojDrawable(emoji.getEmoji());
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiClickListener.onEmojiconClicked(getItem(position));
            }
        });

        return v;
    }
}