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

    private static final Paint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private static final int CHAT_LIST_MARGIN_TOP = Constant.DP_10;
    public static final int CHAT_LIST_MARGIN_LEFT = Constant.DP_10;

    private static final int CHAT_MARGIN_TOP = Constant.DP_2;
    public static final int CHAT_MARGIN_LEFT = Constant.DP_10;

    static {
        paintText.setTextSize(Constant.DP_17);
        paintText.setColor(Color.WHITE);
        paintText.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
    }

    public boolean emptyBitmap;
    public Paint paint;
    public String text;

    private RectF mRect;
    private float mCornerRadius;
    private int mW = 0;
    private int mH = 0;

    private int mTextX, mTextY;
    private IconFactory.Type mType;
    private int mMarginTop = 0;
    private int mMarginLeft = 0;
    private int mKbyteCount;

    public int getMarginLeft() {
        return mMarginLeft;
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

    public int getKByteCount() {
        if (mKbyteCount == 0) {
            return 10;
        } else {
            return mKbyteCount;
        }
    }

    public IconDrawable(IconFactory.Type type) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mRect = new RectF();
        this.mCornerRadius = (float) type.getHeight();
        this.mType = type;
        this.paint.setColor(0x405B95C2);
        switch (type) {
            case CHAT_LIST:
                mMarginLeft = CHAT_LIST_MARGIN_LEFT;
                mMarginTop = CHAT_LIST_MARGIN_TOP;
                break;
            case CHAT:
                mMarginLeft = CHAT_MARGIN_LEFT;
                mMarginTop = CHAT_MARGIN_TOP;
                break;
        }
        this.mRect.set(mMarginLeft, mMarginTop, type.getHeight() + mMarginLeft, type.getHeight() + mMarginTop);
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
        mKbyteCount = bitmap.getByteCount() / 1000;
        if (type != IconFactory.Type.USER && type != IconFactory.Type.TITLE && type != IconFactory.Type.BOT_COMMAND) {
            Matrix matrix = new Matrix();
            RectF srcRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF dstRect = new RectF(mMarginLeft, mMarginTop, type.getHeight() + mMarginLeft, type.getHeight() + mMarginTop);
            matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER);
            mBitmapShader.setLocalMatrix(matrix);
        }

        this.mH = bitmap.getHeight();
        this.mW = bitmap.getWidth();
        this.paint.setColor(Color.WHITE);
        this.paint.setFilterBitmap(true);
        this.paint.setShader(mBitmapShader);
    }

    private void initTextDrawable(int color, String text, IconFactory.Type type) {
        this.text = text;
        this.emptyBitmap = true;
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(color);
        this.paint.setAntiAlias(true);
        this.paint.setTextAlign(Paint.Align.CENTER);
        this.paint.setLinearText(true);
        this.mH = this.mW = type.getHeight();

        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        this.mTextX = mMarginLeft + ((type.getHeight() - (int) paintText.measureText(text)) >> 1);
        this.mTextY = mMarginTop + ((type.getHeight() - (int) (fontMetrics.descent + fontMetrics.ascent)) >> 1);
    }

    @Override
    public int getIntrinsicWidth() {
        return mW;
    }

    @Override
    public int getIntrinsicHeight() {
        return mH;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mType == IconFactory.Type.USER || mType == IconFactory.Type.TITLE || mType == IconFactory.Type.BOT_COMMAND)
            this.mRect.set(0, 0, bounds.width(), bounds.height());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, paint);
        if (emptyBitmap && TextUtil.isNotBlank(text))
            canvas.drawText(text, mTextX, mTextY, paintText);
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
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    public boolean isEmptyBitmap() {
        return emptyBitmap;
    }
}