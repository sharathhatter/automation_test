package com.bigbasket.mobileapp.activity.base.uiv3;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.bigbasket.mobileapp.view.uiv3.FloatingBadgeCountView;

public class BackButtonActivity extends BBActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setViewBasketFloatingButton() {
        FloatingBadgeCountView btnViewBasket = getViewBasketFloatingButton();
        if (btnViewBasket != null) {
            btnViewBasket.setVisibility(View.GONE);
        }
    }

    @Override
    @LayoutRes
    public int getMainLayout() {
        return R.layout.uiv3_main_sub_content_layout;
    }

    @Override
    public void setNavDrawer() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void loadNavigationItems() {
        // Do nothing here
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        BBDrawerLayout drawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }
}
