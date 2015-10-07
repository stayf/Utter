package com.stayfprod.utter.ui.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.ui.adapter.holder.ContactHolder;

import java.util.TreeSet;

public class ContactListAdapter extends RecyclerView.Adapter<ContactHolder> {

    private Contact[] records;
    private Context context;

    public ContactListAdapter(Context context) {
        this.context = context;
    }

    public ContactListAdapter(TreeSet<Contact> records, Context context) {
        this.records = new Contact[records.size()];
        records.toArray(this.records);
        this.context = context;
    }

    public void setData(TreeSet<Contact> records) {
        this.records = new Contact[records.size()];
        records.toArray(this.records);
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactHolder(context);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.setValues(records[position], position, context);
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.length;
    }
}
