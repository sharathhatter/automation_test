package com.bigbasket.mobileapp.model.account;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Address implements Parcelable {

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

    public Address(boolean isDefault, String id, String contactNum, String addressNickName,
                   String firstName, String lastName, String houseNumber, String street,
                   String residentialComplex, String landmark, String area, String cityName,
                   String pincode) {
        this.isDefault = isDefault;
        this.id = id;
        this.contactNum = contactNum;
        this.addressNickName = addressNickName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.houseNumber = houseNumber;
        this.street = street;
        this.residentialComplex = residentialComplex;
        this.landmark = landmark;
        this.area = area;
        this.cityName = cityName;
        this.pincode = pincode;
    }

    public Address(EditText editTextAddressNick, EditText editTextFirstName, EditText editTextLastName,
                   EditText editTextContactNum, EditText editTextHouseNum, EditText editTextStreetName,
                   EditText editTextResidentialComplex, EditText editTextLandmark, CheckBox chkIsAddrDefault,
                   EditText editTextArea, EditText editTextCity, EditText editTextPincode) {
        this.addressNickName = editTextAddressNick.getText() != null ? editTextAddressNick.getText().toString() : "";
        this.isDefault = chkIsAddrDefault.isChecked();
        this.contactNum = editTextContactNum.getText() != null ? editTextContactNum.getText().toString() : "";
        this.firstName = editTextFirstName.getText() != null ? editTextFirstName.getText().toString() : "";
        this.lastName = editTextLastName.getText() != null ? editTextLastName.getText().toString() : "";
        this.houseNumber = editTextHouseNum.getText() != null ? editTextHouseNum.getText().toString() : "";
        this.street = editTextStreetName.getText() != null ? editTextStreetName.getText().toString() : "";
        this.residentialComplex = editTextResidentialComplex.getText() != null ? editTextResidentialComplex.getText().toString() : "";
        this.landmark = editTextLandmark.getText() != null ? editTextLandmark.getText().toString() : "";
        this.area = editTextArea.getText() != null ? editTextArea.getText().toString() : "";
        this.cityName = editTextCity.getText() != null ? editTextCity.getText().toString() : "";
        this.pincode = editTextPincode.getText() != null ? editTextPincode.getText().toString() : "";
    }

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
    }

    @Override
    public int describeContents() {
        return 0;
    }

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

    public static String getAddressIdFromPreferences(Context context) {
        // Using globals, since get-setExtra in Intent won't work in all scenarios as back-button handling is incorrect right now
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefer != null) {
            return prefer.getString(Constants.MEMBER_ADDRESS_ID, null);
        }
        return null;
    }

    public static void setAddressIdInPreferences(Context context, String addressId) {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefer != null) {
            SharedPreferences.Editor editor = prefer.edit();
            if (addressId == null || TextUtils.isEmpty(addressId)) {
                editor.remove(Constants.MEMBER_ADDRESS_ID);
            } else {
                editor.putString(Constants.MEMBER_ADDRESS_ID, addressId);
            }
            editor.commit();
        }
    }

    public static void clearAddressIdFromPreferences(Context context) {
        setAddressIdInPreferences(context, null);
    }
}
