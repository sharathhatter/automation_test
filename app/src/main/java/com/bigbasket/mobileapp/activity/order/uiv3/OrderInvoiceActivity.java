package com.bigbasket.mobileapp.activity.order.uiv3;

import android.view.MenuItem;

import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class OrderInvoiceActivity extends BackButtonActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToHome();
    }

    @Override
    public String getScreenTag(){
        return TrackEventkeys.THANK_YOU_SCREEN;
    }
}
