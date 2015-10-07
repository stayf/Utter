package com.stayfprod.utter.manager;


import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.stayfprod.utter.R;
import com.stayfprod.utter.model.CachedUser;
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

    private static volatile ContactListManager contactListManager;

    public static final int ACTION_ADD_USER_TO_GROUP = 1;

    private TreeSet<Contact> contactList = new TreeSet<Contact>();
    private List<Integer> ignoreUsers = new ArrayList<>();
    private ContactListAdapter listAdapter;
    private int action;

    public static ContactListManager getManager() {
        if (contactListManager == null) {
            synchronized (ChatListManager.class) {
                if (contactListManager == null) {
                    contactListManager = new ContactListManager();
                }
            }
        }
        return contactListManager;
    }


    @Override
    public boolean hasChanged() {
        return true;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public List<Integer> getIgnoreUsers() {
        return ignoreUsers;
    }

    public void clean() {
        contactList.clear();
        listAdapter = null;
        action = 0;
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
        if (contactList.isEmpty()) {
            TdApi.GetContacts func = new TdApi.GetContacts();
            client().send(func, getManager());
        }
        return contactListManager;
    }

    public void getChatInfo(int id, Client.ResultHandler handler) {
        TdApi.GetChat func = new TdApi.GetChat(id);
        client().send(func, handler);
    }

    public void notifySetDataChangedAsync() {
        AndroidUtil.runInUI(new Runnable() {
            @Override
            public void run() {
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initRecycleView(AppCompatActivity compatActivity) {
        SimpleRecyclerView recyclerView = (SimpleRecyclerView) compatActivity.findViewById(R.id.a_contact_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(compatActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        listAdapter = new ContactListAdapter(compatActivity);
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void afterResult(TdApi.TLObject object, int calledConstructor) {
        switch (object.getConstructor()) {
            case TdApi.Contacts.CONSTRUCTOR: {
                UserManager userManager = UserManager.getManager();
                TdApi.Contacts contacts = (TdApi.Contacts) object;
                for (int i = 0; i < contacts.users.length; i++) {
                    TdApi.User user = contacts.users[i];
                    if ( action == ACTION_ADD_USER_TO_GROUP && !ignoreUsers.isEmpty() && ignoreUsers.contains(user.id)) {
                        continue;
                    }
                    Contact contact = new Contact(userManager.insertUserInCache(user), ChatHelper.lastSeenUser(user.status));
                    ContactView.measure(contact);
                    contactList.add(contact);
                }

                listAdapter.setData(contactList);

                notifySetDataChangedAsync();
                break;
            }
        }
    }


}
