package com.bigbasket.mobileapp.model.slot;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class SelectedSlotType implements Parcelable {
    @SerializedName(Constants.FULFILLMENT_ID)
    private String fulfillmentId;

    @SerializedName(Constants.SLOT_ID)
    private String slotId;

    @SerializedName(Constants.SLOT_DATE)
    private String slotDate;

    public SelectedSlotType(String fulfillmentId, String slotId, String slotDate) {
        this.fulfillmentId = fulfillmentId;
        this.slotId = slotId;
        this.slotDate = slotDate;
    }

    public SelectedSlotType(Parcel source) {
        this.fulfillmentId = source.readString();
        this.slotId = source.readString();
        this.slotDate = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fulfillmentId);
        dest.writeString(this.slotId);
        dest.writeString(this.slotDate);
    }

    public static final Parcelable.Creator<SelectedSlotType> CREATOR = new Parcelable.Creator<SelectedSlotType>() {
        @Override
        public SelectedSlotType createFromParcel(Parcel source) {
            return new SelectedSlotType(source);
        }

        @Override
        public SelectedSlotType[] newArray(int size) {
            return new SelectedSlotType[size];
        }
    };

    public String getFulfillmentId() {
        return fulfillmentId;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getSlotDate() {
        return slotDate;
    }
}
