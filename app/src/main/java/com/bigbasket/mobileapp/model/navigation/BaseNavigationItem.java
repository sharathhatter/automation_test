package com.bigbasket.mobileapp.model.navigation;

public abstract class BaseNavigationItem {
    private String itemName;
    private int drawableId;
    private String tag;

    public BaseNavigationItem(String itemName, String tag) {
        this.itemName = itemName;
        this.tag = tag;
        this.drawableId = -1;
    }

    public BaseNavigationItem(String itemName, int drawableId, String tag) {
        this.itemName = itemName;
        this.drawableId = drawableId;
        this.tag = tag;
    }

    public String getItemName() {
        return itemName;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public String getTag() {
        return tag;
    }
}
