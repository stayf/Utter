package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.adapter.holder.IntermediateHolder;
import com.stayfprod.utter.ui.adapter.holder.LoadingHolder;

import java.util.List;

public class IntermediateAdapter extends AbstractLoadingAdapter<AbstractHolder> {

    private List<ChatInfo> records;
    private Context context;

    public IntermediateAdapter(List<ChatInfo> records, Context context) {
        this.records = records;
        this.context = context;
    }

    @Override
    public AbstractHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return new LoadingHolder(context);
    }

    @Override
    public void onBindFooterView(AbstractHolder holder, int position) {

    }

    @Override
    public AbstractHolder onCreateBasicItemViewHolder(ViewGroup parent, int viewType) {
        return new IntermediateHolder(context, records);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindBasicItemView(AbstractHolder holder, int position) {
        ChatInfo record = records.get(position);
        holder.setValues(record, position, context);
    }

    @Override
    public int getBasicItemCount() {
        return records.size();
    }

    @Override
    public int getBasicItemType(int position) {
        return 0;
    }
}

