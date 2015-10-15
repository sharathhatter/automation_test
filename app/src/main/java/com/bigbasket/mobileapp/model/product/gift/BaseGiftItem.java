package com.bigbasket.mobileapp.model.product.gift;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.model.product.BaseProduct;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;

public class BaseGiftItem extends BaseProduct {
    public static final Parcelable.Creator<BaseGiftItem> CREATOR = new Parcelable.Creator<BaseGiftItem>() {
        @Override
        public BaseGiftItem createFromParcel(Parcel source) {
            return new BaseGiftItem(source);
        }

        @Override
        public BaseGiftItem[] newArray(int size) {
            return new BaseGiftItem[size];
        }
    };

    @SerializedName("product_id")
    @Expose
    private String productId;
    @Expose
    private ArrayList<String> messages;
    @SerializedName(Constants.QC_RESERVED_QUANTITY)
    @Expose
    private int reservedQty;
    @Expose
    private int quantity;
    @SerializedName(Constants.IS_PREWRAPPED)
    @Expose
    private boolean isPreWrapped;
    @SerializedName(Constants.IS_CONVERTIBLE_TO_NORMAL)
    @Expose
    private boolean isConvertibleToNormal;
    @SerializedName(Constants.GIFT_WRAP_CHARGE)
    @Expose
    private double giftWrapCharge;

    public BaseGiftItem(Parcel source) {
        super(source);
        this.productId = source.readString();
        boolean isMessagesNull = source.readByte() == (byte) 1;
        if (!isMessagesNull) {
            this.messages = new ArrayList<>();
            source.readStringList(this.messages);
        }
        this.reservedQty = source.readInt();
        this.quantity = source.readInt();
        this.isPreWrapped = source.readByte() == (byte) 1;
        this.isConvertibleToNormal = source.readByte() == (byte) 1;
        this.giftWrapCharge = source.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(productId);
        boolean isMessagesNull = messages == null;
        dest.writeByte(isMessagesNull ? (byte) 1 : (byte) 0);
        if (!isMessagesNull) {
            dest.writeStringList(messages);
        }
        dest.writeInt(reservedQty);
        dest.writeInt(quantity);
        dest.writeByte(isPreWrapped ? (byte) 1 : (byte) 0);
        dest.writeByte(isConvertibleToNormal ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.giftWrapCharge);
    }

    public String getProductId() {
        return productId;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public int getReservedQty() {
        return reservedQty;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isPreWrapped() {
        return isPreWrapped;
    }

    public boolean isConvertibleToNormal() {
        return isConvertibleToNormal;
    }

    public double getGiftWrapCharge() {
        return giftWrapCharge;
    }

    public void setReservedQty(int reservedQty) {
        this.reservedQty = reservedQty;
    }

    public void setMessage(String message) {
        this.messages = new ArrayList<>();
        if (TextUtils.isEmpty(message)) return;
        message = message.trim();
        Collections.addAll(messages, message.split("\n"));
    }
}
