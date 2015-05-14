package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductTabData implements Parcelable {
    @SerializedName(Constants.HEADER_SECTION)
    private Section headerSection;

    @SerializedName(Constants.HEADER_SEL)
    private int headerSelectedIndex;

    @SerializedName(Constants.SORT_ON)
    private String sortedOn;

    @SerializedName(Constants.FILTERED_ON)
    private ArrayList<FilteredOn> filteredOn;

    @SerializedName(Constants.PRODUCT_SORT_OPTION)
    private ArrayList<Option> sortOptions;

    @SerializedName(Constants.FILTER_OPTIONS)
    private ArrayList<FilterOptionCategory> filterOptionItems;

    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    @SerializedName(Constants.CONTENT_SECTION)
    private SectionData contentSectionData;

    @SerializedName(Constants.CART_INFO)
    private HashMap<String, Integer> cartInfo;

    @SerializedName(Constants.TAB_INFO)
    private ArrayList<ProductTabInfo> productTabInfos;

    public ProductTabData(Parcel source) {
        boolean isHeaderSectionNull = source.readByte() == (byte) 1;
        if (!isHeaderSectionNull) {
            headerSection = source.readParcelable(ProductTabData.class.getClassLoader());
        }
        boolean isSortedOnNull = source.readByte() == (byte) 1;
        if (!isSortedOnNull) {
            sortedOn = source.readString();
        }
        boolean isFilteredOnNull = source.readByte() == (byte) 1;
        if (!isFilteredOnNull) {
            filteredOn = new ArrayList<>();
            source.readTypedList(filteredOn, FilteredOn.CREATOR);
        }
        boolean isSortOptionsNull = source.readByte() == (byte) 1;
        if (!isSortOptionsNull) {
            sortOptions = new ArrayList<>();
            source.readTypedList(sortOptions, Option.CREATOR);
        }
        boolean isFilterOptionCategoryNull = source.readByte() == (byte) 1;
        if (!isFilterOptionCategoryNull) {
            filterOptionItems = new ArrayList<>();
            source.readTypedList(filterOptionItems, FilterOptionCategory.CREATOR);
        }
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
        headerSelectedIndex = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isHeaderSectionNull = headerSection == null;
        dest.writeByte(isHeaderSectionNull ? (byte) 1 : (byte) 0);
        if (!isHeaderSectionNull) {
            dest.writeParcelable(headerSection, flags);
        }
        boolean isSortedOnNull = sortedOn == null;
        dest.writeByte(isSortedOnNull ? (byte) 1 : (byte) 0);
        if (!isSortedOnNull) {
            dest.writeString(sortedOn);
        }
        boolean isFilteredOnNull = filteredOn == null;
        dest.writeByte(isFilteredOnNull ? (byte) 1 : (byte) 0);
        if (!isFilteredOnNull) {
            dest.writeTypedList(filteredOn);
        }
        boolean isSortOptionsNull = sortOptions == null;
        dest.writeByte(isSortOptionsNull ? (byte) 1 : (byte) 0);
        if (!isSortOptionsNull) {
            dest.writeTypedList(sortOptions);
        }
        boolean isFilterOptionCategoryNull = filterOptionItems == null;
        dest.writeByte(isFilterOptionCategoryNull ? (byte) 1 : (byte) 0);
        if (!isFilterOptionCategoryNull) {
            dest.writeTypedList(filterOptionItems);
        }
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
        dest.writeInt(headerSelectedIndex);
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
    public Section getHeaderSection() {
        return headerSection;
    }

    @Nullable
    public String getSortedOn() {
        return sortedOn;
    }

    @Nullable
    public ArrayList<FilteredOn> getFilteredOn() {
        return filteredOn;
    }

    @Nullable
    public ArrayList<Option> getSortOptions() {
        return sortOptions;
    }

    @Nullable
    public ArrayList<FilterOptionCategory> getFilterOptionItems() {
        return filterOptionItems;
    }

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

    public void setFilteredOn(ArrayList<FilteredOn> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public ArrayList<ProductTabInfo> getProductTabInfos() {
        return productTabInfos;
    }

    public int getHeaderSelectedIndex() {
        return headerSelectedIndex;
    }
}
