package com.bigbasket.mobileapp.model.account;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Address extends AddressSummary {

    public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
    @SerializedName(Constants.IS_DEFAULT)
    private boolean isDefault;

    @SerializedName(Constants.CONTACT_NUM)
    private String contactNum;
    @SerializedName(Constants.FIRSTNAME)
    private String firstName;
    @SerializedName(Constants.LASTNAME)
    private String lastName;
    @SerializedName(Constants.HOUSE_NO)
    private String houseNumber;
    @SerializedName(Constants.STREET)
    private String street;
    @SerializedName(Constants.RES_CMPLX)
    private String residentialComplex;
    @SerializedName(Constants.LANDMARK)
    private String landmark;
    @SerializedName(Constants.IS_MAPPED)
    private boolean isMapped;
    @SerializedName(Constants.IS_EXPRESS)
    private boolean isExpress;
    @SerializedName(Constants.IS_SELECTED)
    private boolean isSelected;

    public Address(Parcel source) {
        super(source);
        byte isDefaultByteVal = source.readByte();
        this.isDefault = isDefaultByteVal == (byte) 1;
        this.contactNum = source.readString();
        this.firstName = source.readString();
        this.lastName = source.readString();
        this.houseNumber = source.readString();
        this.street = source.readString();
        this.residentialComplex = source.readString();
        this.landmark = source.readString();
        this.isMapped = source.readByte() == (byte) 1;
        this.isExpress = source.readByte() == (byte) 1;
        this.isSelected = source.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(isDefault ? (byte) 1 : (byte) 0);
        dest.writeString(contactNum);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(houseNumber);
        dest.writeString(street);
        dest.writeString(residentialComplex);
        dest.writeString(landmark);
        dest.writeByte(isMapped ? (byte) 1 : (byte) 0);
        dest.writeByte(isExpress ? (byte) 1 : (byte) 0);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getContactNum() {
        return contactNum;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getResidentialComplex() {
        return residentialComplex;
    }

    public String getLandmark() {
        return landmark;
    }

    public boolean isExpress() {
        return isExpress;
    }

    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public String toString() {
        return (!TextUtils.isEmpty(getAddressNickName()) ? getAddressNickName() + " - " : "") +
                (!TextUtils.isEmpty(houseNumber) ? houseNumber + "\n" : "") +
                (!TextUtils.isEmpty(street) ? street + "\n" : "") +
                (!TextUtils.isEmpty(getArea()) ? getArea() + "\n" : "") +
                (!TextUtils.isEmpty(residentialComplex) ? residentialComplex + "\n" : "") +
                (!TextUtils.isEmpty(landmark) ? landmark + "\n" : "") +
                (!TextUtils.isEmpty(getCityName()) ? getCityName() + "\n" : "") +
                (!TextUtils.isEmpty(getPincode()) ? "Pin - " + getPincode() + "\n" : "");
    }
}
