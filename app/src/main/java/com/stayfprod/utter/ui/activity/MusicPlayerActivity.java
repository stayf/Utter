package com.stayfprod.utter.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.manager.UpdateHandler;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.DetermineProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicPlayerActivity extends AbstractActivity implements Observer {


    private boolean isHaveBackgroundImage = false;
    private AtomicBoolean isTouchedSeekByUser = new AtomicBoolean(false);
    private int seekProgress;

    private RelativeLayout a_music_top_layout;
    private DetermineProgressView a_music_play;
    private TextView a_music_timer_gone;
    private TextView a_music_timer_left;
    private SeekBar s_audio_seekBar;
    private TextView a_music_author;
    private TextView a_music_name;
    private ImageView a_music_album_image;
    private Toolbar toolbarOne;
    private Toolbar toolbarTwo;
    private ImageView a_music_repeat;
    private ImageView a_music_shuffle;
    private TdApi.Message loadedMessage;

    private boolean isBackOnTouchList;
    private long chatId;

    private void getPhoto(final TdApi.Audio audio) {
        if (audio != null && FileUtils.isTDFileLocal(audio.audio) && !isHaveBackgroundImage) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(audio.audio.path);
                    byte[] image = retriever.getEmbeddedPicture();
                    if (image != null && image.length > 0) {
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    isHaveBackgroundImage = true;
                                    a_music_album_image.setImageBitmap(bitmap);
                                    updateToolBarStyle();
                                    updateStyle();
                                    supportInvalidateOptionsMenu();
                                }
                            }
                        });
                    } else {
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                updateToolBarStyle();
                                updateStyle();
                                supportInvalidateOptionsMenu();
                                a_music_album_image.setImageResource(R.mipmap.ic_nocover);
                            }
                        });
                    }
                }
            });
        } else {
            if (audio == null) {
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isHaveBackgroundImage = false;
                            updateToolBarStyle();
                            updateStyle();
                            supportInvalidateOptionsMenu();
                            a_music_album_image.setImageResource(R.mipmap.ic_nocover);
                        } catch (Exception e) {
                            //
                        }
                    }
                });
            }
        }
    }

    public void checkOnStart() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.getMessageAudio() != null) {
            getPhoto(audioPlayer.getMessageAudio().audio);
        } else {
            getPhoto(null);
        }

        if (audioPlayer.isPaused()) {
            TdApi.MessageAudio messageAudio = audioPlayer.getMessageAudio();
            if (messageAudio != null) {
                updateDuration(messageAudio, audioPlayer.getCurrentProgress(), false);

                DeterminateProgressDrawable determinateProgressDrawable = a_music_play.getProgressDrawable();
                determinateProgressDrawable.setMainSettings(
                        null, DeterminateProgressDrawable.PlayStatus.PLAY,
                        DeterminateProgressDrawable.ColorRange.BLUE,
                        LoadingContentType.AUDIO, true, false);

                determinateProgressDrawable.setVisibility(true);
                determinateProgressDrawable.invalidate();

                a_music_name.setText(messageAudio.audio.title);
                a_music_author.setText(messageAudio.audio.performer);
            }
        }
    }

    public void updateDuration(TdApi.MessageAudio messageAudio, float progress, boolean byUser) {
        if ((!isTouchedSeekByUser.get() || byUser) && messageAudio != null) {
            int maxDuration = messageAudio.audio.duration;//секунды

            int goneSeconds;
            int leftSeconds;

            if (byUser) {
                goneSeconds = (int) (progress * maxDuration) / 1000;
            } else {
                goneSeconds = (int) (progress * maxDuration) / 100;
            }
            leftSeconds = maxDuration - goneSeconds;

            String durationStrGone = ChatHelper.getDurationString(goneSeconds, maxDuration);
            String durationStrLeft = ChatHelper.getDurationString(leftSeconds, maxDuration);

            a_music_timer_gone.setText(durationStrGone);
            a_music_timer_left.setText(durationStrLeft);

            if (!byUser) {
                s_audio_seekBar.setProgress((int) (progress * 10));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AudioPlayer.getPlayer().addObserver(this);
        UpdateHandler.getHandler().addObserver(this);
        checkOnStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AudioPlayer.getPlayer().deleteObserver(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_music_player);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isBackOnTouchList = bundle.getBoolean("isBackOnTouchList", false);
            chatId = bundle.getLong("chatId");
        }

        s_audio_seekBar = (SeekBar) findViewById(R.id.s_audio_seekBar);
        s_audio_seekBar.setMax(1000);

        a_music_top_layout = (RelativeLayout) findViewById(R.id.a_music_top_layout);
        final AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        SharedMediaManager.getManager().searchAudioPlayer(chatId, audioPlayer.getAudioMessages());

        s_audio_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekProgress = progress;
                    updateDuration(audioPlayer.getMessageAudio(), progress, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchedSeekByUser.set(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioPlayer.seekTo(seekProgress);
                isTouchedSeekByUser.set(false);
            }
        });
        a_music_play = (DetermineProgressView) findViewById(R.id.a_music_play);

        ImageView a_music_back = (ImageView) findViewById(R.id.a_music_back);
        a_music_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.clickBack();
            }
        });
        ImageView a_music_forward = (ImageView) findViewById(R.id.a_music_forward);
        a_music_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.clickForward();
            }
        });

        a_music_author = (TextView) findViewById(R.id.a_music_author);
        a_music_name = (TextView) findViewById(R.id.a_music_name);

        a_music_author.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        a_music_author.setTextSize(18);
        a_music_author.setTextColor(0xFF333333);

        a_music_name.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_music_name.setTextSize(16);
        a_music_name.setTextColor(0xFF333333);

        a_music_timer_gone = (TextView) findViewById(R.id.a_music_timer_gone);
        a_music_timer_gone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_music_timer_gone.setTextSize(12);
        a_music_timer_gone.setTextColor(0xFF569ACE);

        a_music_timer_left = (TextView) findViewById(R.id.a_music_timer_left);
        a_music_timer_left.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        a_music_timer_left.setTextSize(12);
        a_music_timer_left.setTextColor(0xFFB3B3B3);

        a_music_repeat = (ImageView) findViewById(R.id.a_music_repeat);
        a_music_shuffle = (ImageView) findViewById(R.id.a_music_shuffle);


        a_music_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isRepeat()) {
                    audioPlayer.setRepeat(false);
                } else
                    audioPlayer.setRepeat(true);
                updateRepeat();
            }
        });

        a_music_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isShuffled()) {
                    audioPlayer.setShuffled(false);
                } else {
                    audioPlayer.setShuffled(true);
                }
                updateShuffled();
            }
        });

        a_music_album_image = (ImageView) findViewById(R.id.a_music_album_image);


        AndroidUtil.addOnGlobalLayoutListener(a_music_top_layout, new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout.LayoutParams) s_audio_seekBar.getLayoutParams()).topMargin = a_music_top_layout.getHeight() - AndroidUtil.dp(9);
            }
        });

        updateStyle();
        setToolbar();


        a_music_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeterminateProgressDrawable progressDrawable = a_music_play.getProgressDrawable();

                if (progressDrawable.getLoadStatus() != null && loadedMessage != null && audioPlayer.getMsgId() == loadedMessage.id) {
                    TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) loadedMessage.message;
                    a_music_play.setTag(audioPlayer.getMsgId());
                    switch (progressDrawable.getLoadStatus()) {
                        case NO_LOAD:
                            if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_AUDIO_PLAYER,
                                        messageAudio.audio.audio.id, -1, loadedMessage.id, a_music_play, messageAudio.audio,
                                        a_music_play.getTag().toString(), null, a_music_play);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            }
                            break;
                        case PAUSE:
                            if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().proceedLoad(messageAudio.audio.audio.id, loadedMessage.id, true);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            }
                            break;
                        case PROCEED_LOAD:
                            if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().cancelDownloadFile(messageAudio.audio.audio.id, loadedMessage.id, true);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                            }
                            break;
                        case LOADED:
                            break;
                    }
                } else if (progressDrawable.getPlayStatus() != null) {
                    a_music_play.setTag(audioPlayer.getMsgId());
                    if (audioPlayer.isPlaying()) {
                        audioPlayer.pause();
                    } else {
                        if (audioPlayer.isPaused()) {
                            audioPlayer.resume();
                        } else {
                            audioPlayer.play();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music_player, menu);
        MenuItem menuItem = menu.getItem(0);

        if (isHaveBackgroundImage) {
            menuItem.setIcon(R.mipmap.ic_playlist_white);
        } else {
            menuItem.setIcon(R.mipmap.ic_playlist_grey);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_playlist:
                if (isBackOnTouchList) {
                    SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                    sharedMediaManager.setCurrentPage(SharedMediaActivity.SHARED_AUDIO);
                    supportFinishAfterTransition();
                    overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                } else {
                    Intent intent = new Intent(this, SharedMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("selectedPage", SharedMediaActivity.SHARED_AUDIO);
                    bundle.putLong("chatId", chatId);
                    bundle.putBoolean("openPlayerByBack", true);

                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
                break;
            case R.id.action_equalizer_player:
                AudioPlayer audioPlayer = AudioPlayer.getPlayer();

                if (audioPlayer.getMediaPlayer() != null && audioPlayer.getEqualizer() != null) {
                    Intent intent = new Intent(this, EqualizerActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                } else {
                    AndroidUtil.showToastShort("Equalizer not available in stop mode! Please start to play music!");
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateRepeat() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        if (isHaveBackgroundImage) {
            if (!audioPlayer.isRepeat())
                a_music_repeat.setImageResource(R.mipmap.ic_repeat_white);
            else
                a_music_repeat.setImageResource(R.mipmap.ic_repeat_blue);
        } else {
            if (!audioPlayer.isRepeat())
                a_music_repeat.setImageResource(R.mipmap.ic_repeat_grey);
            else
                a_music_repeat.setImageResource(R.mipmap.ic_repeat_blue);
        }
    }

    private void updateShuffled() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        if (isHaveBackgroundImage) {
            if (!audioPlayer.isShuffled())
                a_music_shuffle.setImageResource(R.mipmap.ic_shuffle_white);
            else
                a_music_shuffle.setImageResource(R.mipmap.ic_shuffle_blue);
        } else {
            if (!audioPlayer.isShuffled())
                a_music_shuffle.setImageResource(R.mipmap.ic_shuffle_grey);
            else
                a_music_shuffle.setImageResource(R.mipmap.ic_shuffle_blue);
        }
    }

    private void updateStyle() {

        updateRepeat();
        updateShuffled();

        RelativeLayout.LayoutParams albumParams = (RelativeLayout.LayoutParams) a_music_album_image.getLayoutParams();
        if (isHaveBackgroundImage) {
            albumParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            albumParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            albumParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            albumParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
        } else {
            albumParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            albumParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            albumParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            albumParams.addRule(RelativeLayout.CENTER_VERTICAL);
        }
    }


    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        toolbarOne = (Toolbar) findViewById(R.id.a_actionBarOne);
        toolbarTwo = (Toolbar) findViewById(R.id.a_actionBarTwo);
        updateToolBarStyle();
    }

    @SuppressWarnings("ConstantConditions")
    private void updateToolBarStyle() {
        if (isHaveBackgroundImage) {
            toolbarOne.setVisibility(View.VISIBLE);
            toolbarOne.setNavigationIcon(R.mipmap.ic_back);
            toolbarTwo.setVisibility(View.GONE);
            setSupportActionBar(toolbarOne);
        } else {
            toolbarOne.setVisibility(View.GONE);
            toolbarTwo.setNavigationIcon(R.mipmap.ic_back_grey);
            toolbarTwo.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbarTwo);
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };

        toolbarTwo.setNavigationOnClickListener(onClickListener);
        toolbarOne.setNavigationOnClickListener(onClickListener);
    }


    @Override
    public void onBackPressed() {
        UpdateHandler.getHandler().deleteObserver(this);

        if (isBackOnTouchList) {
            AudioPlayer audioPlayer = AudioPlayer.getPlayer();
            audioPlayer.setIsNeedUpdateSharedAudioActivity(true);
            supportFinishAfterTransition();
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        UpdateHandler.getHandler().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_MUSIC_PLAYER: {
                    checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG: {
                    isHaveBackgroundImage = false;
                    Object[] objects = ((Object[]) nObject.getWhat());
                    AudioPlayer audioPlayer = AudioPlayer.getPlayer();
                    TdApi.Audio audio = ((TdApi.Audio) objects[0]);
                    int msgId = ((int) objects[1]);
                    boolean isPressedPlay = ((boolean) objects[2]);

                    if (msgId == audioPlayer.getMsgId()) {
                        a_music_play.setTag(msgId);
                        getPhoto(audio);
                    }
                    if (isPressedPlay) {
                        if (audioPlayer.getMsgId() == msgId) {
                            audioPlayer.play();
                        }
                    }
                    break;
                }
            }
        }
    }

    public void checkUpdate(final Object[] objects) {
        int action = (int) objects[0];
        switch (action) {
            case AudioPlayer.ACTION_HIDE_BAR:
                updateAll(objects);
                break;
            case AudioPlayer.ACTION_SHOW_BAR:
                updateAll(objects);
                break;
            case AudioPlayer.ACTION_PROGRESS:
                updateAll(objects);
                break;
            case AudioPlayer.ACTION_UPLOAD_NEW_TRACK:
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
                        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) objects[1];
                        float progress = (float) objects[2];
                        loadedMessage = (TdApi.Message) objects[3];

                        if (TextUtil.isBlank(messageAudio.audio.title)) {
                            messageAudio.audio.title = AndroidUtil.getResourceString(R.string.unknown);
                        }

                        if (TextUtil.isBlank(messageAudio.audio.performer)) {
                            messageAudio.audio.performer = AndroidUtil.getResourceString(R.string.unknown);
                        }

                        a_music_name.setText(messageAudio.audio.title);
                        a_music_author.setText(messageAudio.audio.performer);

                        isHaveBackgroundImage = false;
                        updateToolBarStyle();
                        updateStyle();
                        supportInvalidateOptionsMenu();
                        a_music_album_image.setImageResource(R.mipmap.ic_nocover);
                        DeterminateProgressDrawable progressDrawable = a_music_play.getProgressDrawable();

                        if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                            a_music_play.setTag(audioPlayer.getMsgId());
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_AUDIO_PLAYER,
                                    messageAudio.audio.audio.id, -1, loadedMessage.id, a_music_play, messageAudio.audio,
                                    a_music_play.getTag().toString(), null, a_music_play);
                            progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                        }
                    }
                });
                break;
        }
    }

    private void updateAll(Object[] objects) {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) objects[1];

        String name = a_music_name.getText().toString();
        String author = a_music_author.getText().toString();

        if (TextUtil.isBlank(name) || !author.equals(messageAudio.audio.performer) || !name.equals(messageAudio.audio.title)) {
            a_music_name.setText(messageAudio.audio.title);
            a_music_author.setText(messageAudio.audio.performer);
        }

        DeterminateProgressDrawable.PlayStatus playStatus = null;
        DeterminateProgressDrawable determinateProgressDrawable = a_music_play.getProgressDrawable();
        float progress = (float) objects[2];
        if (progress != -1f)
            updateDuration(messageAudio, progress, false);
        if (audioPlayer.isPlaying()) {
            playStatus = DeterminateProgressDrawable.PlayStatus.PAUSE;
        } else {
            playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
        }

        if (determinateProgressDrawable.getPlayStatus() != playStatus) {
            determinateProgressDrawable.setMainSettings(
                    null, playStatus,
                    DeterminateProgressDrawable.ColorRange.BLUE,
                    LoadingContentType.AUDIO, true, false);
            determinateProgressDrawable.setVisibility(true);
            determinateProgressDrawable.invalidate();
        }
    }
}
