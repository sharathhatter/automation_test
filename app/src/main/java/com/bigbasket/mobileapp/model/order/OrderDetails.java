package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OrderDetails implements Parcelable {

    @SerializedName(Constants.PAYMENT_METHOD)
    private String paymentMethod;

    private String paymentMethodDisplay;

    @SerializedName(Constants.TOTAL_ITEMS)
    private int totalItems;

    @SerializedName(Constants.SUB_TOTAL)
    private double subTotal;

    @SerializedName(Constants.DELIVERY_CHARGE)
    private double deliveryCharge;

    @SerializedName(Constants.FINAL_TOTAL)
    private double finalTotal;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(paymentMethod);
        dest.writeString(paymentMethodDisplay);
        dest.writeInt(totalItems);
        dest.writeDouble(subTotal);
        dest.writeDouble(deliveryCharge);
        dest.writeDouble(finalTotal);
    }

    OrderDetails(Parcel source) {
        paymentMethod = source.readString();
        paymentMethodDisplay = source.readString();
        totalItems = source.readInt();
        subTotal = source.readDouble();
        deliveryCharge = source.readDouble();
        finalTotal = source.readDouble();
    }

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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getPaymentMethodDisplay() {
        return paymentMethodDisplay;
    }

    public void setPaymentMethodDisplay(String paymentMethodDisplay) {
        this.paymentMethodDisplay = paymentMethodDisplay;
    }
}
