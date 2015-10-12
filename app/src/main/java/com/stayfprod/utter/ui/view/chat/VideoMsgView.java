package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.chat.VideoMsg;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class VideoMsgView extends AbstractMsgView<VideoMsg> implements ImageUpdatable {

    public static volatile int MAX_VIDEO_CHAT_WIDTH = Constant.DP_180;

    public static int[] calculateVideoThumbSize(int w, int h) {
        return new int[]{MAX_VIDEO_CHAT_WIDTH, h * MAX_VIDEO_CHAT_WIDTH / w};
    }

    private DeterminateProgressDrawable mProgressDrawable;
    private BitmapDrawable mBitmapDrawable;

    public VideoMsgView(Context context) {
        super(context);

        mProgressDrawable = new DeterminateProgressDrawable();
        mProgressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                VideoMsgView.this.invalidate();
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
            final TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) record.tgMessage.message;
            if (mProgressDrawable.getLoadStatus() != null) {
                switch (mProgressDrawable.getLoadStatus()) {
                    case NO_LOAD:
                        if (FileUtil.isTDFileEmpty(messageVideo.video.video)) {
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.VIDEO, messageVideo.video.video.id, -1, record.tgMessage.id, VideoMsgView.this, messageVideo.video, getItemViewTag());
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageVideo, messageVideo.video.video.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PAUSE:
                        if (FileUtil.isTDFileEmpty(messageVideo.video.video)) {
                            FileManager.getManager().proceedLoad(messageVideo.video.video.id, record.tgMessage.id, !isIgnoreEvent);
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageVideo, messageVideo.video.video.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case PROCEED_LOAD:
                        if (FileUtil.isTDFileEmpty(messageVideo.video.video)) {
                            FileManager.getManager().cancelDownloadFile(messageVideo.video.video.id, record.tgMessage.id, !isIgnoreEvent);
                            mProgressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                            if (!isIgnoreEvent)
                                ChatManager.getManager().pressOnSameFiles(messageVideo, messageVideo.video.video.id, viewHolder.getLayoutPosition());
                        }
                        break;
                    case LOADED:
                        break;
                }
            }

            if (mProgressDrawable.getPlayStatus() != null) {
                switch (mProgressDrawable.getPlayStatus()) {
                    case PLAY:
                        if (FileUtil.isTDFileLocal(messageVideo.video.video))
                            ChatHelper.openFile(messageVideo.video.video.path, "video/*", getContext());
                        break;
                    case PAUSE:

                        break;
                }
            }
            return true;
        }
        return false;
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    @Override
    public void setValues(VideoMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);

        final TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) record.tgMessage.message;

        DeterminateProgressDrawable.LoadStatus loadStatus = null;
        DeterminateProgressDrawable.PlayStatus playStatus = null;

        if (FileUtil.isTDFileEmpty(messageVideo.video.thumb.photo)) {
            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.VIDEO_THUMB,
                    messageVideo.video.thumb.photo.id, i, record.tgMessage.id, messageVideo.video.thumb, this, getItemViewTag());
        } else {
            mBitmapDrawable = FileManager.getManager().getNoResizeBitmapFromFile(messageVideo.video.thumb.photo.path, this, getItemViewTag());
        }

        int processLoad = -1;

        if (FileUtil.isTDFileEmpty(messageVideo.video.video)) {
            FileManager fileManager = FileManager.getManager();

            if (fileManager.isHaveStorageObjectByFileID(messageVideo.video.video.id, record.tgMessage.id)) {
                FileManager.StorageObject storageObject =
                        fileManager.updateStorageObjectAsync(FileManager.TypeLoad.VIDEO,
                                messageVideo.video.video.id, record.tgMessage.id, this, messageVideo.video, getItemViewTag());
                if (storageObject != null) {
                    if (storageObject.isCanceled)
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PAUSE;
                    else
                        loadStatus = DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD;
                    processLoad = storageObject.processLoad;
                } else {
                    loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                }
            }else {
                loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
            }
        } else {
            playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
        }

        mProgressDrawable.setMainSettings(
                loadStatus, playStatus,
                DeterminateProgressDrawable.ColorRange.BLACK,
                LoadingContentType.VIDEO, true, false);

        mProgressDrawable.setBounds(record.progressStartX, record.progressStartY);
        mProgressDrawable.setVisibility(true);

        if (processLoad != -1) {
            mProgressDrawable.setProgressWithAnimation(processLoad);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmapDrawable == null) {
            canvas.drawRect(0f, 0f, (float) record.photoWidth, (float) record.photoHeight, EMPTY_PAINT);
        } else {
            mBitmapDrawable.setBounds(0, 0, record.photoWidth, record.photoHeight);
            mBitmapDrawable.draw(canvas);
        }
        mProgressDrawable.draw(canvas);
    }

    public static void measure(VideoMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) message;
        int h = messageVideo.video.thumb.height;
        int w = messageVideo.video.thumb.width;

        int[] newSizeVideo = VideoMsgView.calculateVideoThumbSize(w, h);
        h = newSizeVideo[1];
        w = newSizeVideo[0];

        int cy = (h - DeterminateProgressDrawable.CIRCLE_SIZE) >> 1;
        int cx = (w - DeterminateProgressDrawable.CIRCLE_SIZE) >> 1;

        chatMessage.photoWidth = w;
        chatMessage.photoHeight = h;

        chatMessage.progressStartX = cx;
        chatMessage.progressStartY = cy;

        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(VideoMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            int bidderHeight = (int) (record.photoHeight + getSubContainerMarginTop(record));

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
        this.mBitmapDrawable = bitmapDrawable;
        postInvalidate();
    }
}
