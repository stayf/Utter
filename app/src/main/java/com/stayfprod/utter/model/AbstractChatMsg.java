package com.stayfprod.utter.model;

import com.stayfprod.utter.model.chat.AudioMsg;
import com.stayfprod.utter.model.chat.ChangeIconTitleMsg;
import com.stayfprod.utter.model.chat.ContactMsg;
import com.stayfprod.utter.model.chat.VoiceMsg;
import com.stayfprod.utter.model.chat.DocumentMsg;
import com.stayfprod.utter.model.chat.GeoMsg;
import com.stayfprod.utter.model.chat.PhotoMsg;
import com.stayfprod.utter.model.chat.StickerMsg;
import com.stayfprod.utter.model.chat.SystemMsg;
import com.stayfprod.utter.model.chat.TextMsg;
import com.stayfprod.utter.model.chat.VideoMsg;

import org.drinkless.td.libcore.telegram.TdApi;

public abstract class AbstractChatMsg {

    public enum Type {
        SYSTEM_MSG(0),
        DATE_DIVIDER(1),
        NEW_MSG_DIVIDER(2),
        MSG_BOT_DESCRIPTION(3),
        SYSTEM_CHANGE_TITLE_MSG(4),

        MSG_TEXT(10),
        MSG_VOICE(11),
        MSG_DOCUMENT(12),
        MSG_STICKER(13),
        MSG_PHOTO(14),
        MSG_VIDEO(15),
        MSG_GEO(16),
        MSG_AUDIO(17),
        MSG_CONTACT(18),

        MSG_FORWARD_TEXT(30),
        MSG_FORWARD_VOICE(31),
        MSG_FORWARD_DOCUMENT(32),
        MSG_FORWARD_STICKER(33),
        MSG_FORWARD_PHOTO(34),
        MSG_FORWARD_VIDEO(35),
        MSG_FORWARD_GEO(36),
        MSG_FORWARD_AUDIO(37),
        MSG_CONTACT_FORWARD(38);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean isUserMsg(Type type) {
            return type.getValue() >= 10 && type.getValue() <= 50;
        }

        public static Type fromInteger(int x) {
            switch (x) {
                case 0:
                    return SYSTEM_MSG;
                case 1:
                    return DATE_DIVIDER;
                case 2:
                    return NEW_MSG_DIVIDER;
                case 3:
                    return MSG_BOT_DESCRIPTION;
                case 4:
                    return SYSTEM_CHANGE_TITLE_MSG;

                case 10:
                    return MSG_TEXT;
                case 11:
                    return MSG_VOICE;
                case 12:
                    return MSG_DOCUMENT;
                case 13:
                    return MSG_STICKER;
                case 14:
                    return MSG_PHOTO;
                case 15:
                    return MSG_VIDEO;
                case 16:
                    return MSG_GEO;
                case 17:
                    return MSG_AUDIO;
                case 18:
                    return MSG_CONTACT;

                case 30:
                    return MSG_FORWARD_TEXT;
                case 31:
                    return MSG_FORWARD_VOICE;
                case 32:
                    return MSG_FORWARD_DOCUMENT;
                case 33:
                    return MSG_FORWARD_STICKER;
                case 34:
                    return MSG_FORWARD_PHOTO;
                case 35:
                    return MSG_FORWARD_VIDEO;
                case 36:
                    return MSG_FORWARD_GEO;
                case 37:
                    return MSG_FORWARD_AUDIO;
                case 38:
                    return MSG_CONTACT_FORWARD;

            }
            return null;
        }
    }

    //тип сообщения
    public Type type = Type.MSG_TEXT;
    public TdApi.Message tgMessage;

    public static AbstractChatMsg createEntry(TdApi.Message message) {
        switch (message.message.getConstructor()) {
            case TdApi.MessageText.CONSTRUCTOR: {
                return new TextMsg();
            }
            case TdApi.MessageVoice.CONSTRUCTOR: {
                return new VoiceMsg();
            }
            case TdApi.MessageVenue.CONSTRUCTOR: {
                return new GeoMsg();
            }
            case TdApi.MessageAudio.CONSTRUCTOR: {
                return new AudioMsg();
            }
            case TdApi.MessageDocument.CONSTRUCTOR: {
                return new DocumentMsg();
            }
            case TdApi.MessageSticker.CONSTRUCTOR: {
                return new StickerMsg();
            }
            case TdApi.MessagePhoto.CONSTRUCTOR: {
                return new PhotoMsg();
            }
            case TdApi.MessageVideo.CONSTRUCTOR: {
                return new VideoMsg();
            }
            case TdApi.MessageLocation.CONSTRUCTOR: {
                return new GeoMsg();
            }
            case TdApi.MessageContact.CONSTRUCTOR: {
                return new ContactMsg();
            }
            case TdApi.MessageGroupChatCreate.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageChatChangeTitle.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageChatDeletePhoto.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageChatAddParticipant.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageChatDeleteParticipant.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageDeleted.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageUnsupported.CONSTRUCTOR: {
                return new SystemMsg();
            }
            case TdApi.MessageChatChangePhoto.CONSTRUCTOR: {
                return new ChangeIconTitleMsg();
            }
            case TdApi.MessageChatJoinByLink.CONSTRUCTOR: {
                return new SystemMsg();
            }

            //info BotDescription тут нет
        }
        return new TextMsg();
    }

}
