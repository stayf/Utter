package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.StickerManager;
import com.stayfprod.utter.model.chat.StickerMsg;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class StickerMsgView extends AbstractMsgView<StickerMsg> implements ImageUpdatable {

    public static final Paint EMPTY_STICKER_PAINT = new Paint();
    public static final int MAX_STICKER_USER_HEIGHT = Constant.DP_70;
    public static final int MAX_STICKER_CHAT_HEIGHT = Constant.DP_150;

    static {
        EMPTY_STICKER_PAINT.setColor(Color.WHITE);
        EMPTY_STICKER_PAINT.setAlpha(255);
        EMPTY_STICKER_PAINT.setStyle(Paint.Style.FILL);
    }

    public static int[] calculateStickerChatSize(int w, int h) {
        int maxH = MAX_STICKER_CHAT_HEIGHT;
        if (maxH >= h) {
            return new int[]{w * h / maxH, maxH};
        } else {
            return new int[]{w * maxH / h, maxH};
        }
    }

    public static void setStickerBounds(BitmapDrawable bitmapDrawable, FileManager.TypeLoad type, int... bounds) {
        int w = 0;
        int h = 0;
        switch (type) {
            case USER_STICKER_THUMB:
                w = EmojConstant.sStickerThumbWidth;
                h = EmojConstant.sStickerThumbHeight;
                break;
            default: {
                if (bounds.length == 2) {
                    w = bounds[0];
                    h = bounds[1];
                }
            }
        }
        AndroidUtil.setCenterBoundsFullHeight(bitmapDrawable, w, h);
    }

    public static int getStickerMaxHeight(FileManager.TypeLoad type) {
        //info если 0 то вытаскиваем картинку без ресайза
        int maxHeight = 0;
        switch (type) {
            case USER_STICKER_THUMB:
                maxHeight = MAX_STICKER_USER_HEIGHT;
                break;
            case CHAT_STICKER:
                maxHeight = MAX_STICKER_CHAT_HEIGHT;
                break;
            case USER_STICKER_MICRO_THUMB:
                maxHeight = Constant.DP_40;
                break;
            case CHAT_STICKER_THUMB:
                maxHeight = 0;
                break;
        }
        return maxHeight;
    }

    private BitmapDrawable mBitmapDrawable;

    public StickerMsgView(Context context) {
        super(context);
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        return false;
    }

    @Override
    public void setValues(StickerMsg record, int i, final Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);
        mBitmapDrawable = null;

        TdApi.MessageSticker messageSticker = (TdApi.MessageSticker) record.tgMessage.message;

        if (FileUtil.isTDFileLocal(messageSticker.sticker.sticker)) {
            mBitmapDrawable = FileManager.getManager().getStickerFromFile(messageSticker.sticker.sticker.path,
                    FileManager.TypeLoad.CHAT_STICKER, this, getItemViewTag(), null, record.stickerWidth, record.stickerHeight);
        } else {
            if (FileUtil.isTDFileEmpty(messageSticker.sticker.thumb.photo)) {
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_STICKER_THUMB,
                        messageSticker.sticker.thumb.photo.id, i, record.tgMessage.id, messageSticker.sticker,
                        this, getItemViewTag(), record.stickerWidth, record.stickerHeight, messageSticker.sticker.sticker.id, i);
            } else {
                mBitmapDrawable = FileManager.getManager().getStickerFromFile(messageSticker.sticker.thumb.photo.path,
                        FileManager.TypeLoad.CHAT_STICKER_THUMB, this, getItemViewTag(), null, record.stickerWidth, record.stickerHeight);
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_STICKER,
                        messageSticker.sticker.sticker.id, i, record.tgMessage.id, messageSticker.sticker, this, getItemViewTag(), record.stickerWidth, record.stickerHeight);
            }
        }
        invalidate();
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        this.mBitmapDrawable = bitmapDrawable;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmapDrawable == null) {
            canvas.drawRect(0f, 0f, (float) record.stickerWidth, (float) record.stickerHeight, EMPTY_STICKER_PAINT);
        } else {
            mBitmapDrawable.draw(canvas);
        }
    }

    public static void measure(StickerMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageSticker messageSticker = (TdApi.MessageSticker) message;
        TdApi.File file = messageSticker.sticker.sticker;
        if (FileUtil.isTDFileLocal(file)) {
            if (messageSticker.sticker.height <= 0 || messageSticker.sticker.width <= 0) {
                List<TdApi.Sticker> stickers = StickerManager.getManager().getCachedStickers();
                for (int i = 0; i < stickers.size(); i++) {
                    TdApi.Sticker sticker = stickers.get(i);
                    //todo Индекс по стикерам!!!
                    if (sticker != null && FileUtil.isTDFileLocal(sticker.sticker)) {
                        if (sticker.sticker.path.equalsIgnoreCase(file.path)) {
                            messageSticker.sticker.height = sticker.height;
                            messageSticker.sticker.width = sticker.width;
                            int[] args = calculateStickerChatSize(messageSticker.sticker.width, messageSticker.sticker.height);
                            chatMessage.stickerWidth = args[0];
                            chatMessage.stickerHeight = args[1];
                            break;
                        }
                    }
                }
            } else {
                int[] args = calculateStickerChatSize(messageSticker.sticker.width, messageSticker.sticker.height);
                chatMessage.stickerWidth = args[0];
                chatMessage.stickerHeight = args[1];
            }
        } else {
            int[] args = calculateStickerChatSize(messageSticker.sticker.width, messageSticker.sticker.height);
            chatMessage.stickerWidth = args[0];
            chatMessage.stickerHeight = args[1];
        }

        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(StickerMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            int bidderHeight = (int) (record.stickerHeight + getSubContainerMarginTop(record));
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
