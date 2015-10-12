package com.stayfprod.emojicon;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class SlidingTabLayout extends HorizontalScrollView {

    public interface TabColorizer {
        int getIndicatorColor(int position);
    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 4;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private boolean mDistributeEvenly;

    private RecyclerView mStickerListView;
    private SlidingTabLayout mSlidingTabs;

    private ViewPager mViewPager;
    private SparseArray<String> mContentDescriptions = new SparseArray<String>();
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;
    public EmojIconsPopup emojIconsPopup;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    public void setDistributeEvenly(boolean distributeEvenly) {
        mDistributeEvenly = distributeEvenly;
    }

    protected ImageView createDefaultImageView(Context context) {
        ImageView imageView = new ImageView(context);

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);

        int width = (int) (getResources().getDisplayMetrics().widthPixels / (mViewPager.getAdapter().getCount() + 1));
        imageView.setMinimumWidth(width);

        return imageView;
    }

    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    public void setViewPager(ViewPager viewPager, SlidingTabLayout slidingTabs, RecyclerView stickerListView) {
        this.mSlidingTabs = slidingTabs;
        this.mStickerListView = stickerListView;

        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private void populateTabStrip() {
        final EmojIconsPopup.EmojisPagerAdapter adapter = (EmojIconsPopup.EmojisPagerAdapter) mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount() + 1; i++) {
            View tabView = null;
            ImageView tabIconView = null;
            tabView = createDefaultImageView(getContext());

            if (ImageView.class.isInstance(tabView)) {
                tabIconView = (ImageView) tabView;
            } else
                continue;

            tabIconView.setImageDrawable(getResources().getDrawable(adapter.getDrawableId(i)));
            if (mViewPager.getCurrentItem() == i) {
                tabIconView.setSelected(true);
            }

            if (i < adapter.getCount()) {
                if (i == adapter.getCount() - 1) {
                    tabView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tabClickListener.onClick(view);
                        }
                    });
                } else {
                    tabView.setOnClickListener(tabClickListener);
                }
            } else {
                if (i == adapter.getCount()) {
                    tabView.setOnTouchListener(new EmojIconsPopup.RepeatListener(1000, 50, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (emojIconsPopup != null && emojIconsPopup.getOnEmojiconBackspaceClickedListener() != null) {
                                emojIconsPopup.getOnEmojiconBackspaceClickedListener().onEmojiconBackspaceClicked(v);
                            }
                        }
                    }));
                }
            }
            mTabStrip.addView(tabView);
        }
    }

    public void setContentDescription(int i, String desc) {
        mContentDescriptions.put(i, desc);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;
        private int mSelectedPage;
        private int mPrevSelectedPage = -1;
        private boolean mIsChecked;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }

            if (mStickerListView != null) {
                if (position == 5 && positionOffsetPixels > 0) {
                    mSlidingTabs.setTranslationX(-EmojIconsPopup.sWindowWidth * positionOffset);
                    mStickerListView.setTranslationX(EmojIconsPopup.sWindowWidth - EmojIconsPopup.sWindowWidth * positionOffset);
                }

                if (position == 6 && positionOffset == 0.0f) {
                    mSlidingTabs.setTranslationX(-EmojIconsPopup.sWindowWidth);
                    mStickerListView.setTranslationX(0);
                }

                if (mPrevSelectedPage == 6 && mSelectedPage == 0 && position < 5) {
                    mSlidingTabs.setTranslationX(0);
                    mStickerListView.setTranslationX(EmojIconsPopup.sWindowWidth);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            mPrevSelectedPage = mSelectedPage;
            mSelectedPage = position;
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(position == i);
            }
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

}