package com.stayfprod.utter.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.NotificationManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.StickerRecentManager;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.adapter.BotCommandsAdapter;
import com.stayfprod.utter.ui.adapter.StickerMicroThumbAdapterImpl;
import com.stayfprod.utter.ui.component.BotKeyboardPopup;
import com.stayfprod.utter.ui.component.MusicBarWidget;
import com.stayfprod.utter.ui.listener.AfterTextChangedListener;
import com.stayfprod.utter.ui.adapter.GalleryAdapter;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.manager.ChatListManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.StickerManager;
import com.stayfprod.utter.manager.UpdateHandler;
import com.stayfprod.utter.service.VoiceController;
import com.stayfprod.utter.ui.view.RecordVoiceView;
import com.stayfprod.utter.ui.view.SimpleRecyclerView;
import com.stayfprod.utter.manager.CameraManager;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.ui.view.StickerGridView;
import com.stayfprod.emojicon.EmojiconEditText;
import com.stayfprod.emojicon.EmojiconGridView;
import com.stayfprod.emojicon.EmojIconsPopup;
import com.stayfprod.emojicon.emoji.Emojicon;
import com.stayfprod.utter.manager.WebpSupportManager;
import com.stayfprod.utter.util.TextUtil;

import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;

public class ChatActivity extends AbstractActivity implements Observer {

    private static final String LOG = ChatActivity.class.getSimpleName();

    private static final int SELECT_PHOTO = 100;
    public static final int OPEN_PHOTO = 55;

    private View mPopupAttachView;
    private View mPopupMuteView;
    private RecyclerView mRecyclerViewGallery;
    private EmojiconEditText mEmojiconEditText;
    private GalleryAdapter mGalleryListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    private LinearLayout mTakePhotoButton;
    private String mPhotoPath;
    private EmojIconsPopup mPpopupEmoji;
    private BotKeyboardPopup mPopupBotKeyboard;
    private PopupWindow mPopupAttach;
    private PopupWindow mPopupMute;
    private RelativeLayout mMainLayout;

    private Boolean mIsGroup;
    private Boolean mIsLeave;
    private Boolean mIsMuted = false;
    private Boolean mIsFirstBotOpening;

    private SimpleRecyclerView mBotCommandListView;
    private RelativeLayout mBotPopupCommandsLayout;
    private RelativeLayout mBotPopupCommandsUpLayout;
    private boolean mIsPopUpBotCommandsVisible;

    private ImageView mChatIcon;
    private TextView mChatTitle;
    private TextView mChatSubtext;
    private TextView mChatNoMsges;
    private ImageView mIcMute;

    private ImageView mChatIcSlash;
    private ImageView mChatIcCommand;
    private ImageView mChatIcPanelKb;
    private ImageView mSendButton;
    private RecordVoiceView mChatRecord;
    private RelativeLayout mChatInputHolder;
    private TextView mSlideToCancelTimer;
    private ImageView mEmojiButton;
    private Menu mMenu;
    private MusicBarWidget mMusicBarWidget;

    private volatile boolean mIsClickedOnToolbar;

    @Override
    protected void onStart() {
        super.onStart();
        WebpSupportManager.getManager().registerReceiver(this);
        CameraManager.getManager().addObserver(this);
        ChatManager chatManager = ChatManager.getManager();
        chatManager.addObserver(this);
        UpdateHandler.getHandler().addObserver(this);
        NotificationManager.getManager().addObserver(this);
        BotManager.getManager().addObserver(this);
        VoiceController.getController().addObserver(this);
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        audioPlayer.addObserver(this);
        mMusicBarWidget.checkOnStart();
        if (audioPlayer.isNeedUpdateChatActivity()) {
            audioPlayer.setIsNeedUpdateMessageList(false);
            chatManager.notifySetDataChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //info было в onPause
        try {
            WebpSupportManager.getManager().unregisterReceiver(this);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        VoiceController.getController().deleteObserver(this);
        AudioPlayer.getPlayer().deleteObserver(this);
    }

    @Override
    protected void onDestroy() {
        CameraManager.getManager().deleteObserver(this);
        ChatManager.getManager().deleteObserver(this);
        UpdateHandler.getHandler().deleteObserver(this);
        NotificationManager.getManager().deleteObserver(this);
        BotManager.getManager().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getPopupEmoji().setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());
        mPopupBotKeyboard.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());
    }

