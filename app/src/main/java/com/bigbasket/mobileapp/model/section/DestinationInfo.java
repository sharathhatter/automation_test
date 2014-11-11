package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class DestinationInfo implements Parcelable {

    @SerializedName(Constants.DESTINATION_INFO_ID)
    private int destinationInfoId;

    @SerializedName(Constants.DESTINATION_TYPE)
    private String destinationType;

    @SerializedName(Constants.DESTINATION_SLUG)
    private String destinationSlug;

    @SerializedName(Constants.VERSION)
    private String cacheVersion;

    public int getDestinationInfoId() {
        return destinationInfoId;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public String getDestinationSlug() {
        return destinationSlug;
    }

    public String getCacheVersion() {
        return cacheVersion;
    }

    public DestinationInfo(Parcel source) {
        destinationInfoId = source.readInt();
        destinationType = source.readString();
        boolean _wasDestSlugNull = source.readByte() == (byte) 1;
        if (!_wasDestSlugNull) {
            destinationSlug = source.readString();
        }
        boolean _wasCacheVersionNull = source.readByte() == (byte) 1;
        if (!_wasCacheVersionNull) {
            cacheVersion = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(destinationInfoId);
        dest.writeString(destinationType);
        boolean _wasDestSlugNull = destinationSlug == null;
        dest.writeByte(_wasDestSlugNull ? (byte) 1 : (byte) 0);
        if (!_wasDestSlugNull) {
            dest.writeString(destinationSlug);
        }
        boolean _wasCacheVersionNull = cacheVersion == null;
        dest.writeByte(_wasCacheVersionNull ? (byte) 1 : (byte) 0);
        if (!_wasCacheVersionNull) {
            dest.writeString(cacheVersion);
        }
    }


    public static final Parcelable.Creator<DestinationInfo> CREATOR = new Parcelable.Creator<DestinationInfo>() {
        @Override
        public DestinationInfo createFromParcel(Parcel source) {
            return new DestinationInfo(source);
        }

        @Override
        public DestinationInfo[] newArray(int size) {
            return new DestinationInfo[size];
        }
    };
}
