package com.stayfprod.utter.service;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.stayfprod.utter.Constant;
import com.stayfprod.utter.ui.drawable.IconDrawable;
import com.stayfprod.utter.ui.view.IconUpdatable;
import com.stayfprod.utter.ui.view.chat.ContactMsgView;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.TdApi;

public class IconFactory {

    public static volatile int MAX_ICON_HEIGHT = Constant.DP_70;

    public enum Type {
        CHAT_LIST(Constant.DP_54),
        CHAT(Constant.DP_44),
        TITLE(Constant.DP_45),
        USER(Constant.DP_64),
        BOT_COMMAND(Constant.DP_27);

        private int height;

        Type(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }
    }

    private static int colors[] = {
            0xFF7FCBD9,
            0xFF70B2D8,
            0xFF7BCB81,
            0xFFE48283,
            0xFFEEA781,
            0xFFEF83AC,
            0xFFE6C26C,

            0xff8e85ee,
            0xFF7BCB81,
            0xFFEF83AC
    };

    public static IconDrawable createBitmapIcon(Type type, String bitmapPath) {
        return createIcon(bitmapPath, true, type, 0, null);
    }

    public static IconDrawable createIcon(Type type, int id, String text, TdApi.File file) {
        if (file != null) {
            if (FileUtils.isTDFileLocal(file)) {
                return createBitmapIcon(type, file.path);
            } else {
                return createEmptyIcon(type, id, text);
            }
        }
        return null;
    }

    private static IconDrawable createIcon(String bitmapPath, boolean isUsedImage, Type type, int color, String initials) {
        IconDrawable drawable;
        String key;
        if (isUsedImage) {
            key = bitmapPath + type.getHeight();
            drawable = CacheService.getManager().getIconDrawableFromCache(key);
            if (drawable == null) {
                BitmapDrawable bitmapDrawable = FileUtils.decodeFileInBitmapDrawable(bitmapPath,
                        FileUtils.prepareOptions(bitmapPath, MAX_ICON_HEIGHT, FileUtils.CalculateType.BOTH, false));
                drawable = new IconDrawable(bitmapDrawable == null ? null : bitmapDrawable.getBitmap(), type);
                drawable.setBounds(0, 0, type.getHeight(), type.getHeight());
                CacheService.getManager().addToDrawableCache(key, drawable);
            }
        } else {
            key = color + initials + type.getHeight();
            drawable = new IconDrawable(color, initials, type);
            CacheService.getManager().addToDrawableCache(key, drawable);
        }
        return drawable;
    }

    public static IconDrawable createEmptyIcon(Type type, int id, String initials) {
        int color = chooseColor(id);
        String key = color + initials + type.getHeight();
        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
        if (drawable == null) {
            drawable = new IconDrawable(color, initials, type);
            CacheService.getManager().addToDrawableCache(key, drawable);
        }
        return drawable;
    }

    public static void putLocalIconInCache(final Type type, TdApi.ChatInfo chatInfo) {
        TdApi.File fileLocal = null;
        switch (chatInfo.getConstructor()) {
            case TdApi.PrivateChatInfo.CONSTRUCTOR: {
                TdApi.User user = ((TdApi.PrivateChatInfo) chatInfo).user;
                TdApi.File file = user.profilePhoto.small;

                if (FileUtils.isTDFileLocal(file)) {
                    fileLocal = file;
                }
                break;
            }
            case TdApi.GroupChatInfo.CONSTRUCTOR: {
                TdApi.GroupChatInfo groupChatInfo = (TdApi.GroupChatInfo) chatInfo;
                TdApi.GroupChat groupChat = groupChatInfo.groupChat;

                if (FileUtils.isTDFileLocal(groupChat.photo.small)) {
                    fileLocal = groupChat.photo.small;
                }
                break;
            }
        }

        if (fileLocal != null) {
            final String bitmapPath = fileLocal.path;
            final String key = bitmapPath + type.getHeight();
            IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
            if (drawable == null) {
                BitmapDrawable bitmapDrawable = FileUtils.decodeFileInBitmapDrawable(bitmapPath,
                        FileUtils.prepareOptions(bitmapPath, MAX_ICON_HEIGHT, FileUtils.CalculateType.BOTH, false));
                drawable = new IconDrawable(bitmapDrawable == null ? null : bitmapDrawable.getBitmap(), type);
                drawable.setBounds(0, 0, type.getHeight(), type.getHeight());
                CacheService.getManager().addToDrawableCache(key, drawable);
            }
        }

    }

