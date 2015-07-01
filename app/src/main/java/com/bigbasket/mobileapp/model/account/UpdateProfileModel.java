package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class UpdateProfileModel implements Parcelable {

    public static final Parcelable.Creator<UpdateProfileModel> CREATOR = new Parcelable.Creator<UpdateProfileModel>() {
        @Override
        public UpdateProfileModel createFromParcel(Parcel source) {
            return new UpdateProfileModel(source);
        }

        @Override
        public UpdateProfileModel[] newArray(int size) {
            return new UpdateProfileModel[size];
        }
    };
    @SerializedName(Constants.EMAIL)
    private String email;
    @SerializedName(Constants.FIRSTNAME)
    private String firstName;
    @SerializedName(Constants.LASTNAME)
    private String lastName;
    @SerializedName(Constants.DATE_OF_BIRTH)
    private String dateOfBirth;
    @SerializedName(Constants.MOBILE_NUMBER)
    private String mobileNumber;
    @SerializedName(Constants.TELEPHONE_NUMBER)
    private String telephoneNumber;
    @SerializedName(Constants.HOUSE_NO)
    private String houseNumber;
    @SerializedName(Constants.RES_CMPLX)
    private String residentialComplex;
    @SerializedName(Constants.LANDMARK)
    private String landmark;
    @SerializedName(Constants.AREA)
    private String area;
    @SerializedName(Constants.CITY_NAME)
    private String cityName;
    @SerializedName(Constants.STREET)
    private String street;
    @SerializedName(Constants.PIN_CODE)
    private String pincode;
    @SerializedName(Constants.NEWSPAPER_SUBSCRIPTION)
    private boolean newPaperSubscription;

    public UpdateProfileModel(Parcel source) {
        email = source.readString();
        firstName = source.readString();
        lastName = source.readString();
        boolean isDobNull = source.readByte() == (byte) 1;
        if (!isDobNull) {
            dateOfBirth = source.readString();
        }
        mobileNumber = source.readString();
        boolean isTelephNumNull = source.readByte() == (byte) 1;
        if (!isTelephNumNull) {
            telephoneNumber = source.readString();
        }
        houseNumber = source.readString();
        boolean isResComplxNull = source.readByte() == (byte) 1;
        if (!isResComplxNull) {
            residentialComplex = source.readString();
        }
        boolean isLandmarkNull = source.readByte() == (byte) 1;
        if (!isLandmarkNull) {
            landmark = source.readString();
        }
        area = source.readString();
        cityName = source.readString();
        boolean isStreetNull = source.readByte() == (byte) 1;
        if (!isStreetNull) {
            street = source.readString();
        }
        pincode = source.readString();
        newPaperSubscription = source.readByte() == (byte) 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        boolean isDobNull = dateOfBirth == null;
        dest.writeByte(isDobNull ? (byte) 1 : (byte) 0);
        if (!isDobNull) {
            dest.writeString(dateOfBirth);
        }
        dest.writeString(mobileNumber);
        boolean isTelephNumNull = telephoneNumber == null;
        dest.writeByte(isTelephNumNull ? (byte) 1 : (byte) 0);
        if (!isTelephNumNull) {
            dest.writeString(telephoneNumber);
        }
        dest.writeString(houseNumber);
        boolean isResComplxNull = residentialComplex == null;
        dest.writeByte(isResComplxNull ? (byte) 1 : (byte) 0);
        if (!isResComplxNull) {
            dest.writeString(residentialComplex);
        }
        boolean isLandMarkNull = landmark == null;
        dest.writeByte(isLandMarkNull ? (byte) 1 : (byte) 0);
        if (!isLandMarkNull) {
            dest.writeString(landmark);
        }
        dest.writeString(area);
        dest.writeString(cityName);
        boolean isStreetNull = street == null;
        dest.writeByte(isStreetNull ? (byte) 1 : (byte) 0);
        if (!isStreetNull) {
            dest.writeString(street);
        }
        dest.writeString(pincode);

        dest.writeByte((byte) (newPaperSubscription  ? 1 : 0));
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
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

    public boolean isNewPaperSubscription() {
        return newPaperSubscription;
    }
}
