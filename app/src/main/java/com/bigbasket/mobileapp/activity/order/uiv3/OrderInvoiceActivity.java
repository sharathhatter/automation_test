package com.bigbasket.mobileapp.activity.order.uiv3;

import android.view.MenuItem;

import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class OrderInvoiceActivity extends BackButtonActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(NavigationCodes.GO_TO_HOME);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(NavigationCodes.GO_TO_HOME);
        finish();
    }
}
