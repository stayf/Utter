package com.stayfprod.utter.ui.adapter;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.model.SharedMedia;
import com.stayfprod.utter.ui.activity.ChatActivity;
import com.stayfprod.utter.ui.activity.IntermediateActivity;
import com.stayfprod.utter.ui.activity.PhotoActivity;
import com.stayfprod.utter.ui.activity.ProfileActivity;
import com.stayfprod.utter.ui.activity.SharedMediaActivity;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.ui.view.MediaView;
import com.stayfprod.utter.ui.view.SelectedCircleView;
import com.stayfprod.utter.ui.view.UnselectedCircleView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class SharedMediaAdapter extends RecyclerView.Adapter<AbstractHolder<SharedMedia>> {

    public static final int LAYOUT_HEIGHT = AndroidUtil.dp(106);

    private static final int TYPE_DATE = 1;
    private static final int TYPE_MEDIA = 2;

    private boolean mSelectedMode = false;

    private Context mContext;
    private GridLayoutManager mGridLayoutManager;
    private List<SharedMedia> mMediaList;

    public SharedMediaAdapter(List<SharedMedia> mediaList, Context context, GridLayoutManager gridLayoutManager) {
        this.mMediaList = mediaList;
        this.mContext = context;
        if (gridLayoutManager != null) {
            this.mGridLayoutManager = gridLayoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (SharedMediaAdapter.this.mMediaList.get(position).isDate) {
                        return SharedMediaActivity.sMaxRowSpans;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    @Override
    public AbstractHolder<SharedMedia> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DATE: {
                return new DateHolder();
            }
            case TYPE_MEDIA:
            default: {
                return new MediaHolder();
            }
        }
    }

    @Override
    public void onBindViewHolder(AbstractHolder<SharedMedia> holder, int position) {
        holder.setValues(mMediaList.get(position), position, mContext);
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMediaList.get(position).isDate ? TYPE_DATE : TYPE_MEDIA;
    }

    class DateHolder extends AbstractHolder<SharedMedia> {

        public TextView data;

        public DateHolder() {
            super(new RelativeLayout(mContext));
            itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, Constant.DP_28));

            data = new TextView(mContext);
            ((RelativeLayout) itemView).addView(data);

            data.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            data.setTextColor(0xFF222222);
            data.setTextSize(18);

            RelativeLayout.LayoutParams dateLP = (RelativeLayout.LayoutParams) data.getLayoutParams();
            dateLP.addRule(RelativeLayout.CENTER_VERTICAL);
            dateLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            dateLP.leftMargin = Constant.DP_18;
        }

        @Override
        public void setValues(SharedMedia record, int i, Context context) {
            data.setText(record.date);
        }
    }

    class MediaHolder extends AbstractHolder<SharedMedia> {

        public MediaView mediaView;
        public UnselectedCircleView unselectedCircleView;
        public SelectedCircleView selectedCircleView;

        public MediaHolder() {
            super(new RelativeLayout(mContext));

            RelativeLayout mainLayout = (RelativeLayout) itemView;
            RelativeLayout.LayoutParams mainLP = new RelativeLayout.LayoutParams(LAYOUT_HEIGHT, LAYOUT_HEIGHT);
            mainLP.setMargins(Constant.DP_8, Constant.DP_8, Constant.DP_8, Constant.DP_8);
            mainLayout.setLayoutParams(mainLP);
            mainLayout.setBackgroundColor(0xFFF5F5F5);

            //картинка
            mediaView = new MediaView(mContext);
            mediaView.setId(R.id.media_view);
            mainLayout.addView(mediaView);

            RelativeLayout.LayoutParams mediaViewLP = (RelativeLayout.LayoutParams) mediaView.getLayoutParams();
            mediaViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            mediaViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            mediaViewLP.addRule(RelativeLayout.CENTER_VERTICAL);
            //картинка end

            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setId(AndroidUtil.generateViewId());
            mainLayout.addView(frameLayout);

            RelativeLayout.LayoutParams frameLayoutLP = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLayoutLP.leftMargin = Constant.DP_6;
            frameLayoutLP.topMargin = Constant.DP_6;
            frameLayoutLP.width = Constant.DP_20;
            frameLayoutLP.height = Constant.DP_20;

            unselectedCircleView = new UnselectedCircleView(mContext, true);
            selectedCircleView = new SelectedCircleView(mContext);
            selectedCircleView.setId(R.id.selected_circle_view);
            selectedCircleView.hideButton();
            unselectedCircleView.setVisibility(View.GONE);

            frameLayout.addView(unselectedCircleView);
            frameLayout.addView(selectedCircleView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedMode) {
                        SharedMedia sharedMedia = mMediaList.get(MediaHolder.this.getAdapterPosition());
                        final SharedMediaActivity sharedMediaActivity = ((SharedMediaActivity) mContext);
                        if (sharedMedia != null) {
                            SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                            sharedMedia.isSelected = !sharedMedia.isSelected;
                            if (sharedMedia.isSelected) {
                                sharedMediaManager.getSelectedMediaList().put(sharedMedia.message.id, sharedMedia);
                            } else {
                                sharedMediaManager.getSelectedMediaList().remove(sharedMedia.message.id);
                            }
                            sharedMediaActivity.forwardCounter.setText(sharedMediaManager.getSelectedMediaList().size() + "");
                        }
                        selectedCircleView.switchAnimated(mediaView);
                    } else {
                        try {
                            final int currPos = MediaHolder.this.getAdapterPosition();
                            SharedMedia sharedMedia = mMediaList.get(currPos);

                            TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) sharedMedia.message.message;
                            TdApi.PhotoSize photoSize = messagePhoto.photo.photos[sharedMedia.photoIndex];
                            if (FileUtil.isTDFileLocal(photoSize.photo)) {
                                Intent intent = new Intent(mContext, PhotoActivity.class);
                                intent.putExtra("filePath", photoSize.photo.path);
                                intent.putExtra("imageView", true);
                                intent.putExtra("isFromSharedMedia", true);
                                intent.putExtra("msgId", sharedMedia.message.id);
                                ((AppCompatActivity) mContext).startActivityForResult(intent, ChatActivity.OPEN_PHOTO);
                            }
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!mSelectedMode) {
                        if (mContext instanceof ProfileActivity) {

                        } else {
                            mSelectedMode = true;
                            final int currPos = MediaHolder.this.getAdapterPosition();
                            SharedMediaAdapter.this.notifyDataSetChanged();
                            final SharedMediaActivity sharedMediaActivity = ((SharedMediaActivity) mContext);
                            final Toolbar toolbar = sharedMediaActivity.forwardToolbar;
                            toolbar.setVisibility(View.VISIBLE);
                            toolbar.setTranslationY(-Constant.DP_56);

                            ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", 0);
                            translationY.setInterpolator(new AccelerateInterpolator());
                            translationY.setDuration(300);

                            translationY.addListener(new AnimatorEndListener() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    //info здесь обязательно именно так иначе остается ссылка на старый итем
                                    View findView = mGridLayoutManager.findViewByPosition(currPos);
                                    SelectedCircleView selectedCircleView = (SelectedCircleView) findView.findViewById(R.id.selected_circle_view);
                                    MediaView mediaView = (MediaView) findView.findViewById(R.id.media_view);

                                    SharedMedia sharedMedia = mMediaList.get(currPos);

                                    if (sharedMedia != null) {
                                        SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                        sharedMedia.isSelected = !sharedMedia.isSelected;
                                        if (sharedMedia.isSelected) {
                                            sharedMediaManager.getSelectedMediaList().put(sharedMedia.message.id, sharedMedia);
                                        } else {
                                            sharedMediaManager.getSelectedMediaList().remove(sharedMedia.message.id);
                                        }
                                        ((SharedMediaActivity) mContext).forwardCounter.setText(sharedMediaManager.getSelectedMediaList().size() + "");
                                    }

                                    selectedCircleView.switchAnimated(mediaView);
                                }
                            });

                            translationY.start();

                            //обязательно здесь листенер
                            sharedMediaActivity.forwardCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mSelectedMode = false;
                                    ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", -Constant.DP_56);
                                    translationY.setInterpolator(new AccelerateInterpolator());
                                    translationY.setDuration(300);
                                    translationY.addListener(new AnimatorEndListener() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            SharedMediaManager.getManager().cleanSelectedMedia();
                                            ((SharedMediaActivity) mContext).forwardCounter.setText("0");
                                        }
                                    });
                                    translationY.start();
                                    SharedMediaAdapter.this.notifyDataSetChanged();
                                }
                            });


                            sharedMediaActivity.forwardImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                    if (sharedMediaManager.getSelectedMediaList().size() == 0) {
                                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.no_selected_items));
                                    } else {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("typeList", IntermediateActivity.TypeList.ALL);
                                        bundle.putSerializable("action", IntermediateActivity.Action.FORWARD_MSGES);
                                        Intent intent = new Intent(mContext, IntermediateActivity.class);
                                        intent.putExtras(bundle);
                                        mContext.startActivity(intent);
                                        ((AppCompatActivity) mContext).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                    }
                                }
                            });

                            sharedMediaActivity.forwardDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();
                                    if (sharedMediaManager.getSelectedMediaList().size() == 0) {
                                        AndroidUtil.showToastShort(AndroidUtil.getResourceString(R.string.no_selected_items));
                                    } else {
                                        mSelectedMode = false;
                                        ObjectAnimator translationY = ObjectAnimator.ofFloat(toolbar, "translationY", -Constant.DP_56);
                                        translationY.setInterpolator(new AccelerateInterpolator());
                                        translationY.setDuration(300);
                                        translationY.start();

                                        //если открыто две апктивити то false
                                        sharedMediaManager.deleteSelectedMediaList(sharedMediaActivity.chatId, ProfileActivity.sOpenedProfileCounter == 1);
                                    }
                                }
                            });
                        }

                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void setValues(SharedMedia record, int i, Context context) {
            mediaView.setSharedMedia(record, i, context);
            record.pos = i;
            if (mSelectedMode) {
                unselectedCircleView.setVisibility(View.VISIBLE);
                if (record.isSelected) {
                    mediaView.setScaleX(0.75f);
                    mediaView.setScaleY(0.75f);
                    selectedCircleView.showButton();
                    selectedCircleView.setScaleX(1.0f);
                    selectedCircleView.setScaleY(1.0f);
                } else {
                    mediaView.setScaleX(1f);
                    mediaView.setScaleY(1f);
                    selectedCircleView.hideButton();
                }

            } else {
                unselectedCircleView.setVisibility(View.GONE);
                selectedCircleView.setVisibility(View.GONE);
                mediaView.setScaleX(1f);
                mediaView.setScaleY(1f);
            }
            mediaView.invalidate();
            unselectedCircleView.invalidate();
            selectedCircleView.invalidate();
        }
    }
}
