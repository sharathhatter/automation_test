package com.bigbasket.mobileapp.model.product;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Category implements Parcelable, Serializable {

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    @SerializedName(Constants.SLUG_NAME)
    private String slug;
    @SerializedName(Constants.DISPLAY_NAME)
    private String name;
    @SerializedName(Constants.FILTER)
    private String filter;
    @SerializedName(Constants.SORT_BY)
    private String sortBy;

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Category(Parcel source) {
        this.slug = source.readString();
        this.name = source.readString();
        boolean _wasFilterNull = source.readByte() == (byte) 1;
        if (!_wasFilterNull) {
            filter = source.readString();
        }

        boolean _wasSortByrNull = source.readByte() == (byte) 1;
        if (!_wasSortByrNull) {
            sortBy = source.readString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.slug);
        dest.writeString(this.name);
        boolean _wasFilterNull = filter == null;
        dest.writeByte(_wasFilterNull ? (byte) 1 : (byte) 0);
        if (!_wasFilterNull) {
            dest.writeString(filter);
        }
        boolean _wasSortByNull = filter == null;
        dest.writeByte(_wasSortByNull ? (byte) 1 : (byte) 0);
        if (!_wasSortByNull) {
            dest.writeString(sortBy);
        }
    }

    public String getFilter() {
        return filter;
    }

    public String getSortBy() {
        return sortBy;
    }
}
