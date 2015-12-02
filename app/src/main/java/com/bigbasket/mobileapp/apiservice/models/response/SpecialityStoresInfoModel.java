package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 10/11/15.
 */
public class SpecialityStoresInfoModel implements Parcelable {

    public static final Parcelable.Creator<SpecialityStoresInfoModel> CREATOR = new Parcelable.Creator<SpecialityStoresInfoModel>() {
        @Override
        public SpecialityStoresInfoModel createFromParcel(Parcel source) {
            return new SpecialityStoresInfoModel(source);
        }

        @Override
        public SpecialityStoresInfoModel[] newArray(int size) {
            return new SpecialityStoresInfoModel[size];
        }
    };
    @SerializedName(Constants.STORE_CATEGORY)
    public String storeCategory;
    @SerializedName(Constants.STORE_ID)
    public String storeId;
    @SerializedName(Constants.STORE_URL)
    public String storeUrl;
    @SerializedName(Constants.STORE_ADD1)
    public String storeAdd1;
    @SerializedName(Constants.STORE_ADD2)
    public String storeAdd2;
    @SerializedName(Constants.STORE_NAME)
    public String storeName;
    @SerializedName(Constants.STORE_LOGO)
    public String storeLogo;
    @SerializedName(Constants.STORE_DESC)
    public String storeDesc;

    public SpecialityStoresInfoModel(Parcel source) {
        boolean isStoreCatNull = source.readByte() == (byte) 1;
        if (!isStoreCatNull) {
            this.storeCategory = source.readString();
        }

        boolean isStoreIdNull = source.readByte() == (byte) 1;
        if (!isStoreIdNull) {
            this.storeId = source.readString();
        }

        boolean isStoreUrlNull = source.readByte() == (byte) 1;
        if (!isStoreUrlNull) {
            this.storeUrl = source.readString();
        }
        boolean isStoreAdd1Null = source.readByte() == (byte) 1;
        if (!isStoreAdd1Null) {
            this.storeAdd1 = source.readString();
        }

        boolean isStoreAdd2Null = source.readByte() == (byte) 1;
        if (!isStoreAdd2Null) {
            this.storeAdd2 = source.readString();
        }

        boolean isStoreNameNull = source.readByte() == (byte) 1;
        if (!isStoreNameNull) {
            this.storeName = source.readString();
        }

        boolean isStoreLogoNull = source.readByte() == (byte) 1;
        if (!isStoreLogoNull) {
            this.storeLogo = source.readString();
        }

        boolean isStoreDescNull = source.readByte() == (byte) 1;
        if (!isStoreDescNull) {
            this.storeDesc = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        boolean isStoreCatNull = storeCategory == null;
        dest.writeByte(isStoreCatNull ? (byte) 1 : (byte) 0);
        if (!isStoreCatNull) {
            dest.writeString(storeCategory);
        }

        boolean isStoreIdNull = storeId == null;
        dest.writeByte(isStoreIdNull ? (byte) 1 : (byte) 0);
        if (!isStoreIdNull) {
            dest.writeString(storeId);
        }

        boolean isStoreUrlNull = storeUrl == null;
        dest.writeByte(isStoreUrlNull ? (byte) 1 : (byte) 0);
        if (!isStoreUrlNull) {
            dest.writeString(storeUrl);
        }

        boolean isStoreAdd1Null = storeAdd1 == null;
        dest.writeByte(isStoreAdd1Null ? (byte) 1 : (byte) 0);
        if (!isStoreAdd1Null) {
            dest.writeString(storeAdd1);
        }

        boolean isStoreAdd2Null = storeAdd2 == null;
        dest.writeByte(isStoreAdd2Null ? (byte) 1 : (byte) 0);
        if (!isStoreAdd2Null) {
            dest.writeString(storeAdd2);
        }

        boolean isStoreNameNull = storeName == null;
        dest.writeByte(isStoreNameNull ? (byte) 1 : (byte) 0);
        if (!isStoreNameNull) {
            dest.writeString(storeName);
        }

        boolean isStoreLogoNull = storeLogo == null;
        dest.writeByte(isStoreLogoNull ? (byte) 1 : (byte) 0);
        if (!isStoreLogoNull) {
            dest.writeString(storeLogo);
        }

        boolean isStoreDescNull = storeDesc == null;
        dest.writeByte(isStoreDescNull ? (byte) 1 : (byte) 0);
        if (!isStoreDescNull) {
            dest.writeString(storeDesc);
        }
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }

    public String getStoreAdd1() {
        return storeAdd1;
    }

    public void setStoreAdd1(String storeAdd1) {
        this.storeAdd1 = storeAdd1;
    }

    public String getStoreAdd2() {
        return storeAdd2;
    }

    public void setStoreAdd2(String storeAdd2) {
        this.storeAdd2 = storeAdd2;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLogo() {
        return storeLogo;
    }

    public void setStoreLogo(String storeLogo) {
        this.storeLogo = storeLogo;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public String getStoreCategory() {

        return storeCategory;
    }

    public void setStoreCategory(String storeCategory) {
        this.storeCategory = storeCategory;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

}
