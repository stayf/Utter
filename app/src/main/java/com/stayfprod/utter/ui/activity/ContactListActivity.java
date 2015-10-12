package com.stayfprod.utter.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.stayfprod.utter.App;
import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.ContactListManager;
import com.stayfprod.utter.model.NotificationObject;
import com.stayfprod.utter.util.AndroidUtil;

import java.util.Observable;
import java.util.Observer;

public class ContactListActivity extends AppCompatActivity implements Observer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_contact_list);

        ContactListManager contactListManager = ContactListManager.getManager();
        contactListManager.initRecycleView(this);
        ContactListManager.getManager().getContacts();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        ContactListManager contactListManager = ContactListManager.getManager();
        int action = contactListManager.getAction();
        contactListManager.clean();
        if (action == ContactListManager.ACTION_ADD_USER_TO_GROUP) {
            supportFinishAfterTransition();
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContactListManager.getManager().addObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ContactListManager.getManager().deleteObserver(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.a_contact_list_title);
            title.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
            title.setTextColor(0xffffffff);
            title.setTextSize(20);

            toolbar.setNavigationIcon(R.mipmap.ic_back);

            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof NotificationObject) {
            NotificationObject nObject = (NotificationObject) data;
            switch (nObject.getMessageCode()) {
                case NotificationObject.CLOSE_CONTACT_ACTIVITY: {
                    AndroidUtil.runInUI(new Runnable() {
                        @Override
                        public void run() {
                            ContactListManager.getManager().clean();
                            supportFinishAfterTransition();
                            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                        }
                    });
                    break;
                }
            }
        }
    }
}
