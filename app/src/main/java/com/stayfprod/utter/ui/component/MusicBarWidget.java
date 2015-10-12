package com.stayfprod.utter.ui.component;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.ui.activity.MusicPlayerActivity;
import com.stayfprod.utter.ui.activity.SharedMediaActivity;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.ui.view.MusicBar;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class MusicBarWidget {

    private RelativeLayout mMusicBarLayout;
    private MusicBar mMusicBar;
    private long mChatId;
    private boolean mOpenPlayerByBack;

    public void setOpenPlayerByBack(boolean openPlayerByBack) {
        this.mOpenPlayerByBack = openPlayerByBack;
    }

    public void checkOnStart() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.isPaused()) {
            mMusicBar.stop();
            mMusicBar.setProgress(audioPlayer.getCurrentProgress());
            mMusicBar.setName(audioPlayer.getMessageAudio().audio.performer + " - " + audioPlayer.getMessageAudio().audio.title);
            mMusicBar.invalidateUI();
            showMusicBar();
        } else {
            hideMusicBar();
        }
    }

    public void init(final AbstractActivity abstractActivity) {
        mMusicBarLayout = abstractActivity.findView(R.id.a_music_bar_layout);
        mMusicBar = (MusicBar) mMusicBarLayout.findViewById(R.id.music_bar);
        final AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        mMusicBar.setOnCloseClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.stop();
            }
        });

        mMusicBar.setOnPlayClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isPlaying())
                    audioPlayer.pause();
                else
                    audioPlayer.resume();
            }
        });

        mMusicBarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //уход в активити
                if (mOpenPlayerByBack) {
                    abstractActivity.supportFinishAfterTransition();
                    abstractActivity.overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                } else {
                    Intent intent = new Intent(mMusicBarLayout.getContext(), MusicPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isBackOnTouchList", SharedMediaActivity.sIsOpenedSharedMediaActivity);
                    bundle.putLong("chatId", mChatId);
                    intent.putExtras(bundle);
                    if (SharedMediaActivity.sIsOpenedSharedMediaActivity) {
                        mMusicBarLayout.getContext().startActivity(intent);
                        ((AbstractActivity) mMusicBarLayout.getContext()).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    } else {
                        mMusicBarLayout.getContext().startActivity(intent);
                    }
                }
            }
        });
    }

    public void showMusicBar() {
        mMusicBarLayout.setVisibility(View.VISIBLE);
        mMusicBarLayout.setTranslationY(0);
    }

    public void hideMusicBar() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mMusicBarLayout, "translationY", 0, -AndroidUtil.dp(43));
        objectAnimator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMusicBarLayout.setVisibility(View.GONE);
            }
        });
        objectAnimator.setDuration(150).start();
        if (mMusicBar != null) {
            mMusicBar.setName("");
        }
    }

    public void checkUpdate(Object[] objects) {
        int action = (int) objects[0];
        switch (action) {
            case AudioPlayer.ACTION_HIDE_BAR:
                hideMusicBar();
                break;
            case AudioPlayer.ACTION_SHOW_BAR:
                updateAll(objects);
                break;
            case AudioPlayer.ACTION_PROGRESS:
                updateAll(objects);
                break;
            case AudioPlayer.ACTION_UPLOAD_NEW_TRACK:

                break;
        }
    }

    private void updateAll(Object[] objects) {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) objects[1];

        if ((messageAudio != null && (mMusicBar.isEmptyDrawName() || (mMusicBar.getTag() != null && ((int) mMusicBar.getTag()) != messageAudio.audio.audio.id)))) {
            mMusicBar.setName(messageAudio.audio.performer + " - " + messageAudio.audio.title);
            mMusicBar.setTag(messageAudio.audio.audio.id);
            TdApi.Message message = (TdApi.Message) objects[3];
            mChatId = message.chatId;
            SharedMediaManager.getManager().searchAudioPlayer(mChatId, audioPlayer.getAudioMessages());

        }

        float progress = (float) objects[2];
        if (progress != -1f)
            mMusicBar.setProgress(progress);
        if (audioPlayer.isPlaying()) {
            mMusicBar.play();
            if (mMusicBarLayout.getVisibility() == View.GONE) {
                showMusicBar();
            }
        } else {
            mMusicBar.stop();
        }
        mMusicBar.invalidateUI();
    }

}
