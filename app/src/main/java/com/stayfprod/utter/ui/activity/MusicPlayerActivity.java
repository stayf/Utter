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
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicPlayerActivity extends AbstractActivity implements Observer {

    private boolean mIsHaveBackgroundImage = false;
    private AtomicBoolean mIsTouchedSeekByUser = new AtomicBoolean(false);
    private int mSeekProgress;

    private RelativeLayout mMusicTopLayout;
    private DetermineProgressView mMusicPlay;
    private TextView mMusicTimerGone;
    private TextView mMusicTimerLeft;
    private SeekBar mAudioSeekBar;
    private TextView mMusicAuthor;
    private TextView mMusicName;
    private ImageView mMusicAlbumImage;
    private Toolbar mToolbarOne;
    private Toolbar mToolbarTwo;
    private ImageView mMusicRepeat;
    private ImageView mMusicShuffle;
    private TdApi.Message mLoadedMessage;

    private boolean mIsBackOnTouchList;
    private long mChatId;

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
            mIsBackOnTouchList = bundle.getBoolean("isBackOnTouchList", false);
            mChatId = bundle.getLong("chatId");
        }

        mAudioSeekBar = (SeekBar) findViewById(R.id.s_audio_seekBar);
        mAudioSeekBar.setMax(1000);

        mMusicTopLayout = (RelativeLayout) findViewById(R.id.a_music_top_layout);
        final AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        SharedMediaManager.getManager().searchAudioPlayer(mChatId, audioPlayer.getAudioMessages());

        mAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSeekProgress = progress;
                    updateDuration(audioPlayer.getMessageAudio(), progress, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTouchedSeekByUser.set(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioPlayer.seekTo(mSeekProgress);
                mIsTouchedSeekByUser.set(false);
            }
        });
        mMusicPlay = (DetermineProgressView) findViewById(R.id.a_music_play);

        ImageView musicBack = (ImageView) findViewById(R.id.a_music_back);
        musicBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.clickBack();
            }
        });
        ImageView musicForward = (ImageView) findViewById(R.id.a_music_forward);
        musicForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.clickForward();
            }
        });

        mMusicAuthor = (TextView) findViewById(R.id.a_music_author);
        mMusicName = (TextView) findViewById(R.id.a_music_name);

        mMusicAuthor.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mMusicAuthor.setTextSize(18);
        mMusicAuthor.setTextColor(0xFF333333);

        mMusicName.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mMusicName.setTextSize(16);
        mMusicName.setTextColor(0xFF333333);

        mMusicTimerGone = (TextView) findViewById(R.id.a_music_timer_gone);
        mMusicTimerGone.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mMusicTimerGone.setTextSize(12);
        mMusicTimerGone.setTextColor(0xFF569ACE);

        mMusicTimerLeft = (TextView) findViewById(R.id.a_music_timer_left);
        mMusicTimerLeft.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mMusicTimerLeft.setTextSize(12);
        mMusicTimerLeft.setTextColor(0xFFB3B3B3);

        mMusicRepeat = (ImageView) findViewById(R.id.a_music_repeat);
        mMusicShuffle = (ImageView) findViewById(R.id.a_music_shuffle);


        mMusicRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isRepeat()) {
                    audioPlayer.setRepeat(false);
                } else
                    audioPlayer.setRepeat(true);
                updateRepeat();
            }
        });

        mMusicShuffle.setOnClickListener(new View.OnClickListener() {
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

        mMusicAlbumImage = (ImageView) findViewById(R.id.a_music_album_image);

        AndroidUtil.addOnGlobalLayoutListener(mMusicTopLayout, new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout.LayoutParams) mAudioSeekBar.getLayoutParams()).topMargin = mMusicTopLayout.getHeight() - AndroidUtil.dp(9);
            }
        });

        updateStyle();
        setToolbar();

        mMusicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeterminateProgressDrawable progressDrawable = mMusicPlay.getProgressDrawable();

                if (progressDrawable.getLoadStatus() != null && mLoadedMessage != null && audioPlayer.getMsgId() == mLoadedMessage.id) {
                    TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) mLoadedMessage.message;
                    mMusicPlay.setTag(audioPlayer.getMsgId());
                    switch (progressDrawable.getLoadStatus()) {
                        case NO_LOAD:
                            if (FileUtil.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_AUDIO_PLAYER,
                                        messageAudio.audio.audio.id, -1, mLoadedMessage.id, mMusicPlay, messageAudio.audio,
                                        mMusicPlay.getTag().toString(), null, mMusicPlay);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            }
                            break;
                        case PAUSE:
                            if (FileUtil.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().proceedLoad(messageAudio.audio.audio.id, mLoadedMessage.id, true);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            }
                            break;
                        case PROCEED_LOAD:
                            if (FileUtil.isTDFileEmpty(messageAudio.audio.audio)) {
                                FileManager.getManager().cancelDownloadFile(messageAudio.audio.audio.id, mLoadedMessage.id, true);
                                progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                            }
                            break;
                        case LOADED:
                            break;
                    }
                } else if (progressDrawable.getPlayStatus() != null) {
                    mMusicPlay.setTag(audioPlayer.getMsgId());
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

        if (mIsHaveBackgroundImage) {
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
                if (mIsBackOnTouchList) {
                    SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                    sharedMediaManager.setCurrentPage(SharedMediaActivity.SHARED_AUDIO);
                    supportFinishAfterTransition();
                    overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                } else {
                    Intent intent = new Intent(this, SharedMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("selectedPage", SharedMediaActivity.SHARED_AUDIO);
                    bundle.putLong("chatId", mChatId);
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


    private void getPhoto(final TdApi.Audio audio) {
        if (audio != null && FileUtil.isTDFileLocal(audio.audio) && !mIsHaveBackgroundImage) {
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
                                    mIsHaveBackgroundImage = true;
                                    mMusicAlbumImage.setImageBitmap(bitmap);
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
                                mMusicAlbumImage.setImageResource(R.mipmap.ic_nocover);
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
                            mIsHaveBackgroundImage = false;
                            updateToolBarStyle();
                            updateStyle();
                            supportInvalidateOptionsMenu();
                            mMusicAlbumImage.setImageResource(R.mipmap.ic_nocover);
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

                DeterminateProgressDrawable determinateProgressDrawable = mMusicPlay.getProgressDrawable();
                determinateProgressDrawable.setMainSettings(
                        null, DeterminateProgressDrawable.PlayStatus.PLAY,
                        DeterminateProgressDrawable.ColorRange.BLUE,
                        LoadingContentType.AUDIO, true, false);

                determinateProgressDrawable.setVisibility(true);
                determinateProgressDrawable.invalidate();

                mMusicName.setText(messageAudio.audio.title);
                mMusicAuthor.setText(messageAudio.audio.performer);
            }
        }
    }

    public void updateDuration(TdApi.MessageAudio messageAudio, float progress, boolean byUser) {
        if ((!mIsTouchedSeekByUser.get() || byUser) && messageAudio != null) {
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

            mMusicTimerGone.setText(durationStrGone);
            mMusicTimerLeft.setText(durationStrLeft);

            if (!byUser) {
                mAudioSeekBar.setProgress((int) (progress * 10));
            }
        }
    }

    private void updateRepeat() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        if (mIsHaveBackgroundImage) {
            if (!audioPlayer.isRepeat())
                mMusicRepeat.setImageResource(R.mipmap.ic_repeat_white);
            else
                mMusicRepeat.setImageResource(R.mipmap.ic_repeat_blue);
        } else {
            if (!audioPlayer.isRepeat())
                mMusicRepeat.setImageResource(R.mipmap.ic_repeat_grey);
            else
                mMusicRepeat.setImageResource(R.mipmap.ic_repeat_blue);
        }
    }

    private void updateShuffled() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        if (mIsHaveBackgroundImage) {
            if (!audioPlayer.isShuffled())
                mMusicShuffle.setImageResource(R.mipmap.ic_shuffle_white);
            else
                mMusicShuffle.setImageResource(R.mipmap.ic_shuffle_blue);
        } else {
            if (!audioPlayer.isShuffled())
                mMusicShuffle.setImageResource(R.mipmap.ic_shuffle_grey);
            else
                mMusicShuffle.setImageResource(R.mipmap.ic_shuffle_blue);
        }
    }

    private void updateStyle() {

        updateRepeat();
        updateShuffled();

        RelativeLayout.LayoutParams albumParams = (RelativeLayout.LayoutParams) mMusicAlbumImage.getLayoutParams();
        if (mIsHaveBackgroundImage) {
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
        mToolbarOne = (Toolbar) findViewById(R.id.a_actionBarOne);
        mToolbarTwo = (Toolbar) findViewById(R.id.a_actionBarTwo);
        updateToolBarStyle();
    }

    @SuppressWarnings("ConstantConditions")
    private void updateToolBarStyle() {
        if (mIsHaveBackgroundImage) {
            mToolbarOne.setVisibility(View.VISIBLE);
            mToolbarOne.setNavigationIcon(R.mipmap.ic_back);
            mToolbarTwo.setVisibility(View.GONE);
            setSupportActionBar(mToolbarOne);
        } else {
            mToolbarOne.setVisibility(View.GONE);
            mToolbarTwo.setNavigationIcon(R.mipmap.ic_back_grey);
            mToolbarTwo.setVisibility(View.VISIBLE);
            setSupportActionBar(mToolbarTwo);
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };

        mToolbarTwo.setNavigationOnClickListener(onClickListener);
        mToolbarOne.setNavigationOnClickListener(onClickListener);
    }


    @Override
    public void onBackPressed() {
        UpdateHandler.getHandler().deleteObserver(this);

        if (mIsBackOnTouchList) {
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
                    mIsHaveBackgroundImage = false;
                    Object[] objects = ((Object[]) nObject.getWhat());
                    AudioPlayer audioPlayer = AudioPlayer.getPlayer();
                    TdApi.Audio audio = ((TdApi.Audio) objects[0]);
                    int msgId = ((int) objects[1]);
                    boolean isPressedPlay = ((boolean) objects[2]);

                    if (msgId == audioPlayer.getMsgId()) {
                        mMusicPlay.setTag(msgId);
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
                        mLoadedMessage = (TdApi.Message) objects[3];

                        if (TextUtil.isBlank(messageAudio.audio.title)) {
                            messageAudio.audio.title = AndroidUtil.getResourceString(R.string.unknown);
                        }

                        if (TextUtil.isBlank(messageAudio.audio.performer)) {
                            messageAudio.audio.performer = AndroidUtil.getResourceString(R.string.unknown);
                        }

                        mMusicName.setText(messageAudio.audio.title);
                        mMusicAuthor.setText(messageAudio.audio.performer);

                        mIsHaveBackgroundImage = false;
                        updateToolBarStyle();
                        updateStyle();
                        supportInvalidateOptionsMenu();
                        mMusicAlbumImage.setImageResource(R.mipmap.ic_nocover);
                        DeterminateProgressDrawable progressDrawable = mMusicPlay.getProgressDrawable();

                        if (FileUtil.isTDFileEmpty(messageAudio.audio.audio)) {
                            mMusicPlay.setTag(audioPlayer.getMsgId());
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_AUDIO_PLAYER,
                                    messageAudio.audio.audio.id, -1, mLoadedMessage.id, mMusicPlay, messageAudio.audio,
                                    mMusicPlay.getTag().toString(), null, mMusicPlay);
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

        String name = mMusicName.getText().toString();
        String author = mMusicAuthor.getText().toString();

        if (TextUtil.isBlank(name) || !author.equals(messageAudio.audio.performer) || !name.equals(messageAudio.audio.title)) {
            mMusicName.setText(messageAudio.audio.title);
            mMusicAuthor.setText(messageAudio.audio.performer);
        }

        DeterminateProgressDrawable.PlayStatus playStatus = null;
        DeterminateProgressDrawable determinateProgressDrawable = mMusicPlay.getProgressDrawable();
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
