package com.bigbasket.mobileapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class NameValuePair implements Parcelable {
    public static final Parcelable.Creator<NameValuePair> CREATOR = new Parcelable.Creator<NameValuePair>() {

        @Override
        public NameValuePair createFromParcel(Parcel source) {
            return new NameValuePair(source);
        }

        @Override
        public NameValuePair[] newArray(int size) {
            return new NameValuePair[size];
        }
    };
    private String name;
    private String value;

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public NameValuePair(Parcel source) {
        name = source.readString();
        value = source.readString();
    }

    public static HashMap<String, String> toMap(ArrayList<NameValuePair> nameValuePairs) {
        HashMap<String, String> map = new HashMap<>();
        for (NameValuePair nameValuePair : nameValuePairs) {
            map.put(nameValuePair.getName(), nameValuePair.getValue());
        }
        return map;
    }

    @Nullable
    public static String buildNavigationContext(ArrayList<NameValuePair> nameValuePairs) {
        StringBuilder ncBuilder = new StringBuilder("pl");
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (nameValuePair.getName() == null
                    || nameValuePair.getValue() == null) continue;
            if (nameValuePair.getName().equalsIgnoreCase("is_express")) {
                ncBuilder.append(".is_express");
            } else {
                ncBuilder.append(".");
                ncBuilder.append(nameValuePair.getValue());
            }
        }
        return ncBuilder.toString();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
    }
}
