package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

public class FilterOptionItem implements Parcelable {

    private String displayName;
    private String filterValueSlug;
    private boolean isSelected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(filterValueSlug);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }

    public FilterOptionItem(Parcel source) {
        displayName = source.readString();
        filterValueSlug = source.readString();
        isSelected = source.readByte() == (byte) 1;
    }

    public static final Parcelable.Creator<FilterOptionItem> CREATOR = new Parcelable.Creator<FilterOptionItem>() {
        @Override
        public FilterOptionItem createFromParcel(Parcel source) {
            return new FilterOptionItem(source);
        }

        @Override
        public FilterOptionItem[] newArray(int size) {
            return new FilterOptionItem[size];
        }
    };

    public FilterOptionItem(String displayName, String filterValueSlug) {
        this.displayName = displayName;
        this.filterValueSlug = filterValueSlug;
        this.isSelected = false;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFilterValueSlug() {
        return filterValueSlug;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
