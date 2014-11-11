package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PromoSet implements Parcelable {

    public static final String QTY = "quantity";
    public static final String AMOUNT = "amount";

    public static final String CRITERIA = "criteria";
    public static final String PROMO_PRODUCT = "promo_product";

    @SerializedName(Constants.PROMO_SET_NAME)
    private String name;

    @SerializedName(Constants.VAL_IN_BASKET)
    private int valueInBasket;

    @SerializedName(Constants.PROMO_CRITERIA_VAL)
    private int promoCriteriaVal;

    @SerializedName(Constants.SET_ID)
    private int setId;

    @SerializedName(Constants.SET_TYPE)
    private String setType;

    @SerializedName(Constants.VALUE_TYPE)
    private String valType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(valueInBasket);
        dest.writeInt(promoCriteriaVal);
        dest.writeInt(setId);
        dest.writeString(setType);
        dest.writeString(valType);
    }

    public PromoSet(Parcel source) {
        name = source.readString();
        valueInBasket = source.readInt();
        promoCriteriaVal = source.readInt();
        setId = source.readInt();
        setType = source.readString();
        valType = source.readString();
    }

    public static final Parcelable.Creator<PromoSet> CREATOR = new Parcelable.Creator<PromoSet>() {
        @Override
        public PromoSet createFromParcel(Parcel source) {
            return new PromoSet(source);
        }

        @Override
        public PromoSet[] newArray(int size) {
            return new PromoSet[size];
        }
    };

    public PromoSet(String name, int valueInBasket, int promoCriteriaVal, int setId, String setType, String valType) {
        this.name = name;
        this.valueInBasket = valueInBasket;
        this.promoCriteriaVal = promoCriteriaVal;
        this.setId = setId;
        this.setType = setType;
        this.valType = valType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValueInBasket() {
        return valueInBasket;
    }

    public void setValueInBasket(int valueInBasket) {
        this.valueInBasket = valueInBasket;
    }

    public int getPromoCriteriaVal() {
        return promoCriteriaVal;
    }

    public void setPromoCriteriaVal(int promoCriteriaVal) {
        this.promoCriteriaVal = promoCriteriaVal;
    }

    public int getSetId() {
        return setId;
    }

    public void setSetId(int setId) {
        this.setId = setId;
    }

    public String getSetType() {
        return setType;
    }

    public void setSetType(String setType) {
        this.setType = setType;
    }

    public String getValType() {
        return valType;
    }

    public void setValType(String valType) {
        this.valType = valType;
    }
}
