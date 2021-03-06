package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.StickerRecentManager;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class StickerThumbView extends View implements ImageUpdatable {

    private static final String LOG = StickerThumbView.class.getSimpleName();
    private static final Paint PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    private volatile BitmapDrawable mBitmapDrawable;
    private TdApi.Sticker mSticker;

    static {
        PAINT.setARGB(0, 0, 0, 0);
        PAINT.setAlpha(0);
        PAINT.setStyle(Paint.Style.STROKE);
    }

    public StickerThumbView(Context context) {
        super(context);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mSticker != null && FileUtil.isTDFileLocal(mSticker.sticker)) {
                        ChatManager manager = ChatManager.getManager();
                        TdApi.InputMessageContent msg = manager.createStickerMsg(mSticker.sticker.path);
                        manager.sendMessage(msg);
                        StickerRecentManager.getInstance().addRecentSticker(mSticker);
                    } else {
                        if (mSticker != null && FileUtil.isTDFileEmpty(mSticker.sticker) && mSticker.sticker.id > 0) {
                            FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.USER_STICKER,
                                    mSticker.sticker.id, Integer.valueOf(StickerThumbView.this.getTag().toString()), -1, mSticker, v, v.getTag().toString(), 0, 0);
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
        if (mBitmapDrawable == null) {
            canvas.drawRect(0, 0, EmojConstant.sStickerThumbWidth, EmojConstant.sStickerThumbHeight, PAINT);
        } else {
            mBitmapDrawable.draw(canvas);
        }
    }

    public void setSticker(TdApi.Sticker sticker) {
        this.mSticker = sticker;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(EmojConstant.sStickerThumbWidth, EmojConstant.sStickerThumbHeight);
    }

    public void setStickerDrawable(BitmapDrawable bitmapDrawable, boolean... invalidate) {
        this.mBitmapDrawable = bitmapDrawable;
        //if (invalidate.length > 0 && invalidate[0])
        invalidate();
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable bitmapDrawable, boolean... animated) {
        this.mBitmapDrawable = bitmapDrawable;
        postInvalidate();
    }
}
