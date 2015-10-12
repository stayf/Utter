package com.stayfprod.utter.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.BotManager;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.model.BotCommand;
import com.stayfprod.utter.model.ChatInfo;
import com.stayfprod.utter.factory.IconFactory;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtils;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class BotCommandsAdapter extends RecyclerView.Adapter<BotCommandsAdapter.ViewHolder> {

    private List<BotCommand> botCommands;
    private Context context;

    public BotCommandsAdapter(List<BotCommand> botCommands, Context context) {
        this.botCommands = botCommands;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setTag(position);

        BotCommand botCommand = botCommands.get(position);

        TdApi.File small = botCommand.cachedUser.tgUser.profilePhoto.small;
        if (FileUtils.isTDFileEmpty(small)) {
            IconDrawable iconDrawable = IconFactory.createEmptyIcon(IconFactory.Type.BOT_COMMAND, botCommand.cachedUser.tgUser.id, botCommand.cachedUser.initials);
            holder.icon.setImageDrawable(iconDrawable);
            if (small.id > 0)
                FileManager.getManager().uploadFileAsync(FileManager.TypeLoad.BOT_COMMAND_ICON,
                        small.id, position, -1, botCommand.cachedUser.tgUser, holder.icon, holder.itemView, holder.itemView.getTag().toString());
        } else {
            IconDrawable icon = IconFactory.createBitmapIconForImageView(IconFactory.Type.BOT_COMMAND, small.path, holder.itemView, holder.icon, holder.itemView.getTag().toString());
            if (icon != null) {
                holder.icon.setImageDrawable(icon);
            } else {
                AndroidUtil.setImagePlaceholder(holder.icon);
            }
        }

        holder.title.setText("/" + botCommand.tgBotCommand.command);
        try {
            holder.title.setTag(botCommand.cachedUser.tgUser.username);
        } catch (Exception e) {
            holder.title.setTag("");
        }


        holder.desc.setText(botCommand.tgBotCommand.description);
    }

    @Override
    public int getItemCount() {
        return botCommands.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView title;
        public TextView desc;

        public ViewHolder() {
            super(new RelativeLayout(context));

            RelativeLayout mainLayout = (RelativeLayout) itemView;
            RelativeLayout.LayoutParams paramsML = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, AndroidUtil.dp(36));
            mainLayout.setLayoutParams(paramsML);

            mainLayout.setBackgroundResource(R.drawable.item_click_transparent);
            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ChatManager manager = ChatManager.getManager();
                        ChatInfo chatInfo = ChatManager.getCurrentChatInfo();
                        if (chatInfo != null && chatInfo.isGroupChat) {
                            manager.sendMessage(manager.createTextMsg(title.getText().toString() + "@" + title.getTag().toString()));
                        } else {
                            manager.sendMessage(manager.createTextMsg(title.getText().toString()));
                        }

                        BotManager.getManager().hideCommandListAndCleanEditText();
                    } catch (Exception e) {
                        //
                        BotManager.getManager().hideCommandListAndCleanEditText();
                    }
                }
            });

            //иконка
            icon = new ImageView(context);
            icon.setId(AndroidUtil.generateViewId());
            mainLayout.addView(icon);

            RelativeLayout.LayoutParams iconLP = (RelativeLayout.LayoutParams) icon.getLayoutParams();
            iconLP.height = Constant.DP_27;
            iconLP.width = Constant.DP_27;
            iconLP.leftMargin = Constant.DP_18;
            iconLP.addRule(RelativeLayout.CENTER_VERTICAL);
            //иконка end

            //команда
            title = new TextView(context);
            title.setId(AndroidUtil.generateViewId());
            title.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            title.setTextColor(0xFF222222);
            title.setTextSize(16);
            title.setSingleLine();
            title.setEllipsize(TextUtils.TruncateAt.END);
            mainLayout.addView(title);

            RelativeLayout.LayoutParams titleLP = (RelativeLayout.LayoutParams) title.getLayoutParams();
            titleLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            titleLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            titleLP.leftMargin = Constant.DP_18;
            titleLP.addRule(RelativeLayout.CENTER_VERTICAL);
            titleLP.addRule(RelativeLayout.RIGHT_OF, icon.getId());
            //команда end

            //описание
            desc = new TextView(context);
            desc.setSingleLine();
            desc.setEllipsize(TextUtils.TruncateAt.END);
            desc.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
            desc.setTextColor(0xFF8A8A8A);
            desc.setTextSize(16);
            mainLayout.addView(desc);

            RelativeLayout.LayoutParams descLP = (RelativeLayout.LayoutParams) desc.getLayoutParams();
            descLP.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            descLP.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            descLP.leftMargin = Constant.DP_6;
            descLP.addRule(RelativeLayout.CENTER_VERTICAL);
            descLP.addRule(RelativeLayout.RIGHT_OF, title.getId());
            descLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            //описание end
        }
    }
}