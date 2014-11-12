package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;

public class ProductPromoInfo extends Promo {

    @SerializedName(Constants.PROMO_SAVING)
    private double promoSavings;

    public ProductPromoInfo(String promoName, String promoIcon, int promoId, String promoType,
                            String promoLabel, String promoDesc, double promoSavings) {
        super(promoName, promoIcon, promoId, promoType, promoLabel, promoDesc);
        this.promoSavings = promoSavings;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(promoSavings);
    }

    public ProductPromoInfo(Parcel source) {
        super(source);
        promoSavings = source.readDouble();
    }

    public static final Parcelable.Creator<ProductPromoInfo> CREATOR = new Parcelable.Creator<ProductPromoInfo>() {
        @Override
        public ProductPromoInfo createFromParcel(Parcel source) {
            return new ProductPromoInfo(source);
        }

        @Override
        public ProductPromoInfo[] newArray(int size) {
            return new ProductPromoInfo[size];
        }
    };

    public double getPromoSavings() {
        return promoSavings;
    }

    public String getFormattedPromoSavings() {
        return new DecimalFormat("0.00").format(promoSavings);
    }
}
