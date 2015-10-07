package com.stayfprod.utter.ui.view;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.AndroidUtil;

public abstract class AbstractChatView extends View {

    protected static final GradientDrawable BLUE_CYCLE_DRAWABLE;
    protected static final BitmapDrawable CLOCK_DRAWABLE;
    protected static final BitmapDrawable BADGE_DRAWABLE;
    protected static final BitmapDrawable ERROR_DRAWABLE;
    protected static final BitmapDrawable GROUP_DRAWABLE;
    protected static final BitmapDrawable MUTE_DRAWABLE;

    protected static final int
            MSG_STATUS_CLOCK_SIZE = Constant.DP_11,
            MSG_STATUS_CYCLE_SIZE = Constant.DP_10;

    static {
        BLUE_CYCLE_DRAWABLE = (GradientDrawable) FileUtils.decodeResource(R.drawable.cycle);
        CLOCK_DRAWABLE = (BitmapDrawable) FileUtils.decodeResource(R.mipmap.ic_clock);
        BADGE_DRAWABLE = (BitmapDrawable) FileUtils.decodeResource(R.mipmap.ic_badge);
        ERROR_DRAWABLE = (BitmapDrawable) FileUtils.decodeResource(R.mipmap.ic_error);
        GROUP_DRAWABLE = (BitmapDrawable) FileUtils.decodeResource(R.mipmap.ic_group);
        MUTE_DRAWABLE = (BitmapDrawable) FileUtils.decodeResource(R.mipmap.ic_mute);

        BLUE_CYCLE_DRAWABLE.setDither(true);
        CLOCK_DRAWABLE.setDither(true);
        BADGE_DRAWABLE.setDither(true);
        ERROR_DRAWABLE.setDither(true);
        GROUP_DRAWABLE.setDither(true);
        MUTE_DRAWABLE.setDither(true);
    }


    public AbstractChatView(Context context) {
        super(context);
    }

    public String getItemViewTag() {
        Object tag = this.getTag();
        return tag != null ? tag.toString() : "";
    }

    public int getItemViewIntTag() {
        Object tag = this.getTag();
        return tag != null ? (int) tag : -1;
    }

    protected static int getMeasureWidth(int index) {
        return index == 0 ? AndroidUtil.WINDOW_PORTRAIT_WIDTH : AndroidUtil.WINDOW_PORTRAIT_HEIGHT;
    }

    public int getOrientatedIndex() {
        if (getCurrentOrientation() == 2) {
            return 1;
        }
        return 0;
    }

    public int getCurrentOrientation() {
        return getContext().getResources().getConfiguration().orientation;
    }

    protected static int getMeasureOrientatedIndex(int... orientation) {
        return orientation.length > 0 ? orientation[0] - 1 : Configuration.ORIENTATION_PORTRAIT - 1;
    }

}
