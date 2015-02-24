package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Order implements Parcelable {

    @SerializedName(Constants.ORDER_ID)
    private String orderId;

    @SerializedName(Constants.ORDER_NUMBER)
    private String orderNumber;

    @SerializedName(Constants.DELIVERY_DATE)
    private String deliveryDate;

    @SerializedName(Constants.ITEMS_COUNT)
    private int itemsCount;

    @SerializedName(Constants.ORDER_STATUS)
    private String orderStatus;

    @SerializedName(Constants.ORDER_VALUE)
    private String orderValue;

    @SerializedName(Constants.FULFILLMENT_INFO)
    private FulfillmentInfo fulfillmentInfo;

    @SerializedName(Constants.ORDER_TYPE)
    private String orderType;

    @SerializedName(Constants.VOUCHER)
    private String voucher;

    @SerializedName(Constants.PAYMENT_METHOD)
    private String paymentMethod;

    private Address address;

    public Order(Parcel parcel) {
        this.orderId = parcel.readString();
        this.orderNumber = parcel.readString();
        this.deliveryDate = parcel.readString();
        this.itemsCount = parcel.readInt();
        this.orderStatus = parcel.readString();
        this.orderValue = parcel.readString();
        this.fulfillmentInfo = parcel.readParcelable(Order.class.getClassLoader());
        this.orderType = parcel.readString();
        boolean _wasVoucherNull = parcel.readByte() == (byte) 1;
        if (!_wasVoucherNull)
            this.voucher = parcel.readString();
        this.paymentMethod = parcel.readString();
        boolean _wasAddressNull = parcel.readByte() == (byte) 1;
        if (!_wasAddressNull) {
            address = parcel.readParcelable(Order.class.getClassLoader());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orderId);
        dest.writeString(this.orderNumber);
        dest.writeString(this.deliveryDate);
        dest.writeInt(this.itemsCount);
        dest.writeString(this.orderStatus);
        dest.writeString(this.orderValue != null ? this.orderValue : "0");
        dest.writeParcelable(this.fulfillmentInfo, flags);
        dest.writeString(this.orderType);
        boolean _wasVoucherNull = voucher == null;
        dest.writeByte(_wasVoucherNull ? (byte) 1 : (byte) 0);
        if (!_wasVoucherNull)
            dest.writeString(this.voucher);
        dest.writeString(this.paymentMethod);
        boolean _wasAddressNull = address == null;
        dest.writeByte(_wasAddressNull ? (byte) 1 : (byte) 0);
        if (!_wasAddressNull) {
            dest.writeParcelable(address, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }

        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }
    };

    public String getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public FulfillmentInfo getFulfillmentInfo() {
        return fulfillmentInfo;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getOrderValue() {
        return orderValue;
    }

    public Address getAddress() {
        return address;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getVoucher() {
        return voucher;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
