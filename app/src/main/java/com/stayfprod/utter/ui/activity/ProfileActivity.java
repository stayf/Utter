package com.stayfprod.utter.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ContactListManager;
import com.stayfprod.utter.manager.NotificationManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.manager.UpdateHandler;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.model.ScrollState;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.setting.EditNameActivity;
import com.stayfprod.utter.ui.adapter.ProfileAdapter;
import com.stayfprod.utter.ui.component.ProfilePhotoChoose;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.view.ContactView;
import com.stayfprod.utter.ui.view.FloatingActionButton;
import com.stayfprod.utter.ui.view.ObservableRecyclerView;
import com.stayfprod.utter.ui.view.ObservableScrollViewCallbacks;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ProfileActivity extends AbstractActivity implements Observer, ObservableScrollViewCallbacks {

    private static final String LOG = ProfileActivity.class.getSimpleName();

    private static volatile boolean sIsClickOnItemBlocked;

    public static volatile boolean sIsNeedUpdateUserList;
    public static int sOpenedProfileCounter = 0;

    public Boolean isSubProfile;

    private ObservableRecyclerView mRecyclerView;
    private View mFlexibleSpaceView;
    private Toolbar mToolbarView;
    private TextView mTitleView;
    private TextView mSubTitleView;
    private ImageView mTitleImage;
    private int mFlexibleSpaceHeight;
    private int mFabMargin;
    private FloatingActionButton mFab;
    private ProfilePhotoChoose mProfilePhotoChoose;

    private Boolean mIsGroup;
    private Boolean mIsMuted;
    private Boolean mIsFullWidth;
    private ProfileManager.FOR mFor;
    private ProfileAdapter mProfileAdapter;
    private final ArrayList<Object> mItems = new ArrayList<Object>(203);

    @Override
    public void onBackPressed() {
        NotificationManager.getManager().deleteObserver(this);
        ProfileManager.getManager().deleteObserver(this);
        openedProfileCounterDown();
        if (!isSubProfile) {
            ProfileManager.getManager().clean();
        } else {
            ProfileManager.getManager().revertOldChatInfo();
        }

        UpdateHandler.getHandler().deleteObserver(this);
        SharedMediaManager.getManager().deleteObserver(this);

        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        ContactListManager.getManager().clean();
    }

    @Override
    protected void onStart() {
        NotificationManager.getManager().addObserver(this);
        ProfileManager.getManager().addObserver(this);
        UpdateHandler.getHandler().addObserver(this);
        SharedMediaManager.getManager().addObserver(this);
        super.onStart();

        if (sIsNeedUpdateUserList) {
            updateUserList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        NotificationManager.getManager().deleteObserver(this);
        ProfileManager.getManager().deleteObserver(this);
        UpdateHandler.getHandler().deleteObserver(this);
        SharedMediaManager.getManager().deleteObserver(this);
        super.onDestroy();
    }

    public void forceFinish() {
        UpdateHandler.getHandler().deleteObserver(this);
        NotificationManager.getManager().deleteObserver(this);
        ProfileManager.getManager().deleteObserver(this);
        SharedMediaManager.getManager().deleteObserver(this);
        ProfileManager.getManager().clean();
        finish();
        openedProfileCounterDown();
    }

    private void openedProfileCounterDown() {
        sOpenedProfileCounter--;
        if (sOpenedProfileCounter < 0) {
            sOpenedProfileCounter = 0;
        }
    }

    private void openedProfileCounterUp() {
        if (sOpenedProfileCounter < 0) {
            sOpenedProfileCounter = 0;
        }
        sOpenedProfileCounter++;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        sIsNeedUpdateUserList = false;

        openedProfileCounterUp();

        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();

        ProfileManager profileManager = ProfileManager.getManager();
        final ChatInfo chatInfo = profileManager.getChatInfo();
        if (bundle != null) {
            mIsGroup = bundle.getBoolean("isGroup");
            mIsMuted = bundle.getBoolean("isMuted");
            isSubProfile = bundle.getBoolean("isSubProfile", false);
            if (chatInfo.isBot)
                mFor = ProfileManager.FOR.BOT;
            else if (mIsGroup)
                mFor = ProfileManager.FOR.GROUP;
            else
                mFor = ProfileManager.FOR.USER;
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setScrollViewCallbacks(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mTitleImage = findView(R.id.t_parallax_icon);
        mTitleView = findView(R.id.t_parallax_title);
        mSubTitleView = findView(R.id.t_parallax_subtitle);

        mTitleView.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mTitleView.setTextSize(18);
        mTitleView.setTextColor(0xffffffff);

        mSubTitleView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mSubTitleView.setTextSize(14);
        mSubTitleView.setTextColor(0xffd2eafc);

        mFlexibleSpaceView = findViewById(R.id.flexible_space);
        setTitle(null);
        mToolbarView = findView(R.id.toolbar);

        mFlexibleSpaceHeight = AndroidUtil.dp(156) - getToolBarSize();

        mFlexibleSpaceView.getLayoutParams().height = mFlexibleSpaceHeight + getToolBarSize();

        AndroidUtil.addOnGlobalLayoutListener(mTitleView, new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpaceText(mRecyclerView.getCurrentScrollY());
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setFloatingActionButtonColor(Color.WHITE);

        if (mIsGroup && UserManager.getManager().getCurrUserId() == chatInfo.groupChatFull.adminId) {
            mFab.setFloatingActionButtonDrawable(FileUtil.decodeResource(R.mipmap.ic_attach_photo));
        } else {
            mFab.setFloatingActionButtonDrawable(FileUtil.decodeResource(R.mipmap.ic_message));
        }

        mFab.getLayoutParams().height = Constant.DP_72;
        mFab.getLayoutParams().width = Constant.DP_72;

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsGroup) {
                    if (UserManager.getManager().getCurrUserId() == chatInfo.groupChatFull.adminId) {
                        if (mProfilePhotoChoose == null) {
                            mProfilePhotoChoose = new ProfilePhotoChoose(ProfilePhotoChoose.TYPE_CHAT_PHOTO);
                        }
                        mProfilePhotoChoose.showDialog(ProfileActivity.this);
                    } else {
                        onBackPressed();
                    }
                } else {
                    if (!isSubProfile) {
                        onBackPressed();
                    } else {
                        if (!sIsClickOnItemBlocked) {
                            sIsClickOnItemBlocked = true;
                            if (chatInfo.isBot) {
                                if (BotManager.getManager().isEmptyBotInfo((int) chatInfo.tgChatObject.id)) {
                                    UserManager.getManager().getUserFull((int) chatInfo.tgChatObject.id, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            switch (object.getConstructor()) {
                                                case TdApi.UserFull.CONSTRUCTOR: {
                                                    TdApi.UserFull userFull = (TdApi.UserFull) object;
                                                    CachedUser cachedUser = UserManager.getManager().insertUserInCache(userFull);
                                                    BotManager.getManager().initBot(cachedUser, chatInfo);
                                                    openChatActivity(chatInfo);
                                                    break;
                                                }
                                                default: {
                                                    sIsClickOnItemBlocked = false;
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    BotManager.getManager().initBot(UserManager.getManager().getUserByIdWithRequestAsync(chatInfo.tgChatObject.id), chatInfo);
                                    openChatActivity(chatInfo);
                                }
                            } else {
                                openChatActivity(chatInfo);
                            }
                        }
                    }
                }
            }
        });
        mFabMargin = Constant.DP_6;
        mFab.setScaleX(1);
        mFab.setScaleY(1);
        mFab.showButton();

        setSupportActionBar(mToolbarView);
        mToolbarView.setNavigationIcon(R.mipmap.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbarView.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //todo может быть кейс когда иконки не будет И ее нужно будет подгрузить
        TdApi.File file = null;
        int id = 0;
        String subTitle = "";
        switch (chatInfo.tgChatObject.type.getConstructor()) {
            case TdApi.PrivateChatInfo.CONSTRUCTOR:
                TdApi.User user = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user;
                file = user.profilePhoto.small;
                id = user.id;
                if (chatInfo.isBot) {
                    subTitle = AndroidUtil.getResourceString(R.string.bot);
                } else {
                    subTitle = ChatHelper.lastSeenUser(user.status);
                }
                break;
            case TdApi.GroupChatInfo.CONSTRUCTOR:
                TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                file = groupChatInfo.groupChat.photo.small;
                id = groupChatInfo.groupChat.id;
                subTitle = groupChatInfo.groupChat.participantsCount + AndroidUtil.getResourceString(R.string.members_)
                        + chatInfo.groupMembersOnline + AndroidUtil.getResourceString(R.string._online);
                break;
        }

        mTitleView.setText(chatInfo.chatName);
        mSubTitleView.setText(subTitle);
        try {
            mTitleImage.setImageDrawable(IconFactory.createIcon(IconFactory.Type.TITLE, id, chatInfo.initials, file));
        } catch (Exception e) {
            AndroidUtil.setImagePlaceholder(mTitleImage);
        }

        switch (mFor) {
            case BOT: {
                TdApi.User user = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user;
                TdApi.UserTypeBot userTypeBot = (TdApi.UserTypeBot) user.type;
                CachedUser cachedUser = UserManager.getManager().getUserByIdWithRequestAsync(user.id);

                mItems.add(0, new String[]{TextUtil.isBlank(user.username) ? AndroidUtil.getResourceString(R.string.unknown) : "@" + user.username});
                mItems.add(1, new String[]{((TdApi.BotInfoGeneral) cachedUser.botInfo).shareText});
                if (userTypeBot.canJoinGroupChats) {
                    mItems.add(2, new String[]{""});
                } else {
                    mItems.add(2, null);
                }
                mItems.add(3, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                break;
            }
            case GROUP: {
                mItems.add(0, new String[]{""});
                mItems.add(1, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                //список людей
                break;
            }
            case USER:
            default: {
                TdApi.User user = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user;
                CachedUser cachedUser = UserManager.getManager().getUserByIdWithRequestAsync(user.id);
                mItems.add(0, new String[]{TextUtil.isBlank(user.username) ? AndroidUtil.getResourceString(R.string.unknown) : "@" + user.username});
                mItems.add(1, new String[]{TextUtil.isNotBlank(user.phoneNumber) ? ("+" + user.phoneNumber) : AndroidUtil.getResourceString(R.string.unknown)});
                mItems.add(2, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
            }
        }

        SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
        sharedMediaManager.cleanSearchMedia(true, !isSubProfile);
        mProfileAdapter = new ProfileAdapter(this, mFor, mItems);

        mRecyclerView.setAdapter(mProfileAdapter);
        sharedMediaManager.searchMedia(chatInfo.tgChatObject.id, UserManager.getManager().getCurrUserId(), true, !isSubProfile);

        updateUserList();
    }

    private void openChatActivity(final ChatInfo chatInfo) {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isGroup", mIsGroup);
                    bundle.putBoolean("isMuted", ChatHelper.isChatMuted(chatInfo));
                    Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                    intent.putExtras(bundle);

                    ChatManager.getManager().forceClose();
                    ChatManager.getManager().getChat(0, chatInfo.tgChatObject.id, chatInfo, true, true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    ProfileManager.getManager().forceClose();
                    sIsClickOnItemBlocked = false;
                } catch (Exception e) {
                    sIsClickOnItemBlocked = false;
                }
            }
        });
    }

    private void updateUserList() {
        if (mFor == ProfileManager.FOR.GROUP) {
            sIsNeedUpdateUserList = false;
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    ProfileManager profileManager = ProfileManager.getManager();
                    final ChatInfo chatInfo = profileManager.getChatInfo();
                    UserManager userManager = UserManager.getManager();
                    final List<Contact> tempContactList = new ArrayList<Contact>(chatInfo.groupChatFull.participants.length);
                    ContactListManager contactListManager = ContactListManager.getManager();
                    contactListManager.getIgnoreUsers().clear();
                    for (int i = 0; i < chatInfo.groupChatFull.participants.length; i++) {
                        TdApi.ChatParticipant chatParticipant = chatInfo.groupChatFull.participants[i];

                        //todo проверить что до этого я вставил юзеров в кеш
                        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(chatParticipant.user.id);

                        Contact contact;
                        if (cachedUser.tgUser.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR) {
                            contact = new Contact(cachedUser, AndroidUtil.getResourceString(R.string.has_no_access_to_messages));
                        } else {
                            contact = new Contact(cachedUser, ChatHelper.lastSeenUser(cachedUser.tgUser.status));
                        }
                        contact.chatParticipant = chatParticipant;
                        contact.sortByJoinDate = true;

                        ContactView.profileMeasure(contact);
                        tempContactList.add(contact);
                        contactListManager.getIgnoreUsers().add(contact.cachedUser.tgUser.id);
                    }

                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            mItems.clear();
                            mItems.add(0, new String[]{""});
                            mItems.add(1, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                            mItems.addAll(tempContactList);
                            mProfileAdapter.notifyDataSetChanged();
                        }
                    });

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        MenuItem menuNotification = menu.getItem(0);
        if (!mIsMuted) {
            menuNotification.setIcon(R.mipmap.ic_notifications_on);
        } else {
            menuNotification.setIcon(R.mipmap.ic_notifications_off);
        }
        ProfileManager profileManager = ProfileManager.getManager();
        ChatInfo chatInfo = profileManager.getChatInfo();
        UserManager userManager = UserManager.getManager();

        if (mFor == ProfileManager.FOR.USER || mFor == ProfileManager.FOR.BOT) {
            for (int i = 1; i <= 4; i++) {
                MenuItem mainMenu = menu.getItem(i);
                if (i == 3 || i == 4) {
                    try {
                        int userId = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id;
                        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                        if (cachedUser.tgUser.myLink.getConstructor() == TdApi.LinkStateContact.CONSTRUCTOR) {
                            mainMenu.setVisible(true);
                        } else {
                            mainMenu.setVisible(false);
                        }
                    } catch (Exception e) {
                        mainMenu.setVisible(false);
                    }
                } else {
                    mainMenu.setVisible(true);
                }

                if (i == 2) {
                    try {
                        int userId = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id;
                        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                        if (cachedUser.isBlocked) {
                            mainMenu.setTitle(AndroidUtil.getResourceString(R.string.unblock));
                        } else {
                            mainMenu.setTitle(AndroidUtil.getResourceString(R.string.block));
                        }
                    } catch (Exception e) {
                        //
                    }
                }
                if (i == 1) {
                    try {
                        int userId = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id;
                        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                        if (cachedUser.tgUser.myLink.getConstructor() == TdApi.LinkStateContact.CONSTRUCTOR) {
                            mainMenu.setVisible(true);
                        } else {
                            mainMenu.setVisible(false);
                        }
                    } catch (Exception e) {
                        //
                    }
                }
            }
            for (int i = 5; i <= 7; i++) {
                MenuItem mainMenu = menu.getItem(i);
                mainMenu.setVisible(false);
            }
        } else {
            for (int i = 1; i <= 4; i++) {
                MenuItem mainMenu = menu.getItem(i);
                mainMenu.setVisible(false);
            }
            for (int i = 5; i <= 7; i++) {
                MenuItem mainMenu = menu.getItem(i);
                mainMenu.setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        ContactListManager contactListManager = ContactListManager.getManager();
        ProfileManager profileManager = ProfileManager.getManager();
        final UserManager userManager = UserManager.getManager();
        final ChatInfo chatInfo = profileManager.getChatInfo();
        long chatId = profileManager.getChatInfo().tgChatObject.id;
        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.action_notification_enabled:
                NotificationManager.getManager().setMuteForChat(NotificationManager.UNMUTE, chatId);
                break;
            case R.id.action_notification_mute_one_hour:
                NotificationManager.getManager().setMuteForChat(NotificationManager.MUTE_FOR_HOUR, chatId);
                break;
            case R.id.action_notification_mute_8_hours:
                NotificationManager.getManager().setMuteForChat(NotificationManager.MUTE_FOR_8_HOURS, chatId);
                break;
            case R.id.action_notification_mute_2_days:
                NotificationManager.getManager().setMuteForChat(NotificationManager.MUTE_FOR_2_DAYS, chatId);
                break;
            case R.id.action_notification_disable:
                NotificationManager.getManager().setMuteForChat(NotificationManager.MUTE_DISABLE, chatId);
                break;

            case R.id.action_add_member:
                contactListManager.setAction(ContactListManager.ACTION_ADD_USER_TO_GROUP);
                Intent intent = new Intent(this, ContactListActivity.class);
                this.startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                break;

            case R.id.action_edit_name:
                bundle.putInt("type", EditNameActivity.FOR_GROUP);
                bundle.putString("firstName", profileManager.getChatInfo().chatName.toString());
                Intent intentEditName = new Intent(this, EditNameActivity.class);
                intentEditName.putExtras(bundle);
                this.startActivity(intentEditName);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                break;

            case R.id.action_delete_and_leave:
                final ChatManager chatManager = ChatManager.getManager();
                chatManager.deleteAndLeaveSelf(new ResultController() {
                    @Override
                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                        chatManager.cleanHistory();
                        ProfileActivity.this.onBackPressed();
                    }
                });
                break;

            case R.id.action_share:
                try {
                    //info открыт только если есть в контактах!!!
                    bundle.putSerializable("typeList", IntermediateActivity.TypeList.ALL);
                    bundle.putSerializable("action", IntermediateActivity.Action.SHARED_CONTACT);
                    bundle.putInt("userId", ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id);
                    Intent intermediateIntent = new Intent(this, IntermediateActivity.class);
                    intermediateIntent.putExtras(bundle);

                    startActivity(intermediateIntent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                } catch (Exception e) {
                    Log.e(LOG, "action_share", e);
                    Crashlytics.logException(e);
                }

                break;

            case R.id.action_block:
                try {
                    int userId = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id;
                    final CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                    if (cachedUser.isBlocked) {
                        userManager.unBlockUser(userId, new ResultController() {
                            @Override
                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                                    cachedUser.isBlocked = false;
                                    ProfileActivity.this.supportInvalidateOptionsMenu();
                                }
                            }
                        });
                    } else {
                        userManager.blockUser(userId, new ResultController() {
                            @Override
                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {
                                    cachedUser.isBlocked = true;
                                    ProfileActivity.this.supportInvalidateOptionsMenu();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    //
                }
                break;

            case R.id.action_edit:
                try {
                    int userId = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id;
                    final CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                    bundle.putInt("type", EditNameActivity.FOR_PROFILE_USER);
                    bundle.putString("firstName", cachedUser.tgUser.firstName);
                    bundle.putString("secondName", cachedUser.tgUser.lastName);
                    bundle.putInt("userId", cachedUser.tgUser.id);
                    bundle.putString("phone", cachedUser.tgUser.phoneNumber);
                    bundle.putString("initials", cachedUser.initials);
                    bundle.putSerializable("file", cachedUser.tgUser.profilePhoto.small);

                    Intent intentEditContact = new Intent(this, EditNameActivity.class);
                    intentEditContact.putExtras(bundle);
                    this.startActivity(intentEditContact);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    Log.e(LOG, "action_edit", e);
                }
                break;

            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.are_you_sure_to_delete_contact)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int[] userId = new int[]{((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user.id};
                                    userManager.deleteContacts(userId, new ResultController() {
                                        @Override
                                        public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                            if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {

                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    //
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                break;
        }
        return true;
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

        if (fabTranslationY <= (minFabTranslationY)) {
            if (mIsFullWidth == null || mIsFullWidth) {
                mIsFullWidth = false;
                ((RelativeLayout.LayoutParams) mTitleView.getLayoutParams()).rightMargin = Constant.DP_100;
                mTitleView.requestLayout();
                mTitleView.invalidate();
            }
            mFab.hideButtonAnimated();
        } else {
            if (mIsFullWidth == null || !mIsFullWidth) {
                mIsFullWidth = true;
                ((RelativeLayout.LayoutParams) mTitleView.getLayoutParams()).rightMargin = Constant.DP_36;
                mTitleView.requestLayout();
                mTitleView.invalidate();
            }
            mFab.showButtonAnimated();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mProfilePhotoChoose != null)
            mProfilePhotoChoose.processImage(requestCode, resultCode, data, this);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            final ChatInfo chatInfo = ProfileManager.getManager().getChatInfo();
            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_SHARED_MEDIA_LIST:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mProfileAdapter != null) {

                                switch (mFor) {
                                    case BOT: {
                                        mItems.set(3, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                                        break;
                                    }
                                    case GROUP: {
                                        mItems.set(1, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                                        break;
                                    }
                                    case USER:
                                    default: {
                                        mItems.set(2, new String[]{SharedMediaManager.getManager().getPhotoAndVideoMessagesStringSize(!isSubProfile)});
                                    }
                                }

                                mProfileAdapter.notifyDataSetChanged();
                                if (mProfileAdapter.getSharedMediaAdapter() != null) {
                                    mProfileAdapter.getSharedMediaAdapter().notifyDataSetChanged();
                                }
                            }

                        }
                    });
                    break;
                case NotificationObject.UPDATE_MEMBER_COUNT:
                    final TdApi.UpdateChatParticipantsCount chatParticipantsCount = (TdApi.UpdateChatParticipantsCount) nObject.getWhat();
                    if (chatInfo != null && chatInfo.isGroupChat) {
                        TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                        if (groupChatInfo.groupChat.id == chatParticipantsCount.chatId) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    updateUserList();
                                    final String str = chatParticipantsCount.participantsCount
                                            + AndroidUtil.getResourceString(R.string.members_)
                                            + chatInfo.groupMembersOnline + AndroidUtil.getResourceString(R.string._online);
                                    mSubTitleView.setText(str);
                                }
                            });
                        }
                    }
                    break;

                case NotificationObject.FORCE_CLOSE_CHAT: {
                    forceFinish();
                    break;
                }
                case NotificationObject.CHANGE_CHAT_MUTE_STATUS:
                    Object[] arrMute = (Object[]) nObject.getWhat();
                    final long chatId = (long) arrMute[0];
                    final int muteFot = (int) arrMute[1];
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (ProfileManager.getManager().isSameChatId(chatId)) {
                                if (mIsMuted != null) {
                                    mIsMuted = muteFot > 0;
                                    supportInvalidateOptionsMenu();
                                }
                            }
                        }
                    });
                    break;
                case NotificationObject.UPDATE_CHAT_PHOTO:
                    final TdApi.UpdateChatPhoto updateChatPhoto = (TdApi.UpdateChatPhoto) nObject.getWhat();
                    if (ProfileManager.getManager().isSameChatId(updateChatPhoto.chatId)) {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final IconDrawable dr = IconFactory.createBitmapIcon(IconFactory.Type.TITLE, updateChatPhoto.photo.small.path);
                                    mTitleImage.setImageDrawable(dr);
                                } catch (Exception e) {
                                    //
                                }
                            }
                        });
                    }
                    break;
                case NotificationObject.UPDATE_CHAT_TITLE:
                    final TdApi.UpdateChatTitle updateChatTitle = (TdApi.UpdateChatTitle) nObject.getWhat();
                    if (ProfileManager.getManager().isSameChatId(updateChatTitle.chatId)) {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mTitleView.setText(updateChatTitle.title);
                                    TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                                    TdApi.GroupChat groupChat = groupChatInfo.groupChat;
                                    final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, groupChat.id, chatInfo.initials, groupChat.photo.small);
                                    mTitleImage.setImageDrawable(dr);
                                } catch (Exception e) {
                                    //
                                }
                            }
                        });
                    }
                    break;
                case NotificationObject.UPDATE_CHAT_TITLE_USER:
                    final CachedUser cachedUserForTitle = (CachedUser) nObject.getWhat();
                    ProfileManager profileManager = ProfileManager.getManager();

                    if (isSubProfile) {
                        if (profileManager.isSameChatId(cachedUserForTitle.tgUser.id)) {
                            ProfileActivity.sIsNeedUpdateUserList = true;
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mTitleView.setText(cachedUserForTitle.fullName);
                                        IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, cachedUserForTitle.tgUser.id,
                                                cachedUserForTitle.initials, cachedUserForTitle.tgUser.profilePhoto.small);
                                        mTitleImage.setImageDrawable(dr);
                                        ProfileActivity.this.supportInvalidateOptionsMenu();

                                        if (mFor == ProfileManager.FOR.USER && mItems != null && mItems.size() > 1 && mProfileAdapter != null) {
                                            mItems.set(1, new String[]{TextUtil.isNotBlank(cachedUserForTitle.tgUser.phoneNumber) ?
                                                    ("+" + cachedUserForTitle.tgUser.phoneNumber) : AndroidUtil.getResourceString(R.string.unknown)});
                                            mProfileAdapter.notifyDataSetChanged();
                                        }
                                    } catch (Throwable e) {
                                        //
                                    }
                                }
                            });
                        }
                    } else {
                        if (profileManager.getOldChatInfo() == null && profileManager.isSameChatId(cachedUserForTitle.tgUser.id)) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mTitleView.setText(cachedUserForTitle.fullName);
                                        IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, cachedUserForTitle.tgUser.id,
                                                cachedUserForTitle.initials, cachedUserForTitle.tgUser.profilePhoto.small);
                                        mTitleImage.setImageDrawable(dr);
                                        ProfileActivity.this.supportInvalidateOptionsMenu();

                                        if (mFor == ProfileManager.FOR.USER && mItems != null && mItems.size() > 1 && mProfileAdapter != null) {
                                            mItems.set(1, new String[]{TextUtil.isNotBlank(cachedUserForTitle.tgUser.phoneNumber) ?
                                                    ("+" + cachedUserForTitle.tgUser.phoneNumber) : AndroidUtil.getResourceString(R.string.unknown)});
                                            mProfileAdapter.notifyDataSetChanged();
                                        }
                                    } catch (Throwable e) {
                                        //
                                    }
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }
}
