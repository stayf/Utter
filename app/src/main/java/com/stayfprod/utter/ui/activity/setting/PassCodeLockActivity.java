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

    private TextView title;
    private View popupLockView;
    private PopupWindow popupLock;
    private View a_change_pass_code_shadow_2, a_change_pass_code_shadow_3;
    private RelativeLayout a_change_pass_code_rl_auto_lock;
    private RelativeLayout a_change_pass_code_rl_change_pass;
    private TextView a_change_pass_code_auto_lock_status;
    private TextView a_change_pass_code_text3;
    private TextView a_change_pass_code;
    private Switch a_pass_code_switch;
    private boolean isPassCodeLockEnabled;

    private void dismissPopUpMuteWithAnimation(final Runnable after) {
        if (popupLock != null) {
            if (popupLock.isShowing()) {
                ObjectAnimator anim = ObjectAnimator.ofObject(popupLockView,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        0x33000000,
                        0x00000000);
                anim.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        popupLock.dismiss();
                        if (after != null)
                            after.run();
                    }
                });
                anim.setDuration(200).start();
            } else {
                popupLock.dismiss();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateAllComponents();
    }

    private void updateAllComponents() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (isPassCodeLockEnabled = passCodeManager.isEnabledPassCode(this)) {
            a_change_pass_code_shadow_2.setVisibility(View.VISIBLE);
            a_change_pass_code_rl_auto_lock.setVisibility(View.VISIBLE);
            a_change_pass_code_shadow_3.setVisibility(View.VISIBLE);
            a_change_pass_code_text3.setVisibility(View.VISIBLE);
            a_change_pass_code.setTextColor(0xFF222222);
            a_change_pass_code_rl_change_pass.setClickable(true);
            a_pass_code_switch.setCheckedImmediately(true);
            a_pass_code_switch.setEnabled(true);
            updateCurrentLockStatus();
        } else {
            a_change_pass_code_shadow_2.setVisibility(View.GONE);
            a_change_pass_code_rl_auto_lock.setVisibility(View.GONE);
            a_change_pass_code_shadow_3.setVisibility(View.GONE);
            a_change_pass_code_text3.setVisibility(View.GONE);
            a_change_pass_code.setTextColor(0xFFA6A6A6);
            a_change_pass_code_rl_change_pass.setClickable(false);
            a_pass_code_switch.setCheckedImmediately(false);
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
        a_change_pass_code_auto_lock_status.setText(text);
    }

    @Override
    public void onBackPressed() {
        if (popupLock != null && popupLock.isShowing()) {
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
        popupLockView = LayoutInflater.from(this).inflate(R.layout.popup_auto_lock, main, false);
        popupLock = new PopupWindow(popupLockView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupLock.setContentView(popupLockView);
        popupLock.setAnimationStyle(R.style.popup_anim_style);

        popupLockView.setOnClickListener(new View.OnClickListener() {
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

        a_change_pass_code_shadow_2 = findView(R.id.a_change_pass_code_shadow_2);
        a_change_pass_code_shadow_3 = findView(R.id.a_change_pass_code_shadow_3);

        View p_lock_hour_layout = popupLockView.findViewById(R.id.p_lock_hour_layout);
        View p_lock_30_min_layout = popupLockView.findViewById(R.id.p_lock_30_min_layout);
        View p_lock_15_min_layout = popupLockView.findViewById(R.id.p_lock_15_min_layout);
        View p_lock_5_min_layout = popupLockView.findViewById(R.id.p_lock_5_min_layout);
        View p_lock_disable_layout = popupLockView.findViewById(R.id.p_lock_disable_layout);

        p_lock_hour_layout.setOnClickListener(onClickListener);
        p_lock_30_min_layout.setOnClickListener(onClickListener);
        p_lock_15_min_layout.setOnClickListener(onClickListener);
        p_lock_5_min_layout.setOnClickListener(onClickListener);
        p_lock_disable_layout.setOnClickListener(onClickListener);

        p_lock_hour_layout.setTag(PassCodeManager.AUTO_LOCK_IN_60_MIN);
        p_lock_30_min_layout.setTag(PassCodeManager.AUTO_LOCK_IN_30_MIN);
        p_lock_15_min_layout.setTag(PassCodeManager.AUTO_LOCK_IN_15_MIN);
        p_lock_5_min_layout.setTag(PassCodeManager.AUTO_LOCK_IN_5_MIN);
        p_lock_disable_layout.setTag(PassCodeManager.AUTO_LOCK_DISABLE);

        initToolbar();

        a_pass_code_switch = findView(R.id.a_pass_code_switch);
        a_pass_code_switch.setEnabled(false);

        TextView a_pass_code_lock_text = (TextView) findViewById(R.id.a_pass_code_lock_text);
        a_change_pass_code = (TextView) findViewById(R.id.a_change_pass_code);

        a_pass_code_lock_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_pass_code_lock_text.setTextSize(17);
        a_pass_code_lock_text.setTextColor(0xFF222222);

        a_change_pass_code.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code.setTextSize(17);
        a_change_pass_code.setTextColor(0xFF222222);

        TextView a_change_pass_code_text1 = (TextView) findViewById(R.id.a_change_pass_code_text1);
        a_change_pass_code_text1.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code_text1.setTextSize(14);
        a_change_pass_code_text1.setTextColor(0xFF808080);

        TextView a_change_pass_code_text2 = (TextView) findViewById(R.id.a_change_pass_code_text2);
        a_change_pass_code_text2.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code_text2.setTextSize(14);
        a_change_pass_code_text2.setTextColor(0xFF808080);

        a_change_pass_code_text3 = (TextView) findViewById(R.id.a_change_pass_code_text3);
        a_change_pass_code_text3.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code_text3.setTextSize(14);
        a_change_pass_code_text3.setTextColor(0xFF808080);

        TextView a_change_pass_code_auto_lock = (TextView) findViewById(R.id.a_change_pass_code_auto_lock);
        a_change_pass_code_auto_lock_status = (TextView) findViewById(R.id.a_change_pass_code_auto_lock_status);

        a_change_pass_code_auto_lock.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code_auto_lock.setTextSize(17);
        a_change_pass_code_auto_lock.setTextColor(0xFF222222);

        a_change_pass_code_auto_lock_status.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_change_pass_code_auto_lock_status.setTextSize(16);
        a_change_pass_code_auto_lock_status.setTextColor(0xFF8a8a8a);

        a_change_pass_code_rl_auto_lock = (RelativeLayout) findViewById(R.id.a_change_pass_code_rl_auto_lock);
        a_change_pass_code_rl_auto_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!popupLock.isShowing()) {
                    popupLock.showAtLocation(popupLockView, Gravity.CENTER, 0, 0);
                    ObjectAnimator.ofObject(popupLockView,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            0x00000000,
                            0x33000000).setDuration(1100).start();
                }
            }
        });
        a_change_pass_code_rl_change_pass = (RelativeLayout) findViewById(R.id.a_change_pass_code_rl_change_pass);

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

        a_change_pass_code_rl_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetPassCodeActivity();
            }
        });
        a_change_pass_code_rl_lock.setOnClickListener(onClickListenerPassCode);

        a_pass_code_switch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
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

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            title = (TextView) toolbar.findViewById(R.id.t_pass_code_title);
            title.setText(AndroidUtil.getResourceString(R.string.passcode_lock));
            title.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            title.setTextColor(0xffffffff);
            title.setTextSize(20);

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
