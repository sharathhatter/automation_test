package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SubSectionItem extends SectionItem {

    @SerializedName(Constants.IS_LINK)
    private boolean isLink;

    public SubSectionItem(SectionTextItem title, SectionTextItem description, String image,
                          int renderingId, DestinationInfo destinationInfo) {
        super(title, description, image, renderingId, destinationInfo);
    }

    public SubSectionItem(Parcel source) {
        super(source);
        this.isLink = source.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.isLink ? (byte) 1 : (byte) 0);
    }

    public boolean isLink() {
        return isLink;
    }

    public static final Parcelable.Creator<SubSectionItem> CREATOR = new Parcelable.Creator<SubSectionItem>() {
        @Override
        public SubSectionItem createFromParcel(Parcel source) {
            return new SubSectionItem(source);
        }

        @Override
        public SubSectionItem[] newArray(int size) {
            return new SubSectionItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SubSectionItem that = (SubSectionItem) o;

        return isLink == that.isLink;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isLink ? 1 : 0);
        return result;
    }
}
