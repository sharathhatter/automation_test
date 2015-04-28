package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class AnnotationInfo implements Parcelable {

    public static final Parcelable.Creator<AnnotationInfo> CREATOR = new Parcelable.Creator<AnnotationInfo>() {
        @Override
        public AnnotationInfo createFromParcel(Parcel source) {
            return new AnnotationInfo(source);
        }

        @Override
        public AnnotationInfo[] newArray(int size) {
            return new AnnotationInfo[size];
        }
    };
    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;
    @SerializedName(Constants.ANNOTAION_ID)
    private String annotationId;
    @SerializedName(Constants.DESCRIPTION)
    private String description;
    @SerializedName(Constants.INFO_PAGE)
    private String infoPage;
    @SerializedName(Constants.ICON_URL)
    private String iconUrl;
    @SerializedName(Constants.MESSAGE_OBJ)
    private MessageInfo msgInfo;

    public AnnotationInfo(Parcel parcel) {
        this.displayName = parcel.readString();
        this.annotationId = parcel.readString();
        this.description = parcel.readString();
        this.infoPage = parcel.readString();
        this.iconUrl = parcel.readString();
        msgInfo = parcel.readParcelable(AnnotationInfo.class.getClassLoader());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(annotationId);
        dest.writeString(description);
        dest.writeString(infoPage);
        dest.writeString(iconUrl);
        dest.writeParcelable(msgInfo, flags);
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAnnotationId() {
        return annotationId;
    }

    public MessageInfo getMsgInfo() {
        return msgInfo;
    }
}
