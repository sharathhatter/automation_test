package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.annotations.SerializedName;

public class OrderDetails implements Parcelable {

    public static final Parcelable.Creator<OrderDetails> CREATOR = new Parcelable.Creator<OrderDetails>() {
        @Override
        public OrderDetails createFromParcel(Parcel source) {
            return new OrderDetails(source);
        }

        @Override
        public OrderDetails[] newArray(int size) {
            return new OrderDetails[size];
        }
    };
    @SerializedName(Constants.PAYMENT_METHOD)
    private String paymentMethod;
    @SerializedName(Constants.TOTAL_ITEMS)
    private int totalItems;
    @SerializedName(Constants.SUB_TOTAL)
    private double subTotal;
    @SerializedName(Constants.DELIVERY_CHARGE)
    private double deliveryCharge;
    @SerializedName(Constants.FINAL_TOTAL)
    private double finalTotal;

    OrderDetails(Parcel source) {
        boolean wasFieldNull = source.readByte() == (byte) 1;
        if (!wasFieldNull) {
            paymentMethod = source.readString();
        }
        totalItems = source.readInt();
        subTotal = source.readDouble();
        deliveryCharge = source.readDouble();
        finalTotal = source.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasFieldNull = paymentMethod == null;
        dest.writeByte(wasFieldNull ? (byte) 1 : (byte) 0);
        if (!wasFieldNull) {
            dest.writeString(paymentMethod);
        }
        dest.writeInt(totalItems);
        dest.writeDouble(subTotal);
        dest.writeDouble(deliveryCharge);
        dest.writeDouble(finalTotal);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public String getFormattedFinalTotal() {
        return UIUtil.formatAsMoney(finalTotal);
    }
}
