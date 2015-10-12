package com.bigbasket.mobileapp.model.specialityshops;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SpecialityShopsListData implements Parcelable {

    public static final Creator<SpecialityShopsListData> CREATOR = new Creator<SpecialityShopsListData>() {
        @Override
        public SpecialityShopsListData createFromParcel(Parcel in) {
            return new SpecialityShopsListData(in);
        }

        @Override
        public SpecialityShopsListData[] newArray(int size) {
            return new SpecialityShopsListData[size];
        }
    };

    @SerializedName(Constants.ITEMS)
    private ArrayList<SpecialityStore> storeList;

    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    @SerializedName(Constants.HEADER_SECTION)
    private Section headerSection;

    @SerializedName(Constants.HEADER_SEL)
    private int headerSelectedIndex;

    public SpecialityShopsListData(Parcel source) {
        boolean isStoreListNull = source.readByte() == (byte) 1;
        if (!isStoreListNull) {
            this.storeList = new ArrayList<>();
            source.readTypedList(storeList, SpecialityStore.CREATOR);
        }
        boolean isBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!isBaseImgUrlNull) {
            baseImgUrl = source.readString();
        }
        boolean isHeaderSectionNull = source.readByte() == (byte) 1;
        if (!isHeaderSectionNull) {
            headerSection = source.readParcelable(SpecialityShopsListData.class.getClassLoader());
        }
        headerSelectedIndex = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<SpecialityStore> getStoreList() {
        return storeList;
    }

    @Nullable
    public Section getHeaderSection() {
        return headerSection;
    }

    public int getHeaderSelectedIndex() {
        return headerSelectedIndex;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isStoreListNull = storeList == null;
        dest.writeByte(isStoreListNull ? (byte) 1 : (byte) 0);
        if (!isStoreListNull) {
            dest.writeTypedList(storeList);
        }
        boolean isBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(isBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!isBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        boolean isHeaderSectionNull = headerSection == null;
        dest.writeByte(isHeaderSectionNull ? (byte) 1 : (byte) 0);
        if (!isHeaderSectionNull) {
            dest.writeParcelable(headerSection, flags);
        }

    }
}
