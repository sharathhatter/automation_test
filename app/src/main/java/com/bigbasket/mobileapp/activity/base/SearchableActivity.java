package com.bigbasket.mobileapp.activity.base;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

/**
 * Created by jugal on 9/3/15.
 */
public class SearchableActivity extends Activity implements SearchView.OnQueryTextListener{

    private ArrayAdapter<String> adapter;
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.search_list);
//        ListView lv = (ListView) findViewById(R.id.searchList);
//        SearchView search_view = (SearchView) findViewById(R.id.search_view);
//        adapter = new ArrayAdapter<String>(getApplicationContext(),
//                R.layout.list_items, R.id.txtTerm, getContentResolver().get);
//        renderSearchView();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


//    @Override
//    protected void setOptionsMenu(Menu menu) {
//        BBDrawerLayout drawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawerLayout != null) {
//            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//        }
//        MenuInflater menuInflater = getMenuInflater();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            menuInflater.inflate(R.menu.search_menu, menu);
//            final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
//            searchMenuItem.getActionView().setActivated(true);
//            //onSearchRequested();
//            final SearchView searchView = (SearchView) searchMenuItem.getActionView();
//            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//            SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
//            searchView.setSearchableInfo(searchableInfo);
//        } else {
//            menuInflater.inflate(R.menu.action_menu_pre_honeycomb, menu);
//        }
//    }

    private void renderSearchView() {
    }

}
