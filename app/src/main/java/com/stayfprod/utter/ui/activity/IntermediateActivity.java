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

    private TextView title;
    private TypeList typeList;
    private Action action;
    private Integer botId;
    private Integer userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            typeList = (TypeList) bundle.get("typeList");
            action = (Action) bundle.get("action");
            botId = bundle.getInt("botId");
            userId = bundle.getInt("userId");
        }

        IntermediateManager intermediateManager = IntermediateManager.getManager();
        intermediateManager.initRecycleView(this, typeList, action, botId, userId);

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
        Toolbar toolbar = findView(R.id.a_actionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            title = (TextView) toolbar.findViewById(R.id.t_title);

            switch (typeList) {
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
