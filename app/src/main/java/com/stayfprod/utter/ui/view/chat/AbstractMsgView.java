package com.stayfprod.utter.ui.view.chat;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.chat.AbstractMainMsg;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.view.AbstractChatView;
import com.stayfprod.utter.ui.view.IconUpdatable;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.AndroidUtil;

public abstract class AbstractMsgView<X extends AbstractMainMsg> extends AbstractChatView implements IconUpdatable {

    private static final TextPaint
            FORWARD_LINE_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            NAME_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            TEXT_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            DATE_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public static final Paint EMPTY_PAINT = new Paint();

    private static final int MSG_STATUS_MARGIN_RIGHT = Constant.DP_12,
            MSG_STATUS_CYCLE_MARGIN_TOP = Constant.DP_7,
            MSG_STATUS_CLOCK_MARGIN_TOP = Constant.DP_4,
            NAME_MARGIN_TOP = Constant.DP_1,
            NAME_MARGIN_BOTTOM = Constant.DP_2,
            DATE_MARGIN_RIGHT = Constant.DP_1,
            DATE_MARGIN_TOP = Constant.DP_4,
            NAME_MARGIN_RIGHT = Constant.DP_7,
            NAME_MARGIN_LEFT = Constant.DP_10,
            NAME_FORWARD_MARGIN_LEFT = Constant.DP_5;

    private static final float
            NAME_START_Y,
            DATE_START_Y,
            FORWARD_NAME_START_Y,
            FORWARD_DATE_START_Y;

    public static final int MIN_MSG_HEIGHT = Constant.DP_60;
    public static final int MIN_MSG_FORWARD_HEIGHT = Constant.DP_80;
    public static final int SUB_CONTAINER_MARGIN_RIGHT = Constant.DP_12;
    public static final int SUB_CONTAINER_MARGIN_BOTTOM = Constant.DP_5;

    protected static final int SUB_CONTAINER_MARGIN_LEFT = IconDrawable.CHAT_MARGIN_LEFT + IconFactory.Type.CHAT.getHeight() + NAME_MARGIN_LEFT;
    private static final float SUB_CONTAINER_MARGIN_TOP;

    private static final int FORWARD_LINE_WIDTH = Constant.DP_3;
    private static final int FORWARD_SUB_CONTAINER_MARGIN_LEFT =
            SUB_CONTAINER_MARGIN_LEFT + FORWARD_LINE_WIDTH + IconDrawable.CHAT_MARGIN_LEFT + IconFactory.Type.CHAT.getHeight() + NAME_FORWARD_MARGIN_LEFT;
    private static final float FORWARD_SUB_CONTAINER_MARGIN_TOP;

    static {
        EMPTY_PAINT.setColor(0x0D5B95C2);
        EMPTY_PAINT.setStyle(Paint.Style.FILL);

        FORWARD_LINE_PAINT.setColor(0xFF6BADE0);
        FORWARD_LINE_PAINT.setStyle(Paint.Style.FILL);

        NAME_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_BOLD);
        NAME_PAINT.setColor(0xFF569ace);
        NAME_PAINT.setTextSize(Constant.DP_15);

        DATE_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        DATE_PAINT.setColor(0xFFb2b2b2);
        DATE_PAINT.setTextSize(Constant.DP_13);

        TEXT_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        TEXT_PAINT.setTextSize(Constant.DP_15);
        TEXT_PAINT.setColor(0xFF333333);

        Paint.FontMetrics titlePaintFontMetrics = NAME_PAINT.getFontMetrics();
        float titlePaintHeight = (titlePaintFontMetrics.descent - titlePaintFontMetrics.ascent + titlePaintFontMetrics.leading);
        NAME_START_Y = NAME_MARGIN_TOP + titlePaintHeight;

