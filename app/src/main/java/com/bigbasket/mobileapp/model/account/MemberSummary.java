package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class MemberSummary implements Parcelable {

    @SerializedName(Constants.MEMBER_NAME)
    private String memberName;

    private String address1;
    private String address2;

    @SerializedName(Constants.RESIDENTIAL_COMPLEX)
    private String residentialComplex;

    private String landmark;
    private String area;
    private String zipcode;
    private String mobile;
    private String city;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(memberName);
        dest.writeString(address1);
        dest.writeString(address2 == null ? "" : address2);
        dest.writeString(residentialComplex == null ? "" : residentialComplex);
        dest.writeString(landmark);
        dest.writeString(area);
        dest.writeString(zipcode);
        dest.writeString(mobile);
        dest.writeString(city);
    }

    MemberSummary(Parcel source) {
        memberName = source.readString();
        address1 = source.readString();
        address2 = source.readString();
        residentialComplex = source.readString();
        landmark = source.readString();
        area = source.readString();
        zipcode = source.readString();
        mobile = source.readString();
        city = source.readString();
    }

    public static final Parcelable.Creator<MemberSummary> CREATOR = new Parcelable.Creator<MemberSummary>() {
        @Override
        public MemberSummary createFromParcel(Parcel source) {
            return new MemberSummary(source);
        }

        @Override
        public MemberSummary[] newArray(int size) {
            return new MemberSummary[size];
        }
    };

    public String getMemberName() {
        return memberName;
    }

    public String getAddress() {
        String address = "";
        if (!TextUtils.isEmpty(address1)) {
            address += address1 + ", ";
        }
        if (!TextUtils.isEmpty(address2)) {
            address += address2 + ",\n";
        }
        if (!TextUtils.isEmpty(landmark)) {
            address += landmark + ", ";
        }
        if (!TextUtils.isEmpty(area)) {
            address += area + ", ";
        }
        if (!TextUtils.isEmpty(residentialComplex)) {
            address += residentialComplex + ", \n";
        }
        if (!TextUtils.isEmpty(city)) {
            address += city;
        }
        if (!TextUtils.isEmpty(zipcode)) {
            address += " - " + zipcode;
        }
        return address;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getResidentialComplex() {
        return residentialComplex;
    }

    public void setResidentialComplex(String residentialComplex) {
        this.residentialComplex = residentialComplex;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
