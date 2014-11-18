package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OrderModification implements Parcelable {

    @SerializedName(Constants.TYPE)
    private String type;

    @SerializedName(Constants.MESSAGE)
    private String message;

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(message);
    }

    public OrderModification(Parcel source) {
        this.type = source.readString();
        this.message = source.readString();
    }

    public static final Parcelable.Creator<OrderModification> CREATOR = new Parcelable.Creator<OrderModification>() {
        @Override
        public OrderModification createFromParcel(Parcel source) {
            return new OrderModification(source);
        }

        @Override
        public OrderModification[] newArray(int size) {
            return new OrderModification[size];
        }
    };
}
