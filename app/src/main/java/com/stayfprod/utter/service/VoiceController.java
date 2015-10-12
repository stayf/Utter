package com.stayfprod.utter.service;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.chat.VoiceMsgView;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("JniMissingFunction")
public class VoiceController extends Observable {

    private static final String LOG = VoiceController.class.getSimpleName();
    private static volatile VoiceController voiceController;

    //cd C:\work\Utter\app\src\main
    //javah -d jni -classpath F:\Work\android\sdk\platforms\android-22/android.jar;../../build/intermediates/classes/debug com.stayfprod.utter.service.VoiceController

    static {
        try {
            System.loadLibrary("voice");
        } catch (Exception e) {
            Log.e(LOG, "Can't find audio lib", e);
            Crashlytics.logException(e);
        }
    }

    private volatile AudioTrack mAudioTrack;
    private volatile int mDuration;
    private volatile VoiceMsgView mVoiceMsgView;
    private int mBufferSize;
    private long mPcmDuration;
    private String mPlayingFile;
    private AtomicBoolean mIsPlaying = new AtomicBoolean(false);
    private AtomicBoolean mFinishedProcess = new AtomicBoolean(true);

    private volatile boolean mIsRecording;
    private ByteBuffer mRecordVoiceBuffer = ByteBuffer.allocateDirect(1920);
    private AudioRecord mVoiceRecord;
    private long mStartRecordTime;
    private int mMinRecordBufferSize;
    private boolean mIsNeedSendRecord;
    private int mMsgId;
    private volatile boolean mRecordBlocker;

    @Override
    public boolean hasChanged() {
        return true;
    }

