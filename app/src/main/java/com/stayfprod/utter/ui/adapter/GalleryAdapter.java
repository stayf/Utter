package com.stayfprod.utter.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stayfprod.utter.manager.FileManager;
import com.stayfprod.utter.ui.listener.AnimatorEndListener;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.util.Logs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> /*implements Observer */ {
    public static final int IMAGE_SIZE = AndroidUtil.dp(100);
    private static final int PAD = AndroidUtil.dp(7);
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
    };

    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    /*+ " OR "
    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;*/
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private volatile int limit = 50;

    private Context context;
    private Cursor cursor;
    private CursorLoader cursorLoader;
    private GalleryAdapter galleryAdapter;
    private RecyclerView recyclerViewGallery;
    private SparseArray<StorageObject> selectedItems = new SparseArray<StorageObject>();
    private List<StorageObject> imageList = new ArrayList<StorageObject>();

    public static class StorageObject {
        public int id;
        public String path;
        public int pos;

        public StorageObject(int id, String path, int pos) {
            this.id = id;
            this.path = path;
            this.pos = pos;
        }
    }

    public SparseArray<StorageObject> getSelectedItems() {
        return selectedItems;
    }

    //todo http://stackoverflow.com/questions/15517920/how-do-cursorloader-automatically-updates-the-view-even-if-the-app-is-inactive
    public void updateGallery() {
        limit = 1;
        cursor = cursorLoader.loadInBackground();
    }

    public void rebuildPosSelectedItems() {
        int key = 0;
        SparseArray<StorageObject> sparseArray = new SparseArray<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            key = selectedItems.keyAt(i);
            GalleryAdapter.StorageObject object = selectedItems.get(key);
            object.pos = object.pos + 1;
            sparseArray.put(object.pos, object);
        }
        selectedItems.clear();
        for (int i = 0; i < sparseArray.size(); i++) {
            key = sparseArray.keyAt(i);
            GalleryAdapter.StorageObject object = sparseArray.get(key);
            selectedItems.put(object.pos, object);
        }
        sparseArray.clear();
    }

    private void rebuildImageIndex() {
        for (int i = 0; i < imageList.size(); i++) {
            GalleryAdapter.StorageObject storageObject = imageList.get(i);
            if (i > 0) {
                storageObject.pos++;
            }
        }
    }

    public GalleryAdapter(Context context, RecyclerView localRecyclerViewGallery) {
        this.galleryAdapter = this;
        this.recyclerViewGallery = localRecyclerViewGallery;
        this.context = context;
        cursorLoader = new CursorLoader(
                context,
                QUERY_URI,
                PROJECTION,
                SELECTION,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" + " limit " + limit
        ) {
            @Override
            public Cursor loadInBackground() {
                cursor = super.loadInBackground();
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int columnPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                if (limit == 1) {
                    cursor.moveToNext();
                    imageList.add(0, new StorageObject(cursor.getInt(columnIndex), cursor.getString(columnPath), 0));
                    rebuildPosSelectedItems();
                    rebuildImageIndex();
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (galleryAdapter != null) {
                                galleryAdapter.notifyDataSetChanged();
                                recyclerViewGallery.scrollToPosition(0);
                            }
                        }
                    });
                } else {
                    int i = 0;
                    while (cursor.moveToNext()) {
                        String fullPath = cursor.getString(columnPath);
                        if (new File(fullPath).exists()) {
                            imageList.add(new StorageObject(cursor.getInt(columnIndex), fullPath, i));
                            i++;
                        }
                    }
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            galleryAdapter.notifyDataSetChanged();
                        }
                    });
                }
                cursor.close();
                return cursor;
            }
        };
        cursorLoader.loadInBackground();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_galery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(IMAGE_SIZE, IMAGE_SIZE);
        params.setMargins(PAD, PAD, 0, PAD);

        holder.itemView.setTag(position);
        holder.image.setTag(position);
        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.image.setLayoutParams(params);
        holder.image.setImageResource(android.R.color.transparent);

        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.imageView.setLayoutParams(params);

        if (selectedItems.get(holder.getAdapterPosition()) != null) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.attachCheck.setVisibility(View.VISIBLE);
            holder.attachCheck.setScaleX(1.0f);
            holder.attachCheck.setScaleY(1.0f);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.attachCheck.setVisibility(View.GONE);
        }

        AndroidUtil.setImageDrawable(holder.image, FileManager.getManager().getBitmapFromGallery(
                imageList.get(holder.getAdapterPosition()).path, imageList.get(holder.getAdapterPosition()).id, holder.image, context, holder.image.getTag().toString()));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = ((View) v.getParent().getParent());
                TextView b = (TextView) parent.findViewById(R.id.choose_from_gallery_text);

                if (selectedItems.get(holder.getAdapterPosition()) != null) {
                    selectedItems.remove(holder.getAdapterPosition());
                    holder.imageView.setVisibility(View.GONE);
                    hideButton(holder.attachCheck);
                    if (selectedItems.size() == 0) {
                        b.setText(AndroidUtil.getResourceString(R.string.choose_from_gallery));
                    } else {
                        if (selectedItems.size() > 1) {
                            b.setText(AndroidUtil.getResourceString(R.string.send_) + selectedItems.size() + AndroidUtil.getResourceString(R.string._photos));
                        } else {
                            b.setText(AndroidUtil.getResourceString(R.string.send_) + selectedItems.size() + AndroidUtil.getResourceString(R.string._photo));
                        }
                    }

                } else {
                    StorageObject object = imageList.get(holder.getAdapterPosition());
                    selectedItems.put(holder.getAdapterPosition(), object);
                    if (selectedItems.size() > 1) {
                        b.setText(AndroidUtil.getResourceString(R.string.send_) + selectedItems.size() + AndroidUtil.getResourceString(R.string._photos));
                    } else {
                        b.setText(AndroidUtil.getResourceString(R.string.send_) + selectedItems.size() + AndroidUtil.getResourceString(R.string._photo));
                    }
                    holder.imageView.setVisibility(View.VISIBLE);
                    showButton(holder.attachCheck);
                }
            }
        });
    }

    public void hideButton(final View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new AccelerateInterpolator());
        animSetXY.setDuration(100);
        animSetXY.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //view.setVisibility(View.GONE);
            }
        });
        animSetXY.start();
    }

    public void showButton(final View view) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new OvershootInterpolator());
        animSetXY.setDuration(200);
        animSetXY.start();
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }


    public void update() {
        updateGallery();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ImageView imageView;
        public ImageView attachCheck;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = (ImageView) itemView.findViewById(R.id.i_gallery_photo);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            attachCheck = (ImageView) itemView.findViewById(R.id.i_gallery_ic_attach_check);
        }
    }
}
