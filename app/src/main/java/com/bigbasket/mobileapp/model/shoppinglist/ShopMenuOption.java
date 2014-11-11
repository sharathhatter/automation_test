package com.bigbasket.mobileapp.model.shoppinglist;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ShopMenuOption {

    private String title;
    private String description;
    private String price;
    private int imageId;
    private String tag;

    public ShopMenuOption(String title, String description, String price,
                          int imageId, String tag) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageId = imageId;
        this.tag = tag;
    }

    public ShopMenuOption(String title, String description,
                          int imageId, String tag) {
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public static List<ShopMenuOption> getOfferMenuShopList() {
        List<ShopMenuOption> menuOptionList = new ArrayList<>();
        ShopMenuOption shopMenuOptionOffers = new ShopMenuOption("Discounts",
                "View all products with discounts", R.drawable.offers, Constants.STORE_OFFERS);
        menuOptionList.add(shopMenuOptionOffers);

        ShopMenuOption shopMenuOptionPromo = new ShopMenuOption("Promotions",
                "View all products with special <br /> promotions",
                R.drawable.promotion, Constants.PROMO);
        menuOptionList.add(shopMenuOptionPromo);
        return menuOptionList;
    }

    public static List<ShopMenuOption> getShopMenuOptionList(String btoken) {
        ShopMenuOption shopMenuOptionBrowseCat = new ShopMenuOption("Browse By Categories", null,
                R.drawable.categories, Constants.BROWSE_CAT);
        List<ShopMenuOption> menuOptionList = new ArrayList<>();
        menuOptionList.add(shopMenuOptionBrowseCat);

        ShopMenuOption shopMenuOptionOffers = new ShopMenuOption("Browse By Offers", null,
                R.drawable.offers, Constants.BROWSE_OFFERS);
        menuOptionList.add(shopMenuOptionOffers);

        if (btoken.length() != 0) {
            ShopMenuOption shopMenuOptionShoppingList = new ShopMenuOption("Shopping Lists",
                    null, R.drawable.shoppinglist, Constants.SHOP_LST);
            ShopMenuOption shopMenuOptionSmartBasket = new ShopMenuOption("Smart Basket",
                    null, R.drawable.smartbasket, Constants.SMART_BASKET_SLUG);
            menuOptionList.add(shopMenuOptionShoppingList);
            menuOptionList.add(shopMenuOptionSmartBasket);
        }

        ShopMenuOption shopMenuOptionQuickShop = new ShopMenuOption("Quick Shop",
                null, R.drawable.quickshop, Constants.QUICK_SHOP);
        menuOptionList.add(shopMenuOptionQuickShop);
        return menuOptionList;
    }
}
