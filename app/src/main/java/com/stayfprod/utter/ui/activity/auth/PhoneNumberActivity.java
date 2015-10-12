package com.stayfprod.utter.ui.activity.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.model.Country;
import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.manager.CountryManager;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.R;

public class PhoneNumberActivity extends AbstractActivity {
    private static final int REQUEST_COUNTRY = 44;

    private Context mContext;
    private EditText mCountryCode;
    private EditText mCountry;
    private TextView mError;
    private EditText mPhone;
    private CircleProgressView mProgressView;

    private void openCountryActivity() {
        Intent intent_info = new Intent(mContext, CountryActivity.class);
        startActivityForResult(intent_info, REQUEST_COUNTRY);
        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_no);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COUNTRY) {
            if (resultCode == RESULT_OK) {
                Country c = data.getExtras().getParcelable("data");
                mCountryCode.setText("+" + c.code);
                mCountry.setText(c.name);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_phone_number);
        mContext = this;

        mPhone = (EditText) findViewById(R.id.a_phone_number_phone);
        TextView info = (TextView) findViewById(R.id.a_phone_number_info);
        Country c = CountryManager.defineCountry(this);

        mCountry = (EditText) findViewById(R.id.a_phone_number_country);
        mCountryCode = (EditText) findViewById(R.id.a_phone_number_country_code);

        mCountry.setText(c.name);
        mCountryCode.setText("+" + c.code);

        mCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCountryActivity();
            }
        });

        mCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    openCountryActivity();
                }
            }
        });
        mError = (TextView) findViewById(R.id.a_phone_number_error);
        AndroidUtil.setErrorTextViewTypeface(mError);

        AndroidUtil.setTextViewTypeface(info);
        AndroidUtil.setEditTextTypeface(mPhone);
        AndroidUtil.setEditTextTypeface(mCountry);
        AndroidUtil.setEditTextTypeface(mCountryCode);
        setToolbar();

        mPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    processAction(mProgressView);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        MenuItem menuItem = menu.findItem(R.id.action_ok);
        menuItem.setActionView(R.layout.action_check_layout);
        FrameLayout rootView = (FrameLayout) menuItem.getActionView();
        rootView.findViewById(R.id.ic_check);
        mProgressView = (CircleProgressView) rootView.findViewById(R.id.progressView);
        mProgressView.init(CircleProgressView.FOR_TOOLBAR);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processAction(mProgressView);
            }
        });
        return true;
    }

    private void processAction(CircleProgressView progressView) {
        if (!AuthManager.sIsButtonBlocked) {
            AuthManager.sIsButtonBlocked = true;
            progressView.start();
            mError.setVisibility(View.GONE);
            mPhone.setBackgroundResource(R.drawable.edittext_bottom_line);
            String strPhone = mCountryCode.getText().toString() + mPhone.getText().toString();
            new AuthManager(PhoneNumberActivity.this, progressView).setPhoneNumber(strPhone);
        }
    }

    @SuppressWarnings("ALL")
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

}
