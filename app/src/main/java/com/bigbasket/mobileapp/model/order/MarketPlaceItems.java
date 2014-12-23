package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class MarketPlaceItems implements Parcelable {

    @SerializedName(Constants.IMAGE_URL)
    private String imageUrl;

    @SerializedName(Constants.P_DESC)
    private String desc;

    @SerializedName(Constants.SKU_ID)
    private String sku;

    @SerializedName(Constants.NO_ITEM_IN_CART)
    private int itemInCart;

    @SerializedName(Constants.QTY)
    private double totalQty;

    @SerializedName(Constants.SALE_PRICE)
    private double salePrice;

    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_NAME)
    private String topLevelCategoryName;

    @SerializedName(Constants.PRODUCT_CATEGORY_NAME)
    private String productCategoryName;

    @SerializedName(Constants.PRODUCT_BRAND)
    private String productBrand;

    private int ruleValidationArrayIndex;
    private int itemIndex;

    public MarketPlaceItems(String imageUrl, String desc, int itemInCart,
                            float totalQty, String sku, float salePrice, String topLevelCategoryName,
                            String productCategoryName, String productBrand) {
        this.imageUrl = imageUrl;
        this.desc = desc;
        this.itemInCart = itemInCart;
        this.totalQty = totalQty;
        this.sku = sku;
        this.salePrice = salePrice;
        this.topLevelCategoryName = topLevelCategoryName;
        this.productCategoryName = productCategoryName;
        this.productBrand = productBrand;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public MarketPlaceItems(Parcel source) {
        imageUrl = source.readString();
        desc = source.readString();
        itemInCart = source.readInt();
        totalQty = source.readDouble();
        sku = source.readString();
        salePrice = source.readDouble();
        topLevelCategoryName = source.readString();
        productCategoryName = source.readString();
        productBrand = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(desc);
        dest.writeInt(itemInCart);
        dest.writeDouble(totalQty);
        dest.writeString(sku);
        dest.writeDouble(salePrice);
        dest.writeString(topLevelCategoryName);
        dest.writeString(productCategoryName);
        dest.writeString(productBrand);
    }

    public static final Parcelable.Creator<MarketPlaceItems> CREATOR = new Parcelable.Creator<MarketPlaceItems>() {
        @Override
        public MarketPlaceItems createFromParcel(Parcel source) {
            return new MarketPlaceItems(source);
        }

        @Override
        public MarketPlaceItems[] newArray(int size) {
            return new MarketPlaceItems[size];
        }
    };

    public void setRuleValidationArrayIndex(int ruleValidationArrayIndex) {
        this.ruleValidationArrayIndex = ruleValidationArrayIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public int getRuleValidationArrayIndex() {
        return ruleValidationArrayIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public String getSku() {
        return sku;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public int getItemInCart() {
        return itemInCart;
    }

    public double getTotalQty() {
        return totalQty;
    }

    public String getTopLevelCategoryName() {
        return topLevelCategoryName;
    }

    public void setTopLevelCategoryName(String topLevelCategoryName) {
        this.topLevelCategoryName = topLevelCategoryName;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }
}
