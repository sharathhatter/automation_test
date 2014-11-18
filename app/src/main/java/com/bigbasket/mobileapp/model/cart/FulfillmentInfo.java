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
        this.displayName = parcel.readString();
        this.tc1 = parcel.readString();
        this.tc2 = parcel.readString();
        this.fulfilledBy = parcel.readString();
        this.fulfillmentId = parcel.readString();
        this.fulfilledByInfoPage = parcel.readString();
        this.icon = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(tc1);
        dest.writeString(tc2);
        dest.writeString(fulfilledBy);
        dest.writeString(fulfillmentId);
        dest.writeString(fulfilledByInfoPage);
        dest.writeString(icon);
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

    public void setTc1(String tc1) {
        this.tc1 = tc1;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
