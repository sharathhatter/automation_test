package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class City implements Parcelable {
    private String name;
    private int id;

    public City(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static int getCurrentCityIndex(ArrayList<City> cities, String currentCityName) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getName().equals(currentCityName)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
    }

    public City(Parcel source) {
        name = source.readString();
        id = source.readInt();
    }

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
}
