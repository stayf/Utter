package com.stayfprod.emojicon;

import com.stayfprod.emojicon.emoji.Emojicon;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EmojiAdapter extends BaseAdapter {
    EmojiconGridView.OnEmojiconClickedListener emojiClickListener;

    private List<Emojicon> data;
    private Context context;

    public EmojiAdapter(Context context, List<Emojicon> data) {
        this.data = data;
        this.context = context;
    }

    public EmojiAdapter(Context context, Emojicon[] data) {
        this.data = Arrays.asList(data);
        this.context = context;
    }

    public void setEmojiClickListener(EmojiconGridView.OnEmojiconClickedListener listener) {
        this.emojiClickListener = listener;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Emojicon getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        EmojiView v = (EmojiView)convertView;
        if (v == null) {
            v = new EmojiView(context);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            v.setLayoutParams(new AbsListView.LayoutParams(
                    EmojiConstants.EMOJI_DP_LAYOUT_SIZE, EmojiConstants.EMOJI_DP_LAYOUT_SIZE));
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