package com.bigbasket.mobileapp.model.promo;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PromoDetail extends Promo {

    @SerializedName(Constants.NUM_IN_BASKET)
    private int numPromoCompletedInBasket;

    @SerializedName(Constants.SAVING)
    private double saving;

    @SerializedName(Constants.ADD_ALLOW_IN_ORDER)
    private int additionalNumAllowedInOrder;

    @SerializedName(Constants.ADD_ALLOW_IN_LIFETIME)
    private int additionalNumAllowedInLifetime;

    @SerializedName(Constants.IS_ACTIVE)
    private boolean active;

    @SerializedName(Constants.TERMS_AND_COND)
    private String termsAndCond;

    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    @SerializedName(Constants.PROMO_SAVING)
    private double promoSaving;

    @SerializedName(Constants.REDEMPTION_INFO)
    private PromoRedemptionInfo promoRedemptionInfo;

    @SerializedName(Constants.FIXED_COMBO_PRODUCTS)
    private ArrayList<Product> fixedComboProducts;

    @SerializedName(Constants.FREE_PRODUCTS)
    private ArrayList<Product> freeProducts;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(numPromoCompletedInBasket);
        dest.writeDouble(saving);
        dest.writeInt(additionalNumAllowedInOrder);
        dest.writeInt(additionalNumAllowedInLifetime);
        dest.writeByte(active ? (byte) 1 : (byte) 0);
        dest.writeString(termsAndCond);
        boolean isBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(isBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!isBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        dest.writeDouble(promoSaving);
        dest.writeParcelable(promoRedemptionInfo, flags);
        boolean isFixedComboProductsNull = fixedComboProducts == null;
        dest.writeByte(isFixedComboProductsNull ? (byte) 1 : (byte) 0);
        if (!isFixedComboProductsNull) {
            dest.writeTypedList(fixedComboProducts);
        }
        boolean isFreeProductsNull = freeProducts == null;
        dest.writeByte(isFreeProductsNull ? (byte) 1 : (byte) 0);
        if (!isFreeProductsNull) {
            dest.writeTypedList(freeProducts);
        }
    }

    public PromoDetail(Parcel source) {
        super(source);
        numPromoCompletedInBasket = source.readInt();
        saving = source.readDouble();
        additionalNumAllowedInOrder = source.readInt();
        additionalNumAllowedInLifetime = source.readInt();
        active = source.readByte() == (byte) 1;
        termsAndCond = source.readString();
        boolean isBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!isBaseImgUrlNull) {
            baseImgUrl = source.readString();
        }
        promoSaving = source.readDouble();
        promoRedemptionInfo = source.readParcelable(PromoDetail.class.getClassLoader());
        boolean isFixedComboProductsNull = source.readByte() == (byte) 1;
        if (!isFixedComboProductsNull) {
            fixedComboProducts = new ArrayList<>();
            source.readTypedList(fixedComboProducts, Product.CREATOR);
        }
        boolean isFreeProducts = source.readByte() == (byte) 1;
        if (!isFreeProducts) {
            freeProducts = new ArrayList<>();
            source.readTypedList(freeProducts, Product.CREATOR);
        }
    }

    public static final Parcelable.Creator<PromoDetail> CREATOR = new Parcelable.Creator<PromoDetail>() {
        @Override
        public PromoDetail createFromParcel(Parcel source) {
            return new PromoDetail(source);
        }

        @Override
        public PromoDetail[] newArray(int size) {
            return new PromoDetail[size];
        }
    };

    public ArrayList<Product> getFixedComboProducts() {
        return fixedComboProducts;
    }

    public ArrayList<Product> getFreeProducts() {
        return freeProducts;
    }
    public int getNumPromoCompletedInBasket() {
        return numPromoCompletedInBasket;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

    public boolean isActive() {
        return active;
    }

    public String getTermsAndCond() {
        return termsAndCond;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public double getPromoSaving() {
        return promoSaving;
    }

    public PromoRedemptionInfo getPromoRedemptionInfo() {
        return promoRedemptionInfo;
    }

    public static Spannable getNumCompletedInBasketSpannable(int color, int numPromoCompletedInBasket) {
        String numPromoCompletedInBasketStr = String.valueOf(numPromoCompletedInBasket);
        Spannable numCompletedOfferSpan = new SpannableString(numPromoCompletedInBasketStr + " completed offers");
        numCompletedOfferSpan.setSpan(new ForegroundColorSpan(color),
                0, numPromoCompletedInBasketStr.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return numCompletedOfferSpan;
    }

    public static Spannable getSavingSpannable(int color, String saving, Typeface faceRupee) {
        String prefix = "You've saved ";
        String rupeeSymbol = "`";
        String savingStr = prefix + rupeeSymbol + saving;
        Spannable savingSpan = new SpannableString(savingStr);
        savingSpan.setSpan(new CustomTypefaceSpan("", faceRupee),
                prefix.length(), prefix.length() + rupeeSymbol.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        savingSpan.setSpan(new ForegroundColorSpan(color),
                prefix.length(), savingStr.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return savingSpan;
    }
}
