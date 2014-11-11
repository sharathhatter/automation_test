package com.bigbasket.mobileapp.model.general;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class MessageParamInfo implements Parcelable {

    @SerializedName(Constants.TYPE)
    private String type;

    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;

    @SerializedName(Constants.EXTRA_ATTR)
    private String extraInfo;

    @SerializedName(Constants.INTERNAL_VALUE)
    private String internalValue;

    public MessageParamInfo(String type, String displayName, String extraInfo, String internalValue) {
        this.type = type;
        this.displayName = displayName;
        this.extraInfo = extraInfo;
        this.internalValue = internalValue;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public MessageParamInfo(Parcel parcel) {
        this.type = parcel.readString();
        this.displayName = parcel.readString();
        this.extraInfo = parcel.readString();
        this.internalValue = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(displayName);
        dest.writeString(extraInfo);
        dest.writeString(internalValue);
    }

    public static final Parcelable.Creator<MessageParamInfo> CREATOR = new Parcelable.Creator<MessageParamInfo>() {
        @Override
        public MessageParamInfo createFromParcel(Parcel source) {
            return new MessageParamInfo(source);
        }

        @Override
        public MessageParamInfo[] newArray(int size) {
            return new MessageParamInfo[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getInternalValue() {
        return internalValue;
    }

    public void setInternalValue(String internalValue) {
        this.internalValue = internalValue;
    }

}
