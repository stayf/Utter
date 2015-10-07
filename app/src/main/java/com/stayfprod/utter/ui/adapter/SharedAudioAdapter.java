package com.stayfprod.utter.ui.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.model.LoadingContentType;
import com.stayfprod.utter.model.SharedMusic;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.activity.IntermediateActivity;
import com.stayfprod.utter.ui.activity.SharedMediaActivity;
import com.stayfprod.utter.ui.drawable.DeterminateProgressDrawable;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.ui.view.DetermineProgressView;
import com.stayfprod.utter.ui.view.SelectedCircleView;
import com.stayfprod.utter.ui.view.UnselectedCircleView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class SharedAudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LAYOUT_HEIGHT = Constant.DP_64;

    private static final int TYPE_DATE = 1;
    private static final int TYPE_MUSIC = 2;

    private boolean selectedMode = false;

    private List<SharedMusic> musicList;
    private Context context;
    private LinearLayoutManager linearLayoutManager;

    public SharedAudioAdapter(List<SharedMusic> musicList, Context context, LinearLayoutManager linearLayoutManager) {
        this.musicList = musicList;
        this.context = context;
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DATE: {
                return new DateHolder();
            }
            case TYPE_MUSIC:
            default: {
                return new MusicHolder();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return musicList.get(position).isDivider ? TYPE_DATE : TYPE_MUSIC;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        SharedMusic sharedMusic = musicList.get(position);
        if (sharedMusic.isDivider) {
            DateHolder dateHolder = (DateHolder) holder;
            dateHolder.data.setText(sharedMusic.date);
        } else {
            MusicHolder musicHolder = (MusicHolder) holder;
            musicHolder.nameView.setText(sharedMusic.name);
            musicHolder.performerView.setText(sharedMusic.performer);
            sharedMusic.pos = position;
            if (selectedMode) {
                musicHolder.progressView.setVisibility(View.GONE);
                musicHolder.unselectedCircleView.setVisibility(View.VISIBLE);

                if (sharedMusic.isSelected) {
                    musicHolder.itemView.setBackgroundColor(0xFFF5F5F5);
                    musicHolder.selectedCircleView.showButton();
                    musicHolder.selectedCircleView.setScaleX(1.0f);
                    musicHolder.selectedCircleView.setScaleY(1.0f);
                } else {
                    musicHolder.itemView.setBackgroundResource(R.drawable.item_click_transparent);
                    musicHolder.selectedCircleView.hideButton();
                }

            } else {
                musicHolder.progressView.setVisibility(View.VISIBLE);
                musicHolder.unselectedCircleView.setVisibility(View.GONE);
                musicHolder.selectedCircleView.setVisibility(View.GONE);
                musicHolder.itemView.setBackgroundResource(R.drawable.item_click_transparent);
            }


            final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) sharedMusic.message.message;

            DeterminateProgressDrawable.LoadStatus loadStatus = null;
            DeterminateProgressDrawable.PlayStatus playStatus = null;

            int processLoad = -1;
            AudioPlayer player = AudioPlayer.getPlayer();
            if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
                if (player.getPlayingFile().equals(messageAudio.audio.audio.path) && player.getMsgId() == sharedMusic.message.id) {
                    if (player.isPlaying()) {
                        playStatus = DeterminateProgressDrawable.PlayStatus.PAUSE;
                    } else {
                        playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
                    }
                    player.rebuildLinks(sharedMusic.message, musicHolder.progressView);
                } else {
                    player.cleanLinks(musicHolder.progressView);
                    playStatus = DeterminateProgressDrawable.PlayStatus.PLAY;
                }
            } else {
                FileManager fileManager = FileManager.getManager();

                if (fileManager.isHaveStorageObjectByFileID(messageAudio.audio.audio.id, sharedMusic.message.id)) {
                    FileManager.StorageObject storageObject = fileManager.updateStorageObjectAsync(FileManager.TypeLoad.SHARED_AUDIO,
                            messageAudio.audio.audio.id, sharedMusic.message.id, holder.itemView, messageAudio.audio,
                            holder.itemView.getTag().toString(), musicHolder.nameView, musicHolder.progressView);
                    if (storageObject != null) {
                        if (storageObject.isCanceled)
                            loadStatus = DeterminateProgressDrawable.LoadStatus.PAUSE;
                        else
                            loadStatus = DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD;
                        processLoad = storageObject.processLoad;
                        musicHolder.nameView.setText(storageObject.loadMsg[0]);
                    } else {
                        loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                    }
                } else {
                    loadStatus = DeterminateProgressDrawable.LoadStatus.NO_LOAD;
                }
                player.cleanLinks(musicHolder.progressView);
            }

            DeterminateProgressDrawable determinateProgressDrawable = musicHolder.progressView.getProgressDrawable();

            determinateProgressDrawable.setMainSettings(loadStatus, playStatus, DeterminateProgressDrawable.ColorRange.BLUE,
                    LoadingContentType.AUDIO, true, false);
            determinateProgressDrawable.setBounds(0, 0);
            determinateProgressDrawable.setVisibility(true);

            if (processLoad != -1) {
                determinateProgressDrawable.setProgressWithAnimation(processLoad);
            }
            musicHolder.progressView.invalidate();
            musicHolder.selectedCircleView.invalidate();
            musicHolder.unselectedCircleView.invalidate();
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    class MusicHolder extends RecyclerView.ViewHolder {

        public TextView performerView;
        public TextView nameView;
        public DetermineProgressView progressView;
        public SelectedCircleView selectedCircleView;
        public UnselectedCircleView unselectedCircleView;

        public MusicHolder() {
            super(new RelativeLayout(context));

            RelativeLayout.LayoutParams mainLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, LAYOUT_HEIGHT);
            mainLP.addRule(RelativeLayout.CENTER_VERTICAL);

            itemView.setLayoutParams(mainLP);

            RelativeLayout mainGroupView = (RelativeLayout) itemView;
            mainGroupView.setGravity(Gravity.CENTER_VERTICAL);

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setId(AndroidUtil.generateViewId());
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    Constant.DP_45,
                    Constant.DP_45));

            mainGroupView.addView(frameLayout);

            RelativeLayout.LayoutParams frameLP = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLP.leftMargin = Constant.DP_18;
            frameLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            progressView = new DetermineProgressView(context);
            //progressView.setId(AndroidUtil.generateViewId());

            unselectedCircleView = new UnselectedCircleView(context, false);
            selectedCircleView = new SelectedCircleView(context);
            selectedCircleView.setId(R.id.selected_circle_view);

            frameLayout.addView(progressView);
            frameLayout.addView(unselectedCircleView);
            frameLayout.addView(selectedCircleView);

            selectedCircleView.hideButton();
            unselectedCircleView.setVisibility(View.GONE);

            performerView = new TextView(context);
            performerView.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            performerView.setTextColor(0xFF333333);
            performerView.setTextSize(18);
            performerView.setId(AndroidUtil.generateViewId());

            mainGroupView.addView(performerView);

            RelativeLayout.LayoutParams performerViewLP = (RelativeLayout.LayoutParams) performerView.getLayoutParams();
            performerViewLP.addRule(RelativeLayout.RIGHT_OF, frameLayout.getId());
            performerViewLP.leftMargin = Constant.DP_10;

            nameView = new TextView(context);
            nameView.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            nameView.setTextColor(0xFFB3B3B3);
            nameView.setTextSize(16);
            nameView.setId(AndroidUtil.generateViewId());

            mainGroupView.addView(nameView);

            RelativeLayout.LayoutParams nameViewLP = (RelativeLayout.LayoutParams) nameView.getLayoutParams();
            nameViewLP.addRule(RelativeLayout.RIGHT_OF, frameLayout.getId());
            nameViewLP.addRule(RelativeLayout.BELOW, performerView.getId());
            nameViewLP.leftMargin = Constant.DP_10;

            itemView.setBackgroundResource(R.drawable.item_click_transparent);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedMode) {
                        SharedMusic sharedMusic = musicList.get(MusicHolder.this.getAdapterPosition());
                        final SharedMediaActivity sharedMediaActivity = ((SharedMediaActivity) context);
                        if (sharedMusic != null) {
                            SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                            sharedMusic.isSelected = !sharedMusic.isSelected;
                            if (sharedMusic.isSelected) {
                                sharedMediaManager.getSelectedMusicList().put(sharedMusic.message.id, sharedMusic);
                            } else {
                                sharedMediaManager.getSelectedMusicList().remove(sharedMusic.message.id);
                            }
                            sharedMediaActivity.t_forward_counter.setText(sharedMediaManager.getSelectedMusicList().size() + "");
                        }
                        selectedCircleView.switchAnimated();
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!selectedMode) {
                        selectedMode = true;
                        final int currPos = MusicHolder.this.getAdapterPosition();
                        SharedAudioAdapter.this.notifyDataSetChanged();
                        final SharedMediaActivity sharedMediaActivity = ((SharedMediaActivity) context);
                        final Toolbar toolbar = sharedMediaActivity.forwardToolbar;
                        toolbar.setVisibility(View.VISIBLE);
                        toolbar.setTranslationY(-Constant.DP_56);

                        ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", 0);
                        translationY.setInterpolator(new AccelerateInterpolator());
                        translationY.setDuration(300);

                        translationY.addListener(new AnimatorEndListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //info обязательно так тк после SharedAudioAdapter.this.notifyDataSetChanged() у нас тут старая ссылка
                                View findView = linearLayoutManager.findViewByPosition(currPos);
                                SelectedCircleView selectedCircleView = (SelectedCircleView) findView.findViewById(R.id.selected_circle_view);
                                SharedMusic sharedMusic = musicList.get(currPos);
                                if (sharedMusic != null) {
                                    SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                    sharedMusic.isSelected = !sharedMusic.isSelected;
                                    if (sharedMusic.isSelected) {
                                        sharedMediaManager.getSelectedMusicList().put(sharedMusic.message.id, sharedMusic);
                                    } else {
                                        sharedMediaManager.getSelectedMusicList().remove(sharedMusic.message.id);
                                    }
                                    sharedMediaActivity.t_forward_counter.setText(sharedMediaManager.getSelectedMusicList().size() + "");
                                }
                                selectedCircleView.switchAnimated();
                            }
                        });

                        translationY.start();

                        sharedMediaActivity.t_forward_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedMode = false;
                                ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", -Constant.DP_56);
                                translationY.setInterpolator(new AccelerateInterpolator());
                                translationY.setDuration(300);
                                translationY.addListener(new AnimatorEndListener() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        SharedMediaManager.getManager().cleanSelectedMusic();
                                        ((SharedMediaActivity) context).t_forward_counter.setText("0");
                                    }
                                });
                                translationY.start();
                                SharedAudioAdapter.this.notifyDataSetChanged();
                            }
                        });

                        sharedMediaActivity.t_forward_forward.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                if (sharedMediaManager.getSelectedMusicList().size() == 0) {
                                    AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.no_selected_items));
                                } else {
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("typeList", IntermediateActivity.TypeList.ALL);
                                    bundle.putSerializable("action", IntermediateActivity.Action.FORWARD_MSGES);
                                    Intent intent = new Intent(context, IntermediateActivity.class);
                                    intent.putExtras(bundle);
                                    context.startActivity(intent);
                                    ((AppCompatActivity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                }
                            }
                        });

                        sharedMediaActivity.t_forward_delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                if (sharedMediaManager.getSelectedMusicList().size() == 0) {
                                    AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.no_selected_items));
                                } else {
                                    selectedMode = false;
                                    ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", -Constant.DP_56);
                                    translationY.setInterpolator(new AccelerateInterpolator());
                                    translationY.setDuration(300);
                                    translationY.start();
                                    sharedMediaManager.deleteSelectedMusicList(sharedMediaActivity.chatId);
                                }
                            }
                        });

                        return true;
                    }
                    return false;
                }
            });
            progressView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DeterminateProgressDrawable progressDrawable = progressView.getProgressDrawable();
                    SharedMusic sharedMusic = musicList.get(MusicHolder.this.getAdapterPosition());

                    final TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) sharedMusic.message.message;
                    if (progressDrawable.getLoadStatus() != null) {
                        switch (progressDrawable.getLoadStatus()) {
                            case NO_LOAD:
                                if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                    FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.SHARED_AUDIO,
                                            messageAudio.audio.audio.id, -1, sharedMusic.message.id, itemView, messageAudio.audio,
                                            itemView.getTag().toString(), nameView, progressView);
                                    progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                                }
                                break;
                            case PAUSE:
                                if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                    FileManager.getManager().proceedLoad(messageAudio.audio.audio.id, sharedMusic.message.id, true);
                                    progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PROCEED_LOAD);
                                }
                                break;
                            case PROCEED_LOAD:
                                if (FileUtils.isTDFileEmpty(messageAudio.audio.audio)) {
                                    FileManager.getManager().cancelDownloadFile(messageAudio.audio.audio.id, sharedMusic.message.id, true);
                                    progressDrawable.changeLoadStatusAndUpdate(DeterminateProgressDrawable.LoadStatus.PAUSE);
                                }
                                break;
                            case LOADED:
                                break;
                        }
                    }

                    if (progressDrawable.getPlayStatus() != null) {
                        AudioPlayer player = AudioPlayer.getPlayer();
                        switch (progressDrawable.getPlayStatus()) {
                            case PLAY:
                                if (FileUtils.isTDFileLocal(messageAudio.audio.audio)) {
                                    player.startToPlayAudio(sharedMusic.message, progressView);
                                }
                                break;
                            case PAUSE:
                                player.pause();
                                break;
                        }
                    }
                }
            });
        }
    }

    class DateHolder extends RecyclerView.ViewHolder {

        public TextView data;

        public DateHolder() {
            super(new RelativeLayout(context));
            itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, LAYOUT_HEIGHT));

            data = new TextView(context);
            ((RelativeLayout) itemView).addView(data);

            data.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            data.setTextColor(0xFF222222);
            data.setTextSize(18);

            RelativeLayout.LayoutParams dateLP = (RelativeLayout.LayoutParams) data.getLayoutParams();
            dateLP.addRule(RelativeLayout.CENTER_VERTICAL);
            dateLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            dateLP.leftMargin = Constant.DP_18;
        }
    }
}