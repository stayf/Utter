package com.stayfprod.utter.manager;

import org.drinkless.td.libcore.telegram.TdApi;

public class OptionManager extends ResultController {

    public enum OptionType {
        broadcast_size_max,
        chat_big_size,
        connection_state,
        disabled_bigchat_create,
        disabled_bigchat_message,
        disabled_bigchat_upload_audio,
        disabled_bigchat_upload_document,
        disabled_bigchat_upload_photo,
        disabled_broadcast_create,
        disabled_chat_create,
        disabled_chat_message,
        disabled_chat_upload_audio,
        disabled_chat_upload_document,
        disabled_chat_upload_photo,
        disabled_bigfile_download,
        disabled_file_download,
        disabled_photo_download,
        disabled_pm_message,
        disabled_pm_upload_audio,
        disabled_pm_upload_document,
        disabled_pm_upload_photo,
        group_chat_size_max,
        forwarded_messages_count_max,//100
        my_id,
        network_unreachable(false),//info только это поле изменяемо
        test_mode;
        public Integer intVal;
        public String strVal;
        public Boolean boolVal;

        OptionType() {

        }

        OptionType(boolean val) {
            this.boolVal = val;
        }

        public Boolean getBoolVal() {
            return boolVal;
        }

        public void setBoolVal(Boolean boolVal) {
            this.boolVal = boolVal;
        }

        public int getIntVal() {
            return intVal;
        }

        public void setIntVal(int intVal) {
            this.intVal = intVal;
        }

        public String getStrVal() {
            return strVal;
        }

        public void setStrVal(String strVal) {
            this.strVal = strVal;
        }

        public Object getVal() {
            if (strVal != null) {
                return strVal;
            }
            if (intVal != null) {
                return intVal;
            }
            if (boolVal != null) {
                return boolVal;
            }
            return null;
        }
    }

    private static volatile OptionManager optionManager;

    public static OptionManager getManager() {
        if (optionManager == null) {
            synchronized (ChatListManager.class) {
                if (optionManager == null) {
                    optionManager = new OptionManager();
                }
            }
        }
        return optionManager;
    }

    public void setNetworkUnreachableOption(boolean value) {
        Boolean curVal = OptionType.network_unreachable.getBoolVal();
        if (curVal == null || curVal != value) {
            OptionType.network_unreachable.setBoolVal(value);
            setOption(OptionType.network_unreachable, new TdApi.OptionBoolean(value));
        }
    }

    public void setUpdateOption(TdApi.UpdateOption update) {
        switch (update.value.getConstructor()) {
            case TdApi.OptionBoolean.CONSTRUCTOR:
                TdApi.OptionBoolean optionBoolean = (TdApi.OptionBoolean) update.value;
                OptionType optionTypeBool = OptionType.valueOf(update.name);
                if (optionTypeBool != null) {
                    optionTypeBool.setBoolVal(optionBoolean.value);
                }
                break;
            case TdApi.OptionEmpty.CONSTRUCTOR:
                break;
            case TdApi.OptionInteger.CONSTRUCTOR:
                TdApi.OptionInteger optionInteger = (TdApi.OptionInteger) update.value;
                OptionType optionTypeInt = OptionType.valueOf(update.name);
                if (optionTypeInt != null) {
                    optionTypeInt.setIntVal(optionInteger.value);
                }
                break;
            case TdApi.OptionString.CONSTRUCTOR:
                TdApi.OptionString optionString = (TdApi.OptionString) update.value;
                OptionType optionTypeStr = OptionType.valueOf(update.name);
                if (optionTypeStr != null) {
                    optionTypeStr.setStrVal(optionString.value);
                }
                break;
        }
    }

    public void setOption(OptionType optionType, TdApi.OptionValue value) {
        TdApi.SetOption setOption = new TdApi.SetOption(optionType.name(), value);
        client().send(setOption, getManager());
    }

    public void getOption(final OptionType optionType) {
        TdApi.GetOption getOption = new TdApi.GetOption(optionType.name());
        client().send(getOption, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                switch (object.getConstructor()) {
                    case TdApi.OptionBoolean.CONSTRUCTOR:
                        TdApi.OptionBoolean optionBoolean = (TdApi.OptionBoolean) object;
                        optionType.setBoolVal(optionBoolean.value);
                        break;
                    case TdApi.OptionInteger.CONSTRUCTOR:
                        TdApi.OptionInteger optionInteger = (TdApi.OptionInteger) object;
                        optionType.setIntVal(optionInteger.value);
                        break;
                    case TdApi.OptionString.CONSTRUCTOR:
                        TdApi.OptionString optionString = (TdApi.OptionString) object;
                        optionType.setStrVal(optionString.value);
                        break;
                    case TdApi.OptionEmpty.CONSTRUCTOR:

                        break;
                }
            }
        });
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Ok.CONSTRUCTOR:
                //info Придет в ответ на setOption
                break;
        }
    }

    //info прочитать можно тут https://core.telegram.org/tdlib/options
    /*name=test_mode; value= false
    name=online_update_period_ms; value= 120000
    name=online_cloud_timeout_ms; value= 300000
    name=offline_idle_timeout_ms; value= 30000
    name=offline_blur_timeout_ms; value= 5000
    name=notify_default_delay_ms; value= 1500
    name=notify_cloud_delay_ms; value= 30000
    name=chat_size_max; value= 200
    name=chat_big_size; value= 10
    name=broadcast_size_max; value= 100*/
}
