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

    private int ruleValidationArrayIndex;
    private int itemIndex;

    public MarketPlaceItems(String imageUrl, String desc, int itemInCart, float totalQty, String sku, float salePrice) {
        this.imageUrl = imageUrl;
        this.desc = desc;
        this.itemInCart = itemInCart;
        this.totalQty = totalQty;
        this.sku = sku;
        this.salePrice = salePrice;
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
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(desc);
        dest.writeInt(itemInCart);
        dest.writeDouble(totalQty);
        dest.writeString(sku);
        dest.writeDouble(salePrice);

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
}
