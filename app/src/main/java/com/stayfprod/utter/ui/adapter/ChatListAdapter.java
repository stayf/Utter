package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.adapter.holder.ChatListHolder;
import com.stayfprod.utter.ui.adapter.holder.LoadingHolder;

import java.util.List;

public class ChatListAdapter extends AbstractLoadingAdapter<AbstractHolder> {

    private View.OnTouchListener mItemTouchListener;
    private List<ChatInfo> mRecords;
    private Context mContext;

    public ChatListAdapter(List<ChatInfo> records, Context context) {
        this.mRecords = records;
        this.mContext = context;
    }

    @Override
    public AbstractHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
      return new LoadingHolder(mContext);
    }

    @Override
    public void onBindFooterView(AbstractHolder holder, int position) {

    }

    @Override
    public AbstractHolder onCreateBasicItemViewHolder(ViewGroup parent, int viewType) {
        return new ChatListHolder(mContext, mRecords, mItemTouchListener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindBasicItemView(AbstractHolder holder, int position) {
        ChatInfo record = mRecords.get(position);
        holder.setValues(record, position, mContext);
    }

    @Override
    public int getBasicItemCount() {
        return mRecords.size();
    }

    @Override
    public int getBasicItemType(int position) {
        return 0;
    }

    /*
    * Объявить до onCreateViewHolder
    * */
    public void setOnItemTouchListener(View.OnTouchListener itemTouchListener) {
        this.mItemTouchListener = itemTouchListener;
    }
}

