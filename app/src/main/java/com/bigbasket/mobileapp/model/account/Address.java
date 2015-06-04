package com.bigbasket.mobileapp.model.account;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Address implements Parcelable {

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
    @SerializedName(Constants.ID)
    private String id;
    @SerializedName(Constants.CONTACT_NUM)
    private String contactNum;
    @SerializedName(Constants.ADDR_NICK)
    private String addressNickName;
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
    @SerializedName(Constants.AREA)
    private String area;
    @SerializedName(Constants.CITY_NAME)
    private String cityName;
    @SerializedName(Constants.PIN)
    private String pincode;
    @SerializedName(Constants.IS_MAPPED)
    private boolean isMapped;
    @SerializedName(Constants.IS_EXPRESS)
    private boolean isExpress;

    public Address(Parcel source) {
        byte isDefaultByteVal = source.readByte();
        this.isDefault = isDefaultByteVal == (byte) 1;
        this.id = source.readString();
        this.contactNum = source.readString();
        this.addressNickName = source.readString();
        this.firstName = source.readString();
        this.lastName = source.readString();
        this.houseNumber = source.readString();
        this.street = source.readString();
        this.residentialComplex = source.readString();
        this.landmark = source.readString();
        this.area = source.readString();
        this.cityName = source.readString();
        this.pincode = source.readString();
        this.isMapped = source.readByte() == (byte) 1;
        this.isExpress = source.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isDefault ? (byte) 1 : (byte) 0);
        dest.writeString(id);
        dest.writeString(contactNum);
        dest.writeString(addressNickName);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(houseNumber);
        dest.writeString(street);
        dest.writeString(residentialComplex);
        dest.writeString(landmark);
        dest.writeString(area);
        dest.writeString(cityName);
        dest.writeString(pincode);
        dest.writeByte(isMapped ? (byte) 1 : (byte) 0);
        dest.writeByte(isExpress ? (byte) 1 : (byte) 0);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContactNum() {
        return contactNum;
    }

    public String getAddressNickName() {
        return addressNickName;
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCityName() {
        return cityName;
    }

    public String getPincode() {
        return pincode;
    }

    public boolean isExpress() {
        return isExpress;
    }

    @Override
    public String toString() {
        return (!TextUtils.isEmpty(houseNumber) ? houseNumber + "\n" : "") +
                (!TextUtils.isEmpty(street) ? street + "\n" : "") +
                (!TextUtils.isEmpty(area) ? area + "\n" : "") +
                (!TextUtils.isEmpty(residentialComplex) ? residentialComplex + "\n" : "") +
                (!TextUtils.isEmpty(landmark) ? landmark + "\n" : "") +
                (!TextUtils.isEmpty(cityName) ? cityName + "\n" : "") +
                (!TextUtils.isEmpty(pincode) ? "Pin - " + pincode + "\n" : "");
    }
}
