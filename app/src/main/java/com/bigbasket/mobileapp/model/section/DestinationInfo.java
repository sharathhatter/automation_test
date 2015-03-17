package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class DestinationInfo implements Parcelable, Serializable {

    public static final String PRODUCT_CATEGORY = "product_category";
    public static final String PRODUCT_LIST = "product_list";
    public static final String SEARCH = "search";
    public static final String SHOPPING_LIST_LANDING = "shopping_list_landing";
    public static final String SHOPPING_LIST_SUMMARY = "shopping_list_summary";
    public static final String SHOPPING_LIST = "shopping_list";
    public static final String PROMO_DETAIL = "promo_detail";
    public static final String CATEGORY_LANDING = "category_landing";
    public static final String PREVIOUS_ORDERS = "previous_orders";
    public static final String FLAT_PAGE = "flat_page";
    public static final String PRODUCT_DETAIL = "product_detail";
    public static final String DEEP_LINK = "deep_link";
    public static final String PROMO_LIST = "promo_list";
    public static final String HOME = "home";

    @SerializedName(Constants.DESTINATION_TYPE)
    private String destinationType;

    @SerializedName(Constants.DESTINATION_SLUG)
    private String destinationSlug;

    @SerializedName(Constants.VERSION)
    private String cacheVersion;

    public DestinationInfo(String destinationType, String destinationSlug) {
        this.destinationType = destinationType;
        this.destinationSlug = destinationSlug;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public String getDestinationSlug() {
        return destinationSlug;
    }

    public String getCacheVersion() {
        return cacheVersion;
    }

    public DestinationInfo(Parcel source) {
        destinationType = source.readString();
        boolean _wasDestSlugNull = source.readByte() == (byte) 1;
        if (!_wasDestSlugNull) {
            destinationSlug = source.readString();
        }
        boolean _wasCacheVersionNull = source.readByte() == (byte) 1;
        if (!_wasCacheVersionNull) {
            cacheVersion = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(destinationType);
        boolean _wasDestSlugNull = destinationSlug == null;
        dest.writeByte(_wasDestSlugNull ? (byte) 1 : (byte) 0);
        if (!_wasDestSlugNull) {
            dest.writeString(destinationSlug);
        }
        boolean _wasCacheVersionNull = cacheVersion == null;
        dest.writeByte(_wasCacheVersionNull ? (byte) 1 : (byte) 0);
        if (!_wasCacheVersionNull) {
            dest.writeString(cacheVersion);
        }
    }

    @Nullable
    public ArrayList<NameValuePair> getProductQueryParams() {
        if (TextUtils.isEmpty(destinationSlug)) return null;

        if (destinationSlug.contains("&") || destinationSlug.contains("=")) {
            return getAsNameValuePair();
        }
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, destinationSlug));
        return nameValuePairs;
    }

    public ArrayList<NameValuePair> getAsNameValuePair() {
        String[] params = destinationSlug.split("&");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        for (String paramData : params) {
            if (paramData.contains("=")) {
                String[] splittedParams = paramData.split("=");
                if (splittedParams.length == 2) {
                    nameValuePairs.add(new NameValuePair(splittedParams[0], splittedParams[1]));
                }
            }
        }
        return nameValuePairs;
    }

    public static final Parcelable.Creator<DestinationInfo> CREATOR = new Parcelable.Creator<DestinationInfo>() {
        @Override
        public DestinationInfo createFromParcel(Parcel source) {
            return new DestinationInfo(source);
        }

        @Override
        public DestinationInfo[] newArray(int size) {
            return new DestinationInfo[size];
        }
    };
}
