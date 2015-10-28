package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductTabData implements Parcelable {


    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    @SerializedName(Constants.CONTENT_SECTION)
    private SectionData contentSectionData;

    @SerializedName(Constants.CART_INFO)
    private HashMap<String, Integer> cartInfo;

    @SerializedName(Constants.TAB_INFO)
    private ArrayList<ProductTabInfo> productTabInfos;

    @SerializedName(Constants.SCREEN_NAME)
    private String screenName;

    @SuppressWarnings("unchecked")
    public ProductTabData(Parcel source) {

        boolean isBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!isBaseImgUrlNull) {
            baseImgUrl = source.readString();
        }
        boolean isContentSectionNull = source.readByte() == (byte) 1;
        if (!isContentSectionNull) {
            contentSectionData = source.readParcelable(ProductTabData.class.getClassLoader());
        }
        boolean isCartInfoNull = source.readByte() == (byte) 1;
        if (!isCartInfoNull) {
            String cartInfoJson = source.readString();
            cartInfo = new HashMap<>();
            cartInfo = (HashMap<String, Integer>) new Gson().fromJson(cartInfoJson, cartInfo.getClass());
        }
        boolean isProductTabInfoNull = source.readByte() == (byte) 1;
        if (!isProductTabInfoNull) {
            productTabInfos = new ArrayList<>();
            source.readTypedList(productTabInfos, ProductTabInfo.CREATOR);
        }
        boolean isScreenNameNull = source.readByte() == (byte) 1;
        if (!isScreenNameNull) {
            screenName = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(isBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!isBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        boolean isContentSectionNull = contentSectionData == null;
        dest.writeByte(isContentSectionNull ? (byte) 1 : (byte) 0);
        if (!isContentSectionNull) {
            dest.writeParcelable(contentSectionData, flags);
        }
        boolean isCartInfoNull = cartInfo == null;
        dest.writeByte(isCartInfoNull ? (byte) 1 : (byte) 0);
        if (!isCartInfoNull) {
            dest.writeString(new Gson().toJson(cartInfo));
        }
        boolean isProductTabInfoNull = productTabInfos == null;
        dest.writeByte(isProductTabInfoNull ? (byte) 1 : (byte) 0);
        if (!isProductTabInfoNull) {
            dest.writeTypedList(productTabInfos);
        }
        boolean isScreenNameNull = screenName == null;
        dest.writeByte(isScreenNameNull ? (byte) 1 : (byte) 0);
        if (!isScreenNameNull) {
            dest.writeString(screenName);
        }
    }

    public static final Parcelable.Creator<ProductTabData> CREATOR = new Parcelable.Creator<ProductTabData>() {
        @Override
        public ProductTabData createFromParcel(Parcel source) {
            return new ProductTabData(source);
        }

        @Override
        public ProductTabData[] newArray(int size) {
            return new ProductTabData[size];
        }
    };

    @Nullable
    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    @Nullable
    public SectionData getContentSectionData() {
        return contentSectionData;
    }

    @Nullable
    public HashMap<String, Integer> getCartInfo() {
        return cartInfo;
    }

    public ArrayList<ProductTabInfo> getProductTabInfos() {
        return productTabInfos;
    }

    public String getScreenName() {
        return screenName;
    }
}
