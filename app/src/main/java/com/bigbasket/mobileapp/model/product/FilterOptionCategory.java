package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FilterOptionCategory implements Parcelable {

    public static final Parcelable.Creator<FilterOptionCategory> CREATOR = new Parcelable.Creator<FilterOptionCategory>() {
        @Override
        public FilterOptionCategory createFromParcel(Parcel source) {
            return new FilterOptionCategory(source);
        }

        @Override
        public FilterOptionCategory[] newArray(int size) {
            return new FilterOptionCategory[size];
        }
    };
    @SerializedName(Constants.FILTER_SLUG)
    private String filterSlug;
    @SerializedName(Constants.FILTER_NAME)
    private String filterName;
    @SerializedName(Constants.FILTER_VALUES)
    private List<FilterOptionItem> filterOptionItems;

    public FilterOptionCategory(Parcel source) {
        filterSlug = source.readString();
        filterName = source.readString();
        filterOptionItems = new ArrayList<>();
        source.readTypedList(filterOptionItems, FilterOptionItem.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filterSlug);
        dest.writeString(filterName);
        dest.writeTypedList(filterOptionItems);
    }

    public String getFilterSlug() {
        return filterSlug;
    }

    public String getFilterName() {
        return filterName;
    }

    public List<FilterOptionItem> getFilterOptionItems() {
        return filterOptionItems;
    }
}
