package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;

import java.util.ArrayList;

public interface ShoppingListNamesAware {
    ArrayList<ShoppingListName> getShoppingListNames();

    void setShoppingListNames(ArrayList<ShoppingListName> shoppingListNames);

    String getSelectedProductId();

    void setSelectedProductId(String selectedProductId);

    void postShoppingListItemDeleteOperation();
}