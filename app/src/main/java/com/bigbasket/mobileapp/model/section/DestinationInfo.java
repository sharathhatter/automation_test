package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DestinationInfo implements Parcelable, Serializable {

    public static final String PRODUCT_CATEGORY = "product_category";
    public static final String PRODUCT_LIST = "product_list";
    public static final String SEARCH = "search";
    public static final String SHOPPING_LIST_LANDING = "shopping_list_landing";
    public static final String SHOPPING_LIST_SUMMARY = "shopping_list_summary";
    public static final String PROMO_DETAIL = "promo_detail";
    public static final String CATEGORY_LANDING = "category_landing";
    public static final String PREVIOUS_ORDERS = "previous_orders";
    public static final String FLAT_PAGE = "flat_page";
    public static final String PRODUCT_DETAIL = "product_detail";
    public static final String DEEP_LINK = "deep_link";
    public static final String PROMO_LIST = "promo_list";
    public static final String HOME = "home";
    public static final String COMMUNICATION_HUB = "com_hub";
    public static final String CALL = "call";
    public static final String DYNAMIC_PAGE = Constants.DYNAMIC_PAGE;
    public static final String DISCOUNT = "discount";
    public static final String BASKET = "basket";
    public static final String STORE_LIST = "sp_store_list";
    public static final String COMMUNICATION_HUB_FAQ = "faq";

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

    public String getDestinationType() {
        return destinationType;
    }

    public String getDestinationSlug() {
        return destinationSlug;
    }

    public String getCacheVersion() {
        return cacheVersion;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DestinationInfo that = (DestinationInfo) o;

        if (destinationType != null ? !destinationType.equals(that.destinationType) : that.destinationType != null)
            return false;
        if (destinationSlug != null ? !destinationSlug.equals(that.destinationSlug) : that.destinationSlug != null)
            return false;
        return !(cacheVersion != null ? !cacheVersion.equals(that.cacheVersion) : that.cacheVersion != null);

    }

    @Override
    public int hashCode() {
        int result = destinationType != null ? destinationType.hashCode() : 0;
        result = 31 * result + (destinationSlug != null ? destinationSlug.hashCode() : 0);
        result = 31 * result + (cacheVersion != null ? cacheVersion.hashCode() : 0);
        return result;
    }
}
