package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.chat.VoiceMsg;
import com.stayfprod.utter.service.VoiceController;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class VoiceMsgView extends AbstractMsgView<VoiceMsg> {

    private static final TextPaint TIMER_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LEFT_PAINT = new Paint();
    private static final Paint RIGHT_PAINT = new Paint();
    private static final GradientDrawable THUMB_DRAWABLE;

    private static final int
            SEEK_BAR_MARGIN_LEFT = Constant.DP_12,
            SEEK_BAR_MARGIN_TOP = Constant.DP_23,
            SEEK_BAR_HEIGHT = Constant.DP_2,
            THUMB_HEIGHT = Constant.DP_16,
            TIMER_MARGIN_LEFT = Constant.DP_12,
            TIMER_MARGIN_TOP = Constant.DP_12;

    private static final float TIMER_START_Y;
    private static final float SEEK_BAR_START_X;
    private static final float SEEK_BAR_START_Y;
    private static final int THUMB_START_Y;

    static {
        THUMB_DRAWABLE = (GradientDrawable) FileUtil.decodeResource(R.drawable.audio_progress_thumb);

        LEFT_PAINT.setColor(0xFF68ade1);
        RIGHT_PAINT.setColor(0xFFDCEBF5);

        TIMER_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        TIMER_PAINT.setTextSize(Constant.DP_13);
        TIMER_PAINT.setColor(0xFF68ade1);

        Paint.FontMetrics timerPaintFontMetrics = TIMER_PAINT.getFontMetrics();
        TIMER_START_Y = TIMER_MARGIN_TOP + (timerPaintFontMetrics.descent - timerPaintFontMetrics.ascent + timerPaintFontMetrics.leading);
        SEEK_BAR_START_X = DeterminateProgressDrawable.CIRCLE_SIZE + SEEK_BAR_MARGIN_LEFT;
        SEEK_BAR_START_Y = SEEK_BAR_MARGIN_TOP;

        THUMB_START_Y = (int) (SEEK_BAR_START_Y - ((THUMB_HEIGHT - SEEK_BAR_HEIGHT) >> 1));
    }

    private volatile boolean mIsThumbVisible;
    private volatile int mMin = 0;
    private volatile int mMax = 100;
    private volatile int mPlayProgress = 0;
    private StringBuffer mTimer = new StringBuffer(20);
    private DeterminateProgressDrawable mProgressDrawable;

    public VoiceMsgView(Context context) {
        super(context);
        mProgressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                VoiceMsgView.this.invalidate();
            }
        };
    }

    @Override
    public boolean isClickOnActionButton(View view, MotionEvent event) {
        Rect bounds = mProgressDrawable.getBounds();
        return (event.getX() >= bounds.left + getSubContainerMarginLeft(record)
                && event.getX() <= bounds.right + getSubContainerMarginLeft(record)
                && event.getY() >= bounds.top + getSubContainerMarginTop(record)
                && event.getY() <= bounds.bottom + getSubContainerMarginTop(record));
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        //info тут проверка на квадрат, но лучше сделать круг
        if (isIgnoreEvent || isClickOnActionButton(view, event)) {
            final TdApi.MessageVoice messageAudio = (TdApi.MessageVoice) record.tgMessage.message;
            if (mProgressDrawable.getLoadStatus() != null) {
                //нажать на точно такой же контент
                switch (mProgressDrawable.getLoadStatus()) {
                    case NO_LOAD:
                        if (FileUtil.isTDFileEmpty(messageAudio.voice.voice)) {
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.VOICE,
                                    messageAudio.voice.voice.id, -1, record.tgMessage.id, VoiceMsgView.this, messageAudio.voice, getItemViewTag());
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);

                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.voice.voice.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PAUSE:
                        if (FileUtil.isTDFileEmpty(messageAudio.voice.voice)) {
                            FileManager.getManager().proceedLoad(messageAudio.voice.voice.id, record.tgMessage.id, !isIgnoreEvent);
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.voice.voice.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PROCEED_LOAD:
                        if (FileUtil.isTDFileEmpty(messageAudio.voice.voice)) {
                            FileManager.getManager().cancelDownloadFile(messageAudio.voice.voice.id, record.tgMessage.id, !isIgnoreEvent);
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.voice.voice.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case LOADED:
                        break;
                }
            }
            if (mProgressDrawable.getPlayStatus() != null) {
                switch (mProgressDrawable.getPlayStatus()) {
                    case PLAY:
                        if (FileUtil.isTDFileLocal(messageAudio.voice.voice)) {
                            VoiceController.getController().startToPlayVoice(messageAudio.voice.voice.path, VoiceMsgView.this, messageAudio.voice.duration);
                        }
                        break;
                    case PAUSE:
                        VoiceController.getController().pause();
                        break;
                }
            }
            return true;
        }
        return false;
    }

    public void setThumbVisible(boolean isThumbVisible) {
        this.mIsThumbVisible = isThumbVisible;
    }

    public void setProgress(int val, boolean... invalidate) {
        this.mPlayProgress = val;
        if (invalidate.length > 0 && invalidate[0]) {
            invalidate();
        }
    }

    public void setTimer(String timer, boolean... invalidate) {
        this.mTimer.setLength(0);
        this.mTimer.append(timer);
        if (invalidate.length > 0 && invalidate[0]) {
            invalidate();
        }
    }

    public void setMax(int val) {
        this.mMax = val;
    }

    @Override
    public void setValues(VoiceMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);

        final TdApi.MessageVoice messageVoice = (TdApi.MessageVoice) record.tgMessage.message;

        DeterminateProgressDrawable.LoadStatus loadStatus = null;
        DeterminateProgressDrawable.PlayStatus playStatus = null;
        setTimer(record.duration);
        int processLoad = -1;
        VoiceController player = VoiceController.getController();
        if (FileUtil.isTDFileLocal(messageVoice.voice.voice)) {
            mIsThumbVisible = true;
            if (player.isPlaying() && player.getPlayingFile().equals(messageVoice.voice.voice.path) && player.getMsgId() == record.tgMessage.id) {
                player.rebuildLinks(messageVoice.voice.voice.path, this, messageVoice.voice.duration);
                playStatus = DeterminateProgressDrawable.PlayStatus.PAUSE;
                //info здесь можно сделать получение текущего прогресса из плеера
            } else {
                player.cleanLinks(this);
                mPlayProgress = 0;
                playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
            }
        } else {
            mPlayProgress = 0;
            mIsThumbVisible = false;
            FileManager fileManager = FileManager.getManager();

            if (fileManager.isHaveStorageObjectByFileID(messageVoice.voice.voice.id, record.tgMessage.id)) {
                FileManager.StorageObject storageObject =
                        fileManager.updateStorageObjectAsync(FileManager.TypeLoad.VOICE,
                                messageVoice.voice.voice.id, record.tgMessage.id, this, messageVoice.voice, getItemViewTag());
                if (storageObject != null) {
                    if (storageObject.isCanceled)
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PAUSE;
                    else
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD;
                    processLoad = storageObject.processLoad;
                }else {
                    loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                }
            } else {
                loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
            }
            player.cleanLinks(this);
        }

        mProgressDrawable.setMainSettings(loadStatus, playStatus, DeterminateProgressDrawable.ColorRange.BLUE,
                LoadingContentType.AUDIO, true, false);
        mProgressDrawable.setBounds(0, 0);
        mProgressDrawable.setVisibility(true);

        if (processLoad != -1) {
            mProgressDrawable.setProgressWithAnimation(processLoad);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = getOrientatedIndex();

        mProgressDrawable.draw(canvas);

        float k = ((record.timerStartX[i] - TIMER_MARGIN_LEFT - SEEK_BAR_START_X) / mMax);
        float centerThumbX = k * mPlayProgress + SEEK_BAR_START_X;

        canvas.drawRect(SEEK_BAR_START_X, SEEK_BAR_START_Y, centerThumbX, SEEK_BAR_START_Y + SEEK_BAR_HEIGHT, LEFT_PAINT);
        canvas.drawRect(centerThumbX, SEEK_BAR_START_Y, record.timerStartX[i] - TIMER_MARGIN_LEFT, SEEK_BAR_START_Y + SEEK_BAR_HEIGHT, RIGHT_PAINT);

        int startThumbX = (int) (centerThumbX - (THUMB_HEIGHT >> 1));

        if (mIsThumbVisible) {
            THUMB_DRAWABLE.setBounds(startThumbX, THUMB_START_Y, startThumbX + THUMB_HEIGHT, THUMB_START_Y + THUMB_HEIGHT);
            THUMB_DRAWABLE.draw(canvas);
        }

        canvas.drawText(mTimer.toString(), record.timerStartX[i], TIMER_START_Y, TIMER_PAINT);
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public static void measure(VoiceMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageVoice messageVoice = (TdApi.MessageVoice) message;
        chatMessage.duration = ChatHelper.getDurationString(messageVoice.voice.duration);
        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(VoiceMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            AbstractMsgView.mainMeasure(record, i, windowWidth);
            record.timerStartX[i] = windowWidth - SUB_CONTAINER_MARGIN_RIGHT - TIMER_PAINT.measureText(record.duration) - getSubContainerMarginLeft(record);

            int bidderHeight = (int) (DeterminateProgressDrawable.CIRCLE_SIZE + getSubContainerMarginTop(record));
            measureLayoutHeight(bidderHeight, i, record);
            if (i == 0)
                measureByOrientation(record, Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, record.layoutHeight[getOrientatedIndex()]);
    }

}
