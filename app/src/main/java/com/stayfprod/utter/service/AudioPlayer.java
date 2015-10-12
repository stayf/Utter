package com.stayfprod.utter.service;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.DetermineProgressView;
import com.stayfprod.utter.ui.view.chat.AudioMsgView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayer extends Observable {

    private static final String LOG = AudioPlayer.class.getSimpleName();

    public static final int ACTION_SHOW_BAR = 0;
    public static final int ACTION_HIDE_BAR = 1;
    public static final int ACTION_PROGRESS = 2;
    public static final int ACTION_UPLOAD_NEW_TRACK = 3;

    private static final String REPEAT = "REPEAT";
    private static final String SHUFFLE = "SHUFFLE";
    private static final String EQUALIZER_PRESET = "EQUALIZER_PRESET";

    private static volatile AudioPlayer sAudioPlayer;

    private List<TdApi.Message> mAudioMessages = new ArrayList<TdApi.Message>(100);

    private TdApi.Message mMessage;
    private AudioMsgView mAudioMsgView;
    private DetermineProgressView mProgressView;
    private float mCurrentProgress;
    private boolean mIsNeedUpdateChatActivity;
    private boolean mIsNeedUpdateSharedAudioActivity;

    private volatile MediaPlayer mMediaPlayer;
    private volatile Equalizer mEqualizer;

    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    private volatile Visualizer mVisualizer;

    public Visualizer getVisualizer() {
        return mVisualizer;
    }

    public void setVisualizer(Visualizer mVisualizer) {
        this.mVisualizer = mVisualizer;
    }

    private volatile int currentTrackPosition = -1;

    public void setCurrentTrackPosition(int currentTrackPosition) {
        this.currentTrackPosition = currentTrackPosition;
    }

    public boolean isNeedUpdateChatActivity() {
        return mIsNeedUpdateChatActivity;
    }

    public boolean isNeedUpdateSharedAudioActivity() {
        return mIsNeedUpdateSharedAudioActivity;
    }

    public void setIsNeedUpdateSharedAudioActivity(boolean isNeedUpdateSharedAudioActivity) {
        this.mIsNeedUpdateSharedAudioActivity = isNeedUpdateSharedAudioActivity;
    }

    public void setIsNeedUpdateMessageList(boolean isNeedUpdateMessageList) {
        this.mIsNeedUpdateChatActivity = isNeedUpdateMessageList;
    }

    public List<TdApi.Message> getAudioMessages() {
        return mAudioMessages;
    }

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && isPlaying.get() && mMediaPlayer.isPlaying()) {
                isPaused.set(false);
                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                mCurrentProgress = ((float) mCurrentPosition * 100) / (mMediaPlayer.getDuration());
                notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), mCurrentProgress, getMessage()}));
                mHandler.postDelayed(this, 120);
            }
        }
    };

    public static AudioPlayer getPlayer() {
        if (sAudioPlayer == null) {
            synchronized (AudioPlayer.class) {
                if (sAudioPlayer == null) {
                    sAudioPlayer = new AudioPlayer();
                }
            }
        }
        return sAudioPlayer;
    }

    //проценты
    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    public TdApi.MessageAudio getMessageAudio() {
        try {
            return ((TdApi.MessageAudio) mMessage.message);
        } catch (Exception e) {
            return null;
        }
    }

    public TdApi.Message getMessage() {
        return mMessage;
    }

    private SharedPreferences getSharedPreferences() {
        return App.getAppContext().getSharedPreferences("Player", Context.MODE_PRIVATE);
    }

    public boolean isRepeat() {
        return getSharedPreferences().getBoolean(REPEAT, false);
    }

    public short getEqualizerPreset() {
        return (short) getSharedPreferences().getInt(EQUALIZER_PRESET, 0);
    }

    public void setEqualizerPreset(short preset) {
        getSharedPreferences().edit().putInt(EQUALIZER_PRESET, preset).commit();
    }

    public boolean isShuffled() {
        return getSharedPreferences().getBoolean(SHUFFLE, false);
    }

    public void setShuffled(boolean val) {
        getSharedPreferences().edit().putBoolean(SHUFFLE, val).commit();
    }

    public void setRepeat(boolean val) {
        getSharedPreferences().edit().putBoolean(REPEAT, val).commit();
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(isRepeat());
        }
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);

    public void rebuildLinks(TdApi.Message message, AudioMsgView audioMsgView) {
        this.mMessage = message;
        this.mAudioMsgView = audioMsgView;
    }

    public void rebuildLinks(TdApi.Message message, DetermineProgressView progressView) {
        this.mMessage = message;
        this.mProgressView = progressView;
    }

    public void cleanLinks(DetermineProgressView progressView) {
        if (this.mProgressView == progressView) {
            this.mProgressView = null;
        }
    }

    public void cleanLinks(AudioMsgView audioMsgView) {
        //может прийти одна и та же View на разные сообщения
        if (this.mAudioMsgView == audioMsgView) {
            this.mAudioMsgView = null;
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return mMediaPlayer != null && !mMediaPlayer.isPlaying() && isPaused.get();
    }

    public String getPlayingFile() {
        try {
            return ((TdApi.MessageAudio) mMessage.message).audio.audio.path;
        } catch (Exception e) {
            return "";
        }
    }

    public int getMsgId() {
        if (mMessage != null) {
            return mMessage.id;
        }
        return 0;
    }

    public void stop(boolean... isSendAction) {
        isPlaying.set(false);
        isPaused.set(false);
        try {
            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
            }
            if (mMediaPlayer != null) {
                if (mAudioMsgView != null)
                    mAudioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                if (mProgressView != null)
                    mProgressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            //
        } finally {
            releasePlayer();
        }
        if (isSendAction.length == 0 || isSendAction[0]) {
            notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_HIDE_BAR, getMessageAudio(), 0f, getMessage()}));
        }
    }

    public void seekTo(int timeInPercent) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.seekTo((int) (((float) timeInPercent / 10) * ((float) mMediaPlayer.getDuration() / 100)));
            } catch (Exception e) {
                //
            }
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            try {
                if (mAudioMsgView != null)
                    mAudioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                if (mProgressView != null)
                    mProgressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                isPlaying.set(false);
                isPaused.set(true);
                mMediaPlayer.pause();
            } catch (Exception e) {
                //
            }
        }
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), -1f, getMessage()}));
    }

    public void resume() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.start();
                if (mAudioMsgView != null)
                    mAudioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                if (mProgressView != null)
                    mProgressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                isPlaying.set(true);
                mRunnable.run();
            } catch (Exception e) {
                //
            }
        }
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), -1f, getMessage()}));
    }


    public void play(final boolean... isSendAction) {
        try {
            VoiceController.getController().pause();
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getPlayingFile());

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    if (mEqualizer == null) {
                        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
                        mEqualizer.setEnabled(true);
                        mEqualizer.usePreset(getEqualizerPreset());
                    }

                    if (mVisualizer != null) {
                        mVisualizer.setEnabled(true);
                    }
                    isPlaying.set(true);
                    mRunnable.run();
                    if (mAudioMsgView != null)
                        mAudioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                    if (mProgressView != null)
                        mProgressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                    if (isSendAction.length == 0 || isSendAction[0]) {
                        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_SHOW_BAR, getMessageAudio(), 0f, getMessage()}));
                    }
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    AndroidUtil.showToastShort("Player error:" + what + "; " + extra);
                    return false;
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!isRepeat()) {
                        if (isShuffled()) {
                            if (!mAudioMessages.isEmpty()) {
                                try {
                                    final Random random = new Random();
                                    int pos = random.nextInt(mAudioMessages.size());
                                    TdApi.Message message = mAudioMessages.get(pos);
                                    tryStartToPlay(message);
                                    currentTrackPosition = pos;
                                } catch (Exception e) {
                                    stop();
                                }
                            }
                        } else {
                            if (mAudioMessages != null && mAudioMessages.size() > currentTrackPosition + 1) {
                                try {
                                    int pos = currentTrackPosition + 1;
                                    TdApi.Message message = mAudioMessages.get(pos);
                                    tryStartToPlay(message);
                                    currentTrackPosition = pos;
                                } catch (Exception e) {
                                    stop();
                                }
                            } else {
                                stop();
                            }
                        }
                    }
                }
            });

            mMediaPlayer.setLooping(isRepeat());
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(LOG, "play", e);
            Crashlytics.logException(e);
        }
    }

    private void tryStartToPlay(TdApi.Message message) throws Exception {
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) message.message;

        if (TextUtil.isBlank(messageAudio.audio.title)) {
            messageAudio.audio.title = AndroidUtil.getResourceString(R.string.unknown);
        }

        if (TextUtil.isBlank(messageAudio.audio.performer)) {
            messageAudio.audio.performer = AndroidUtil.getResourceString(R.string.unknown);
        }

        if (FileUtil.isTDFileLocal(messageAudio.audio.audio)) {
            //играем
            mProgressView = null;
            mAudioMsgView = null;

            if (mMediaPlayer == null) {
                AudioPlayer.this.mIsNeedUpdateChatActivity = true;
                AudioPlayer.this.mIsNeedUpdateSharedAudioActivity = true;
                AudioPlayer.this.mMessage = message;
                play();
            } else {
                mMediaPlayer.reset();
                AudioPlayer.this.mIsNeedUpdateChatActivity = true;
                AudioPlayer.this.mIsNeedUpdateSharedAudioActivity = true;
                AudioPlayer.this.mMessage = message;
                mMediaPlayer.setDataSource(getPlayingFile());
                mMediaPlayer.prepareAsync();
            }

            AudioPlayer.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG, new Object[]{messageAudio.audio, message.id, false}));
        } else {
            //грузим
            stop();
            AudioPlayer.this.mIsNeedUpdateChatActivity = true;
            AudioPlayer.this.mIsNeedUpdateSharedAudioActivity = true;
            AudioPlayer.this.mMessage = message;
            AudioPlayer.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER,
                    new Object[]{ACTION_UPLOAD_NEW_TRACK, messageAudio, 0f, message}));

        }
    }

    public void clickForward() {
        try {
            if (currentTrackPosition != -1) {
                if (mAudioMessages.size() > currentTrackPosition + 1) {
                    currentTrackPosition = currentTrackPosition + 1;
                    tryStartToPlay(mAudioMessages.get(currentTrackPosition));
                } else {
                    if (!mAudioMessages.isEmpty()) {
                        currentTrackPosition = 0;
                        tryStartToPlay(mAudioMessages.get(0));
                    }
                }
            } else {
                if (!mAudioMessages.isEmpty()) {
                    currentTrackPosition = 0;
                    tryStartToPlay(mAudioMessages.get(0));
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    public void clickBack() {
        try {
            if (currentTrackPosition != -1) {
                if (!mAudioMessages.isEmpty()) {
                    int newPos = currentTrackPosition - 1;
                    if (newPos >= 0) {
                        if (mAudioMessages.size() > newPos) {
                            currentTrackPosition = newPos;
                            tryStartToPlay(mAudioMessages.get(currentTrackPosition));
                        } else {
                            currentTrackPosition = mAudioMessages.size() - 1;
                            tryStartToPlay(mAudioMessages.get(currentTrackPosition));
                        }
                    } else {
                        currentTrackPosition = mAudioMessages.size() - 1;
                        tryStartToPlay(mAudioMessages.get(currentTrackPosition));
                    }
                }
            } else {
                if (!mAudioMessages.isEmpty()) {
                    currentTrackPosition = 0;
                    tryStartToPlay(mAudioMessages.get(0));
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    public void startToPlayAudio(TdApi.Message message, DetermineProgressView progressView) {
        this.mAudioMsgView = null;
        mIsNeedUpdateChatActivity = true;
        if (this.mMessage == null) {
            this.mMessage = message;
            this.mProgressView = progressView;
            play();
        } else if (mMediaPlayer == null) {
            if (this.mMessage != message) {
                this.mMessage = message;
                this.mProgressView = progressView;
            }
            play();
        } else {
            try {
                if (this.mMessage.id == message.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                Log.w(LOG, "startToPlayAudio", e);
            }

            stop(false);
            this.mMessage = message;
            this.mProgressView = progressView;
            play(false);
        }
    }

    public void startToPlayAudio(TdApi.Message message, AudioMsgView audioMsgView) {
        this.mProgressView = null;
        if (this.mMessage == null) {
            this.mMessage = message;
            this.mAudioMsgView = audioMsgView;
            play();
        } else if (mMediaPlayer == null) {
            if (this.mMessage != message) {
                this.mMessage = message;
                this.mAudioMsgView = audioMsgView;
            }
            play();
        } else {
            try {
                if (this.mMessage.id == message.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                Log.w(LOG, "startToPlayAudio", e);
            }
            stop(false);
            this.mMessage = message;
            this.mAudioMsgView = audioMsgView;
            play(false);
        }

    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.release();
                if (mEqualizer != null) {
                    mEqualizer.release();
                }
            } catch (Exception e) {
                //
            } finally {
                mMediaPlayer = null;
                mEqualizer = null;
            }
        }
    }
}
