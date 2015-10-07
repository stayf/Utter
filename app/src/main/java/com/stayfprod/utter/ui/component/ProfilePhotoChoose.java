package com.stayfprod.utter.ui.component;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ChatManager;
import com.stayfprod.utter.manager.ProfileManager;
import com.stayfprod.utter.manager.ResultController;
import com.stayfprod.utter.manager.UserManager;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.ui.activity.AbstractActivity;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.manager.CameraManager;
import com.stayfprod.utter.util.ChatHelper;

public class ProfilePhotoChoose {

    private static final String LOG = ProfilePhotoChoose.class.getSimpleName();

    public static final int TYPE_SETTING_PHOTO = 1;
    public static final int TYPE_CHAT_PHOTO = 2;

    private static final int SELECT_PHOTO = 10010;

    private String photoPath;
    private int type;

    public ProfilePhotoChoose(int type) {
        this.type = type;
    }

    public void showDialog(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(AndroidUtil.getResourceString(R.string.chose_action));

        UserManager userManager = UserManager.getManager();
        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userManager.getCurrUserId());

        int size = cachedUser.tgUser.profilePhoto.small.id > 0 ? 3 : 2;

        final String[] names = new String[size];

        names[0] = AndroidUtil.getResourceString(R.string.image_from_gallery);
        names[1] = AndroidUtil.getResourceString(R.string.image_from_camera);

        if (size > 2) {
            names[2] = AndroidUtil.getResourceString(R.string.delete_profile_photo);
        }

        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        ((AbstractActivity) context).startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                        break;
                    case 1:
                        try {
                            photoPath = CameraManager.getManager().dispatchTakePictureIntent(context);
                        } catch (Exception e) {
                            AndroidUtil.showToastLong(e.getMessage() + "");
                        }
                        break;
                    case 2:
                        UserManager userManager = UserManager.getManager();
                        CachedUser cachedUser = userManager.getUserByIdWithRequestAsync(userManager.getCurrUserId());
                        userManager.deleteProfilePhoto(cachedUser.tgUser.profilePhoto.id);
                        break;
                }
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    }

    public void processImage(int requestCode, int resultCode, Intent data, Context context) {
        switch (requestCode) {
            case CameraManager.REQUEST_TAKE_PHOTO:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    if (type == TYPE_SETTING_PHOTO) {
                        changeUserPhoto(data, context);
                    } else {
                        changeChatPhoto(data, context);
                    }
                }
                break;
            case SELECT_PHOTO:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    if (type == TYPE_SETTING_PHOTO) {
                        changeUserPhoto(data, context);
                    } else {
                        changeChatPhoto(data, context);
                    }
                }
                break;
        }
    }

    private void changeUserPhoto(Intent data, Context context) {
        try {
            UserManager userManager = UserManager.getManager();
            if (data != null) {
                Uri selectedImage = data.getData();
                userManager.setProfilePhoto(ChatHelper.getRealPathFromURI(selectedImage, context));
            } else {
                userManager.setProfilePhoto(photoPath);
            }
        } catch (Throwable e) {
            AndroidUtil.showToastShort(e.getMessage());
        }
    }

    private void changeChatPhoto(Intent data, Context context) {
        try {
            ProfileManager profileManager = ProfileManager.getManager();
            ChatManager chatManager = ChatManager.getManager();
            if (data != null) {
                Uri selectedImage = data.getData();
                chatManager.changeChatPhoto(ChatHelper.getRealPathFromURI(selectedImage, context), profileManager.getChatInfo().tgChatObject.id);
            } else {
                chatManager.changeChatPhoto(photoPath, profileManager.getChatInfo().tgChatObject.id);
            }
        } catch (Throwable e) {
            AndroidUtil.showToastShort(e.getMessage());
        }
    }
}