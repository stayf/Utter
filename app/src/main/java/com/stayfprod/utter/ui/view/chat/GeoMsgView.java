package com.stayfprod.utter.ui.view.chat;


import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.manager.GeoPointManager;
import com.stayfprod.utter.model.chat.GeoMsg;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class GeoMsgView extends AbstractMsgView<GeoMsg> implements ImageUpdatable {

    public static final int GEO_WIDTH = Constant.DP_150;
    public static final int GEO_HEIGHT = Constant.DP_100;

    private BitmapDrawable geoDrawable;
    private TdApi.Location geoPoint;

    public GeoMsgView(Context context) {
        super(context);
    }

    @Override
    public boolean onViewClick(View view, MotionEvent event, boolean isIgnoreEvent) {
        if (geoDrawable != null && geoPoint != null) {
            try {
                //лучше это:
                //if (mapIntent.resolveActivity(InitApp.currContext.getPackageManager()) != null)
                String title = "";
                if (record.tgMessage.message.getConstructor() == TdApi.MessageVenue.CONSTRUCTOR) {
                    TdApi.MessageVenue messageVenue = (TdApi.MessageVenue) record.tgMessage.message;
                    title =  messageVenue.title + ". " + messageVenue.address;
                }

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("geo:" + geoPoint.latitude
                                + "," + geoPoint.longitude
                                + "?q=" + geoPoint.latitude
                                + "," + geoPoint.longitude
                                + "(" + title
                                + ")&z=10"));
                intent.setComponent(new ComponentName(
                        "com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity"));
                getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    getContext().startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.google.android.apps.maps")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    getContext().startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps")));
                }
            } catch (Exception e) {
                AndroidUtil.showToastLong("Error:" + e.getMessage());
            }
        }
        return true;
    }

    public static void measure(GeoMsg record, int... orientation) {
        if (record != null) {
            int i = getMeasureOrientatedIndex(orientation);
            int windowWidth = getMeasureWidth(i);
            mainMeasure(record, i, windowWidth);

            measureLayoutHeight((int) (getSubContainerMarginTop(record) + GEO_HEIGHT), i, record);

            if (i == 0)
                measure(record, Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (geoDrawable == null) {
            canvas.drawRect(0f, 0f, GEO_WIDTH, GEO_HEIGHT, EMPTY_PAINT);
        } else {
            geoDrawable.draw(canvas);
        }
    }

    @Override
    public void setValues(GeoMsg record, int i, Context context, RecyclerView.ViewHolder viewHolder) {
        super.setValues(record, i, context, viewHolder);
        if (record.tgMessage.message.getConstructor() == TdApi.MessageLocation.CONSTRUCTOR) {
            TdApi.MessageLocation messageGeoPoint = (TdApi.MessageLocation) record.tgMessage.message;
            geoPoint = messageGeoPoint.location;
            geoDrawable = GeoPointManager.getManager().generateAndSetImage(messageGeoPoint.location, this, getItemViewTag());
        } else {
            TdApi.MessageVenue messageVenue = (TdApi.MessageVenue) record.tgMessage.message;
            geoPoint = messageVenue.location;
            geoDrawable = GeoPointManager.getManager().generateAndSetImage(messageVenue.location, this, getItemViewTag());
        }
        invalidate();
    }

    @Override
    public void setImageAndUpdateAsync(BitmapDrawable geoDrawable, boolean... animated) {
        this.geoDrawable = geoDrawable;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, record.layoutHeight[getOrientatedIndex()]);
    }

}
