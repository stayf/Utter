package com.stayfprod.utter.manager;


import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.Contact;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.ui.adapter.ContactListAdapter;
import com.stayfprod.utter.ui.view.ContactView;
import com.stayfprod.utter.ui.view.SimpleRecyclerView;
import com.stayfprod.utter.util.ChatHelper;
import com.stayfprod.utter.util.AndroidUtil;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ContactListManager extends ResultController {
    private final static String LOG = ChatManager.class.getSimpleName();

    private static volatile ContactListManager sContactListManager;

    public static final int ACTION_ADD_USER_TO_GROUP = 1;

    public static ContactListManager getManager() {
        if (sContactListManager == null) {
            synchronized (ChatListManager.class) {
                if (sContactListManager == null) {
                    sContactListManager = new ContactListManager();
                }
            }
        }
        return sContactListManager;
    }

    private TreeSet<Contact> mContactList = new TreeSet<Contact>();
    private List<Integer> mIgnoreUsers = new ArrayList<>();
    private ContactListAdapter mListAdapter;
    private int mAction;

    @Override
    public boolean hasChanged() {
        return true;
    }

    public int getAction() {
        return mAction;
    }

    public void setAction(int action) {
        this.mAction = action;
    }

    public List<Integer> getIgnoreUsers() {
        return mIgnoreUsers;
    }

    public void clean() {
        mContactList.clear();
        mListAdapter = null;
        mAction = 0;
    }

    public void closeActivity() {
        notifyObservers(new NotificationObject(NotificationObject.CLOSE_CONTACT_ACTIVITY, null));
    }

    /*
    * Теперь getContacts возвращает
    * ответ только после получения полного результата с сервера.
    * Проблем с ним больше не должно возникать.
    * */
    public ContactListManager getContacts() {
        if (mContactList.isEmpty()) {
            TdApi.GetContacts func = new TdApi.GetContacts();
            client().send(func, getManager());
        }
        return sContactListManager;
    }

    public void getChatInfo(int id, Client.ResultHandler handler) {
        TdApi.GetChat func = new TdApi.GetChat(id);
        client().send(func, handler);
    }

    public void notifySetDataChangedAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (mListAdapter != null) {
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initRecycleView(AppCompatActivity compatActivity) {
        SimpleRecyclerView recyclerView = (SimpleRecyclerView) compatActivity.findViewById(R.id.a_contact_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(compatActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        mListAdapter = new ContactListAdapter(compatActivity);
        recyclerView.setAdapter(mListAdapter);
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Contacts.CONSTRUCTOR: {
                UserManager userManager = UserManager.getManager();
                TdApi.Contacts contacts = (TdApi.Contacts) object;
                for (int i = 0; i < contacts.users.length; i++) {
                    TdApi.User user = contacts.users[i];
                    if (mAction == ACTION_ADD_USER_TO_GROUP && !mIgnoreUsers.isEmpty() && mIgnoreUsers.contains(user.id)) {
                        continue;
                    }
                    Contact contact = new Contact(userManager.insertUserInCache(user), ChatHelper.lastSeenUser(user.status));
                    ContactView.measure(contact);
                    mContactList.add(contact);
                }

                mListAdapter.setData(mContactList);

                notifySetDataChangedAsync();
                break;
            }
        }
    }

}
