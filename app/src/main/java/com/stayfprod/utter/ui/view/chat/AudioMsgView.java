package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.chat.AudioMsg;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class AudioMsgView extends AbstractMsgView<AudioMsg> {

    private DeterminateProgressDrawable progressDrawable;

    private String storageDownloadedFileSize;

    public AudioMsgView(Context context) {
        super(context);
        progressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                AudioMsgView.this.invalidate();
            }
        };
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {

        if (isIgnoreEvent || isClickOnActionButton(view, event)) {
            final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) record.tgMessage.message;
            if (progressDrawable.getLoadStatus() != null) {
                switch (progressDrawable.getLoadStatus()) {
                    case NO_LOAD:
                        if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.AUDIO,
                                    messageAudio.audio.audio.id, -1, record.tgMessage.id, AudioMsgView.this, messageAudio.audio, getItemViewTag());
                            progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.audio.audio.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PAUSE:
                        if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                            FileManager.getManager().proceedLoad(messageAudio.audio.audio.id, record.tgMessage.id, !isIgnoreEvent);
                            progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.audio.audio.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PROCEED_LOAD:
                        if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                            FileManager.getManager().cancelDownloadFile(messageAudio.audio.audio.id, record.tgMessage.id, !isIgnoreEvent);
                            progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageAudio, messageAudio.audio.audio.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case LOADED:
                        /*if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
                            ChatHelper.openFile(messageAudio.audio.audio.path, messageAudio.audio.mimeType, getContext());
                        }*/
                        break;
                }
            }

            if (progressDrawable.getPlayStatus() != null) {
                AudioPlayer player = AudioPlayer.getPlayer();
                switch (progressDrawable.getPlayStatus()) {
                    case PLAY:
                        if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
                            player.startToPlayAudio(record.tgMessage, AudioMsgView.this);
                        }
                        break;
                    case PAUSE:
                        player.pause();
                        break;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public void setValues(AudioMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);
        storageDownloadedFileSize = null;

        final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) record.tgMessage.message;

        DeterminateProgressDrawable.LoadStatus loadStatus = null;
        DeterminateProgressDrawable.PlayStatus playStatus = null;

        int processLoad = -1;
        AudioPlayer player = AudioPlayer.getPlayer();
        if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
            if (player.getPlayingFile().equals(messageAudio.audio.audio.path) && player.getMsgId() == record.tgMessage.id) {
                if (player.isPlaying()) {
                    playStatus = DeterminateProgressDrawable.PlayStatus.PAUSE;
                } else {
                    playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
                }
                player.rebuildLinks(record.tgMessage, this);
            } else {
                player.cleanLinks(this);
                playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
            }
        } else {
            FileManager fileManager = FileManager.getManager();

            if (fileManager.isHaveStorageObjectByFileID(messageAudio.audio.audio.id, record.tgMessage.id)) {
                FileManager.StorageObject storageObject = fileManager.updateStorageObjectAsync(FileManager.TypeLoad.AUDIO,
                        messageAudio.audio.audio.id, record.tgMessage.id, this, messageAudio.audio, getItemViewTag());
                if (storageObject != null) {
                    if (storageObject.isCanceled)
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PAUSE;
                    else
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD;
                    processLoad = storageObject.processLoad;
                } else {
                    loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                }
            } else {
                loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
            }
            player.cleanLinks(this);
        }

        progressDrawable.setMainSettings(loadStatus, playStatus, DeterminateProgressDrawable.ColorRange.BLUE,
                LoadingContentType.AUDIO, true, false);
        progressDrawable.setBounds(0, 0);
        progressDrawable.setVisibility(true);

        if (processLoad != -1) {
            progressDrawable.setProgressWithAnimation(processLoad);
        }
        invalidate();
    }

    @Override
    public boolean isClickOnActionButton(View view, MotionEvent event) {
        Rect bounds = progressDrawable.getBounds();
        return (event.getX() >= bounds.left + getSubContainerMarginLeft(record)
                && event.getX() <= bounds.right + getSubContainerMarginLeft(record)
                && event.getY() >= bounds.top + getSubContainerMarginTop(record)
                && event.getY() <= bounds.bottom + getSubContainerMarginTop(record));
    }

    public void setSubTitleSize(StringBuffer[] fileSubTitle) {
        record.fileSubTitle[0] = fileSubTitle[0].toString();
        record.fileSubTitle[1] = fileSubTitle[1].toString();
        storageDownloadedFileSize = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int i = getOrientatedIndex();

        progressDrawable.draw(canvas);

        int fileMarginLeft = DocumentMsgView.FILE_MARGIN_LEFT;
        TextPaint titlePaint = DocumentMsgView.FILE_NAME_PAINT;
        TextPaint subTitlePaint = DocumentMsgView.FILE_SIZE_PAINT;

        float fileTitleStartY = DocumentMsgView.FILE_NAME_START_Y;
        float fileSubTitleStartY = DocumentMsgView.FILE_SIZE_START_Y;

        if (progressDrawable.isHaveBackground()) {
            canvas.translate(DeterminateProgressDrawable.PROGRESS_BAR_IMAGE_SIZE + fileMarginLeft, 0);
        } else {
            canvas.translate(DeterminateProgressDrawable.CIRCLE_SIZE + fileMarginLeft, 0);
        }

        String fileSubTitle = storageDownloadedFileSize != null ? storageDownloadedFileSize : record.fileSubTitle[i];

        canvas.drawText(record.fileTitle[i], 0, record.fileTitle[i].length(), 0, fileTitleStartY, titlePaint);
        canvas.drawText(fileSubTitle, 0, fileSubTitle.length(), 0, fileSubTitleStartY, subTitlePaint);
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return progressDrawable;
    }

    public static void measure(AudioMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) message;

        if (TextUtil.isBlank(messageAudio.audio.performer)) {
            messageAudio.audio.performer = AndroidUtil.getResourceString(R.string.unknown);
        }

        if (TextUtil.isBlank(messageAudio.audio.title)) {
            messageAudio.audio.title = AndroidUtil.getResourceString(R.string.unknown);
        }

        measureByOrientation(chatMessage);
    }

    public int[] getMaxTextWidths() {
        return record.maxFileTextWidth;
    }

    public static void measureByOrientation(AudioMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) record.tgMessage.message;

            int progressSize = DeterminateProgressDrawable.CIRCLE_SIZE;
            int fileMarginLeft = DocumentMsgView.FILE_MARGIN_LEFT;

            TextPaint titlePaint = DocumentMsgView.FILE_NAME_PAINT;
            TextPaint subTitlePaint = DocumentMsgView.FILE_SIZE_PAINT;

            record.maxFileTextWidth[i] = windowWidth - getSubContainerMarginLeft(record) - SUB_CONTAINER_MARGIN_RIGHT - fileMarginLeft - progressSize;
            record.fileTitle[i] = TextUtils.ellipsize(messageAudio.audio.performer, titlePaint, record.maxFileTextWidth[i], TextUtils.TruncateAt.END).toString();
            record.fileSubTitle[i] = TextUtils.ellipsize(messageAudio.audio.title, subTitlePaint, record.maxFileTextWidth[i], TextUtils.TruncateAt.END).toString();

            int bidderHeight = (int) (progressSize + getSubContainerMarginTop(record));

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
