package com.bigbasket.mobileapp.model.cart;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;


public class FulfillmentInfo implements Parcelable {

    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;

    @SerializedName(Constants.TC2)
    private String tc2;

    @SerializedName(Constants.FULFILLED_BY)
    private String fulfilledBy;

    @SerializedName(Constants.FULFILLMENT_ID)
    private String fulfillmentId;

    @SerializedName(Constants.FULFILLED_BY_INFO_PAGE)
    private String fulfilledByInfoPage;

    @SerializedName(Constants.TC1)
    private String tc1;

    @SerializedName(Constants.ICON_URL)
    private String icon;


    public FulfillmentInfo(String displayName, String tc2, String fulfilledBy, String fulfillmentId,
                           String fulfilledByInfoPage, String tc1, String icon) {
        this.displayName = displayName;
        this.tc2 = tc2;
        this.fulfilledBy = fulfilledBy;
        this.fulfillmentId = fulfillmentId;
        this.fulfilledByInfoPage = fulfilledByInfoPage;
        this.tc1 = tc1;
        this.icon = icon;
    }

    public FulfillmentInfo() {
    }

    public FulfillmentInfo(JSONObject jsonObject) throws JSONException {

        this.displayName = jsonObject.getString(Constants.DISPLAY_NAME);
        this.tc1 = jsonObject.getString(Constants.TC1);
        this.tc2 = jsonObject.getString(Constants.TC2);
        this.fulfilledBy = jsonObject.getString(Constants.FULFILLED_BY);
        this.fulfillmentId = jsonObject.getString(Constants.FULFILLMENT_ID);
        this.fulfilledByInfoPage = jsonObject.getString(Constants.FULFILLED_BY_INFO_PAGE);
        this.icon = jsonObject.getString(Constants.ICON_URL);
    }

    public FulfillmentInfo(Parcel parcel) {

        boolean wasDisplayNull = parcel.readByte() == (byte) 1;
        if (!wasDisplayNull) {
            displayName = parcel.readString();
        }
        boolean wasTc1Null = parcel.readByte() == (byte) 1;
        if (!wasTc1Null) {
            this.tc1 = parcel.readString();
        }

        boolean wasTc2 = parcel.readByte() == (byte) 1;
        if (!wasTc2) {
            this.tc2 = parcel.readString();
        }

        boolean wasfulfilledBy = parcel.readByte() == (byte) 1;
        if (!wasfulfilledBy) {
            this.fulfilledBy = parcel.readString();
        }

        boolean wasfulfillmentId = parcel.readByte() == (byte) 1;
        if (!wasfulfillmentId) {
            this.fulfillmentId = parcel.readString();
        }

        boolean wasfulfilledByInfoPage = parcel.readByte() == (byte) 1;
        if (!wasfulfilledByInfoPage) {
            this.fulfilledByInfoPage = parcel.readString();
        }

        boolean wasIconNull = parcel.readByte() == (byte) 1;
        if (!wasIconNull) {
            this.icon = parcel.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasDisplayNull = displayName == null;
        dest.writeByte(wasDisplayNull ? (byte) 1 : (byte) 0);
        if (!wasDisplayNull) {
            dest.writeString(displayName);
        }

        boolean wasTc1Null = tc1 == null;
        dest.writeByte(wasTc1Null ? (byte) 1 : (byte) 0);
        if (!wasTc1Null) {
            dest.writeString(tc1);
        }

        boolean wasTc2 = tc2 == null;
        dest.writeByte(wasTc2 ? (byte) 1 : (byte) 0);
        if (!wasTc2) {
            dest.writeString(tc2);
        }

        boolean wasfulfilledBy = fulfilledBy == null;
        dest.writeByte(wasfulfilledBy ? (byte) 1 : (byte) 0);
        if (!wasfulfilledBy) {
            dest.writeString(fulfilledBy);
        }

        boolean wasfulfillmentId = fulfillmentId == null;
        dest.writeByte(wasfulfillmentId ? (byte) 1 : (byte) 0);
        if (!wasfulfillmentId) {
            dest.writeString(fulfillmentId);
        }

        boolean wasfulfilledByInfoPage = fulfilledByInfoPage == null;
        dest.writeByte(wasfulfilledByInfoPage ? (byte) 1 : (byte) 0);
        if (!wasfulfilledByInfoPage) {
            dest.writeString(fulfilledByInfoPage);
        }

        boolean wasIconNull = icon == null;
        dest.writeByte(wasIconNull ? (byte) 1 : (byte) 0);
        if (!wasIconNull) {
            dest.writeString(icon);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FulfillmentInfo> CREATOR = new Parcelable.Creator<FulfillmentInfo>() {
        @Override
        public FulfillmentInfo[] newArray(int size) {
            return new FulfillmentInfo[size];
        }

        @Override
        public FulfillmentInfo createFromParcel(Parcel source) {
            return new FulfillmentInfo(source);
        }
    };

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTc2() {
        return tc2;
    }

    public String getFulfilledBy() {
        return fulfilledBy;
    }

    public String getFulfillmentId() {
        return fulfillmentId;
    }

    public String getFulfilledByInfoPage() {
        return fulfilledByInfoPage;
    }

    public String getTc1() {
        return tc1;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
