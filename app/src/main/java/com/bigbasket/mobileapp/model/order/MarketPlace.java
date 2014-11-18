package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 16/7/14.
 */
public class MarketPlace implements Parcelable {

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


    public MarketPlace(boolean ruleValidationError, ArrayList<MarketPlaceRuleValidator> marketPlaceRuleValidators,
                       boolean ageCheckRequired, ArrayList<MarketPlaceAgeCheck> ageCheckRequiredDetail,
                       boolean pharamaPrescriptionNeeded, //PharmaPrescriptionInfo pharmaPrescriptionInfo,
                       String ruleValidationTitle, ArrayList<SavedPrescription> savedPrescription) {
        this.ruleValidationError = ruleValidationError;
        this.marketPlaceRuleValidators = marketPlaceRuleValidators;
        this.ageCheckRequired = ageCheckRequired;
        this.ageCheckRequiredDetail = ageCheckRequiredDetail;
        this.pharamaPrescriptionNeeded = pharamaPrescriptionNeeded;
        //this.pharmaPrescriptionInfo = pharmaPrescriptionInfo;
        this.ruleValidationTitle = ruleValidationTitle;
        this.savedPrescription = savedPrescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
        pharmaPrescriptionInfo = source.readParcelable(MarketPlace.class.getClassLoader());

        this.ruleValidationTitle = source.readString();

        boolean _wasSavedPrescriptionInfoNull = source.readByte() == (byte) 1;
        if (!_wasSavedPrescriptionInfoNull) {
            savedPrescription = new ArrayList<>();
            source.readTypedList(savedPrescription, SavedPrescription.CREATOR);
        }
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
        boolean _wasAgeCheckRequiredDetailNull = false;
        if (ageCheckRequiredDetail == null) {
            _wasAgeCheckRequiredDetailNull = true;
        }
        dest.writeByte(_wasAgeCheckRequiredDetailNull ? (byte) 1 : (byte) 0);
        if (ageCheckRequiredDetail != null) {
            dest.writeTypedList(ageCheckRequiredDetail);
        }

        dest.writeByte(this.pharamaPrescriptionNeeded ? (byte) 1 : (byte) 0);
        dest.writeParcelable(pharmaPrescriptionInfo, flags);

        dest.writeString(ruleValidationTitle);

        boolean _wasSavedPrescriptionInfoNull = false;
        if (savedPrescription == null) {
            _wasSavedPrescriptionInfoNull = true;
        }
        dest.writeByte(_wasSavedPrescriptionInfoNull ? (byte) 1 : (byte) 0);
        if (savedPrescription != null) {
            dest.writeTypedList(savedPrescription);
        }

    }

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

}
