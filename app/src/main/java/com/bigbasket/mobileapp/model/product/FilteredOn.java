package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FilteredOn implements Parcelable {

    @SerializedName(Constants.FILTER_SLUG)
    private String filterSlug;

    @SerializedName(Constants.FILTER_VALUES)
    private ArrayList<String> filterValues;

    public String getFilterSlug() {
        return filterSlug;
    }

    public ArrayList<String> getFilterValues() {
        return filterValues;
    }

    public static FilteredOn getFilteredOn(ArrayList<FilteredOn> filteredOns, String filterSlug) {
        for (FilteredOn filteredOn : filteredOns) {
            if (filteredOn.getFilterSlug().equalsIgnoreCase(filterSlug)) {
                return filteredOn;
            }
        }
        return null;
    }

    public void setFilterSlug(String filterSlug) {
        this.filterSlug = filterSlug;
    }

    public void setFilterValues(ArrayList<String> filterValues) {
        this.filterValues = filterValues;
    }

    public FilteredOn(String filterSlug) {
        this.filterSlug = filterSlug;
    }

    public FilteredOn(Parcel source) {
        filterSlug = source.readString();
        filterValues = new ArrayList<>();
        source.readStringList(filterValues);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filterSlug);
        dest.writeStringList(filterValues);
    }

    public static final Parcelable.Creator<FilteredOn> CREATOR = new Parcelable.Creator<FilteredOn>() {
        @Override
        public FilteredOn createFromParcel(Parcel source) {
            return new FilteredOn(source);
        }

        @Override
        public FilteredOn[] newArray(int size) {
            return new FilteredOn[size];
        }
    };
}
