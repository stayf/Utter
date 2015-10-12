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
import com.stayfprod.emojicon.EmojiconsPopup;
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
        musicBarWidget.checkOnStart();
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
        popupBotKeyboard.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());
    }

    private void dismissPopUpAttachWithAnimation() {
        if (popupAttach.isShowing()) {
            ObjectAnimator anim = ObjectAnimator.ofObject(popupAttachView,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    0x33000000,
                    0x00000000);
            anim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    popupAttach.dismiss();
                }
            });
            anim.setDuration(200).start();
        } else {
            popupAttach.dismiss();
        }
    }

    private void dismissPopUpBotCommandsWithAnimation() {
        if (isPopUpBotCommandsVisible) {
            isPopUpBotCommandsVisible = false;
            ObjectAnimator popupAnim = ObjectAnimator.ofObject(bot_popup_commands_up_layout,
                    "backgroundColor", new ArgbEvaluator(),
                    0x33000000, 0x00000000);
            popupAnim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bot_popup_commands_layout, "translationY", 0, AndroidUtil.dp(124));
                    objectAnimator.addListener(new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            bot_popup_commands_layout.setVisibility(View.GONE);
                            bot_popup_commands_up_layout.setVisibility(View.GONE);
                        }
                    });

                    objectAnimator.setDuration(150).start();
                }
            });
            popupAnim.setDuration(100).start();
        }
    }

    private void dismissPopUpMuteWithAnimation() {
        if (popupMute != null) {
            if (popupMute.isShowing()) {
                ObjectAnimator anim = ObjectAnimator.ofObject(popupMuteView,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        0x33000000,
                        0x00000000);
                anim.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        popupMute.dismiss();
                    }
                });
                anim.setDuration(200).start();
            } else {
                popupMute.dismiss();
            }
        }
    }

    private void showPopUpBotCommands() {
        if (!isPopUpBotCommandsVisible) {
            isPopUpBotCommandsVisible = true;
            bot_popup_commands_layout.setVisibility(View.VISIBLE);
            bot_popup_commands_up_layout.setVisibility(View.VISIBLE);
            ObjectAnimator popupAnim = ObjectAnimator.ofFloat(bot_popup_commands_layout, "translationY", AndroidUtil.dp(124), 0);
            popupAnim.setDuration(150);
            popupAnim.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ObjectAnimator.ofObject(bot_popup_commands_up_layout,
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

        if (popupMute != null && popupMute.isShowing()) {
            dismissPopUpMuteWithAnimation();
            return;
        }

        if (popupAttach.isShowing()) {
            dismissPopUpAttachWithAnimation();
            return;
        }

        if (popupEmoji != null && getPopupEmoji().isKeyBoardOpen()) {
            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(emojiconEditText.getWindowToken(), 0);
            return;
        }

        if (popupEmoji != null && getPopupEmoji().isShowing()) {
            getPopupEmoji().dismiss();
            main.setPadding(0, 0, 0, 0);
            return;
        }

        if (popupBotKeyboard.isShowing()) {
            updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
            popupBotKeyboard.dismiss();
            main.setPadding(0, 0, 0, 0);
            return;
        }

        if (popupEmoji != null)
            getPopupEmoji().dismiss();
        popupBotKeyboard.dismiss();
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
                if (ChatManager.isNeedRemoveChat) {
                    ChatManager.isNeedRemoveChat = false;
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

        if (popupEmoji != null && getPopupEmoji() != null)
            getPopupEmoji().dismiss();

        if (popupBotKeyboard != null)
            popupBotKeyboard.dismiss();

        if (popupAttach != null)
            popupAttach.dismiss();

        if (popupMute != null)
            popupMute.dismiss();

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

        if (ChatManager.isNeedRemoveChat) {
            ChatManager.isNeedRemoveChat = false;
            ChatListManager.getManager().removeChat(chatId);
        }

        StickerManager stickerManager = StickerManager.getManager();
        stickerManager.cleanStickerViews();
    }

    private static final int SELECT_PHOTO = 100;
    public static final int OPEN_PHOTO = 55;

    private View popupAttachView;
    private View popupMuteView;
    private RecyclerView recyclerViewGallery;
    private EmojiconEditText emojiconEditText;
    private GalleryAdapter galleryListAdapter;
    private RecyclerView.LayoutManager chatListLayoutManager;

    private LinearLayout takePhotoButton;
    private String photoPath;
    private EmojiconsPopup popupEmoji;
    private BotKeyboardPopup popupBotKeyboard;
    private PopupWindow popupAttach;
    private PopupWindow popupMute;
    private RelativeLayout main;

    private Boolean isGroup;
    private Boolean isLeave;
    private Boolean isMuted = false;
    private Boolean isFirstBotOpening;
    private SimpleRecyclerView botCommandListView;
    private RelativeLayout bot_popup_commands_layout;
    private RelativeLayout bot_popup_commands_up_layout;
    private boolean isPopUpBotCommandsVisible;

    private ImageView a_chat_ic_slash;
    private ImageView a_chat_ic_comand;
    private ImageView a_chat_ic_panel_kb;
    private ImageView sendButton;
    private RecordVoiceView a_chat_record;
    private RelativeLayout a_chat_input_holder;
    private TextView slide_to_cancel_timer;
    private ImageView emojiButton;

    private MenuItem menuMute;

    private MusicBarWidget musicBarWidget;

    public EmojiconsPopup getPopupEmoji() {
        if (popupEmoji == null) {

            popupEmoji = new EmojiconsPopup(main, this, new StickerGridView(this), new StickerMicroThumbAdapterImpl(this));

            popupEmoji.setSizeForSoftKeyboard();
            popupEmoji.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {
                @Override
                public void onEmojiconBackspaceClicked(View v) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    emojiconEditText.dispatchKeyEvent(event);
                }
            });

            popupEmoji.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    changeEmojiKeyboardIcon(emojiButton, R.mipmap.ic_smiles);
                    StickerRecentManager.getInstance().saveRecents();
                }
            });

            popupEmoji.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
                @Override
                public void onKeyboardOpen(int keyBoardHeight) {
                    AndroidUtil.setKeyboardHeight(keyBoardHeight);
                }

                @Override
                public void onKeyboardClose() {
                    if (popupEmoji.isShowing())
                        popupEmoji.dismiss();
                }
            });
            popupEmoji.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {
                @Override
                public void onEmojiconClicked(Emojicon emojicon) {
                    int selectionCursor = emojiconEditText.getSelectionStart();
                    emojiconEditText.getText().insert(selectionCursor, emojicon.getEmoji());
                }
            });

            popupEmoji.setAnimationStyle(R.style.popup_anim_style);
            popupAttach.setAnimationStyle(R.style.popup_anim_style);

            popupEmoji.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {
                @Override
                public void onEmojiconBackspaceClicked(View v) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    emojiconEditText.dispatchKeyEvent(event);
                }
            });

            popupEmoji.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());

        }
        return popupEmoji;
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
                if (popupMute == null) {
                    popupMuteView = LayoutInflater.from(this).inflate(R.layout.popup_mute, main, false);
                    popupMute = new PopupWindow(popupMuteView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    popupMute.setContentView(popupMuteView);
                    popupMute.setAnimationStyle(R.style.popup_anim_style);

                    popupMuteView.setOnClickListener(new View.OnClickListener() {
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

                    View p_mute_hour_layout = popupMuteView.findViewById(R.id.p_mute_hour_layout);
                    View p_mute_8_hours_layout = popupMuteView.findViewById(R.id.p_mute_8_hours_layout);
                    View p_mute_2_days_layout = popupMuteView.findViewById(R.id.p_mute_2_days_layout);
                    View p_mute_disable_layout = popupMuteView.findViewById(R.id.p_mute_disable_layout);

                    p_mute_hour_layout.setOnClickListener(onClickListener);
                    p_mute_8_hours_layout.setOnClickListener(onClickListener);
                    p_mute_2_days_layout.setOnClickListener(onClickListener);
                    p_mute_disable_layout.setOnClickListener(onClickListener);

                    p_mute_hour_layout.setTag(NotificationManager.MUTE_FOR_HOUR);
                    p_mute_8_hours_layout.setTag(NotificationManager.MUTE_FOR_8_HOURS);
                    p_mute_2_days_layout.setTag(NotificationManager.MUTE_FOR_2_DAYS);
                    p_mute_disable_layout.setTag(NotificationManager.MUTE_DISABLE);
                }

                if (isMuted) {
                    Long chatId = ChatManager.getCurrentChatId();
                    NotificationManager.getManager().setMuteForChat(NotificationManager.UNMUTE, chatId);
                } else {
                    if (!popupMute.isShowing()) {
                        popupMute.showAtLocation(popupMuteView, Gravity.CENTER, 0, 0);
                        ObjectAnimator.ofObject(popupMuteView,
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

    private volatile boolean isClickedOnToolbar;

    private void openProfileActivity() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isGroup", isGroup);
                bundle.putBoolean("isMuted", isMuted);

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

        int k = 0;


        Bundle b = getIntent().getExtras();

        if (b != null) {
            isGroup = b.getBoolean("isGroup");
            isLeave = b.getBoolean("isLeave");
            isMuted = b.getBoolean("isMuted");
            if (isMuted == null) {
                isMuted = false;
            }
            isFirstBotOpening = b.getBoolean("isFirstBotOpening");
        }

        setContentView(R.layout.activity_chat);
        FileManager.canDownloadFile = true;
        setToolbar();
        main = (RelativeLayout) findViewById(R.id.activity_chat_layout);
        popupAttachView = LayoutInflater.from(this).inflate(R.layout.popup_attach, main, false);

        takePhotoButton = (LinearLayout) popupAttachView.findViewById(R.id.p_open_camera);

        RelativeLayout t_layout_main = findView(R.id.t_layout_main);
        t_layout_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGroup) {
                    if (!isClickedOnToolbar) {
                        try {
                            isClickedOnToolbar = true;
                            UserManager userManager = UserManager.getManager();
                            int userId = ((TdApi.PrivateChatInfo) ChatManager.getCurrentChatInfo().tgChatObject.type).user.id;
                            CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userId);
                            if (cachedUser.isHaveFullInfo) {
                                openProfileActivity();
                                isClickedOnToolbar = false;
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
                                        isClickedOnToolbar = false;
                                    }
                                });
                            }
                        } catch (Exception e) {
                            isClickedOnToolbar = false;
                        }
                    }
                } else {
                    if (isLeave == null || !isLeave) {
                        openProfileActivity();
                    }
                }
            }
        });


        final LinearLayout openGallerySendImages = (LinearLayout) popupAttachView.findViewById(R.id.p_open_gallery_send_images);
        final TextView choose_from_gallery_text = (TextView) openGallerySendImages.findViewById(R.id.choose_from_gallery_text);

        final TextView take_photo_text = (TextView) popupAttachView.findViewById(R.id.take_photo_text);
        choose_from_gallery_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        choose_from_gallery_text.setTextSize(16);
        choose_from_gallery_text.setTextColor(0xFF222222);

        take_photo_text.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        take_photo_text.setTextSize(16);
        take_photo_text.setTextColor(0xFF222222);

        RelativeLayout p_open_sub_layout = (RelativeLayout) popupAttachView.findViewById(R.id.p_open_sub_layout);
        p_open_sub_layout.getLayoutParams().height = AndroidUtil.getKeyboardHeight();

        RelativeLayout p_open_main_layout = (RelativeLayout) popupAttachView.findViewById(R.id.p_open_main_layout);
        p_open_main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopUpAttachWithAnimation();
            }
        });

        openGallerySendImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SparseArray<GalleryAdapter.StorageObject> selectedItems = galleryListAdapter.getSelectedItems();
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
                                    galleryListAdapter.notifyDataSetChanged();
                                    choose_from_gallery_text.setText(AndroidUtil.getResourceString(R.string.choose_from_gallery));
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

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    photoPath = CameraManager.getManager().dispatchTakePictureIntent(ChatActivity.this);
                } catch (Exception e) {
                    AndroidUtil.showToastLong(e.getMessage() + "");
                }
            }
        });

        recyclerViewGallery = (RecyclerView) popupAttachView.findViewById(R.id.p_list_galery);
        galleryListAdapter = new GalleryAdapter(this, recyclerViewGallery);
        chatListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        popupAttach = new PopupWindow(popupAttachView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        popupAttach.setContentView(popupAttachView);

        recyclerViewGallery.setLayoutManager(chatListLayoutManager);

        recyclerViewGallery.setAdapter(galleryListAdapter);

        ChatManager chatManager = ChatManager.getManager();
        chatManager.initRecycleView(this, isLeave);

        ChatManager.getManager().getChat(true);

        a_chat_no_msges = findView(R.id.a_chat_no_msges);
        a_chat_no_msges.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_chat_no_msges.setTextSize(16);
        a_chat_no_msges.setTextColor(0xff999999);

        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojiconEditText.setLongClickable(false);

        emojiconEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

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

        a_chat_input_holder = (RelativeLayout) findViewById(R.id.a_chat_input_holder);
        a_chat_record = (RecordVoiceView) findViewById(R.id.a_chat_record);
        a_chat_record.setTag(a_chat_record.getVisibility());

        if (isLeave != null && isLeave) {
            a_chat_input_holder.setVisibility(View.GONE);
            a_chat_record.setVisibility(View.GONE);
        }

        AndroidUtil.setEditTextTypeface(emojiconEditText);

        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView attachButton = (ImageView) findViewById(R.id.a_chat_attach);
        sendButton = (ImageView) findViewById(R.id.a_chat_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv = (ImageView) v;
                iv.setVisibility(View.GONE);
                attachButton.setVisibility(View.VISIBLE);
                String msg = emojiconEditText.getText().toString();
                emojiconEditText.setText("");
                ChatManager manager = ChatManager.getManager();

                if (TextUtil.isNotBlank(msg)) {
                    manager.sendMessage(manager.createTextMsg(msg));
                }

            }
        });

        //ChatManager.getManager().readChatHistory();

        a_chat_ic_slash = (ImageView) findViewById(R.id.a_chat_ic_slash);
        a_chat_ic_comand = findView(R.id.a_chat_ic_comand);
        a_chat_ic_panel_kb = findView(R.id.a_chat_ic_panel_kb);

        a_chat_ic_slash.setTag(a_chat_ic_slash.getVisibility());
        a_chat_ic_comand.setTag(a_chat_ic_comand.getVisibility());
        a_chat_ic_panel_kb.setTag(a_chat_ic_panel_kb.getVisibility());

        a_chat_ic_panel_kb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                popupBotKeyboard.dismiss();
                main.setPadding(0, 0, 0, 0);
                emojiconEditText.setFocusableInTouchMode(true);
                emojiconEditText.requestFocus();
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        emojiconEditText.addTextChangedListener(new AfterTextChangedListener() {

            private boolean isFirst = true;

            @SuppressWarnings("ResourceType")
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (isFirst) {
                        isFirst = false;

                        sendButton.setVisibility(View.VISIBLE);
                        attachButton.setVisibility(View.GONE);

                        a_chat_record.setTag(a_chat_record.getVisibility());
                        a_chat_record.setVisibility(View.GONE);

                        a_chat_ic_panel_kb.setTag(a_chat_ic_panel_kb.getVisibility());
                        a_chat_ic_panel_kb.setVisibility(View.GONE);

                        a_chat_ic_comand.setTag(a_chat_ic_comand.getVisibility());
                        a_chat_ic_comand.setVisibility(View.GONE);

                        a_chat_ic_slash.setTag(a_chat_ic_slash.getVisibility());
                        a_chat_ic_slash.setVisibility(View.GONE);
                    }

                    BotManager.getManager().findCommand(s.toString());
                } else {
                    isFirst = true;
                    dismissPopUpBotCommandsWithAnimation();

                    sendButton.setVisibility(View.GONE);
                    attachButton.setVisibility(View.VISIBLE);

                    a_chat_record.setVisibility((int) a_chat_record.getTag());
                    a_chat_ic_panel_kb.setVisibility((int) a_chat_ic_panel_kb.getTag());
                    a_chat_ic_comand.setVisibility((int) a_chat_ic_comand.getTag());
                    a_chat_ic_slash.setVisibility((int) a_chat_ic_slash.getTag());
                }
            }
        });

        emojiconEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        if (!getPopupEmoji().isKeyBoardOpen()) {
                            getPopupEmoji().dismiss();
                            main.setPadding(0, 0, 0, 0);

                            if (popupBotKeyboard.isShowing()) {
                                popupBotKeyboard.dismiss();
                                updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                            }

                            emojiconEditText.setFocusableInTouchMode(true);
                            emojiconEditText.requestFocus();
                            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        }
                        return true;
                    }
                }
                return false;
            }
        });


        popupBotKeyboard = new BotKeyboardPopup(this, main);

        BotManager.getManager().setPopupBotKeyboard(popupBotKeyboard, main);
        popupBotKeyboard.setSizeForSoftKeyboard();
        popupBotKeyboard.setSize(WindowManager.LayoutParams.MATCH_PARENT, AndroidUtil.getKeyboardHeight());

        popupBotKeyboard.setAnimationStyle(R.style.popup_anim_style);
        popupBotKeyboard.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //нельзя тут ничего делать!!
            }
        });

        popupBotKeyboard.setOnSoftKeyboardOpenCloseListener(new BotKeyboardPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
                AndroidUtil.setKeyboardHeight(keyBoardHeight);
            }

            @Override
            public void onKeyboardClose() {
                if (popupBotKeyboard.isShowing())
                    popupBotKeyboard.dismiss();
            }
        });

        a_chat_ic_comand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!popupBotKeyboard.isShowing()) {
                    ChatManager.getManager().stopScroll();
                    if (popupBotKeyboard.isKeyBoardOpen()) {
                        popupBotKeyboard.showAtBottom();
                        main.setPadding(0, 0, 0, 0);
                    } else {
                        popupBotKeyboard.showAtBottomFirstTime();
                        main.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
                    }
                }
                a_chat_ic_panel_kb.setVisibility(View.VISIBLE);
                a_chat_ic_comand.setVisibility(View.GONE);

                if (getPopupEmoji().isShowing()) {
                    getPopupEmoji().dismiss();
                }
            }
        });


        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPopupEmoji().isShowing()) {
                    if (popupBotKeyboard.isShowing()) {
                        popupBotKeyboard.dismiss();
                        updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                    }
                    ChatManager.getManager().stopScroll();
                    if (getPopupEmoji().isKeyBoardOpen()) {
                        getPopupEmoji().showAtBottom();
                        main.setPadding(0, 0, 0, 0);
                        changeEmojiKeyboardIcon(emojiButton, R.mipmap.ic_msg_panel_kb);
                    } else {
                        getPopupEmoji().showAtBottomFirstTime();
                        main.setPadding(0, 0, 0, AndroidUtil.getKeyboardHeight());
                        changeEmojiKeyboardIcon(emojiButton, R.mipmap.ic_msg_panel_kb);
                    }
                } else {
                    getPopupEmoji().dismiss();
                    main.setPadding(0, 0, 0, 0);
                    emojiconEditText.setFocusableInTouchMode(true);
                    emojiconEditText.requestFocus();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emojiconEditText.getWindowToken(), 0);
                if (!popupAttach.isShowing()) {
                    popupAttach.showAtLocation(popupAttachView, Gravity.CENTER, 0, 0);
                    ObjectAnimator.ofObject(popupAttachView,
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
        slide_to_cancel_timer = (TextView) findViewById(R.id.slide_to_cancel_timer);

        cancelLayout.setTranslationX(WINDOW_CURRENT_WIDTH);

        TextView recordVoiceText = (TextView) recordVoiceTextLayout.getChildAt(1);
        recordVoiceText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        recordVoiceText.setTextSize(17);
        recordVoiceText.setTextColor(0xFFB3B3B3);

        slide_to_cancel_timer.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        slide_to_cancel_timer.setTextSize(17);
        slide_to_cancel_timer.setTextColor(Color.BLACK);

        a_chat_record.setAfterTouchListener(new View.OnTouchListener() {
            float remMajorX = 0;
            float remTempX = 0;
            int cancelDistance = AndroidUtil.dp(100);
            Float remTextTranslationX;
            ObjectAnimator objectAnimatorStart = ObjectAnimator.ofFloat(cancelLayout, "translationX", WINDOW_CURRENT_WIDTH, 0);
            boolean cancelLock;

            public void cancel(final boolean isNeedSend) {
                cancelLock = true;
                ObjectAnimator animLayout = ObjectAnimator.ofFloat(cancelLayout, "translationX", 0, WINDOW_CURRENT_WIDTH);
                ObjectAnimator animButtonX = ObjectAnimator.ofInt(a_chat_record, "dx", a_chat_record.getDx(), 0);
                ObjectAnimator animButtonRadius = ObjectAnimator.ofInt(a_chat_record, "currRadius", a_chat_record.getCurrRadius(), RecordVoiceView.MIN_PRESSED_BUTTON_RADIUS);
                ObjectAnimator animVoiceRadius = ObjectAnimator.ofInt(a_chat_record, "currVoiceRadius", a_chat_record.getCurrVoiceRadius(), RecordVoiceView.MIN_PRESSED_BUTTON_RADIUS);
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(animLayout, animButtonX, animButtonRadius, animVoiceRadius);
                animSet.setDuration(300);
                a_chat_record.canTouch = false;
                VoiceController voiceController = VoiceController.getController();
                voiceController.stopRecordVoice(isNeedSend);
                animSet.addListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        a_chat_record.onCancel();
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
                        slide_to_cancel_timer.setText("00:00");
                        objectAnimatorStart.cancel();
                        objectAnimatorStart.removeAllListeners();
                        objectAnimatorStart.addListener(new AnimatorEndListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                VoiceController voiceController = VoiceController.getController();
                                boolean isStarted = voiceController.startRecordVoice();
                                if (isStarted) {
                                    a_chat_record.onStartRecording();
                                }
                            }
                        });
                        objectAnimatorStart.setDuration(300);
                        objectAnimatorStart.start();
                        ((Vibrator) App.getAppContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(40);

                        remMajorX = event.getX();
                        remTempX = event.getX();
                        a_chat_record.onPressed();
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
                                a_chat_record.onMove(tempDx);
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

        a_chat_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (isFirstBotOpening != null && isFirstBotOpening) {
            processFirstBotOpening();
        }

        bot_popup_commands_layout = findView(R.id.bot_popup_commands_layout);
        bot_popup_commands_up_layout = findView(R.id.bot_popup_commands_up_layout);
        /*bot_popup_commands_up_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopUpBotCommandsWithAnimation();
            }
        });*/

        botCommandListView = (SimpleRecyclerView) bot_popup_commands_layout.findViewById(R.id.bot_popup_commands_list);

        botCommandListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        BotCommandsAdapter botCommandsAdapter = new BotCommandsAdapter(BotManager.getManager().getBotCommandListForSearch(), ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        botCommandListView.setLayoutManager(linearLayoutManager);

        botCommandListView.setAdapter(botCommandsAdapter);

        BotManager.getManager().setAdapter(botCommandsAdapter);


        a_chat_ic_slash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiconEditText.setText("/");
                emojiconEditText.setSelection(emojiconEditText.getText().length());
            }
        });

        musicBarWidget = new MusicBarWidget();
        musicBarWidget.init(this);
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
                            a_chat_record.setVisibility(View.VISIBLE);
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

                    //a_chat_input_holder.setVisibility(View.INVISIBLE);
                    a_chat_record.setVisibility(View.GONE);

                    a_chat_input_holder.addView(relativeLayoutStartButton);

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


                    if (popupMute != null && popupMute.isShowing()) {
                        dismissPopUpMuteWithAnimation();
                    }

                    if (popupAttach != null && popupAttach.isShowing()) {
                        dismissPopUpAttachWithAnimation();
                    }

                    if (getPopupEmoji() != null && getPopupEmoji().isKeyBoardOpen()) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(emojiconEditText.getWindowToken(), 0);
                    }

                    if (getPopupEmoji() != null && getPopupEmoji().isShowing()) {
                        getPopupEmoji().dismiss();
                        main.setPadding(0, 0, 0, 0);
                    }

                    if (popupBotKeyboard != null && popupBotKeyboard.isShowing()) {
                        updateKeyboardIconsVisibility(View.VISIBLE, View.GONE, View.GONE);
                        popupBotKeyboard.dismiss();
                        main.setPadding(0, 0, 0, 0);
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
        this.menu = menu;
        MenuItem menuMute;
        if (isGroup != null && isGroup && isLeave != null && !isLeave) {
            getMenuInflater().inflate(R.menu.menu_group_chat, menu);
            menuMute = menu.getItem(2);
        } else {
            getMenuInflater().inflate(R.menu.menu_chat, menu);
            menuMute = menu.getItem(1);
        }

        if (isMuted != null && isMuted)
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
                    manager.sendMessage(manager.createPhotoMsg(photoPath));
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

    private Menu menu;


    public void updateKeyboardIconsVisibility(int command, int slash, int panelKb) {
        if (sendButton.getVisibility() == View.VISIBLE) {
            a_chat_ic_comand.setTag(command);
            a_chat_ic_slash.setTag(slash);
            a_chat_ic_panel_kb.setTag(panelKb);
        } else {
            a_chat_ic_comand.setVisibility(command);
            a_chat_ic_slash.setVisibility(slash);
            a_chat_ic_panel_kb.setVisibility(panelKb);
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
                    musicBarWidget.checkUpdate((Object[]) nObject.getWhat());
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
                            slide_to_cancel_timer.setText(ChatHelper.getDurationString(time));
                            a_chat_record.setCurrVoiceRadius(voiceAmplitude);
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
                    emojiconEditText.setText("");
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
                                if (isMuted != null) {
                                    isMuted = muteFot > 0;
                                    supportInvalidateOptionsMenu();
                                    if (isMuted)
                                        t_ic_mute.setVisibility(View.VISIBLE);
                                    else
                                        t_ic_mute.setVisibility(View.GONE);
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
                                MenuItem item = menu.findItem(R.id.action_clear_leave_group);
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
                                            t_chat_subtext.setText(str);
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
                                            t_chat_subtext.setText(str);
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
                                        t_chat_subtext.setText(str);
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

                                        t_chat_title.setText(updateChatTitle.title);

                                        TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo.tgChatObject.type;
                                        TdApi.GroupChat groupChat = groupChatInfo.groupChat;

                                        final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, groupChat.id, chatInfo.initials, groupChat.photo.small);
                                        t_chat_icon.setImageDrawable(dr);

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
                                        t_chat_title.setText(cachedUserForTitle.fullName);
                                        final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, cachedUserForTitle.tgUser.id,
                                                chatInfo.initials, cachedUserForTitle.tgUser.profilePhoto.small);
                                        t_chat_icon.setImageDrawable(dr);
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
                                        t_chat_icon.setImageDrawable(dr);
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
                            if (galleryListAdapter != null)
                                galleryListAdapter.update();
                        }
                    });
                    break;


            }
        }
    }

    private ImageView t_chat_icon;
    private TextView t_chat_title;
    private TextView t_chat_subtext;
    private TextView a_chat_no_msges;
    private ImageView t_ic_mute;

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_back);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            t_chat_icon = (ImageView) toolbar.findViewById(R.id.t_chat_icon);
            t_chat_title = (TextView) toolbar.findViewById(R.id.t_chat_title);
            t_chat_subtext = (TextView) toolbar.findViewById(R.id.t_chat_subtext);
            t_ic_mute = (ImageView) toolbar.findViewById(R.id.t_ic_mute);

            if (isMuted != null && isMuted)
                t_ic_mute.setVisibility(View.VISIBLE);
            else
                t_ic_mute.setVisibility(View.GONE);

            t_chat_title.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            t_chat_title.setTextSize(18);
            t_chat_title.setTextColor(0xffffffff);

            t_chat_subtext.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            t_chat_subtext.setTextSize(14);
            t_chat_subtext.setTextColor(0xffd2eafc);

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

                t_chat_title.setText(chatInfo.chatName);

                if (isLeave != null && isLeave) {
                    t_chat_subtext.setText(AndroidUtil.getResourceString(R.string.you_are_not_in_chat));
                } else {
                    t_chat_subtext.setText(subTitle);
                }

                if (file != null) {
                    final IconDrawable dr = IconFactory.createIcon(IconFactory.Type.TITLE, id, initials, file);
                    t_chat_icon.setImageDrawable(dr);
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
