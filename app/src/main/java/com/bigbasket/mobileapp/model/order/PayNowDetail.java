package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PayNowDetail implements Parcelable {
    private String msg;
    @SerializedName(Constants.VAL)
    private String value;
    @SerializedName(Constants.VAL_TYPE)
    private String valueType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeString(value);

        boolean isValTypeNull = valueType == null;
        dest.writeByte(isValTypeNull ? (byte) 1 : (byte) 0);
        if (!isValTypeNull) {
            dest.writeString(valueType);
        }
    }

    public PayNowDetail(Parcel source) {
        this.msg = source.readString();
        this.value = source.readString();
        boolean isValTypeNull = source.readByte() == (byte) 1;
        if (!isValTypeNull) {
            this.valueType = source.readString();
        }
    }

    public String getMsg() {
        return msg;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public String getValueType() {
        return valueType;
    }

    public static final Parcelable.Creator<PayNowDetail> CREATOR = new Parcelable.Creator<PayNowDetail>() {
        @Override
        public PayNowDetail createFromParcel(Parcel source) {
            return new PayNowDetail(source);
        }

        @Override
        public PayNowDetail[] newArray(int size) {
            return new PayNowDetail[size];
        }
    };
}
