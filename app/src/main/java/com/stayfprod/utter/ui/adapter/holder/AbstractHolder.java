package com.stayfprod.utter.ui.adapter.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class AbstractHolder<T> extends RecyclerView.ViewHolder {
    public AbstractHolder(View itemView) {
        super(itemView);
    }

    public String getItemViewTag() {
        Object tag = itemView.getTag();
        return tag != null ? itemView.getTag().toString() : "";
    }

    public int getItemViewIntTag() {
        Object tag = itemView.getTag();
        return tag != null ? (int) itemView.getTag() : -1;
    }

    public abstract void setValues(T record, int i, Context context);
}