        Paint.FontMetrics datePaintFontMetrics = DATE_PAINT.getFontMetrics();
        float datePaintHeight = (datePaintFontMetrics.descent - datePaintFontMetrics.ascent + datePaintFontMetrics.leading);
        DATE_START_Y = DATE_MARGIN_TOP + datePaintHeight;

        FORWARD_NAME_START_Y = NAME_MARGIN_TOP + titlePaintHeight;
        FORWARD_DATE_START_Y = DATE_MARGIN_TOP + datePaintHeight;

        SUB_CONTAINER_MARGIN_TOP = NAME_MARGIN_BOTTOM + NAME_START_Y + Constant.DP_5;
        FORWARD_SUB_CONTAINER_MARGIN_TOP = SUB_CONTAINER_MARGIN_TOP + NAME_MARGIN_BOTTOM + FORWARD_NAME_START_Y + Constant.DP_5;
    }

    protected static void measureLayoutHeight(int bidderHeight, int i, AbstractMainMsg record) {
        //info здесь вроде высота одинаковая, у некоторых элементов
        int minHeight = record.isForward ? MIN_MSG_FORWARD_HEIGHT : MIN_MSG_HEIGHT;
        if (bidderHeight > minHeight) {
            record.layoutHeight[i] = bidderHeight + SUB_CONTAINER_MARGIN_BOTTOM;
        } else {
            record.layoutHeight[i] = minHeight;
        }
    }

    protected static float getSubContainerMarginTop(AbstractMainMsg record) {
        if (!record.isForward)
            return SUB_CONTAINER_MARGIN_TOP;
        else
            return FORWARD_SUB_CONTAINER_MARGIN_TOP;
    }

    protected static int getSubContainerMarginLeft(AbstractMainMsg record) {
        if (!record.isForward)
            return SUB_CONTAINER_MARGIN_LEFT;
        else
            return FORWARD_SUB_CONTAINER_MARGIN_LEFT;
    }

    public volatile X record;
    protected RecyclerView.ViewHolder viewHolder;
    private IconDrawable iconDrawable;
    private IconDrawable iconDrawableForward;

    public AbstractMsgView(Context context) {
        super(context);

        this.setOnTouchListener(new OnTouchListener() {

            private Handler mHandler = new Handler();

            private float mDownX;
            private float mDownY;
            boolean isPressed;
            private final float SCROLL_THRESHOLD = 10;
            private final long LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout() - 10;
            Runnable mBackColorRunnable = new Runnable() {
                @Override
                public void run() {
                    AbstractMsgView.this.setBackgroundColor(0x1A000000);
                }
            };

            Runnable mCancelClickRunnable = new Runnable() {
                @Override
                public void run() {
                    isPressed = false;
                }
            };

            public void setBackgroundTransparent() {
                mHandler.removeCallbacks(mBackColorRunnable);
                AbstractMsgView.this.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();

                        if (!isClickOnActionButton(view, event)) {
                            mHandler.postDelayed(mBackColorRunnable, 120);
                        }

                        mHandler.postDelayed(mCancelClickRunnable, LONG_PRESS_TIMEOUT);
                        isPressed = true;
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        if (isPressed && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD
                                || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                            isPressed = false;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE: {
                        setBackgroundTransparent();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        setBackgroundTransparent();
                        if (isPressed) {
                            isPressed = false;
                            mHandler.removeCallbacks(mCancelClickRunnable);
                            boolean result = onViewClick(view, event, false);
                            return true;
                        }
                        break;
                    default: {
                        isPressed = false;
                    }
                }
                return true;
            }
        });
    }

    public abstract boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent);

    public boolean isClickOnActionButton(View view, MotionEvent event) {
        return false;
    }

    @Override
    public void setIconAsync(IconDrawable iconDrawable, boolean isForwardIcon, boolean... animated) {
        if (iconDrawable != null) {
            final Rect dirty;
            if (!isForwardIcon) {
                this.iconDrawable.emptyBitmap = iconDrawable.emptyBitmap;
                this.iconDrawable.mPaint = iconDrawable.mPaint;
                this.iconDrawable.text = iconDrawable.text;
                dirty = this.iconDrawable.getDirtyBounds();
            } else {
                this.iconDrawableForward.emptyBitmap = iconDrawable.emptyBitmap;
                this.iconDrawableForward.mPaint = iconDrawable.mPaint;
                this.iconDrawableForward.text = iconDrawable.text;
                dirty = this.iconDrawableForward.getDirtyBounds();
            }
            postInvalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (record != null) {
            int i = getOrientatedIndex();

            if (iconDrawable != null)
                iconDrawable.draw(canvas);

            if (record.msgIcon != null) {
                switch (record.msgIcon) {
                    case ACCEPT_NOT_RED:
                        BLUE_CYCLE_DRAWABLE.setBounds(record.cycleRect[i]);
                        BLUE_CYCLE_DRAWABLE.draw(canvas);
                        break;
                    case NOT_SEND:
                        CLOCK_DRAWABLE.setBounds(record.clockRect[i]);
                        CLOCK_DRAWABLE.draw(canvas);
                        break;
                }
            }

            if (record.drawName[i] != null)
                canvas.drawText(record.drawName[i], 0, record.drawName[i].length(), SUB_CONTAINER_MARGIN_LEFT, NAME_START_Y, NAME_PAINT);

            if (record.date != null)
                canvas.drawText(record.date, 0, record.date.length(), record.dateStartX[i], DATE_START_Y, DATE_PAINT);

            canvas.translate(SUB_CONTAINER_MARGIN_LEFT, SUB_CONTAINER_MARGIN_TOP);

            if (record.isForward) {
                canvas.drawRect(0, 0, FORWARD_LINE_WIDTH, record.layoutHeight[getOrientatedIndex()], FORWARD_LINE_PAINT);

                if (iconDrawableForward != null) {
                    iconDrawableForward.draw(canvas);
                }

                if (record.forwardDrawName[i] != null) {
                    canvas.drawText(record.forwardDrawName[i], 0, record.forwardDrawName[i].length(),
                            FORWARD_SUB_CONTAINER_MARGIN_LEFT - SUB_CONTAINER_MARGIN_LEFT, FORWARD_NAME_START_Y, NAME_PAINT);
                }

                if (record.forwardDate != null) {
                    canvas.drawText(record.forwardDate, 0, record.forwardDate.length(), record.forwardDateStartX[i], FORWARD_DATE_START_Y, DATE_PAINT);
                }

                canvas.translate(FORWARD_SUB_CONTAINER_MARGIN_LEFT - SUB_CONTAINER_MARGIN_LEFT, FORWARD_SUB_CONTAINER_MARGIN_TOP - SUB_CONTAINER_MARGIN_TOP);
            }
        }
    }

    public static void measureByOrientation(AbstractMainMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            if (i == 0)
                measureByOrientation(record, Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    protected static void mainMeasure(AbstractMainMsg record, int i, int windowWidth) {
        if (record != null) {
            //ставим справого края
            float statusMsgStartX;
            switch (record.msgIcon) {
                case ACCEPT_NOT_RED:
                    statusMsgStartX = windowWidth - MSG_STATUS_CYCLE_SIZE - MSG_STATUS_MARGIN_RIGHT;
                    record.cycleRect[i].left = (int) statusMsgStartX;
                    record.cycleRect[i].top = MSG_STATUS_CYCLE_MARGIN_TOP;
                    record.cycleRect[i].right = windowWidth - MSG_STATUS_MARGIN_RIGHT;
                    record.cycleRect[i].bottom = MSG_STATUS_CYCLE_SIZE + MSG_STATUS_CYCLE_MARGIN_TOP;
                    break;
                case NOT_SEND:
                    statusMsgStartX = windowWidth - MSG_STATUS_CLOCK_SIZE - MSG_STATUS_MARGIN_RIGHT;
                    record.clockRect[i].left = (int) statusMsgStartX;
                    record.clockRect[i].top = MSG_STATUS_CLOCK_MARGIN_TOP;
                    record.clockRect[i].right = windowWidth - MSG_STATUS_MARGIN_RIGHT;
                    record.clockRect[i].bottom = MSG_STATUS_CLOCK_SIZE + MSG_STATUS_CLOCK_MARGIN_TOP;
                    break;
                default: {
                    //забираем большее растояние
                    statusMsgStartX = windowWidth - MSG_STATUS_CLOCK_SIZE - MSG_STATUS_MARGIN_RIGHT;
                }
            }

            float dateWidth = DATE_PAINT.measureText(record.date);
            float maxNameWidth = (statusMsgStartX - DATE_MARGIN_RIGHT - dateWidth - NAME_MARGIN_RIGHT) - SUB_CONTAINER_MARGIN_LEFT;


            record.drawName[i] = TextUtils.ellipsize(record.cachedUser.fullName, NAME_PAINT, maxNameWidth, TextUtils.TruncateAt.END);
            float nameWidth = NAME_PAINT.measureText(record.drawName[i], 0, record.drawName[i].length());

            record.dateStartX[i] = SUB_CONTAINER_MARGIN_LEFT + nameWidth + NAME_MARGIN_RIGHT;

            if (record.isForward) {
                float forwardDateWidth = DATE_PAINT.measureText(record.forwardDate);
                float maxForwardNameWidth = windowWidth - FORWARD_SUB_CONTAINER_MARGIN_LEFT - forwardDateWidth;
                record.forwardDrawName[i] = TextUtils.ellipsize(record.cachedForwardUser.fullName, NAME_PAINT, maxForwardNameWidth, TextUtils.TruncateAt.END);

                float forwardNameWidth = NAME_PAINT.measureText(record.forwardDrawName[i], 0, record.forwardDrawName[i].length());
                record.forwardDateStartX[i] = FORWARD_SUB_CONTAINER_MARGIN_LEFT - SUB_CONTAINER_MARGIN_LEFT + forwardNameWidth + NAME_MARGIN_RIGHT;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }

    public void setValues(X record, int i, Context context, RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
        this.setTag(i);
        this.record = record;

        if (record != null && record.cachedUser != null) {
            if (FileUtils.isTDFileEmpty(record.cachedUser.tgUser.profilePhoto.small)) {
                iconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT, record.cachedUser.tgUser.id, record.cachedUser.initials);
                if (record.cachedUser.tgUser.profilePhoto.small.id > 0)
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_ICON,
                            record.cachedUser.tgUser.profilePhoto.small.id, i, record.tgMessage.id, record.cachedUser.tgUser, this, getItemViewTag(), false);
            } else {
                iconDrawable = IconFactory.createBitmapIconForChat(IconFactory.Type.CHAT, record.cachedUser.tgUser.profilePhoto.small.path, this, getItemViewTag());
            }
            setForwardValues(record, i);
        }
    }

    private void setForwardValues(X record, int i) {
        if (record.isForward) {
            if (FileUtils.isTDFileEmpty(record.cachedForwardUser.tgUser.profilePhoto.small)) {
                iconDrawableForward = IconFactory.createEmptyIcon(IconFactory.Type.CHAT,
                        record.cachedForwardUser.tgUser.id, record.cachedForwardUser.initials);
                if (record.cachedForwardUser.tgUser.profilePhoto.small.id > 0) {
                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_ICON,
                            record.cachedForwardUser.tgUser.profilePhoto.small.id, i,
                            record.tgMessage.id, record.cachedForwardUser.tgUser, this, getItemViewTag(), true);
                }
            } else {
                iconDrawableForward = IconFactory.createBitmapIconForChat(IconFactory.Type.CHAT,
                        record.cachedForwardUser.tgUser.profilePhoto.small.path, this, getItemViewTag(), true);
            }
        }
    }
}
