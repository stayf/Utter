package com.stayfprod.utter.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.ui.activity.setting.PassCodeLockActivity;
import com.stayfprod.utter.ui.view.LockPatternView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.LockUtil;
import com.stayfprod.utter.util.TextUtil;

import java.util.List;

public class EnterPassCodeActivity extends AbstractActivity {

    public static volatile boolean sIsOpenedActivity;

    private Boolean mIsInSettings;
    private StringBuilder mTmpPin = new StringBuilder();

    @Override
    protected void onStart() {
        sIsOpenedActivity = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        sIsOpenedActivity = false;
        super.onStop();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_enter_pass_code);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mIsInSettings = bundle.getBoolean("isInSettings", false);
        }

        RelativeLayout relativeMainLayout = findView(R.id.a_enter_pass_main_layout);

        final PassCodeManager passCodeManager = PassCodeManager.getManager();
        LockUtil.Type type = passCodeManager.getPassCodeType(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = null;
        if (type == null) {
            return;
        }
        switch (type) {
            case PASSWORD:
                itemView = layoutInflater.inflate(R.layout.pager_setting_password, relativeMainLayout, true);

                final EditText pasInput = (EditText) itemView.findViewById(R.id.pas_input);
                final TextView pasText = (TextView) itemView.findViewById(R.id.pas_text);
                ImageView pasLogo = (ImageView) itemView.findViewById(R.id.pas_logo);
                pasLogo.setVisibility(View.VISIBLE);

                pasText.setText(AndroidUtil.getResourceString(R.string.enter_your_password_to_unlock));

                pasText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                pasText.setTextColor(0xffD2EAFC);
                pasText.setTextSize(14);

                pasInput.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                pasInput.setTextColor(Color.WHITE);
                pasInput.setTextSize(16);

                if (App.CURRENT_VERSION_SDK >= 21) {
                    pasInput.setLetterSpacing(0.7f);
                }

                pasInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            boolean isSame = passCodeManager.comparePassCode(pasInput.getText().toString(), LockUtil.Type.PASSWORD, false);
                            if (!isSame) {
                                TextUtil.shakeText(pasText);
                                pasInput.setText("");
                                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                            } else {
                                pasInput.setText("");
                                passCodeManager.setLockByUser(EnterPassCodeActivity.this, false);
                                passCodeManager.setCurrentAutoLockStartTimer(EnterPassCodeActivity.this, System.currentTimeMillis());
                                openNextActivity();
                            }
                            return true;
                        }
                        return false;
                    }
                });
                break;
            case PIN:
                itemView = layoutInflater.inflate(R.layout.pager_setting_pin, relativeMainLayout, true);
                LinearLayout pinButton1 = (LinearLayout) itemView.findViewById(R.id.pin_button_1);
                LinearLayout pinButton2 = (LinearLayout) itemView.findViewById(R.id.pin_button_2);
                LinearLayout pinButton3 = (LinearLayout) itemView.findViewById(R.id.pin_button_3);
                LinearLayout pinButton4 = (LinearLayout) itemView.findViewById(R.id.pin_button_4);
                LinearLayout pinButton5 = (LinearLayout) itemView.findViewById(R.id.pin_button_5);
                LinearLayout pinButton6 = (LinearLayout) itemView.findViewById(R.id.pin_button_6);
                LinearLayout pinButton7 = (LinearLayout) itemView.findViewById(R.id.pin_button_7);
                LinearLayout pinButton8 = (LinearLayout) itemView.findViewById(R.id.pin_button_8);
                LinearLayout pinButton9 = (LinearLayout) itemView.findViewById(R.id.pin_button_9);
                LinearLayout pinButton0 = (LinearLayout) itemView.findViewById(R.id.pin_button_0);
                RelativeLayout pinButtonDelete = (RelativeLayout) itemView.findViewById(R.id.pin_button_delete);

                setTextMainStyle(pinButton1.getChildAt(0));
                setTextSubStyle(pinButton1.getChildAt(1));
                setTextMainStyle(pinButton2.getChildAt(0));
                setTextSubStyle(pinButton2.getChildAt(1));
                setTextMainStyle(pinButton3.getChildAt(0));
                setTextSubStyle(pinButton3.getChildAt(1));
                setTextMainStyle(pinButton4.getChildAt(0));
                setTextSubStyle(pinButton4.getChildAt(1));
                setTextMainStyle(pinButton5.getChildAt(0));
                setTextSubStyle(pinButton5.getChildAt(1));
                setTextMainStyle(pinButton6.getChildAt(0));
                setTextSubStyle(pinButton6.getChildAt(1));
                setTextMainStyle(pinButton7.getChildAt(0));
                setTextSubStyle(pinButton7.getChildAt(1));
                setTextMainStyle(pinButton8.getChildAt(0));
                setTextSubStyle(pinButton8.getChildAt(1));
                setTextMainStyle(pinButton9.getChildAt(0));
                setTextSubStyle(pinButton9.getChildAt(1));
                setTextMainStyle(pinButton0.getChildAt(0));
                setTextSubStyle(pinButton0.getChildAt(1));

                final TextView pinInput = (TextView) itemView.findViewById(R.id.pin_input);
                final TextView textView = (TextView) itemView.findViewById(R.id.pin_text);

                textView.setText(AndroidUtil.getResourceString(R.string.enter_your_pin_to_unlock));

                ImageView pinLogo = (ImageView) itemView.findViewById(R.id.pin_logo);
                pinLogo.setVisibility(View.VISIBLE);

                View.OnClickListener onClickListenerPin = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTmpPin.append(v.getTag().toString());
                        int length = pinInput.getText().length();
                        if (length < 12) {
                            pinInput.append("\u25CF" + "  ");
                        }

                        if (length >= 9) {
                            PassCodeManager passCodeManager = PassCodeManager.getManager();
                            boolean isSame = passCodeManager.comparePassCode(mTmpPin.toString(), LockUtil.Type.PIN, false);
                            mTmpPin.setLength(0);
                            if (!isSame) {
                                TextUtil.shakeText(textView);
                                pinInput.setText("");
                                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                            } else {
                                pinInput.setText("");
                                passCodeManager.setLockByUser(EnterPassCodeActivity.this, false);
                                passCodeManager.setCurrentAutoLockStartTimer(EnterPassCodeActivity.this, System.currentTimeMillis());
                                openNextActivity();
                            }
                        }
                    }
                };

                pinButton1.setOnClickListener(onClickListenerPin);
                pinButton2.setOnClickListener(onClickListenerPin);
                pinButton3.setOnClickListener(onClickListenerPin);
                pinButton4.setOnClickListener(onClickListenerPin);
                pinButton5.setOnClickListener(onClickListenerPin);
                pinButton6.setOnClickListener(onClickListenerPin);
                pinButton7.setOnClickListener(onClickListenerPin);
                pinButton8.setOnClickListener(onClickListenerPin);
                pinButton9.setOnClickListener(onClickListenerPin);
                pinButton0.setOnClickListener(onClickListenerPin);

                pinButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = pinInput.getText().toString();
                        String tmp = mTmpPin.toString();
                        mTmpPin.setLength(0);
                        if (tmp.length() > 0)
                            mTmpPin.append(tmp.substring(0, tmp.length() - 1));
                        if (TextUtil.isNotBlank(text)) {
                            StringBuilder stringBuilder = new StringBuilder(text).reverse().replace(0, 3, "").reverse();
                            pinInput.setText(stringBuilder.toString());
                        }
                    }
                });

                LinearLayout pinMain = (LinearLayout) itemView.findViewById(R.id.pin_main);
                pinMain.setOrientation(getResources().getConfiguration().orientation);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ((LinearLayout.LayoutParams) pinMain.getChildAt(0).getLayoutParams()).weight = 1;
                    ((RelativeLayout) pinInput.getParent()).setGravity(Gravity.NO_GRAVITY);
                }

                textView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                textView.setTextColor(0xffD2EAFC);
                textView.setTextSize(14);

                pinInput.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                pinInput.setTextColor(Color.WHITE);
                pinInput.setTextSize(22);
                break;
            case PATTERN:
                itemView = layoutInflater.inflate(R.layout.pager_setting_pattern, relativeMainLayout, true);
                final LockPatternView lockPattern = (LockPatternView) itemView.findViewById(R.id.pattern);
                final TextView patternText = (TextView) itemView.findViewById(R.id.pattern_text);
                patternText.setText(AndroidUtil.getResourceString(R.string.enter_your_pattern_to_unlock));
                ImageView patternLogo = (ImageView) itemView.findViewById(R.id.pattern_logo);
                patternLogo.setVisibility(View.VISIBLE);
                patternText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                patternText.setTextColor(0xffD2EAFC);
                patternText.setTextSize(14);
                lockPattern.setOnPatternListener(new LockPatternView.OnPatternListener() {
                    @Override
                    public void onPatternStart() {

                    }

                    @Override
                    public void onPatternCleared() {

                    }

                    @Override
                    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

                    }

                    @Override
                    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                        if (pattern.size() < 2) {
                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.minimum_count_of_points_is_2));
                        } else {
                            boolean isSame = passCodeManager.comparePassCode(pattern, LockUtil.Type.PATTERN, false);
                            if (!isSame) {
                                TextUtil.shakeText(patternText);
                                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                lockPattern.clearPattern();
                            } else {
                                lockPattern.clearPattern();
                                passCodeManager.setLockByUser(EnterPassCodeActivity.this, false);
                                passCodeManager.setCurrentAutoLockStartTimer(EnterPassCodeActivity.this, System.currentTimeMillis());
                                openNextActivity();
                            }
                        }
                    }
                });


                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    FrameLayout zeroChild = (FrameLayout) ((ViewGroup) ((RelativeLayout) itemView).getChildAt(0)).getChildAt(0);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 0);
                    lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
                    zeroChild.setLayoutParams(lp);
                }

                break;
            case GESTURE:
                itemView = layoutInflater.inflate(R.layout.pager_setting_gesture, relativeMainLayout, true);

                final TextView gestureText = (TextView) itemView.findViewById(R.id.gesture_text);

                gestureText.setText(AndroidUtil.getResourceString(R.string.enter_your_gesture_to_unlock));
                gestureText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                gestureText.setTextColor(0xffD2EAFC);
                gestureText.setTextSize(14);

                ImageView gestureLogo = (ImageView) itemView.findViewById(R.id.gesture_logo);
                gestureLogo.setVisibility(View.GONE);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ((LinearLayout) itemView).getChildAt(0).getLayoutParams();
                    layoutParams.weight = 0;
                    layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    layoutParams.weight = LinearLayout.LayoutParams.WRAP_CONTENT;
                }

                GestureOverlayView gOverlay = (GestureOverlayView) itemView.findViewById(R.id.gesture);

                GestureOverlayView.OnGesturePerformedListener gesturePerformedListener = new GestureOverlayView.OnGesturePerformedListener() {
                    @Override
                    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

                        overlay.cancelClearAnimation();
                        overlay.clear(true);

                        PassCodeManager passCodeManager = PassCodeManager.getManager();
                        boolean isSame = passCodeManager.comparePassCode(gesture, LockUtil.Type.GESTURE, false);
                        if (!isSame) {
                            TextUtil.shakeText(gestureText);
                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                        } else {
                            passCodeManager.setLockByUser(EnterPassCodeActivity.this, false);
                            passCodeManager.setCurrentAutoLockStartTimer(EnterPassCodeActivity.this, System.currentTimeMillis());
                            openNextActivity();
                        }
                    }
                };
                gOverlay.addOnGesturePerformedListener(gesturePerformedListener);

                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (mIsInSettings != null && mIsInSettings) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    private void openNextActivity() {
        if (mIsInSettings != null && mIsInSettings) {
            Intent intent = new Intent(this, PassCodeLockActivity.class);
            this.startActivity(intent);
            this.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            supportFinishAfterTransition();
        } else {
            finish();
        }
    }

    private void setTextMainStyle(View view) {
        if (view != null) {
            TextView textView = (TextView) view;
            textView.setTypeface(AndroidUtil.TF_ROBOTO_LIGHT);
            textView.setTextColor(0xffffffff);
            textView.setTextSize(38);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((LinearLayout.LayoutParams) textView.getLayoutParams()).topMargin = 0;
                ((LinearLayout.LayoutParams) ((LinearLayout) textView.getParent()).getLayoutParams()).width = Constant.DP_76;
            }
        }
    }

    private void setTextSubStyle(View view) {
        if (view != null) {
            TextView textView = (TextView) view;
            textView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textView.setTextColor(0xffD2EAFC);
            textView.setTextSize(13);
        }
    }
}
