package com.stayfprod.utter.manager;

import android.util.Log;

import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.util.FileUtils;
import com.stayfprod.utter.util.Logs;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class UserManager extends ResultController {
    private static final String LOG = "UserManagerLog";
    private static volatile UserManager userManager;
    private final ReentrantLock locker = new ReentrantLock();

    public void setCurrUserId(Integer currUserId) {
        this.currUserId = currUserId;
    }

    private volatile Integer currUserId;
    private ConcurrentHashMap<Integer, CachedUser> userList = new ConcurrentHashMap<Integer, CachedUser>(50);

    public Integer getCurrUserId() {
        if (currUserId == null) {
            return 0;
        }
        return currUserId;
    }

    public void cleanUserCache(){
        userList.clear();
    }

    //может быть такое что апдейт на фотку пришел раньше чем инфа о юзере(юзера просто может не быть в кеше)
    //в таком случае как только к нам придет инфа о юзере там уже будет обновленный файл
    public synchronized CachedUser insertUserInCache(TdApi.User user) {
        int id = user.id;
        CachedUser cachedUser = userList.get(id);

        if (cachedUser == null) {
            cachedUser = new CachedUser(user, false, new TdApi.BotInfoEmpty(),
                    TextUtil.createUserInitials(user),
                    TextUtil.createFullName(user));
            userList.put(id, cachedUser);
        } else {
            //если юзер есть, сранить хэши,если не совпадает, тогда обновляем все поля(именно поля а не ссылку)
            if (TextUtil.isBlank(cachedUser.fullName) || user.hashCode() != cachedUser.tgUser.hashCode()) {

                if (user.type.getConstructor() == TdApi.UserTypeDeleted.CONSTRUCTOR) {
                    cachedUser.fullName = "Deleted User";
                    cachedUser.initials = "DE";
                } else {
                    cachedUser.fullName = TextUtil.createFullName(user);
                    cachedUser.initials = TextUtil.createUserInitials(user);
                }

                cachedUser.tgUser.firstName = user.firstName;
                cachedUser.tgUser.foreignLink = user.foreignLink;
                cachedUser.tgUser.id = user.id;
                cachedUser.tgUser.lastName = user.lastName;
                cachedUser.tgUser.myLink = user.myLink;
                cachedUser.tgUser.phoneNumber = user.phoneNumber;
                cachedUser.tgUser.profilePhoto.big = user.profilePhoto.big;
                cachedUser.tgUser.profilePhoto.small = user.profilePhoto.small;
                cachedUser.tgUser.profilePhoto.id = user.profilePhoto.id;
                cachedUser.tgUser.status = user.status;
                cachedUser.tgUser.username = user.username;
                cachedUser.tgUser.type = user.type;
            }
        }
        return cachedUser;
    }


    public synchronized CachedUser insertUserInCache(TdApi.UserFull userFull) {
        int id = userFull.user.id;
        CachedUser cachedUser = userList.get(id);

        if (cachedUser == null) {
            cachedUser = new CachedUser(userFull.user, userFull.isBlocked, userFull.botInfo,
                    TextUtil.createUserInitials(userFull.user),
                    TextUtil.createFullName(userFull.user));
            userList.put(id, cachedUser);
        } else {
            if (userFull.user.hashCode() != cachedUser.tgUser.hashCode()) {
                insertUserInCache(userFull.user);
            }
            cachedUser.botInfo = userFull.botInfo;
            cachedUser.isBlocked = userFull.isBlocked;
            cachedUser.isHaveFullInfo = true;
        }
        return cachedUser;
    }

    private CachedUser createEmptyUser(Integer id) {
        TdApi.File fileEmptyPhotoBig = new TdApi.File(-1, "", -1, "");
        TdApi.File fileEmptyPhotoSmall = new TdApi.File(-1, "", -1, "");
        TdApi.ProfilePhoto profilePhoto = new TdApi.ProfilePhoto(-1, fileEmptyPhotoSmall, fileEmptyPhotoBig);
        TdApi.User user = new TdApi.User(id, "", "", "", "", null, profilePhoto, null, null, null);
        return new CachedUser(user, false, new TdApi.BotInfoEmpty(), "", "");
    }

    public CachedUser getUserByIdWithRequestAsync(Long id) {
        Integer i_id = id != null ? id.intValue() : null;
        return getUserByIdWithRequestAsync(i_id);
    }

    public CachedUser getUserByIdWithRequestAsync(Integer id) {
        if (id != null) {
            if (!userList.containsKey(id) || TextUtil.isBlank(userList.get(id).fullName)) {
                getUser(id);
                if (userList.containsKey(id)) {
                    return userList.get(id);
                }
                CachedUser emptyCachedUser = createEmptyUser(id);
                userList.put(id, emptyCachedUser);
                return emptyCachedUser;
            }
            return userList.get(id);
        }
        return null;
    }


    //todo затестить в случае когда человек находится в чате и офлайн, после приходит сообщение о добавлении новго юзера и сообщения от нового юзера!!!
    public CachedUser getUserByIdWithRequestSync(final Integer id) {
        if (id != null) {
            if (!userList.containsKey(id) || TextUtil.isBlank(userList.get(id).fullName)) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                ThreadService.runSingleTaskUser(new Runnable() {
                    @Override
                    public void run() {
                        getUser(id, new ResultController() {
                            @Override
                            public void afterResult(TdApi.TLObject object, int calledConstructor) {
                                switch (object.getConstructor()) {
                                    case TdApi.User.CONSTRUCTOR: {
                                        processGetUser(object, calledConstructor);
                                        break;
                                    }
                                }
                                countDownLatch.countDown();
                            }
                        });
                    }
                });

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    //
                }

                if (userList.containsKey(id)) {
                    return userList.get(id);
                } else {
                    CachedUser emptyCachedUser = createEmptyUser(id);
                    userList.put(id, emptyCachedUser);
                    return emptyCachedUser;
                }
            }
            return userList.get(id);
        }
        return null;
    }

    public CachedUser getUserByIdNoRequest(Integer id, TdApi.MessageContact messageContact) {
        if (id != null) {
            if (!userList.containsKey(id)) {
                CachedUser emptyCachedUser = createEmptyUser(id);
                emptyCachedUser.initials = TextUtil.createUserInitials(messageContact);
                emptyCachedUser.fullName = TextUtil.createFullName(messageContact);
                emptyCachedUser.tgUser.firstName = messageContact.firstName;
                emptyCachedUser.tgUser.lastName = messageContact.lastName;
                emptyCachedUser.tgUser.phoneNumber = messageContact.phoneNumber;
                userList.put(id, emptyCachedUser);
                return emptyCachedUser;
            }
            return userList.get(id);
        }
        return null;
    }

    public static boolean isEmptyUser(CachedUser cachedUser) {
        return cachedUser.tgUser.type == null;
    }

    /*Например, вместе с getDialogs он получается, если последнее сообщение в
    одном из самых свежих 50 диалогов отправлено пользователем.
    Если сейчас таких сообщений нет, видимо, поиожет только
    перелогиниться и отправить сообщение до перезапуска приложения.*/

    private void processGetUser(TdApi.TLObject object, int calledConstructor) {
        TdApi.User user = (TdApi.User) object;
        if (calledConstructor == TdApi.GetMe.CONSTRUCTOR) {
            currUserId = user.id;
            insertUserInCache(user);
            if (FileUtils.isTDFileEmpty(user.profilePhoto.small)) {
                TdApi.File fileEmpty = user.profilePhoto.small;
                if (fileEmpty.id > 0) {
                    FileManager.getManager().uploadFile(FileManager.TypeLoad.USER_IMAGE, new Object[]{user.id}, fileEmpty.id, -1, -1);
                }
            }
            userManager.notifyObservers(new NotificationObject(NotificationObject.USER_DATA_UPDATE, userList.get(currUserId)));
        } else {
            insertUserInCache(user);
            if (currUserId == user.id) {
                userManager.notifyObservers(new NotificationObject(NotificationObject.USER_DATA_UPDATE, userList.get(currUserId)));
            }
        }
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        //на getMe и getUser вернет User

        switch (object.getConstructor()) {

            case TdApi.User.CONSTRUCTOR: {
                processGetUser(object, calledConstructor);
                break;
            }
            case TdApi.UserFull.CONSTRUCTOR: {
                TdApi.UserFull userFull = (TdApi.UserFull) object;
                Log.d(LOG, "userFull=" + userFull.toString());
                break;
            }

            case TdApi.Contacts.CONSTRUCTOR: {
                TdApi.Contacts contacts = (TdApi.Contacts) object;
                for (int i = 0; i < contacts.users.length; i++) {
                    TdApi.User user = contacts.users[i];
                    insertUserInCache(user);
                }
                Log.d(LOG, "contacts=" + contacts.toString());
                break;
            }
        }
    }

    public static UserManager getManager() {
        if (userManager == null) {
            synchronized (UserManager.class) {
                if (userManager == null) {
                    userManager = new UserManager();
                }
            }
        }
        return userManager;
    }

    //@class UserType @description Allows to distinguish different kinds of users: general users, deleted users and bots

    //@description General user
    //userTypeGeneral = UserType;

    //@description Deleted user or deleted bot. All fields of type User will be empty in this case. None of active action can be performed with deleted user
    //userTypeDeleted = UserType;

    //@description Bot (see https://core.telegram.org/bots) @can_join_group_chats If true, bot can be invited to group chats
    //@can_read_all_group_chat_messages If true, bot can read all group chat messages and not only addressed to him. In private chats bot always can read all messages
    //userTypeBot can_join_group_chats:Bool can_read_all_group_chat_messages:Bool = UserType;

    //@description Existing user, but currently there is no any information about it except user_id. It can happens very-very rarely.
    //userTypeUnknown = UserType;

    //@description Deletes users from contacts list @user_ids Identifiers of users to be deleted
    public void deleteContacts(int[] userIds, ResultController resultController) {
        TdApi.DeleteContacts deleteContacts = new TdApi.DeleteContacts();
        deleteContacts.userIds = userIds;
        client().send(deleteContacts, resultController);
    }

    //@description Adds new contacts/edits existing contacts. Returns list of corresponding users in the same order as input contacts.
    //If contact doesn't registered in Telegram, user with id == 0 will be returned @input_contacts List of contacts to import/edit
    public void importContacts(TdApi.InputContact[] inputContacts, ResultController resultController) {
        TdApi.ImportContacts importContacts = new TdApi.ImportContacts();
        importContacts.inputContacts = inputContacts;
        client().send(importContacts, resultController);
    }

    @Override
    public boolean hasChanged() {
        return true;
    }

    //не подгружает из сети
    public UserManager getMe() {
        client().send(new TdApi.GetMe(), getManager());
        return userManager;
    }

    //не подгружает из сети
    public UserManager getUser(int userId) {
        TdApi.GetUser func = new TdApi.GetUser();
        func.userId = userId;
        client().send(func, getManager());
        return userManager;
    }

    public UserManager getUser(int userId, ResultController resultHandler) {
        TdApi.GetUser func = new TdApi.GetUser();
        func.userId = userId;
        client().send(func, resultHandler);
        return userManager;
    }

    //не понятно подгружает ли из сети
    public UserManager getUserFull(int userId) {
        TdApi.GetUserFull func = new TdApi.GetUserFull();
        func.userId = userId;
        client().send(func, getManager());
        return userManager;
    }


    public void blockUser(int userId, ResultController resultController) {
        TdApi.BlockUser blockUser = new TdApi.BlockUser();
        blockUser.userId = userId;
        client().send(blockUser, resultController);
    }

    public void unBlockUser(int userId, ResultController resultController) {
        TdApi.UnblockUser unblockUser = new TdApi.UnblockUser();
        unblockUser.userId = userId;
        client().send(unblockUser, resultController);
    }

    public void getUserFull(int userId, ResultController resultHandler) {
        TdApi.GetUserFull func = new TdApi.GetUserFull();
        func.userId = userId;
        client().send(func, resultHandler);
    }

    public UserManager getInfoMeIfNeed() {
        if (currUserId == null) {
            getMe();
        } else {
            if (!userList.containsKey(currUserId) || TextUtil.isBlank(userList.get(currUserId).initials)) {
                getUser(currUserId);
            }
        }
        return userManager;
    }

    //@description Changes first and last names of logged in user. If something changes, updateUser will be sent
    //@first_name New value of user first name, must not be empty @last_name New value of user last name
    public void changeCurrentUserName(String firstName, String lastName, final ResultController resultController) {
        TdApi.ChangeName changeName = new TdApi.ChangeName();
        changeName.firstName = firstName;
        changeName.lastName = lastName;
        client().send(changeName, resultController);
    }

    //@description Uploads new profile photo for logged in user. Photo will not change until change will be synchronized with server.
    // Photo will not be changed if application is killed before it can send request to server. If something changes, updateUser will be sent
    //@photo_path Path to new profile photo @crop Crop settings for photo. You can use null/default constructor to get automatic crop
    public void setProfilePhoto(String photoPath, ResultController resultController) {
        TdApi.SetProfilePhoto setProfilePhoto = new TdApi.SetProfilePhoto();
        setProfilePhoto.photoPath = photoPath;
        client().send(setProfilePhoto, resultController == null ? this : resultController);
    }

    public void deleteProfilePhoto(long id) {
        TdApi.DeleteProfilePhoto deleteProfilePhoto = new TdApi.DeleteProfilePhoto();
        deleteProfilePhoto.profilePhotoId = id;
        client().send(deleteProfilePhoto, new ResultController() {
            @Override
            public void afterResult(TdApi.TLObject object, int calledConstructor) {

            }
        });
    }

    public void setProfilePhoto(String photoPath) {
        setProfilePhoto(photoPath, null);
    }
}
