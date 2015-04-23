package com.bigbasket.mobileapp.model.order;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class VoucherApplied implements Parcelable {

    public static final Parcelable.Creator<VoucherApplied> CREATOR = new Parcelable.Creator<VoucherApplied>() {
        @Override
        public VoucherApplied createFromParcel(Parcel source) {
            return new VoucherApplied(source);
        }

        @Override
        public VoucherApplied[] newArray(int size) {
            return new VoucherApplied[size];
        }
    };
    @SerializedName(Constants.APPLIED_VOUCHER)
    private String voucherCode;
    private boolean isApplied;

    public VoucherApplied(Parcel source) {
        voucherCode = source.readString();
        isApplied = source.readByte() == (byte) 1;
    }

    public VoucherApplied(String voucherCode) {
        this(voucherCode, false);
    }

    public VoucherApplied(String voucherCode, boolean isApplied) {
        this.voucherCode = voucherCode;
        this.isApplied = isApplied;
    }

    public static void saveToPreference(ArrayList<VoucherApplied> voucherAppliedList, Context context) {
        Gson gson = new Gson();
        String voucherAppliedJson = gson.toJson(voucherAppliedList);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.APPLIED_VOUCHER, voucherAppliedJson);
        editor.commit();
    }

    public static HashMap<String, Boolean> toMap(ArrayList<VoucherApplied> voucherAppliedList) {
        HashMap<String, Boolean> map = new HashMap<>();
        for (VoucherApplied voucherApplied : voucherAppliedList) {
            map.put(voucherApplied.voucherCode, voucherApplied.isApplied);
        }
        return map;
    }

    public static ArrayList<VoucherApplied> readFromPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String voucherAppliedJson = sharedPreferences.getString(Constants.APPLIED_VOUCHER, null);
        if (!TextUtils.isEmpty(voucherAppliedJson)) {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<VoucherApplied>>() {
            }.getType();
            return gson.fromJson(voucherAppliedJson, collectionType);
        }
        return null;
    }

    public static void clearFromPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.APPLIED_VOUCHER);
        editor.commit();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(voucherCode);
        dest.writeByte(isApplied ? (byte) 1 : (byte) 0);
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public boolean isApplied() {
        return isApplied;
    }

    public void setApplied(boolean isApplied) {
        this.isApplied = isApplied;
    }
}
