package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class MarketPlaceItems implements Parcelable {

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
    public int describeContents() {
        return 0;
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

    public double getSalePrice() {
        return salePrice;
    }

    public String getSku() {
        return sku;
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

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public String getProductBrand() {
        return productBrand;
    }
}
