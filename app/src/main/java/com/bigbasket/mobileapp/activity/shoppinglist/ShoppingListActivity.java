package com.bigbasket.mobileapp.activity.shoppinglist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;

public class ShoppingListActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(ShoppingListFragment.class.getName());
            if (fragment != null) {
                ((ShoppingListNamesAware) fragment).createNewShoppingList();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