    public VoiceController() {
        mBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize, AudioTrack.MODE_STREAM);
    }

    private native int isOpusFile(String s);

    private native int openOpusFile(String s);

    private native void closeOpusFile();

    private native void readOpusFile(ByteBuffer byteBuffer, int i);

    private native int getFinished();

    private native int getSize();

    private native long getPcmOffset();

    private native long getTotalPcmDuration();

    private native int startRecord(String path);

    private native int writeFrame(ByteBuffer frame, int len);

    private native void stopRecord();

    public static VoiceController getController() {
        if (voiceController == null) {
            synchronized (VoiceController.class) {
                if (voiceController == null) {
                    voiceController = new VoiceController();
                }
            }
        }
        return voiceController;
    }

    public boolean isPlaying() {
        return mIsPlaying.get();
    }

    public String getPlayingFile() {
        return mPlayingFile;
    }

    private void destroy() {
        mVoiceMsgView = null;
        mPlayingFile = null;
        closeOpusFile();
    }

    public void fullDestroy() {
        stop();
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                if (!mFinishedProcess.get()) {
                    while (true) {
                        if (mFinishedProcess.compareAndSet(true, false)) {
                            break;
                        }
                    }
                }
                destroy();
            }
        });
    }

    private void stop() {
        if (mAudioTrack != null) {
            mIsPlaying.set(false);
            mAudioTrack.stop();
        }
    }

    public void pause() {
        if (mAudioTrack != null) {
            mAudioTrack.pause();
            mIsPlaying.set(false);
            if (mVoiceMsgView != null)
                mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
        }
    }

    private void resume() {
        if (!mIsPlaying.get()) {
            mAudioTrack.play();
            mIsPlaying.set(true);
            if (mVoiceMsgView != null)
                mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
            processAudioAsync();
        }
    }

    private void processAudioAsync() {
        ThreadService.runSingleTaskAudio(new Runnable() {
            @Override
            public void run() {
                processAudio();
            }
        });
    }

    private void processAudioAsyncWithWaiting(final String file, final VoiceMsgView voiceMsgView, final int duration) {
        ThreadService.runSingleTaskBackground(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mFinishedProcess.compareAndSet(true, false)) {
                        break;
                    }
                }
                try {
                    final Semaphore semaphore = new Semaphore(0);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mVoiceMsgView != null) {
                                mVoiceMsgView.setProgress(0);
                                mVoiceMsgView.setTimer(ChatHelper.getDurationString(mDuration));
                                mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                            }

                            semaphore.release();
                        }
                    });
                    semaphore.acquire();
                } catch (Exception e) {
                    Log.w(LOG, "processAudioAsyncWithWaiting", e);
                }

                closeOpusFile();
                mVoiceMsgView = voiceMsgView;
                mDuration = duration;
                mPlayingFile = file;
                mMsgId = mVoiceMsgView.record.tgMessage.id;
                if (isOpusFile(file) == 1) {
                    if (openOpusFile(file) == 1) {
                        mIsPlaying.set(true);
                        mAudioTrack.play();
                        mPcmDuration = getTotalPcmDuration();
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (mVoiceMsgView != null) {
                                    mVoiceMsgView.setProgress(0);
                                    mVoiceMsgView.setMax((int) mPcmDuration);
                                    mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                                    mVoiceMsgView.setTimer(ChatHelper.getDurationString(0, mDuration));
                                }
                            }
                        });
                        processAudioAsync();
                    }
                }
            }
        });
    }

    private void processAudio() {
        mFinishedProcess.set(false);

        while (mIsPlaying.get()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(mBufferSize);
            readOpusFile(buffer, mBufferSize);
            int size = getSize();
            long pmcOffset = getPcmOffset();
            boolean isFinished = getFinished() == 1;

            if (getSize() != 0) {
                buffer.rewind();
                byte[] data = new byte[size];
                buffer.get(data);
                mAudioTrack.write(data, 0, size);
            }

            final int offset = (int) pmcOffset;
            float scale = offset / (float) mPcmDuration;
            final int progressTime = (int) (mDuration * scale);

            try {
                final Semaphore semaphore = new Semaphore(0);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceMsgView != null) {
                            mVoiceMsgView.setTimer(ChatHelper.getDurationString(progressTime, mDuration));
                            mVoiceMsgView.setProgress(offset, true);
                        }
                        semaphore.release();
                    }
                });
                semaphore.acquire();
                if (isFinished) {
                    mIsPlaying.set(false);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mVoiceMsgView != null) {
                                mVoiceMsgView.setProgress(0);
                                mVoiceMsgView.setTimer(ChatHelper.getDurationString(mDuration));
                                mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                            }
                            semaphore.release();
                        }
                    });
                    semaphore.acquire();
                    destroy();
                    break;
                }
            } catch (Exception e) {
                destroy();
                Log.w(LOG, "processAudio", e);
            }

        }
        mFinishedProcess.set(true);
    }

    private void play(String file) {
        if (isOpusFile(file) == 1) {
            if (openOpusFile(file) == 1) {
                mIsPlaying.set(true);
                mAudioTrack.play();
                mPcmDuration = getTotalPcmDuration();
                mVoiceMsgView.setProgress(0);
                mVoiceMsgView.setMax((int) mPcmDuration);
                mVoiceMsgView.setTimer(ChatHelper.getDurationString(0, mDuration));
                mVoiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                processAudioAsync();
            }
        }
    }

    public void rebuildLinks(final String file, VoiceMsgView voiceMsgView, int duration) {
        this.mVoiceMsgView = voiceMsgView;
        this.mDuration = duration;
        this.mPlayingFile = file;
        this.mVoiceMsgView.setMax((int) mPcmDuration);
    }

    public void cleanLinks(VoiceMsgView voiceMsgView) {
        if (this.mVoiceMsgView == voiceMsgView) {
            this.mVoiceMsgView = null;
        }
    }

    public int getMsgId() {
        return mMsgId;
    }

    public void startToPlayVoice(final String file, VoiceMsgView voiceMsgView, int duration) {
        AudioPlayer.getPlayer().pause();
        if (this.mVoiceMsgView == null && mPlayingFile == null) {
            this.mVoiceMsgView = voiceMsgView;
            this.mDuration = duration;
            this.mPlayingFile = file;
            this.mMsgId = mVoiceMsgView.record.tgMessage.id;
            play(file);
        } else {
            try {
                if (this.mVoiceMsgView.record.tgMessage.id == voiceMsgView.record.tgMessage.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                //
            }
            //если пришел другой трек
            stop();
            processAudioAsyncWithWaiting(file, voiceMsgView, duration);
        }
    }

    public boolean startRecordVoice() {
        if (!mRecordBlocker) {
            mRecordBlocker = true;
            final File recordFile = FileUtil.createRecordVoiceFile();
            if (startRecord(recordFile.getAbsolutePath()) == 1) {
                mMinRecordBufferSize = 3 * AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                mVoiceRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mMinRecordBufferSize);
                try {
                    mVoiceRecord.startRecording();
                    mStartRecordTime = SystemClock.uptimeMillis();
                    mIsRecording = true;
                    recordVoicePartly(recordFile);
                } catch (Exception e) {
                    Log.w(LOG, "", e);
                    mRecordBlocker = false;
                }
                return true;
            }
        }
        return false;
    }

    public void recordVoicePartly(final File recordFile) {
        ThreadService.runSingleTaskVoiceRecord(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mIsRecording) {
                        try {
                            short[] shortBuffer = new short[mMinRecordBufferSize];
                            int len = mVoiceRecord.read(shortBuffer, 0, shortBuffer.length);
                            if (len > 0) {
                                int voiceAmplitude = 0;

                                ByteBuffer tmpBuffer = ByteBuffer.wrap(AndroidUtil.shortToByteTwiddle(shortBuffer, len));
                                for (int i = 0; i < len; i++) {
                                    short s = shortBuffer[i];
                                    if (Math.abs(s) > voiceAmplitude)
                                        voiceAmplitude = Math.abs(s);
                                }

                                while (tmpBuffer.hasRemaining()) {
                                    int remLimit = -1;
                                    if (tmpBuffer.remaining() > mRecordVoiceBuffer.remaining()) {
                                        remLimit = tmpBuffer.limit();
                                        tmpBuffer.limit(mRecordVoiceBuffer.remaining() + tmpBuffer.position());
                                    }
                                    mRecordVoiceBuffer.put(tmpBuffer);
                                    if (mRecordVoiceBuffer.position() == mRecordVoiceBuffer.limit()) {
                                        int length = mRecordVoiceBuffer.limit();
                                        if (writeFrame(mRecordVoiceBuffer, length) != 0) {
                                            mRecordVoiceBuffer.rewind();
                                        }
                                    }
                                    if (remLimit != -1) {
                                        tmpBuffer.limit(remLimit);
                                    }
                                }
                                notifyObservers(new NotificationObject(NotificationObject.UPDATE_RECORD_VOICE_STATE,
                                        new Object[]{
                                                voiceAmplitude,
                                                (int) ((SystemClock.uptimeMillis() - mStartRecordTime) / 1000)}));
                            }
                        } catch (Throwable e) {
                            Crashlytics.logException(e);
                            mIsRecording = false;
                        }
                    }

                    if (mVoiceRecord != null) {
                        mVoiceRecord.release();
                        mVoiceRecord = null;
                        stopRecord();
                    }

                    //тут либо отменяем либо отсылаем полученный файл
                    if (mIsNeedSendRecord) {
                        ChatManager chatManager = ChatManager.getManager();
                        int secDuration = (int) ((SystemClock.uptimeMillis() - mStartRecordTime) / 1000);
                        if (secDuration > 1 && recordFile != null) {
                            chatManager.sendMessage(chatManager.createVoiceMsg(recordFile.getAbsolutePath(), secDuration));
                        } else {
                            if (recordFile != null) {
                                FileUtil.deleteFile(recordFile.getAbsolutePath());
                            }
                        }
                    } else {
                        if (recordFile != null) {
                            FileUtil.deleteFile(recordFile.getAbsolutePath());
                        }
                    }
                } catch (Throwable e) {
                    Crashlytics.logException(e);
                }
                mIsNeedSendRecord = false;
                mRecordBlocker = false;
            }
        });
    }

    public void stopRecordVoice(boolean isNeedSendRecord) {
        if (mIsRecording) {
            this.mIsNeedSendRecord = isNeedSendRecord;
            mIsRecording = false;
        }
    }
}
