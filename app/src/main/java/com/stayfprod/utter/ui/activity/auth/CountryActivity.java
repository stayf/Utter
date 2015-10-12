package com.stayfprod.utter.ui.activity.auth;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.ui.adapter.CountryAdapter;
import com.stayfprod.utter.model.Country;
import com.stayfprod.utter.manager.CountryManager;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.R;
import java.util.Arrays;
import java.util.List;

public class CountryActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_country);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.a_country_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager chatListLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(chatListLayoutManager);
        List<Country> countryList = Arrays.asList(CountryManager.COUNTRIES);
        CountryAdapter countryAdapter = new CountryAdapter(countryList, this);
        recyclerView.setAdapter(countryAdapter);
        setToolbar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_up_no, R.anim.slide_up_out);
    }

    @SuppressWarnings("ALL")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            TextView t_toolbar_title = (TextView) toolbar.findViewById(R.id.t_toolbar_title);
            t_toolbar_title.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            t_toolbar_title.setTextColor(0xffffffff);
            t_toolbar_title.setTextSize(20);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

}
