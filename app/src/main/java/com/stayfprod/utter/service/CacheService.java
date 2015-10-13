package com.stayfprod.utter.service;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LruCache;

import com.stayfprod.utter.App;
import com.stayfprod.utter.ui.drawable.IconDrawable;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CacheService {
    private static volatile CacheService sCacheService;

    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int cacheSize = maxMemory / 8;

    private final Set<SoftReference<Bitmap>> reusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    private final LruCache<String, Drawable> drawableCache = new LruCache<String, Drawable>(cacheSize) {
        @Override
        protected int sizeOf(String key, Drawable drawable) {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap().getByteCount() / 1024;
            } else {
                return ((IconDrawable) drawable).getKByteCount();
            }
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
            if (oldValue instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) (oldValue)).getBitmap();
                if (bitmap.isMutable()) {
                    reusableBitmaps.add(new SoftReference<Bitmap>(bitmap));
                }
            }
        }
    };

    public static CacheService getInstance() {
        if (sCacheService == null) {
            synchronized (CacheService.class) {
                if (sCacheService == null) {
                    sCacheService = new CacheService();
                }
            }
        }
        return sCacheService;
    }

    public void cleanBitmaps() {
        drawableCache.evictAll();
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (drawableCache.get(key) == null && bitmap != null) {
            drawableCache.put(key, new BitmapDrawable(App.getAppResources(), bitmap));
        }
    }

    public void addBitmapToMemoryCache(String key, BitmapDrawable bitmapDrawable) {
        if (drawableCache.get(key) == null && bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            drawableCache.put(key, bitmapDrawable);
        }
    }

    public BitmapDrawable getBitmapDrawable(String key) {
        return (BitmapDrawable) drawableCache.get(key);
    }


    public void addToDrawableCache(String key, IconDrawable drawable) {
        drawableCache.put(key, drawable);
    }

    public IconDrawable getIconDrawableFromCache(String key) {
        return (IconDrawable) drawableCache.get(key);
    }

    private Bitmap decodeSource(Resources resources, int id) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;
        bmOptions.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, id, bmOptions);
    }

    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (!reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();
                    if (null != item && item.isMutable()) {
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;
                            iterator.remove();
                            break;
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @SuppressLint("NewApi")
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        if (App.CURRENT_VERSION_SDK >= Build.VERSION_CODES.KITKAT) {
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

}
