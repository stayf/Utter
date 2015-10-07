package com.stayfprod.emojicon;


import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;

public interface StickerMicroThumbAdapter {
    void setPager(ViewPager viewPager);

    void setStickerGridView(EmojiconGridView stickerGridView);

    void setLayoutManager(LinearLayoutManager linearLayoutManager);
}
