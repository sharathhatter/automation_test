package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class CartItem extends BaseCartItem {
    public static final byte REGULAR_PRICE_AND_PROMO_NOT_APPLIED = 1;
    public static final byte PROMO_APPLIED_AND_PROMO_PRICING = 2;
    public static final byte PROMO_APPLIED_AND_MIXED_PRICING = 3;
    public static final byte REGULAR_PRICE_AND_NO_PROMO = 4;
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
    @SerializedName(Constants.PRODUCT_TOP_LEVEL_CATEGORY_NAME)
    private String topCategoryName;
    @SerializedName(Constants.PRODUCT_CATEGORY_NAME)
    private String productCategoryName;
    @SerializedName(Constants.IS_EXPRESS_AVAILABLE)
    private boolean isExpress;
    @SerializedName(Constants.PACKAGE_DESC)
    private String packDesc;
    @SerializedName(Constants.PRODUCT_WEIGHT)
    private String productWeight;
    @SerializedName(Constants.SKU_TYPE)
    private String skuType;
    @SerializedName(Constants.STORE_AVAILABILITY)
    private HashMap<String, String> storeAvailability;

    CartItem(Parcel source) {
        super(source);
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
        topCategoryName = source.readString();
        productCategoryName = source.readString();
        isExpress = source.readByte() == (byte) 1;
        packDesc = source.readString();
        productWeight = source.readString();
        skuType = source.readString();
        boolean isStoreAvailabilityNull = source.readByte() == (byte) 1;
        if (!isStoreAvailabilityNull) {
            String storeAvailabilityJson = source.readString();
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            storeAvailability = new Gson().fromJson(storeAvailabilityJson, type);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
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
        dest.writeString(topCategoryName);
        dest.writeString(productCategoryName);
        dest.writeByte(isExpress ? (byte) 1 : (byte) 0);
        dest.writeString(packDesc);
        dest.writeString(productWeight);
        dest.writeString(skuType);
        boolean isStoreAvailabilityNull = storeAvailability == null;
        dest.writeByte(isStoreAvailabilityNull ? (byte) 1 : (byte) 0);
        if (!isStoreAvailabilityNull) {
            dest.writeString(new Gson().toJson(storeAvailability));
        }
    }

    public String getAnnotationId() {
        return annotationId;
    }

    public String getFulfillmentId() {
        return fulfillmentId;
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

    @Nullable
    public HashMap<String, String> getStoreAvailability() {
        return storeAvailability;
    }

    public CartItemPromoInfo getCartItemPromoInfo() {
        return cartItemPromoInfo;
    }

    public String getTopCategoryName() {
        return topCategoryName;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public boolean isExpress() {
        return isExpress;
    }

    public String getPackDesc() {
        return packDesc;
    }

    public String getProductWeight() {
        return productWeight;
    }

    public String getSkuType() {
        return skuType;
    }
}
