package com.stayfprod.utter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.emojicon.EmojiCache;
import com.stayfprod.emojicon.EmojConstant;
import com.stayfprod.emojicon.EmojiconRecentsManager;
import com.stayfprod.utter.manager.NotificationManager;
import com.stayfprod.utter.manager.PassCodeManager;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.ui.activity.ChatListActivity;
import com.stayfprod.utter.ui.activity.EnterPassCodeActivity;
import com.stayfprod.utter.ui.activity.InitActivity;
import com.stayfprod.utter.ui.listener.AppStatusListener;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.util.FileUtil;

import java.io.File;
import java.nio.ByteBuffer;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    public static final String STG_FOLDER_MAIN = "Utter";
    public static final String STG_TEMP_FOLDER_MAIN = STG_FOLDER_MAIN + File.separator + "tmp";
    public static final String STG_TEMP_FOLDER = STG_TEMP_FOLDER_MAIN + File.separator + System.currentTimeMillis();
    public static final String STG_FOLDER_DB = STG_FOLDER_MAIN + File.separator + "db";
    public static final String STG_FOLDER_FILES = STG_FOLDER_MAIN + File.separator + "files";
    public static final String GALLERY_PHOTO_STG = STG_FOLDER_MAIN + File.separator + "PhotoGallery";
    public static final String STG_RECENT_STICKERS = STG_FOLDER_MAIN + File.separator + "RecentStickers";
    public static final String STG_RECORD_VOICE = STG_FOLDER_MAIN + File.separator + "VoiceRecords";

    private static volatile Context sAppContext;
    private static volatile Handler sAppHandler;

    public static final int CURRENT_VERSION_SDK = android.os.Build.VERSION.SDK_INT;

    private static boolean inForeground = true;
    private static int resumed = 0;
    private static int paused = 0;
    private static boolean isInBackground;
    private static boolean isAwakeFromBackground;
    private static final int backgroundAllowance = 500;
    private static volatile boolean isFirstInit = true;

    public static Context getAppContext() {
        return sAppContext;
    }

    public static Handler getAppHandler() {
        return sAppHandler;
    }

    public static void setAppContext(Context appContext) {
        sAppContext = appContext;
    }

    public static boolean isAppInForeground() {
        if (paused >= resumed)
            inForeground = false;
        else if (resumed > paused)
            inForeground = true;
        return inForeground;
    }

    public static boolean isBadAppContext(Context context) {
        if (!(sAppContext instanceof AppCompatActivity)) {
            Intent intent = new Intent(context, InitActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            AppCompatActivity activity = (AppCompatActivity) context;
            activity.getWindow().setWindowAnimations(0);
            activity.overridePendingTransition(0, 0);
            activity.startActivity(intent);
            activity.supportFinishAfterTransition();
            return true;
        }
        return false;
    }

    public static void checkAppContext(Context context) {
        if (!(sAppContext instanceof AppCompatActivity)) {
            sAppContext = context;
        }
    }

    public static Resources getAppResources() {
        return sAppContext.getResources();
    }

    private void initCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {

                Crashlytics.logException(ex);
                Log.e("APP", "initCrashHandler", ex);

                Intent i = new Intent(getApplicationContext(), InitActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                System.exit(1);
            }
        });
    }


    @Override
    public void onCreate() {

        super.onCreate();
        //info 250 миллисекунд на слабом, на номальном 50
        Fabric.with(this, new Crashlytics());
        initCrashHandler();

        sAppContext = getApplicationContext();
        sAppHandler = new Handler(this.getMainLooper());

        //заставляем gc работать жещще
        if (CURRENT_VERSION_SDK < Build.VERSION_CODES.LOLLIPOP) {
            //info выделение памяти 50 миллисекунд на слабом устройстве
            ByteBuffer huckBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
            huckBuffer.clear();
        }
        EmojConstant.init(sAppContext);

        //info 563 миллескунды на слабом устройстве только initEmojiBitmaps, на нормальном 78 миллисекунд
        //todo вынести в аснихронное получение, разбить на части
        EmojiCache.getInstance().initEmojiBitmaps(sAppContext.getResources());

        ThreadService.runSingleTaskWithLowestPriority(new Runnable() {
            @Override
            public void run() {
                //поднимаем закешированные смайлы
                EmojiconRecentsManager.getInstance(sAppContext);
                FileUtil.cleanOldTempDirectory();
            }
        });

        registerActivityLifecycleCallbacks(new AppStatusListener() {
            @Override
            public void onActivityResumed(Activity activity) {
                ++resumed;
                if (isAppInForeground()) {
                    NotificationManager.getManager().cancelNotification(activity);
                }

                isInBackground = false;
                final PassCodeManager passCodeManager = PassCodeManager.getManager();
                if ((isAwakeFromBackground && isAppInForeground()) || isFirstInit) {
                    if (ChatListActivity.sIsChatListActivityStarted && !EnterPassCodeActivity.sIsOpenedActivity) {
                        if (passCodeManager.isNeedPassCode(getAppContext())) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    passCodeManager.openEnterPassCodeActivity(getAppContext(), false);
                                }
                            }, 40);
                        } else {
                            passCodeManager.setCurrentAutoLockStartTimerAsync(getAppContext(), System.currentTimeMillis());
                        }
                    }

                    if (ChatListActivity.sIsChatListActivityStarted) {
                        isFirstInit = false;
                    }
                } else {
                    passCodeManager.setCurrentAutoLockStartTimerAsync(getAppContext(), System.currentTimeMillis());
                }
                isAwakeFromBackground = false;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                ++paused;
                isInBackground = true;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isInBackground) {
                            isAwakeFromBackground = true;
                        }
                    }
                }, backgroundAllowance);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                PassCodeManager.getManager().setCurrentAutoLockStartTimerAsync(getAppContext(), System.currentTimeMillis());
            }
        });
    }

    /*
    * Вызывается при изменении конфигурации устройства
    * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:

                break;
            case Configuration.ORIENTATION_PORTRAIT:

                break;
        }
    }

    /*
    * Вызывается когда система работает в условиях нехватки памяти, и просит работающие процессы попытаться сэкономить ресурсы.
    * */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /*
    * Вызывается, когда операционная система решает, что сейчас хорошее время для обрезания ненужной памяти из процесса.
    * */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
