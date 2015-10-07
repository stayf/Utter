package com.stayfprod.emojicon;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SlidingTabLayout extends HorizontalScrollView {
    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {
        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 4;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private boolean mDistributeEvenly;

    private ViewPager mViewPager;
    private SparseArray<String> mContentDescriptions = new SparseArray<String>();
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     * <p/>
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} to achieve
     * similar effects.
     */
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

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    private RecyclerView stickerListView;
    private SlidingTabLayout slidingTabs;

    public void setViewPager(ViewPager viewPager, SlidingTabLayout slidingTabs, RecyclerView stickerListView) {
        this.slidingTabs = slidingTabs;
        this.stickerListView = stickerListView;

        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    public EmojiconsPopup emojiconsPopup;

    private void populateTabStrip() {
        final EmojiconsPopup.EmojisPagerAdapter adapter = (EmojiconsPopup.EmojisPagerAdapter) mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount() + 1; i++) {
            View tabView = null;
            ImageView tabIconView = null;

            if (tabView == null) {
                tabView = createDefaultImageView(getContext());
            }

            if (tabIconView == null && ImageView.class.isInstance(tabView)) {
                tabIconView = (ImageView) tabView;
            }

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
                            /*ObjectAnimator slidingTabsAnim = ObjectAnimator.ofFloat(slidingTabs, "translationX", -EmojiconsPopup.WINDOW_WIDTH);
                            ObjectAnimator stickerScrollViewAnim = ObjectAnimator.ofFloat(stickerScrollView, "translationX", 0);

                            AnimatorSet animSet = new AnimatorSet();
                            animSet.playTogether(slidingTabsAnim, stickerScrollViewAnim);
                            animSet.setDuration(200);
                            animSet.start();*/
                        }
                    });
                } else {
                    tabView.setOnClickListener(tabClickListener);
                }

            } else {
                if (i == adapter.getCount()) {
                    tabView.setOnTouchListener(new EmojiconsPopup.RepeatListener(1000, 50, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (emojiconsPopup != null && emojiconsPopup.getOnEmojiconBackspaceClickedListener() != null) {
                                emojiconsPopup.getOnEmojiconBackspaceClickedListener().onEmojiconBackspaceClicked(v);
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
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;
        private int selectedPage;
        private int prevSelectedPage = -1;
        private boolean isChecked;

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

            if(stickerListView != null){
                if (position == 5 && positionOffsetPixels > 0) {
                    slidingTabs.setTranslationX(-EmojiconsPopup.WINDOW_WIDTH * positionOffset);
                    stickerListView.setTranslationX(EmojiconsPopup.WINDOW_WIDTH - EmojiconsPopup.WINDOW_WIDTH * positionOffset);
                }

                if (position == 6 && positionOffset == 0.0f) {
                    slidingTabs.setTranslationX(-EmojiconsPopup.WINDOW_WIDTH);
                    stickerListView.setTranslationX(0);
                }

                if (prevSelectedPage == 6 && selectedPage == 0 && position < 5) {
                    slidingTabs.setTranslationX(0);
                    stickerListView.setTranslationX(EmojiconsPopup.WINDOW_WIDTH);
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
            prevSelectedPage = selectedPage;
            selectedPage = position;
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