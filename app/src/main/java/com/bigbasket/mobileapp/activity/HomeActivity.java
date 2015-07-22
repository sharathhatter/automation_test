package com.bigbasket.mobileapp.activity;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class HomeActivity extends BBActivity {
    // This activity is for MoEngage, so that this activity can be targeted via InApp Notifications

    @Override
    public void startFragment() {
        int fragmentCode = getIntent().getIntExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        startFragment(fragmentCode);
    }

    @Override
    public void onResume(){
        super.onResume();
        setNextScreenNavigationContext(TrackEventkeys.HOME);
    }
}
