package com.bigbasket.mobileapp.model.account;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
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

    @SerializedName(Constants.LOCATION)
    private String[] location;

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
        int locLength = source.readInt();
        if(locLength > 0) {
            this.location = new String[locLength];
            source.readStringArray(location);
        }
    }

    public Address(boolean isDefault, String contactNum, String firstName, String lastName, String houseNumber, String street, String residentialComplex, String landmark, boolean isMapped, boolean isExpress, boolean isSelected) {
        this.isDefault = isDefault;
        this.contactNum = contactNum;
        this.firstName = firstName;
        this.lastName = lastName;
        this.houseNumber = houseNumber;
        this.street = street;
        this.residentialComplex = residentialComplex;
        this.landmark = landmark;
        this.isMapped = isMapped;
        this.isExpress = isExpress;
        this.isSelected = isSelected;
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
        if(location != null){
            dest.writeInt(location.length);
            if(location.length > 0) {
                dest.writeStringArray(location);
            }
        } else {
            dest.writeInt(0);
        }
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

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return (!UIUtil.isEmpty(getName()) ? getName() + "\n" : "") +
                (!UIUtil.isEmpty(houseNumber) ? houseNumber + "\n" : "") +
                (!UIUtil.isEmpty(street) ? street + "\n" : "") +
                (!UIUtil.isEmpty(getArea()) ? getArea() + "\n" : "") +
                (!UIUtil.isEmpty(residentialComplex) ? residentialComplex + "\n" : "") +
                (!UIUtil.isEmpty(landmark) ? landmark + "\n" : "") +
                (!UIUtil.isEmpty(getCityName()) ? getCityName() + "\n" : "") +
                (!UIUtil.isEmpty(getPincode()) ? "Pin - " + getPincode() + "\n" : "");
    }
}
