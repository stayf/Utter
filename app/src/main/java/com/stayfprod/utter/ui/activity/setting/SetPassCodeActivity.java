package com.stayfprod.utter.ui.activity.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.ui.view.LockPatternView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.LockUtil;
import com.stayfprod.utter.util.TextUtil;

import java.util.List;

public class SetPassCodeActivity extends AbstractActivity {

    private static final int NUM_PAGES = 4;

    private static final int PAGE_PIN = 0;
    private static final int PAGE_PASSWORD = 1;
    private static final int PAGE_PATTERN = 2;
    private static final int PAGE_GESTURE = 3;

    private LayoutInflater mLayoutInflater;
    private ViewPager mPager;
    private Spinner mSpinner;
    private TextView mPinText;
    private EditText mPasInput;
    private TextView mPasText;
    private TextView mPatternText;
    private TextView mGestureText;

    private boolean mIsRepeatPin;
    private boolean mIsRepeatPassword;
    private boolean mIsRepeatPattern;
    private boolean mIsRepeatGesture;
    private StringBuilder mTmpPin = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_set_pass_code);

        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPager = (ViewPager) findViewById(R.id.a_set_pass_code_pager);
        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return NUM_PAGES;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @SuppressLint("NewApi")
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = null;

                switch (position) {
                    case PAGE_PIN:
                    default:
                        itemView = mLayoutInflater.inflate(R.layout.pager_setting_pin, container, false);

                        RelativeLayout p_pin_keyboard_container = (RelativeLayout) itemView.findViewById(R.id.p_pin_keyboard_container);
                        LinearLayout.LayoutParams containerKeyBoardLP = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 5);

                        p_pin_keyboard_container.setLayoutParams(containerKeyBoardLP);

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
                        mPinText = (TextView) itemView.findViewById(R.id.pin_text);

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
                                    if (!mIsRepeatPin) {
                                        passCodeManager.addPassCode(mTmpPin.toString(), LockUtil.Type.PIN, true);
                                        mTmpPin.setLength(0);
                                        mIsRepeatPin = true;
                                        mPinText.setText(AndroidUtil.getResourceString(R.string.repeat_your_pin));
                                        pinInput.setText("");
                                        mSpinner.setEnabled(false);
                                    } else {
                                        boolean isSame = passCodeManager.comparePassCode(mTmpPin.toString(), LockUtil.Type.PIN, true);
                                        if (!isSame) {
                                            TextUtil.shakeText(mPinText);
                                            pinInput.setText("");
                                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                        } else {
                                            pinInput.setText("");
                                            passCodeManager.addPassCode(mTmpPin.toString(), LockUtil.Type.PIN, false);
                                            passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtil.Type.PIN);
                                            onBackPressed();
                                        }
                                        mTmpPin.setLength(0);
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

                        mPinText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        mPinText.setTextColor(0xffD2EAFC);
                        mPinText.setTextSize(14);

                        pinInput.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                        pinInput.setTextColor(Color.WHITE);
                        pinInput.setTextSize(22);
                        break;
                    case PAGE_PASSWORD:
                        itemView = mLayoutInflater.inflate(R.layout.pager_setting_password, container, false);

                        mPasInput = (EditText) itemView.findViewById(R.id.pas_input);
                        mPasText = (TextView) itemView.findViewById(R.id.pas_text);

                        mPasInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    checkPassword();
                                    return true;
                                }
                                return false;
                            }
                        });

                        mPasText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        mPasText.setTextColor(0xffD2EAFC);
                        mPasText.setTextSize(14);

                        mPasInput.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                        mPasInput.setTextColor(Color.WHITE);
                        mPasInput.setTextSize(16);

                        if (App.CURRENT_VERSION_SDK >= 21) {
                            mPasInput.setLetterSpacing(0.7f);
                        }
                        break;
                    case PAGE_PATTERN:
                        itemView = mLayoutInflater.inflate(R.layout.pager_setting_pattern, container, false);
                        final LockPatternView lockPattern = (LockPatternView) itemView.findViewById(R.id.pattern);
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
                                    PassCodeManager passCodeManager = PassCodeManager.getManager();
                                    if (!mIsRepeatPattern) {
                                        passCodeManager.addPassCode(pattern, LockUtil.Type.PATTERN, true);
                                        mIsRepeatPattern = true;
                                        mPatternText.setText(AndroidUtil.getResourceString(R.string.repeat_your_pattern));
                                        mSpinner.setEnabled(false);
                                        lockPattern.clearPattern();
                                    } else {
                                        boolean isSame = passCodeManager.comparePassCode(pattern, LockUtil.Type.PATTERN, true);
                                        if (!isSame) {
                                            TextUtil.shakeText(mPatternText);
                                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                            lockPattern.clearPattern();
                                        } else {
                                            passCodeManager.addPassCode(pattern, LockUtil.Type.PATTERN, false);
                                            passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtil.Type.PATTERN);
                                            lockPattern.clearPattern();
                                            onBackPressed();
                                        }
                                    }
                                }
                            }
                        });
                        mPatternText = (TextView) itemView.findViewById(R.id.pattern_text);
                        mPatternText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        mPatternText.setTextColor(0xffD2EAFC);
                        mPatternText.setTextSize(14);

                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            FrameLayout zeroChild = (FrameLayout) ((ViewGroup) ((RelativeLayout) itemView).getChildAt(0)).getChildAt(0);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 0);
                            lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
                            zeroChild.setLayoutParams(lp);
                        }

                        break;
                    case PAGE_GESTURE:
                        itemView = mLayoutInflater.inflate(R.layout.pager_setting_gesture, container, false);

                        mGestureText = (TextView) itemView.findViewById(R.id.gesture_text);
                        mGestureText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        mGestureText.setTextColor(0xffD2EAFC);
                        mGestureText.setTextSize(14);

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
                                if (!mIsRepeatGesture) {
                                    passCodeManager.addPassCode(gesture, LockUtil.Type.GESTURE, true);
                                    mIsRepeatGesture = true;
                                    mGestureText.setText(AndroidUtil.getResourceString(R.string.repeat_your_gesture));
                                    mSpinner.setEnabled(false);
                                } else {
                                    boolean isSame = passCodeManager.comparePassCode(gesture, LockUtil.Type.GESTURE, true);
                                    if (!isSame) {
                                        TextUtil.shakeText(mGestureText);
                                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                    } else {
                                        passCodeManager.addPassCode(gesture, LockUtil.Type.GESTURE, false);
                                        passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtil.Type.GESTURE);
                                        onBackPressed();
                                    }
                                }
                            }
                        };
                        gOverlay.addOnGesturePerformedListener(gesturePerformedListener);

                        break;
                }
                container.addView(itemView);
                return itemView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        };
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSpinner.setSelection(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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

                switch (mPager.getCurrentItem()) {
                    case PAGE_PIN:
                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.please_)
                                + mPinText.getText() + ". \r\n " + AndroidUtil.getResourceString(R.string.must_be_4_numbers));
                        TextUtil.shakeText(mPinText);
                        break;
                    case PAGE_PASSWORD:
                        checkPassword();
                        break;
                    case PAGE_PATTERN:
                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.please_) + mPatternText.getText() + ".");
                        TextUtil.shakeText(mPatternText);
                        break;
                    case PAGE_GESTURE:
                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.please_) + mGestureText.getText() + ".");
                        TextUtil.shakeText(mGestureText);
                        break;
                }
            }
        });
        return true;
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

    private void checkPassword() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (mPasInput.getText().length() < 4) {
            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.minimum_length_for_password_is_4));
            TextUtil.shakeText(mPasText);
            return;
        }
        if (!mIsRepeatPassword) {
            passCodeManager.addPassCode(mPasInput.getText().toString(), LockUtil.Type.PASSWORD, true);
            mIsRepeatPassword = true;
            mPasText.setText(AndroidUtil.getResourceString(R.string.repeat_your_password));
            mPasInput.setText("");
            mSpinner.setEnabled(false);
        } else {
            boolean isSame = passCodeManager.comparePassCode(mPasInput.getText().toString(), LockUtil.Type.PASSWORD, true);
            if (!isSame) {
                TextUtil.shakeText(mPasText);
                mPasInput.setText("");
                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
            } else {
                passCodeManager.addPassCode(mPasInput.getText().toString(), LockUtil.Type.PASSWORD, false);
                passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtil.Type.PASSWORD);
                mPasInput.setText("");
                onBackPressed();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            mSpinner = (Spinner) findViewById(R.id.t_set_pass_code_spinner);
            mSpinner.setBackgroundColor(0xFF5B95C2);
            CharSequence[] strings = getResources().getTextArray(R.array.pass_code_list);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, strings) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                    view.setTextColor(0xffffffff);
                    view.setTextSize(20);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                    view.setTextColor(0xFF212121);
                    view.setTextSize(20);
                    view.setBackgroundResource(R.drawable.item_click_white);
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mPasInput != null) {
                        try {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(mPasInput.getWindowToken(), 0);
                        } catch (Exception e) {
                            //
                        }
                    }
                    mPager.setCurrentItem(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

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
