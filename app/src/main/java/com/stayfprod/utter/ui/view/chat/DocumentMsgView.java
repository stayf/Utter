package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.chat.DocumentMsg;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class DocumentMsgView extends AbstractMsgView<DocumentMsg> implements ImageUpdatable {

    public static final TextPaint FILE_NAME_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    public static final TextPaint FILE_SIZE_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public static final int FILE_MARGIN_LEFT = Constant.DP_10;
    public static final int FILE_SIZE_MARGIN_TOP = Constant.DP_2;

    public static final float
            FILE_NAME_START_Y,
            FILE_SIZE_START_Y;

    static {
        FILE_NAME_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        FILE_NAME_PAINT.setTextSize(Constant.DP_15);
        FILE_NAME_PAINT.setColor(0xFF333333);

        FILE_SIZE_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        FILE_SIZE_PAINT.setTextSize(Constant.DP_13);
        FILE_SIZE_PAINT.setColor(0xFFb2b2b2);

        Paint.FontMetrics namePaintFontMetrics = FILE_NAME_PAINT.getFontMetrics();
        FILE_NAME_START_Y = 0 + (namePaintFontMetrics.descent - namePaintFontMetrics.ascent + namePaintFontMetrics.leading);

        Paint.FontMetrics sizePaintFontMetrics = FILE_SIZE_PAINT.getFontMetrics();
        FILE_SIZE_START_Y = 0 + (FILE_NAME_START_Y + FILE_SIZE_MARGIN_TOP + sizePaintFontMetrics.descent - sizePaintFontMetrics.ascent + sizePaintFontMetrics.leading);
    }

    private DeterminateProgressDrawable progressDrawable;

    private String storageFileSize;

    public int[] getMaxTextWidths() {
        return record.maxFileTextWidth;
    }

    public DocumentMsgView(Context context) {
        super(context);
        progressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                DocumentMsgView.this.invalidate();
            }
        };
    }

    @Override
    public boolean isClickOnActionButton(View view, MotionEvent event) {
        Rect bounds = progressDrawable.getBounds();
        return (event.getX() >= bounds.left + getSubContainerMarginLeft(record)
                && event.getX() <= bounds.right + getSubContainerMarginLeft(record)
                && event.getY() >= bounds.top + getSubContainerMarginTop(record)
                && event.getY() <= bounds.bottom + getSubContainerMarginTop(record));
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        if (isIgnoreEvent || isClickOnActionButton(view, event)) {
            final TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) record.tgMessage.message;
            switch (progressDrawable.getLoadStatus()) {
                case NO_LOAD:
                    if (FileUtils.isTDFileEmpty(messageDocument.document.document)) {
                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.DOCUMENT,
                                messageDocument.document.document.id, -1, record.tgMessage.id, DocumentMsgView.this, messageDocument.document, getItemViewTag());
                        progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                        if (!isIgnoreEvent)
                            ChatManager.getManager().pressOnSameFiles(messageDocument, messageDocument.document.document.id, viewHolder.getLayoutPosition());
                    }
                    break;
                case PAUSE:
                    if (FileUtils.isTDFileEmpty(messageDocument.document.document)) {
                        FileManager.getManager().proceedLoad(messageDocument.document.document.id, record.tgMessage.id, !isIgnoreEvent);
                        progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                        if (!isIgnoreEvent)
                            ChatManager.getManager().pressOnSameFiles(messageDocument, messageDocument.document.document.id, viewHolder.getLayoutPosition());
                    }
                    break;
                case PROCEED_LOAD:
                    if (FileUtils.isTDFileEmpty(messageDocument.document.document)) {
                        FileManager.getManager().cancelDownloadFile(messageDocument.document.document.id, record.tgMessage.id, !isIgnoreEvent);
                        progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                        if (!isIgnoreEvent)
                            ChatManager.getManager().pressOnSameFiles(messageDocument, messageDocument.document.document.id, viewHolder.getLayoutPosition());
                    }
                    break;
                case LOADED:
                    //открываем
                    if (FileUtils.isTDFileLocal(messageDocument.document.document)) {
                        ChatHelper.openFile(messageDocument.document.document.path, messageDocument.document.mimeType, getContext());
                    }
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public void setValues(DocumentMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);

        final TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) record.tgMessage.message;

        DeterminateProgressDrawable.ColorRange colorRange = null;
        DeterminateProgressDrawable.LoadStatus loadStatus;
        boolean isHaveBackground = true;
        storageFileSize = null;

        if (FileUtils.isTDFileEmpty(messageDocument.document.thumb.photo)) {
            if (messageDocument.document.thumb.photo.id != 0) {
                colorRange = DeterminateProgressDrawable.ColorRange.BLACK;
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.DOCUMENT_THUMB,
                        messageDocument.document.thumb.photo.id, i, record.tgMessage.id, this, messageDocument.document.thumb, getItemViewTag());
            } else {
                colorRange = DeterminateProgressDrawable.ColorRange.BLUE;
                isHaveBackground = false;
            }
        } else {
            colorRange = DeterminateProgressDrawable.ColorRange.BLACK;
            progressDrawable.setBackgroundImage(FileManager.getManager().getNoResizeBitmapCircleProgressBar(messageDocument.document.thumb.photo.path, this, getItemViewTag()));
        }

        int processLoad = -1;

        if (FileUtils.isTDFileLocal(messageDocument.document.document)) {
            loadStatus = DeterminateProgressDrawable.LoadStatus.LOADED;
        } else {
            FileManager fileManager = FileManager.getManager();
            //проверка не грузится ли сейчас
            if (fileManager.isHaveStorageObjectByFileID(messageDocument.document.document.id, record.tgMessage.id)) {
                FileManager.StorageObject storageObject =
                        fileManager.updateStorageObjectAsync(FileManager.TypeLoad.DOCUMENT,
                                messageDocument.document.document.id, record.tgMessage.id, this, messageDocument.document, getItemViewTag());
                if (storageObject == null) {
                    loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                } else {
                    StringBuffer loadMsg = storageObject.loadMsg[getOrientatedIndex()];
                    //fixme не понятно по каким причинам Null
                    if (loadMsg != null) {
                        storageFileSize = loadMsg.toString();
                    }

                    if (storageObject.isCanceled) {
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PAUSE;
                    } else {
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD;
                    }
                    processLoad = storageObject.processLoad;
                }
            } else {
                loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
            }
        }

        progressDrawable.setMainSettings(loadStatus, colorRange,
                LoadingContentType.DOCUMENT, true, isHaveBackground);
        progressDrawable.setBounds(0, 0);
        progressDrawable.setVisibility(true);

        if (processLoad != -1) {
            progressDrawable.setProgressWithAnimation(processLoad);
        }
        invalidate();
    }

    public void setFileSize(StringBuffer[] fileSize) {
        record.fileSize[0] = fileSize[0].toString();
        record.fileSize[1] = fileSize[1].toString();
        storageFileSize = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = getOrientatedIndex();

        progressDrawable.draw(canvas);

        if (progressDrawable.isHaveBackground()) {
            canvas.translate(DeterminateProgressDrawable.PROGRESS_BAR_IMAGE_SIZE + FILE_MARGIN_LEFT, 0);
        } else {
            canvas.translate(DeterminateProgressDrawable.CIRCLE_SIZE + FILE_MARGIN_LEFT, 0);
        }

        String fileSize = storageFileSize != null ? storageFileSize : record.fileSize[i];

        canvas.drawText(record.fileName[i], 0, record.fileName[i].length(), 0, FILE_NAME_START_Y, FILE_NAME_PAINT);
        canvas.drawText(fileSize, 0, fileSize.length(), 0, FILE_SIZE_START_Y, FILE_SIZE_PAINT);
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return progressDrawable;
    }

    public static void measure(DocumentMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) message;
        chatMessage.text = new SpannableString(ChatHelper.getFileName(messageDocument.document.fileName));
        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(DocumentMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            final TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) record.tgMessage.message;

            boolean isHaveBackground = true;
            if (FileUtils.isTDFileEmpty(messageDocument.document.thumb.photo)) {
                if (messageDocument.document.thumb.photo.id == 0) {
                    isHaveBackground = false;
                }
            }

            int progressSize = (isHaveBackground ? DeterminateProgressDrawable.PROGRESS_BAR_IMAGE_SIZE : DeterminateProgressDrawable.CIRCLE_SIZE);
            record.maxFileTextWidth[i] = windowWidth - getSubContainerMarginLeft(record) - SUB_CONTAINER_MARGIN_RIGHT - FILE_MARGIN_LEFT - progressSize;

            record.fileName[i] = TextUtils.ellipsize(record.text, FILE_NAME_PAINT, record.maxFileTextWidth[i], TextUtils.TruncateAt.END).toString();

            //fixme
            if (FileUtils.isTDFileLocal(messageDocument.document.document)) {
                record.fileSize[i] = TextUtils.ellipsize(ChatHelper.getFileSize(messageDocument.document.document.size),
                        FILE_NAME_PAINT, record.maxFileTextWidth[i], TextUtils.TruncateAt.END).toString();
            } else {
                record.fileSize[i] = TextUtils.ellipsize(ChatHelper.getFileSize(messageDocument.document.document.size),
                        FILE_NAME_PAINT, record.maxFileTextWidth[i], TextUtils.TruncateAt.END).toString();
            }

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

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        progressDrawable.setBackgroundImage(bitmapDrawable);
        postInvalidate();
    }
}
