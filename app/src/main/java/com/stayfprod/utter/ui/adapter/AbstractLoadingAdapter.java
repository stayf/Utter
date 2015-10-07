package com.stayfprod.utter.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.stayfprod.utter.util.AndroidUtil;

public abstract class AbstractLoadingAdapter<X extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<X> {

    protected static final int TYPE_LOADING = Integer.MIN_VALUE + 1;
    private volatile boolean isLoading;

    @Override
    public X onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING)
            return onCreateFooterViewHolder(parent, viewType);
        return onCreateBasicItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(X holder, int position) {
        if (position == getBasicItemCount() && holder.getItemViewType() == TYPE_LOADING) {
            onBindFooterView(holder, position);
        } else {
            onBindBasicItemView(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = getBasicItemCount();
        if (isLoading)
            itemCount += 1;
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getBasicItemCount() && isLoading) {
            return TYPE_LOADING;
        }
        return getBasicItemType(position);
    }

    public void setLoadingData(){
        isLoading = true;
    }

    public void updateDataAfterLoading(){
        isLoading = false;
        notifyDataSetChanged();
    }

    public void updateDataAfterLoadingAsync(){
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                updateDataAfterLoading();
            }
        });
    }

    public abstract X onCreateFooterViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindFooterView(X holder, int position);

    public abstract X onCreateBasicItemViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindBasicItemView(X holder, int position);

    public abstract int getBasicItemCount();

    public abstract int getBasicItemType(int position);
}
