package com.stayfprod.utter.ui.view.chat;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.emojicon.EmojiconHandler;
import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.Patterns;
import com.stayfprod.utter.model.chat.TextMsg;
import com.stayfprod.utter.service.StaticLayoutFactory;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.ChatHelper;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.regex.Pattern;

public class TextMsgView extends AbstractMsgView<TextMsg> {

    private static final String LOG = TextMsgView.class.getSimpleName();
    public static final TextPaint TEXT_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    static {
        TEXT_PAINT.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        TEXT_PAINT.setTextSize(Constant.DP_15);
        TEXT_PAINT.setColor(0xFF333333);
        TEXT_PAINT.linkColor = 0xFF569ace;
    }

    public TextMsgView(Context context) {
        super(context);
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        try {
            //info нажатие на ссылки в тексте
            Spanned spanned = record.text;

            URLSpan[] urls = spanned.getSpans(0, spanned.length(), URLSpan.class);

            int index = getOrientatedIndex();

            for (int i = 0; i < urls.length; i++) {
                int start = spanned.getSpanStart(urls[i]);
                int end = spanned.getSpanEnd(urls[i]);

                Path dest = new Path();
                record.staticTextLayout[index].getSelectionPath(start, end, dest);

                RectF rectF = new RectF();
                dest.computeBounds(rectF, true);

                rectF.offset(getSubContainerMarginLeft(record), getSubContainerMarginTop(record));

                if (rectF.contains(event.getX(), event.getY())) {
                    urls[i].onClick(view);
                    return true;
                }
            }
        } catch (Exception e) {
            //Log.w(LOG, "onViewClick", e);
        }
        return false;
    }

    @Override
    public void setValues(TextMsg record, int i, Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = getOrientatedIndex();
        if (record.staticTextLayout[i] != null) {
            float startY = record.staticTextLayoutStartY[i];
            canvas.translate(0, startY);
            record.staticTextLayout[i].draw(canvas);
        }
    }

    public static void measureContact(TextMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageContact messageContact = (TdApi.MessageContact) message;
        chatMessage.text = new SpannableString(messageContact.firstName + " " + messageContact.lastName + " " + messageContact.phoneNumber);
        measureByOrientation(chatMessage);
    }

    public static void measureVenue(TextMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageVenue messageVenue = (TdApi.MessageVenue) message;
        setText(chatMessage, messageVenue.title + messageVenue.address);
        measureByOrientation(chatMessage);
    }

    public static void measureWebPage(TextMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageWebPage messageWebPage = (TdApi.MessageWebPage) message;
        setText(chatMessage, messageWebPage.webPage.url);
        measureByOrientation(chatMessage);
    }

    public static void setText(TextMsg chatMessage, CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        Linkify.addLinks(builder, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
        Linkify.addLinks(builder, Patterns.WEB_URL, "http://");

        ChatHelper.addLinks(builder, Pattern.compile("(^|\\s)/(\\w|@)+"), "botHelpLinks");
        //ChatInfo chatInfo = ChatManager.getCurrentChatInfo();

        /*if (chatInfo != null && (chatInfo.isBot || chatInfo.isGroupChat)) {
            BotManager.getManager().linkifyMsg(builder);
        }*/

        EmojiconHandler.addEmojis(App.getAppContext(), builder, Constant.DP_20, 0, -1, false);
        chatMessage.text = builder;
    }

    public static void measureText(TextMsg chatMessage, TdApi.MessageContent message) {
        TdApi.MessageText messageText = (TdApi.MessageText) message;
        setText(chatMessage, messageText.text);
        measureByOrientation(chatMessage);
    }

    public static void measureMedia(TextMsg chatMessage, TdApi.MessageContent message) {
        measureByOrientation(chatMessage);
    }

    public static void measureByOrientation(TextMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            int subContentWidth = windowWidth - getSubContainerMarginLeft(record) - SUB_CONTAINER_MARGIN_RIGHT;
            record.staticTextLayout[i] = StaticLayoutFactory.createSimpleLayout(record.text, TEXT_PAINT, subContentWidth, null);

            record.staticTextLayoutStartY[i] = (record.staticTextLayout[i].getLineAscent(0) - TEXT_PAINT.ascent())
                    + (record.staticTextLayout[i].getLineDescent(0) == 0 ? TEXT_PAINT.descent() : 0);

            int bidderHeight = (int) (record.staticTextLayout[i].getHeight() + getSubContainerMarginTop(record));

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
