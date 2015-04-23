package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class OrderInvoiceDetails extends OrderDetails implements Parcelable {

    public static final Parcelable.Creator<OrderInvoiceDetails> CREATOR = new Parcelable.Creator<OrderInvoiceDetails>() {
        @Override
        public OrderInvoiceDetails createFromParcel(Parcel source) {
            return new OrderInvoiceDetails(source);
        }

        @Override
        public OrderInvoiceDetails[] newArray(int size) {
            return new OrderInvoiceDetails[size];
        }
    };
    @SerializedName("food_value")
    private double foodValue;
    @SerializedName(Constants.DELIVERY_CHARGES)
    private double deliveryCharge;
    @SerializedName("total")
    private double total;
    @SerializedName("total_savings")
    private double totalSavings;
    @SerializedName("vat_info")
    private double vatValue;
    @SerializedName("order_status")
    private String orderStatus;

    public OrderInvoiceDetails(Parcel source) {
        super(source);
        this.foodValue = source.readDouble();
        this.deliveryCharge = source.readDouble();
        this.total = source.readDouble();
        this.totalSavings = source.readDouble();
        this.vatValue = source.readDouble();
        this.orderStatus = source.readString();
    }

    public double getFoodValue() {
        return foodValue;
    }

    @Override
    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public double getTotal() {
        return total;
    }

    public double getTotalSavings() {
        return totalSavings;
    }

    public double getVatValue() {
        return vatValue;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(foodValue);
        dest.writeDouble(deliveryCharge);
        dest.writeDouble(total);
        dest.writeDouble(totalSavings);
        dest.writeDouble(vatValue);
        dest.writeString(orderStatus);
    }
}
