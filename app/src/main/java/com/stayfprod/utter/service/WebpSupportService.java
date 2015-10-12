package com.stayfprod.utter.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import com.google.webp.libwebp;
import com.stayfprod.utter.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class WebpSupportService extends Service {

    public static final String ACTION = "com.stayfprod.utter.service.WebpSupportService";

    private static final String LOG = WebpSupportService.class.getSimpleName();

    static {
        try {
            System.loadLibrary("webp");
        } catch (Exception e) {
            Log.w(LOG, "failed to load webp library", e);
        }
    }

    public static int[] calculateSize(int w, int h, int maxH) {
        if (maxH >= h) {
            return new int[]{w, h};
        } else {
            return new int[]{w * maxH / h, maxH};
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        int key = intent.getIntExtra("key", 0);
        String path = intent.getStringExtra("path");
        int maxHeight = intent.getIntExtra("maxHeight", 0);
        Bitmap tmpBitmap = decode(path);
        Bitmap resBitmap = null;
        if (tmpBitmap != null) {
            if (maxHeight != 0) {
                resBitmap = scaleBitmap(tmpBitmap, calculateSize(tmpBitmap.getWidth(), tmpBitmap.getHeight(), maxHeight));
            } else {
                resBitmap = tmpBitmap;
            }
        }
        String tmpFile = FileUtil.createTmpPngFile(resBitmap);
        publishResults(tmpFile, key, path);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void publishResults(String tmpFile, int key, String path) {
        Intent intent = new Intent(WebpSupportService.ACTION);
        intent.putExtra("tmpFile", tmpFile);
        intent.putExtra("key", key);
        intent.putExtra("path", path);
        sendBroadcast(intent);
    }

    @SuppressWarnings("ALL")
    public Bitmap decode(String path) {
        File file = new File(path);
        FileInputStream fis = null;
        byte[] bytes;
        try {
            bytes = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            fis.read(bytes);
        } catch (Exception e) {
            return null;
        } finally {
            FileUtil.close(fis);
        }
        return decodeViaLibrary(bytes);
    }

    public Bitmap scaleBitmap(Bitmap tempBitmap, int[] sizes) {
        Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, sizes[0], sizes[1], true);
        if (bitmap != tempBitmap) {
            tempBitmap.recycle();
        }
        return bitmap;
    }

    Bitmap decodeViaLibrary(byte[] encoded) {
        int[] width = new int[]{0};
        int[] height = new int[]{0};
        byte[] decoded = libwebp.WebPDecodeARGB(encoded, encoded.length, width, height);
        if (width[0] == 0 || height[0] == 0 || decoded == null) {
            return null;
        }
        int[] pixels = new int[decoded.length / 4];
        ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);
        Bitmap bm = Bitmap.createBitmap(width[0], height[0], Bitmap.Config.ARGB_8888);
        bm.setPixels(pixels, 0, width[0], 0, 0, width[0], height[0]);
        decoded = null;
        return bm;
    }
}
