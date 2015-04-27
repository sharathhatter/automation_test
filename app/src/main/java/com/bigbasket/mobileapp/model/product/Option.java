package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Option implements Parcelable {

    public static final Parcelable.Creator<Option> CREATOR = new Parcelable.Creator<Option>() {
        @Override
        public Option createFromParcel(Parcel source) {
            return new Option(source);
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };
    @SerializedName(Constants.DISPLAY_NAME)
    private String sortName;
    @SerializedName(Constants.VALUE)
    private String sortSlug;

    public Option(Parcel source) {
        sortName = source.readString();
        sortSlug = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sortName);
        dest.writeString(sortSlug);
    }

    @Override
    public String toString() {
        return sortName;
    }

    public String getSortName() {
        return sortName;
    }

    public String getSortSlug() {
        return sortSlug;
    }
}
