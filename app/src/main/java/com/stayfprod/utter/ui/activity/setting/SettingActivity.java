package com.stayfprod.utter.ui.activity.setting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.AuthManager;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.manager.UpdateHandler;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.ScrollState;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.component.ProfilePhotoChoose;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.view.FloatingActionButton;
import com.stayfprod.utter.ui.view.ObservableScrollView;
import com.stayfprod.utter.ui.view.ObservableScrollViewCallbacks;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;

public class SettingActivity extends AbstractActivity implements ObservableScrollViewCallbacks, Observer {

    @Override
    protected void onStart() {
        super.onStart();
        UpdateHandler.getHandler().addObserver(this);
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        if (isPassCodeLockEnabled = passCodeManager.isEnabledPassCode(this)) {
            a_setting_passcode_status.setText(AndroidUtil.getResourceString(R.string.enabled));
        } else {
            a_setting_passcode_status.setText(AndroidUtil.getResourceString(R.string.disabled));
        }
    }

    @Override
    protected void onDestroy() {
        UpdateHandler.getHandler().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UpdateHandler.getHandler().deleteObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_name:
                if (cachedUser != null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("userId", cachedUser.tgUser.id);
                    bundle.putString("firstName", cachedUser.tgUser.firstName);
                    bundle.putString("secondName", cachedUser.tgUser.lastName);
                    Intent intent = new Intent(this, EditNameActivity.class);
                    intent.putExtras(bundle);
                    this.startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
                break;
            case R.id.action_log_out:
                /*StickerManager.getManager().destroy();
                ChatListManager.getManager().destroy();*/
                new AuthManager(null, null).reset(this);
                break;
        }
        return true;
    }

