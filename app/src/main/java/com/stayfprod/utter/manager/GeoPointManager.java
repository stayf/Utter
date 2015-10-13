package com.stayfprod.utter.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.stayfprod.utter.App;
import com.stayfprod.utter.service.CacheService;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.view.chat.GeoMsgView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoPointManager {
    private static final String LOG = GeoPointManager.class.getSimpleName();

    private static volatile GeoPointManager sGeoPointManager;

    public static GeoPointManager getManager() {
        if (sGeoPointManager == null) {
            synchronized (GeoPointManager.class) {
                if (sGeoPointManager == null) {
                    sGeoPointManager = new GeoPointManager();
                }
            }
        }
        return sGeoPointManager;
    }

    public Bitmap getGoogleMapThumbnail(double lati, double longi, String key) {
        Bitmap bmp = null;
        HttpURLConnection urlc = null;
        InputStream inputStream = null;
        try {
            URL url = new URL("http://maps.google.com/maps/api/staticmap?center=" + lati + "," + longi + "&zoom=12&size=300x200&markers=color:red%7C" + lati + "," + longi + "&sensor=false");
            urlc = (HttpURLConnection) (url.openConnection());
            urlc.connect();
            inputStream = urlc.getInputStream();
            bmp = BitmapFactory.decodeStream(inputStream);
        } catch (Throwable e) {
            Log.w(LOG, "getGoogleMapThumbnail", e);
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
            FileUtil.close(inputStream);
        }

        return bmp;
    }

    public BitmapDrawable generateAndSetImage(TdApi.Location geoPoint, final GeoMsgView geoMsgView, final String tag) {
        final double lati = geoPoint.latitude;
        final double longi = geoPoint.longitude;

        final String key = lati + "" + longi;
        BitmapDrawable bitmapDrawable = CacheService.getInstance().getBitmapDrawable(key);
        if (bitmapDrawable == null) {
            ThreadService.runTaskBackground(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = getGoogleMapThumbnail(lati, longi, key);
                    final BitmapDrawable bitmapDrawable = new BitmapDrawable(App.getAppResources(), bitmap);
                    bitmapDrawable.setBounds(0, 0, GeoMsgView.GEO_WIDTH, GeoMsgView.GEO_HEIGHT);
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (AndroidUtil.isItemViewVisible(geoMsgView, tag)) {
                                geoMsgView.setImageAndUpdateAsync(bitmapDrawable);
                            }
                        }
                    });
                    CacheService.getInstance().addBitmapToMemoryCache(key, bitmapDrawable);
                }
            });
        }

        return bitmapDrawable;
    }
}
