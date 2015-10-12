package com.stayfprod.utter.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.adapter.SharedAudioAdapter;
import com.stayfprod.utter.ui.adapter.SharedMediaAdapter;
import com.stayfprod.utter.ui.component.MusicBarWidget;
import com.stayfprod.utter.ui.view.DisabledViewPager;
import com.stayfprod.utter.util.AndroidUtil;

import java.util.Observable;
import java.util.Observer;

public class SharedMediaActivity extends AbstractActivity implements Observer {

    public static final int SHARED_MEDIA = 0;
    public static final int SHARED_AUDIO = 1;

    public static boolean sIsOpenedSharedMediaActivity;
    public static int sMaxRowSpans;

    private Spinner mSpinner;
    private DisabledViewPager mPager;
    private LayoutInflater mLayoutInflater;
    private SharedMediaAdapter mSharedMediaAdapter;
    private SharedAudioAdapter mSharedAudioAdapter;
    private MusicBarWidget mMusicBarWidget;
    private int mSelectedPage;
    private boolean mOpenPlayerByBack;

    public Toolbar forwardToolbar;
    public ImageView forwardCancel;
    public ImageView forwardDelete;
    public ImageView forwardImage;
    public TextView forwardCounter;
    public long chatId;

    @Override
    protected void onStart() {
        sIsOpenedSharedMediaActivity = true;
        super.onStart();
        mMusicBarWidget.checkOnStart();
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        audioPlayer.addObserver(this);

        if (audioPlayer.isNeedUpdateSharedAudioActivity()) {
            audioPlayer.setIsNeedUpdateSharedAudioActivity(false);
            if (mSharedAudioAdapter != null)
                mSharedAudioAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        sIsOpenedSharedMediaActivity = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        SharedMediaManager.getManager().deleteObserver(this);
        AudioPlayer.getPlayer().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sMaxRowSpans = SharedMediaActivity.sWindowCurrentWidth / SharedMediaAdapter.LAYOUT_HEIGHT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        SharedMediaManager.getManager().addObserver(this);

        setContentView(R.layout.activity_shared_media);

        setToolbar();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mSelectedPage = bundle.getInt("selectedPage", SHARED_MEDIA);
            chatId = bundle.getLong("chatId", 0L);
            mOpenPlayerByBack = bundle.getBoolean("openPlayerByBack", false);
        }

        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPager = (DisabledViewPager) findViewById(R.id.a_shared_pager);

        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @SuppressLint("NewApi")
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = null;
                SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                switch (position) {
                    case SHARED_MEDIA:
                    default:
                        itemView = mLayoutInflater.inflate(R.layout.pager_shared_media, container, false);
                        RecyclerView sharedMediaList = (RecyclerView) itemView.findViewById(R.id.a_shared_media_list);

                        sMaxRowSpans = SharedMediaActivity.sWindowCurrentWidth / SharedMediaAdapter.LAYOUT_HEIGHT;
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(SharedMediaActivity.this, sMaxRowSpans);

                        sharedMediaManager.cleanSearchMedia(false, true);
                        mSharedMediaAdapter = new SharedMediaAdapter(sharedMediaManager.getPhotoAndVideoSharedMessages(), SharedMediaActivity.this, gridLayoutManager);

                        sharedMediaList.setLayoutManager(gridLayoutManager);
                        sharedMediaList.setAdapter(mSharedMediaAdapter);

                        sharedMediaManager.searchMedia(chatId, UserManager.getManager().getCurrUserId(), false, true);

                        break;
                    case SHARED_AUDIO:
                        itemView = mLayoutInflater.inflate(R.layout.pager_shared_music, container, false);
                        RecyclerView sharedMusicList = (RecyclerView) itemView.findViewById(R.id.a_shared_music_list);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SharedMediaActivity.this);
                        sharedMediaManager.cleanSearchAudio();
                        mSharedAudioAdapter = new SharedAudioAdapter(sharedMediaManager.getAudioMessages(), SharedMediaActivity.this, linearLayoutManager);
                        sharedMusicList.setLayoutManager(linearLayoutManager);
                        sharedMusicList.setAdapter(mSharedAudioAdapter);

                        sharedMediaManager.searchAudio(chatId, UserManager.getManager().getCurrUserId());
                        break;
                }
                container.addView(itemView);
                return itemView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        };

        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSpinner.setSelection(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPager.setCurrentItem(mSelectedPage);

        mMusicBarWidget = new MusicBarWidget();
        mMusicBarWidget.init(this);
        mMusicBarWidget.setOpenPlayerByBack(mOpenPlayerByBack);

    }

    private void initForwardToolbar() {
        forwardToolbar = (Toolbar) findViewById(R.id.a_actionBarOne);

        forwardCancel = (ImageView) forwardToolbar.findViewById(R.id.t_forward_cancel);
        forwardCounter = (TextView) forwardToolbar.findViewById(R.id.t_forward_counter);

        forwardDelete = (ImageView) forwardToolbar.findViewById(R.id.t_forward_delete);
        forwardImage = (ImageView) forwardToolbar.findViewById(R.id.t_forward_forward);

        forwardCounter.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        forwardCounter.setTextColor(Color.WHITE);
        forwardCounter.setTextSize(20);
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        initForwardToolbar();
        forwardToolbar.setVisibility(View.GONE);
        Toolbar mainToolBar = (Toolbar) findViewById(R.id.a_actionBarTwo);

        mSpinner = (Spinner) findViewById(R.id.t_shared_spinner);
        mSpinner.setBackgroundColor(Color.WHITE);
        CharSequence[] strings = getResources().getTextArray(R.array.shared_list);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, strings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                view.setTextColor(0xFF222222);
                view.setTextSize(20);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                view.setTextColor(0xFF212121);
                view.setTextSize(20);
                view.setBackgroundResource(R.drawable.item_click_white);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                mSelectedPage = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setSupportActionBar(mainToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolBar.setNavigationIcon(R.mipmap.ic_back_grey);
        mainToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedMediaManager.getManager().deleteObserver(this);
        AudioPlayer.getPlayer().deleteObserver(this);
        SharedMediaManager.getManager().cleanSelectedMusic();
        SharedMediaManager.getManager().cleanSelectedMedia();
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }


    private void forceClose() {
        SharedMediaManager.getManager().deleteObserver(this);
        AudioPlayer.getPlayer().deleteObserver(this);
        //info очищаяю после отправки
        finish();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_SHARED_MEDIA_PAGE: {
                    mSelectedPage = (int) nObject.getWhat();
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            mPager.setCurrentItem(mSelectedPage);
                        }
                    });

                    break;
                }
                case NotificationObject.UPDATE_MUSIC_PLAYER: {
                    mMusicBarWidget.checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.UPDATE_SHARED_MEDIA_LIST:
                case NotificationObject.UPDATE_JUST_SHARED_MEDIA_LIST:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mSharedMediaAdapter != null) {
                                mSharedMediaAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;
                case NotificationObject.UPDATE_SHARED_AUDIO_LIST:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mSharedAudioAdapter != null) {
                                mSharedAudioAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;

                case NotificationObject.FORCE_CLOSE_SHARED_ACTIVITY:
                    forceClose();
                    break;

                case NotificationObject.UPDATE_MUSIC_PHOTO_AND_TAG: {
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (mSharedAudioAdapter != null) {
                                mSharedAudioAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;
                }
            }
        }
    }
}
