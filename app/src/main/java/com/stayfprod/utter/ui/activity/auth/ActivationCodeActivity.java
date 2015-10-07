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
import com.stayfprod.utter.service.SMSMonitor;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.R;

public class ActivationCodeActivity extends AbstractActivity {
    private static String LOG = ActivationCodeActivity.class.getSimpleName();

    private SMSMonitor smsMonitor = null;
    private String phone = "";
    private TextView error;
    private TextView info;
    private EditText code;
    private IntentFilter intentFilter;
    private CircleProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;
        setContentView(R.layout.activity_activation_code);
        smsMonitor = new SMSMonitor() {
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
        intentFilter = new IntentFilter(SMSMonitor.ACTION);

        setToolbar();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String phone = extras.getString("phone");
            if (phone != null && !phone.isEmpty()) {
                this.phone = phone;
            }
        }

        error = (TextView) findViewById(R.id.a_activation_code_error);
        code = (EditText) findViewById(R.id.a_activation_code_edit_text_code);
        info = (TextView) findViewById(R.id.a_activation_code_text_view_info);

        AndroidUtil.setErrorTextViewTypeface(error);
        AndroidUtil.setTextViewTypeface(info);
        AndroidUtil.setEditTextTypeface(code);

        info.append(phone);


        code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    processAction(progressView);
                    return true;
                }
                return false;
            }
        });
    }

    private void processAction(CircleProgressView progressView) {
        if (!AuthManager.isButtonBlocked) {
            AuthManager.isButtonBlocked = true;
            progressView.start();
            error.setVisibility(View.GONE);
            code.setBackgroundResource(R.drawable.edittext_bottom_line);
            String c = code.getText().toString();
            new AuthManager(ActivationCodeActivity.this, progressView).setPhoneCode(c);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            TextView t_toolbar_title = (TextView) toolbar.findViewById(R.id.t_toolbar_title);
            t_toolbar_title.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            t_toolbar_title.setTextColor(0xffffffff);
            t_toolbar_title.setTextSize(20);
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
        progressView = (CircleProgressView) rootView.findViewById(R.id.progressView);
        progressView.init(CircleProgressView.FOR_TOOLBAR);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processAction(progressView);
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
                registerReceiver(smsMonitor, intentFilter);
                isRegisteredSmsMonitor = true;
            } catch (Exception e) {
                Log.w(LOG, "registerSmsMonitor", e);
            }
    }

    public void unRegisterSmsMonitor() {
        if (isRegisteredSmsMonitor)
            try {
                unregisterReceiver(smsMonitor);
                isRegisteredSmsMonitor = false;
            } catch (Throwable e) {
                Log.w(LOG, "unRegisterSmsMonitor", e);
            }
    }
}
