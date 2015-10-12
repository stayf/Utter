package com.stayfprod.utter.manager;

import android.util.Log;

import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.service.ThreadService;
import com.stayfprod.utter.util.FileUtil;
import com.stayfprod.utter.util.TextUtil;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class UserManager extends ResultController {
    private static final String LOG = "UserManagerLog";
    private static volatile UserManager sUserManager;

    private volatile Integer mCurrUserId;
    private ConcurrentHashMap<Integer, CachedUser> mUserList = new ConcurrentHashMap<Integer, CachedUser>(50);

    public void setCurrUserId(Integer currUserId) {
        this.mCurrUserId = currUserId;
    }

    public Integer getCurrUserId() {
        if (mCurrUserId == null) {
            return 0;
        }
        return mCurrUserId;
    }

    public void cleanUserCache(){
        mUserList.clear();
    }

    //может быть такое что апдейт на фотку пришел раньше чем инфа о юзере(юзера просто может не быть в кеше)
    //в таком случае как только к нам придет инфа о юзере там уже будет обновленный файл
    public synchronized CachedUser insertUserInCache(TdApi.User user) {
        int id = user.id;
        CachedUser cachedUser = mUserList.get(id);

        if (cachedUser == null) {
            cachedUser = new CachedUser(user, false, new TdApi.BotInfoEmpty(),
                    TextUtil.createUserInitials(user),
                    TextUtil.createFullName(user));
            mUserList.put(id, cachedUser);
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
        CachedUser cachedUser = mUserList.get(id);

        if (cachedUser == null) {
            cachedUser = new CachedUser(userFull.user, userFull.isBlocked, userFull.botInfo,
                    TextUtil.createUserInitials(userFull.user),
                    TextUtil.createFullName(userFull.user));
            mUserList.put(id, cachedUser);
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
            if (!mUserList.containsKey(id) || TextUtil.isBlank(mUserList.get(id).fullName)) {
                getUser(id);
                if (mUserList.containsKey(id)) {
                    return mUserList.get(id);
                }
                CachedUser emptyCachedUser = createEmptyUser(id);
                mUserList.put(id, emptyCachedUser);
                return emptyCachedUser;
            }
            return mUserList.get(id);
        }
        return null;
    }

    //todo затестить в случае когда человек находится в чате и офлайн, после приходит сообщение о добавлении новго юзера и сообщения от нового юзера!!!
    public CachedUser getUserByIdWithRequestSync(final Integer id) {
        if (id != null) {
            if (!mUserList.containsKey(id) || TextUtil.isBlank(mUserList.get(id).fullName)) {
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

                if (mUserList.containsKey(id)) {
                    return mUserList.get(id);
                } else {
                    CachedUser emptyCachedUser = createEmptyUser(id);
                    mUserList.put(id, emptyCachedUser);
                    return emptyCachedUser;
                }
            }
            return mUserList.get(id);
        }
        return null;
    }

    public CachedUser getUserByIdNoRequest(Integer id, TdApi.MessageContact messageContact) {
        if (id != null) {
            if (!mUserList.containsKey(id)) {
                CachedUser emptyCachedUser = createEmptyUser(id);
                emptyCachedUser.initials = TextUtil.createUserInitials(messageContact);
                emptyCachedUser.fullName = TextUtil.createFullName(messageContact);
                emptyCachedUser.tgUser.firstName = messageContact.firstName;
                emptyCachedUser.tgUser.lastName = messageContact.lastName;
                emptyCachedUser.tgUser.phoneNumber = messageContact.phoneNumber;
                mUserList.put(id, emptyCachedUser);
                return emptyCachedUser;
            }
            return mUserList.get(id);
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
            mCurrUserId = user.id;
            insertUserInCache(user);
            if (FileUtil.isTDFileEmpty(user.profilePhoto.small)) {
                TdApi.File fileEmpty = user.profilePhoto.small;
                if (fileEmpty.id > 0) {
                    FileManager.getManager().uploadFile(FileManager.TypeLoad.USER_IMAGE, new Object[]{user.id}, fileEmpty.id, -1, -1);
                }
            }
            sUserManager.notifyObservers(new NotificationObject(NotificationObject.USER_DATA_UPDATE, mUserList.get(mCurrUserId)));
        } else {
            insertUserInCache(user);
            if (mCurrUserId == user.id) {
                sUserManager.notifyObservers(new NotificationObject(NotificationObject.USER_DATA_UPDATE, mUserList.get(mCurrUserId)));
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
        if (sUserManager == null) {
            synchronized (UserManager.class) {
                if (sUserManager == null) {
                    sUserManager = new UserManager();
                }
            }
        }
        return sUserManager;
    }

    public void deleteContacts(int[] userIds, ResultController resultController) {
        TdApi.DeleteContacts deleteContacts = new TdApi.DeleteContacts();
        deleteContacts.userIds = userIds;
        client().send(deleteContacts, resultController);
    }

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
        return sUserManager;
    }

    //не подгружает из сети
    public UserManager getUser(int userId) {
        TdApi.GetUser func = new TdApi.GetUser();
        func.userId = userId;
        client().send(func, getManager());
        return sUserManager;
    }

    public UserManager getUser(int userId, ResultController resultHandler) {
        TdApi.GetUser func = new TdApi.GetUser();
        func.userId = userId;
        client().send(func, resultHandler);
        return sUserManager;
    }

    //не понятно подгружает ли из сети
    public UserManager getUserFull(int userId) {
        TdApi.GetUserFull func = new TdApi.GetUserFull();
        func.userId = userId;
        client().send(func, getManager());
        return sUserManager;
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
        if (mCurrUserId == null) {
            getMe();
        } else {
            if (!mUserList.containsKey(mCurrUserId) || TextUtil.isBlank(mUserList.get(mCurrUserId).initials)) {
                getUser(mCurrUserId);
            }
        }
        return sUserManager;
    }

    public void changeCurrentUserName(String firstName, String lastName, final ResultController resultController) {
        TdApi.ChangeName changeName = new TdApi.ChangeName();
        changeName.firstName = firstName;
        changeName.lastName = lastName;
        client().send(changeName, resultController);
    }

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
