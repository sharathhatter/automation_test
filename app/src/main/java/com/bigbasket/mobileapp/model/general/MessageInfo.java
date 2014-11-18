package com.bigbasket.mobileapp.model.general;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class MessageInfo implements Parcelable {

    @SerializedName(Constants.MSG_STR)
    private String messageStr;

    @SerializedName(Constants.PARAMS)
    private ArrayList<MessageParamInfo> params;

    public MessageInfo(String messageStr, ArrayList<MessageParamInfo> params) {
        this.messageStr = messageStr;
        this.params = params;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public MessageInfo(Parcel parcel) {
        this.messageStr = parcel.readString();
        boolean isParamsNull = parcel.readByte() == (byte) 1;
        if (!isParamsNull) {
            params = new ArrayList<>();
            parcel.readTypedList(params, MessageParamInfo.CREATOR);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageStr);
        boolean isParamsNull = params == null;
        dest.writeByte(isParamsNull ? (byte) 1 : (byte) 0);
        if (!isParamsNull) {
            dest.writeTypedList(params);
        }
    }

    public static final Parcelable.Creator<MessageInfo> CREATOR = new Parcelable.Creator<MessageInfo>() {
        @Override
        public MessageInfo createFromParcel(Parcel source) {
            return new MessageInfo(source);
        }

        @Override
        public MessageInfo[] newArray(int size) {
            return new MessageInfo[size];
        }
    };

    public String getMessageStr() {
        return messageStr;
    }

    public void setMessageStr(String messageStr) {
        this.messageStr = messageStr;
    }

    public ArrayList<MessageParamInfo> getParams() {
        return params;
    }

    public void setParams(ArrayList<MessageParamInfo> params) {
        this.params = params;
    }
}
