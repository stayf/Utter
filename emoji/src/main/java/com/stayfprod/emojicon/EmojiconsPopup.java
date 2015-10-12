package com.stayfprod.emojicon;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

import java.util.Arrays;
import java.util.List;

import com.stayfprod.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import com.stayfprod.emojicon.emoji.Emojicon;
import com.stayfprod.emojicon.emoji.Nature;
import com.stayfprod.emojicon.emoji.Objects;
import com.stayfprod.emojicon.emoji.People;
import com.stayfprod.emojicon.emoji.Places;
import com.stayfprod.emojicon.emoji.Symbols;

public class EmojIconsPopup extends PopupWindow implements ViewPager.OnPageChangeListener, EmojiconRecents {

    public static int sWindowWidth;

    private OnEmojiconBackspaceClickedListener mOnEmojiconBackspaceClickedListener;
    private OnSoftKeyboardOpenCloseListener mOnSoftKeyboardOpenCloseListener;
    private View mRootView;
    private Context mContext;
    private int mEmojiTabLastSelectedIndex = -1;
    private PagerAdapter mEmojisAdapter;
    private EmojiconRecentsManager mRecentsManager;
    private int mKeyBoardHeight = 0;
    private Boolean mIsFirstOpening = false;
    private Boolean mIsOpenedKeyboard = false;
    private Boolean mIsOpenedPopup = false;
    private ViewPager mEmojisPager;

    public EmojiconGridView stickerGridView;
    public OnEmojiconClickedListener onEmojiconClickedListener;

