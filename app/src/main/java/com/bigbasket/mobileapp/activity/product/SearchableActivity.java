package com.bigbasket.mobileapp.activity.product;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;


public class SearchableActivity extends BBActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_search_layout;
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            menuInflater.inflate(R.menu.search_menu, menu);

            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();

            // Setting the search listener
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                private boolean extended = false;

                @Override
                public void onClick(View v) {
                    if (!extended) {
                        extended = true;
                        ViewGroup.LayoutParams lp = v.getLayoutParams();
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                }
            });
        } else {
            menuInflater.inflate(R.menu.search_menu_pre_honeycomb, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_apply_filter && getDrawerLayout() != null) {
            BBDrawerLayout drawerLayout = getDrawerLayout();
            drawerLayout.closeDrawer(Gravity.LEFT);
            drawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}