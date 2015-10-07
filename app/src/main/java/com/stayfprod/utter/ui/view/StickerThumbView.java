package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojiConstants;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.StickerRecentManager;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

import org.drinkless.td.libcore.telegram.TdApi;

public class StickerThumbView extends View implements ImageUpdatable {

    private static final String LOG = StickerThumbView.class.getSimpleName();

    private volatile BitmapDrawable bitmapDrawable;
    private static Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private TdApi.Sticker sticker;

    static {
        paint.setARGB(0, 0, 0, 0);
        paint.setAlpha(0);
        paint.setStyle(Paint.Style.STROKE);
    }

    public StickerThumbView(Context context) {
        super(context);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (sticker != null && FileUtils.isTDFileLocal(sticker.sticker)) {
                        ChatManager manager = ChatManager.getManager();
                        TdApi.InputMessageContent msg = manager.createStickerMsg(sticker.sticker.path);
                        manager.sendMessage(msg);
                        StickerRecentManager.getInstance().addRecentSticker(sticker);
                    } else {
                        if (sticker != null && FileUtils.isTDFileEmpty(sticker.sticker) && sticker.sticker.id > 0) {
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.USER_STICKER,
                                    sticker.sticker.id, Integer.valueOf(StickerThumbView.this.getTag().toString()), -1, sticker, v, v.getTag().toString(), 0, 0);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG, "setOnClickListener", e);
                    Crashlytics.logException(e);
                }
            }
        });

    }

    public StickerThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmapDrawable == null) {
            canvas.drawRect(0, 0, EmojiConstants.STICKER_THUMB_WIDTH, EmojiConstants.STICKER_THUMB_HEIGHT, paint);
        } else {
            bitmapDrawable.draw(canvas);
        }
    }

    public void setSticker(TdApi.Sticker sticker) {
        this.sticker = sticker;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(EmojiConstants.STICKER_THUMB_WIDTH, EmojiConstants.STICKER_THUMB_HEIGHT);
    }

    public void setStickerDrawable(BitmapDrawable bitmapDrawable, boolean... invalidate) {
        this.bitmapDrawable = bitmapDrawable;
        //if (invalidate.length > 0 && invalidate[0])
        invalidate();
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        this.bitmapDrawable = bitmapDrawable;
        postInvalidate();
    }
}
