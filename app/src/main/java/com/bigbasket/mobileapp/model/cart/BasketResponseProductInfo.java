package com.bigbasket.mobileapp.model.cart;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BasketResponseProductInfo {

    @SerializedName(Constants.BASKET_CART_INNER_TOTAl_PRICE)
    private String totalPrice;

    @SerializedName(Constants.BASKET_CART_OUTER_TOTAl_SAVE_PRICE)
    private String totalSavings;

    @SerializedName(Constants.BASKET_CART_INNER_CART_COUNT)
    private int totalQty;

    @SerializedName(Constants.BASKET_CART_INNER_PRICE)
    private String unitPrice;

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getTotalSavings() {
        return totalSavings;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public String getUnitPrice() {
        return unitPrice;
    }
}
