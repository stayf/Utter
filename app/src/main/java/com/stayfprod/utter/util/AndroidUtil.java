package com.stayfprod.utter.util;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("SuspiciousNameCombination")
public class AndroidUtil {
    private static final String LOG = AndroidUtil.class.getSimpleName();

    public static final Typeface TF_ROBOTO_MEDIUM;
    public static final Typeface TF_ROBOTO_REGULAR;
    public static final Typeface TF_ROBOTO_BOLD;
    public static final Typeface TF_ROBOTO_LIGHT;

    public static float DENSITY;
    public static int WINDOW_PORTRAIT_WIDTH;
    public static int WINDOW_PORTRAIT_HEIGHT;

    public static volatile int P_KEYBOARD_HEIGHT;
    public static volatile int L_KEYBOARD_HEIGHT;

    static {
        try {
            TF_ROBOTO_MEDIUM = createFont("fonts/Roboto-Medium.ttf");
            TF_ROBOTO_REGULAR = createFont("fonts/Roboto-Regular.ttf");
            TF_ROBOTO_BOLD = createFont("fonts/Roboto-Bold.ttf");
            TF_ROBOTO_LIGHT = createFont("fonts/Roboto-Light.ttf");
        } catch (Exception e) {
            throw new RuntimeException(LOG + ":" + "can't init fonts", e);
        }

        try {
            int screenOrientation = App.getAppResources().getConfiguration().orientation;

            Point size = new Point();
            ((AppCompatActivity) App.getAppContext()).getWindowManager().getDefaultDisplay().getSize(size);

            switch (screenOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    WINDOW_PORTRAIT_WIDTH = size.y;
                    WINDOW_PORTRAIT_HEIGHT = size.x;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    WINDOW_PORTRAIT_WIDTH = size.x;
                    WINDOW_PORTRAIT_HEIGHT = size.y;
                    break;
            }

            DENSITY = App.getAppResources().getDisplayMetrics().density;

            L_KEYBOARD_HEIGHT = dp(160);
            P_KEYBOARD_HEIGHT = dp(230);
        } catch (Exception e) {
            DENSITY = 3.0f;
            Log.e(LOG, "init", e);
            Crashlytics.logException(e);
        }
    }

