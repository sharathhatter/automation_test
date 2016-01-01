package com.bigbasket.mobileapp.apiservice.models.request;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class ValidatePaymentRequest implements Parcelable {
    @Nullable
    private String txnId;
    @Nullable
    private String orderId;
    @Nullable
    private String potentialOrderId;
    @Nullable
    private String finalTotal;
    /**
     * When user is pressing back during order-payment, this is passed null, so that the order gets converted to Cash on Delivery
     */
    @Nullable
    private String selectedPaymentMethod;
    private boolean isPayNow;
    private boolean isWallet;

    public ValidatePaymentRequest(@Nullable String txnId) {
        this.txnId = txnId;
    }

    public ValidatePaymentRequest(@Nullable String txnId, @Nullable String orderId,
                                  @Nullable String potentialOrderId,
                                  @Nullable String selectedPaymentMethod) {
        this(txnId);
        this.orderId = orderId;
        this.potentialOrderId = potentialOrderId;
        this.selectedPaymentMethod = selectedPaymentMethod;
    }

    protected ValidatePaymentRequest(Parcel in) {
        boolean wasTxnIdNull = in.readByte() == 1;
        if (!wasTxnIdNull) {
            txnId = in.readString();
        }
        boolean wasOrderIdNull = in.readByte() == 1;
        if (!wasOrderIdNull) {
            orderId = in.readString();
        }
        boolean wasPotentialOrderIdNull = in.readByte() == 1;
        if (!wasPotentialOrderIdNull) {
            potentialOrderId = in.readString();
        }
        boolean wasFinalTotalNull = in.readByte() == 1;
        if (!wasFinalTotalNull) {
            finalTotal = in.readString();
        }
        boolean wasSelectedPaymentMethodNull = in.readByte() == 1;
        if (!wasSelectedPaymentMethodNull) {
            selectedPaymentMethod = in.readString();
        }
        isPayNow = in.readByte() != 0;
        isWallet = in.readByte() != 0;
    }

    public static final Creator<ValidatePaymentRequest> CREATOR = new Creator<ValidatePaymentRequest>() {
        @Override
        public ValidatePaymentRequest createFromParcel(Parcel in) {
            return new ValidatePaymentRequest(in);
        }

        @Override
        public ValidatePaymentRequest[] newArray(int size) {
            return new ValidatePaymentRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isFieldNull = txnId == null;
        dest.writeByte(isFieldNull ? (byte) 1 : (byte) 0);
        if (!isFieldNull) {
            dest.writeString(txnId);
        }
        isFieldNull = orderId == null;
        dest.writeByte(isFieldNull ? (byte) 1 : (byte) 0);
        if (!isFieldNull) {
            dest.writeString(orderId);
        }
        isFieldNull = potentialOrderId == null;
        dest.writeByte(isFieldNull ? (byte) 1 : (byte) 0);
        if (!isFieldNull) {
            dest.writeString(potentialOrderId);
        }
        isFieldNull = finalTotal == null;
        dest.writeByte(isFieldNull ? (byte) 1 : (byte) 0);
        if (!isFieldNull) {
            dest.writeString(finalTotal);
        }
        isFieldNull = selectedPaymentMethod == null;
        dest.writeByte(isFieldNull ? (byte) 1: (byte) 0);
        if (!isFieldNull) {
            dest.writeString(selectedPaymentMethod);
        }
        dest.writeByte((byte) (isPayNow ? 1 : 0));
        dest.writeByte((byte) (isWallet ? 1 : 0));
    }

    @Nullable
    public String getTxnId() {
        return txnId;
    }

    @Nullable
    public String getOrderId() {
        return orderId;
    }

    @Nullable
    public String getPotentialOrderId() {
        return potentialOrderId;
    }

    @Nullable
    public String getFinalTotal() {
        return finalTotal;
    }

    @Nullable
    public String getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    public boolean isPayNow() {
        return isPayNow;
    }

    public boolean isWallet() {
        return isWallet;
    }

    public void setTxnId(@Nullable String txnId) {
        this.txnId = txnId;
    }

    public void setFinalTotal(@Nullable String finalTotal) {
        this.finalTotal = finalTotal;
    }

    public void setIsPayNow(boolean isPayNow) {
        this.isPayNow = isPayNow;
    }

    public void setIsWallet(boolean isWallet) {
        this.isWallet = isWallet;
    }
}
