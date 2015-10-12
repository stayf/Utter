package com.stayfprod.utter.ui.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.util.TextUtil;
import com.stayfprod.utter.util.AndroidUtil;

public class IconDrawable extends Drawable {

    private static Paint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private static int CHAT_LIST_MARGIN_TOP = Constant.DP_10;
    public static int CHAT_LIST_MARGIN_LEFT = Constant.DP_10;

    private static int CHAT_MARGIN_TOP = Constant.DP_2;
    public static int CHAT_MARGIN_LEFT = Constant.DP_10;

    static {
        paintText.setTextSize(Constant.DP_17);
        paintText.setColor(Color.WHITE);
        paintText.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
    }

    public boolean emptyBitmap;
    public Paint mPaint;
    public String text;

    private RectF mRect;
    private float mCornerRadius;
    private int w = 0;
    private int h = 0;

    private int textX, textY;
    private IconFactory.Type type;
    private int marginTop = 0;
    private int marginLeft = 0;

    public int getMarginLeft() {
        return marginLeft;
    }

    public int getWidth() {
        if (mRect != null) {
            return (int) mRect.width();
        }
        return 0;
    }

    public int getHeight() {
        if (mRect != null) {
            return (int) mRect.height();
        }
        return 0;
    }

    private int kByteCount;

    public int getKByteCount() {
        if (kByteCount == 0) {
            return 10;
        } else {
            return kByteCount;
        }
    }

    public IconDrawable(IconFactory.Type type) {
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mRect = new RectF();
        this.mCornerRadius = (float) type.getHeight();
        this.type = type;
        this.mPaint.setColor(0x405B95C2);
        switch (type) {
            case CHAT_LIST:
                marginLeft = CHAT_LIST_MARGIN_LEFT;
                marginTop = CHAT_LIST_MARGIN_TOP;
                break;
            case CHAT:
                marginLeft = CHAT_MARGIN_LEFT;
                marginTop = CHAT_MARGIN_TOP;
                break;
        }
        this.mRect.set(marginLeft, marginTop, type.getHeight() + marginLeft, type.getHeight() + marginTop);
    }

    public IconDrawable(int color, String text, IconFactory.Type type) {
        this(type);
        initTextDrawable(color, text, type);
    }

    public IconDrawable(Bitmap bitmap, IconFactory.Type type) {
        this(type);
        initBitmapDrawable(bitmap, type);
    }

    private void initBitmapDrawable(Bitmap bitmap, IconFactory.Type type) {

        this.emptyBitmap = false;
        BitmapShader mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        kByteCount = bitmap.getByteCount() / 1000;
        if (type != IconFactory.Type.USER && type != IconFactory.Type.TITLE && type != IconFactory.Type.BOT_COMMAND) {
            Matrix matrix = new Matrix();
            RectF srcRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF dstRect = new RectF(marginLeft, marginTop, type.getHeight() + marginLeft, type.getHeight() + marginTop);
            matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER);
            mBitmapShader.setLocalMatrix(matrix);
        }

        this.h = bitmap.getHeight();
        this.w = bitmap.getWidth();
        this.mPaint.setColor(Color.WHITE);
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setShader(mBitmapShader);
    }

    private void initTextDrawable(int color, String text, IconFactory.Type type) {
        this.text = text;
        this.emptyBitmap = true;
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(color);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextAlign(Paint.Align.CENTER);
        this.mPaint.setLinearText(true);
        this.h = this.w = type.getHeight();

        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        this.textX = marginLeft + ((type.getHeight() - (int) paintText.measureText(text)) >> 1);
        this.textY = marginTop + ((type.getHeight() - (int) (fontMetrics.descent + fontMetrics.ascent)) >> 1);
    }

    @Override
    public int getIntrinsicWidth() {
        return w;
    }

    @Override
    public int getIntrinsicHeight() {
        return h;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (type == IconFactory.Type.USER || type == IconFactory.Type.TITLE || type == IconFactory.Type.BOT_COMMAND)
            this.mRect.set(0, 0, bounds.width(), bounds.height());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        if (emptyBitmap && TextUtil.isNotBlank(text))
            canvas.drawText(text, textX, textY, paintText);
    }

    @Override
    public Rect getDirtyBounds() {
        Rect out = new Rect();
        mRect.roundOut(out);
        return out;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    public boolean isEmptyBitmap() {
        return emptyBitmap;
    }
}