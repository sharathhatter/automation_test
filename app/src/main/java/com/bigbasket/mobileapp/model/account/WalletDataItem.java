package com.bigbasket.mobileapp.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class WalletDataItem implements Parcelable {


    public static final Parcelable.Creator<WalletDataItem> CREATOR = new Parcelable.Creator<WalletDataItem>() {
        @Override
        public WalletDataItem createFromParcel(Parcel source) {
            return new WalletDataItem(source);
        }

        @Override
        public WalletDataItem[] newArray(int size) {
            return new WalletDataItem[size];
        }
    };
    String type;
    @SerializedName(Constants.ORDER_NUMBER)
    String orderNumber;

    @SerializedName(Constants.ORDER_ID)
    private String orderId;

    @SerializedName(Constants.STARTING_BALANCE)
    private double startingBalance;

    @SerializedName(Constants.ENDING_BALANCE)
    private double endingBalance;
    private String date;
    private double amount;
    private String primary_reason;
    private String secondary_reason;

    public WalletDataItem(Parcel source) {
        orderId = source.readString();
        startingBalance = source.readDouble();
        endingBalance = source.readDouble();
        date = source.readString();
        amount = source.readDouble();
        primary_reason = source.readString();
        boolean isSecReasonNull = source.readByte() == (byte) 1;
        if (!isSecReasonNull) {
            secondary_reason = source.readString();
        }
        type = source.readString();
        orderNumber = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeDouble(startingBalance);
        dest.writeDouble(endingBalance);
        dest.writeString(date);
        dest.writeDouble(amount);
        dest.writeString(primary_reason);
        boolean isSecReasonNull = secondary_reason == null;
        dest.writeByte(isSecReasonNull ? (byte) 1 : (byte) 0);
        if (!isSecReasonNull) {
            dest.writeString(secondary_reason);
        }
        dest.writeString(type);
        dest.writeString(orderNumber);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getEndingBalance() {
        return endingBalance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPrimary_reason() {
        return primary_reason;
    }

    public String getSecondary_reason() {
        return secondary_reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "[OrderId=" + orderId + ", Amount=" +
                amount + " , Date=" + date + "Starting_balance" + startingBalance + "Ending_balance" + endingBalance +
                "Primary_reason" + primary_reason + "Secondary_reason" + secondary_reason + "]";
    }

}
