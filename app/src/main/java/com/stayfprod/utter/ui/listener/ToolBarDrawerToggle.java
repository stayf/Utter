package com.stayfprod.utter.ui.listener;


import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatListManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.service.VoiceController;
import com.stayfprod.utter.ui.view.DialogView;
import com.stayfprod.utter.ui.view.ToolBarListView;
import com.stayfprod.utter.util.AndroidUtil;

import java.lang.reflect.Field;

public class ToolBarDrawerToggle extends ActionBarDrawerToggle {
    private static final String LOG = ToolBarDrawerToggle.class.getSimpleName();

    private static final int DP_56 = AndroidUtil.dp(56);
    private static final int DP_1 = AndroidUtil.dp(1);

    private ActionMenuView mMenuView;
    private ToolBarListView mToolBarListView;
    private Toolbar mToolbar;
    private float mRemSlideOffset = 0;

    public ToolBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                               ToolBarListView toolBarListView, Toolbar toolbar) {
        super(activity, drawerLayout, R.string.drawer_state_opened, R.string.drawer_state_closed);
        this.mToolBarListView = toolBarListView;
        this.mToolbar = toolbar;
        drawerLayout.setDrawerListener(this);
    }

    public ToolBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar) {
        super(activity, drawerLayout, toolbar, R.string.drawer_state_opened, R.string.drawer_state_closed);
    }

    public void initMenu() {
        try {
            boolean invisible = mMenuView == null;
            Field field = mToolbar.getClass().getDeclaredField("mMenuView");
            field.setAccessible(true);
            mMenuView = (ActionMenuView) field.get(mToolbar);
            if (invisible) {
                mMenuView.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            Log.e(LOG, "init", e);
            Crashlytics.logException(e);
        }
    }

    @Override
    //slideOffset от 0 до 1
    public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);

        float dif = slideOffset - mRemSlideOffset;
        int y = (int) (DP_56 * dif);

        if (mMenuView != null) {
            if (mMenuView.getVisibility() == View.INVISIBLE) {
                mMenuView.setVisibility(View.VISIBLE);
            }
            mMenuView.setAlpha(slideOffset);
        }

        if (slideOffset == 1f) {
            y = DP_56;
        }

        if (slideOffset == 0f) {
            y = -DP_56;
            mMenuView.setVisibility(View.INVISIBLE);
        }

        if (y == 0) {
            y = dif >= 0 ? DP_1 : -DP_1;
        }

        mToolBarListView.scrollListBy(y);
        mRemSlideOffset = slideOffset;
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        DialogView.isNeedDraw = true;
        if (ChatManager.sIsNeedRemoveChat) {
            ChatManager.sIsNeedRemoveChat = false;
            ChatListManager.getManager().removeChat(ChatManager.getCurrentChatId());
        }
        FileManager.getManager().cleanTempStorage();
        ChatManager.getManager().clean();
        VoiceController.getController().fullDestroy();
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        DialogView.isNeedDraw = false;
        ChatManager.getManager().readChatHistory();
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        if (newState == DrawerLayout.STATE_DRAGGING || newState == DrawerLayout.STATE_SETTLING) {
            DialogView.isNeedDraw = true;
        } else {
            //DialogView.isNeedDraw = false;
        }
    }
}
