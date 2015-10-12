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

    private int mKeyBoardHeight = 0;
    private Boolean mIsFirstOpening = false;
    private Boolean mIsOpenedKeyboard = false;
    private Boolean mIsOpenedPopup = false;
    private Context mContext;
    private View mRootView;
    private OnSoftKeyboardOpenCloseListener mOnSoftKeyboardOpenCloseListener;

    public BotKeyboardPopup(Context context, View rootView) {
        super(context);
        mContext = context;
        this.mRootView = rootView;
        setContentView(BotManager.getManager().getKeyboard());
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(WindowManager.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(com.stayfprod.emojicon.R.dimen.keyboard_height));
        this.setBackgroundDrawable(new ColorDrawable());
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.mOnSoftKeyboardOpenCloseListener = listener;
    }

    public void showAtBottom() {
        mIsFirstOpening = false;
        setContentView(BotManager.getManager().getKeyboard());
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
    }

    public void showAtBottomFirstTime() {
        setContentView(BotManager.getManager().getKeyboard());
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        mIsFirstOpening = true;
    }

    public Boolean isKeyBoardOpen() {
        return mIsOpenedKeyboard;
    }

    public Boolean isPopupOpen() {
        return mIsOpenedPopup;
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
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);
                int screenHeight;
                if (Build.VERSION.SDK_INT >= 5.0) {
                    screenHeight = calculateScreenHeightForLollipop();
                } else {
                    screenHeight = mRootView.getRootView().getHeight();
                }
                int heightDifference = screenHeight - (r.bottom - r.top);

                int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
                }

                mIsOpenedPopup = mRootView.getPaddingBottom() > 0;

                if (heightDifference > 100) {
                    mKeyBoardHeight = heightDifference;
                    setSize(WindowManager.LayoutParams.MATCH_PARENT, mKeyBoardHeight);
                    if (!mIsOpenedKeyboard) {
                        if (mOnSoftKeyboardOpenCloseListener != null)
                            mOnSoftKeyboardOpenCloseListener.onKeyboardOpen(mKeyBoardHeight);
                    }
                    mIsOpenedKeyboard = true;
                } else {
                    mIsOpenedKeyboard = false;
                    if (mOnSoftKeyboardOpenCloseListener != null && isShowing() && !mIsFirstOpening)
                        mOnSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }
}