    public static String getResourceString(int id) {
        return App.getAppResources().getString(id);
    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    public static void runInUI(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            App.appHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    public static void runInUI(Runnable runnable, int time) {
        App.appHandler.postDelayed(runnable, time);
    }

    public static int dp(float value) {
        return (int) (DENSITY * value + 0.5f);
    }

    public static int px(float dp) {
        return (int) ((dp - 0.5f) / DENSITY);
    }

    public static void setEditTextTypeface(EditText editText) {
        editText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        editText.setTextSize(17);
        editText.setTextColor(0xFF333333);
    }

    public static void setTextViewTypeface(TextView textView) {
        textView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        textView.setTextSize(15);
        textView.setTextColor(0xFF8A8A8A);
    }

    public static void setErrorTextViewTypeface(TextView textView) {
        textView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        textView.setTextSize(15);
        textView.setTextColor(0xFFDB7777);
    }

    public static int getKeyboardHeight() {
        switch (App.appContext.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return P_KEYBOARD_HEIGHT;
            case Configuration.ORIENTATION_LANDSCAPE:
                return L_KEYBOARD_HEIGHT;
        }
        return 0;
    }

    public static void setKeyboardHeight(int values) {
        switch (App.appContext.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                P_KEYBOARD_HEIGHT = values;
            case Configuration.ORIENTATION_LANDSCAPE:
                L_KEYBOARD_HEIGHT = values;
        }
    }

    private static Typeface createFont(String font) {
        return Typeface.createFromAsset(App.getAppContext().getAssets(), font);
    }

    public static void showToastShort(final String msg) {
        runInUI(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(App.getAppContext(), msg.replace("_", " "), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static void showToastLong(final String msg) {
        runInUI(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(App.getAppContext(), msg.replace("_", " "), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public static void setImageAsyncWithAnim(final ImageView imageView, final BitmapDrawable bitmapDrawable, final View itemView, final String tag) {
        runInUI(new Runnable() {
            @Override
            public void run() {
                if (isItemViewVisible(itemView, tag) && setImageDrawable(imageView, bitmapDrawable))
                    showViewAnim(imageView);
            }
        });
    }

    public static void setImageAsync(final ImageView imageView, final BitmapDrawable bitmapDrawable, final View itemView, final String tag) {
        runInUI(new Runnable() {
            @Override
            public void run() {
                if (isItemViewVisible(itemView, tag)) {
                    setImageDrawable(imageView, bitmapDrawable);
                }
            }
        });
    }

    public static void setDrawableAsync(final ImageView imageView, final Drawable drawable, final View itemView, final String tag) {
        runInUI(new Runnable() {
            @Override
            public void run() {
                if (isItemViewVisible(itemView, tag)) {
                    setImageDrawable(imageView, drawable);
                }
            }
        });
    }

    public static boolean isItemViewVisible(final View itemView, String tag) {
        return itemView != null && itemView.getTag().toString().equals(tag);
    }

    public static void showViewAnim(ImageView imageView) {
        ObjectAnimator.ofFloat(imageView, View.ALPHA, 0.0f, 1.0f).setDuration(100).start();
    }

    public static boolean setImageDrawable(ImageView imageView, Drawable drawable) {
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
            return true;
        } else {
            setImagePlaceholder(imageView);
            return false;
        }
    }

    public static boolean setImageDrawable(ImageView imageView, BitmapDrawable drawable) {
        if (drawable != null && drawable.getBitmap() != null) {
            imageView.setImageDrawable(drawable);
            return true;
        } else {
            setImagePlaceholder(imageView);
            return false;
        }
    }

    public static void setImagePlaceholder(ImageView imageView) {
        imageView.setImageResource(android.R.color.transparent);
    }

    /*
    * View.GONE,View.VISIBLE,View.INVISIBLE
    * */
    public static void setVisibility(View view, int val) {
        if (view.getVisibility() != val) {
            view.setVisibility(val);
        }
    }

    //большую картинку вмещает в определенную облась, а маленькую ставит строго по центру
    public static void setCenterBounds(BitmapDrawable bitmapDrawable, int w, int h) {
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                int original_width = bitmap.getWidth();
                int original_height = bitmap.getHeight();
                int new_width = original_width;
                int new_height = original_height;

                if (original_width > w) {
                    new_width = w;
                    new_height = (new_width * original_height) / original_width;
                }

                if (new_height > h) {
                    new_height = h;
                    new_width = (new_height * original_width) / original_height;
                }

                int dx = (w - new_width) >> 1;
                int dy = (h - new_height) >> 1;

                bitmapDrawable.setBounds(dx, dy, new_width + dx, new_height + dy);
            }
        }
    }

    public static void setCenterBoundsFullHeight(BitmapDrawable bitmapDrawable, int w, int h) {
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                int original_width = bitmap.getWidth();
                int original_height = bitmap.getHeight();
                int new_width = original_width;
                int new_height = original_height;

                if (original_width > w) {
                    new_width = w;
                    new_height = (new_width * original_height) / original_width;
                }

                if (new_height > h) {
                    new_height = h;
                    new_width = (new_height * original_width) / original_height;
                }

                if (new_height < h) {
                    new_height = h;
                    new_width = (new_height * original_width) / original_height;
                }

                int dx = (w - new_width) >> 1;
                int dy = (h - new_height) >> 1;

                bitmapDrawable.setBounds(dx, dy, new_width + dx, new_height + dy);
            }
        }
    }

    public static void setCropBounds(BitmapDrawable bitmapDrawable, int size) {
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                int original_width = bitmap.getWidth();
                int original_height = bitmap.getHeight();
                int new_width = original_width;
                int new_height = original_height;

                if (original_width > original_height) {
                    new_height = size;
                    float scale;
                    int dx;

                    scale = (float) size / original_height;
                    new_width = (int) (original_width * scale);
                    dx = (new_width - size) >> 1;
                    bitmapDrawable.setBounds(-dx, 0, size + dx, size);
                } else if (original_width == original_height) {
                    bitmapDrawable.setBounds(0, 0, size, size);
                } else {
                    new_width = size;
                    float scale;
                    int dy;
                    scale = (float) size / original_width;
                    new_height = (int) (original_height * scale);
                    dy = (new_height - size) >> 1;

                    //Logs.e(dy + " " + new_height + " " + scale + " " + original_height);
                    bitmapDrawable.setBounds(0, -dy, size, size + dy);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void setBackgroundDrawable(View view, BitmapDrawable bitmapDrawable) {
        if (App.CURRENT_VERSION_SDK >= 16) {
            if (bitmapDrawable != null)
                view.setBackground(bitmapDrawable);
            else
                view.setBackgroundResource(android.R.color.transparent);
        } else {
            if (bitmapDrawable != null)
                view.setBackgroundDrawable(bitmapDrawable);
            else
                view.setBackgroundResource(android.R.color.transparent);
        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void setBackground(View v, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            v.setBackground(drawable);
        } else {
            v.setBackgroundDrawable(drawable);
        }
    }

    public static void setBackgroundChangedView(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private Handler handlerBackground = new Handler();

            Runnable actionChangeBackground = new Runnable() {
                @Override
                public void run() {
                    view.setBackgroundColor(0x0D000000);
                }
            };

            private float mDownX;
            private float mDownY;
            boolean isPressed;
            private final float SCROLL_THRESHOLD = AndroidUtil.dp(10);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mDownX = event.getX();
                        mDownY = event.getY();
                        handlerBackground.postDelayed(actionChangeBackground, 50);
                        isPressed = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE: {
                        handlerBackground.removeCallbacks(actionChangeBackground);
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (isPressed && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD
                                || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                            handlerBackground.removeCallbacks(actionChangeBackground);
                            v.setBackgroundColor(Color.TRANSPARENT);
                            isPressed = false;
                        }
                        break;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Return a float value within the range.
     * This is just a wrapper for Math.min() and Math.max().
     * This may be useful if you feel it confusing ("Which is min and which is max?").
     *
     * @param value    the target value
     * @param minValue minimum value. If value is less than this, minValue will be returned
     * @param maxValue maximum value. If value is greater than this, maxValue will be returned
     * @return float value limited to the range
     */
    public static float getFloat(final float value, final float minValue, final float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

    /**
     * Add an OnGlobalLayoutListener for the view.
     * This is just a convenience method for using {@code ViewTreeObserver.OnGlobalLayoutListener()}.
     * This also handles removing listener when onGlobalLayout is called.
     *
     * @param view     the target view to add global layout listener
     * @param runnable runnable to be executed after the view is laid out
     */
    public static void addOnGlobalLayoutListener(final View view, final Runnable runnable) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1;
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static byte[] shortToByteTwiddle(short[] input, int limit) {
        int short_index, byte_index;

        byte[] buffer = new byte[limit * 2];

        short_index = byte_index = 0;

        for (/*NOP*/; short_index != limit; /*NOP*/) {
            buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
            buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

            ++short_index;
            byte_index += 2;
        }

        return buffer;
    }

}
