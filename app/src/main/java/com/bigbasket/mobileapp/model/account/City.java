package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

public class City implements Parcelable {
    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
    private String name;
    private int id;
    private String phone;

    public City(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public City(Parcel source) {
        name = source.readString();
        id = source.readInt();
        boolean isPhoneNull = source.readByte() == (byte) 1;
        if (!isPhoneNull) {
            phone = source.readString();
        }
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);

        boolean isPhoneNull = phone == null;
        dest.writeByte(isPhoneNull ? (byte) 1 : (byte) 0);
        if (!isPhoneNull) {
            dest.writeString(phone);
        }
    }
}
