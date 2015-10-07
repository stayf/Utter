package com.stayfprod.utter.manager;

import android.support.v7.app.AppCompatActivity;

import com.stayfprod.utter.App;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Observable;

public abstract class ResultController extends Observable implements Client.ResultHandler {

    protected Client client() {
        initTG();
        return TG.getClientInstance();
    }

    @SuppressWarnings("ALL")
    public static void initTG() {
        if (TG.isEmptyInstance() || AuthManager.resetAction) {
            synchronized (ResultController.class) {
                if (TG.isEmptyInstance() || AuthManager.resetAction) {
                    AuthManager.resetAction = false;
                    TG.stopClient();
                    TG.setUpdatesHandler(UpdateHandler.getHandler());
                    TG.setDir(FileUtils.mkExternalDir(App.STG_FOLDER_DB).getAbsolutePath() + "/");
                    TG.setFilesDir(FileUtils.mkExternalDir(App.STG_FOLDER_FILES).getAbsolutePath() + "/");
                }
            }
        }
    }

    @Override
    public void onResult(TdApi.TLObject object, int calledConstructor) {
        afterResult(object, calledConstructor);

        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                TdApi.Error error = (TdApi.Error) object;
                if (error.text.toLowerCase().contains("no auth")) {
                    try {
                        StickerManager.getManager().destroy();
                        ChatListManager.getManager().destroy();
                        new AuthManager(null, null).openAuthActivity((AppCompatActivity) App.getAppContext());
                    }catch (Exception e){
                        //
                    }
                } else {
                    String errorText = error.text.toLowerCase();
                    //todo на данный момент не известны ни коды ошибок ни их постоянно меняющееся описание(((
                    if (!errorText.contains("unknown chat id") && !errorText.contains("chat not found") && !errorText.contains("name_not_modified")) {
                        AndroidUtil.showToastShort(/*error.code + "-" +*/ error.text /*+ " for " + calledConstructor*/);
                    }
                }
                break;
        }
    }

    public abstract void afterResult(TdApi.TLObject object, int calledConstructor);
}
