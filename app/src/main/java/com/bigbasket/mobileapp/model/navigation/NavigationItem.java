package com.bigbasket.mobileapp.model.navigation;

import java.util.ArrayList;

public class NavigationItem extends BaseNavigationItem {

    private boolean isExpandable;
    private ArrayList<NavigationSubItem> navigationSubItems;

    public NavigationItem(String itemName, int drawableId, String tag, boolean isExpandable) {
        super(itemName, drawableId, tag);
        this.isExpandable = isExpandable;
    }

    public NavigationItem(String itemName, int drawableId, String tag, boolean isExpandable,
                          ArrayList<NavigationSubItem> navigationSubItems) {
        this(itemName, drawableId, tag, isExpandable);
        this.navigationSubItems = navigationSubItems;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public ArrayList<NavigationSubItem> getNavigationSubItems() {
        return navigationSubItems;
    }
}
