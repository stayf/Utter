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
import com.stayfprod.utter.util.LockUtils;
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
    private Spinner spinner;
    private TextView pin_text;
    private EditText pas_input;
    private TextView pas_text;
    private TextView pattern_text;
    private TextView gesture_text;

    private boolean isRepeatPin;
    private boolean isRepeatPassword;
    private boolean isRepeatPattern;
    private boolean isRepeatGesture;
    private StringBuilder tmpPin = new StringBuilder();

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
                        pin_text = (TextView) itemView.findViewById(R.id.pin_text);

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
                                    if (!isRepeatPin) {
                                        passCodeManager.addPassCode(tmpPin.toString(), LockUtils.Type.PIN, true);
                                        tmpPin.setLength(0);
                                        isRepeatPin = true;
                                        pin_text.setText(AndroidUtil.getResourceString(R.string.repeat_your_pin));
                                        pin_input.setText("");
                                        spinner.setEnabled(false);
                                    } else {
                                        boolean isSame = passCodeManager.comparePassCode(tmpPin.toString(), LockUtils.Type.PIN, true);
                                        if (!isSame) {
                                            TextUtil.shakeText(pin_text);
                                            pin_input.setText("");
                                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                        } else {
                                            pin_input.setText("");
                                            passCodeManager.addPassCode(tmpPin.toString(), LockUtils.Type.PIN, false);
                                            passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtils.Type.PIN);
                                            onBackPressed();
                                        }
                                        tmpPin.setLength(0);
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
                    case PAGE_PASSWORD:
                        itemView = mLayoutInflater.inflate(R.layout.pager_setting_password, container, false);

                        pas_input = (EditText) itemView.findViewById(R.id.pas_input);
                        pas_text = (TextView) itemView.findViewById(R.id.pas_text);

                        pas_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    checkPassword();
                                    return true;
                                }
                                return false;
                            }
                        });

                        pas_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        pas_text.setTextColor(0xffD2EAFC);
                        pas_text.setTextSize(14);

                        pas_input.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                        pas_input.setTextColor(Color.WHITE);
                        pas_input.setTextSize(16);

                        if (App.CURRENT_VERSION_SDK >= 21) {
                            pas_input.setLetterSpacing(0.7f);
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
                                    if (!isRepeatPattern) {
                                        passCodeManager.addPassCode(pattern, LockUtils.Type.PATTERN, true);
                                        isRepeatPattern = true;
                                        pattern_text.setText(AndroidUtil.getResourceString(R.string.repeat_your_pattern));
                                        spinner.setEnabled(false);
                                        lockPattern.clearPattern();
                                    } else {
                                        boolean isSame = passCodeManager.comparePassCode(pattern, LockUtils.Type.PATTERN, true);
                                        if (!isSame) {
                                            TextUtil.shakeText(pattern_text);
                                            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                            lockPattern.clearPattern();
                                        } else {
                                            passCodeManager.addPassCode(pattern, LockUtils.Type.PATTERN, false);
                                            passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtils.Type.PATTERN);
                                            lockPattern.clearPattern();
                                            onBackPressed();
                                        }
                                    }
                                }
                            }
                        });
                        pattern_text = (TextView) itemView.findViewById(R.id.pattern_text);
                        pattern_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        pattern_text.setTextColor(0xffD2EAFC);
                        pattern_text.setTextSize(14);

                        /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ((LinearLayout) itemView).getChildAt(0).getLayoutParams();
                            layoutParams.weight = 0;
                            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            layoutParams.weight = LinearLayout.LayoutParams.WRAP_CONTENT;
                        }*/

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

                        gesture_text = (TextView) itemView.findViewById(R.id.gesture_text);
                        gesture_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                        gesture_text.setTextColor(0xffD2EAFC);
                        gesture_text.setTextSize(14);

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
                                if (!isRepeatGesture) {
                                    passCodeManager.addPassCode(gesture, LockUtils.Type.GESTURE, true);
                                    isRepeatGesture = true;
                                    gesture_text.setText(AndroidUtil.getResourceString(R.string.repeat_your_gesture));
                                    spinner.setEnabled(false);
                                } else {
                                    boolean isSame = passCodeManager.comparePassCode(gesture, LockUtils.Type.GESTURE, true);
                                    if (!isSame) {
                                        TextUtil.shakeText(gesture_text);
                                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
                                    } else {
                                        passCodeManager.addPassCode(gesture, LockUtils.Type.GESTURE, false);
                                        passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtils.Type.GESTURE);
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
                spinner.setSelection(position, true);
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
                                + pin_text.getText() + ". \r\n " + AndroidUtil.getResourceString(R.string.must_be_4_numbers));
                        TextUtil.shakeText(pin_text);
                        break;
                    case PAGE_PASSWORD:
                        checkPassword();
                        break;
                    case PAGE_PATTERN:
                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.please_) + pattern_text.getText() + ".");
                        TextUtil.shakeText(pattern_text);
                        break;
                    case PAGE_GESTURE:
                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.please_) + gesture_text.getText() + ".");
                        TextUtil.shakeText(gesture_text);
                        break;
                }
            }
        });
        return true;
    }

    private void checkPassword() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (pas_input.getText().length() < 4) {
            AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.minimum_length_for_password_is_4));
            TextUtil.shakeText(pas_text);
            return;
        }
        if (!isRepeatPassword) {
            passCodeManager.addPassCode(pas_input.getText().toString(), LockUtils.Type.PASSWORD, true);
            isRepeatPassword = true;
            pas_text.setText(AndroidUtil.getResourceString(R.string.repeat_your_password));
            pas_input.setText("");
            spinner.setEnabled(false);
        } else {
            boolean isSame = passCodeManager.comparePassCode(pas_input.getText().toString(), LockUtils.Type.PASSWORD, true);
            if (!isSame) {
                TextUtil.shakeText(pas_text);
                pas_input.setText("");
                AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.passcodes_do_not_match));
            } else {
                passCodeManager.addPassCode(pas_input.getText().toString(), LockUtils.Type.PASSWORD, false);
                passCodeManager.setEnablePassCode(SetPassCodeActivity.this, true, LockUtils.Type.PASSWORD);
                pas_input.setText("");
                onBackPressed();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            spinner = (Spinner) findViewById(R.id.t_set_pass_code_spinner);
            spinner.setBackgroundColor(0xFF5B95C2);
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
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (pas_input != null) {
                        try {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(pas_input.getWindowToken(), 0);
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
