package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CartItem extends BaseCartItem {
    public static final byte REGULAR_PRICE_AND_PROMO_NOT_APPLIED = 1;
    public static final byte PROMO_APPLIED_AND_PROMO_PRICING = 2;
    public static final byte PROMO_APPLIED_AND_MIXED_PRICING = 3;
    public static final byte REGULAR_PRICE_AND_NO_PROMO = 4;

    @SerializedName(Constants.PRODUCT_ID)
    private int skuId;

    @SerializedName(Constants.TOTAL_QTY)
    private double totalQty;

    @SerializedName(Constants.PRODUCT_DESC)
    private String productDesc;

    @SerializedName(Constants.PRODUCT_BRAND)
    private String productBrand;

    @SerializedName(Constants.IMAGE_URL)
    private String productImgUrl;

    @SerializedName(Constants.PROMO_APPLIED_TYPE)
    private byte promoAppliedType;

    @SerializedName(Constants.PROMO_INFO)
    private CartItemPromoInfo cartItemPromoInfo;

    @SerializedName(Constants.FULFILLMENT_ID)
    private String fulfillmentId;

    @SerializedName(Constants.ANNOTATION_ID)
    private String annotationId;

    private int index;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(skuId);
        dest.writeDouble(totalQty);
        dest.writeString(productDesc);
        dest.writeString(productBrand);
        dest.writeString(productImgUrl);
        dest.writeByte(promoAppliedType);
        boolean isCartItemPromoInfoNull = cartItemPromoInfo == null;
        dest.writeByte(isCartItemPromoInfoNull ? (byte) 1 : (byte) 0);
        if (!isCartItemPromoInfoNull) {
            dest.writeParcelable(cartItemPromoInfo, flags);
        }
        dest.writeString(fulfillmentId);
        dest.writeString(annotationId);
    }

    CartItem(Parcel source) {
        skuId = source.readInt();
        totalQty = source.readDouble();
        productDesc = source.readString();
        productBrand = source.readString();
        productImgUrl = source.readString();
        promoAppliedType = source.readByte();
        boolean isCartItemPromoInfoNull = source.readByte() == (byte) 1;
        if (!isCartItemPromoInfoNull) {
            cartItemPromoInfo = source.readParcelable(CartItem.class.getClassLoader());
        }
        fulfillmentId = source.readString();
        annotationId = source.readString();
    }

    public static final Parcelable.Creator<CartItem> CREATOR = new Parcelable.Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel source) {
            return new CartItem(source);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public String getAnnotationId() {
        return annotationId;
    }

    public String getFulfillmentId() {
        return fulfillmentId;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSkuId() {
        return skuId;
    }

    public double getTotalQty() {
        return totalQty;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public String getProductImgUrl() {
        return productImgUrl;
    }

    public byte getPromoAppliedType() {
        return promoAppliedType;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public double getTotalPrice() {
        return getSalePrice() * this.totalQty;
    }

    public CartItemPromoInfo getCartItemPromoInfo() {
        return cartItemPromoInfo;
    }
}
