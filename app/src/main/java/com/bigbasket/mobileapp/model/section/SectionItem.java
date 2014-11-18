package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SectionItem implements Parcelable {

    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;

    private String image;

    @SerializedName(Constants.DESTINATION_INFO_ID)
    private int destinationInfoId;

    public String getDisplayName() {
        return displayName;
    }

    public String getImage() {
        return image;
    }

    public int getDestinationInfoId() {
        return destinationInfoId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean _wasDisplayNameNull = displayName == null;
        boolean _wasImageNull = image == null;
        dest.writeByte(_wasDisplayNameNull ? (byte) 1 : (byte) 0);
        if (!_wasDisplayNameNull) {
            dest.writeString(displayName);
        }
        dest.writeByte(_wasImageNull ? (byte) 1 : (byte) 0);
        if (!_wasImageNull) {
            dest.writeString(image);
        }
        dest.writeInt(destinationInfoId);
    }

    public SectionItem(Parcel source) {
        boolean _wasDisplayNameNull = source.readByte() == (byte) 1;
        if (!_wasDisplayNameNull) {
            displayName = source.readString();
        }
        boolean _wasImageNull = source.readByte() == (byte) 1;
        if (!_wasImageNull) {
            image = source.readString();
        }
        destinationInfoId = source.readInt();
    }

    public static final Parcelable.Creator<SectionItem> CREATOR = new Parcelable.Creator<SectionItem>() {
        @Override
        public SectionItem createFromParcel(Parcel source) {
            return new SectionItem(source);
        }

        @Override
        public SectionItem[] newArray(int size) {
            return new SectionItem[size];
        }
    };
}
