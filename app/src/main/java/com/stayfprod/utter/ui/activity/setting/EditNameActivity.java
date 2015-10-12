package com.stayfprod.utter.ui.activity.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class EditNameActivity extends AbstractActivity {

    private static final String LOG = EditNameActivity.class.getSimpleName();

    public static final int FOR_CURRENT_USER = 0;
    public static final int FOR_PROFILE_USER = 1;
    public static final int FOR_GROUP = 2;

    private EditText firstName;
    private EditText secondName;
    private ImageView a_edit_name_user;
    private TextView a_edit_name_phone;

    private boolean isBlockedButton;
    private Integer type;
    private int userId;
    private String phone;
    private String initials;
    private TdApi.File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_edit_name);
        firstName = (EditText) findViewById(R.id.a_set_name_first);
        secondName = (EditText) findViewById(R.id.a_set_name_second);
        ImageView a_edit_name_ic_user = findView(R.id.a_edit_name_ic_user);
        AndroidUtil.setEditTextTypeface(firstName);
        AndroidUtil.setEditTextTypeface(secondName);
        setToolbar();

        a_edit_name_phone = findView(R.id.a_edit_name_phone);
        a_edit_name_user = findView(R.id.a_edit_name_user);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            type = bundle.getInt("type");
            userId = bundle.getInt("userId");
            firstName.setText(bundle.getString("firstName"));
            firstName.setSelection(firstName.getText().length());
            secondName.setText(bundle.getString("secondName"));
            phone = bundle.getString("phone", "");

            initials = bundle.getString("initials", "");
            file = (TdApi.File) bundle.getSerializable("file");
        }

        if (type == null) {
            type = FOR_CURRENT_USER;
        }

        switch (type) {
            case FOR_CURRENT_USER:
                break;
            case FOR_PROFILE_USER:
                a_edit_name_phone.setVisibility(View.VISIBLE);
                a_edit_name_user.setVisibility(View.VISIBLE);
                a_edit_name_phone.setText("+" + phone);

                a_edit_name_phone.setTextColor(Color.BLACK);
                a_edit_name_phone.setTextSize(18);
                a_edit_name_phone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                a_edit_name_ic_user.setVisibility(View.GONE);

                IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, userId, initials, file);
                a_edit_name_user.setImageDrawable(dr);

                ((RelativeLayout.LayoutParams) firstName.getLayoutParams()).leftMargin = Constant.DP_18;
                break;
            case FOR_GROUP:
                a_edit_name_ic_user.setImageResource(R.mipmap.ic_groupusers);
                secondName.setVisibility(View.GONE);
                firstName.setHint(AndroidUtil.getResourceString(R.string.chat_name));
                break;
        }
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
                if (!isBlockedButton) {
                    UserManager userManager = UserManager.getManager();
                    switch (type) {
                        case FOR_CURRENT_USER:
                            if (TextUtil.isNotBlank(firstName.getText().toString())) {
                                isBlockedButton = true;
                                progressView.start();
                                userManager.changeCurrentUserName(firstName.getText().toString(), secondName.getText().toString(),
                                        new ResultController() {
                                            @Override
                                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                progressView.stop();
                                                isBlockedButton = false;
                                                if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                                                    AndroidUtil.runInUI(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            EditNameActivity.this.onBackPressed();
                                                        }
                                                    });
                                                } else {
                                                    if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                                                        TdApi.Error error = (TdApi.Error) object;
                                                        String errorText = error.text.toLowerCase();
                                                        //Logs.e(errorText);
                                                        if (errorText.contains("name_not_modified")) {
                                                            AndroidUtil.runInUI(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    EditNameActivity.this.onBackPressed();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                            break;
                        case FOR_PROFILE_USER:
                            if (TextUtil.isNotBlank(firstName.getText().toString())) {
                                try {
                                    isBlockedButton = true;
                                    progressView.start();
                                    TdApi.InputContact[] inputContacts = new TdApi.InputContact[1];
                                    inputContacts[0] = new TdApi.InputContact();
                                    inputContacts[0].firstName = firstName.getText().toString();
                                    inputContacts[0].lastName = secondName.getText().toString();
                                    inputContacts[0].phoneNumber = phone;
                                    userManager.importContacts(inputContacts, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            progressView.stop();
                                            isBlockedButton = false;
                                            if (object.getConstructor() != TdApi.Error.CONSTRUCTOR) {
                                                AndroidUtil.runInUI(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        EditNameActivity.this.onBackPressed();
                                                    }
                                                });
                                            } else {
                                                TdApi.Error error = (TdApi.Error) object;
                                                String errorText = error.text.toLowerCase();
                                                if (errorText.contains("name_not_modified")) {
                                                    AndroidUtil.runInUI(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            EditNameActivity.this.onBackPressed();
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    progressView.stop();
                                    isBlockedButton = false;
                                    Log.e(LOG, "click", e);
                                }
                            }
                            break;
                        case FOR_GROUP:
                            ChatManager chatManager = ChatManager.getManager();
                            if (TextUtil.isNotBlank(firstName.getText().toString())) {
                                isBlockedButton = true;
                                progressView.start();
                                chatManager.changeChatTitle(firstName.getText().toString(),
                                        new ResultController() {
                                            @Override
                                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                progressView.stop();
                                                isBlockedButton = false;
                                                if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                                                    AndroidUtil.runInUI(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            EditNameActivity.this.onBackPressed();
                                                        }
                                                    });
                                                } else {
                                                    if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                                                        TdApi.Error error = (TdApi.Error) object;
                                                        String errorText = error.text.toLowerCase();
                                                        if (errorText.contains("name_not_modified")) {
                                                            AndroidUtil.runInUI(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    EditNameActivity.this.onBackPressed();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                            break;
                    }

                }
            }
        });
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
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


    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }
}
