package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class PharmaPrescriptionInfo implements Parcelable {

    @SerializedName(Constants.MESSAGE_OBJ)
    private MessageInfo msgInfo;

    public PharmaPrescriptionInfo(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public PharmaPrescriptionInfo(Parcel parcel) {
        msgInfo = parcel.readParcelable(PharmaPrescriptionInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(msgInfo, flags);
    }

    public static final Parcelable.Creator<PharmaPrescriptionInfo> CREATOR = new Parcelable.Creator<PharmaPrescriptionInfo>() {
        @Override
        public PharmaPrescriptionInfo createFromParcel(Parcel source) {
            return new PharmaPrescriptionInfo(source);
        }

        @Override
        public PharmaPrescriptionInfo[] newArray(int size) {
            return new PharmaPrescriptionInfo[size];
        }
    };

    public MessageInfo getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }

}
