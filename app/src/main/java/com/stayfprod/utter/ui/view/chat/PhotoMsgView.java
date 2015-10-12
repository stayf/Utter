package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.chat.PhotoMsg;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class PhotoMsgView extends AbstractMsgView<PhotoMsg> implements ImageUpdatable {

    public static final int MAX_IMAGE_CHAT_WIDTH = Constant.DP_180;

    private DeterminateProgressDrawable mProgressDrawable;
    private BitmapDrawable mBitmapDrawable;

    public PhotoMsgView(Context context) {
        super(context);
        mProgressDrawable = new DeterminateProgressDrawable() {
            @Override
            public void invalidate() {
                PhotoMsgView.this.invalidate();
            }
        };
    }

    public static int[] calculateSize(int w, int h, int W) {
        if (W >= w) {
            //не трогаем
            return new int[]{w, h};
        } else {
            return new int[]{W, h * W / w};
        }
    }

    public static int[] calculateImageSize(int w, int h) {
        return calculateSize(w, h, MAX_IMAGE_CHAT_WIDTH);
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        return false;
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        this.mBitmapDrawable = bitmapDrawable;
        postInvalidate();
    }

    public DeterminateProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public boolean isLocalMsg() {
        return record.tgMessage.id > ChatHelper.LOCAL_MSG_ID_MIN;
    }

    @Override
    public void setValues(PhotoMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);

        mBitmapDrawable = null;

        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) record.tgMessage.message;
        final TdApi.PhotoSize[] photoSizes = messagePhoto.photo.photos;
        //todo не хватает установки координат для прогресика при отправки фото
        final TdApi.PhotoSize photoSize = photoSizes[record.photoIndex];

        int processLoad = -1;

        DeterminateProgressDrawable.LoadStatus loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;

        if (FileUtil.isTDFileLocal(photoSize.photo)) {
            mBitmapDrawable = FileManager.getManager().getImageFromFile(photoSize.photo.path, this, getItemViewTag());
            mProgressDrawable.clean();
        } else {
            mProgressDrawable.clean();
            //фамб существует
            if (record.thumbIndex != -1) {
                TdApi.PhotoSize photoSizeThumb = photoSizes[record.thumbIndex];
                if (FileUtil.isTDFileEmpty(photoSizeThumb.photo)) {
                    //скачиваем фамб, а потом сразу полную картинку, если позиция видна
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.PHOTO_THUMB,
                            photoSizeThumb.photo.id, i, record.tgMessage.id, this, photoSizeThumb, photoSize.photo.id, photoSize, i, getItemViewTag());
                } else {
                    //у нас есть фамб но нет картинки, показываем фамб, картинку на кач
                    mBitmapDrawable = FileManager.getManager().getNoResizeBitmapFromFile(photoSizeThumb.photo.path, this, getItemViewTag());
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.PHOTO,
                            photoSize.photo.id, i, record.tgMessage.id, this, photoSize, getItemViewTag());
                }
            } else {
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.PHOTO,
                        photoSize.photo.id, i, record.tgMessage.id, this, photoSize, getItemViewTag());
            }
        }

        mProgressDrawable.setMainSettings(
                loadStatus, null,
                DeterminateProgressDrawable.ColorRange.BLACK,
                LoadingContentType.PHOTO, true, false);

        mProgressDrawable.setBounds(record.progressStartX, record.progressStartY);
        mProgressDrawable.setVisibility(false);

        //todo нужно ложить файл в отдельный список на загрузку в сеть
        if (processLoad != -1) {
            mProgressDrawable.setProgressWithForceAnimation(processLoad);
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

    public static void measure(PhotoMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message;
        TdApi.PhotoSize[] photoSizes = messagePhoto.photo.photos;
        //ищим s либо a
        int a = -1;
        int s = -1;
        int prevIteration = 0;
        for (int j = 0; j < photoSizes.length; j++) {
            TdApi.PhotoSize photoSize = photoSizes[j];
            if (photoSize.type != null && photoSize.type.equals("s")) {
                s = j;
            }
            if (photoSize.type != null && photoSize.type.equals("a")) {
                a = j;
            }
            if (photoSize.width > AndroidUtil.WINDOW_PORTRAIT_WIDTH) {
                break;
            }
            prevIteration = j;
        }
        chatMessage.thumbIndex = a != -1 ? a : s;

        final TdApi.PhotoSize photoSize = photoSizes[prevIteration];
        if (photoSize.width == 0 || photoSize.height == 0) {
            //если сообщение я отправил

            if (FileUtil.isTDFileLocal(photoSize.photo)) {
                //читаем размеры из файла
                int[] size = FileUtil.readFileSize(photoSize.photo.path);
                int[] newSize = PhotoMsgView.calculateImageSize(size[1], size[0]);
                photoSize.height = size[0];
                photoSize.width = size[1];
                chatMessage.photoWidth = newSize[0];
                chatMessage.photoHeight = newSize[1];
            }
        } else {
            int[] newSize = PhotoMsgView.calculateImageSize(photoSize.width, photoSize.height);
            chatMessage.photoWidth = newSize[0];
            chatMessage.photoHeight = newSize[1];
        }

        chatMessage.photoIndex = prevIteration;
        int cy = (chatMessage.photoHeight - DeterminateProgressDrawable.CIRCLE_SIZE) >> 1;
        int cx = (chatMessage.photoWidth - DeterminateProgressDrawable.CIRCLE_SIZE) >> 1;
        chatMessage.progressStartX = cx;
        chatMessage.progressStartY = cy;

        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(PhotoMsg record, int... orientation) {
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
}
