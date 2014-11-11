package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class FilterOptionCategory implements Parcelable {

    private String filterSlug;
    private String filterName;
    private List<FilterOptionItem> filterOptionItems;

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

    public FilterOptionCategory(Parcel source) {
        filterSlug = source.readString();
        filterName = source.readString();
        filterOptionItems = new ArrayList<>();
        source.readTypedList(filterOptionItems, FilterOptionItem.CREATOR);
    }

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

    public FilterOptionCategory(String filterName, String filterSlug, List<FilterOptionItem> filterOptionItems) {
        this.filterSlug = filterSlug;
        this.filterName = filterName;
        this.filterOptionItems = filterOptionItems;
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
