package com.stayfprod.utter.ui.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stayfprod.utter.App;
import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.manager.CameraManager;
import com.stayfprod.utter.R;
import com.stayfprod.utter.ui.view.TouchImageView;

import java.io.File;

public class PhotoActivity extends AbstractActivity {

    private File file;
    private BitmapDrawable bitmapDrawable;
    private Boolean isImageView;
    private int msgId;
    private boolean isFromSharedMedia;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //выбираем меню в зависисоти от того, есть ли фотка в галерее
        boolean inGallery = CameraManager.isImageInGallery(PhotoActivity.this, file);
        if (inGallery) {
            getMenuInflater().inflate(R.menu.menu_photo_delete, menu);
            if (isFromSharedMedia) {
                menu.getItem(0).setVisible(false);
            }
        } else {
            getMenuInflater().inflate(R.menu.menu_photo, menu);
            if (isFromSharedMedia) {
                menu.getItem(1).setVisible(false);
            }
        }
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (isImageView != null && isImageView) {
            switch (id) {
                case R.id.action_photo_add_to_gallery:
                    //добавляем в галерею, возвращаем, что добавили в галерею, показываем уведомление о добавлении
                    CameraManager.getManager().galleryAddPic(this, file.getAbsolutePath());
                    onBackPressed();
                    break;
                case R.id.action_photo_delete:
                    Intent data = new Intent();
                    data.putExtra("delete", true);
                    data.putExtra("msgId", msgId);
                    setResult(RESULT_OK, data);
                    onBackPressed();
                    break;
            }

        } else {
            switch (id) {
                case R.id.action_photo_add_to_gallery:
                    CameraManager.getManager().galleryAddPic(this, file.getAbsolutePath());
                    onBackPressed();
                    break;
                case R.id.action_photo_delete:
                    //удалить, выйти
                    file.delete();
                    onBackPressed();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_photo);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String filePath = extras.getString("filePath");
            isImageView = extras.getBoolean("imageView");
            msgId = extras.getInt("msgId");
            isFromSharedMedia = extras.getBoolean("isFromSharedMedia", false);

            file = new File(filePath);
            bitmapDrawable = FileManager.getManager().getBitmapFullScreen(file);

            TouchImageView touchImageView = (TouchImageView) findViewById(R.id.photo_touch_image);
            touchImageView.setImageDrawable(bitmapDrawable);
            touchImageView.setMaxZoom(4f);
        }
        setToolbar();
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_back_photo);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}
