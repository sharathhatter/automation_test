package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by manu on 30/12/15.
 */
public class PlaceOrderReponseContent implements Parcelable {
    public static final Parcelable.Creator<PlaceOrderReponseContent> CREATOR = new Parcelable.Creator<PlaceOrderReponseContent>() {
        @Override
        public PlaceOrderReponseContent[] newArray(int size) {
            return new PlaceOrderReponseContent[size];
        }

        @Override
        public PlaceOrderReponseContent createFromParcel(Parcel source) {
            return new PlaceOrderReponseContent(source);
        }
    };
    @SerializedName(Constants.ORDERS)
    public ArrayList<Order> orders;
    @SerializedName(Constants.ADD_MORE_LINK)
    public String addMoreLink;
    @SerializedName(Constants.ADD_MORE_MSG)
    public String addMoreMsg;

    public PlaceOrderReponseContent(Parcel parcel) {
        this.addMoreLink = parcel.readString();
        this.addMoreMsg = parcel.readString();
        this.orders = parcel.createTypedArrayList(Order.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addMoreLink);
        dest.writeString(addMoreMsg);
        dest.writeTypedList(orders);
    }
}
