package com.bigbasket.mobileapp.model.navigation;

public abstract class BaseNavigationItem {
    private String itemName;
    private int drawableId;
    private String tag;
    private String version;

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

    protected BaseNavigationItem(String itemName, String tag, String version) {
        this.itemName = itemName;
        this.tag = tag;
        this.version = version;
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

    public String getVersion() {
        return version;
    }
}
