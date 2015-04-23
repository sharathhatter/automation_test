package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class MarketPlace implements Parcelable {

    public static final Parcelable.Creator<MarketPlace> CREATOR = new Parcelable.Creator<MarketPlace>() {
        @Override
        public MarketPlace createFromParcel(Parcel source) {
            return new MarketPlace(source);
        }

        @Override
        public MarketPlace[] newArray(int size) {
            return new MarketPlace[size];
        }
    };
    @SerializedName(Constants.RULE_VALIDATION_ERROR)
    private boolean ruleValidationError;
    @SerializedName(Constants.RULE_VALIDATION_DETAILS)
    private ArrayList<MarketPlaceRuleValidator> marketPlaceRuleValidators;
    @SerializedName(Constants.AGE_CHECK_REQUIRED)
    private boolean ageCheckRequired;
    @SerializedName(Constants.AGE_CHECK_REQUIRED_DETAILS)
    private ArrayList<MarketPlaceAgeCheck> ageCheckRequiredDetail;  // arrayList of objects
    @SerializedName(Constants.PHARMA_PRESCRIPTION_NEEDED)
    private boolean pharamaPrescriptionNeeded;
    @SerializedName(Constants.PHARMA_PRESCRIPTION_INFO)
    private PharmaPrescriptionInfo pharmaPrescriptionInfo;
    @SerializedName(Constants.RULE_VALIDATION_TITLE)
    private String ruleValidationTitle;
    @SerializedName(Constants.SAVED_PRESCRIPTION)
    private ArrayList<SavedPrescription> savedPrescription;
    @SerializedName(Constants.TERMS_AND_COND)
    private String termsAndCond;

    public MarketPlace(Parcel source) {
        this.ruleValidationError = source.readByte() == (byte) 1;
        boolean _wasMarketPlaceRuleValidatorsNull = source.readByte() == (byte) 1;
        if (!_wasMarketPlaceRuleValidatorsNull) {
            marketPlaceRuleValidators = new ArrayList<>();
            source.readTypedList(marketPlaceRuleValidators, MarketPlaceRuleValidator.CREATOR);
        }

        this.ageCheckRequired = source.readByte() == (byte) 1;
        boolean _wasAgeCheckRequiredDetailNull = source.readByte() == (byte) 1;
        if (!_wasAgeCheckRequiredDetailNull) {
            ageCheckRequiredDetail = new ArrayList<>();
            source.readTypedList(ageCheckRequiredDetail, MarketPlaceAgeCheck.CREATOR);
        }

        this.pharamaPrescriptionNeeded = source.readByte() == (byte) 1;

        boolean wasPharmaPrescNull = source.readByte() == (byte) 1;
        if (!wasPharmaPrescNull) {
            pharmaPrescriptionInfo = source.readParcelable(MarketPlace.class.getClassLoader());
        }

        boolean wasRuleValidationTitleNull = source.readByte() == (byte) 1;
        if (!wasRuleValidationTitleNull) {
            this.ruleValidationTitle = source.readString();
        }

        boolean _wasSavedPrescriptionInfoNull = source.readByte() == (byte) 1;
        if (!_wasSavedPrescriptionInfoNull) {
            savedPrescription = new ArrayList<>();
            source.readTypedList(savedPrescription, SavedPrescription.CREATOR);
        }

        boolean _wasTermsAndCondNull = source.readByte() == (byte) 1;
        if (!_wasTermsAndCondNull) {
            termsAndCond = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.ruleValidationError ? (byte) 1 : (byte) 0);
        boolean _wasMarketPlaceRuleValidatorsNull = false;
        if (marketPlaceRuleValidators == null) {
            _wasMarketPlaceRuleValidatorsNull = true;
        }
        dest.writeByte(_wasMarketPlaceRuleValidatorsNull ? (byte) 1 : (byte) 0);
        if (marketPlaceRuleValidators != null) {
            dest.writeTypedList(marketPlaceRuleValidators);
        }

        dest.writeByte(this.ageCheckRequired ? (byte) 1 : (byte) 0);
        boolean _wasAgeCheckRequiredDetailNull = ageCheckRequiredDetail == null;
        dest.writeByte(_wasAgeCheckRequiredDetailNull ? (byte) 1 : (byte) 0);
        if (!_wasAgeCheckRequiredDetailNull) {
            dest.writeTypedList(ageCheckRequiredDetail);
        }

        dest.writeByte(this.pharamaPrescriptionNeeded ? (byte) 1 : (byte) 0);

        boolean wasPharmaPrescInfoNull = pharmaPrescriptionInfo == null;
        dest.writeByte(wasPharmaPrescInfoNull ? (byte) 1 : (byte) 0);
        if (!wasPharmaPrescInfoNull) {
            dest.writeParcelable(pharmaPrescriptionInfo, flags);
        }

        boolean wasRuleValidationTitleNull = ruleValidationTitle == null;
        dest.writeByte(wasRuleValidationTitleNull ? (byte) 1 : (byte) 0);
        if (!wasRuleValidationTitleNull) {
            dest.writeString(ruleValidationTitle);
        }

        boolean _wasSavedPrescriptionInfoNull = false;
        if (savedPrescription == null) {
            _wasSavedPrescriptionInfoNull = true;
        }
        dest.writeByte(_wasSavedPrescriptionInfoNull ? (byte) 1 : (byte) 0);
        if (savedPrescription != null) {
            dest.writeTypedList(savedPrescription);
        }

        boolean _wasTermsAndCondNull = termsAndCond == null;
        dest.writeByte(_wasTermsAndCondNull ? (byte) 1 : (byte) 0);
        if (!_wasTermsAndCondNull) {
            dest.writeString(termsAndCond);
        }
    }

    public ArrayList<SavedPrescription> getSavedPrescription() {
        return savedPrescription;
    }

    public String getRuleValidationTitle() {
        return ruleValidationTitle;
    }

    public boolean isRuleValidationError() {
        return ruleValidationError;
    }

    public ArrayList<MarketPlaceRuleValidator> getMarketPlaceRuleValidators() {
        return marketPlaceRuleValidators;
    }

    public boolean isAgeCheckRequired() {
        return ageCheckRequired;
    }

    public ArrayList<MarketPlaceAgeCheck> getAgeCheckRequiredDetail() {
        return ageCheckRequiredDetail;
    }

    public boolean isPharamaPrescriptionNeeded() {
        return pharamaPrescriptionNeeded;
    }

    public PharmaPrescriptionInfo getPharmaPrescriptionInfo() {
        return pharmaPrescriptionInfo;
    }

    public boolean hasTermsAndCond() {
        return !TextUtils.isEmpty(termsAndCond);
    }

    public String getTermsAndCond() {
        return termsAndCond;
    }
}