    private void dismissPopUpAttachWithAnimation() {
        if (mPopupAttach.isShowing()) {
            ObjectAnimator anim = ObjectAnimator.ofObject(mPopupAttachView,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    0x33000000,
                    0x00000000);
            anim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPopupAttach.dismiss();
                }
            });
            anim.setDuration(200).start();
        } else {
            mPopupAttach.dismiss();
        }
    }

    private void dismissPopUpBotCommandsWithAnimation() {
        if (mIsPopUpBotCommandsVisible) {
            mIsPopUpBotCommandsVisible = false;
            ObjectAnimator popupAnim = ObjectAnimator.ofObject(mBotPopupCommandsUpLayout,
                    "backgroundColor", new ArgbEvaluator(),
                    0x33000000, 0x00000000);
            popupAnim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBotPopupCommandsLayout, "translationY", 0, AndroidUtil.dp(124));
                    objectAnimator.addListener(new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mBotPopupCommandsLayout.setVisibility(View.GONE);
                            mBotPopupCommandsUpLayout.setVisibility(View.GONE);
                        }
                    });

                    objectAnimator.setDuration(150).start();
                }
            });
            popupAnim.setDuration(100).start();
        }
    }

    private void dismissPopUpMuteWithAnimation() {
        if (mPopupMute != null) {
            if (mPopupMute.isShowing()) {
                ObjectAnimator anim = ObjectAnimator.ofObject(mPopupMuteView,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        0x33000000,
                        0x00000000);
                anim.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mPopupMute.dismiss();
                    }
                });
                anim.setDuration(200).start();
            } else {
                mPopupMute.dismiss();
            }
        }
    }

    private void showPopUpBotCommands() {
        if (!mIsPopUpBotCommandsVisible) {
            mIsPopUpBotCommandsVisible = true;
            mBotPopupCommandsLayout.setVisibility(View.VISIBLE);
            mBotPopupCommandsUpLayout.setVisibility(View.VISIBLE);
            ObjectAnimator popupAnim = ObjectAnimator.ofFloat(mBotPopupCommandsLayout, "translationY", AndroidUtil.dp(124), 0);
            popupAnim.setDuration(150);
            popupAnim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ObjectAnimator.ofObject(mBotPopupCommandsUpLayout,
                            "backgroundColor", new ArgbEvaluator(),
                            0x00000000, 0x33000000).setDuration(100).start();

                }
            });
            popupAnim.start();
        }
    }

    @Override
    public void onBackPressed() {
        ChatManager.getManager().stopScroll();

        if (mPopupMute != null && mPopupMute.isShowing()) {
            dismissPopUpMuteWithAnimation();
            return;
        }

        if (mPopupAttach.isShowing()) {
            dismissPopUpAttachWithAnimation();
            return;
        }

        if (mPpopupEmoji != null && getPopupEmoji().isKeyBoardOpen()) {
            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mEmojiconEditText.getWindowToken(), 0);
            return;
        }

        if (mPpopupEmoji != null && getPopupEmoji().isShowing()) {
            getPopupEmoji().dismiss();
            mMainLayout.setPadding(0, 0, 0, 0);
            return;
        }

        if (mPopupBotKeyboard.isShowing()) {
            updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
            mPopupBotKeyboard.dismiss();
            mMainLayout.setPadding(0, 0, 0, 0);
            return;
        }

        if (mPpopupEmoji != null)
            getPopupEmoji().dismiss();
        mPopupBotKeyboard.dismiss();
        dismissPopUpAttachWithAnimation();
        dismissPopUpMuteWithAnimation();

        ChatManager.getManager().deleteObserver(this);
        UpdateHandler.getHandler().deleteObserver(this);
        NotificationManager.getManager().deleteObserver(this);
        CameraManager.getManager().deleteObserver(this);
        BotManager.getManager().deleteObserver(this);

        /*Intent intent = new Intent(context, ChatListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivity(intent);*/
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);


        final Long chatId = ChatManager.getCurrentChatId();

        ChatManager.getManager().clean();
        FileManager.getManager().cleanTempStorage();
        VoiceController.getController().fullDestroy();
        BotManager.getManager().clean();

        StickerManager stickerManager = StickerManager.getManager();
        stickerManager.cleanStickerViews();

        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (ChatManager.sIsNeedRemoveChat) {
                    ChatManager.sIsNeedRemoveChat = false;
                    ChatListManager.getManager().removeChat(chatId);
                    System.gc();
                }
                StickerManager stickerManager = StickerManager.getManager();
                if (stickerManager.isNeedRebuild()) {
                    stickerManager.getStickers(true);
                }
            }
        }, 200);
    }

    public void forceFinish() {

        if (mPpopupEmoji != null && getPopupEmoji() != null)
            getPopupEmoji().dismiss();

        if (mPopupBotKeyboard != null)
            mPopupBotKeyboard.dismiss();

        if (mPopupAttach != null)
            mPopupAttach.dismiss();

        if (mPopupMute != null)
            mPopupMute.dismiss();

        ChatManager.getManager().deleteObserver(this);
        UpdateHandler.getHandler().deleteObserver(this);
        NotificationManager.getManager().deleteObserver(this);
        BotManager.getManager().deleteObserver(this);
        finish();
        final Long chatId = ChatManager.getCurrentChatId();

        ChatManager.getManager().clean();
        FileManager.getManager().cleanTempStorage();
        VoiceController.getController().fullDestroy();
        //BotManager.getManager().clean();

        if (ChatManager.sIsNeedRemoveChat) {
            ChatManager.sIsNeedRemoveChat = false;
            ChatListManager.getManager().removeChat(chatId);
        }

        StickerManager stickerManager = StickerManager.getManager();
        stickerManager.cleanStickerViews();
    }

    public EmojIconsPopup getPopupEmoji() {
        if (mPpopupEmoji == null) {

            mPpopupEmoji = new EmojIconsPopup(mMainLayout, this, new StickerGridView(this), new StickerMicroThumbAdapterImpl(this));

            mPpopupEmoji.setSizeForSoftKeyboard();
            mPpopupEmoji.setOnEmojiconBackspaceClickedListener(new EmojIconsPopup.OnEmojiconBackspaceClickedListener() {
                @Override
                public void onEmojiconBackspaceClicked(View v) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    mEmojiconEditText.dispatchKeyEvent(event);
                }
            });

            mPpopupEmoji.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    changeEmojiKeyboardIcon(mEmojiButton, R.mipmap.ic_smiles);
                    StickerRecentManager.getInstance().saveRecents();
                }
            });

            mPpopupEmoji.setOnSoftKeyboardOpenCloseListener(new EmojIconsPopup.OnSoftKeyboardOpenCloseListener() {
                @Override
                public void onKeyboardOpen(int keyBoardHeight) {
                    AndroidUtil.setKeyboardHeight(keyBoardHeight);
                }

                @Override
                public void onKeyboardClose() {
                    if (mPpopupEmoji.isShowing())
                        mPpopupEmoji.dismiss();
                }
            });
            mPpopupEmoji.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {
                @Override
                public void onEmojiconClicked(Emojicon emojicon) {
                    int selectionCursor = mEmojiconEditText.getSelectionStart();
                    mEmojiconEditText.getText().insert(selectionCursor, emojicon.getEmoji());
                }
            });

            mPpopupEmoji.setAnimationStyle(R.style.popup_anim_style);
            mPopupAttach.setAnimationStyle(R.style.popup_anim_style);

            mPpopupEmoji.setOnEmojiconBackspaceClickedListener(new EmojIconsPopup.OnEmojiconBackspaceClickedListener() {
                @Override
                public void onEmojiconBackspaceClicked(View v) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    mEmojiconEditText.dispatchKeyEvent(event);
                }
            });

            mPpopupEmoji.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());

        }
        return mPpopupEmoji;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_history_chat:
                ChatManager.getManager().deleteChatHistory();
                break;
            case R.id.action_clear_leave_group:
                ChatManager.getManager().deleteChatParticipant();
                break;
            case R.id.action_mute:
                if (mPopupMute == null) {
                    mPopupMuteView = LayoutInflater.from(this).inflate(R.layout.popup_mute, mMainLayout, false);
                    mPopupMute = new PopupWindow(mPopupMuteView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    mPopupMute.setContentView(mPopupMuteView);
                    mPopupMute.setAnimationStyle(R.style.popup_anim_style);

                    mPopupMuteView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismissPopUpMuteWithAnimation();
                        }
                    });

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Long chatId = ChatManager.getCurrentChatId();
                            NotificationManager.getManager().setMuteForChat((int) v.getTag(), chatId);
                            dismissPopUpMuteWithAnimation();
                            //isMuted = !isMuted;
                            supportInvalidateOptionsMenu();
                        }
                    };

                    View p_mute_hour_layout = mPopupMuteView.findViewById(R.id.p_mute_hour_layout);
                    View p_mute_8_hours_layout = mPopupMuteView.findViewById(R.id.p_mute_8_hours_layout);
                    View p_mute_2_days_layout = mPopupMuteView.findViewById(R.id.p_mute_2_days_layout);
                    View p_mute_disable_layout = mPopupMuteView.findViewById(R.id.p_mute_disable_layout);

                    p_mute_hour_layout.setOnClickListener(onClickListener);
                    p_mute_8_hours_layout.setOnClickListener(onClickListener);
                    p_mute_2_days_layout.setOnClickListener(onClickListener);
                    p_mute_disable_layout.setOnClickListener(onClickListener);

                    p_mute_hour_layout.setTag(NotificationManager.MUTE_FOR_HOUR);
                    p_mute_8_hours_layout.setTag(NotificationManager.MUTE_FOR_8_HOURS);
                    p_mute_2_days_layout.setTag(NotificationManager.MUTE_FOR_2_DAYS);
                    p_mute_disable_layout.setTag(NotificationManager.MUTE_DISABLE);
                }

                if (mIsMuted) {
                    Long chatId = ChatManager.getCurrentChatId();
                    NotificationManager.getManager().setMuteForChat(NotificationManager.UNMUTE, chatId);
                } else {
                    if (!mPopupMute.isShowing()) {
                        mPopupMute.showAtLocation(mPopupMuteView, Gravity.CENTER, 0, 0);
                        ObjectAnimator.ofObject(mPopupMuteView,
                                "backgroundColor",
                                new ArgbEvaluator(),
                                0x00000000,
                                0x33000000).setDuration(1100).start();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openProfileActivity() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isGroup", mIsGroup);
                bundle.putBoolean("isMuted", mIsMuted);

                ProfileManager.getManager().setChatInfo(ChatManager.getCurrentChatInfo());

                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtras(bundle);

                ChatActivity.this.startActivity(intent);
                ChatActivity.this.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            mIsGroup = bundle.getBoolean("isGroup");
            mIsLeave = bundle.getBoolean("isLeave");
            mIsMuted = bundle.getBoolean("isMuted");
            if (mIsMuted == null) {
                mIsMuted = false;
            }
            mIsFirstBotOpening = bundle.getBoolean("isFirstBotOpening");
        }

        setContentView(R.layout.activity_chat);
        FileManager.sCanDownloadFile = true;
        setToolbar();
        mMainLayout = (RelativeLayout) findViewById(R.id.activity_chat_layout);
        mPopupAttachView = LayoutInflater.from(this).inflate(R.layout.popup_attach, mMainLayout, false);

        mTakePhotoButton = (LinearLayout) mPopupAttachView.findViewById(R.id.p_open_camera);

        RelativeLayout t_layout_main = findView(R.id.t_layout_main);
        t_layout_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsGroup) {
                    if (!mIsClickedOnToolbar) {
                        try {
                            mIsClickedOnToolbar = true;
                            UserManager userManager = UserManager.getManager();
                            int userId = ((TdApi.PrivateChatInfo) ChatManager.getCurrentChatInfo().tgChatObject.type).user.id;
                            CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                            if (cachedUser.isHaveFullInfo) {
                                openProfileActivity();
                                mIsClickedOnToolbar = false;
                            } else {
                                userManager.getUserFull(userId, new ResultController() {
                                    @Override
                                    public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                        switch (object.getConstructor()) {
                                            case TdApi.UserFull.CONSTRUCTOR: {
                                                TdApi.UserFull userFull = (TdApi.UserFull) object;
                                                CachedUser cachedUser = UserManager.getManager().insertUserInCache(userFull);
                                                openProfileActivity();
                                                break;
                                            }
                                        }
                                        mIsClickedOnToolbar = false;
                                    }
                                });
                            }
                        } catch (Exception e) {
                            mIsClickedOnToolbar = false;
                        }
                    }
                } else {
                    if (mIsLeave == null || !mIsLeave) {
                        openProfileActivity();
                    }
                }
            }
        });


        final LinearLayout openGallerySendImages = (LinearLayout) mPopupAttachView.findViewById(R.id.p_open_gallery_send_images);
        final TextView chooseFromGalleryText = (TextView) openGallerySendImages.findViewById(R.id.choose_from_gallery_text);

        final TextView takePhotoText = (TextView) mPopupAttachView.findViewById(R.id.take_photo_text);
        chooseFromGalleryText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        chooseFromGalleryText.setTextSize(16);
        chooseFromGalleryText.setTextColor(0xFF222222);

        takePhotoText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        takePhotoText.setTextSize(16);
        takePhotoText.setTextColor(0xFF222222);

        RelativeLayout openSubLayout = (RelativeLayout) mPopupAttachView.findViewById(R.id.p_open_sub_layout);
        openSubLayout.getLayoutParams().height = AndroidUtil.getKeyboardHeight();

        RelativeLayout p_open_main_layout = (RelativeLayout) mPopupAttachView.findViewById(R.id.p_open_main_layout);
        p_open_main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopUpAttachWithAnimation();
            }
        });

        openGallerySendImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SparseArray<GalleryAdapter.StorageObject> selectedItems = mGalleryListAdapter.getSelectedItems();
                if (selectedItems.size() > 0) {
                    dismissPopUpAttachWithAnimation();
                    ThreadService.runTaskBackground(new Runnable() {
                        @Override
                        public void run() {
                            int[] keys = new int[selectedItems.size()];
                            for (int i = 0; i < selectedItems.size(); i++) {
                                int key = selectedItems.keyAt(i);
                                GalleryAdapter.StorageObject object = selectedItems.get(key);
                                ChatManager manager = ChatManager.getManager();
                                manager.sendMessage(manager.createPhotoMsg(object.path));
                                keys[i] = key;
                            }

                            for (int i = 0; i < keys.length; i++) {
                                GalleryAdapter.StorageObject object = selectedItems.get(keys[i]);
                                final int pos = object.pos;
                                selectedItems.remove(keys[i]);
                            }
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    mGalleryListAdapter.notifyDataSetChanged();
                                    chooseFromGalleryText.setText(AndroidUtil.getResourceString(R.string.choose_from_gallery));
                                }
                            });
                        }
                    });
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            }
        });

        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPhotoPath = CameraManager.getManager().dispatchTakePictureIntent(ChatActivity.this);
                } catch (Exception e) {
                    AndroidUtil.showToastLong(e.getMessage() + "");
                }
            }
        });

        mRecyclerViewGallery = (RecyclerView) mPopupAttachView.findViewById(R.id.p_list_galery);
        mGalleryListAdapter = new GalleryAdapter(this, mRecyclerViewGallery);
        mChatListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mPopupAttach = new PopupWindow(mPopupAttachView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mPopupAttach.setContentView(mPopupAttachView);

        mRecyclerViewGallery.setLayoutManager(mChatListLayoutManager);

        mRecyclerViewGallery.setAdapter(mGalleryListAdapter);

        ChatManager chatManager = ChatManager.getManager();
        chatManager.initRecycleView(this, mIsLeave);

        ChatManager.getManager().getChat(true);

        mChatNoMsges = findView(R.id.a_chat_no_msges);
        mChatNoMsges.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChatNoMsges.setTextSize(16);
        mChatNoMsges.setTextColor(0xff999999);

        mEmojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        mEmojiconEditText.setLongClickable(false);

        mEmojiconEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        mChatInputHolder = (RelativeLayout) findViewById(R.id.a_chat_input_holder);
        mChatRecord = (RecordVoiceView) findViewById(R.id.a_chat_record);
        mChatRecord.setTag(mChatRecord.getVisibility());

        if (mIsLeave != null && mIsLeave) {
            mChatInputHolder.setVisibility(View.GONE);
            mChatRecord.setVisibility(View.GONE);
        }

        AndroidUtil.setEditTextTypeface(mEmojiconEditText);

        mEmojiButton = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView attachButton = (ImageView) findViewById(R.id.a_chat_attach);
        mSendButton = (ImageView) findViewById(R.id.a_chat_send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv = (ImageView) v;
                iv.setVisibility(View.GONE);
                attachButton.setVisibility(View.VISIBLE);
                String msg = mEmojiconEditText.getText().toString();
                mEmojiconEditText.setText("");
                ChatManager manager = ChatManager.getManager();

                if (TextUtil.isNotBlank(msg)) {
                    manager.sendMessage(manager.createTextMsg(msg));
                }

            }
        });

        //ChatManager.getManager().readChatHistory();

        mChatIcSlash = (ImageView) findViewById(R.id.a_chat_ic_slash);
        mChatIcCommand = findView(R.id.a_chat_ic_comand);
        mChatIcPanelKb = findView(R.id.a_chat_ic_panel_kb);

        mChatIcSlash.setTag(mChatIcSlash.getVisibility());
        mChatIcCommand.setTag(mChatIcCommand.getVisibility());
        mChatIcPanelKb.setTag(mChatIcPanelKb.getVisibility());

        mChatIcPanelKb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                mPopupBotKeyboard.dismiss();
                mMainLayout.setPadding(0, 0, 0, 0);
                mEmojiconEditText.setFocusableInTouchMode(true);
                mEmojiconEditText.requestFocus();
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mEmojiconEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        mEmojiconEditText.addTextChangedListener(new AfterTextChangedListener() {

            private boolean isFirst = true;

            @SuppressWarnings("ResourceType")
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (isFirst) {
                        isFirst = false;

                        mSendButton.setVisibility(View.VISIBLE);
                        attachButton.setVisibility(View.GONE);

                        mChatRecord.setTag(mChatRecord.getVisibility());
                        mChatRecord.setVisibility(View.GONE);

                        mChatIcPanelKb.setTag(mChatIcPanelKb.getVisibility());
                        mChatIcPanelKb.setVisibility(View.GONE);

                        mChatIcCommand.setTag(mChatIcCommand.getVisibility());
                        mChatIcCommand.setVisibility(View.GONE);

                        mChatIcSlash.setTag(mChatIcSlash.getVisibility());
                        mChatIcSlash.setVisibility(View.GONE);
                    }

                    BotManager.getManager().findCommand(s.toString());
                } else {
                    isFirst = true;
                    dismissPopUpBotCommandsWithAnimation();

                    mSendButton.setVisibility(View.GONE);
                    attachButton.setVisibility(View.VISIBLE);

                    mChatRecord.setVisibility((int) mChatRecord.getTag());
                    mChatIcPanelKb.setVisibility((int) mChatIcPanelKb.getTag());
                    mChatIcCommand.setVisibility((int) mChatIcCommand.getTag());
                    mChatIcSlash.setVisibility((int) mChatIcSlash.getTag());
                }
            }
        });

        mEmojiconEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        if (!getPopupEmoji().isKeyBoardOpen()) {
                            getPopupEmoji().dismiss();
                            mMainLayout.setPadding(0, 0, 0, 0);

                            if (mPopupBotKeyboard.isShowing()) {
                                mPopupBotKeyboard.dismiss();
                                updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                            }

                            mEmojiconEditText.setFocusableInTouchMode(true);
                            mEmojiconEditText.requestFocus();
                            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(mEmojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        }
                        return true;
                    }
                }
                return false;
            }
        });


        mPopupBotKeyboard = new BotKeyboardPopup(this, mMainLayout);

        BotManager.getManager().setPopupBotKeyboard(mPopupBotKeyboard, mMainLayout);
        mPopupBotKeyboard.setSizeForSoftKeyboard();
        mPopupBotKeyboard.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());

        mPopupBotKeyboard.setAnimationStyle(R.style.popup_anim_style);
        mPopupBotKeyboard.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //нельзя тут ничего делать!!
            }
        });

        mPopupBotKeyboard.setOnSoftKeyboardOpenCloseListener(new BotKeyboardPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
                AndroidUtil.setKeyboardHeight(keyBoardHeight);
            }

            @Override
            public void onKeyboardClose() {
                if (mPopupBotKeyboard.isShowing())
                    mPopupBotKeyboard.dismiss();
            }
        });

        mChatIcCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPopupBotKeyboard.isShowing()) {
                    ChatManager.getManager().stopScroll();
                    if (mPopupBotKeyboard.isKeyBoardOpen()) {
                        mPopupBotKeyboard.showAtBottom();
                        mMainLayout.setPadding(0, 0, 0, 0);
                    } else {
                        mPopupBotKeyboard.showAtBottomFirstTime();
                        mMainLayout.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
                    }
                }
                mChatIcPanelKb.setVisibility(View.VISIBLE);
                mChatIcCommand.setVisibility(View.GONE);

                if (getPopupEmoji().isShowing()) {
                    getPopupEmoji().dismiss();
                }
            }
        });


        mEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPopupEmoji().isShowing()) {
                    if (mPopupBotKeyboard.isShowing()) {
                        mPopupBotKeyboard.dismiss();
                        updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                    }
                    ChatManager.getManager().stopScroll();
                    if (getPopupEmoji().isKeyBoardOpen()) {
                        getPopupEmoji().showAtBottom();
                        mMainLayout.setPadding(0, 0, 0, 0);
                        changeEmojiKeyboardIcon(mEmojiButton, R.mipmap.ic_msg_panel_kb);
                    } else {
                        getPopupEmoji().showAtBottomFirstTime();
                        mMainLayout.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
                        changeEmojiKeyboardIcon(mEmojiButton, R.mipmap.ic_msg_panel_kb);
                    }
                } else {
                    getPopupEmoji().dismiss();
                    mMainLayout.setPadding(0, 0, 0, 0);
                    mEmojiconEditText.setFocusableInTouchMode(true);
                    mEmojiconEditText.requestFocus();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mEmojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEmojiconEditText.getWindowToken(), 0);
                if (!mPopupAttach.isShowing()) {
                    mPopupAttach.showAtLocation(mPopupAttachView, Gravity.CENTER, 0, 0);
                    ObjectAnimator.ofObject(mPopupAttachView,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            0x00000000,
                            0x33000000).setDuration(1100).start();

                } else {
                    dismissPopUpAttachWithAnimation();
                }
            }
        });

        final LinearLayout cancelLayout = (LinearLayout) findViewById(R.id.slide_to_cancel_layout);
        final LinearLayout recordVoiceTextLayout = (LinearLayout) findViewById(R.id.slide_to_cancel_text);
        mSlideToCancelTimer = (TextView) findViewById(R.id.slide_to_cancel_timer);

        cancelLayout.setTranslationX(sWindowCurrentWidth);

        TextView recordVoiceText = (TextView) recordVoiceTextLayout.getChildAt(1);
        recordVoiceText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        recordVoiceText.setTextSize(17);
        recordVoiceText.setTextColor(0xFFB3B3B3);

        mSlideToCancelTimer.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mSlideToCancelTimer.setTextSize(17);
        mSlideToCancelTimer.setTextColor(Color.BLACK);

        mChatRecord.setAfterTouchListener(new View.OnTouchListener() {
            float remMajorX = 0;
            float remTempX = 0;
            int cancelDistance = AndroidUtil.dp(100);
            Float remTextTranslationX;
            ObjectAnimator objectAnimatorStart = ObjectAnimator.ofFloat(cancelLayout, "translationX", sWindowCurrentWidth, 0);
            boolean cancelLock;

            public void cancel(final boolean isNeedSend) {
                cancelLock = true;
                ObjectAnimator animLayout = ObjectAnimator.ofFloat(cancelLayout, "translationX", 0, sWindowCurrentWidth);
                ObjectAnimator animButtonX = ObjectAnimator.ofInt(mChatRecord, "dx", mChatRecord.getDx(), 0);
                ObjectAnimator animButtonRadius = ObjectAnimator.ofInt(mChatRecord, "currRadius", mChatRecord.getCurrRadius(), RecordVoiceView.MIN_PRESSED_BUTTON_RADIUS);
                ObjectAnimator animVoiceRadius = ObjectAnimator.ofInt(mChatRecord, "currVoiceRadius", mChatRecord.getCurrVoiceRadius(), RecordVoiceView.MIN_PRESSED_BUTTON_RADIUS);
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(animLayout, animButtonX, animButtonRadius, animVoiceRadius);
                animSet.setDuration(300);
                mChatRecord.canTouch = false;
                VoiceController voiceController = VoiceController.getController();
                voiceController.stopRecordVoice(isNeedSend);
                animSet.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mChatRecord.onCancel();
                        cancelLock = false;
                    }
                });
                animSet.start();
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (cancelLock) {
                            return true;
                        }
                        mSlideToCancelTimer.setText("00:00");
                        objectAnimatorStart.cancel();
                        objectAnimatorStart.removeAllListeners();
                        objectAnimatorStart.addListener(new AnimatorEndListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                VoiceController voiceController = VoiceController.getController();
                                boolean isStarted = voiceController.startRecordVoice();
                                if (isStarted) {
                                    mChatRecord.onStartRecording();
                                }
                            }
                        });
                        objectAnimatorStart.setDuration(300);
                        objectAnimatorStart.start();
                        ((Vibrator) App.getAppContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(40);

                        remMajorX = event.getX();
                        remTempX = event.getX();
                        mChatRecord.onPressed();
                        recordVoiceTextLayout.setAlpha(1);
                        if (remTextTranslationX == null) {
                            remTextTranslationX = recordVoiceTextLayout.getTranslationX();
                        } else {
                            recordVoiceTextLayout.setTranslationX(remTextTranslationX);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE: {
                        objectAnimatorStart.cancel();
                        cancel(false);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        objectAnimatorStart.cancel();
                        cancel(true);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                        float majDx = remMajorX - event.getX();
                        if (cancelDistance <= remMajorX - event.getX()) {
                            cancel(false);
                        } else {
                            if (event.getX() < remMajorX) {
                                int tempDx = (int) (remTempX - event.getX());
                                mChatRecord.onMove(tempDx);
                                remTempX = event.getX();
                                recordVoiceTextLayout.setTranslationX(-majDx);
                                recordVoiceTextLayout.setAlpha(1 - majDx / cancelDistance);
                            }
                        }
                        break;
                }
                return true;
            }
        });

        mChatRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (mIsFirstBotOpening != null && mIsFirstBotOpening) {
            processFirstBotOpening();
        }

        mBotPopupCommandsLayout = findView(R.id.bot_popup_commands_layout);
        mBotPopupCommandsUpLayout = findView(R.id.bot_popup_commands_up_layout);

        mBotCommandListView = (SimpleRecyclerView) mBotPopupCommandsLayout.findViewById(R.id.bot_popup_commands_list);

        mBotCommandListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        BotCommandsAdapter botCommandsAdapter = new BotCommandsAdapter(BotManager.getManager().getBotCommandListForSearch(), ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        mBotCommandListView.setLayoutManager(linearLayoutManager);

        mBotCommandListView.setAdapter(botCommandsAdapter);

        BotManager.getManager().setAdapter(botCommandsAdapter);


        mChatIcSlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiconEditText.setText("/");
                mEmojiconEditText.setSelection(mEmojiconEditText.getText().length());
            }
        });

        mMusicBarWidget = new MusicBarWidget();
        mMusicBarWidget.init(this);
    }

    public void processFirstBotOpening() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                try {
                    RelativeLayout relativeLayoutStartButton = new RelativeLayout(ChatActivity.this);
                    relativeLayoutStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mChatRecord.setVisibility(View.VISIBLE);
                            v.setVisibility(View.GONE);
                            BotManager botManager = BotManager.getManager();
                            botManager.changeSizeOfLastBotMsg(false);
                            botManager.sendBotStartMessage(ChatManager.getCurrentChatId());
                        }
                    });
                    relativeLayoutStartButton.setBackgroundResource(R.drawable.item_click_white_no_transparent);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, AndroidUtil.dp(48));
                    layoutParams.addRule(RelativeLayout.BELOW, R.id.a_chat_editmsg_line);
                    relativeLayoutStartButton.setLayoutParams(layoutParams);

                    //mChatInputHolder.setVisibility(View.INVISIBLE);
                    mChatRecord.setVisibility(View.GONE);

                    mChatInputHolder.addView(relativeLayoutStartButton);

                    TextView textViewStart = new TextView(ChatActivity.this);
                    textViewStart.setText(AndroidUtil.getResourceString(R.string.start_big));
                    textViewStart.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                    textViewStart.setTextSize(17);
                    textViewStart.setTextColor(0xFF569ACE);
                    relativeLayoutStartButton.addView(textViewStart);

                    RelativeLayout.LayoutParams textViewStartLP = (RelativeLayout.LayoutParams) textViewStart.getLayoutParams();
                    textViewStartLP.addRule(RelativeLayout.CENTER_VERTICAL);
                    textViewStartLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    BotManager botManager = BotManager.getManager();
                    botManager.changeSizeOfLastBotMsg(true);


                    if (mPopupMute != null && mPopupMute.isShowing()) {
                        dismissPopUpMuteWithAnimation();
                    }

                    if (mPopupAttach != null && mPopupAttach.isShowing()) {
                        dismissPopUpAttachWithAnimation();
                    }

                    if (getPopupEmoji() != null && getPopupEmoji().isKeyBoardOpen()) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(mEmojiconEditText.getWindowToken(), 0);
                    }

                    if (getPopupEmoji() != null && getPopupEmoji().isShowing()) {
                        getPopupEmoji().dismiss();
                        mMainLayout.setPadding(0, 0, 0, 0);
                    }

                    if (mPopupBotKeyboard != null && mPopupBotKeyboard.isShowing()) {
                        updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                        mPopupBotKeyboard.dismiss();
                        mMainLayout.setPadding(0, 0, 0, 0);
                    }
                } catch (Throwable e) {
                    Log.e(LOG, "processFirstBotOpening", e);
                    Crashlytics.logException(e);
                }
            }
        });
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        MenuItem menuMute;
        if (mIsGroup != null && mIsGroup && mIsLeave != null && !mIsLeave) {
            getMenuInflater().inflate(R.menu.menu_group_chat, menu);
            menuMute = menu.getItem(2);
        } else {
            getMenuInflater().inflate(R.menu.menu_chat, menu);
            menuMute = menu.getItem(1);
        }

        if (mIsMuted != null && mIsMuted)
            menuMute.setTitle(AndroidUtil.getResourceString(R.string.unmute));
        else
            menuMute.setTitle(AndroidUtil.getResourceString(R.string.mute));
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case CameraManager.REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    ChatManager manager = ChatManager.getManager();
                    manager.sendMessage(manager.createPhotoMsg(mPhotoPath));
                    dismissPopUpAttachWithAnimation();
                }
                break;
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    ChatManager manager = ChatManager.getManager();
                    manager.sendMessage(manager.createPhotoMsg(ChatHelper.getRealPathFromURI(selectedImage, ChatActivity.this)));
                    dismissPopUpAttachWithAnimation();
                }
                break;
            case OPEN_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        final Integer msgId = extras.getInt("msgId");
                        final Boolean delete = extras.getBoolean("delete");
                        if (delete) {
                            final ChatManager chatManager = ChatManager.getManager();
                            chatManager.deleteMessage(ChatManager.getCurrentChatId(), msgId);
                        }
                    }
                }
                break;
        }
    }

    public void updateKeyboardIconsVisibility(int command, int slash, int panelKb) {
        if (mSendButton.getVisibility() == View.VISIBLE) {
            mChatIcCommand.setTag(command);
            mChatIcSlash.setTag(slash);
            mChatIcPanelKb.setTag(panelKb);
        } else {
            mChatIcCommand.setVisibility(command);
            mChatIcSlash.setVisibility(slash);
            mChatIcPanelKb.setVisibility(panelKb);
        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            final ChatInfo chatInfo = ChatManager.getCurrentChatInfo();

            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_MUSIC_PLAYER: {
                    mMusicBarWidget.checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG: {
                    ChatManager.getManager().notifySetDataChangedAsync();
                    break;
                }
                case NotificationObject.UPDATE_RECORD_VOICE_STATE: {
                    Object[] values = (Object[]) nObject.getWhat();
                    final int voiceAmplitude = (int) values[0];
                    final int time = (int) values[1];
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            mSlideToCancelTimer.setText(ChatHelper.getDurationString(time));
                            mChatRecord.setCurrVoiceRadius(voiceAmplitude);
                        }
                    });
                    break;
                }
                case NotificationObject.FORCE_CLOSE_CHAT: {
                    forceFinish();
                    break;
                }
                case NotificationObject.BOT_SHOW_START: {
                    processFirstBotOpening();
                    break;
                }
                case NotificationObject.BOT_CHANGE_ICON_VISIBILITY: {
                    int[] values = (int[]) nObject.getWhat();
                    //command, slash, panelKb
                    updateKeyboardIconsVisibility(values[0], values[1], values[2]);
                    break;
                }
                case NotificationObject.BOT_SHOW_COMMAND_LIST: {
                    showPopUpBotCommands();
                    break;
                }
                case NotificationObject.BOT_HIDE_COMMAND_LIST: {
                    dismissPopUpBotCommandsWithAnimation();
                    break;
                }
                case NotificationObject.BOT_HIDE_COMMAND_LIST_AND_CLEAN_EDIT: {
                    dismissPopUpBotCommandsWithAnimation();
                    mEmojiconEditText.setText("");
                    break;
                }
                case NotificationObject.CHANGE_CHAT_MUTE_STATUS:
                    Object[] arrMute = (Object[]) nObject.getWhat();
                    final long chatId = (long) arrMute[0];
                    final int muteFot = (int) arrMute[1];
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (ChatManager.getManager().isSameChatId(chatId)) {
                                if (mIsMuted != null) {
                                    mIsMuted = muteFot > 0;
                                    supportInvalidateOptionsMenu();
                                    if (mIsMuted)
                                        mIcMute.setVisibility(View.VISIBLE);
                                    else
                                        mIcMute.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                    break;
                case NotificationObject.LEFT_CHAT:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (ChatManager.isHaveChatInfo()) {
                                MenuItem item = mMenu.findItem(R.id.action_clear_leave_group);
                                item.setVisible(false);
                            }
                        }
                    });
                    break;

                case NotificationObject.UPDATE_USER_STATUS:
                    final TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) nObject.getWhat();
                    if (chatInfo != null) {
                        if (!chatInfo.isGroupChat) {
                            TdApi.PrivateChatInfo privateChatInfo = (TdApi.PrivateChatInfo) chatInfo.tgChatObject.type;
                            if (privateChatInfo.user.id == updateUserStatus.userId) {
                                //разве нужно?
                                privateChatInfo.user.status = updateUserStatus.status;
                                final String str = ChatHelper.lastSeenUser(updateUserStatus.status);
                                AndroidUtil.runInUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ChatManager.isHaveChatInfo())
                                            mChatSubtext.setText(str);
                                    }
                                });
                            }
                        } else {
                            //если группа, пробежаться по всем учасникам
                            TdApi.ChatParticipant[] participants = chatInfo.groupChatFull.participants;
                            TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                            boolean isNeedSubTextUpdate = false;
                            //находим юзера в этом чате
                            for (int i = 0; i < participants.length; i++) {
                                TdApi.ChatParticipant participant = participants[i];
                                if (participant.user.id == updateUserStatus.userId) {
                                    isNeedSubTextUpdate = true;
                                    participant.user.status = updateUserStatus.status;
                                    break;
                                }
                            }
                            if (isNeedSubTextUpdate) {
                                Object[] objects = ChatHelper.calculateOnlineUsersInGroupChat(chatInfo.groupChatFull);
                                chatInfo.groupMembersOnline = (int) objects[0];
                                final String str = groupChatInfo.groupChat.participantsCount
                                        + AndroidUtil.getResourceString(R.string.members_)
                                        + chatInfo.groupMembersOnline + AndroidUtil.getResourceString(R.string._online);
                                AndroidUtil.runInUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ChatManager.isHaveChatInfo())
                                            mChatSubtext.setText(str);
                                    }
                                });
                            }
                        }
                    }
                    break;
                case NotificationObject.UPDATE_MEMBER_COUNT:
                    final TdApi.UpdateChatParticipantsCount chatParticipantsCount = (TdApi.UpdateChatParticipantsCount) nObject.getWhat();
                    if (chatInfo != null && chatInfo.isGroupChat) {
                        TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                        if (groupChatInfo.groupChat.id == chatParticipantsCount.chatId) {
                            final String str = chatParticipantsCount.participantsCount
                                    + AndroidUtil.getResourceString(R.string.members_)
                                    + chatInfo.groupMembersOnline + AndroidUtil.getResourceString(R.string._online);
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    if (ChatManager.isHaveChatInfo())
                                        mChatSubtext.setText(str);
                                }
                            });
                        }
                    }
                    break;
                case NotificationObject.UPDATE_CHAT_TITLE:
                    final TdApi.UpdateChatTitle updateChatTitle = (TdApi.UpdateChatTitle) nObject.getWhat();
                    if (ChatManager.getManager().isSameChatId(updateChatTitle.chatId)) {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (ChatManager.isHaveChatInfo()) {
                                    try {

                                        mChatTitle.setText(updateChatTitle.title);

                                        TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                                        TdApi.GroupChat groupChat = groupChatInfo.groupChat;

                                        final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, groupChat.id, chatInfo.initials, groupChat.photo.small);
                                        mChatIcon.setImageDrawable(dr);

                                    } catch (Exception e) {
                                        //
                                    }
                                }
                            }
                        });
                    }
                    break;
                case NotificationObject.UPDATE_CHAT_TITLE_USER:
                    final CachedUser cachedUserForTitle = (CachedUser) nObject.getWhat();
                    if (ChatManager.getManager().isSameChatId(cachedUserForTitle.tgUser.id)) {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (ChatManager.isHaveChatInfo()) {
                                    try {
                                        mChatTitle.setText(cachedUserForTitle.fullName);
                                        final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, cachedUserForTitle.tgUser.id,
                                                chatInfo.initials, cachedUserForTitle.tgUser.profilePhoto.small);
                                        mChatIcon.setImageDrawable(dr);
                                    } catch (Exception e) {
                                        //
                                    }
                                }
                            }
                        });
                    }
                    break;
                case NotificationObject.UPDATE_CHAT_PHOTO:
                    final TdApi.UpdateChatPhoto updateChatPhoto = (TdApi.UpdateChatPhoto) nObject.getWhat();
                    if (ChatManager.getManager().isSameChatId(updateChatPhoto.chatId)) {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (ChatManager.isHaveChatInfo()) {
                                        final IconDrawable dr = IconFactory.createBitmapIcon(IconFactory.Type.TITLE, updateChatPhoto.photo.small.path);
                                        mChatIcon.setImageDrawable(dr);
                                    }
                                } catch (Exception e) {
                                    //
                                }
                            }
                        });
                    }
                    break;
                case NotificationObject.GALLERY_UPDATE_DATA:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mGalleryListAdapter != null)
                                mGalleryListAdapter.update();
                        }
                    });
                    break;


            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mChatIcon = (ImageView) toolbar.findViewById(R.id.t_chat_icon);
            mChatTitle = (TextView) toolbar.findViewById(R.id.t_chat_title);
            mChatSubtext = (TextView) toolbar.findViewById(R.id.t_chat_subtext);
            mIcMute = (ImageView) toolbar.findViewById(R.id.t_ic_mute);

            if (mIsMuted != null && mIsMuted)
                mIcMute.setVisibility(View.VISIBLE);
            else
                mIcMute.setVisibility(View.GONE);

            mChatTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            mChatTitle.setTextSize(18);
            mChatTitle.setTextColor(0xffffffff);

            mChatSubtext.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            mChatSubtext.setTextSize(14);
            mChatSubtext.setTextColor(0xffd2eafc);

            ChatInfo chatInfo = ChatManager.getCurrentChatInfo();
            if (chatInfo != null) {
                TdApi.File file = null;
                int id = 0;
                String initials = chatInfo.initials;
                String title = "";
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
                        subTitle = groupChatInfo.groupChat.participantsCount
                                + AndroidUtil.getResourceString(R.string.members_)
                                + chatInfo.groupMembersOnline + AndroidUtil.getResourceString(R.string._online);
                        break;
                }

                mChatTitle.setText(chatInfo.chatName);

                if (mIsLeave != null && mIsLeave) {
                    mChatSubtext.setText(AndroidUtil.getResourceString(R.string.you_are_not_in_chat));
                } else {
                    mChatSubtext.setText(subTitle);
                }

                if (file != null) {
                    final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, id, initials, file);
                    mChatIcon.setImageDrawable(dr);
                }
            }

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}
