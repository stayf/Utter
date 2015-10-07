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

    private RelativeLayout a_music_bar_layout;
    private MusicBar musicBar;
    private long chatId;
    private boolean openPlayerByBack;

    public void setOpenPlayerByBack(boolean openPlayerByBack) {
        this.openPlayerByBack = openPlayerByBack;
    }

    public void checkOnStart() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.isPaused()) {
            musicBar.stop();
            musicBar.setProgress(audioPlayer.getCurrentProgress());
            musicBar.setName(audioPlayer.getMessageAudio().audio.performer + " - " + audioPlayer.getMessageAudio().audio.title);
            musicBar.invalidateUI();
            showMusicBar();
        } else {
            hideMusicBar();
        }
    }

    public void init(final AbstractActivity abstractActivity) {
        a_music_bar_layout = abstractActivity.findView(R.id.a_music_bar_layout);
        musicBar = (MusicBar) a_music_bar_layout.findViewById(R.id.music_bar);
        final AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        musicBar.setOnCloseClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.stop();
            }
        });

        musicBar.setOnPlayClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isPlaying())
                    audioPlayer.pause();
                else
                    audioPlayer.resume();
            }
        });

        a_music_bar_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //уход в активити
                if (openPlayerByBack) {
                    abstractActivity.supportFinishAfterTransition();
                    abstractActivity.overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                } else {
                    Intent intent = new Intent(a_music_bar_layout.getContext(), MusicPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isBackOnTouchList", SharedMediaActivity.isOpenedSharedMediaActivity);
                    bundle.putLong("chatId", chatId);
                    intent.putExtras(bundle);
                    if (SharedMediaActivity.isOpenedSharedMediaActivity) {
                        a_music_bar_layout.getContext().startActivity(intent);
                        ((AbstractActivity) a_music_bar_layout.getContext()).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    } else {
                        a_music_bar_layout.getContext().startActivity(intent);
                    }
                }
            }
        });
    }

    public void showMusicBar() {
        a_music_bar_layout.setVisibility(View.VISIBLE);
        a_music_bar_layout.setTranslationY(0);
        /*ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(a_music_bar_layout, "translationY", -AndroidUtil.dp(43), 0);
        objectAnimator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        objectAnimator.setDuration(150).start();*/
    }

    public void hideMusicBar() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(a_music_bar_layout, "translationY", 0, -AndroidUtil.dp(43));
        objectAnimator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                a_music_bar_layout.setVisibility(View.GONE);
            }
        });
        objectAnimator.setDuration(150).start();
        if (musicBar != null) {
            musicBar.setName("");
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
                //updateAll(objects);
                break;
        }
    }

    private void updateAll(Object[] objects) {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) objects[1];

        if ((messageAudio != null && (musicBar.isEmptyDrawName() || (musicBar.getTag() != null && ((int) musicBar.getTag()) != messageAudio.audio.audio.id)))) {
            musicBar.setName(messageAudio.audio.performer + " - " + messageAudio.audio.title);
            musicBar.setTag(messageAudio.audio.audio.id);
            TdApi.Message message = (TdApi.Message) objects[3];
            chatId = message.chatId;
            SharedMediaManager.getManager().searchAudioPlayer(chatId, audioPlayer.getAudioMessages());

        }

        float progress = (float) objects[2];
        if (progress != -1f)
            musicBar.setProgress(progress);
        if (audioPlayer.isPlaying()) {
            musicBar.play();
            if (a_music_bar_layout.getVisibility() == View.GONE) {
                showMusicBar();
            }
        } else {
            musicBar.stop();
        }
        musicBar.invalidateUI();
    }

}
