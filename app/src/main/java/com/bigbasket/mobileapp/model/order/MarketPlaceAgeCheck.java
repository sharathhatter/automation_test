package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

public class MarketPlaceAgeCheck extends FulfillmentInfo implements Parcelable {


    @SerializedName(Constants.AGE_LIMIT)
    private int ageLimit;

    @SerializedName(Constants.AGE_MESSAGE)
    private String ageMessage;

    public MarketPlaceAgeCheck(String displayName, String tc2, String fulfilledBy,
                               String fulfillmentId, String fulfilledByInfoPage, String tc1,
                               String icon, int ageLimit, String ageMessage) {
        super(displayName, tc2, fulfilledBy, fulfillmentId, fulfilledByInfoPage, tc1, icon);
        this.ageLimit = ageLimit;
        this.ageMessage = ageMessage;
    }

    public MarketPlaceAgeCheck(int ageLimit, String ageMessage) {
        this.ageLimit = ageLimit;
        this.ageMessage = ageMessage;
    }

    public MarketPlaceAgeCheck(JSONObject jsonObject, int ageLimit, String ageMessage) throws JSONException {
        super(jsonObject);
        this.ageLimit = ageLimit;
        this.ageMessage = ageMessage;
    }

    public MarketPlaceAgeCheck(Parcel parcel, int ageLimit, String ageMessage) {
        super(parcel);
        this.ageLimit = ageLimit;
        this.ageMessage = ageMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public MarketPlaceAgeCheck(Parcel source) {
        super(source);
        ageLimit = source.readInt();
        ageMessage = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(ageLimit);
        dest.writeString(ageMessage);
    }

    public static final Parcelable.Creator<MarketPlaceAgeCheck> CREATOR = new Parcelable.Creator<MarketPlaceAgeCheck>() {
        @Override
        public MarketPlaceAgeCheck createFromParcel(Parcel source) {
            return new MarketPlaceAgeCheck(source);
        }

        @Override
        public MarketPlaceAgeCheck[] newArray(int size) {
            return new MarketPlaceAgeCheck[size];
        }
    };

    public int getAgeLimit() {
        return ageLimit;
    }

    public String getAgeMessage() {
        return ageMessage;
    }

}
