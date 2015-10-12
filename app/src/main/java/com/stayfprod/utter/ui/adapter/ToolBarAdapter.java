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
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.AndroidUtil;

public class ToolBarAdapter extends BaseAdapter {

    private static final int DP_56 = Constant.DP_56;
    private static final int DP_44 = Constant.DP_44;
    private static final int DP_4 = Constant.DP_4;
    private static final int DP_5 = Constant.DP_5;
    private static final int DP_2 = Constant.DP_2;

    private final Context mContext;

    private RelativeLayout mFirstLayout;
    private RelativeLayout mSecondLayout;

    private TextView mMainTitle;
    private ImageView mChatIcon;
    private TextView mChatTitle;
    private TextView mChatSubText;

    public void setMainTitle(String val) {
        mMainTitle.setText(val);
        this.notifyDataSetChanged();
    }

    public void setAlpha(float slideOffset) {
        int alphaMainTitle = (int) (255 * (1f - slideOffset));
        int alphaChat = (int) (255 * (slideOffset));
        mMainTitle.setTextColor(mMainTitle.getTextColors().withAlpha(alphaMainTitle));
        mChatTitle.setTextColor(mChatTitle.getTextColors().withAlpha(alphaChat));
        mChatSubText.setTextColor(mChatSubText.getTextColors().withAlpha(alphaChat));
        mChatIcon.setAlpha(slideOffset);
        this.notifyDataSetChanged();
    }

    public void setChatTitle(final String title) {
        ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatTitle.setText(title);
                notifyDataSetChanged();
            }
        });
    }

    public void setChatSubTitle(final String subTitle) {
        ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatSubText.setText(subTitle);
                notifyDataSetChanged();
            }
        });
    }

    public void setChatToolBar(final String subTitle, final Spannable title, final IconDrawable iconDrawable) {
        ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatIcon.setImageDrawable(iconDrawable);
                mChatTitle.setText(title);
                mChatSubText.setText(subTitle);
                notifyDataSetChanged();
            }
        });
    }

    public ToolBarAdapter(Context context) {
        this.mContext = context;
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
            return mSecondLayout;
        } else {
            return mFirstLayout;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void createItemTwo() {
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        relativeLayout.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, DP_56));
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        mChatIcon = new ImageView(mContext);
        mChatIcon.setId(R.id.i_toolbar_icon);
        mChatIcon.setPadding(DP_4, DP_4, DP_4, DP_4);
        relativeLayout.addView(mChatIcon);

        RelativeLayout.LayoutParams iconToolbarLP = (RelativeLayout.LayoutParams) mChatIcon.getLayoutParams();

        iconToolbarLP.height = IconFactory.Type.TITLE.getHeight();
        iconToolbarLP.width = IconFactory.Type.TITLE.getHeight();

        mChatTitle = new TextView(mContext);
        mChatTitle.setId(R.id.i_toolbar_title);
        mChatTitle.setSingleLine();
        mChatTitle.setEllipsize(TextUtils.TruncateAt.END);
        mChatTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChatTitle.setTextSize(18);
        mChatTitle.setTextColor(0xffffffff);

        relativeLayout.addView(mChatTitle);

        RelativeLayout.LayoutParams chatTitleLP = (RelativeLayout.LayoutParams) mChatTitle.getLayoutParams();
        chatTitleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatTitleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatTitleLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        chatTitleLP.addRule(RelativeLayout.RIGHT_OF, mChatIcon.getId());
        chatTitleLP.topMargin = DP_2;
        chatTitleLP.leftMargin = DP_5;

        mChatSubText = new TextView(mContext);
        mChatSubText.setSingleLine();
        mChatSubText.setEllipsize(TextUtils.TruncateAt.END);
        mChatSubText.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
        mChatSubText.setTextSize(14);
        mChatSubText.setTextColor(0xffd2eafc);

        relativeLayout.addView(mChatSubText);

        RelativeLayout.LayoutParams chatSubTextLP = (RelativeLayout.LayoutParams) mChatSubText.getLayoutParams();
        chatSubTextLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        chatSubTextLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;

        chatSubTextLP.addRule(RelativeLayout.RIGHT_OF, mChatIcon.getId());
        chatSubTextLP.addRule(RelativeLayout.BELOW, mChatTitle.getId());
        chatSubTextLP.leftMargin = DP_5;

        mSecondLayout = relativeLayout;
    }

    private void createItemOne() {
        mFirstLayout = new RelativeLayout(mContext);

        AbsListView.LayoutParams firstLayoutLP = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, DP_56);
        mFirstLayout.setLayoutParams(firstLayoutLP);
        mFirstLayout.setVerticalGravity(Gravity.CENTER);
        mMainTitle = new TextView(mContext);
        mFirstLayout.addView(mMainTitle);

        mMainTitle.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        mMainTitle.setTextColor(0xffffffff);
        mMainTitle.setTextSize(20);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (position) {
            case 1:
                return mSecondLayout;
            default:
                return mFirstLayout;
        }
    }
}
