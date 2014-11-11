package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CreditDetails implements Parcelable {

    private String type;

    @SerializedName(Constants.CREDIT_VALUE)
    private String creditValue;

    @SerializedName(Constants.MESG)
    private String message;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(creditValue);
        dest.writeString(message);
    }

    CreditDetails(Parcel source) {
        type = source.readString();
        creditValue = source.readString();
        message = source.readString();
    }

    public static final Parcelable.Creator<CreditDetails> CREATOR = new Parcelable.Creator<CreditDetails>() {
        @Override
        public CreditDetails createFromParcel(Parcel source) {
            return new CreditDetails(source);
        }

        @Override
        public CreditDetails[] newArray(int size) {
            return new CreditDetails[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(String creditValue) {
        this.creditValue = creditValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
