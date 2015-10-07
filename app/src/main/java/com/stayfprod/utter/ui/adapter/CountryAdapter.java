package com.stayfprod.utter.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stayfprod.utter.model.Country;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.R;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {

    private List<Country> countryList;
    private AppCompatActivity activity;

    public CountryAdapter(List<Country> countryList, Context context) {
        this.countryList = countryList;
        this.activity = (AppCompatActivity) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Country country = countryList.get(position);
        holder.code.setText("+" + country.code + "");
        holder.name.setText(country.name);
        holder.title.setText(country.title);
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ViewHolder viewHolder;

        public TextView title;
        public TextView code;
        public TextView name;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.viewHolder = this;

            title = (TextView) itemView.findViewById(R.id.i_county_title);
            code = (TextView) itemView.findViewById(R.id.i_county_code);
            name = (TextView) itemView.findViewById(R.id.i_country_name);

            itemView.setBackgroundResource(R.drawable.item_click_transparent);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Country c = countryList.get(viewHolder.getPosition());
                    Intent data = new Intent();
                    data.putExtra("data", c);
                    activity.setResult(Activity.RESULT_OK, data);
                    activity.supportFinishAfterTransition();
                    activity.overridePendingTransition(R.anim.slide_up_no, R.anim.slide_up_out);
                }
            });

            code.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            code.setTextColor(0xFF2F8CC9);
            code.setTextSize(17);

            name.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            name.setTextColor(0xFF212121);
            name.setTextSize(17);

            title.setTypeface(AndroidUtil.TF_ROBOTO_BOLD);
            title.setTextColor(0xFF808080);
            title.setTextSize(22);
        }
    }
}
