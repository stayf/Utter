package com.stayfprod.utter.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.stayfprod.utter.R;
import com.stayfprod.utter.manager.IntermediateManager;
import com.stayfprod.utter.util.AndroidUtil;

public class IntermediateActivity extends AbstractActivity {

    public enum TypeList {
        USERS_ONLY,
        GROUPS_ONLY,
        ALL
    }

    public enum Action {
        ADD_BOT,
        FORWARD_MSGES,
        SHARED_CONTACT
    }

    private TypeList mTypeList;
    private Action mAction;
    private Integer mBotId;
    private Integer mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTypeList = (TypeList) bundle.get("typeList");
            mAction = (Action) bundle.get("action");
            mBotId = bundle.getInt("botId");
            mUserId = bundle.getInt("userId");
        }

        IntermediateManager intermediateManager = IntermediateManager.getManager();
        intermediateManager.initRecycleView(this, mTypeList, mAction, mBotId, mUserId);

        initToolbar();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        IntermediateManager.getManager().destroy();
    }

    public void forceFinish() {
        IntermediateManager.getManager().destroy();
        finish();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = findView(R.id.a_action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.t_title);

            switch (mTypeList) {
                case USERS_ONLY:
                    title.setText(AndroidUtil.getResourceString(R.string.select_user));
                    break;
                case GROUPS_ONLY:
                    title.setText(AndroidUtil.getResourceString(R.string.select_group));
                    break;
                case ALL:
                    title.setText(AndroidUtil.getResourceString(R.string.select_chat));
                    break;
            }

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

}
