package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;

import java.util.ArrayList;
import java.util.List;

public interface ShoppingListNamesAware {
    void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames);

    String getSelectedProductId();

    void setSelectedProductId(String selectedProductId);

    void postShoppingListItemDeleteOperation();

    void addToShoppingList(List<ShoppingListName> selectedShoppingListNames);

    void postAddToShoppingListOperation();

    void createNewShoppingList();

    void onNewShoppingListCreated(String listName);
}