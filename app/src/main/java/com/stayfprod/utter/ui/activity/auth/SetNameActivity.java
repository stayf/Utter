package com.stayfprod.utter.ui.activity.auth;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.R;
import com.stayfprod.utter.util.TextUtil;

public class SetNameActivity extends AbstractActivity {

    private EditText mFirstName;
    private EditText mSecondName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;
        setContentView(R.layout.activity_set_name);

        mFirstName = (EditText) findViewById(R.id.a_set_name_first);
        mSecondName = (EditText) findViewById(R.id.a_set_name_second);
        TextView info = (TextView) findViewById(R.id.a_set_name_info);
        AndroidUtil.setTextViewTypeface(info);
        AndroidUtil.setEditTextTypeface(mFirstName);
        AndroidUtil.setEditTextTypeface(mSecondName);
        setToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        MenuItem menuItem = menu.findItem(R.id.action_ok);
        menuItem.setActionView(R.layout.action_check_layout);
        FrameLayout rootView = (FrameLayout) menuItem.getActionView();
        rootView.findViewById(R.id.ic_check);
        final CircleProgressView progressView = (CircleProgressView) rootView.findViewById(R.id.progressView);
        progressView.init(CircleProgressView.FOR_TOOLBAR);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fn = mFirstName.getText().toString();
                if (TextUtil.isNotBlank(fn)) {
                    if (!AuthManager.sIsButtonBlocked) {
                        AuthManager.sIsButtonBlocked = true;
                        progressView.start();
                        String ln = mSecondName.getText().toString();
                        new AuthManager(SetNameActivity.this, progressView).setUserName(fn, ln);
                    }
                }
            }
        });
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            TextView tToolbarTitle = (TextView) toolbar.findViewById(R.id.t_toolbar_title);
            tToolbarTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            tToolbarTitle.setTextColor(0xffffffff);
            tToolbarTitle.setTextSize(20);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {

    }
}
