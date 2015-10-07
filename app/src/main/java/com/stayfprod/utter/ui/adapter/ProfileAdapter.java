package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ContactListManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.SharedMediaManager;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.ui.activity.ContactListActivity;
import com.stayfprod.utter.ui.activity.IntermediateActivity;
import com.stayfprod.utter.ui.activity.ProfileActivity;
import com.stayfprod.utter.ui.activity.SharedMediaActivity;
import com.stayfprod.utter.ui.adapter.holder.AbstractHolder;
import com.stayfprod.utter.ui.component.ProfileUserListDialog;
import com.stayfprod.utter.ui.view.ContactView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import static com.stayfprod.utter.manager.ProfileManager.*;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<AbstractHolder<Object>> {

    private LayoutInflater mInflater;
    private ArrayList<Object> mItems;
    private SharedMediaAdapter sharedMediaAdapter;
    private Context context;
    public FOR mFor;


    public ProfileAdapter(Context context, FOR mFor, ArrayList<Object> items) {
        this.context = context;
        this.mFor = mFor;
        this.mInflater = LayoutInflater.from(context);
        if (items == null) {
            this.mItems = new ArrayList<Object>();
        } else {
            this.mItems = items;
        }
    }

    public SharedMediaAdapter getSharedMediaAdapter() {
        return sharedMediaAdapter;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemType(position, mFor);
    }

    @Override
    public AbstractHolder<Object> onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        switch (viewType) {
            case ADD_MEMBER_TYPE:
                return new AddMemberHolder(relativeLayout, mFor);
            case ADD_SHARED_MEDIA_TYPE:
                return new SharedMediaHolder(relativeLayout, mFor);
            case ADD_USER_LIST_TYPE:
                return new UserListHolder(new ContactView(context), mFor);
            case ADD_GROUP_TYPE:
                return new AddGroupHolder(relativeLayout, mFor);
            case ADD_USER_TOP_LIST_TYPE:
                return new UserTopListHolder(new ContactView(context), mFor);
            case INFO_NAME_TYPE:
            default:
                return new NameHolder(relativeLayout, mFor);
            case INFO_ADDITION_TYPE:
                return new AdditionInfoHolder(relativeLayout, mFor);
        }
    }

    @Override
    public void onBindViewHolder(AbstractHolder<Object> viewHolder, int position) {
        if (position == 0) {
            viewHolder.itemView.setPadding(0, AndroidUtil.dp(156), 0, 0);
        } else {
            viewHolder.itemView.setPadding(0, 0, 0, 0);
        }
        viewHolder.setValues(mItems.get(position), position, context);
    }

    static class UserTopListHolder extends AbstractHolder<Object> {

        public ContactView contactView;

        public UserTopListHolder(ContactView contactView, FOR mFor) {
            super(new RelativeLayout(contactView.getContext()));
            itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    ContactView.LAYOUT_HEIGHT + AndroidUtil.dp(32)));

            final Context context = contactView.getContext();
            RelativeLayout mainLayout = (RelativeLayout) itemView;

            RelativeLayout subLayout = new RelativeLayout(context);
            subLayout.setId(AndroidUtil.generateViewId());
            subLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    AndroidUtil.dp(30)));

            mainLayout.addView(subLayout);

            //тень
            View shadowTop = new View(context);
            shadowTop.setBackgroundResource(R.mipmap.shadow_top);
            shadowTop.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowTop);

            RelativeLayout.LayoutParams shadowTopLP = (RelativeLayout.LayoutParams) shadowTop.getLayoutParams();
            shadowTopLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowTopLP.height = AndroidUtil.dp(2f);
            shadowTopLP.addRule(RelativeLayout.BELOW, subLayout.getId());
            //тень end

            mainLayout.addView(contactView);

            RelativeLayout.LayoutParams contactViewLP = (RelativeLayout.LayoutParams) contactView.getLayoutParams();
            contactViewLP.addRule(RelativeLayout.BELOW, shadowTop.getId());

            contactView.setBackgroundResource(R.drawable.item_click_white_no_transparent);
            contactView.setIsInProfile(true);
            this.contactView = contactView;
            this.contactView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Contact contact = UserTopListHolder.this.contactView.record;
                    return new ProfileUserListDialog().processLongClick(context, contact);
                }
            });
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            contactView.setIsFirstItem(true);
            contactView.setValues((Contact) record, i, context);
        }
    }

    static class UserListHolder extends AbstractHolder<Object> {

        public UserListHolder(ContactView view, FOR mFor) {
            super(view);
            itemView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    ContactView.LAYOUT_HEIGHT));
            itemView.setBackgroundResource(R.drawable.item_click_white_no_transparent);
            ((ContactView) itemView).setIsInProfile(true);
            (itemView).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Contact contact = ((ContactView) UserListHolder.this.itemView).record;
                    return new ProfileUserListDialog().processLongClick(UserListHolder.this.itemView.getContext(), contact);
                }
            });
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            ((ContactView) itemView).setValues((Contact) record, i, context);
        }
    }

    static class AddMemberHolder extends AbstractHolder<Object> {
        public AddMemberHolder(View viewGroup, FOR mFor) {
            super(viewGroup);

            final Context context = viewGroup.getContext();
            RelativeLayout mainLayout = (RelativeLayout) viewGroup;

            RelativeLayout.LayoutParams mainLayoutLP = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mainLayout.setLayoutParams(mainLayoutLP);

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setId(AndroidUtil.generateViewId());
            frameLayout.setBackgroundColor(Color.WHITE);
            mainLayout.addView(frameLayout);

            RelativeLayout.LayoutParams frameLayoutLP = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
            frameLayoutLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            frameLayoutLP.height = Constant.DP_24;
            frameLayoutLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            //контейнер
            RelativeLayout container = new RelativeLayout(context);
            container.setId(AndroidUtil.generateViewId());
            container.setBackgroundColor(Color.WHITE);
            mainLayout.addView(container);

            RelativeLayout.LayoutParams containerLP = (RelativeLayout.LayoutParams) container.getLayoutParams();
            containerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            containerLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;//AndroidUtil.dp(174);
            containerLP.addRule(RelativeLayout.BELOW, frameLayout.getId());
            //контейнер end

            //кнопка
            RelativeLayout shareMediaButton = new RelativeLayout(context);
            shareMediaButton.setBackgroundResource(R.drawable.item_click_transparent);
            shareMediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactListManager contactListManager = ContactListManager.getManager();
                    contactListManager.setAction(ContactListManager.ACTION_ADD_USER_TO_GROUP);

                    Intent intent = new Intent(context, ContactListActivity.class);
                    context.startActivity(intent);
                    ((AppCompatActivity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
            container.addView(shareMediaButton);

            RelativeLayout.LayoutParams shareMediaButtonLP = (RelativeLayout.LayoutParams) shareMediaButton.getLayoutParams();
            shareMediaButtonLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shareMediaButtonLP.height = AndroidUtil.dp(54);
            shareMediaButtonLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            ImageView imageView = new ImageView(context);
            imageView.setId(AndroidUtil.generateViewId());
            imageView.setImageResource(R.mipmap.ic_add);
            shareMediaButton.addView(imageView);

            RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageViewLP.addRule(RelativeLayout.CENTER_VERTICAL);
            imageViewLP.leftMargin = Constant.DP_16;

            TextView textViewTitle = new TextView(context);
            textViewTitle.setText(AndroidUtil.getResourceString(R.string.add_member));
            textViewTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewTitle.setTextSize(17);
            textViewTitle.setTextColor(0xFF222222);
            shareMediaButton.addView(textViewTitle);

            RelativeLayout.LayoutParams textViewTitleLP = (RelativeLayout.LayoutParams) textViewTitle.getLayoutParams();
            textViewTitleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.addRule(RelativeLayout.CENTER_VERTICAL);
            textViewTitleLP.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
            textViewTitleLP.leftMargin = Constant.DP_64;
            //кнопка end

            //тень низ
            View shadowBottom = new View(context);
            shadowBottom.setId(AndroidUtil.generateViewId());
            shadowBottom.setBackgroundResource(R.mipmap.shadow_bottom);
            shadowBottom.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowBottom);

            RelativeLayout.LayoutParams shadowBottomLP = (RelativeLayout.LayoutParams) shadowBottom.getLayoutParams();
            shadowBottomLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowBottomLP.height = AndroidUtil.dp(3f);
            shadowBottomLP.addRule(RelativeLayout.BELOW, container.getId());
            shadowBottomLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            //тень низ end
        }

        @Override
        public void setValues(Object record, int i, Context context) {

        }
    }

    class SharedMediaHolder extends AbstractHolder<Object> {
        public TextView textViewCounter;
        public RecyclerView recyclerView;

        public SharedMediaHolder(View viewGroup, FOR mFor) {
            super(viewGroup);

            final Context context = viewGroup.getContext();
            RelativeLayout mainLayout = (RelativeLayout) viewGroup;
            RelativeLayout.LayoutParams mainLayoutLP = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mainLayout.setLayoutParams(mainLayoutLP);
            mainLayoutLP.topMargin = Constant.DP_12;

            //тень
            View shadowTop = new View(context);
            shadowTop.setBackgroundResource(R.mipmap.shadow_top);
            shadowTop.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowTop);

            RelativeLayout.LayoutParams shadowTopLP = (RelativeLayout.LayoutParams) shadowTop.getLayoutParams();
            shadowTopLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowTopLP.height = AndroidUtil.dp(2f);
            //тень end

            //контейнер
            RelativeLayout container = new RelativeLayout(context);
            container.setId(AndroidUtil.generateViewId());
            container.setBackgroundColor(Color.WHITE);
            mainLayout.addView(container);

            RelativeLayout.LayoutParams containerLP = (RelativeLayout.LayoutParams) container.getLayoutParams();
            containerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            containerLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;//AndroidUtil.dp(174);
            containerLP.addRule(RelativeLayout.BELOW, shadowTop.getId());
            //контейнер end

            //список info
            recyclerView = new RecyclerView(context);
            //recyclerView.setHorizontalScrollBarEnabled(true);
            recyclerView.setId(AndroidUtil.generateViewId());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            SharedMediaManager sharedMediaManager = SharedMediaManager.getManager();

            sharedMediaAdapter = new SharedMediaAdapter(sharedMediaManager.getPhotoAndVideoProfileMessages(!((ProfileActivity) context).isSubProfile), context, null);

            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(sharedMediaAdapter);

            container.addView(recyclerView);

            RelativeLayout.LayoutParams recyclerViewLP = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            recyclerViewLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            recyclerViewLP.height = AndroidUtil.dp(122);
            //список end

            //кнопка
            RelativeLayout shareMediaButton = new RelativeLayout(context);
            shareMediaButton.setBackgroundResource(R.drawable.item_click_transparent);
            shareMediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, SharedMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("selectedPage", SharedMediaActivity.SHARED_MEDIA);
                    bundle.putLong("chatId", ProfileManager.getManager().getChatInfo().tgChatObject.id);

                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    ((AppCompatActivity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
            container.addView(shareMediaButton);

            RelativeLayout.LayoutParams shareMediaButtonLP = (RelativeLayout.LayoutParams) shareMediaButton.getLayoutParams();
            shareMediaButtonLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shareMediaButtonLP.height = AndroidUtil.dp(54);
            shareMediaButtonLP.addRule(RelativeLayout.BELOW, recyclerView.getId());
            shareMediaButtonLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            ImageView imageView = new ImageView(context);
            imageView.setId(AndroidUtil.generateViewId());
            imageView.setImageResource(R.mipmap.ic_attach_gallery);
            shareMediaButton.addView(imageView);

            RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageViewLP.addRule(RelativeLayout.CENTER_VERTICAL);
            imageViewLP.leftMargin = Constant.DP_16;

            TextView textViewTitle = new TextView(context);
            textViewTitle.setText(AndroidUtil.getResourceString(R.string.shred_media));

            textViewTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewTitle.setTextSize(17);
            textViewTitle.setTextColor(0xFF222222);
            shareMediaButton.addView(textViewTitle);

            RelativeLayout.LayoutParams textViewTitleLP = (RelativeLayout.LayoutParams) textViewTitle.getLayoutParams();
            textViewTitleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.addRule(RelativeLayout.CENTER_VERTICAL);
            textViewTitleLP.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
            textViewTitleLP.leftMargin = Constant.DP_64;

            textViewCounter = new TextView(context);
            textViewCounter.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewCounter.setTextSize(16);
            textViewCounter.setTextColor(0xFF8A8A8A);
            shareMediaButton.addView(textViewCounter);

            RelativeLayout.LayoutParams textViewCounterLP = (RelativeLayout.LayoutParams) textViewCounter.getLayoutParams();
            textViewCounterLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewCounterLP.addRule(RelativeLayout.CENTER_VERTICAL);
            textViewCounterLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textViewCounterLP.rightMargin = Constant.DP_16;
            //кнопка end

            //тень низ
            View shadowBottom = new View(context);
            shadowBottom.setBackgroundResource(R.mipmap.shadow_bottom);
            shadowBottom.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowBottom);

            RelativeLayout.LayoutParams shadowBottomLP = (RelativeLayout.LayoutParams) shadowBottom.getLayoutParams();
            shadowBottomLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowBottomLP.height = AndroidUtil.dp(3f);
            shadowBottomLP.addRule(RelativeLayout.BELOW, container.getId());
            shadowBottomLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            //тень низ end
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            String size = ((String[]) record)[0];
            textViewCounter.setText(size);

            if (TextUtil.isBlank(size) || size.equals("0")) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    static class NameHolder extends AbstractHolder<Object> {

        public TextView textViewName;

        public NameHolder(View viewGroup, FOR mFor) {
            super(viewGroup);

            Context context = viewGroup.getContext();
            RelativeLayout mainLayout = (RelativeLayout) viewGroup;
            mainLayout.setBackgroundColor(Color.WHITE);
            mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            //тень
            View shadow = new View(context);
            shadow.setBackgroundResource(R.mipmap.shadow_bottom);
            shadow.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadow);

            RelativeLayout.LayoutParams shadowLP = (RelativeLayout.LayoutParams) shadow.getLayoutParams();
            shadowLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowLP.height = AndroidUtil.dp(2f);
            //тень end

            //контейнер
            RelativeLayout container = new RelativeLayout(context);

            mainLayout.addView(container);

            RelativeLayout.LayoutParams containerLP = (RelativeLayout.LayoutParams) container.getLayoutParams();
            containerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            containerLP.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            //контейнер end

            //иконка
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.mipmap.ic_user);
            imageView.setId(AndroidUtil.generateViewId());
            container.addView(imageView);

            RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.leftMargin = Constant.DP_16;
            imageViewLP.topMargin = AndroidUtil.dp(48);
            //иконка end

            //имя
            textViewName = new TextView(context);
            textViewName.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewName.setTextSize(17);
            textViewName.setTextColor(0xFF222222);
            textViewName.setId(AndroidUtil.generateViewId());
            container.addView(textViewName);

            RelativeLayout.LayoutParams textViewNameLP = (RelativeLayout.LayoutParams) textViewName.getLayoutParams();
            textViewNameLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewNameLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewNameLP.leftMargin = Constant.DP_64;
            textViewNameLP.topMargin = AndroidUtil.dp(46);
            textViewNameLP.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
            //имя end

            //инфа
            TextView textViewInfo = new TextView(context);
            textViewInfo.setText(AndroidUtil.getResourceString(R.string.username));
            textViewInfo.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewInfo.setTextSize(14);
            textViewInfo.setTextColor(0xFF8a8a8a);
            textViewInfo.setId(AndroidUtil.generateViewId());
            container.addView(textViewInfo);

            RelativeLayout.LayoutParams textViewInfoLP = (RelativeLayout.LayoutParams) textViewInfo.getLayoutParams();
            textViewInfoLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewInfoLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewInfoLP.topMargin = Constant.DP_4;
            textViewInfoLP.addRule(RelativeLayout.ALIGN_LEFT, textViewName.getId());
            textViewInfoLP.addRule(RelativeLayout.BELOW, textViewName.getId());
            //инфа end

            //разделитель
            View divider = new View(context);
            divider.setBackgroundColor(0xFFEAEAEA);
            container.addView(divider);

            RelativeLayout.LayoutParams dividerLP = (RelativeLayout.LayoutParams) divider.getLayoutParams();
            dividerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            dividerLP.height = AndroidUtil.dp(1.5f);
            dividerLP.topMargin = Constant.DP_20;
            dividerLP.addRule(RelativeLayout.ALIGN_LEFT, textViewInfo.getId());
            dividerLP.addRule(RelativeLayout.BELOW, textViewInfo.getId());
            //разделитель end
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            textViewName.setText(((String[]) record)[0]);
        }
    }

    static class AdditionInfoHolder extends AbstractHolder<Object> {
        public TextView textViewAddtion;

        public AdditionInfoHolder(View viewGroup, FOR mFor) {
            super(viewGroup);

            Context context = viewGroup.getContext();
            RelativeLayout mainLayout = (RelativeLayout) viewGroup;
            mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            //контейнер
            RelativeLayout container = new RelativeLayout(context);
            container.setId(AndroidUtil.generateViewId());
            container.setBackgroundColor(Color.WHITE);
            mainLayout.addView(container);

            RelativeLayout.LayoutParams containerLP = (RelativeLayout.LayoutParams) container.getLayoutParams();
            containerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            containerLP.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            container.setPadding(0, 0, 0, Constant.DP_20);
            //контейнер end

            //иконка

            ImageView imageView = new ImageView(context);
            if (mFor == FOR.USER) {
                imageView.setImageResource(R.mipmap.ic_phone);
            } else if (mFor == FOR.BOT) {
                imageView.setImageResource(R.mipmap.ic_about);
            }
            imageView.setId(AndroidUtil.generateViewId());
            container.addView(imageView);

            RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.leftMargin = Constant.DP_16;
            imageViewLP.topMargin = AndroidUtil.dp(24);
            //иконка end

            //доп инфа
            textViewAddtion = new TextView(context);
            textViewAddtion.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewAddtion.setTextSize(17);
            textViewAddtion.setTextColor(0xFF222222);
            textViewAddtion.setId(AndroidUtil.generateViewId());
            container.addView(textViewAddtion);

            RelativeLayout.LayoutParams textViewAdditionLP = (RelativeLayout.LayoutParams) textViewAddtion.getLayoutParams();
            textViewAdditionLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewAdditionLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewAdditionLP.leftMargin = Constant.DP_64;
            textViewAdditionLP.topMargin = Constant.DP_14;
            textViewAdditionLP.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
            //доп инфа end

            //инфа
            TextView textViewInfo = new TextView(context);
            if (mFor == FOR.USER) {
                textViewInfo.setText(AndroidUtil.getResourceString(R.string.phone));

            } else if (mFor == FOR.BOT) {
                textViewInfo.setText(AndroidUtil.getResourceString(R.string.about));
            }

            textViewInfo.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewInfo.setTextSize(14);
            textViewInfo.setTextColor(0xFF8a8a8a);
            textViewInfo.setId(AndroidUtil.generateViewId());
            container.addView(textViewInfo);

            RelativeLayout.LayoutParams textViewInfoLP = (RelativeLayout.LayoutParams) textViewInfo.getLayoutParams();
            textViewInfoLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewInfoLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewInfoLP.topMargin = Constant.DP_4;
            textViewInfoLP.addRule(RelativeLayout.ALIGN_LEFT, textViewAddtion.getId());
            textViewInfoLP.addRule(RelativeLayout.BELOW, textViewAddtion.getId());
            //инфа end

            //тень
            View shadow = new View(context);
            shadow.setBackgroundResource(R.mipmap.shadow_bottom);
            shadow.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadow);

            RelativeLayout.LayoutParams shadowLP = (RelativeLayout.LayoutParams) shadow.getLayoutParams();
            shadowLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowLP.height = AndroidUtil.dp(3f);
            shadowLP.addRule(RelativeLayout.BELOW, container.getId());
            shadowLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            //тень end
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            textViewAddtion.setText(((String[]) record)[0]);
        }
    }

    static class AddGroupHolder extends AbstractHolder<Object> {
        public RelativeLayout shareMediaButton;

        public AddGroupHolder(View viewGroup, FOR mFor) {
            super(viewGroup);

            final Context context = viewGroup.getContext();
            RelativeLayout mainLayout = (RelativeLayout) viewGroup;
            RelativeLayout.LayoutParams mainLayoutLP = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mainLayout.setLayoutParams(mainLayoutLP);
            mainLayoutLP.topMargin = Constant.DP_12;

            //тень
            View shadowTop = new View(context);
            shadowTop.setBackgroundResource(R.mipmap.shadow_top);
            shadowTop.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowTop);

            RelativeLayout.LayoutParams shadowTopLP = (RelativeLayout.LayoutParams) shadowTop.getLayoutParams();
            shadowTopLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowTopLP.height = AndroidUtil.dp(2f);
            //тень end

            //контейнер
            RelativeLayout container = new RelativeLayout(context);
            container.setId(AndroidUtil.generateViewId());
            container.setBackgroundColor(Color.WHITE);
            mainLayout.addView(container);

            RelativeLayout.LayoutParams containerLP = (RelativeLayout.LayoutParams) container.getLayoutParams();
            containerLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            containerLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;//AndroidUtil.dp(174);
            containerLP.addRule(RelativeLayout.BELOW, shadowTop.getId());
            //контейнер end

            //кнопка
            shareMediaButton = new RelativeLayout(context);
            shareMediaButton.setBackgroundResource(R.drawable.item_click_transparent);
            shareMediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("typeList", IntermediateActivity.TypeList.GROUPS_ONLY);
                    bundle.putSerializable("action", IntermediateActivity.Action.ADD_BOT);
                    ChatInfo chatInfo = ProfileManager.getManager().getChatInfo();
                    TdApi.User user = ((TdApi.PrivateChatInfo) chatInfo.tgChatObject.type).user;
                    bundle.putInt("botId", user.id);

                    Intent intent = new Intent(context, IntermediateActivity.class);
                    intent.putExtras(bundle);

                    context.startActivity(intent);
                    ((AppCompatActivity) context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
            container.addView(shareMediaButton);

            RelativeLayout.LayoutParams shareMediaButtonLP = (RelativeLayout.LayoutParams) shareMediaButton.getLayoutParams();
            shareMediaButtonLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shareMediaButtonLP.height = AndroidUtil.dp(54);
            shareMediaButtonLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            ImageView imageView = new ImageView(context);
            imageView.setId(AndroidUtil.generateViewId());
            imageView.setImageResource(R.mipmap.ic_add);
            shareMediaButton.addView(imageView);

            RelativeLayout.LayoutParams imageViewLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            imageViewLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            imageViewLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageViewLP.addRule(RelativeLayout.CENTER_VERTICAL);
            imageViewLP.leftMargin = Constant.DP_16;

            TextView textViewTitle = new TextView(context);
            textViewTitle.setText(AndroidUtil.getResourceString(R.string.add_to_group));

            textViewTitle.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            textViewTitle.setTextSize(17);
            textViewTitle.setTextColor(0xFF222222);
            shareMediaButton.addView(textViewTitle);

            RelativeLayout.LayoutParams textViewTitleLP = (RelativeLayout.LayoutParams) textViewTitle.getLayoutParams();
            textViewTitleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            textViewTitleLP.addRule(RelativeLayout.CENTER_VERTICAL);
            textViewTitleLP.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
            textViewTitleLP.leftMargin = Constant.DP_64;
            //кнопка end

            //тень низ
            View shadowBottom = new View(context);
            shadowBottom.setBackgroundResource(R.mipmap.shadow_bottom);
            shadowBottom.setId(AndroidUtil.generateViewId());

            mainLayout.addView(shadowBottom);

            RelativeLayout.LayoutParams shadowBottomLP = (RelativeLayout.LayoutParams) shadowBottom.getLayoutParams();
            shadowBottomLP.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            shadowBottomLP.height = AndroidUtil.dp(3f);
            shadowBottomLP.addRule(RelativeLayout.BELOW, container.getId());
            shadowBottomLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            //тень низ end
        }

        @Override
        public void setValues(Object record, int i, Context context) {
            if (record == null) {
                RelativeLayout.LayoutParams shareMediaButtonLP = (RelativeLayout.LayoutParams) shareMediaButton.getLayoutParams();
                shareMediaButtonLP.height = 0;
                itemView.setVisibility(View.GONE);
            } else {
                RelativeLayout.LayoutParams shareMediaButtonLP = (RelativeLayout.LayoutParams) shareMediaButton.getLayoutParams();
                shareMediaButtonLP.height = AndroidUtil.dp(54);
                itemView.setVisibility(View.VISIBLE);
            }
        }
    }

}