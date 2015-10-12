package com.stayfprod.utter.ui.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.ui.adapter.holder.ContactHolder;

import java.util.TreeSet;

public class ContactListAdapter extends RecyclerView.Adapter<ContactHolder> {

    private Contact[] mRecords;
    private Context mContext;

    public ContactListAdapter(Context context) {
        this.mContext = context;
    }

    public ContactListAdapter(TreeSet<Contact> records, Context context) {
        this.mRecords = new Contact[records.size()];
        records.toArray(this.mRecords);
        this.mContext = context;
    }

    public void setData(TreeSet<Contact> records) {
        this.mRecords = new Contact[records.size()];
        records.toArray(this.mRecords);
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(mContext);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.setValues(mRecords[position], position, mContext);
    }

    @Override
    public int getItemCount() {
        return mRecords == null ? 0 : mRecords.length;
    }
}
