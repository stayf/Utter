package com.stayfprod.utter.ui.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.DrawerMenu;
import com.stayfprod.utter.util.AndroidUtil;

public class MenuDrawerAdapter extends ArrayAdapter<DrawerMenu> {

    public MenuDrawerAdapter(Context context, List<DrawerMenu> data) {
        super(context, R.layout.item_drawer_menu, data);
    }

    public MenuDrawerAdapter(Context context, DrawerMenu[] data) {
        super(context, R.layout.item_drawer_menu, data);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), R.layout.item_drawer_menu, null);
            ViewHolder holder = new ViewHolder();
            holder.logoutImage = (ImageView) v.findViewById(R.id.i_drawer_menu_icon);
            holder.logoutText = (TextView) v.findViewById(R.id.i_drawer_menu_text);

            holder.logoutText.setTextColor(Color.BLACK);
            holder.logoutText.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            holder.logoutText.setTextSize(15);

            v.setTag(holder);
        }

        DrawerMenu drawerMenu = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.logoutText.setText(drawerMenu.text);
        holder.logoutImage.setImageResource(drawerMenu.icon);

        return v;
    }

    class ViewHolder {
        ImageView logoutImage;
        TextView logoutText;
    }
}