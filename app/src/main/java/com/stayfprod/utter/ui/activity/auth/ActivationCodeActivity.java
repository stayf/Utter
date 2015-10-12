package com.stayfprod.utter.ui.activity.auth;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.reciver.SMSMonitor;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.R;

public class ActivationCodeActivity extends AbstractActivity {
    private static String LOG = ActivationCodeActivity.class.getSimpleName();

    private SMSMonitor mSmsMonitor = null;
    private String mPhone = "";
    private TextView mError;
    private TextView mInfo;
    private EditText mCode;
    private IntentFilter mIntentFilter;
    private CircleProgressView mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;
        setContentView(R.layout.activity_activation_code);
        mSmsMonitor = new SMSMonitor() {
            @Override
            protected void afterReceiveMsg(String code) {
                try {
                    EditText editText = ((EditText) findViewById(R.id.a_activation_code_edit_text_code));
                    editText.setText(code);
                } catch (Exception e) {
                    Log.w(LOG, "afterReceiveMsg", e);
                }
            }
        };
        mIntentFilter = new IntentFilter(SMSMonitor.ACTION);

        setToolbar();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String phone = extras.getString("phone");
            if (phone != null && !phone.isEmpty()) {
                this.mPhone = phone;
            }
        }

        mError = (TextView) findViewById(R.id.a_activation_code_error);
        mCode = (EditText) findViewById(R.id.a_activation_code_edit_text_code);
        mInfo = (TextView) findViewById(R.id.a_activation_code_text_view_info);

        AndroidUtil.setErrorTextViewTypeface(mError);
        AndroidUtil.setTextViewTypeface(mInfo);
        AndroidUtil.setEditTextTypeface(mCode);

        mInfo.append(mPhone);

        mCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    private void processAction(CircleProgressView progressView) {
        if (!AuthManager.sIsButtonBlocked) {
            AuthManager.sIsButtonBlocked = true;
            progressView.start();
            mError.setVisibility(View.GONE);
            mCode.setBackgroundResource(R.drawable.edittext_bottom_line);
            String c = mCode.getText().toString();
            new AuthManager(ActivationCodeActivity.this, progressView).setPhoneCode(c);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            TextView tToolbarTitle = (TextView) toolbar.findViewById(R.id.t_toolbar_title);
            tToolbarTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            tToolbarTitle.setTextColor(0xffffffff);
            tToolbarTitle.setTextSize(20);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSmsMonitor();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterSmsMonitor();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    private boolean isRegisteredSmsMonitor = false;

    //два метода были в onResume и onPause работая стабильно
    public void registerSmsMonitor() {
        if (!isRegisteredSmsMonitor)
            try {
                registerReceiver(mSmsMonitor, mIntentFilter);
                isRegisteredSmsMonitor = true;
            } catch (Exception e) {
                Log.w(LOG, "registerSmsMonitor", e);
            }
    }

    public void unRegisterSmsMonitor() {
        if (isRegisteredSmsMonitor)
            try {
                unregisterReceiver(mSmsMonitor);
                isRegisteredSmsMonitor = false;
            } catch (Throwable e) {
                Log.w(LOG, "unRegisterSmsMonitor", e);
            }
    }
}