    private View mFlexibleSpaceView;
    private Toolbar mToolbarView;
    private TextView mTitleView;
    private TextView mSubTitleView;
    private ImageView mTitleImage;
    private int mFlexibleSpaceHeight;
    private int mFabMargin;
    private FloatingActionButton mFab;
    private CachedUser cachedUser;
    private TextView a_setting_passcode_status;
    private boolean isPassCodeLockEnabled;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_setting);

        mToolbarView = findView(R.id.toolbar);
        setSupportActionBar(mToolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mToolbarView.setNavigationIcon(R.mipmap.ic_back);

        mTitleImage = (ImageView) findViewById(R.id.t_parallax_icon);
        mTitleView = (TextView) findViewById(R.id.t_parallax_title);
        mSubTitleView = (TextView) findViewById(R.id.t_parallax_subtitle);

        mTitleView.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mTitleView.setTextSize(18);
        mTitleView.setTextColor(0xffffffff);

        mSubTitleView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mSubTitleView.setTextSize(14);
        mSubTitleView.setTextColor(0xffd2eafc);

        mFlexibleSpaceView = findViewById(R.id.flexible_space);
        setTitle(null);

        final ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.scroll);
        scrollView.setScrollViewCallbacks(this);

        mFlexibleSpaceHeight = AndroidUtil.dp(156) - getToolBarSize();
        int flexibleSpaceAndToolbarHeight = AndroidUtil.dp(156);

        findViewById(R.id.body).setPadding(0, flexibleSpaceAndToolbarHeight, 0, 0);
        mFlexibleSpaceView.getLayoutParams().height = flexibleSpaceAndToolbarHeight;

        AndroidUtil.addOnGlobalLayoutListener(mTitleView, new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpaceText(scrollView.getCurrentScrollY());
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setFloatingActionButtonColor(Color.WHITE);
        mFab.setFloatingActionButtonDrawable(FileUtils.decodeResource(R.mipmap.ic_attach_photo));

        mFab.getLayoutParams().height = Constant.DP_72;
        mFab.getLayoutParams().width = Constant.DP_72;

        mFabMargin = Constant.DP_6;
        mFab.setScaleX(1);
        mFab.setScaleY(1);
        mFab.showButton();

        TextView a_setting_hash_tag = (TextView) findViewById(R.id.a_setting_hash_tag);
        a_setting_hash_tag.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_hash_tag.setTextSize(17);
        a_setting_hash_tag.setTextColor(0xFF222222);

        TextView a_setting_username = (TextView) findViewById(R.id.a_setting_username);
        a_setting_username.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_username.setTextSize(14);
        a_setting_username.setTextColor(0xFF8a8a8a);

        TextView a_setting_phone = (TextView) findViewById(R.id.a_setting_phone);
        a_setting_phone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_phone.setTextSize(17);
        a_setting_phone.setTextColor(0xFF222222);

        TextView a_setting_phone2 = (TextView) findViewById(R.id.a_setting_phone2);
        a_setting_phone2.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_phone2.setTextSize(14);
        a_setting_phone2.setTextColor(0xFF8a8a8a);

        TextView a_setting_passcode_lock = (TextView) findViewById(R.id.a_setting_passcode_lock);
        a_setting_passcode_lock.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_passcode_lock.setTextSize(17);
        a_setting_passcode_lock.setTextColor(0xFF222222);

        a_setting_passcode_status = (TextView) findViewById(R.id.a_setting_passcode_status);
        a_setting_passcode_status.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_setting_passcode_status.setTextSize(16);
        a_setting_passcode_status.setTextColor(0xFF8a8a8a);
        //акшион)

        mToolbarView.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //todo может быть кейс когда иконки не будет И ее нужно будет подгрузить

        try {
            TdApi.File file = null;
            int id = 0;
            String subTitle = "";
            String initials = "";
            String title = "";

            UserManager userManager = UserManager.getManager();
            if (userManager.getCurrUserId() != 0) {
                cachedUser = userManager.getUserByIdWithRequestAsync(userManager.getCurrUserId());
                file = cachedUser.tgUser.profilePhoto.small;
                initials = cachedUser.initials;
                title = cachedUser.fullName;
                subTitle = AndroidUtil.getResourceString(R.string.online);
                a_setting_phone.setText("+" + cachedUser.tgUser.phoneNumber);
                a_setting_hash_tag.setText(TextUtil.isNotBlank(cachedUser.tgUser.username) ? cachedUser.tgUser.username : AndroidUtil.getResourceString(R.string.unknown));
                id = userManager.getCurrUserId();
            }

            mTitleView.setText(title);
            mSubTitleView.setText(subTitle);
            mTitleImage.setImageDrawable(IconFactory.createIcon(IconFactory.Type.TITLE, id, initials, file));
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        RelativeLayout a_setting_content_2 = findView(R.id.a_setting_content_2);
        a_setting_content_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPassCodeLockEnabled) {
                    PassCodeManager passCodeManager = PassCodeManager.getManager();
                    passCodeManager.openEnterPassCodeActivity(SettingActivity.this, true);
                } else {
                    Intent intent = new Intent(SettingActivity.this, PassCodeLockActivity.class);
                    SettingActivity.this.startActivity(intent);
                    SettingActivity.this.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            }
        });

        profilePhotoChoose = new ProfilePhotoChoose(ProfilePhotoChoose.TYPE_SETTING_PHOTO);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePhotoChoose.showDialog(SettingActivity.this);
            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private Boolean isFullWidth;

    private void updateFlexibleSpaceText(final int scrollY) {
        mFlexibleSpaceView.setTranslationY(-scrollY);
        int adjustedScrollY = (int) AndroidUtil.getFloat(scrollY, 0, mFlexibleSpaceHeight);
        float maxScale = 0.2f;
        float scale = maxScale * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight;

        mTitleView.setPivotX(0);
        mTitleView.setPivotY(0);
        mTitleView.setScaleX(1 + scale);
        mTitleView.setScaleY(1 + scale);
        int maxTitleTranslationY = mToolbarView.getHeight() + Constant.DP_40 - (int) (mTitleView.getHeight() * (1 + scale));
        int titleTranslationY = (int) (maxTitleTranslationY * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight);
        mTitleView.setTranslationY(titleTranslationY);
        mTitleView.setTranslationX(-scale * Constant.DP_2);

        mSubTitleView.setPivotX(0);
        mSubTitleView.setPivotY(0);
        mSubTitleView.setTranslationY(titleTranslationY + scale * Constant.DP_40);
        mSubTitleView.setTranslationX(-scale * Constant.DP_2);

        float maxScaleImage = 0.72f;
        float scaleImage = maxScaleImage * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight;
        mTitleImage.setPivotX(0);
        mTitleImage.setPivotY(0);
        mTitleImage.setScaleX(1 + scaleImage);
        mTitleImage.setScaleY(1 + scaleImage);
        mTitleImage.setTranslationY(scaleImage * Constant.DP_80);
        mTitleImage.setTranslationX(-scaleImage * Constant.DP_60);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceHeight - mFab.getHeight() / 2;
        int minFabTranslationY = Constant.DP_2;
        float fabTranslationY = AndroidUtil.getFloat(
                -scrollY + mFlexibleSpaceHeight - mFab.getHeight() / 2, minFabTranslationY, maxFabTranslationY);
        mFab.setTranslationX(mFlexibleSpaceView.getWidth() - mFabMargin - mFab.getWidth());
        mFab.setTranslationY(fabTranslationY + getToolBarSize());

       /* if (fabTranslationY <= (minFabTranslationY)) {
            mFab.hideButtonAnimated();
        } else {
            mFab.showButtonAnimated();
        }*/

        if (fabTranslationY <= (minFabTranslationY)) {
            if (isFullWidth == null || isFullWidth) {
                isFullWidth = false;
                ((RelativeLayout.LayoutParams) mTitleView.getLayoutParams()).rightMargin = Constant.DP_100;
                mTitleView.requestLayout();
                mTitleView.invalidate();
            }
            mFab.hideButtonAnimated();
        } else {
            if (isFullWidth == null || !isFullWidth) {
                isFullWidth = true;
                ((RelativeLayout.LayoutParams) mTitleView.getLayoutParams()).rightMargin = Constant.DP_36;
                mTitleView.requestLayout();
                mTitleView.invalidate();
            }
            mFab.showButtonAnimated();
        }


    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            final IconDrawable dr;
            switch (nObject.getMessageCode()) {
                case NotificationObject.USER_IMAGE_UPDATE:
                    try {
                        dr = IconFactory.createBitmapIcon(IconFactory.Type.TITLE, ((TdApi.File) nObject.getWhat()).path);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mTitleImage != null)
                                    mTitleImage.setImageDrawable(dr);
                            }
                        });
                    } catch (Exception e) {
                        //
                    }
                    break;

                case NotificationObject.USER_DATA_UPDATE:
                    final CachedUser cachedUser = (CachedUser) nObject.getWhat();
                    final TdApi.File file = cachedUser.tgUser.profilePhoto.small;
                    dr = IconFactory.createIcon(IconFactory.Type.TITLE, cachedUser.tgUser.id, cachedUser.initials, file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTitleView.setText(cachedUser.fullName);
                            mSubTitleView.setText(ChatHelper.lastSeenUser(cachedUser.tgUser.status));
                            mTitleImage.setImageDrawable(dr);
                        }
                    });
                    break;
            }
        }
    }

    private ProfilePhotoChoose profilePhotoChoose;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        profilePhotoChoose.processImage(requestCode, resultCode, data, this);
    }
}
