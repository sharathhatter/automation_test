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
    private double lbx;
    private double lby;
    private double ubx;
    private double uby;

    public City(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public City(Parcel source) {
        name = source.readString();
        id = source.readInt();
        lbx = source.readDouble();
        lby = source.readDouble();
        ubx = source.readDouble();
        uby = source.readDouble();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getLbx() {
        return lbx;
    }

    public double getLby() {
        return lby;
    }

    public double getUbx() {
        return ubx;
    }

    public double getUby() {
        return uby;
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
        dest.writeDouble(lbx);
        dest.writeDouble(lby);
        dest.writeDouble(ubx);
        dest.writeDouble(uby);
    }
}
