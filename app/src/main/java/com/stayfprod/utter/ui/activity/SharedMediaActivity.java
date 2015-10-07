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

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;
import java.util.Observer;

public class SharedMediaActivity extends AbstractActivity implements Observer {

    public static final int SHARED_MEDIA = 0;
    public static final int SHARED_AUDIO = 1;

    public static boolean isOpenedSharedMediaActivity;

    private Spinner spinner;
    private DisabledViewPager mPager;
    private LayoutInflater mLayoutInflater;

    public static int MAX_ROW_SPANS;

    public Toolbar forwardToolbar;
    public ImageView t_forward_cancel;
    public ImageView t_forward_delete;
    public ImageView t_forward_forward;
    public TextView t_forward_counter;

    private SharedMediaAdapter sharedMediaAdapter;
    private SharedAudioAdapter sharedAudioAdapter;

    private int selectedPage;
    public long chatId;
    private boolean openPlayerByBack;

    private MusicBarWidget musicBarWidget;

    @Override
    protected void onStart() {
        isOpenedSharedMediaActivity = true;
        super.onStart();
        musicBarWidget.checkOnStart();
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        audioPlayer.addObserver(this);

        if (audioPlayer.isNeedUpdateSharedAudioActivity()) {
            audioPlayer.setIsNeedUpdateSharedAudioActivity(false);
            if (sharedAudioAdapter != null)
                sharedAudioAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        isOpenedSharedMediaActivity = false;
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
        MAX_ROW_SPANS = SharedMediaActivity.WINDOW_CURRENT_WIDTH / SharedMediaAdapter.LAYOUT_HEIGHT;
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
            selectedPage = bundle.getInt("selectedPage", SHARED_MEDIA);
            chatId = bundle.getLong("chatId", 0L);
            openPlayerByBack = bundle.getBoolean("openPlayerByBack", false);
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
                        RecyclerView a_shared_media_list = (RecyclerView) itemView.findViewById(R.id.a_shared_media_list);

                        MAX_ROW_SPANS = SharedMediaActivity.WINDOW_CURRENT_WIDTH / SharedMediaAdapter.LAYOUT_HEIGHT;
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(SharedMediaActivity.this, MAX_ROW_SPANS);

                        sharedMediaManager.cleanSearchMedia(false, true);
                        sharedMediaAdapter = new SharedMediaAdapter(sharedMediaManager.getPhotoAndVideoSharedMessages(), SharedMediaActivity.this, gridLayoutManager);

                        a_shared_media_list.setLayoutManager(gridLayoutManager);
                        a_shared_media_list.setAdapter(sharedMediaAdapter);

                        sharedMediaManager.searchMedia(chatId, UserManager.getManager().getCurrUserId(), false, true);

                        break;
                    case SHARED_AUDIO:
                        itemView = mLayoutInflater.inflate(R.layout.pager_shared_music, container, false);
                        RecyclerView a_shared_music_list = (RecyclerView) itemView.findViewById(R.id.a_shared_music_list);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SharedMediaActivity.this);
                        sharedMediaManager.cleanSearchAudio();
                        sharedAudioAdapter = new SharedAudioAdapter(sharedMediaManager.getAudioMessages(), SharedMediaActivity.this, linearLayoutManager);
                        a_shared_music_list.setLayoutManager(linearLayoutManager);
                        a_shared_music_list.setAdapter(sharedAudioAdapter);

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
                spinner.setSelection(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPager.setCurrentItem(selectedPage);

        musicBarWidget = new MusicBarWidget();
        musicBarWidget.init(this);
        musicBarWidget.setOpenPlayerByBack(openPlayerByBack);

    }

    private void initForwardToolbar() {
        forwardToolbar = (Toolbar) findViewById(R.id.a_actionBarOne);

        t_forward_cancel = (ImageView) forwardToolbar.findViewById(R.id.t_forward_cancel);
        t_forward_counter = (TextView) forwardToolbar.findViewById(R.id.t_forward_counter);

        t_forward_delete = (ImageView) forwardToolbar.findViewById(R.id.t_forward_delete);
        t_forward_forward = (ImageView) forwardToolbar.findViewById(R.id.t_forward_forward);

        t_forward_counter.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
        t_forward_counter.setTextColor(Color.WHITE);
        t_forward_counter.setTextSize(20);
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        initForwardToolbar();
        forwardToolbar.setVisibility(View.GONE);
        Toolbar mainToolBar = (Toolbar) findViewById(R.id.a_actionBarTwo);

        spinner = (Spinner) findViewById(R.id.t_shared_spinner);
        spinner.setBackgroundColor(Color.WHITE);
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
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                selectedPage = position;
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
        /*SharedMediaManager.getManager().cleanSelectedMusic();
        SharedMediaManager.getManager().cleanSelectedMedia();*/
        finish();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            switch (nObject.getMessageCode()) {
                case NotificationObject.UPDATE_SHARED_MEDIA_PAGE: {
                    selectedPage = (int) nObject.getWhat();
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            mPager.setCurrentItem(selectedPage);
                        }
                    });

                    break;
                }
                case NotificationObject.UPDATE_MUSIC_PLAYER: {
                    musicBarWidget.checkUpdate((Object[]) nObject.getWhat());
                    break;
                }
                case NotificationObject.UPDATE_SHARED_MEDIA_LIST:
                case NotificationObject.UPDATE_JUST_SHARED_MEDIA_LIST:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedMediaAdapter != null) {
                                sharedMediaAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;
                case NotificationObject.UPDATE_SHARED_AUDIO_LIST:
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedAudioAdapter != null) {
                                sharedAudioAdapter.notifyDataSetChanged();
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
                            if (sharedAudioAdapter != null) {
                                sharedAudioAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;
                }
            }
        }
    }
}
