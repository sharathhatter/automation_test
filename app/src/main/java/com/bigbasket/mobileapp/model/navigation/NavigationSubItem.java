package com.bigbasket.mobileapp.model.navigation;

public class NavigationSubItem extends BaseNavigationItem {

    private boolean isLoading;
    private String imageUrl;

    public NavigationSubItem(String itemName, int drawableId, String tag, boolean isLoading) {
        super(itemName, drawableId, tag);
        this.isLoading = isLoading;
    }

    public NavigationSubItem(String itemName, String imageUrl, String tag, boolean isLoading) {
        super(itemName, tag);
        this.isLoading = isLoading;
        this.imageUrl = imageUrl;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
