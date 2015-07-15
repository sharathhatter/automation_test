package com.bigbasket.mobileapp.interfaces;

import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;

import java.util.ArrayList;

public interface LaunchProductListAware {
    void launchProductList(ArrayList<NameValuePair> nameValuePairs,
                           @Nullable String sectionName, @Nullable String sectionItemName);
    void launchShoppingList(ShoppingListName shoppingListName);
}
