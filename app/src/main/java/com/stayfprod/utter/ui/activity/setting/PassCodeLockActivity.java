package com.stayfprod.utter.ui.activity.setting;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.ui.view.Switch;
import com.stayfprod.utter.util.AndroidUtil;

public class PassCodeLockActivity extends AbstractActivity {

    private TextView mTitle;
    private View mPopupLockView;
    private PopupWindow mPopupLock;
    private View mChangePassCodeShadowTwo, mChangePassCodeShadowThree;
    private RelativeLayout mChangedPassCodeRlAutoLock;
    private RelativeLayout mChangePassCodeRlChangePass;
    private TextView mChangePassCodeAutoLockStatus;
    private TextView mChangePassCodeTextThree;
    private TextView mChangePassCode;
    private Switch mPassCodeSwitch;
    private boolean mIsPassCodeLockEnabled;

    @Override
    protected void onStart() {
        super.onStart();
        updateAllComponents();
    }

    @Override
    public void onBackPressed() {
        if (mPopupLock != null && mPopupLock.isShowing()) {
            dismissPopUpMuteWithAnimation(null);
            return;
        }

        dismissPopUpMuteWithAnimation(null);
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_pass_code_lock);
        ViewGroup main = (ViewGroup) findViewById(R.id.a_pass_code_main);
        mPopupLockView = LayoutInflater.from(this).inflate(R.layout.popup_auto_lock, main, false);
        mPopupLock = new PopupWindow(mPopupLockView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupLock.setContentView(mPopupLockView);
        mPopupLock.setAnimationStyle(R.style.popup_anim_style);

        mPopupLockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopUpMuteWithAnimation(null);
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismissPopUpMuteWithAnimation(new Runnable() {
                    @Override
                    public void run() {
                        PassCodeManager.getManager().setCurrentAutoLockType(PassCodeLockActivity.this, (int) v.getTag());
                        updateCurrentLockStatus();
                    }
                });
            }
        };

        mChangePassCodeShadowTwo = findView(R.id.a_change_pass_code_shadow_2);
        mChangePassCodeShadowThree = findView(R.id.a_change_pass_code_shadow_3);

        View pLockHourLayout = mPopupLockView.findViewById(R.id.p_lock_hour_layout);
        View pLock30MinLayout = mPopupLockView.findViewById(R.id.p_lock_30_min_layout);
        View pLock15MinLayout = mPopupLockView.findViewById(R.id.p_lock_15_min_layout);
        View pLock5MinLayout = mPopupLockView.findViewById(R.id.p_lock_5_min_layout);
        View pLockDisableLayout = mPopupLockView.findViewById(R.id.p_lock_disable_layout);

        pLockHourLayout.setOnClickListener(onClickListener);
        pLock30MinLayout.setOnClickListener(onClickListener);
        pLock15MinLayout.setOnClickListener(onClickListener);
        pLock5MinLayout.setOnClickListener(onClickListener);
        pLockDisableLayout.setOnClickListener(onClickListener);

        pLockHourLayout.setTag(PassCodeManager.AUTO_LOCK_IN_60_MIN);
        pLock30MinLayout.setTag(PassCodeManager.AUTO_LOCK_IN_30_MIN);
        pLock15MinLayout.setTag(PassCodeManager.AUTO_LOCK_IN_15_MIN);
        pLock5MinLayout.setTag(PassCodeManager.AUTO_LOCK_IN_5_MIN);
        pLockDisableLayout.setTag(PassCodeManager.AUTO_LOCK_DISABLE);

        initToolbar();

        mPassCodeSwitch = findView(R.id.a_pass_code_switch);
        mPassCodeSwitch.setEnabled(false);

        TextView passCodeLockText = (TextView) findViewById(R.id.a_pass_code_lock_text);
        mChangePassCode = (TextView) findViewById(R.id.a_change_pass_code);

        passCodeLockText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        passCodeLockText.setTextSize(17);
        passCodeLockText.setTextColor(0xFF222222);

        mChangePassCode.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChangePassCode.setTextSize(17);
        mChangePassCode.setTextColor(0xFF222222);

        TextView changePassCodeText1 = (TextView) findViewById(R.id.a_change_pass_code_text1);
        changePassCodeText1.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        changePassCodeText1.setTextSize(14);
        changePassCodeText1.setTextColor(0xFF808080);

        TextView changePassCodeText2 = (TextView) findViewById(R.id.a_change_pass_code_text2);
        changePassCodeText2.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        changePassCodeText2.setTextSize(14);
        changePassCodeText2.setTextColor(0xFF808080);

        mChangePassCodeTextThree = (TextView) findViewById(R.id.a_change_pass_code_text3);
        mChangePassCodeTextThree.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChangePassCodeTextThree.setTextSize(14);
        mChangePassCodeTextThree.setTextColor(0xFF808080);

        TextView changePassCodeAutoLock = (TextView) findViewById(R.id.a_change_pass_code_auto_lock);
        mChangePassCodeAutoLockStatus = (TextView) findViewById(R.id.a_change_pass_code_auto_lock_status);

        changePassCodeAutoLock.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        changePassCodeAutoLock.setTextSize(17);
        changePassCodeAutoLock.setTextColor(0xFF222222);

        mChangePassCodeAutoLockStatus.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChangePassCodeAutoLockStatus.setTextSize(16);
        mChangePassCodeAutoLockStatus.setTextColor(0xFF8a8a8a);

        mChangedPassCodeRlAutoLock = (RelativeLayout) findViewById(R.id.a_change_pass_code_rl_auto_lock);
        mChangedPassCodeRlAutoLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPopupLock.isShowing()) {
                    mPopupLock.showAtLocation(mPopupLockView, Gravity.CENTER, 0, 0);
                    ObjectAnimator.ofObject(mPopupLockView,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            0x00000000,
                            0x33000000).setDuration(1100).start();
                }
            }
        });
        mChangePassCodeRlChangePass = (RelativeLayout) findViewById(R.id.a_change_pass_code_rl_change_pass);

        RelativeLayout a_change_pass_code_rl_lock = (RelativeLayout) findViewById(R.id.a_change_pass_code_rl_lock);

        final View.OnClickListener onClickListenerPassCode = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PassCodeManager passCodeManager = PassCodeManager.getManager();
                if (passCodeManager.isEnabledPassCode(PassCodeLockActivity.this)) {
                    passCodeManager.setEnablePassCode(PassCodeLockActivity.this, false, null);
                    updateAllComponents();
                } else {
                    openSetPassCodeActivity();
                }
            }
        };

        mChangePassCodeRlChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetPassCodeActivity();
            }
        });
        a_change_pass_code_rl_lock.setOnClickListener(onClickListenerPassCode);

        mPassCodeSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                onClickListenerPassCode.onClick(view);
            }
        });
    }

    private void openSetPassCodeActivity() {
        Intent intent = new Intent(this, SetPassCodeActivity.class);
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private void dismissPopUpMuteWithAnimation(final Runnable after) {
        if (mPopupLock != null) {
            if (mPopupLock.isShowing()) {
                ObjectAnimator anim = ObjectAnimator.ofObject(mPopupLockView,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        0x33000000,
                        0x00000000);
                anim.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mPopupLock.dismiss();
                        if (after != null)
                            after.run();
                    }
                });
                anim.setDuration(200).start();
            } else {
                mPopupLock.dismiss();
            }
        }
    }

    private void updateAllComponents() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (mIsPassCodeLockEnabled = passCodeManager.isEnabledPassCode(this)) {
            mChangePassCodeShadowTwo.setVisibility(View.VISIBLE);
            mChangedPassCodeRlAutoLock.setVisibility(View.VISIBLE);
            mChangePassCodeShadowThree.setVisibility(View.VISIBLE);
            mChangePassCodeTextThree.setVisibility(View.VISIBLE);
            mChangePassCode.setTextColor(0xFF222222);
            mChangePassCodeRlChangePass.setClickable(true);
            mPassCodeSwitch.setCheckedImmediately(true);
            mPassCodeSwitch.setEnabled(true);
            updateCurrentLockStatus();
        } else {
            mChangePassCodeShadowTwo.setVisibility(View.GONE);
            mChangedPassCodeRlAutoLock.setVisibility(View.GONE);
            mChangePassCodeShadowThree.setVisibility(View.GONE);
            mChangePassCodeTextThree.setVisibility(View.GONE);
            mChangePassCode.setTextColor(0xFFA6A6A6);
            mChangePassCodeRlChangePass.setClickable(false);
            mPassCodeSwitch.setCheckedImmediately(false);
        }
    }

    private void updateCurrentLockStatus() {
        String text = "";
        switch (PassCodeManager.getManager().getCurrentAutoLockStatus(this)) {
            case PassCodeManager.AUTO_LOCK_DISABLE:
                text = AndroidUtil.getResourceString(R.string.disabled);
                break;
            case PassCodeManager.AUTO_LOCK_IN_5_MIN:
                text = AndroidUtil.getResourceString(R.string.in_5_min);
                break;
            case PassCodeManager.AUTO_LOCK_IN_15_MIN:
                text = AndroidUtil.getResourceString(R.string.in_15_min);
                break;
            case PassCodeManager.AUTO_LOCK_IN_30_MIN:
                text = AndroidUtil.getResourceString(R.string.in_30_min);
                break;
            case PassCodeManager.AUTO_LOCK_IN_60_MIN:
                text = AndroidUtil.getResourceString(R.string.in_1_hour);
                break;
        }
        mChangePassCodeAutoLockStatus.setText(text);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mTitle = (TextView) toolbar.findViewById(R.id.t_pass_code_title);
            mTitle.setText(AndroidUtil.getResourceString(R.string.passcode_lock));
            mTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            mTitle.setTextColor(0xffffffff);
            mTitle.setTextSize(20);

            toolbar.setNavigationIcon(R.mipmap.ic_back);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}
