package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.stayfprod.emojicon.EmojiConstants;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.ui.view.StickerThumbView;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.Logs;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class StickerAdapter extends BaseAdapter {

    List<TdApi.Sticker> data;
    private final Context context;

    public StickerAdapter(Context context, List<TdApi.Sticker> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public TdApi.Sticker getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        StickerThumbView v = (StickerThumbView) convertView;

        if (v == null) {
            v = new StickerThumbView(context);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(EmojiConstants.STICKER_THUMB_WIDTH, EmojiConstants.STICKER_THUMB_HEIGHT);
            v.setLayoutParams(layoutParams);
        }

        TdApi.Sticker sticker = data.get(position);
        v.setTag(position);

        if (sticker != null && sticker.thumb != null) {
            v.setSticker(sticker);
            if (FileUtils.isTDFileLocal(sticker.thumb.photo)) {
                BitmapDrawable bitmapDrawable = FileManager.getManager().getStickerFromFile(sticker.thumb.photo.path,
                        FileManager.TypeLoad.USER_STICKER_THUMB, v, v.getTag().toString(), null);
                v.setStickerDrawable(bitmapDrawable);
            } else {
                int fileId = sticker.sticker.id;
                if (fileId > 0) {
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.USER_STICKER_THUMB,
                            sticker.thumb.photo.id, position, -1, sticker, v, v.getTag().toString(), 0, 0, fileId, position);
                }
                v.setStickerDrawable(null);
            }
        } else {
            v.setSticker(null);
            v.setStickerDrawable(null);
        }

        return v;
    }

}