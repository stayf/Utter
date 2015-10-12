package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.emojicon.EmojiconGridView;
import com.stayfprod.emojicon.StickerMicroThumbAdapter;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.StickerManager;
import com.stayfprod.utter.manager.StickerRecentManager;
import com.stayfprod.utter.model.StickerMicroThumb;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class StickerMicroThumbAdapterImpl extends RecyclerView.Adapter<StickerMicroThumbAdapterImpl.Holder> implements StickerMicroThumbAdapter {

    private static final String LOG = StickerMicroThumbAdapterImpl.class.getSimpleName();

    private LinearLayoutManager mLinearLayoutManager;
    private List<StickerMicroThumb> mData;
    private Context mContext;
    private ViewPager mViewPager;
    private EmojiconGridView mStickerGridView;
    private boolean mIsVisible;

    public StickerMicroThumbAdapterImpl(Context context) {
        this.mData = StickerManager.getManager().getCachedTitleStickers();
        this.mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        relativeLayout.setLayoutParams(layoutParams);


        ImageView imageView = new ImageView(mContext);
        relativeLayout.addView(imageView);
        relativeLayout.setPadding(EmojConstant.dp(10), 0, EmojConstant.dp(10), 0);

        imageView.setId(R.id.image_micro_thumb);

        RelativeLayout.LayoutParams imageLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        imageLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        imageLP.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        imageLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageLP.addRule(RelativeLayout.CENTER_VERTICAL);

        return new Holder(relativeLayout, imageView);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int i) {
        mIsVisible = true;
        View itemView = viewHolder.itemView;
        itemView.setTag(i);
        ImageView imageView = viewHolder.imageView;
        RelativeLayout.LayoutParams imageLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        StickerMicroThumb stickerMicroThumb = mData.get(i);
        if (i == 0 || i == 1) {
            imageView.setImageResource(mData.get(i).resourceId);
            imageLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            imageLP.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            if (i == 0) {
                imageLP.leftMargin = EmojConstant.dp(14);
            } else {
                imageLP.leftMargin = 0;
            }
        } else {
            int size = EmojConstant.dp(28);
            imageLP.width = size;
            imageLP.height = size;
            imageLP.leftMargin = 0;
            if (stickerMicroThumb != null && stickerMicroThumb.sticker != null && stickerMicroThumb.sticker.thumb != null) {
                if (FileUtil.isTDFileLocal(stickerMicroThumb.sticker.thumb.photo)) {
                    BitmapDrawable bitmapDrawable = FileManager.getManager().getStickerFromFile(stickerMicroThumb.sticker.thumb.photo.path,
                            FileManager.TypeLoad.USER_STICKER_MICRO_THUMB, itemView, itemView.getTag().toString(), imageView);
                    if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
                        imageView.setImageDrawable(bitmapDrawable);
                    } else {
                        imageView.setImageBitmap(null);
                    }
                } else {
                    int fileId = stickerMicroThumb.sticker.sticker.id;
                    if (fileId > 0) {
                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.USER_STICKER_MICRO_THUMB,
                                stickerMicroThumb.sticker.thumb.photo.id, i, -1, stickerMicroThumb.sticker, imageView, itemView, itemView.getTag().toString());
                        imageView.setImageBitmap(null);
                    }
                }
            }
        }

        if (stickerMicroThumb != null && stickerMicroThumb.isSelected) {
            itemView.setBackgroundColor(0xFFE2E5E7);
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void setPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
    }

    @Override
    public void setStickerGridView(EmojiconGridView stickerGridView) {
        this.mStickerGridView = stickerGridView;
        stickerGridView.gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            boolean isFirst = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                try {
                    if (mIsVisible) {
                        StickerManager stickerManager = StickerManager.getManager();
                        TdApi.Sticker sticker = stickerManager.findStickerByPos(firstVisibleItem);
                        if (sticker.isRecent) {
                            if (stickerManager.getCurrentSet() != -1 || isFirst) {
                                isFirst = false;
                                stickerManager.setCurrentSet(-1L);
                                updateMicroThumbBackground(-1L);
                            }
                        } else {
                            if (stickerManager.getCurrentSet() != sticker.setId) {
                                long oldId = stickerManager.getCurrentSet();
                                stickerManager.setCurrentSet(sticker.setId);
                                updateMicroThumbBackground(oldId, sticker.setId);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG, "onScroll", e);
                    Crashlytics.logException(e);
                }
            }
        });
    }

    @Override
    public void setLayoutManager(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
        StickerManager stickerManager = StickerManager.getManager();
        stickerManager.setMicroStickerLinearLayoutManager(this.mLinearLayoutManager);
    }

    private void updateMicroThumbBackground(long newId) {
        for (int i = 1; i < mData.size(); i++) {
            StickerMicroThumb stickerMicroThumb = mData.get(i);
            stickerMicroThumb.isSelected = false;

            if (stickerMicroThumb.sticker != null) {
                if (stickerMicroThumb.sticker.setId == newId) {
                    stickerMicroThumb.isSelected = true;
                    mLinearLayoutManager.scrollToPosition(i);

                }
            }
        }

        if (newId == -1) {
            mData.get(1).isSelected = true;
            mLinearLayoutManager.scrollToPosition(0);
        }
        //без runInUI не работает на версии 4,1
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, 1);
    }

    private void updateMicroThumbBackground(long oldId, long newId) {
        int counter = 0;

        if (newId == -1) {
            mData.get(1).isSelected = true;
            mLinearLayoutManager.scrollToPosition(0);
        } else {
            if (oldId == -1) {
                mData.get(1).isSelected = false;
            }
            for (int i = 2; i < mData.size(); i++) {
                StickerMicroThumb stickerMicroThumb = mData.get(i);
                if (stickerMicroThumb.sticker.setId == oldId) {
                    stickerMicroThumb.isSelected = false;
                    counter++;
                } else {
                    stickerMicroThumb.isSelected = false;
                }

                if (stickerMicroThumb.sticker.setId == newId) {
                    stickerMicroThumb.isSelected = true;
                    if (i == 2) {
                        mLinearLayoutManager.scrollToPosition(0);
                    } else
                        mLinearLayoutManager.scrollToPosition(i);
                    counter++;
                }

                if (counter == 2) {
                    break;
                }

                if (oldId == -1 && counter == 1) {
                    break;
                }
            }
        }
        //без runInUI не работает на версии 4,1
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, 1);

    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public Holder(View itemView, ImageView imageView) {
            super(itemView);
            this.imageView = imageView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = Holder.this.getAdapterPosition();
                        //java.lang.ArrayIndexOutOfBoundsException: length=20; index=-1 если используем саппорт либу!!
                        if (position != -1) {
                            StickerMicroThumb stickerMicroThumb = mData.get(position);
                            if (position == 0) {
                                StickerMicroThumbAdapterImpl.this.mViewPager.setCurrentItem(0);
                            } else if (position == 1) {
                                mStickerGridView.gridView.setSelection(0);
                            } else {
                                StickerRecentManager stickerRecentManager = StickerRecentManager.getInstance();
                                int difPos = stickerRecentManager.currentRecentCollectionSize - stickerRecentManager.loadRecentCollectionSize;
                                if (difPos < 0) {
                                    difPos = 0;
                                }
                                mStickerGridView.gridView.setSelection(stickerMicroThumb.startPos + difPos);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(LOG, "click", e);
                        Crashlytics.logException(e);
                    }
                }
            });
        }
    }
}
