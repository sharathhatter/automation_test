package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductTabInfo implements Parcelable {
    @SerializedName(Constants.TAB_NAME)
    private String tabName;

    @SerializedName(Constants.TAB_TYPE)
    private String tabType;

    @SerializedName(Constants.PRODUCT_INFO)
    private ProductInfo productInfo;

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

    @SerializedName(Constants.SELLER_INFO)
    private SellerInfo sellerInfo;


    public ProductTabInfo(Parcel source) {
        tabName = source.readString();
        tabType = source.readString();

        boolean isProductListInfoNull = source.readByte() == (byte) 1;
        if (!isProductListInfoNull) {
            productInfo = source.readParcelable(ProductTabInfo.class.getClassLoader());
        }

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
        headerSelectedIndex = source.readInt();

        boolean isContentSellerIfoNull = source.readByte() == (byte) 1;
        if (!isContentSellerIfoNull) {
            sellerInfo = source.readParcelable(ProductTabInfo.class.getClassLoader());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tabName);
        dest.writeString(tabType);

        boolean isProductListDataNull = productInfo == null;
        dest.writeByte(isProductListDataNull ? (byte) 1 : (byte) 0);
        if (!isProductListDataNull) {
            dest.writeParcelable(productInfo, flags);
        }

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
        dest.writeInt(headerSelectedIndex);

        boolean isContentSellerIfoNull = sellerInfo == null;
        dest.writeByte(isContentSellerIfoNull ? (byte) 1 : (byte) 0);
        if (!isContentSellerIfoNull) {
            dest.writeParcelable(sellerInfo, flags);
        }
    }

    public static final Parcelable.Creator<ProductTabInfo> CREATOR = new Parcelable.Creator<ProductTabInfo>() {
        @Override
        public ProductTabInfo createFromParcel(Parcel source) {
            return new ProductTabInfo(source);
        }

        @Override
        public ProductTabInfo[] newArray(int size) {
            return new ProductTabInfo[size];
        }
    };

    public String getTabName() {
        return tabName;
    }

    public String getTabType() {
        return tabType;
    }

    @Nullable
    public ProductInfo getProductInfo() {
        return productInfo;
    }


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

    public void setFilteredOn(ArrayList<FilteredOn> filteredOn) {
        this.filteredOn = filteredOn;
    }

    public int getHeaderSelectedIndex() {
        return headerSelectedIndex;
    }
}
