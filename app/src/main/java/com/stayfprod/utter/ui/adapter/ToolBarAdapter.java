package com.stayfprod.utter.ui.adapter;


import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.service.IconFactory;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.AndroidUtil;

public class ToolBarAdapter extends BaseAdapter {

    private static final int DP_56 = Constant.DP_56;
    private static final int DP_44 = Constant.DP_44;
    private static final int DP_4 = Constant.DP_4;
    private static final int DP_5 = Constant.DP_5;
    private static final int DP_2 = Constant.DP_2;

    private final Context context;

    private RelativeLayout firstLayout;
    private RelativeLayout secondLayout;

    private TextView mainTitle;
    private ImageView chatIcon;
    private TextView chatTitle;
    private TextView chatSubText;

    public void setMainTitle(String val) {
        mainTitle.setText(val);
        this.notifyDataSetChanged();
    }

    public void setAlpha(float slideOffset) {
        int alphaMainTitle = (int) (255 * (1f - slideOffset));
        int alphaChat = (int) (255 * (slideOffset));
        mainTitle.setTextColor(mainTitle.getTextColors().withAlpha(alphaMainTitle));
        chatTitle.setTextColor(chatTitle.getTextColors().withAlpha(alphaChat));
        chatSubText.setTextColor(chatSubText.getTextColors().withAlpha(alphaChat));
        chatIcon.setAlpha(slideOffset);
        this.notifyDataSetChanged();
    }

    public void setChatTitle(final String title) {
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatTitle.setText(title);
                notifyDataSetChanged();
            }
        });
    }

    public void setChatSubTitle(final String subTitle) {
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatSubText.setText(subTitle);
                notifyDataSetChanged();
            }
        });
    }

    public void setChatToolBar(final String subTitle, final Spannable title, final IconDrawable iconDrawable) {
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatIcon.setImageDrawable(iconDrawable);
                chatTitle.setText(title);
                chatSubText.setText(subTitle);
                notifyDataSetChanged();
            }
        });
    }

    public ToolBarAdapter(Context context) {
        this.context = context;
        createItemOne();
        createItemTwo();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public View getItem(int position) {
        if (position == 1) {
            return secondLayout;
        } else {
            return firstLayout;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void createItemTwo() {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, DP_56));
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        chatIcon = new ImageView(context);
        chatIcon.setId(R.id.i_toolbar_icon);
        chatIcon.setPadding(DP_4, DP_4, DP_4, DP_4);
        relativeLayout.addView(chatIcon);

        RelativeLayout.LayoutParams iconToolbarLP = (RelativeLayout.LayoutParams) chatIcon.getLayoutParams();

        iconToolbarLP.height = IconFactory.Type.TITLE.getHeight();
        iconToolbarLP.width = IconFactory.Type.TITLE.getHeight();

        chatTitle = new TextView(context);
        chatTitle.setId(R.id.i_toolbar_title);
        chatTitle.setSingleLine();
        chatTitle.setEllipsize(TextUtils.TruncateAt.END);
        chatTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        chatTitle.setTextSize(18);
        chatTitle.setTextColor(0xffffffff);

        relativeLayout.addView(chatTitle);

        RelativeLayout.LayoutParams chatTitleLP = (RelativeLayout.LayoutParams) chatTitle.getLayoutParams();
        chatTitleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatTitleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatTitleLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        chatTitleLP.addRule(RelativeLayout.RIGHT_OF, chatIcon.getId());
        chatTitleLP.topMargin = DP_2;
        chatTitleLP.leftMargin = DP_5;

        chatSubText = new TextView(context);
        chatSubText.setSingleLine();
        chatSubText.setEllipsize(TextUtils.TruncateAt.END);
        chatSubText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        chatSubText.setTextSize(14);
        chatSubText.setTextColor(0xffd2eafc);

        relativeLayout.addView(chatSubText);

        RelativeLayout.LayoutParams chatSubTextLP = (RelativeLayout.LayoutParams) chatSubText.getLayoutParams();
        chatSubTextLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatSubTextLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;

        chatSubTextLP.addRule(RelativeLayout.RIGHT_OF, chatIcon.getId());
        chatSubTextLP.addRule(RelativeLayout.BELOW, chatTitle.getId());
        chatSubTextLP.leftMargin = DP_5;

        secondLayout = relativeLayout;

    }

    private void createItemOne() {
        firstLayout = new RelativeLayout(context);

        AbsListView.LayoutParams firstLayoutLP = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, DP_56);
        firstLayout.setLayoutParams(firstLayoutLP);
        firstLayout.setVerticalGravity(Gravity.CENTER);
        mainTitle = new TextView(context);
        firstLayout.addView(mainTitle);

        mainTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mainTitle.setTextColor(0xffffffff);
        mainTitle.setTextSize(20);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (position) {
            case 1:
                return secondLayout;
            default:
                return firstLayout;
        }
    }
}
