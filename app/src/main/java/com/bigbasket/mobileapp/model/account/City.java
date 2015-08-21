package com.bigbasket.mobileapp.model.account;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;

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

    public City(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public City(Parcel source) {
        name = source.readString();
        id = source.readInt();
    }

    public City(Cursor cursor) {
        this.name = cursor.getString(cursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_CITY));
        this.id = cursor.getInt(cursor.getColumnIndex(AreaPinInfoAdapter.COLUMN_CITY_ID));
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
    }
}
