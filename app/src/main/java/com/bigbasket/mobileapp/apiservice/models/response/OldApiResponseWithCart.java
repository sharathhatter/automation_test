package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;

import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class OldApiResponseWithCart extends CartSummary {
    public String status;
    public String message;

    @SerializedName(Constants.ERROR_TYPE)
    public String errorType;

    public int getErrorTypeAsInt() {
        return Integer.parseInt(errorType);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if(status != null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(this.status);
        }
        if(message == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(this.message);
        }
        if(errorType == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(this.errorType);
        }
    }

    public OldApiResponseWithCart() {
    }

    protected OldApiResponseWithCart(Parcel in) {
        super(in);
        if(in.readInt() == 1) {
            this.status = in.readString();
        }
        if(in.readInt() == 1) {
            this.message = in.readString();
        }
        if(in.readInt() == 1) {
            this.errorType = in.readString();
        }
    }

    public static final Creator<OldApiResponseWithCart> CREATOR = new Creator<OldApiResponseWithCart>() {
        public OldApiResponseWithCart createFromParcel(Parcel source) {
            return new OldApiResponseWithCart(source);
        }

        public OldApiResponseWithCart[] newArray(int size) {
            return new OldApiResponseWithCart[size];
        }
    };
}
