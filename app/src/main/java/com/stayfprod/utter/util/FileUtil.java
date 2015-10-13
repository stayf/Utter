package com.stayfprod.utter.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.service.CacheService;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    private static final String LOG = FileUtil.class.getSimpleName();

    public enum CalculateType {
        BY_WIDTH,
        BY_HEIGHT,
        BOTH
    }

    public static BitmapDrawable decodeFileInBitmapDrawable(String path) {
        return decodeFileInBitmapDrawable(path, FileUtil.superBitmapOptions());
    }

    public static BitmapDrawable decodeFileInBitmapDrawable(String path, BitmapFactory.Options opts) {
        Bitmap bitmap = null;
        FileInputStream fs = null;
        try {
            //декодирование может занимать 10-30 миллисекунд
            fs = new FileInputStream(path);
            bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, opts);
        } catch (Throwable e) {
            Log.w(LOG, "decodeFileInBitmapDrawable", e);
            //Crashlytics.logException(e);
        } finally {
            close(fs);
        }
        return new BitmapDrawable(App.getAppResources(), bitmap);
    }

    /*
    * Никогда не юзать эту штуку вместе с
    * option.inPurgeable = true;
    * option.inInputShareable = true;
    * */
    private static void addInBitmapOptions(BitmapFactory.Options options) {
        if (options != null) {
            // inBitmap only works with mutable bitmaps, so force the decoder to
            // return mutable bitmaps.
            options.inMutable = true;

            if (options.inSampleSize == 0) {
                options.inSampleSize = 1;
            }

            // Try to find a bitmap to use for inBitmap.
            Bitmap inBitmap = CacheService.getInstance().getBitmapFromReusableSet(options);
            if (inBitmap != null) {
                // If a suitable bitmap has been found, set it as the value of
                // inBitmap.
                options.inBitmap = inBitmap;
            }
        }
    }

    public static BitmapFactory.Options prepareOptions(String path, int maxSize, CalculateType calculateType, boolean... inPurgeable) {
        FileInputStream fis = null;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bmOptions.inJustDecodeBounds = true;
            fis = new FileInputStream(path);
            BitmapFactory.decodeStream(fis, null, bmOptions);
            switch (calculateType) {
                case BY_WIDTH:
                    bmOptions.inSampleSize = calculateScaleByWidth(bmOptions, maxSize);
                    break;
                case BY_HEIGHT:
                    bmOptions.inSampleSize = calculateScaleByHeight(bmOptions, maxSize);
                    break;
                case BOTH:
                    bmOptions.inSampleSize = calculateScale(bmOptions, maxSize);
                    break;
            }

            return inPurgeable.length == 0 ? superBitmapOptions(bmOptions) : superBitmapOptions(inPurgeable[0], bmOptions);
        } catch (Throwable e) {
            Log.e(LOG, "prepareOptions", e);
            Crashlytics.logException(e);
        } finally {
            close(fis);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static BitmapFactory.Options superBitmapOptions(boolean inPurgeable, BitmapFactory.Options... bmOptions) {
        BitmapFactory.Options option = bmOptions.length == 1 ? bmOptions[0] : null;
        if (option == null) {
            option = new BitmapFactory.Options();
        }

        option.inJustDecodeBounds = false;
        //addInBitmapOptions(option);
        //info прикол в том, что при малых размер изображения не работают inPurgeable и inInputShareable
        //Сглаживание цветовой палитры
        option.inDither = false;
        //При проблемах с памятью разрешаем системе временно удалить объект Bitmap(gc отработает).
        //когда потребуется вывести картинку на экран, то объект восстанавливается.
        //Естественно, при этом падает производительность из-за повторной работы по обработке изображени
        //получается так что максимум он может открыть 1024 файлов(принудительно не закрывает их система) и после вылетит EMFILE, каждое открытие файла(пусть одного и того же) увеличивает счетчик
        //флаги inPurgeable и  inInputShareable хорошо подойдут для постоянно хранящихся картинок в памяти
        //option.inPurgeable = true;
        //Если true, то Bitmap хранит ссылку на источник, иначе – данные источника.
        //Но даже если true, то вполне может быть, что по усмотрению системы будут храниться данные, а не ссылка. Этот параметр актуален только при включенном inPurgeable.
        //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
        //option.inInputShareable = true;
        option.inTempStorage = new byte[32768];
        return option;
    }

    public static BitmapFactory.Options superBitmapOptions(BitmapFactory.Options... bmOptions) {
        return superBitmapOptions(true, bmOptions);
    }

    public static int[] readFileSize(String path) {
        int[] returned = {0, 0};
        FileInputStream fis = null;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            fis = new FileInputStream(path);
            BitmapFactory.decodeStream(fis, null, bmOptions);
            returned[0] = bmOptions.outHeight;
            returned[1] = bmOptions.outWidth;
        } catch (Throwable e) {
            Log.e(LOG, "readFileSize", e);
            Crashlytics.logException(e);
        } finally {
            close(fis);
        }
        return returned;
    }

    public static Drawable decodeResource(Resources resources, int id) {
        return ResourcesCompat.getDrawable(resources, id, null);
    }

    public static Drawable decodeResource(int id) {
        return ResourcesCompat.getDrawable(App.getAppResources(), id, null);
    }

    public static BitmapDrawable decodeImageResource(int id) {
        return (BitmapDrawable) ResourcesCompat.getDrawable(App.getAppResources(), id, null);
    }

    public static int calculateScale(BitmapFactory.Options bmOptions, int maxSize) {
        int scale = 1;
        if (bmOptions.outWidth > maxSize) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSize /
                    (double) Math.max(bmOptions.outHeight, bmOptions.outWidth)) / Math.log(0.5)));
            //scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSize / (double) bmOptions.outWidth) / Math.log(0.5)));
        }
        return scale;
    }

    public static int calculateScaleByWidth(BitmapFactory.Options bmOptions, int maxSize) {
        int scale = 1;
        if (bmOptions.outWidth > maxSize) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSize / (double) bmOptions.outWidth) / Math.log(0.5)));
        }
        return scale;
    }

    public static int calculateScaleByHeight(BitmapFactory.Options bmOptions, int maxH) {
        int scale = 1;
        if (bmOptions.outHeight > maxH) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxH / (double) bmOptions.outHeight) / Math.log(0.5)));
        }
        return scale;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (source != null) {
            destination.transferFrom(source, 0, source.size());
            source.close();
        }
        destination.close();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createGalleryFile(File inputFile) throws Exception {
        File folder = FileUtil.mkExternalDir(App.GALLERY_PHOTO_STG);
        File f = new File(folder.getAbsolutePath(), inputFile.getAbsolutePath().hashCode() + ".jpg");
        f.createNewFile();
        return f;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createRecentStickersFile() throws Exception {
        File folder = FileUtil.mkExternalDir(App.STG_RECENT_STICKERS);
        File f = new File(folder.getAbsolutePath(), "stickers.tmp");
        f.createNewFile();
        return f;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createRecordVoiceFile() {
        File folder = FileUtil.mkExternalDir(App.STG_RECORD_VOICE);
        File f = new File(folder.getAbsolutePath(), System.currentTimeMillis() + ".ogg");
        try {
            f.createNewFile();
        } catch (IOException e) {
            //
        }
        return f;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFile(String folderName, String fileName) {
        File folder = FileUtil.mkExternalDir(folderName);
        File f = new File(folder.getAbsolutePath(), fileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
            //
        }
        return f;
    }

    @SuppressLint("SimpleDateFormat")
    public static File createTakePhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String createTmpPngFile(Bitmap bitmap) {
        String tempFileStr = "";
        FileOutputStream fos = null;
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), App.STG_TEMP_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File tmp = File.createTempFile("sticker_", null, dir);
            fos = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            tempFileStr = tmp.getAbsolutePath();
        } catch (Exception e) {
            Log.w(LOG, "createTmpPng", e);
        } finally {
            close(fos);
        }
        return tempFileStr;
    }

    public static void deleteFile(String file) {
        File f = new File(file);
        if (!f.delete()) {
            Log.w(LOG, "can't delete file " + file);
        }
    }

    public static void cleanOldTempDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(), App.STG_TEMP_FOLDER_MAIN);
        File[] files = directory.listFiles();
        if (files == null) {
            Log.e(LOG, "Failed to list contents of " + directory);
            return;
        }

        for (File file : files) {
            try {
                if (!file.getAbsolutePath().contains(App.STG_TEMP_FOLDER)) {
                    deleteDirectory(file);
                }
            } catch (Exception e) {
                Log.e(LOG, "cleanOldTempDirectory", e);
            }
        }
    }

    public static void cleanDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }

        if (!directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            Log.e(LOG, "Failed to list contents of " + directory);
            return;
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            Log.w(LOG, "cleanDirectory", exception);
        }
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    //везде позакрывать с его помощью
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                //
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File mkExternalDir(String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static boolean isTDFileLocal(TdApi.File file) {
        return /*TextUtil.isNotBlank(file.persistentId) &&*/ file != null && TextUtil.isNotBlank(file.path);
    }

    public static boolean isTDFileEmpty(TdApi.File file) {
        return TextUtil.isBlank(file.path);
    }

    public static void saveObject(File file, Object data) {
        ObjectOutputStream oos = null;
        try {
            try {
                oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(data);
            } finally {
                if (oos != null)
                    oos.close();
            }
        } catch (Exception e) {
            Log.e(LOG, "saveObject to " + file + " failed", e);
            Crashlytics.logException(e);
        }
    }

    public static void saveObject(Context context, String fileName, Object data) {
        saveObject(new File(context.getCacheDir(), fileName), data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadObject(File file) {
        Object instance = null;
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                try {
                    ois = new ObjectInputStream(new FileInputStream(file));
                    instance = ois.readObject();
                } finally {
                    if (ois != null)
                        ois.close();
                }
            } catch (Exception e) {
                Log.w(LOG, "loadObject from " + file + " failed", e);
            }
        }
        try {
            return (T) instance;
        } catch (ClassCastException e) {
            return null;
        }
    }
}

