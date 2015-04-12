package com.bigbasket.mobileapp.activity.order.uiv3;

import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class OrderInvoiceActivity extends BBActivity {

    @Override
    public void onBackPressed() {
        goToHome(false);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.THANK_YOU_SCREEN;
    }
}
