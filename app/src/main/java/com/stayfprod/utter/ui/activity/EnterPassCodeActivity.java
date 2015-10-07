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
import com.stayfprod.utter.util.LockUtils;
import com.stayfprod.utter.util.TextUtil;

import java.util.List;

public class EnterPassCodeActivity extends AbstractActivity {

    public static volatile boolean isOpenedActivity;
    private Boolean isInSettings;
    private StringBuilder tmpPin = new StringBuilder();


    @Override
    protected void onStart() {
        isOpenedActivity = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        isOpenedActivity = false;
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
            isInSettings = bundle.getBoolean("isInSettings", false);
        }

        RelativeLayout relativeMainLayout = findView(R.id.a_enter_pass_main_layout);

        final PassCodeManager passCodeManager = PassCodeManager.getManager();
        LockUtils.Type type = passCodeManager.getPassCodeType(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = null;
        if (type == null) {
            return;
        }
        switch (type) {
            case PASSWORD:
                itemView = layoutInflater.inflate(R.layout.pager_setting_password, relativeMainLayout, true);

                final EditText pas_input = (EditText) itemView.findViewById(R.id.pas_input);
                final TextView pas_text = (TextView) itemView.findViewById(R.id.pas_text);
                ImageView pas_logo = (ImageView) itemView.findViewById(R.id.pas_logo);
                pas_logo.setVisibility(View.VISIBLE);

                pas_text.setText(AndroidUtil.getResourceString(R.string.enter_your_password_to_unlock));

                pas_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                pas_text.setTextColor(0xffD2EAFC);
                pas_text.setTextSize(14);

                pas_input.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                pas_input.setTextColor(Color.WHITE);
                pas_input.setTextSize(16);

                if (App.CURRENT_VERSION_SDK >= 21) {
                    pas_input.setLetterSpacing(0.7f);
                }

                pas_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            boolean isSame = passCodeManager.comparePassCode(pas_input.getText().toString(), LockUtils.Type.PASSWORD, false);
                            if (!isSame) {
                                TextUtil.shakeText(pas_text);
                                pas_input.setText("");
                                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                            } else {
                                pas_input.setText("");
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
                LinearLayout pin_button_1 = (LinearLayout) itemView.findViewById(R.id.pin_button_1);
                LinearLayout pin_button_2 = (LinearLayout) itemView.findViewById(R.id.pin_button_2);
                LinearLayout pin_button_3 = (LinearLayout) itemView.findViewById(R.id.pin_button_3);
                LinearLayout pin_button_4 = (LinearLayout) itemView.findViewById(R.id.pin_button_4);
                LinearLayout pin_button_5 = (LinearLayout) itemView.findViewById(R.id.pin_button_5);
                LinearLayout pin_button_6 = (LinearLayout) itemView.findViewById(R.id.pin_button_6);
                LinearLayout pin_button_7 = (LinearLayout) itemView.findViewById(R.id.pin_button_7);
                LinearLayout pin_button_8 = (LinearLayout) itemView.findViewById(R.id.pin_button_8);
                LinearLayout pin_button_9 = (LinearLayout) itemView.findViewById(R.id.pin_button_9);
                LinearLayout pin_button_0 = (LinearLayout) itemView.findViewById(R.id.pin_button_0);
                RelativeLayout pin_button_delete = (RelativeLayout) itemView.findViewById(R.id.pin_button_delete);

                setTextMainStyle(pin_button_1.getChildAt(0));
                setTextSubStyle(pin_button_1.getChildAt(1));
                setTextMainStyle(pin_button_2.getChildAt(0));
                setTextSubStyle(pin_button_2.getChildAt(1));
                setTextMainStyle(pin_button_3.getChildAt(0));
                setTextSubStyle(pin_button_3.getChildAt(1));
                setTextMainStyle(pin_button_4.getChildAt(0));
                setTextSubStyle(pin_button_4.getChildAt(1));
                setTextMainStyle(pin_button_5.getChildAt(0));
                setTextSubStyle(pin_button_5.getChildAt(1));
                setTextMainStyle(pin_button_6.getChildAt(0));
                setTextSubStyle(pin_button_6.getChildAt(1));
                setTextMainStyle(pin_button_7.getChildAt(0));
                setTextSubStyle(pin_button_7.getChildAt(1));
                setTextMainStyle(pin_button_8.getChildAt(0));
                setTextSubStyle(pin_button_8.getChildAt(1));
                setTextMainStyle(pin_button_9.getChildAt(0));
                setTextSubStyle(pin_button_9.getChildAt(1));
                setTextMainStyle(pin_button_0.getChildAt(0));
                setTextSubStyle(pin_button_0.getChildAt(1));

                final TextView pin_input = (TextView) itemView.findViewById(R.id.pin_input);
                final TextView pin_text = (TextView) itemView.findViewById(R.id.pin_text);

                pin_text.setText(AndroidUtil.getResourceString(R.string.enter_your_pin_to_unlock));

                ImageView pin_logo = (ImageView) itemView.findViewById(R.id.pin_logo);
                pin_logo.setVisibility(View.VISIBLE);

                View.OnClickListener onClickListenerPin = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tmpPin.append(v.getTag().toString());
                        int length = pin_input.getText().length();
                        if (length < 12) {
                            pin_input.append("\u25CF" + "  ");
                        }

                        if (length >= 9) {
                            PassCodeManager passCodeManager = PassCodeManager.getManager();
                            boolean isSame = passCodeManager.comparePassCode(tmpPin.toString(), LockUtils.Type.PIN, false);
                            tmpPin.setLength(0);
                            if (!isSame) {
                                TextUtil.shakeText(pin_text);
                                pin_input.setText("");
                                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                            } else {
                                pin_input.setText("");
                                passCodeManager.setLockByUser(EnterPassCodeActivity.this, false);
                                passCodeManager.setCurrentAutoLockStartTimer(EnterPassCodeActivity.this, System.currentTimeMillis());
                                openNextActivity();
                            }
                        }
                    }
                };

                pin_button_1.setOnClickListener(onClickListenerPin);
                pin_button_2.setOnClickListener(onClickListenerPin);
                pin_button_3.setOnClickListener(onClickListenerPin);
                pin_button_4.setOnClickListener(onClickListenerPin);
                pin_button_5.setOnClickListener(onClickListenerPin);
                pin_button_6.setOnClickListener(onClickListenerPin);
                pin_button_7.setOnClickListener(onClickListenerPin);
                pin_button_8.setOnClickListener(onClickListenerPin);
                pin_button_9.setOnClickListener(onClickListenerPin);
                pin_button_0.setOnClickListener(onClickListenerPin);

                pin_button_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = pin_input.getText().toString();
                        String tmp = tmpPin.toString();
                        tmpPin.setLength(0);
                        if (tmp.length() > 0)
                            tmpPin.append(tmp.substring(0, tmp.length() - 1));
                        if (TextUtil.isNotBlank(text)) {
                            StringBuilder stringBuilder = new StringBuilder(text).reverse().replace(0, 3, "").reverse();
                            pin_input.setText(stringBuilder.toString());
                        }
                    }
                });

                LinearLayout pin_main = (LinearLayout) itemView.findViewById(R.id.pin_main);
                pin_main.setOrientation(getResources().getConfiguration().orientation);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ((LinearLayout.LayoutParams) pin_main.getChildAt(0).getLayoutParams()).weight = 1;
                    ((RelativeLayout) pin_input.getParent()).setGravity(Gravity.NO_GRAVITY);
                }

                pin_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                pin_text.setTextColor(0xffD2EAFC);
                pin_text.setTextSize(14);

                pin_input.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                pin_input.setTextColor(Color.WHITE);
                pin_input.setTextSize(22);
                break;
            case PATTERN:
                itemView = layoutInflater.inflate(R.layout.pager_setting_pattern, relativeMainLayout, true);
                final LockPatternView lockPattern = (LockPatternView) itemView.findViewById(R.id.pattern);
                final TextView pattern_text = (TextView) itemView.findViewById(R.id.pattern_text);
                pattern_text.setText(AndroidUtil.getResourceString(R.string.enter_your_pattern_to_unlock));
                ImageView pattern_logo = (ImageView) itemView.findViewById(R.id.pattern_logo);
                pattern_logo.setVisibility(View.VISIBLE);
                pattern_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                pattern_text.setTextColor(0xffD2EAFC);
                pattern_text.setTextSize(14);
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
                            boolean isSame = passCodeManager.comparePassCode(pattern, LockUtils.Type.PATTERN, false);
                            if (!isSame) {
                                TextUtil.shakeText(pattern_text);
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

                final TextView gesture_text = (TextView) itemView.findViewById(R.id.gesture_text);

                gesture_text.setText(AndroidUtil.getResourceString(R.string.enter_your_gesture_to_unlock));
                gesture_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                gesture_text.setTextColor(0xffD2EAFC);
                gesture_text.setTextSize(14);

                ImageView gesture_logo = (ImageView) itemView.findViewById(R.id.gesture_logo);
                gesture_logo.setVisibility(View.GONE);

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
                        boolean isSame = passCodeManager.comparePassCode(gesture, LockUtils.Type.GESTURE, false);
                        if (!isSame) {
                            TextUtil.shakeText(gesture_text);
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
        if (isInSettings != null && isInSettings) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    private void openNextActivity() {
        if (isInSettings != null && isInSettings) {
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
