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

public class EmojiconsPopup extends PopupWindow implements ViewPager.OnPageChangeListener, EmojiconRecents {

    public static int WINDOW_WIDTH;

    OnEmojiconClickedListener onEmojiconClickedListener;
    OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    View rootView;
    Context mContext;
    private int mEmojiTabLastSelectedIndex = -1;
    private PagerAdapter mEmojisAdapter;
    private EmojiconRecentsManager mRecentsManager;
    private int keyBoardHeight = 0;
    private Boolean isFirstOpening = false;
    private Boolean isOpenedKeyboard = false;
    private Boolean isOpenedPopup = false;
    private ViewPager emojisPager;


    public EmojiconsPopup(View rootView, Context mContext, EmojiconGridView stickerGridView, RecyclerView.Adapter stickerMicroViewAdapter) {
        super(mContext);
        this.mContext = mContext;
        this.rootView = rootView;
        this.stickerGridView = stickerGridView;
        View customView = createCustomView(stickerMicroViewAdapter);
        setContentView(customView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //default size
        setSize(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.keyboard_height));
        this.setBackgroundDrawable(new ColorDrawable());
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    public void setOnEmojiconClickedListener(OnEmojiconClickedListener listener) {
        this.onEmojiconClickedListener = listener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        this.onEmojiconBackspaceClickedListener = listener;
    }

    public OnEmojiconBackspaceClickedListener getOnEmojiconBackspaceClickedListener() {
        return this.onEmojiconBackspaceClickedListener;
    }

    public void showAtBottom() {
        isFirstOpening = false;
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);

    }

    public void showAtBottomFirstTime() {
        showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        isFirstOpening = true;
    }

    public Boolean isKeyBoardOpen() {
        return isOpenedKeyboard;
    }

    public Boolean isPopupOpen() {
        return isOpenedPopup;
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
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight;
                if (Build.VERSION.SDK_INT >= 5.0) {
                    screenHeight = calculateScreenHeightForLollipop();
                } else {
                    screenHeight = rootView.getRootView().getHeight();
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

                isOpenedPopup = rootView.getPaddingBottom() > 0;

                if (heightDifference > 100) {
                    keyBoardHeight = heightDifference;
                    setSize(LayoutParams.MATCH_PARENT, keyBoardHeight);
                    if (!isOpenedKeyboard) {
                        if (onSoftKeyboardOpenCloseListener != null)
                            onSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
                    }
                    isOpenedKeyboard = true;
                } else {
                    isOpenedKeyboard = false;
                    if (onSoftKeyboardOpenCloseListener != null && isShowing() && !isFirstOpening)
                        onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public EmojiconGridView stickerGridView;
    //public RelativeLayout stickerLayout;
    //public RelativeLayout emojiMainLayout;

    @SuppressLint("NewApi")
    private View createCustomView(RecyclerView.Adapter stickerMicroAdapter) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.emojicons, null, false);
        emojisPager = (ViewPager) view.findViewById(R.id.emojis_pager);
        SlidingTabLayout slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        RecyclerView stickerListView = (RecyclerView) view.findViewById(R.id.sticker_micro_thumb_list);
        stickerListView.setTranslationX(WINDOW_WIDTH);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //linearLayoutManager.setSmoothScrollbarEnabled(false);

        stickerListView.setLayoutManager(linearLayoutManager);
        StickerMicroThumbAdapter stickerMicroThumbAdapter = ((StickerMicroThumbAdapter) stickerMicroAdapter);

        stickerMicroThumbAdapter.setPager(emojisPager);
        stickerMicroThumbAdapter.setLayoutManager(linearLayoutManager);
        stickerMicroThumbAdapter.setStickerGridView(stickerGridView);
        stickerListView.setAdapter(stickerMicroAdapter);

        emojisPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        emojisPager.addOnPageChangeListener(this);
        emojisPager.setOffscreenPageLimit(7);
        EmojiconRecents recents = this;
        mEmojisAdapter = new EmojisPagerAdapter(
                Arrays.asList(
                        new EmojiconRecentsGridView(mContext, null, null, this),
                        new EmojiconGridView(mContext, People.DATA, recents, this),
                        new EmojiconGridView(mContext, Nature.DATA, recents, this),
                        new EmojiconGridView(mContext, Objects.DATA, recents, this),
                        new EmojiconGridView(mContext, Places.DATA, recents, this),
                        new EmojiconGridView(mContext, Symbols.DATA, recents, this),
                        stickerGridView
                ), mContext
        );
        emojisPager.setAdapter(mEmojisAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.emojiconsPopup = this;
        slidingTabLayout.setCustomTabView(R.layout.tab_emoji_item, 0);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setOnPageChangeListener(this);
        slidingTabLayout.setViewPager(emojisPager, slidingTabs, stickerListView);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return 0xff68ADE1;
            }
        });

        // get last selected page
        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext());
        int page = mRecentsManager.getRecentPage();
        if (page == 0 && mRecentsManager.size() == 0) {
            page = 1;
        }

        if (page == 0) {
            onPageSelected(page);
        } else {
            emojisPager.setCurrentItem(page, false);
        }
        return view;
    }

    public void setMaxOffscreenPageLimit() {
        //emojisPager.setOffscreenPageLimit(6);
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentsGridView fragment = ((EmojisPagerAdapter) emojisPager.getAdapter()).getRecentFragment();
        fragment.addRecentEmoji(context, emojicon);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //private volatile int screenOffsetCount = 0;

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

    //ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onPageScrollStateChanged(int state) {
        /*Log.e("state=", "" + state);
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (screenOffsetCount != 6) {
                screenOffsetCount = 6;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                        emojisPager.setOffscreenPageLimit(screenOffsetCount);
                    }
                });
            }
        }*/
    }

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen(int keyBoardHeight);

        void onKeyboardClose();
    }

    public static class EmojisPagerAdapter extends PagerAdapter {
        private List<EmojiconGridView> views;
        private Context context;

        public EmojisPagerAdapter(List<EmojiconGridView> views, Context context) {
            super();
            this.views = views;
            this.context = context;
        }

        public EmojiconRecentsGridView getRecentFragment() {
            for (int i = 0; i < views.size(); i++) {
                if (views.get(i) instanceof EmojiconRecentsGridView)
                    return (EmojiconRecentsGridView) views.get(i);
            }
            return null;
        }

        @Override
        public int getCount() {
            return views.size();
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
            if (position < views.size()) {
                v = views.get(position).gridView;
                ((Activity) this.context).runOnUiThread(new Runnable() {
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
            ((Activity) this.context).runOnUiThread(new Runnable() {
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

        private final int normalInterval;
        private final View.OnClickListener clickListener;
        private Handler handler = new Handler();
        private int initialInterval;
        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;

        public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    view.setSelected(true);
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    view.setSelected(false);
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }
}