    public EmojIconsPopup(View rootView, Context mContext, EmojiconGridView stickerGridView, RecyclerView.Adapter stickerMicroViewAdapter) {
        super(mContext);
        this.mContext = mContext;
        this.mRootView = rootView;
        this.stickerGridView = stickerGridView;
        View customView = createCustomView(stickerMicroViewAdapter);
        setContentView(customView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.keyboard_height));
        this.setBackgroundDrawable(new ColorDrawable());
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.mOnSoftKeyboardOpenCloseListener = listener;
    }

    public void setOnEmojiconClickedListener(OnEmojiconClickedListener listener) {
        this.onEmojiconClickedListener = listener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        this.mOnEmojiconBackspaceClickedListener = listener;
    }

    public OnEmojiconBackspaceClickedListener getOnEmojiconBackspaceClickedListener() {
        return this.mOnEmojiconBackspaceClickedListener;
    }

    public void showAtBottom() {
        mIsFirstOpening = false;
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
    }

    public void showAtBottomFirstTime() {
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        mIsFirstOpening = true;
    }

    public Boolean isKeyBoardOpen() {
        return mIsOpenedKeyboard;
    }

    public Boolean isPopupOpen() {
        return mIsOpenedPopup;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EmojiconRecentsManager.getInstance(mContext).saveRecents();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public int calculateScreenHeightForLollipop() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public void setSizeForSoftKeyboard() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);
                int screenHeight;
                if (Build.VERSION.SDK_INT >= 5.0) {
                    screenHeight = calculateScreenHeightForLollipop();
                } else {
                    screenHeight = mRootView.getRootView().getHeight();
                }
                int heightDifference = screenHeight
                        - (r.bottom - r.top);

                int resourceId = mContext.getResources()
                        .getIdentifier("status_bar_height",
                                "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources()
                            .getDimensionPixelSize(resourceId);
                }

                mIsOpenedPopup = mRootView.getPaddingBottom() > 0;

                if (heightDifference > 100) {
                    mKeyBoardHeight = heightDifference;
                    setSize(LayoutParams.MATCH_PARENT, mKeyBoardHeight);
                    if (!mIsOpenedKeyboard) {
                        if (mOnSoftKeyboardOpenCloseListener != null)
                            mOnSoftKeyboardOpenCloseListener.onKeyboardOpen(mKeyBoardHeight);
                    }
                    mIsOpenedKeyboard = true;
                } else {
                    mIsOpenedKeyboard = false;
                    if (mOnSoftKeyboardOpenCloseListener != null && isShowing() && !mIsFirstOpening)
                        mOnSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    @SuppressLint("NewApi")
    private View createCustomView(RecyclerView.Adapter stickerMicroAdapter) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.emojicons, null, false);
        mEmojisPager = (ViewPager) view.findViewById(R.id.emojis_pager);
        SlidingTabLayout slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        RecyclerView stickerListView = (RecyclerView) view.findViewById(R.id.sticker_micro_thumb_list);
        stickerListView.setTranslationX(sWindowWidth);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);

        stickerListView.setLayoutManager(linearLayoutManager);
        StickerMicroThumbAdapter stickerMicroThumbAdapter = ((StickerMicroThumbAdapter) stickerMicroAdapter);

        stickerMicroThumbAdapter.setPager(mEmojisPager);
        stickerMicroThumbAdapter.setLayoutManager(linearLayoutManager);
        stickerMicroThumbAdapter.setStickerGridView(stickerGridView);
        stickerListView.setAdapter(stickerMicroAdapter);

        mEmojisPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mEmojisPager.addOnPageChangeListener(this);
        mEmojisPager.setOffscreenPageLimit(7);
        EmojiconRecents recents = this;
        mEmojisAdapter = new EmojisPagerAdapter(
                Arrays.asList(
                        new EmojiconRecentsGridView(mContext, this),
                        new EmojiconGridView(mContext, People.DATA, recents, this),
                        new EmojiconGridView(mContext, Nature.DATA, recents, this),
                        new EmojiconGridView(mContext, Objects.DATA, recents, this),
                        new EmojiconGridView(mContext, Places.DATA, recents, this),
                        new EmojiconGridView(mContext, Symbols.DATA, recents, this),
                        stickerGridView
                ), mContext
        );
        mEmojisPager.setAdapter(mEmojisAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.emojIconsPopup = this;
        slidingTabLayout.setCustomTabView(R.layout.tab_emoji_item, 0);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setOnPageChangeListener(this);
        slidingTabLayout.setViewPager(mEmojisPager, slidingTabs, stickerListView);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return 0xff68ADE1;
            }
        });

        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext());
        int page = mRecentsManager.getRecentPage();
        if (page == 0 && mRecentsManager.size() == 0) {
            page = 1;
        }

        if (page == 0) {
            onPageSelected(page);
        } else {
            mEmojisPager.setCurrentItem(page, false);
        }
        return view;
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentsGridView fragment = ((EmojisPagerAdapter) mEmojisPager.getAdapter()).getRecentFragment();
        fragment.addRecentEmoji(context, emojicon);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int i) {
        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                mEmojiTabLastSelectedIndex = i;
                mRecentsManager.setRecentPage(i);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen(int keyBoardHeight);

        void onKeyboardClose();
    }

    public static class EmojisPagerAdapter extends PagerAdapter {
        private List<EmojiconGridView> mViews;
        private Context mContext;

        public EmojisPagerAdapter(List<EmojiconGridView> views, Context context) {
            super();
            this.mViews = views;
            this.mContext = context;
        }

        public EmojiconRecentsGridView getRecentFragment() {
            for (int i = 0; i < mViews.size(); i++) {
                if (mViews.get(i) instanceof EmojiconRecentsGridView)
                    return (EmojiconRecentsGridView) mViews.get(i);
            }
            return null;
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        private int[] imageResId = {
                R.drawable.ic_emoji_recent_light,
                R.drawable.ic_emoji_people_light,
                R.drawable.ic_emoji_nature_light,
                R.drawable.ic_emoji_objects_light,
                R.drawable.ic_emoji_places_light,
                R.drawable.ic_emoji_symbols_light,
                R.drawable.ic_emoji_stickers_light,
                R.drawable.ic_emoji_backspace_light
        };

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        public int getDrawableId(int position) {
            return imageResId[position];
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            final View v;
            if (position < mViews.size()) {
                v = mViews.get(position).gridView;
                ((Activity) this.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.addView(v, 0);
                    }
                });
            } else {
                v = null;
            }
            return v;
        }

        @Override
        public void destroyItem(final ViewGroup container, int position, final Object view) {
            ((Activity) this.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    container.removeView((View) view);
                }
            });
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }
    }

    public static class RepeatListener implements View.OnTouchListener {

        private final int mNormalInterval;
        private final View.OnClickListener mClickListener;
        private Handler mHandler = new Handler();
        private int mInitialInterval;
        private View mDownView;

        private Runnable mHandlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (mDownView == null) {
                    return;
                }
                mHandler.removeCallbacksAndMessages(mDownView);
                mHandler.postAtTime(this, mDownView, SystemClock.uptimeMillis() + mNormalInterval);
                mClickListener.onClick(mDownView);
            }
        };

        public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.mInitialInterval = initialInterval;
            this.mNormalInterval = normalInterval;
            this.mClickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownView = view;
                    view.setSelected(true);
                    mHandler.removeCallbacks(mHandlerRunnable);
                    mHandler.postAtTime(mHandlerRunnable, mDownView, SystemClock.uptimeMillis() + mInitialInterval);
                    mClickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    view.setSelected(false);
                    mHandler.removeCallbacksAndMessages(mDownView);
                    mDownView = null;
                    return true;
            }
            return false;
        }
    }
}