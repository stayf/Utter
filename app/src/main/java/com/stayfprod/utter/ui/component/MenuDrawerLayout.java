package com.stayfprod.utter.ui.component;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.DrawerMenu;
import com.stayfprod.utter.ui.activity.ContactListActivity;
import com.stayfprod.utter.ui.activity.setting.SettingActivity;
import com.stayfprod.utter.ui.adapter.MenuDrawerAdapter;
import com.stayfprod.utter.util.AndroidUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MenuDrawerLayout extends DrawerLayout {

    private static final String LOG = MenuDrawerLayout.class.getSimpleName();
    private static final int CONST_Y = 150;

    private View mDrawer;
    private float mX = 0f;
    private float mY = 0f;
    private boolean mDragOpenActivated;

    public MenuDrawerLayout(Context context) {
        super(context);
        init();
    }

    public MenuDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        try {
            Field mDragger = this.getClass().getSuperclass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(this);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            mEdgeSize.setInt(draggerObj, 0);//set val in dp
        } catch (Exception e) {
            Log.e(LOG, "init", e);
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDrawer = findViewById(R.id.left_drawer);
        ListView mDrawerListView = (ListView) findViewById(R.id.left_drawer_list);
        List<DrawerMenu> menuList = new ArrayList<>(3);
        menuList.add(new DrawerMenu(R.mipmap.menu_contacts, AndroidUtil.getResourceString(R.string.contacts)));
        menuList.add(new DrawerMenu(R.mipmap.ic_settings, AndroidUtil.getResourceString(R.string.settings)));
        menuList.add(new DrawerMenu(R.mipmap.ic_logout, AndroidUtil.getResourceString(R.string.log_out)));
        mDrawerListView.setAdapter(new MenuDrawerAdapter(getContext(), menuList));
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 2: {
                        new AuthManager(null, null).reset((AppCompatActivity) getContext());
                        break;
                    }
                    case 0: {
                        MenuDrawerLayout.this.closeDrawer(mDrawer);
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getContext(), ContactListActivity.class);
                                getContext().startActivity(intent);
                            }
                        }, 200);
                        break;
                    }
                    case 1: {
                        if (UserManager.getManager().getCurrUserId() != 0) {
                            MenuDrawerLayout.this.closeDrawer(mDrawer);
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getContext(), SettingActivity.class);
                                    getContext().startActivity(intent);
                                }
                            }, 200);
                        }
                        break;
                    }
                }
            }
        });
    }

    private boolean proceedMoveAction(MotionEvent event) {
        if (isDrawerOpen(mDrawer)) {
            //закрываем дровер
            super.onTouchEvent(event);
            return true;
        } else {
            boolean isGoodY = Math.abs(mY - event.getY()) < 0.99f;
            boolean isGoodXY = (event.getX() > mX && isGoodY);
            //не стоит открывать если иксы одинаковы,ждем следующее событие
            if (isGoodXY || mDragOpenActivated) {
                //активируем
                if (!mDragOpenActivated) {
                    event.setLocation(-1, CONST_Y);
                    event.setAction(MotionEvent.ACTION_DOWN);
                    super.onTouchEvent(event);
                    mDragOpenActivated = true;
                } else {
                    if (event.getX() != mX) {
                        //продолжаем тянуть
                        float dif = event.getX() - mX;
                        dif = dif <= 0f ? 0.1f : dif;

                        event.setLocation(dif, CONST_Y);
                        //info падает с двумя пальцами pointerIndex out of range
                        try {
                            super.onTouchEvent(event);
                        } catch (IllegalArgumentException e) {
                            closeDrawer(mDrawer);
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //смысл в том, что сначало поймать момент когда юзер проведет пальцем(down,move в право), затем кидаем x координату фальшивую, после все должно само отработать
        boolean returned = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP: {
                if (mDragOpenActivated) {
                    returned = true;
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                mX = event.getX();
                mY = event.getY();
                mDragOpenActivated = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                returned = proceedMoveAction(event);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                //тащим в данный момент
                if (mDragOpenActivated) {
                    //отмена идет как move, но тем не менее нужно отдать следующим обработчикам + обратно вернуть event
                    event.setAction(MotionEvent.ACTION_MOVE);
                    proceedMoveAction(event);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    returned = false;
                    break;
                }
                //продалжаем в default
            }
            default: {
                //последнее действие после открытия дровера обрабатывается дровером
                if (mDragOpenActivated) {
                    returned = true;
                }
                mDragOpenActivated = false;
                mX = 0f;
                mY = 0f;
                super.onTouchEvent(event);
            }
        }

        if (mDragOpenActivated || isDrawerOpen(mDrawer) || isDrawerVisible(mDrawer)) {
            //не требуем других обработчиков
            returned = true;
        }

        //Log.e("event=",returned + " " + event.getAction());

        return returned;
    }
}
