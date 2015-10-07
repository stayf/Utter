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
import com.stayfprod.utter.util.FileUtils;

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

    @Override
    public boolean hasChanged() {
        return true;
    }

    public VoiceController() {
        bufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
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

    private volatile AudioTrack audioTrack;
    private volatile int duration;

    private volatile VoiceMsgView voiceMsgView;

    private int bufferSize;
    private long pcmDuration;
    private String playingFile;

    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean finishedProcess = new AtomicBoolean(true);

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
        return isPlaying.get();
    }

    public String getPlayingFile() {
        return playingFile;
    }

    private void destroy() {
        voiceMsgView = null;
        playingFile = null;
        closeOpusFile();
        //todo може release тут?
    }

    public void fullDestroy() {
        stop();
        ThreadService.runTaskBackground(new Runnable() {
            @Override
            public void run() {
                if (!finishedProcess.get()) {
                    while (true) {
                        if (finishedProcess.compareAndSet(true, false)) {
                            break;
                        }
                    }
                }
                destroy();
            }
        });
    }

    private void stop() {
        if (audioTrack != null) {
            isPlaying.set(false);
            audioTrack.stop();
        }
    }

    public void pause() {
        if (audioTrack != null) {
            audioTrack.pause();
            isPlaying.set(false);
            if (voiceMsgView != null)
                voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
        }
    }

    private void resume() {
        if (!isPlaying.get()) {
            audioTrack.play();
            isPlaying.set(true);
            if (voiceMsgView != null)
                voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
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

    private void processAudioAsyncWithWaiting(final String file, final VoiceMsgView _voiceMsgView, final int _duration) {
        ThreadService.runSingleTaskBackground(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (finishedProcess.compareAndSet(true, false)) {
                        break;
                    }
                }
                try {
                    final Semaphore semaphore = new Semaphore(0);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (voiceMsgView != null) {
                                voiceMsgView.setProgress(0);
                                voiceMsgView.setTimer(ChatHelper.getDurationString(duration));
                                voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
                            }

                            semaphore.release();
                        }
                    });
                    semaphore.acquire();
                } catch (Exception e) {
                    Log.w(LOG, "processAudioAsyncWithWaiting", e);
                }

                closeOpusFile();
                voiceMsgView = _voiceMsgView;
                duration = _duration;
                playingFile = file;
                msgId = voiceMsgView.record.tgMessage.id;
                if (isOpusFile(file) == 1) {
                    if (openOpusFile(file) == 1) {
                        isPlaying.set(true);
                        audioTrack.play();
                        pcmDuration = getTotalPcmDuration();
                        AndroidUtil.runInUI(new Runnable() {
                            @Override
                            public void run() {
                                if (voiceMsgView != null) {
                                    voiceMsgView.setProgress(0);
                                    voiceMsgView.setMax((int) pcmDuration);
                                    voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                                    voiceMsgView.setTimer(ChatHelper.getDurationString(0, duration));
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
        finishedProcess.set(false);

        while (isPlaying.get()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            readOpusFile(buffer, bufferSize);
            int size = getSize();
            long pmcOffset = getPcmOffset();
            boolean isFinished = getFinished() == 1;

            if (getSize() != 0) {
                buffer.rewind();
                byte[] data = new byte[size];
                buffer.get(data);
                audioTrack.write(data, 0, size);
            }

            final int offset = (int) pmcOffset;
            float scale = offset / (float) pcmDuration;
            final int progressTime = (int) (duration * scale);

            try {
                final Semaphore semaphore = new Semaphore(0);
                AndroidUtil.runInUI(new Runnable() {
                    @Override
                    public void run() {
                        if (voiceMsgView != null) {
                            voiceMsgView.setTimer(ChatHelper.getDurationString(progressTime, duration));
                            voiceMsgView.setProgress(offset, true);
                        }
                        semaphore.release();
                    }
                });
                semaphore.acquire();
                if (isFinished) {
                    isPlaying.set(false);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (voiceMsgView != null) {
                                voiceMsgView.setProgress(0);
                                voiceMsgView.setTimer(ChatHelper.getDurationString(duration));
                                voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PLAY);
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
        finishedProcess.set(true);
    }

    private void play(String file) {
        if (isOpusFile(file) == 1) {
            if (openOpusFile(file) == 1) {
                isPlaying.set(true);
                audioTrack.play();
                pcmDuration = getTotalPcmDuration();
                voiceMsgView.setProgress(0);
                voiceMsgView.setMax((int) pcmDuration);
                voiceMsgView.setTimer(ChatHelper.getDurationString(0, duration));
                voiceMsgView.getProgressDrawable().changePlayStatusAndUpdate(DeterminateProgressDrawable.PlayStatus.PAUSE);
                processAudioAsync();
            }
        }
    }

    public void rebuildLinks(final String file, VoiceMsgView _voiceMsgView, int _duration) {
        this.voiceMsgView = _voiceMsgView;
        this.duration = _duration;
        this.playingFile = file;
        this.voiceMsgView.setMax((int) pcmDuration);
    }

    public void cleanLinks(VoiceMsgView _voiceMsgView) {
        if (this.voiceMsgView == _voiceMsgView) {
            this.voiceMsgView = null;
        }
    }

    public int getMsgId() {
        return msgId;
    }

    public void startToPlayVoice(final String file, VoiceMsgView _voiceMsgView, int _duration) {
        AudioPlayer.getPlayer().pause();
        if (this.voiceMsgView == null && playingFile == null) {
            this.voiceMsgView = _voiceMsgView;
            this.duration = _duration;
            this.playingFile = file;
            this.msgId = voiceMsgView.record.tgMessage.id;
            play(file);
        } else {
            try {
                if (this.voiceMsgView.record.tgMessage.id == _voiceMsgView.record.tgMessage.id) {
                    resume();
                    return;
                }
            } catch (Exception e) {
                //
            }
            //если пришел другой трек
            stop();
            processAudioAsyncWithWaiting(file, _voiceMsgView, _duration);
        }
    }

    private volatile boolean isRecording;

    private ByteBuffer recordVoiceBuffer = ByteBuffer.allocateDirect(1920);
    private AudioRecord voiceRecord;
    private long startRecordTime;
    private int minRecordBufferSize;
    private boolean isNeedSendRecord;
    private int msgId;
    private volatile boolean recordBlocker;

    public boolean startRecordVoice() {
        if (!recordBlocker) {
            recordBlocker = true;
            final File recordFile = FileUtils.createRecordVoiceFile();
            if (startRecord(recordFile.getAbsolutePath()) == 1) {
                minRecordBufferSize = 3 * AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                voiceRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minRecordBufferSize);
                try {
                    voiceRecord.startRecording();
                    startRecordTime = SystemClock.uptimeMillis();
                    isRecording = true;
                    recordVoicePartly(recordFile);
                } catch (Exception e) {
                    //FileUtils.deleteFile(recordFile.getAbsolutePath());
                    Log.w(LOG, "", e);
                    recordBlocker = false;
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
                    while (isRecording) {
                        try {
                            short[] shortBuffer = new short[minRecordBufferSize];
                            int len = voiceRecord.read(shortBuffer, 0, shortBuffer.length);
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
                                    if (tmpBuffer.remaining() > recordVoiceBuffer.remaining()) {
                                        remLimit = tmpBuffer.limit();
                                        tmpBuffer.limit(recordVoiceBuffer.remaining() + tmpBuffer.position());
                                    }
                                    recordVoiceBuffer.put(tmpBuffer);
                                    if (recordVoiceBuffer.position() == recordVoiceBuffer.limit()) {
                                        int length = recordVoiceBuffer.limit();
                                        if (writeFrame(recordVoiceBuffer, length) != 0) {
                                            recordVoiceBuffer.rewind();
                                        }
                                    }
                                    if (remLimit != -1) {
                                        tmpBuffer.limit(remLimit);
                                    }
                                }
                                notifyObservers(new NotificationObject(NotificationObject.UPDATE_RECORD_VOICE_STATE,
                                        new Object[]{
                                                voiceAmplitude,
                                                (int) ((SystemClock.uptimeMillis() - startRecordTime) / 1000)}));
                            }
                        } catch (Throwable e) {
                            Crashlytics.logException(e);
                            isRecording = false;
                        }
                    }

                    if (voiceRecord != null) {
                        //fixme java.lang.IllegalStateException: stop() called on an uninitialized AudioRecord.
                        //http://stackoverflow.com/questions/4843739/audiorecord-object-not-initializing
                        //voiceRecord.stop();
                        voiceRecord.release();
                        voiceRecord = null;
                        stopRecord();
                    }

                    //тут либо отменяем либо отсылаем полученный файл
                    if (isNeedSendRecord) {
                        ChatManager chatManager = ChatManager.getManager();
                        int secDuration = (int) ((SystemClock.uptimeMillis() - startRecordTime) / 1000);
                        if (secDuration > 1 && recordFile != null) {
                            chatManager.sendMessage(chatManager.createVoiceMsg(recordFile.getAbsolutePath(), secDuration));
                        } else {
                            if (recordFile != null) {
                                FileUtils.deleteFile(recordFile.getAbsolutePath());
                            }
                        }
                    } else {
                        if (recordFile != null) {
                            FileUtils.deleteFile(recordFile.getAbsolutePath());
                        }
                    }
                } catch (Throwable e) {
                    Crashlytics.logException(e);
                }
                isNeedSendRecord = false;
                recordBlocker = false;
            }
        });
    }

    public void stopRecordVoice(boolean isNeedSendRecord) {
        if (isRecording) {
            this.isNeedSendRecord = isNeedSendRecord;
            isRecording = false;
        }
    }
}
