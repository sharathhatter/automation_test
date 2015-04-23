package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class MarketPlaceAgeCheck extends FulfillmentInfo implements Parcelable {


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
    @SerializedName(Constants.AGE_LIMIT)
    private int ageLimit;
    @SerializedName(Constants.AGE_MESSAGE)
    private String ageMessage;

    public MarketPlaceAgeCheck(Parcel source) {
        super(source);
        ageLimit = source.readInt();

        boolean wasAgeMsgNull = source.readByte() == (byte) 1;
        if (!wasAgeMsgNull) {
            ageMessage = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(ageLimit);

        boolean wasAgeMsgNull = ageMessage == null;
        dest.writeByte(wasAgeMsgNull ? (byte) 1 : (byte) 0);
        if (!wasAgeMsgNull) {
            dest.writeString(ageMessage);
        }
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public String getAgeMessage() {
        return ageMessage;
    }

}
