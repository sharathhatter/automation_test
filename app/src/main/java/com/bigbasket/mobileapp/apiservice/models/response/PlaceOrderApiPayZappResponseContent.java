package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by manu on 28/12/15.
 */
public class PlaceOrderApiPayZappResponseContent extends PlaceOrderReponseContent {
    public static final Parcelable.Creator<PlaceOrderApiPayZappResponseContent> CREATOR = new Parcelable.Creator<PlaceOrderApiPayZappResponseContent>() {
        @Override
        public PlaceOrderApiPayZappResponseContent[] newArray(int size) {
            return new PlaceOrderApiPayZappResponseContent[size];
        }

        @Override
        public PlaceOrderApiPayZappResponseContent createFromParcel(Parcel source) {
            return new PlaceOrderApiPayZappResponseContent(source);
        }
    };

    @SerializedName(Constants.POST_PARAMS)
    public PayzappPostParams payzappPostParams;

    public PlaceOrderApiPayZappResponseContent(Parcel parcel) {
        super(parcel);
        this.payzappPostParams = parcel.readParcelable(PayzappPostParams.class.getClassLoader());
    }



    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(payzappPostParams, flags);
    }
}
