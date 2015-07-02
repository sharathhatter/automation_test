package com.bigbasket.mobileapp.activity;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;

public class HomeActivity extends BBActivity {
    @Override
    public void syncBasket() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove(Constants.IS_BASKET_COUNT_DIRTY);
        editor.commit();
        new GetCartCountTask<>(this).startTask();
    }

}