    public static IconDrawable createBitmapIconForContact(final Type type, final String bitmapPath, final ContactMsgView itemView, final String tag) {
        final String key = bitmapPath + type.getHeight();
        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
        if (drawable == null) {
            drawable = new IconDrawable(type);
            ThreadService.runSingleTaskWithLowestPriority(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
                        if (drawable == null) {
                            BitmapDrawable bitmapDrawable = FileUtils.decodeFileInBitmapDrawable(bitmapPath,
                                    FileUtils.prepareOptions(bitmapPath, MAX_ICON_HEIGHT, FileUtils.CalculateType.BOTH, false));
                            drawable = new IconDrawable(bitmapDrawable == null ? null : bitmapDrawable.getBitmap(), type);
                            drawable.setBounds(0, 0, type.getHeight(), type.getHeight());
                            CacheService.getManager().addToDrawableCache(key, drawable);
                            if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                itemView.setUserIconDrawableAndUpdateAsync(drawable);
                            }
                        } else {
                            itemView.setUserIconDrawableAndUpdateAsync(drawable);
                        }
                    }
                }
            });
        }
        return drawable;
    }

    public static IconDrawable createBitmapIconForChat(final Type type, final String bitmapPath, final View itemView, final String tag, boolean... isForward) {
        final String key = bitmapPath + type.getHeight();
        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
        final boolean forward = isForward.length > 0 && isForward[0];

        if (drawable == null) {
            drawable = new IconDrawable(type);
            ThreadService.runSingleTaskWithLowestPriority(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);
                        if (drawable == null) {
                            //todo стоит сохранять все же в кеше картинку и проверять ее???
                            BitmapDrawable bitmapDrawable = FileUtils.decodeFileInBitmapDrawable(bitmapPath,
                                    FileUtils.prepareOptions(bitmapPath, MAX_ICON_HEIGHT, FileUtils.CalculateType.BOTH, false));
                            drawable = new IconDrawable(bitmapDrawable == null ? null : bitmapDrawable.getBitmap(), type);
                            drawable.setBounds(0, 0, type.getHeight(), type.getHeight());
                            CacheService.getManager().addToDrawableCache(key, drawable);
                            if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                ((IconUpdatable) itemView).setIconAsync(drawable, forward);
                            }
                        } else {
                            ((IconUpdatable) itemView).setIconAsync(drawable, forward);
                        }
                    }
                }
            });
        }
        return drawable;
    }


    public static IconDrawable createBitmapIconForImageView(final Type type, final String bitmapPath, final View itemView, final ImageView imageView, final String tag) {
        final String key = bitmapPath + type.getHeight();
        IconDrawable drawable = CacheService.getManager().getIconDrawableFromCache(key);

        if (drawable == null) {
            ThreadService.runSingleTaskWithLowestPriority(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        final IconDrawable cachedDrawable = CacheService.getManager().getIconDrawableFromCache(key);
                        if (cachedDrawable == null) {
                            //todo стоит сохранять все же в кеше картинку и проверять ее???
                            BitmapDrawable bitmapDrawable = FileUtils.decodeFileInBitmapDrawable(bitmapPath,
                                    FileUtils.prepareOptions(bitmapPath, MAX_ICON_HEIGHT, FileUtils.CalculateType.BOTH, false));
                            final IconDrawable drawable = new IconDrawable(bitmapDrawable == null ? null : bitmapDrawable.getBitmap(), type);
                            drawable.setBounds(0, 0, type.getHeight(), type.getHeight());
                            CacheService.getManager().addToDrawableCache(key, drawable);
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                        imageView.setImageDrawable(drawable);
                                    }
                                }
                            });
                        } else {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                                        imageView.setImageDrawable(cachedDrawable);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        return drawable;
    }

    private static int chooseColor(int id) {
        return colors[(Math.abs(id) % 10)];
    }
}
