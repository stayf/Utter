package com.stayfprod.emojicon;
import com.stayfprod.emojicon.emoji.Emojicon;
import android.content.Context;

public interface EmojiconRecents {
    void addRecentEmoji(Context context, Emojicon emojicon);
}
