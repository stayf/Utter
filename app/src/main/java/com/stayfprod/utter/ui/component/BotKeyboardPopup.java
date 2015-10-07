package com.stayfprod.utter.ui.component;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.stayfprod.utter.manager.BotManager;

public class BotKeyboardPopup extends PopupWindow {

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen(int keyBoardHeight);

        void onKeyboardClose();
    }

    private OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    private int keyBoardHeight = 0;
    private Boolean isFirstOpening = false;
    private Boolean isOpenedKeyboard = false;
    private Boolean isOpenedPopup = false;
    private Context mContext;
    private View rootView;

    public BotKeyboardPopup(Context context, View rootView) {
        super(context);
        mContext = context;
        this.rootView = rootView;
        setContentView(BotManager.getManager().getKeyboard());
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(WindowManager.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(com.stayfprod.emojicon.R.dimen.keyboard_height));
        this.setBackgroundDrawable(new ColorDrawable());
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    public void showAtBottom() {
        isFirstOpening = false;
        setContentView(BotManager.getManager().getKeyboard());
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
    }

    public void showAtBottomFirstTime() {
        setContentView(BotManager.getManager().getKeyboard());
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        isFirstOpening = true;
    }

    public Boolean isKeyBoardOpen() {
        return isOpenedKeyboard;
    }

    public Boolean isPopupOpen() {
        return isOpenedPopup;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public int calculateScreenHeightForLollipop() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public void setSizeForSoftKeyboard() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight;
                if (Build.VERSION.SDK_INT >= 5.0) {
                    screenHeight = calculateScreenHeightForLollipop();
                } else {
                    screenHeight = rootView.getRootView().getHeight();
                }
                int heightDifference = screenHeight - (r.bottom - r.top);

                int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
                }

                isOpenedPopup = rootView.getPaddingBottom() > 0;

                if (heightDifference > 100) {
                    keyBoardHeight = heightDifference;
                    setSize(WindowManager.LayoutParams.MATCH_PARENT, keyBoardHeight);
                    if (!isOpenedKeyboard) {
                        if (onSoftKeyboardOpenCloseListener != null)
                            onSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
                    }
                    isOpenedKeyboard = true;
                } else {
                    isOpenedKeyboard = false;
                    if (onSoftKeyboardOpenCloseListener != null && isShowing() && !isFirstOpening)
                        onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }
}
