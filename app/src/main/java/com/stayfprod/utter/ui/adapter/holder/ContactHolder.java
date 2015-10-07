package com.stayfprod.utter.ui.adapter.holder;


import android.content.Context;
import android.widget.RelativeLayout;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.ui.view.ContactView;

public class ContactHolder extends AbstractHolder<Contact> {

    public ContactHolder(Context context) {
        super(new ContactView(context));
        itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                ContactView.LAYOUT_HEIGHT));
        itemView.setBackgroundResource(R.drawable.item_click_transparent);
    }

    @Override
    public void setValues(Contact record, int i, Context context) {
        ((ContactView)itemView).setValues(record,i,context);
    }
}
