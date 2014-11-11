package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 16/7/14.
 */
public class MarketPlaceItems implements Parcelable { //implements Parcelable

    @SerializedName(Constants.IMAGE_URL)
    private String imageUrl;

    @SerializedName(Constants.P_DESC)
    private String desc;

    @SerializedName(Constants.SKU_ID)
    private String sku;

    @SerializedName(Constants.NO_ITEM_IN_CART)
    private int itemInCart;

    @SerializedName(Constants.QTY)
    private float totalQty;

    @SerializedName(Constants.SALE_PRICE)
    private float salePrice;

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
        totalQty = source.readFloat();
        sku = source.readString();
        salePrice = source.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(desc);
        dest.writeInt(itemInCart);
        dest.writeFloat(totalQty);
        dest.writeString(sku);
        dest.writeFloat(salePrice);

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


    public int getRuleValidationArrayIndex() {
        return ruleValidationArrayIndex;
    }

    public void setRuleValidationArrayIndex(int ruleValidationArrayIndex) {
        this.ruleValidationArrayIndex = ruleValidationArrayIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getItemInCart() {
        return itemInCart;
    }

    public void setItemInCart(int itemInCart) {
        this.itemInCart = itemInCart;
    }

    public float getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(float totalQty) {
        this.totalQty = totalQty;
    }
}
