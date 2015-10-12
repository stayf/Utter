package com.stayfprod.utter.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.DialogDrawParams;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.factory.StaticLayoutFactory;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.TextUtil;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class DialogView extends AbstractChatView implements IconUpdatable {

    private static final String LOG = DialogView.class.getSimpleName();

    public static volatile boolean isNeedDraw = true;

    static final Paint LINE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final TextPaint
            DATA_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            COUNTER_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            TEXT_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG),
            TITLE_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private static final int
            DATE_MARGIN_TOP = Constant.DP_12,
            DATE_MARGIN_RIGHT = Constant.DP_12,
            MSG_STATUS_CYCLE_MARGIN_TOP = Constant.DP_17_5,
            MSG_STATUS_CLOCK_MARGIN_TOP = Constant.DP_16_5,
            MSG_STATUS_MARGIN_RIGHT = Constant.DP_4,
            MSG_COUNTER_MARGIN_RIGHT = Constant.DP_12,
            MSG_COUNTER_MARGIN_TOP = Constant.DP_10,
            MSG_COUNTER_HEIGHT = Constant.DP_20,
            MSG_COUNTER_PADDING_2 = Constant.DP_20,
            MSG_COUNTER_PADDING_1 = Constant.DP_7,
            GROUP_MARGIN_TOP = Constant.DP_18,
            GROUP_MARGIN_LEFT = Constant.DP_10,
            GROUP_WIDTH = Constant.DP_18,
            GROUP_HEIGHT = Constant.DP_10,
            MUTE_HEIGHT = Constant.DP_14,
            MUTE_WIDTH = Constant.DP_14,
            MUTE_MARGIN_TOP = Constant.DP_15,
            MUTE_MARGIN_LEFT = Constant.DP_1,
            TITLE_MARGIN_TOP = Constant.DP_8,
            TEXT_MARGIN_TOP = Constant.DP_10;

    static final float
            DATE_START_Y,
            COUNTER_START_Y,
            TITLE_START_Y,
            TEXT_START_Y_ILL;

    public static final float TEXT_MARGIN_METRICS;

    public static final int LAYOUT_HEIGHT = Constant.DP_74;

    static {
        LINE_PAINT.setColor(0xFFF8F8F8);

        DATA_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        DATA_PAINT.setColor(0xFF999999);
        DATA_PAINT.setTextSize(Constant.DP_13);

        COUNTER_PAINT.setTextSize(Constant.DP_14);
        COUNTER_PAINT.setColor(Color.WHITE);
        COUNTER_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);

        TITLE_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        TITLE_PAINT.setTextSize(Constant.DP_17);
        TITLE_PAINT.setColor(0xFF222222);

        TEXT_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        TEXT_PAINT.setColor(0xFF8a8a8a);
        TEXT_PAINT.setTextSize(Constant.DP_16);

        Paint.FontMetrics dataPaintFontMetrics = DATA_PAINT.getFontMetrics();
        DATE_START_Y = DATE_MARGIN_TOP + (dataPaintFontMetrics.descent - dataPaintFontMetrics.ascent + dataPaintFontMetrics.leading);

        Paint.FontMetrics counterPaintFontMetrics = DATA_PAINT.getFontMetrics();
        COUNTER_START_Y = DATE_START_Y + MSG_COUNTER_MARGIN_TOP + ((MSG_COUNTER_HEIGHT - (int) (counterPaintFontMetrics.descent + counterPaintFontMetrics.ascent)) >> 1);

        Paint.FontMetrics titlePaintFontMetrics = TITLE_PAINT.getFontMetrics();
        TITLE_START_Y = TITLE_MARGIN_TOP + (titlePaintFontMetrics.descent - titlePaintFontMetrics.ascent + titlePaintFontMetrics.leading);

        Paint.FontMetrics textPaintFontMetrics = TEXT_PAINT.getFontMetrics();
        //info для ContactView
        TEXT_MARGIN_METRICS = (textPaintFontMetrics.descent - textPaintFontMetrics.ascent + textPaintFontMetrics.leading);
        TEXT_START_Y_ILL = TITLE_START_Y + TEXT_MARGIN_TOP;
    }

    private volatile ChatInfo record;

    private IconDrawable iconDrawable;

    public DialogView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isNeedDraw) {
            int i = getOrientatedIndex();
            DialogDrawParams drawParams = record.drawParams;

            if (iconDrawable != null) {
                iconDrawable.draw(canvas);
            }

            if (record != null) {
                if (TextUtil.isNotBlank(record.date))
                    canvas.drawText(record.date, 0, record.date.length(), drawParams.dateStartX[i], DATE_START_Y, DATA_PAINT);

                switch (record.outputMsgIcon) {
                    case ACCEPT_NOT_RED:
                        BLUE_CYCLE_DRAWABLE.setBounds(record.drawParams.cycleRect[i]);
                        BLUE_CYCLE_DRAWABLE.draw(canvas);
                        break;
                    case NOT_SEND:
                        CLOCK_DRAWABLE.setBounds(record.drawParams.clockRect[i]);
                        CLOCK_DRAWABLE.draw(canvas);
                        break;
                }

                switch (record.inputMsgIcon) {

                    case NEW_MSG: {
                        if (drawParams.unreadCountStr != null) {
                            if (record.drawParams.badgeRect[i] != null) {
                                BADGE_DRAWABLE.setBounds(record.drawParams.badgeRect[i]);
                                BADGE_DRAWABLE.draw(canvas);
                                canvas.drawText(drawParams.unreadCountStr, drawParams.counterStartX[i], COUNTER_START_Y, COUNTER_PAINT);
                            }
                        }
                        break;
                    }
                    case ERROR: {
                        ERROR_DRAWABLE.setBounds(record.drawParams.errorRect[i]);
                        ERROR_DRAWABLE.draw(canvas);
                        break;
                    }
                }

                if (record.isGroupChat) {
                    GROUP_DRAWABLE.setBounds(record.drawParams.groupRect);
                    GROUP_DRAWABLE.draw(canvas);
                }

                canvas.drawText(drawParams.drawTitle[i], 0, drawParams.drawTitle[i].length(), drawParams.titleStartX, TITLE_START_Y, TITLE_PAINT);

                if (drawParams.isMute) {
                    MUTE_DRAWABLE.setBounds(record.drawParams.muteRect[i]);
                    MUTE_DRAWABLE.draw(canvas);
                }

                canvas.translate(drawParams.textStartX, 0);
                canvas.drawRect(0, drawParams.layoutHeight - Constant.DP_1, getMeasureWidth(i), drawParams.layoutHeight, LINE_PAINT);

                canvas.translate(0, drawParams.staticTextLayoutStartY[i]);
                drawParams.staticTextLayout[i].draw(canvas);
            }
        }
    }

    public static void measure(ChatInfo record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);

            DialogDrawParams drawParams = record.drawParams;
            if (i == 0)
                drawParams.unreadCountStr = record.tgChatObject.unreadCount > 0 ? String.valueOf(record.tgChatObject.unreadCount) : null;

            drawParams.dateStartX[i] = windowWidth - DATA_PAINT.measureText(record.date) - DATE_MARGIN_RIGHT;
            float statusMsgStartX;
            switch (record.outputMsgIcon) {
                case ACCEPT_NOT_RED:
                    statusMsgStartX = (drawParams.dateStartX[i]) - MSG_STATUS_CYCLE_SIZE - MSG_STATUS_MARGIN_RIGHT;
                    drawParams.cycleRect[i].left = (int) statusMsgStartX;
                    drawParams.cycleRect[i].top = MSG_STATUS_CYCLE_MARGIN_TOP;
                    drawParams.cycleRect[i].right = (int) drawParams.dateStartX[i] - MSG_STATUS_MARGIN_RIGHT;
                    drawParams.cycleRect[i].bottom = MSG_STATUS_CYCLE_SIZE + MSG_STATUS_CYCLE_MARGIN_TOP;
                    break;
                case NOT_SEND:
                    statusMsgStartX = (drawParams.dateStartX[i]) - MSG_STATUS_CLOCK_SIZE - MSG_STATUS_MARGIN_RIGHT;
                    drawParams.clockRect[i].left = (int) statusMsgStartX;
                    drawParams.clockRect[i].top = MSG_STATUS_CLOCK_MARGIN_TOP;
                    drawParams.clockRect[i].right = (int) drawParams.dateStartX[i] - MSG_STATUS_MARGIN_RIGHT;
                    drawParams.clockRect[i].bottom = MSG_STATUS_CLOCK_SIZE + MSG_STATUS_CLOCK_MARGIN_TOP;
                    break;
                default: {
                    //забираем большее растояние
                    statusMsgStartX = drawParams.dateStartX[i] - MSG_STATUS_CLOCK_SIZE - MSG_STATUS_MARGIN_RIGHT;
                }
            }

            int inputMsgStartX = 0;
            int inputMsgEndX = windowWidth - MSG_COUNTER_MARGIN_RIGHT;

            //record.inputMsgIcon = InputMsgIconType.ERROR;
            switch (record.inputMsgIcon) {
                case NEW_MSG: {
                    if (drawParams.unreadCountStr != null) {
                        int length = drawParams.unreadCountStr.length();
                        int badgeWidth = (--length) * MSG_COUNTER_PADDING_1 + MSG_COUNTER_PADDING_2;

                        inputMsgStartX = inputMsgEndX - badgeWidth;
                        int startY = (int) (DATE_START_Y + MSG_COUNTER_MARGIN_TOP);
                        int endY = startY + MSG_COUNTER_HEIGHT;

                        drawParams.badgeRect[i].left = inputMsgStartX;
                        drawParams.badgeRect[i].top = startY;
                        drawParams.badgeRect[i].right = inputMsgEndX;
                        drawParams.badgeRect[i].bottom = endY;
                        drawParams.counterStartX[i] = inputMsgStartX + ((badgeWidth - (int) COUNTER_PAINT.measureText(drawParams.unreadCountStr)) >> 1);
                    } else {
                        //кто-то успевает обновить кол-во у записи
                        inputMsgStartX = inputMsgEndX;
                    }

                    break;
                }
                case ERROR: {
                    inputMsgStartX = inputMsgEndX - MSG_COUNTER_HEIGHT;
                    int startY = (int) (DATE_START_Y + MSG_COUNTER_MARGIN_TOP);
                    int endY = startY + MSG_COUNTER_HEIGHT;
                    drawParams.errorRect[i].left = inputMsgStartX;
                    drawParams.errorRect[i].top = startY;
                    drawParams.errorRect[i].right = inputMsgEndX;
                    drawParams.errorRect[i].bottom = endY;
                    break;
                }
                default: {
                    inputMsgStartX = inputMsgEndX;
                }
            }

            if (i == 0)
                drawParams.titleStartX = Constant.DP_2;

            int groupStartX = IconDrawable.CHAT_LIST_MARGIN_LEFT + IconFactory.Type.CHAT_LIST.getHeight() + GROUP_MARGIN_LEFT;

            if (i == 0) {
                if (record.isGroupChat) {
                    int endX = groupStartX + GROUP_WIDTH;
                    drawParams.groupRect.left = groupStartX;
                    drawParams.groupRect.top = GROUP_MARGIN_TOP;
                    drawParams.groupRect.right = endX;
                    drawParams.groupRect.bottom = GROUP_MARGIN_TOP + GROUP_HEIGHT;
                    drawParams.titleStartX += endX;
                } else {
                    drawParams.titleStartX += groupStartX;
                }
            }

            if (i == 0)
                drawParams.isMute = record.tgChatObject.notificationSettings.muteFor > 0;

            float maxTitleWidth = (statusMsgStartX - MUTE_MARGIN_LEFT - drawParams.titleStartX);

            if (drawParams.isMute) {
                maxTitleWidth -= MUTE_WIDTH + MUTE_MARGIN_LEFT;
            }

            drawParams.drawTitle[i] = TextUtils.ellipsize(record.chatName, TITLE_PAINT, maxTitleWidth, TextUtils.TruncateAt.END);

            if (drawParams.isMute) {
                int muteStartX = (int) (drawParams.titleStartX + TITLE_PAINT.measureText(drawParams.drawTitle[i], 0, drawParams.drawTitle[i].length()));
                drawParams.muteRect[i].left = muteStartX + MUTE_MARGIN_LEFT;
                drawParams.muteRect[i].top = MUTE_MARGIN_TOP;
                drawParams.muteRect[i].right = muteStartX + MUTE_WIDTH + MUTE_MARGIN_LEFT;
                drawParams.muteRect[i].bottom = MUTE_MARGIN_TOP + MUTE_HEIGHT;
            }

            if (i == 0)
                drawParams.textStartX = groupStartX;

            float textEndX = inputMsgStartX - Constant.DP_1;
            int maxTextWidth = (int) (textEndX - groupStartX);
            //fixme -150????

            if (maxTextWidth <= 0) {
                String msg = maxTextWidth + " " + textEndX + " " + groupStartX + " " + record.inputMsgIcon.name() + " " + inputMsgStartX + " " + windowWidth;
                //Logs.e(msg);
                Crashlytics.log(msg);

            }

            if (i == 0)
                drawParams.layoutHeight = LAYOUT_HEIGHT;

            drawParams.staticTextLayout[i] = StaticLayoutFactory.createSimpleLayout(record.text, TEXT_PAINT, maxTextWidth, TextUtils.TruncateAt.END, 1);
            //fixme упало с Null

            /*
            * java.lang.NullPointerException
               at com.stayfprod.utter.ui.view.DialogView.measure(DialogView.java:289)
               at com.stayfprod.utter.manager.ChatListManager$5.run(ChatListManager.java:329)
               at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1112)
               at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:587)
               at com.stayfprod.utter.service.ThreadService$1$1.run(ThreadService.java:64)
               at java.lang.Thread.run(Thread.java:841)
            *
            * */
            drawParams.staticTextLayoutStartY[i] = TEXT_START_Y_ILL +
                    (drawParams.staticTextLayout[i].getLineAscent(0) - TEXT_PAINT.ascent())
                    + (drawParams.staticTextLayout[i].getLineDescent(0) == 0 ? TEXT_PAINT.descent() : 0);

            if (i == 0) {
                measure(record, Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //if (isNeedDraw) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
        //}
    }

    public void setValues(ChatInfo record, int i, Context context) {
        this.setTag(i);
        this.record = record;

        switch (record.tgChatObject.type.getConstructor()) {
            case TdApi.PrivateChatInfo.CONSTRUCTOR: {
                TdApi.User user = ((TdApi.PrivateChatInfo) record.tgChatObject.type).user;
                TdApi.File file = user.profilePhoto.small;

                if (FileUtils.isTDFileEmpty(file)) {
                    iconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT_LIST, user.id, record.initials);
                    if (file.id > 0) {
                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_LIST_ICON,
                                file.id, i, -1, user, this, getItemViewTag());
                    }
                } else {
                    iconDrawable = IconFactory.createBitmapIconForChat(IconFactory.Type.CHAT_LIST, file.path, this, getItemViewTag());
                }
                break;
            }
            case TdApi.GroupChatInfo.CONSTRUCTOR: {
                TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) record.tgChatObject.type;
                TdApi.GroupChat groupChat = groupChatInfo.groupChat;

                if (FileUtils.isTDFileEmpty(groupChat.photo.small)) {
                    iconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.CHAT_LIST, groupChat.id, record.initials);
                    if (groupChat.photo.small.id > 0) {
                        FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.CHAT_LIST_ICON,
                                groupChat.photo.small.id, i, -1, groupChat, this, getItemViewTag());
                    }
                } else {
                    iconDrawable = IconFactory.createBitmapIconForChat(
                            IconFactory.Type.CHAT_LIST, groupChat.photo.small.path, this, getItemViewTag());
                }
                break;
            }
        }
        invalidate();
    }

    @Override
    public void setIconAsync(IconDrawable iconDrawable, boolean isForward, boolean... animated) {
        if (iconDrawable != null) {
            this.iconDrawable.emptyBitmap = iconDrawable.emptyBitmap;
            this.iconDrawable.mPaint = iconDrawable.mPaint;
            this.iconDrawable.text = iconDrawable.text;

            final Rect dirty = this.iconDrawable.getDirtyBounds();
            postInvalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
        }
    }
}
