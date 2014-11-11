package com.bigbasket.mobileapp.activity.base.uiv3;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

public class BackButtonActivity extends BBActivity {


    @Override
    public void setNavDrawer(Toolbar toolbar, Bundle savedInstanceState) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        BBDrawerLayout drawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
}
