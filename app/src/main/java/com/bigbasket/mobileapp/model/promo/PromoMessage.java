package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PromoMessage implements Parcelable {

    public static final Parcelable.Creator<PromoMessage> CREATOR = new Parcelable.Creator<PromoMessage>() {
        @Override
        public PromoMessage createFromParcel(Parcel source) {
            return new PromoMessage(source);
        }

        @Override
        public PromoMessage[] newArray(int size) {
            return new PromoMessage[size];
        }
    };
    @SerializedName(Constants.MSG)
    private String promoMessage;
    @SerializedName(Constants.CRITERIA_MSGS)
    private ArrayList<String> criteriaMessages;

    public PromoMessage(Parcel source) {
        promoMessage = source.readString();

        criteriaMessages = new ArrayList<>();
        source.readStringList(criteriaMessages);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(promoMessage);
        dest.writeStringList(criteriaMessages);
    }

    public String getPromoMessage() {
        return promoMessage;
    }

    public ArrayList<String> getCriteriaMessages() {
        return criteriaMessages;
    }

    public List<Spannable> getCriteriaMsgSpannableList() {
        return UIUtil.createBulletSpannableList(criteriaMessages);
    }
}
