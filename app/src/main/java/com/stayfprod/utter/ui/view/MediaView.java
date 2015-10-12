package com.stayfprod.utter.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.SharedMedia;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class MediaView extends View implements ImageUpdatable {

    private static final Paint DRAW_BITMAP_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final TextPaint TEXT_PAINT = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
    private static final BitmapDrawable PLAY_ICON;

    static {
        PLAY_ICON = FileUtil.decodeImageResource(R.mipmap.ic_playsm);
        TEXT_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        TEXT_PAINT.setColor(0xFFFFFFFF);
        TEXT_PAINT.setTextSize(Constant.DP_10);
    }

    private BitmapDrawable mBitmapDrawable;
    public SharedMedia sharedMedia;

    public MediaView(Context context) {
        super(context);
        init();
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void setSharedMedia(SharedMedia sharedMedia, int i, final Context context) {
        this.sharedMedia = sharedMedia;
        this.setTag(i);
        setLocalBitmapDrawable(null);

        switch (this.sharedMedia.message.message.getConstructor()) {
            case TdApi.MessageVideo.CONSTRUCTOR:
                TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) this.sharedMedia.message.message;
                if (FileUtil.isTDFileEmpty(messageVideo.video.thumb.photo)) {
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_VIDEO_THUMB,
                            messageVideo.video.thumb.photo.id, i, this.sharedMedia.message.id, messageVideo.video.thumb, this, getItemViewTag());
                } else {
                    setLocalBitmapDrawable(FileManager.getManager().getNoResizeBitmapFromFile(messageVideo.video.thumb.photo.path, this, getItemViewTag()));
                }
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) this.sharedMedia.message.message;
                final TdApi.PhotoSize[] photoSizes = messagePhoto.photo.photos;
                final TdApi.PhotoSize photoSize = photoSizes[this.sharedMedia.photoIndex];

                if (FileUtil.isTDFileLocal(photoSize.photo)) {
                    setLocalBitmapDrawable(FileManager.getManager().getImageFromFile(photoSize.photo.path, this, getItemViewTag()));
                } else {
                    //фамб существует
                    if (this.sharedMedia.thumbIndex != -1) {
                        TdApi.PhotoSize photoSizeThumb = photoSizes[this.sharedMedia.thumbIndex];
                        if (FileUtil.isTDFileEmpty(photoSizeThumb.photo)) {
                            //скачиваем фамб, а потом сразу полную картинку, если позиция видна
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_PHOTO_THUMB,
                                    photoSizeThumb.photo.id, i, this.sharedMedia.message.id, this, photoSizeThumb, photoSize.photo.id, photoSize, i, getItemViewTag());
                        } else {
                            //у нас есть фамб но нет картинки, показываем фамб, картинку на кач
                            setLocalBitmapDrawable(FileManager.getManager().getNoResizeBitmapFromFile(photoSizeThumb.photo.path, this, getItemViewTag()));
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_PHOTO,
                                    photoSize.photo.id, i, this.sharedMedia.message.id, this, photoSize, getItemViewTag());
                        }
                    } else {
                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_PHOTO,
                                photoSize.photo.id, i, this.sharedMedia.message.id, this, photoSize, getItemViewTag());
                    }
                }
                break;
        }
        invalidate();
    }

    private void setLocalBitmapDrawable(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            this.mBitmapDrawable = bitmapDrawable;
        } else {
            this.mBitmapDrawable = null;
        }
    }

    public String getItemViewTag() {
        Object tag = this.getTag();
        return tag != null ? tag.toString() : "";
    }

    public void setBitmapDrawable(BitmapDrawable bitmapDrawable) {
        this.mBitmapDrawable = bitmapDrawable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.mBitmapDrawable != null) {

            AndroidUtil.setCropBounds(this.mBitmapDrawable, getWidth());
            this.mBitmapDrawable.draw(canvas);

            if (sharedMedia.isVideo) {
                int endIconX = getWidth() - Constant.DP_8;
                int startIconX = endIconX - PLAY_ICON.getIntrinsicWidth();
                int startIconY = Constant.DP_8;
                int endIconY = startIconY + PLAY_ICON.getIntrinsicHeight();
                PLAY_ICON.setBounds(startIconX, startIconY, endIconX, endIconY);
                PLAY_ICON.draw(canvas);

                if (TextUtil.isNotBlank(sharedMedia.videoTime)) {
                    float startTextX = startIconX - Constant.DP_8 - TEXT_PAINT.measureText(sharedMedia.videoTime);
                    canvas.drawText(sharedMedia.videoTime, startTextX, Constant.DP_19, TEXT_PAINT);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        setLocalBitmapDrawable(bitmapDrawable);
        postInvalidate();
    }
}
