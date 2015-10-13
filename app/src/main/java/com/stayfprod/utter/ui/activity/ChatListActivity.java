package com.stayfprod.utter.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.manager.ChatListManager;
import com.stayfprod.utter.manager.UpdateHandler;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.Connection;
import com.stayfprod.utter.R;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.adapter.holder.ChatListHolder;
import com.stayfprod.utter.ui.component.MenuDrawerLayout;
import com.stayfprod.utter.ui.component.MusicBarWidget;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.drawable.IconDrawable;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;

public class ChatListActivity extends AbstractActivity implements Observer {

    public static volatile boolean sIsChatListActivityStarted;

    private MenuDrawerLayout mMenuDrawerLayout;
    private ImageView mUserIcon;
    private TextView mUserName;
    private TextView mUserPhone;
    private TextView mConnectionState;
    private ImageView mLockClose;
    private ImageView mLockOpen;
    private FrameLayout mLockerLayout;
    private MusicBarWidget mMusicBarWidget;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mMenuDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mMenuDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }

        if (!mMenuDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ChatListHolder.sIsClickOnItemBlocked = false;
        updateLocker();
        AudioPlayer.getPlayer().addObserver(this);
        mMusicBarWidget.checkOnStart();
    }

    private void updateLocker() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (passCodeManager.isEnabledPassCode(this)) {
            mLockerLayout.setVisibility(View.VISIBLE);
            if (passCodeManager.isLockedByUser(this)) {
                mLockOpen.setVisibility(View.GONE);
                mLockClose.setVisibility(View.VISIBLE);
            } else {
                mLockOpen.setVisibility(View.VISIBLE);
                mLockClose.setVisibility(View.GONE);
            }
        } else {
            mLockerLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AudioPlayer.getPlayer().deleteObserver(this);
    }

    @Override
    protected void onResume() {
        //overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        UserManager.getManager().deleteObserver(this);
        UpdateHandler.getHandler().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this)) {
            return;
        }

        sIsChatListActivityStarted = true;

        UserManager.getManager().addObserver(this);
        UpdateHandler.getHandler().addObserver(this);

        setContentView(R.layout.activity_chat_list);
        mMenuDrawerLayout = (MenuDrawerLayout) findViewById(R.id.menu_drawer_layout);

        mUserIcon = (ImageView) mMenuDrawerLayout.findViewById(R.id.d_user_icon);
        mUserPhone = (TextView) mMenuDrawerLayout.findViewById(R.id.d_user_phone);
        mUserName = (TextView) mMenuDrawerLayout.findViewById(R.id.d_user_name);

        mUserName.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mUserName.setTextSize(17);
        mUserName.setTextColor(0xffffffff);

        mUserPhone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mUserPhone.setTextSize(13);
        mUserPhone.setTextColor(0xffffffff);
        initToolbar();

        UserManager userManager = UserManager.getManager();
        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userManager.getCurrUserId());

        mUserIcon.setImageDrawable(IconFactory.createIcon(IconFactory.Type.USER, cachedUser.tgUser.id, cachedUser.initials, cachedUser.tgUser.profilePhoto.small));
        mUserName.setText(cachedUser.fullName);
        mUserPhone.setText("+" + cachedUser.tgUser.phoneNumber);

        ChatListManager.getManager().initRecycleView(this, mMenuDrawerLayout);

        mMusicBarWidget = new MusicBarWidget();
        mMusicBarWidget.init(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mConnectionState = (TextView) toolbar.findViewById(R.id.t_connection_state);
            mConnectionState.setText(Connection.currentState.text);
            mConnectionState.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            mConnectionState.setTextColor(0xffffffff);
            mConnectionState.setTextSize(20);

            toolbar.setNavigationIcon(R.mipmap.ic_menu);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMenuDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });

            mLockerLayout = findView(R.id.t_locker_layout);
            mLockClose = (ImageView) mLockerLayout.findViewById(R.id.t_locke_close);
            mLockOpen = (ImageView) mLockerLayout.findViewById(R.id.t_locke_open);

            mLockerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PassCodeManager passCodeManager = PassCodeManager.getManager();
                    if (passCodeManager.isLockedByUser(ChatListActivity.this)) {
                        passCodeManager.setLockByUser(ChatListActivity.this, false);
                    } else {
                        passCodeManager.setLockByUser(ChatListActivity.this, true);
                    }
                    updateLocker();
                }
            });
        }
    }


    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            final IconDrawable dr;
            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_MUSIC_PLAYER: {
                    mMusicBarWidget.checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.USER_IMAGE_UPDATE:
                    dr = IconFactory.createBitmapIcon(IconFactory.Type.USER, ((TdApi.File) nObject.getWhat()).path);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUserIcon.setImageDrawable(dr);
                        }
                    });
                    break;

                case NotificationObject.USER_DATA_UPDATE:
                    final CachedUser cachedUser = (CachedUser) nObject.getWhat();
                    final TdApi.File file = cachedUser.tgUser.profilePhoto.small;
                    dr = IconFactory.createIcon(IconFactory.Type.USER, cachedUser.tgUser.id, cachedUser.initials, file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUserIcon.setImageDrawable(dr);
                            mUserName.setText(cachedUser.fullName);
                            mUserPhone.setText("+" + cachedUser.tgUser.phoneNumber);
                        }
                    });
                    break;
                case NotificationObject.CHANGE_CONNECTION:
                    final String status = (String) nObject.getWhat();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mConnectionState.setText(status);
                        }
                    });
                    break;
            }
        }
    }
}
