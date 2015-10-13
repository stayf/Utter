package com.stayfprod.utter.manager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.service.CacheService;
import com.stayfprod.utter.ui.activity.ChatListActivity;
import com.stayfprod.utter.ui.view.CircleProgressView;
import com.stayfprod.utter.util.AndroidUtil;
import com.stayfprod.utter.R;
import com.stayfprod.utter.ui.activity.auth.ActivationCodeActivity;
import com.stayfprod.utter.ui.activity.auth.PhoneNumberActivity;
import com.stayfprod.utter.ui.activity.auth.SetNameActivity;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

public class AuthManager extends ResultController {

    public static volatile boolean sIsButtonBlocked = false;
    public static volatile boolean sIsHaveAuth = false;
    public static volatile boolean sResetAction = false;

    private AppCompatActivity mActivity;
    private String mPhoneNumber;
    private CircleProgressView mProgressView;

    //info двойная авторизация
    /*TdApi.AuthStateWaitPassword
    TdApi.CheckAuthPassword
    TdApi.RecoverAuthPassword
    TdApi.RequestAuthPasswordRecovery
    TdApi.SetAuthBotToken*/

    public AuthManager(AppCompatActivity activity, CircleProgressView progressView) {
        this.mActivity = activity;
        this.mProgressView = progressView;
    }

    public void checkAuthState() {
        client().send(new TdApi.GetAuthState(), this);
    }

    public void setPhoneNumber(String phone) {
        TdApi.SetAuthPhoneNumber func = new TdApi.SetAuthPhoneNumber();
        func.phoneNumber = phone;
        mPhoneNumber = phone;
        client().send(func, this);
    }

    /*
    * Чтобы удалить существующую базу с ключами, можно использовать auth_reset.
    * */
    public void reset(AppCompatActivity compatActivity) {
        //info флаг ниже вызова клиента не опускать иначе падает
        AudioPlayer.getPlayer().stop();
        TdApi.ResetAuth reset = new TdApi.ResetAuth();
        reset.force = false;
        //info не нужна нам доп обработка повторная по no auth
        client().send(reset, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object, int calledConstructor) {
                //TdApi.AuthStateLoggingOut
            }
        });
    }

    public void openAuthActivity(AppCompatActivity compatActivity){
        AudioPlayer.getPlayer().stop();
        CacheService.getInstance().cleanBitmaps();
        UserManager.getManager().cleanUserCache();
        StickerManager.getManager().destroy();
        ChatListManager.getManager().destroy();
        Intent intent = new Intent(compatActivity, PhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        compatActivity.startActivity(intent);
        compatActivity.supportFinishAfterTransition();
        PassCodeManager passCodeManager = PassCodeManager.getManager();
        passCodeManager.setEnablePassCode(compatActivity, false, null);
    }

    public void setPhoneCode(String code) {
        TdApi.SetAuthCode func = new TdApi.SetAuthCode();
        func.code = code;
        client().send(func, this);
    }

    public void setUserName(String fn, String ln) {
        TdApi.SetAuthName func = new TdApi.SetAuthName();
        func.lastName = ln;
        func.firstName = fn;
        client().send(func, this);
    }

    private boolean isActivationCodeActivity() {
        return mActivity != null && mActivity.getClass().getSimpleName().equals(ActivationCodeActivity.class.getSimpleName());
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        sIsButtonBlocked = false;

        if (mProgressView != null)
            mProgressView.stop();

        switch (object.getConstructor()) {
            case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR: {
                sIsHaveAuth = true;
                if (mActivity != null && !sResetAction) {
                    Intent intent = new Intent(mActivity, PhoneNumberActivity.class);
                    mActivity.startActivity(intent);
                }
                break;
            }
            case TdApi.AuthStateWaitCode.CONSTRUCTOR: {
                sIsHaveAuth = true;
                if (mActivity != null) {
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(mActivity, ActivationCodeActivity.class);
                            intent.putExtra("phone", mPhoneNumber);
                            mActivity.startActivity(intent);
                            mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                            if(mActivity instanceof SetNameActivity){
                                mActivity.supportFinishAfterTransition();
                            }
                        }
                    });
                }
                break;
            }

            case TdApi.AuthStateWaitName.CONSTRUCTOR: {
                sIsHaveAuth = true;
                if (mActivity != null) {
                    if (isActivationCodeActivity()) {
                        ((ActivationCodeActivity) mActivity).unRegisterSmsMonitor();
                    }
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(mActivity, SetNameActivity.class);
                            mActivity.startActivity(intent);
                            mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            mActivity.supportFinishAfterTransition();
                        }
                    });
                }
                break;
            }
            case TdApi.AuthStateOk.CONSTRUCTOR: {
                if (mActivity != null) {
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivationCodeActivity()) {
                                ((ActivationCodeActivity) mActivity).unRegisterSmsMonitor();
                            }
                            Intent intent = new Intent(mActivity, ChatListActivity.class /*ChatListActivity.class*/ /*SettingActivity.class*/);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            if (!sIsHaveAuth) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            }
                            mActivity.startActivity(intent);
                            if (sIsHaveAuth) {
                                mActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                mActivity.supportFinishAfterTransition();
                            } else {
                                mActivity.overridePendingTransition(0, 0);
                            }
                        }
                    });
                }
                break;
            }
            case TdApi.Error.CONSTRUCTOR: {
                TdApi.Error error = (TdApi.Error) object;

                if (error.code == 400 && mActivity != null) {
                    if (error.text.equals("PHONE_CODE_INVALID")) {
                        final TextView errorView = (TextView) mActivity.findViewById(R.id.a_activation_code_error);
                        final EditText code = (EditText) mActivity.findViewById(R.id.a_activation_code_edit_text_code);
                        if (errorView != null && code != null) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    errorView.setVisibility(View.VISIBLE);
                                    errorView.setText(AndroidUtil.getResourceString(R.string.wrong_code));
                                    code.setBackgroundResource(R.drawable.edittext_bottom_line_error);
                                }
                            });
                        }
                    }

                    if (error.text.equals("PHONE_CODE_EMPTY")) {
                        final TextView errorView = (TextView) mActivity.findViewById(R.id.a_activation_code_error);
                        final EditText code = (EditText) mActivity.findViewById(R.id.a_activation_code_edit_text_code);
                        if (errorView != null && code != null) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    errorView.setVisibility(View.VISIBLE);
                                    errorView.setText(AndroidUtil.getResourceString(R.string.code_is_empty));
                                    code.setBackgroundResource(R.drawable.edittext_bottom_line_error);
                                }
                            });
                        }
                    }

                    if (error.text.equals("PHONE_NUMBER_INVALID")) {
                        final TextView errorView = (TextView) mActivity.findViewById(R.id.a_phone_number_error);
                        final EditText phone = (EditText) mActivity.findViewById(R.id.a_phone_number_phone);
                        if (errorView != null && phone != null) {
                            AndroidUtil.runInUI(new Runnable() {
                                @Override
                                public void run() {
                                    errorView.setVisibility(View.VISIBLE);
                                    phone.setBackgroundResource(R.drawable.edittext_bottom_line_error);
                                }
                            });
                        }
                    }
                }

                //UiHelper.showToastLong(error.code + "-" + error.text);
                break;
            }
        }
    }
}
