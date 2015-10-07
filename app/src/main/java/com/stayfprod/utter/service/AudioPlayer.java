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
import com.stayfprod.utter.util.FileUtils;
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

    private List<TdApi.Message> audioMessages = new ArrayList<TdApi.Message>(100);

    private static volatile AudioPlayer audioPlayer;

    private TdApi.Message message;
    private AudioMsgView audioMsgView;
    private DetermineProgressView progressView;
    private float currentProgress;
    private boolean isNeedUpdateChatActivity;
    private boolean isNeedUpdateSharedAudioActivity;

    private volatile MediaPlayer mediaPlayer;
    private volatile Equalizer mEqualizer;

    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
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
        return isNeedUpdateChatActivity;
    }

    public boolean isNeedUpdateSharedAudioActivity() {
        return isNeedUpdateSharedAudioActivity;
    }

    public void setIsNeedUpdateSharedAudioActivity(boolean isNeedUpdateSharedAudioActivity) {
        this.isNeedUpdateSharedAudioActivity = isNeedUpdateSharedAudioActivity;
    }

    public void setIsNeedUpdateMessageList(boolean isNeedUpdateMessageList) {
        this.isNeedUpdateChatActivity = isNeedUpdateMessageList;
    }

    public List<TdApi.Message> getAudioMessages() {
        return audioMessages;
    }

    private Handler mHandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying.get() && mediaPlayer.isPlaying()) {
                isPaused.set(false);
                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                currentProgress = ((float) mCurrentPosition * 100) / (mediaPlayer.getDuration());
                notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), currentProgress, getMessage()}));
                mHandler.postDelayed(this, 120);
            }
        }
    };

    public static AudioPlayer getPlayer() {
        if (audioPlayer == null) {
            synchronized (AudioPlayer.class) {
                if (audioPlayer == null) {
                    audioPlayer = new AudioPlayer();
                }
            }
        }
        return audioPlayer;
    }

    //проценты
    public float getCurrentProgress() {
        return currentProgress;
    }

    public TdApi.MessageAudio getMessageAudio() {
        try {
            return ((TdApi.MessageAudio) message.message);
        } catch (Exception e) {
            return null;
        }
    }

    public TdApi.Message getMessage() {
        return message;
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
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isRepeat());
        }
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);

    public void rebuildLinks(TdApi.Message message, AudioMsgView audioMsgView) {
        this.message = message;
        this.audioMsgView = audioMsgView;
    }

    public void rebuildLinks(TdApi.Message message, DetermineProgressView progressView) {
        this.message = message;
        this.progressView = progressView;
    }

    public void cleanLinks(DetermineProgressView progressView) {
        if (this.progressView == progressView) {
            this.progressView = null;
        }
    }

    public void cleanLinks(AudioMsgView audioMsgView) {
        //сожет прийти одна и та же View на разные сообщения
        if (this.audioMsgView == audioMsgView) {
            this.audioMsgView = null;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return mediaPlayer != null && !mediaPlayer.isPlaying() && isPaused.get();
    }

    public String getPlayingFile() {
        try {
            return ((TdApi.MessageAudio) message.message).audio.audio.path;
        } catch (Exception e) {
            return "";
        }
    }

    public int getMsgId() {
        if (message != null) {
            return message.id;
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
            if (mediaPlayer != null) {
                if (audioMsgView != null)
                    audioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                if (progressView != null)
                    progressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                mediaPlayer.stop();
            }
        } catch (Exception e) {
            //
        } finally {
            //audioMsgView = null;
            //messageAudio = null;
            //progressView = null
            releasePlayer();
        }
        if (isSendAction.length == 0 || isSendAction[0]) {
            notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_HIDE_BAR, getMessageAudio(), 0f, getMessage()}));
        }
    }

    public void seekTo(int timeInPercent) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo((int) (((float) timeInPercent / 10) * ((float) mediaPlayer.getDuration() / 100)));
            } catch (Exception e) {
                //
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            try {
                if (audioMsgView != null)
                    audioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                if (progressView != null)
                    progressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                isPlaying.set(false);
                isPaused.set(true);
                mediaPlayer.pause();
            } catch (Exception e) {
                //
            }
        }
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), -1f, getMessage()}));
    }

    public void resume() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.start();
                if (audioMsgView != null)
                    audioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                if (progressView != null)
                    progressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                isPlaying.set(true);
                runnable.run();
            } catch (Exception e) {
                //
            }
        }
        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_PROGRESS, getMessageAudio(), -1f, getMessage()}));
    }


    public void play(final boolean... isSendAction) {
        try {
            VoiceController.getController().pause();
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getPlayingFile());

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    if (mEqualizer == null) {
                        mEqualizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
                        mEqualizer.setEnabled(true);
                        mEqualizer.usePreset(getEqualizerPreset());
                    }

                    if (mVisualizer != null) {
                        mVisualizer.setEnabled(true);
                    }
                    isPlaying.set(true);
                    runnable.run();
                    if (audioMsgView != null)
                        audioMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                    if (progressView != null)
                        progressView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                    if (isSendAction.length == 0 || isSendAction[0]) {
                        notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_SHOW_BAR, getMessageAudio(), 0f, getMessage()}));
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    AndroidUtil.showToastShort("Player error:" + what + "; " + extra);
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!isRepeat()) {
                        if (isShuffled()) {
                            if (!audioMessages.isEmpty()) {
                                try {
                                    final Random random = new Random();
                                    int pos = random.nextInt(audioMessages.size());
                                    TdApi.Message message = audioMessages.get(pos);
                                    tryStartToPlay(message);
                                    currentTrackPosition = pos;
                                } catch (Exception e) {
                                    stop();
                                }
                            }
                        } else {
                            if (audioMessages != null && audioMessages.size() > currentTrackPosition + 1) {
                                try {
                                    int pos = currentTrackPosition + 1;
                                    TdApi.Message message = audioMessages.get(pos);
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

            mediaPlayer.setLooping(isRepeat());
            mediaPlayer.prepareAsync();
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

        if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
            //играем
            progressView = null;
            audioMsgView = null;

            if (mediaPlayer == null) {
                AudioPlayer.this.isNeedUpdateChatActivity = true;
                AudioPlayer.this.isNeedUpdateSharedAudioActivity = true;
                AudioPlayer.this.message = message;
                play();
            } else {
                mediaPlayer.reset();
                AudioPlayer.this.isNeedUpdateChatActivity = true;
                AudioPlayer.this.isNeedUpdateSharedAudioActivity = true;
                AudioPlayer.this.message = message;
                mediaPlayer.setDataSource(getPlayingFile());
                mediaPlayer.prepareAsync();
            }

            //todo если sharedActivity видна force апдейт

            AudioPlayer.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG, new Object[]{messageAudio.audio, message.id, false}));
        } else {
            //грузим
            stop();
            AudioPlayer.this.isNeedUpdateChatActivity = true;
            AudioPlayer.this.isNeedUpdateSharedAudioActivity = true;
            AudioPlayer.this.message = message;
            AudioPlayer.this.notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER,
                    new Object[]{ACTION_UPLOAD_NEW_TRACK, messageAudio, 0f, message}));

        }
    }

    public void clickForward() {
        try {
            if (currentTrackPosition != -1) {
                if (audioMessages.size() > currentTrackPosition + 1) {
                    currentTrackPosition = currentTrackPosition + 1;
                    tryStartToPlay(audioMessages.get(currentTrackPosition));
                } else {
                    if (!audioMessages.isEmpty()) {
                        currentTrackPosition = 0;
                        tryStartToPlay(audioMessages.get(0));
                    }
                }
            } else {
                if (!audioMessages.isEmpty()) {
                    currentTrackPosition = 0;
                    tryStartToPlay(audioMessages.get(0));
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    public void clickBack() {
        try {
            if (currentTrackPosition != -1) {
                if (!audioMessages.isEmpty()) {
                    int newPos = currentTrackPosition - 1;
                    if (newPos >= 0) {
                        if (audioMessages.size() > newPos) {
                            currentTrackPosition = newPos;
                            tryStartToPlay(audioMessages.get(currentTrackPosition));
                        } else {
                            currentTrackPosition = audioMessages.size() - 1;
                            tryStartToPlay(audioMessages.get(currentTrackPosition));
                        }
                    } else {
                        currentTrackPosition = audioMessages.size() - 1;
                        tryStartToPlay(audioMessages.get(currentTrackPosition));
                    }
                }
            } else {
                if (!audioMessages.isEmpty()) {
                    currentTrackPosition = 0;
                    tryStartToPlay(audioMessages.get(0));
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    public void startToPlayAudio(TdApi.Message message, DetermineProgressView progressView) {
        this.audioMsgView = null;
        isNeedUpdateChatActivity = true;
        if (this.message == null) {
            this.message = message;
            this.progressView = progressView;
            play();
        } else if (mediaPlayer == null) {
            if (this.message != message) {
                this.message = message;
                this.progressView = progressView;
            }
            play();
        } else {
            try {
                if (this.message.id == message.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                Log.w(LOG, "startToPlayAudio", e);
            }

            stop(false);
            this.message = message;
            this.progressView = progressView;
            play(false);
            //notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_UPLOAD_NEW_TRACK, getMessageAudio(), 0f, getMessage()}));
        }
    }

    public void startToPlayAudio(TdApi.Message message, AudioMsgView audioMsgView) {
        this.progressView = null;
        if (this.message == null) {
            this.message = message;
            this.audioMsgView = audioMsgView;
            play();
        } else if (mediaPlayer == null) {
            if (this.message != message) {
                this.message = message;
                this.audioMsgView = audioMsgView;
            }
            play();
        } else {
            try {
                if (this.message.id == message.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                Log.w(LOG, "startToPlayAudio", e);
            }
            stop(false);
            this.message = message;
            this.audioMsgView = audioMsgView;
            play(false);
            //notifyObservers(new NotificationObject(NotificationObject.UPDATE_MUSIC_PLAYER, new Object[]{ACTION_UPLOAD_NEW_TRACK, getMessageAudio(), 0f, getMessage()}));
        }

    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                if (mEqualizer != null) {
                    mEqualizer.release();
                }
            } catch (Exception e) {
                //
            } finally {
                mediaPlayer = null;
                mEqualizer = null;
            }
        }
    }
}
