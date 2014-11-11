package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
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

//    public Order(String orderId, String orderNumber, FulfillmentInfo fulfillmentInfo) {
//        this.orderId = orderId;
//        this.orderNumber = orderNumber;
//        this.fulfillmentInfo = fulfillmentInfo;
//    }


    public Order(String orderId, String orderNumber, String deliveryDate, int itemsCount, String orderStatus,
                 String orderValue) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.deliveryDate = deliveryDate;
        this.itemsCount = itemsCount;
        this.orderStatus = orderStatus;
        this.orderValue = orderValue;
    }

    public Order(Parcel parcel) {
        this.orderId = parcel.readString();
        this.orderNumber = parcel.readString();
        this.deliveryDate = parcel.readString();
        this.itemsCount = parcel.readInt();
        this.orderStatus = parcel.readString();
        this.orderValue = parcel.readString(); // todo null handling
        this.fulfillmentInfo = parcel.readParcelable(Order.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orderId);
        dest.writeString(this.orderNumber);
        dest.writeString(this.deliveryDate);
        dest.writeInt(this.itemsCount);
        dest.writeString(this.orderStatus);
        dest.writeString(this.orderValue);
        dest.writeParcelable(this.fulfillmentInfo, flags);
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

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public FulfillmentInfo getFulfillmentInfo() {
        return fulfillmentInfo;
    }

    public void setFulfillmentInfo(FulfillmentInfo fulfillmentInfo) {
        this.fulfillmentInfo = fulfillmentInfo;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(String orderValue) {
        this.orderValue = orderValue;
    }
}
