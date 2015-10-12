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

    public static volatile boolean isChatListActivityStarted;

    private MenuDrawerLayout menuDrawerLayout;

    private ImageView userIcon;
    private TextView userName;
    private TextView userPhone;
    private TextView connectionState;
    private ImageView t_locke_close;
    private ImageView t_locke_open;
    private FrameLayout t_locker_layout;
    private MusicBarWidget musicBarWidget;

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
        if (menuDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            menuDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }

        if (!menuDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ChatListHolder.isClickOnItemBlocked = false;
        updateLocker();
        AudioPlayer.getPlayer().addObserver(this);
        musicBarWidget.checkOnStart();
    }

    private void updateLocker() {
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (passCodeManager.isEnabledPassCode(this)) {
            t_locker_layout.setVisibility(View.VISIBLE);
            if (passCodeManager.isLockedByUser(this)) {
                t_locke_open.setVisibility(View.GONE);
                t_locke_close.setVisibility(View.VISIBLE);
            } else {
                t_locke_open.setVisibility(View.VISIBLE);
                t_locke_close.setVisibility(View.GONE);
            }
        } else {
            t_locker_layout.setVisibility(View.GONE);
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

        isChatListActivityStarted = true;

        UserManager.getManager().addObserver(this);
        UpdateHandler.getHandler().addObserver(this);

        setContentView(R.layout.activity_chat_list);
        menuDrawerLayout = (MenuDrawerLayout) findViewById(R.id.menu_drawer_layout);

        userIcon = (ImageView) menuDrawerLayout.findViewById(R.id.d_user_icon);
        userPhone = (TextView) menuDrawerLayout.findViewById(R.id.d_user_phone);
        userName = (TextView) menuDrawerLayout.findViewById(R.id.d_user_name);

        userName.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        userName.setTextSize(17);
        userName.setTextColor(0xffffffff);

        userPhone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        userPhone.setTextSize(13);
        userPhone.setTextColor(0xffffffff);
        initToolbar();

        UserManager userManager = UserManager.getManager();
        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userManager.getCurrUserId());

        userIcon.setImageDrawable(IconFactory.createIcon(IconFactory.Type.USER, cachedUser.tgUser.id, cachedUser.initials, cachedUser.tgUser.profilePhoto.small));
        userName.setText(cachedUser.fullName);
        userPhone.setText("+" + cachedUser.tgUser.phoneNumber);

        ChatListManager.getManager().initRecycleView(this, menuDrawerLayout);

        musicBarWidget = new MusicBarWidget();
        musicBarWidget.init(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            connectionState = (TextView) toolbar.findViewById(R.id.t_connection_state);
            connectionState.setText(Connection.currentState.text);
            connectionState.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            connectionState.setTextColor(0xffffffff);
            connectionState.setTextSize(20);

            toolbar.setNavigationIcon(R.mipmap.ic_menu);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });

            t_locker_layout = findView(R.id.t_locker_layout);
            t_locke_close = (ImageView) t_locker_layout.findViewById(R.id.t_locke_close);
            t_locke_open = (ImageView) t_locker_layout.findViewById(R.id.t_locke_open);

            t_locker_layout.setOnClickListener(new View.OnClickListener() {
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
                    musicBarWidget.checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.USER_IMAGE_UPDATE:
                    try {
                        dr = IconFactory.createBitmapIcon(IconFactory.Type.USER, ((TdApi.File) nObject.getWhat()).path);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userIcon.setImageDrawable(dr);
                            }
                        });
                    } catch (Exception e) {
                        //
                    }
                    break;

                case NotificationObject.USER_DATA_UPDATE:
                    final CachedUser cachedUser = (CachedUser) nObject.getWhat();
                    final TdApi.File file = cachedUser.tgUser.profilePhoto.small;
                    dr = IconFactory.createIcon(IconFactory.Type.USER, cachedUser.tgUser.id, cachedUser.initials, file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userIcon.setImageDrawable(dr);
                            userName.setText(cachedUser.fullName);
                            userPhone.setText("+" + cachedUser.tgUser.phoneNumber);
                        }
                    });
                    break;
                case NotificationObject.CHANGE_CONNECTION:
                    final String status = (String) nObject.getWhat();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectionState.setText(status);
                        }
                    });
                    break;
            }
        }
    }
}
