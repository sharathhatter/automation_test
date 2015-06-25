package com.bigbasket.mobileapp.activity;

import android.content.Intent;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class HomeActivity extends BBActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            if (data != null && data.getBooleanExtra(Constants.RELOAD_APP, false)) {
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                startFragment(FragmentCodes.START_HOME);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
