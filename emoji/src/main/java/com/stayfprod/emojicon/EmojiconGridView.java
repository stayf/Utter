package com.stayfprod.emojicon;
import com.stayfprod.emojicon.emoji.Emojicon;
import com.stayfprod.emojicon.emoji.People;
import java.util.Arrays;
import android.app.Activity;
import android.content.Context;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

public class EmojiconGridView{
	public GridView gridView;
	EmojiconsPopup mEmojiconPopup;
    EmojiconRecents mRecents;
    Emojicon[] mData;

    public EmojiconGridView(){

    }
    
    public EmojiconGridView(final Context context, Emojicon[] emojicons, EmojiconRecents recents, EmojiconsPopup emojiconPopup) {
		mEmojiconPopup = emojiconPopup;

		gridView = new GridView(context);
		//gridView.setBackgroundResource(android.R.color.transparent);
		gridView.setColumnWidth(EmojiConstants.EMOJI_GRID_VIEW_WIDTH);
		gridView.setHorizontalSpacing(0);
		gridView.setNumColumns(GridView.AUTO_FIT);
		gridView.setVerticalSpacing(0);

		GridView.LayoutParams layoutParams = new GridView.LayoutParams(
				GridView.LayoutParams.MATCH_PARENT,GridView.LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(layoutParams);

		setRecents(recents);
	        if (emojicons== null) {
	            mData = People.DATA;
	        } else {
	            Object[] o = (Object[]) emojicons;
	            mData = Arrays.asList(o).toArray(new Emojicon[o.length]);
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
