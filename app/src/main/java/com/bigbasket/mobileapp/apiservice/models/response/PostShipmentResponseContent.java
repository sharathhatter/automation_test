package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.wallet.WalletOption;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostShipmentResponseContent implements Parcelable {

    @SerializedName(Constants.VOUCHERS)
    public ArrayList<ActiveVouchers> activeVouchersArrayList;

    @SerializedName(Constants.PAYMENT_TYPES)
    public ArrayList<PaymentType> paymentTypes;

    @SerializedName(Constants.EVOUCHER_CODE)
    public String evoucherCode;

    @SerializedName(Constants.CREDIT_DETAILS)
    public ArrayList<CreditDetails> creditDetails;

    @SerializedName(Constants.ORDER_DETAILS)
    public OrderDetails orderDetails;

    @SerializedName(Constants.NEW_FLOW_URL)
    public String newFlowUrl;

    @SerializedName(Constants.WALLET_OPTION)
    public WalletOption walletOption;

    public PostShipmentResponseContent() {
    }

    protected PostShipmentResponseContent(Parcel in) {
        boolean wasActiveVouchersArrayListNull = in.readByte() == (byte) 1;
        if (!wasActiveVouchersArrayListNull) {
            activeVouchersArrayList = in.createTypedArrayList(ActiveVouchers.CREATOR);
        }
        boolean wasPaymentTypesNull = in.readByte() == (byte) 1;
        if (!wasPaymentTypesNull) {
            paymentTypes = in.createTypedArrayList(PaymentType.CREATOR);
        }
        boolean wasEvoucherCodeNull = in.readByte() == (byte) 1;
        if (!wasEvoucherCodeNull) {
            evoucherCode = in.readString();
        }
        boolean wasCreditDetailsNull = in.readByte() == (byte) 1;
        if (!wasCreditDetailsNull) {
            creditDetails = in.createTypedArrayList(CreditDetails.CREATOR);
        }
        boolean wasOrderDetailsNull = in.readByte() == (byte) 1;
        if (!wasOrderDetailsNull) {
            orderDetails = in.readParcelable(OrderDetails.class.getClassLoader());
        }
        boolean wasNewFlowUrlNull = in.readByte() == (byte) 1;
        if (!wasNewFlowUrlNull) {
            newFlowUrl = in.readString();
        }
        boolean wasWalletOptionNull = in.readByte() == (byte) 1;
        if (!wasWalletOptionNull) {
            walletOption = in.readParcelable(WalletOption.class.getClassLoader());
        }
    }

    public static final Creator<PostShipmentResponseContent> CREATOR = new Creator<PostShipmentResponseContent>() {
        @Override
        public PostShipmentResponseContent createFromParcel(Parcel in) {
            return new PostShipmentResponseContent(in);
        }

        @Override
        public PostShipmentResponseContent[] newArray(int size) {
            return new PostShipmentResponseContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasActiveVouchersArrayListNull = activeVouchersArrayList == null;
        dest.writeByte(wasActiveVouchersArrayListNull ? (byte) 1 : (byte) 0);
        if (!wasActiveVouchersArrayListNull) {
            dest.writeTypedList(activeVouchersArrayList);
        }
        boolean wasPaymentTypesNull = paymentTypes == null;
        dest.writeByte(wasPaymentTypesNull ? (byte) 1 : (byte) 0);
        if (!wasPaymentTypesNull) {
            dest.writeTypedList(paymentTypes);
        }
        boolean wasEvoucherCodeNull = evoucherCode == null;
        dest.writeByte(wasEvoucherCodeNull ? (byte) 1 : (byte) 0);
        if (!wasEvoucherCodeNull) {
            dest.writeString(evoucherCode);
        }
        boolean wasCreditDetailsNull = creditDetails == null;
        dest.writeByte(wasCreditDetailsNull ? (byte) 1 : (byte) 0);
        if (!wasCreditDetailsNull) {
            dest.writeTypedList(creditDetails);
        }
        boolean wasOrderDetailsNull = orderDetails == null;
        dest.writeByte(wasOrderDetailsNull ? (byte) 1 : (byte) 0);
        if (!wasOrderDetailsNull) {
            dest.writeParcelable(orderDetails, flags);
        }
        boolean wasNewFlowUrlNull = newFlowUrl == null;
        dest.writeByte(wasNewFlowUrlNull ? (byte) 1 : (byte) 0);
        if (!wasNewFlowUrlNull) {
            dest.writeString(newFlowUrl);
        }
        boolean wasWalletOptionNull = walletOption == null;
        dest.writeByte(wasWalletOptionNull ? (byte) 1 : (byte) 0);
        if (!wasWalletOptionNull) {
            dest.writeParcelable(walletOption, flags);
        }

    }
}
