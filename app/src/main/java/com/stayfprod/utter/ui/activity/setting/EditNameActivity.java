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

    private EditText mFirstName;
    private EditText mSecondName;
    private ImageView mEditNameUser;
    private TextView mEditNamePhone;

    private boolean mIsBlockedButton;
    private Integer mType;
    private int mUserId;
    private String mPhone;
    private String mInitials;
    private TdApi.File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_edit_name);
        mFirstName = (EditText) findViewById(R.id.a_set_name_first);
        mSecondName = (EditText) findViewById(R.id.a_set_name_second);
        ImageView a_edit_name_ic_user = findView(R.id.a_edit_name_ic_user);
        AndroidUtil.setEditTextTypeface(mFirstName);
        AndroidUtil.setEditTextTypeface(mSecondName);
        setToolbar();

        mEditNamePhone = findView(R.id.a_edit_name_phone);
        mEditNameUser = findView(R.id.a_edit_name_user);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mType = bundle.getInt("type");
            mUserId = bundle.getInt("userId");
            mFirstName.setText(bundle.getString("firstName"));
            mFirstName.setSelection(mFirstName.getText().length());
            mSecondName.setText(bundle.getString("secondName"));
            mPhone = bundle.getString("phone", "");

            mInitials = bundle.getString("initials", "");
            mFile = (TdApi.File) bundle.getSerializable("file");
        }

        if (mType == null) {
            mType = FOR_CURRENT_USER;
        }

        switch (mType) {
            case FOR_CURRENT_USER:
                break;
            case FOR_PROFILE_USER:
                mEditNamePhone.setVisibility(View.VISIBLE);
                mEditNameUser.setVisibility(View.VISIBLE);
                mEditNamePhone.setText("+" + mPhone);

                mEditNamePhone.setTextColor(Color.BLACK);
                mEditNamePhone.setTextSize(18);
                mEditNamePhone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                a_edit_name_ic_user.setVisibility(View.GONE);

                IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, mUserId, mInitials, mFile);
                mEditNameUser.setImageDrawable(dr);

                ((RelativeLayout.LayoutParams) mFirstName.getLayoutParams()).leftMargin = Constant.DP_18;
                break;
            case FOR_GROUP:
                a_edit_name_ic_user.setImageResource(R.mipmap.ic_groupusers);
                mSecondName.setVisibility(View.GONE);
                mFirstName.setHint(AndroidUtil.getResourceString(R.string.chat_name));
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
                if (!mIsBlockedButton) {
                    UserManager userManager = UserManager.getManager();
                    switch (mType) {
                        case FOR_CURRENT_USER:
                            if (TextUtil.isNotBlank(mFirstName.getText().toString())) {
                                mIsBlockedButton = true;
                                progressView.start();
                                userManager.changeCurrentUserName(mFirstName.getText().toString(), mSecondName.getText().toString(),
                                        new ResultController() {
                                            @Override
                                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                progressView.stop();
                                                mIsBlockedButton = false;
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
                            if (TextUtil.isNotBlank(mFirstName.getText().toString())) {
                                try {
                                    mIsBlockedButton = true;
                                    progressView.start();
                                    TdApi.InputContact[] inputContacts = new TdApi.InputContact[1];
                                    inputContacts[0] = new TdApi.InputContact();
                                    inputContacts[0].firstName = mFirstName.getText().toString();
                                    inputContacts[0].lastName = mSecondName.getText().toString();
                                    inputContacts[0].phoneNumber = mPhone;
                                    userManager.importContacts(inputContacts, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            progressView.stop();
                                            mIsBlockedButton = false;
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
                                    mIsBlockedButton = false;
                                    Log.e(LOG, "click", e);
                                }
                            }
                            break;
                        case FOR_GROUP:
                            ChatManager chatManager = ChatManager.getManager();
                            if (TextUtil.isNotBlank(mFirstName.getText().toString())) {
                                mIsBlockedButton = true;
                                progressView.start();
                                chatManager.changeChatTitle(mFirstName.getText().toString(),
                                        new ResultController() {
                                            @Override
                                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                                progressView.stop();
                                                mIsBlockedButton = false;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            TextView tToolbarTitle = (TextView) toolbar.findViewById(R.id.t_toolbar_title);
            tToolbarTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            tToolbarTitle.setTextColor(0xffffffff);
            tToolbarTitle.setTextSize(20);
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
