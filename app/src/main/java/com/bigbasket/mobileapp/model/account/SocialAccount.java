package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SocialAccount implements Parcelable {
    public static final String FB = "fb";
    public static final String GP = "gp";

    public static ArrayList<String> getSocialLoginTypes() {
        ArrayList<String> socialAccountTypes = new ArrayList<>();
        socialAccountTypes.add(FB);
        socialAccountTypes.add(GP);
        return socialAccountTypes;
    }

    private String email;
    private String displayName;
    private String gender;
    private String link;
    private String id;
    private String photo;
    private boolean verified;

    @SerializedName(Constants.REF_CODE)
    private String refCode;

    @SerializedName(Constants.FIRSTNAME)
    private String firstName;

    @SerializedName(Constants.LASTNAME)
    private String lastName;

    public SocialAccount(String email, String displayName, String gender, String link, String id,
                         boolean verified, String firstName, String lastName, String photo) {
        this.email = email;
        this.displayName = displayName;
        this.gender = gender;
        this.link = link;
        this.id = id;
        this.verified = verified;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGender() {
        return gender;
    }

    public String getLink() {
        return link;
    }

    public String getId() {
        return id;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(displayName);

        boolean wasGenderNull = gender == null;
        dest.writeByte(wasGenderNull ? (byte) 1 : (byte) 0);
        if (!wasGenderNull) {
            dest.writeString(gender);
        }

        boolean wasLinkNull = link == null;
        dest.writeByte(wasLinkNull ? (byte) 1 : (byte) 0);
        if (!wasLinkNull) {
            dest.writeString(link);
        }

        boolean wasIdNull = id == null;
        dest.writeByte(wasIdNull ? (byte) 1 : (byte) 0);
        if (!wasIdNull) {
            dest.writeString(id);
        }

        dest.writeByte(verified ? (byte) 1 : (byte) 0);

        boolean wasFirstNameNull = firstName == null;
        dest.writeByte(wasFirstNameNull ? (byte) 1 : (byte) 0);
        if (!wasFirstNameNull) {
            dest.writeString(firstName);
        }

        boolean wasLastNameNull = lastName == null;
        dest.writeByte(wasLastNameNull ? (byte) 1 : (byte) 0);
        if (!wasLastNameNull) {
            dest.writeString(lastName);
        }

        boolean wasPhotoNull = photo == null;
        dest.writeByte(wasPhotoNull ? (byte) 1 : (byte) 0);
        if (!wasPhotoNull) {
            dest.writeString(photo);
        }
    }

    public SocialAccount(Parcel source) {
        email = source.readString();
        displayName = source.readString();

        boolean wasGenderNull = source.readByte() == (byte) 1;
        if (!wasGenderNull) {
            gender = source.readString();
        }

        boolean wasLinkNull = source.readByte() == (byte) 1;
        if (!wasLinkNull) {
            link = source.readString();
        }

        boolean wasIdNull = source.readByte() == (byte) 1;
        if (!wasIdNull) {
            id = source.readString();
        }

        verified = source.readByte() == (byte) 1;

        boolean wasFirstNameNull = source.readByte() == (byte) 1;
        if (!wasFirstNameNull) {
            firstName = source.readString();
        }

        boolean wasLastNameNull = source.readByte() == (byte) 1;
        if (!wasLastNameNull) {
            lastName = source.readString();
        }

        boolean wasPhotoNull = source.readByte() == (byte) 1;
        if (!wasPhotoNull) {
            photo = source.readString();
        }
    }

    public static final Parcelable.Creator<SocialAccount> CREATOR = new Parcelable.Creator<SocialAccount>() {
        @Override
        public SocialAccount createFromParcel(Parcel source) {
            return new SocialAccount(source);
        }

        @Override
        public SocialAccount[] newArray(int size) {
            return new SocialAccount[size];
        }
    };
}
