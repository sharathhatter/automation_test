package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.account.MemberSummary;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.shipments.SlotDisplay;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderInvoice implements Parcelable {

    public static final Parcelable.Creator<OrderInvoice> CREATOR = new Parcelable.Creator<OrderInvoice>() {
        @Override
        public OrderInvoice createFromParcel(Parcel source) {
            return new OrderInvoice(source);
        }

        @Override
        public OrderInvoice[] newArray(int size) {
            return new OrderInvoice[size];
        }
    };
    @SerializedName(Constants.ORDER_NUMBER)
    private String orderNumber;
    @SerializedName(Constants.MEMBER_DETAILS)
    private MemberSummary memberSummary;
    @SerializedName(Constants.SLOT_INFO)
    private SlotDisplay slotDisplay;
    @SerializedName(Constants.INVOICE_NUMBER)
    private String invoiceNumber;
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;
    @SerializedName(Constants.ITEMS)
    private ArrayList<CartItemList> cartItems;
    @SerializedName(Constants.ORDER_DETAILS)
    private OrderInvoiceDetails orderInvoiceDetails;
    @SerializedName("order_modifications")
    private ArrayList<OrderModification> orderModifications;
    @SerializedName(Constants.CREDIT_DETAILS)
    private ArrayList<CreditDetails> creditDetails;
    @SerializedName(Constants.FULFILLMENT_INFO)
    private FulfillmentInfo fulfillmentInfo;

    public OrderInvoice(Parcel source) {
        this.orderNumber = source.readString();
        this.memberSummary = source.readParcelable(OrderInvoice.class.getClassLoader());
        this.slotDisplay = source.readParcelable(OrderInvoice.class.getClassLoader());
        this.invoiceNumber = source.readString();
        boolean _wasBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!_wasBaseImgUrlNull) {
            this.baseImgUrl = source.readString();
        }
        this.cartItems = new ArrayList<>();
        source.readTypedList(this.cartItems, CartItemList.CREATOR);
        this.orderInvoiceDetails = source.readParcelable(OrderInvoice.class.getClassLoader());
        this.orderModifications = new ArrayList<>();
        source.readTypedList(this.orderModifications, OrderModification.CREATOR);
        boolean _wasCreditDetailsNull = source.readByte() == (byte) 1;
        if (!_wasCreditDetailsNull) {
            this.creditDetails = new ArrayList<>();
            source.readTypedList(this.creditDetails, CreditDetails.CREATOR);
        }
        this.fulfillmentInfo = source.readParcelable(OrderInvoice.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderNumber);
        dest.writeParcelable(memberSummary, flags);
        dest.writeParcelable(slotDisplay, flags);
        dest.writeString(invoiceNumber);
        boolean _wasBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(_wasBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!_wasBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        dest.writeTypedList(cartItems);
        dest.writeParcelable(orderInvoiceDetails, flags);
        dest.writeTypedList(orderModifications);
        boolean _wasCreditDetailsNull = creditDetails == null;
        dest.writeByte(_wasCreditDetailsNull ? (byte) 1 : (byte) 0);
        if (!_wasCreditDetailsNull) {
            dest.writeTypedList(creditDetails);
        }
        dest.writeParcelable(fulfillmentInfo, flags);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public MemberSummary getMemberSummary() {
        return memberSummary;
    }

    public SlotDisplay getSlot() {
        return slotDisplay;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public ArrayList<CartItemList> getCartItems() {
        return cartItems;
    }

    public OrderInvoiceDetails getOrderInvoiceDetails() {
        return orderInvoiceDetails;
    }

    public ArrayList<OrderModification> getOrderModifications() {
        return orderModifications;
    }

    public ArrayList<CreditDetails> getCreditDetails() {
        return creditDetails;
    }

    public FulfillmentInfo getFulfillmentInfo() {
        return fulfillmentInfo;
    }
}
