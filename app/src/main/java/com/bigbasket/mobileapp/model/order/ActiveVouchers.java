package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ActiveVouchers implements Parcelable {
    public static final Creator<ActiveVouchers> CREATOR = new Parcelable.Creator<ActiveVouchers>() {
        @Override
        public ActiveVouchers createFromParcel(Parcel source) {
            return new ActiveVouchers(source);
        }

        @Override
        public ActiveVouchers[] newArray(int size) {
            return new ActiveVouchers[size];
        }
    };
    @SerializedName(Constants.CODE)
    private String code;
    @SerializedName(Constants.CUSTOMER_DESC)
    private String customerDesc;
    @SerializedName(Constants.MESSAGE)
    private String message;
    @SerializedName(Constants.VALIDITY)
    private String validity;
    @SerializedName(Constants.CAN_APPLY)
    private boolean canApply;

    public ActiveVouchers(String code, String customerDesc, String message, String validity, boolean canApply) {
        this.code = code;
        this.customerDesc = customerDesc;
        this.validity = validity;
        this.message = message;
        this.canApply = canApply;
    }

    public ActiveVouchers(Parcel source) {
        this.code = source.readString();
        this.customerDesc = source.readString();
        this.message = source.readString();
        this.validity = source.readString();
        this.canApply = source.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(customerDesc);
        dest.writeString(message);
        dest.writeString(validity);
        dest.writeByte(canApply ? (byte) 1 : (byte) 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomerDesc() {
        return customerDesc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValidity() {
        return validity;
    }

    public boolean canApply() {
        return canApply;
    }
}
