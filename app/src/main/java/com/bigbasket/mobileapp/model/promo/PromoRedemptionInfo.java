package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PromoRedemptionInfo implements Parcelable {

    @SerializedName(Constants.INFO_MESSAGE)
    private PromoMessage promoMessage;

    @SerializedName(Constants.PROMO_SETS)
    private ArrayList<PromoSet> promoSets;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(promoMessage, flags);
        dest.writeTypedList(promoSets);
    }

    public PromoRedemptionInfo(Parcel source) {
        promoMessage = source.readParcelable(PromoRedemptionInfo.class.getClassLoader());
        promoSets = new ArrayList<>();
        source.readTypedList(promoSets, PromoSet.CREATOR);
    }

    public static final Parcelable.Creator<PromoRedemptionInfo> CREATOR = new Parcelable.Creator<PromoRedemptionInfo>() {
        @Override
        public PromoRedemptionInfo createFromParcel(Parcel source) {
            return new PromoRedemptionInfo(source);
        }

        @Override
        public PromoRedemptionInfo[] newArray(int size) {
            return new PromoRedemptionInfo[size];
        }
    };

    public PromoMessage getPromoMessage() {
        return promoMessage;
    }

    public ArrayList<PromoSet> getPromoSets() {
        return promoSets;
    }
}
