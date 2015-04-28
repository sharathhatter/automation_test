package com.bigbasket.mobileapp.model.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class MessageParamInfo implements Parcelable {

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
    @SerializedName(Constants.TYPE)
    private String type;
    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;
    @SerializedName(Constants.EXTRA_ATTR)
    private String extraInfo;
    @SerializedName(Constants.INTERNAL_VALUE)
    private String internalValue;

    public MessageParamInfo(Parcel parcel) {
        this.type = parcel.readString();
        this.displayName = parcel.readString();
        this.extraInfo = parcel.readString();
        this.internalValue = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(displayName);
        dest.writeString(extraInfo);
        dest.writeString(internalValue);
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public String getInternalValue() {
        return internalValue;
    }

}
