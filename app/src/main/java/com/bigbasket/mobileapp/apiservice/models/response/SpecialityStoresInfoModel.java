package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 10/11/15.
 */
public class SpecialityStoresInfoModel implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    public SpecialityStoresInfoModel(Parcel source) {
        this.storeCategory = source.readString();
        this.storeId = source.readString();
        this.storeUrl = source.readString();
        this.storeAdd1 = source.readString();
        this.storeAdd2 = source.readString();
        this.storeName = source.readString();
        this.storeLogo = source.readString();
        this.storeDesc = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(storeCategory);
        dest.writeString(storeId);
        dest.writeString(storeUrl);
        dest.writeString(storeAdd1);
        dest.writeString(storeAdd2);
        dest.writeString(storeName);
        dest.writeString(storeLogo);
        dest.writeString(storeDesc);
    }

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
}
