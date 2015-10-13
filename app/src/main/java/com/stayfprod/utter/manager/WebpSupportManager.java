package com.stayfprod.utter.manager;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.reciver.WebpReceiver;
import com.stayfprod.utter.service.CacheService;
import com.stayfprod.utter.service.WebpSupportService;
import com.stayfprod.utter.ui.view.ImageUpdatable;
import com.stayfprod.utter.ui.view.chat.StickerMsgView;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.AndroidUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WebpSupportManager {
    private static final String LOG = WebpSupportManager.class.getSimpleName();

    static class QueueEntry {
        String path;
        FileManager.TypeLoad type;
        View itemView;
        String tag;
        boolean localAnimate;
        int[] bounds;

        ImageView imageView;

        public QueueEntry(String path, FileManager.TypeLoad type, View itemView, String tag, boolean localAnimate, int[] bounds) {
            this.path = path;
            this.type = type;
            this.itemView = itemView;
            this.tag = tag;
            this.localAnimate = localAnimate;
            this.bounds = bounds;
        }

        public QueueEntry(String path, FileManager.TypeLoad type, View itemView, String tag, boolean localAnimate, ImageView imageView) {
            this.path = path;
            this.type = type;
            this.itemView = itemView;
            this.tag = tag;
            this.localAnimate = localAnimate;
            this.imageView = imageView;
        }
    }

    public static final boolean IS_NEED_NATIVE_LIB = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;

    private static WebpSupportManager sManager;

    private WebpReceiver mWebpReceiver;
    private SparseArray<Object[]> mObjectSparseList = new SparseArray<Object[]>();
    private ConcurrentLinkedQueue<QueueEntry> mConcurrentLinkedQueue = new ConcurrentLinkedQueue<QueueEntry>();
    private ConcurrentHashMap<String, String> mTmpFileHashMap = new ConcurrentHashMap<String, String>();

    private AtomicInteger mCounterList = new AtomicInteger(0);
    private AtomicBoolean mIsRunnable = new AtomicBoolean(false);

    public static WebpSupportManager getManager() {
        if (sManager == null) {
            synchronized (WebpSupportManager.class) {
                if (sManager == null) {
                    sManager = new WebpSupportManager();
                }
            }
        }
        return sManager;
    }

    private WebpReceiver getWebpReceiver() {
        if (mWebpReceiver == null) {
            mWebpReceiver = new WebpReceiver();
        }
        return mWebpReceiver;
    }

    public void handleReceived(final int key, final String tmpFile, final String path) {
        //именно один поток должен это обрабатывать!!!!
        ThreadService.runTaskChatBackground(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    QueueEntry queueObjects = mConcurrentLinkedQueue.poll();
                    if (queueObjects != null) {
                        if (AndroidUtil.isItemViewVisible(queueObjects.itemView, queueObjects.tag)) {
                            if (queueObjects.type == FileManager.TypeLoad.USER_STICKER_MICRO_THUMB) {
                                senRequest(queueObjects.path, queueObjects.type, queueObjects.itemView, queueObjects.tag, queueObjects.localAnimate, queueObjects.imageView);
                            } else {
                                senRequest(queueObjects.path, queueObjects.type, queueObjects.itemView, queueObjects.tag, queueObjects.localAnimate, queueObjects.bounds);
                            }
                            break;
                        } else
                            continue;
                    }
                    mIsRunnable.set(false);
                    break;
                }

                mTmpFileHashMap.put(path, tmpFile);
                Object[] objects = mObjectSparseList.get(key);
                if (objects != null) {
                    FileManager.TypeLoad type = (FileManager.TypeLoad) objects[4];

                    if (type == FileManager.TypeLoad.USER_STICKER_MICRO_THUMB) {
                        View itemView = (View) objects[0];
                        String tag = (String) objects[1];
                        boolean animate = (boolean) objects[2];
                        ImageView imageView = (ImageView) objects[3];

                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                            getAndDisplayImage(tmpFile, path, itemView, tag, type, imageView, animate);
                        }
                    } else {
                        View itemView = (View) objects[0];
                        String tag = (String) objects[1];
                        boolean animate = (boolean) objects[2];
                        int[] bounds = (int[]) objects[3];

                        if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                            getAndDisplayImage(tmpFile, path, itemView, tag, type, bounds, animate);
                        }
                    }
                    mObjectSparseList.remove(key);
                }
            }
        });
    }

    private boolean getAndDisplayImage(final String tmpFile, String path, final View itemView, final String tag, final FileManager.TypeLoad type, final int[] bounds, final boolean animate) {
        BitmapDrawable bitmapDrawable = CacheService.getInstance().getBitmapDrawable(path + type.name());
        if (bitmapDrawable == null) {
            bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(tmpFile, FileUtil.superBitmapOptions());
            if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
                StickerMsgView.setStickerBounds(bitmapDrawable, type, bounds);
                CacheService.getInstance().addBitmapToMemoryCache(path + type.name(), bitmapDrawable);
            }
        }
        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                ((ImageUpdatable) itemView).setImageAndUpdateAsync(bitmapDrawable, animate);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean getAndDisplayImage(final String tmpFile, String path, final View itemView, final String tag, final FileManager.TypeLoad type, final ImageView imageView, final boolean animate) {
        final BitmapDrawable cachedBitmapDrawable = CacheService.getInstance().getBitmapDrawable(path + type.name());
        final BitmapDrawable bitmapDrawable;
        if (cachedBitmapDrawable == null) {
            bitmapDrawable = FileUtil.decodeFileInBitmapDrawable(tmpFile, FileUtil.superBitmapOptions());
            if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
                CacheService.getInstance().addBitmapToMemoryCache(path + type.name(), bitmapDrawable);
            }
        } else {
            bitmapDrawable = cachedBitmapDrawable;
        }

        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            AndroidUtil.runInUI(new Runnable() {
                @Override
                public void run() {
                    if (AndroidUtil.isItemViewVisible(itemView, tag)) {
                        imageView.setImageDrawable(bitmapDrawable);
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public void loadWebP(final String path, final FileManager.TypeLoad type, final View itemView, final String tag, final int[] bounds, final boolean... animate) {
        boolean localAnimate = (animate.length > 0 && animate[0]);
        if (mTmpFileHashMap.containsKey(path)) {
            boolean answer = getAndDisplayImage(mTmpFileHashMap.get(path), path, itemView, tag, type, bounds, localAnimate);
            if (!answer) {
                mTmpFileHashMap.remove(path);
                mConcurrentLinkedQueue.add(new QueueEntry(path, type, itemView, tag, localAnimate, bounds));
                if (AndroidUtil.isItemViewVisible(itemView, tag) && mIsRunnable.compareAndSet(false, true)) {
                    senRequest(path, type, itemView, tag, localAnimate, bounds);
                }
            }
        } else {
            mConcurrentLinkedQueue.add(new QueueEntry(path, type, itemView, tag, localAnimate, bounds));
            if (AndroidUtil.isItemViewVisible(itemView, tag) && mIsRunnable.compareAndSet(false, true)) {
                senRequest(path, type, itemView, tag, localAnimate, bounds);
            }
        }
    }

    public void loadWebP(final String path, final FileManager.TypeLoad type, final View itemView, final String tag, final ImageView imageView, final boolean... animate) {
        boolean localAnimate = (animate.length > 0 && animate[0]);
        if (mTmpFileHashMap.containsKey(path)) {
            boolean answer = getAndDisplayImage(mTmpFileHashMap.get(path), path, itemView, tag, type, imageView, localAnimate);
            if (!answer) {
                mTmpFileHashMap.remove(path);
                mConcurrentLinkedQueue.add(new QueueEntry(path, type, itemView, tag, localAnimate, imageView));
                if (AndroidUtil.isItemViewVisible(itemView, tag) && mIsRunnable.compareAndSet(false, true)) {
                    senRequest(path, type, itemView, tag, localAnimate, imageView);
                }
            }
        } else {
            mConcurrentLinkedQueue.add(new QueueEntry(path, type, itemView, tag, localAnimate, imageView));
            if (AndroidUtil.isItemViewVisible(itemView, tag) && mIsRunnable.compareAndSet(false, true)) {
                senRequest(path, type, itemView, tag, localAnimate, imageView);
            }
        }
    }

    private void senRequest(final String path, final FileManager.TypeLoad type, final View itemView, final String tag, final boolean localAnimate, int[] bounds) {
        int key = mCounterList.incrementAndGet();
        mObjectSparseList.put(key, new Object[]{itemView, tag, localAnimate, bounds, type});
        startService(StickerMsgView.getStickerMaxHeight(type), path, key);
    }

    private void senRequest(final String path, final FileManager.TypeLoad type, final View itemView, final String tag, final boolean localAnimate, final ImageView imageView) {
        int key = mCounterList.incrementAndGet();
        mObjectSparseList.put(key, new Object[]{itemView, tag, localAnimate, imageView, type});
        startService(StickerMsgView.getStickerMaxHeight(type), path, key);
    }

    private void startService(int maxHeight, String path, int key) {
        Intent service = new Intent(App.getAppContext(), WebpSupportService.class);
        service.putExtra("maxHeight", maxHeight);
        service.putExtra("path", path);
        service.putExtra("key", key);
        App.getAppContext().startService(service);
    }

    public void registerReceiver(Context context) {
        if (IS_NEED_NATIVE_LIB) {
            IntentFilter intentFilter = new IntentFilter(WebpSupportService.ACTION);
            context.registerReceiver(getWebpReceiver(), intentFilter);
        }
    }

    public void unregisterReceiver(Context context) {
        if (IS_NEED_NATIVE_LIB) {
            context.unregisterReceiver(getWebpReceiver());
        }
    }
}
