package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.shipments.SlotDisplay;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Order implements Parcelable {

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
    @SerializedName(Constants.VARIABLE_WEIGHT_MSG)
    public String variableWeightMsg;
    @SerializedName(Constants.VARIABLE_WEIGHT_LINK)
    public String variableWeightLink;
    @SerializedName(Constants.ORDER_ID)
    private String orderId;
    @SerializedName(Constants.ORDER_NUMBER)
    private String orderNumber;
    @SerializedName(Constants.SLOT_INFO)
    private SlotDisplay slotDisplay;
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
    @SerializedName(Constants.ORDER_STATE)
    private int orderState;
    @SerializedName(Constants.TOTAL_PAGE)
    private int totalCount;
    @SerializedName(Constants.VOUCHER)
    private String voucher;
    @SerializedName(Constants.PAYMENT_METHOD)
    private String paymentMethod;
    private Address address;
    @SerializedName(Constants.CAN_PAY)
    private boolean canPay;

    public Order(Parcel parcel) {
        this.orderId = parcel.readString();
        this.orderNumber = parcel.readString();
        this.slotDisplay = parcel.readParcelable(OrderInvoice.class.getClassLoader());
        this.itemsCount = parcel.readInt();
        this.orderStatus = parcel.readString();
        this.orderValue = parcel.readString();
        this.fulfillmentInfo = parcel.readParcelable(FulfillmentInfo.class.getClassLoader());
        this.orderType = parcel.readString();
        this.orderState = parcel.readInt();
        this.totalCount = parcel.readInt();
        boolean _wasVoucherNull = parcel.readByte() == (byte) 1;
        if (!_wasVoucherNull)
            this.voucher = parcel.readString();
        this.paymentMethod = parcel.readString();
        boolean _wasAddressNull = parcel.readByte() == (byte) 1;
        if (!_wasAddressNull) {
            address = parcel.readParcelable(Address.class.getClassLoader());
        }
        this.canPay = parcel.readByte() == (byte) 1;
        boolean _wasVariableWeightMsgNull = parcel.readByte() == (byte) 0;
        if (!_wasVariableWeightMsgNull) {
            variableWeightMsg = parcel.readString();
        }
        boolean _wasVariableWeightLinkNull = parcel.readByte() == (byte) 0;
        if (!_wasVariableWeightLinkNull) {
            variableWeightLink = parcel.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orderId);
        dest.writeString(this.orderNumber);
        dest.writeParcelable(this.slotDisplay, flags);
        dest.writeInt(this.itemsCount);
        dest.writeString(this.orderStatus);
        dest.writeString(this.orderValue != null ? this.orderValue : "0");
        dest.writeParcelable(this.fulfillmentInfo, flags);
        dest.writeString(this.orderType);
        dest.writeInt(this.orderState);
        dest.writeInt(this.totalCount);
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
        dest.writeByte(canPay ? (byte) 1 : (byte) 0);
        boolean _wasVariableWeightMsgNull = variableWeightMsg == null;
        dest.writeByte(_wasVariableWeightMsgNull ? (byte) 0 : (byte) 1);
        if (!_wasVariableWeightMsgNull) {
            dest.writeString(variableWeightMsg);
        }
        boolean _wasVariableWeightLinkNull = variableWeightLink == null;
        dest.writeByte(_wasVariableWeightLinkNull ? (byte) 0 : (byte) 1);
        if (!_wasVariableWeightLinkNull) {
            dest.writeString(variableWeightLink);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public FulfillmentInfo getFulfillmentInfo() {
        return fulfillmentInfo;
    }

    public SlotDisplay getSlotDisplay() {
        return slotDisplay;
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

    public int getOrderState() {
        return orderState;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public boolean canPay() {
        return canPay;
    }


    public String getVariableWeightMsg() {
        return variableWeightMsg;
    }

    public String getVariableWeightLink() {
        return variableWeightLink;
    }
}
