package com.bigbasket.mobileapp.model.product;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ProductAdditionalInfo implements Parcelable {

    public static final Parcelable.Creator<ProductAdditionalInfo> CREATOR = new Parcelable.Creator<ProductAdditionalInfo>() {
        @Override
        public ProductAdditionalInfo createFromParcel(Parcel source) {
            return new ProductAdditionalInfo(source);
        }

        @Override
        public ProductAdditionalInfo[] newArray(int size) {
            return new ProductAdditionalInfo[size];
        }
    };
    @SerializedName(Constants.TITLE)
    private String title;
    @SerializedName(Constants.CONTENT)
    private String content;

    public ProductAdditionalInfo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public ProductAdditionalInfo(Parcel source) {
        title = source.readString();
        content = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
