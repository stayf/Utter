package com.stayfprod.utter.manager;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.R;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

public class CameraManager extends Observable {
    private static final String LOG = CameraManager.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 1;

    private static volatile CameraManager sCameraManager;

    public static CameraManager getManager() {
        if (sCameraManager == null) {
            synchronized (CameraManager.class) {
                if (sCameraManager == null) {
                    sCameraManager = new CameraManager();
                }
            }
        }
        return sCameraManager;
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    public void galleryAddPic(final Context context, final String path) {
        ThreadService.runTaskBackground(new Runnable() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void run() {
                try {
                    File inputFile = new File(path);
                    File galleryFile = FileUtil.createGalleryFile(inputFile);
                    FileUtil.copyFile(inputFile, galleryFile);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, galleryFile.getAbsolutePath());
                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    AndroidUtil.showToastLong(AndroidUtil.getResourceString(R.string.photo_added_to_gallery));
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            CameraManager.this.notifyObservers(new NotificationObject(NotificationObject.GALLERY_UPDATE_DATA, null));
                        }
                    });

                } catch (Exception e) {
                    AndroidUtil.showToastLong(AndroidUtil.getResourceString(R.string.can_not_add_file_to_gallery));
                }
            }
        });
    }

    public static boolean isImageInGallery(Context context, File imageFile) {
        boolean returned = false;
        try {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{FileUtil.createGalleryFile(imageFile).getAbsolutePath()}, null);

            if (cursor != null) {
                returned = cursor.moveToFirst();
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(LOG, "isImageInGallery", e);
            Crashlytics.logException(e);
        }
        return returned;
    }

    public String dispatchTakePictureIntent(Context context) {
        String imgPath = "";
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = FileUtil.createTakePhotoFile();
                imgPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Log.w(LOG, "dispatchTakePictureIntent", ex);
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        return imgPath;
    }
}