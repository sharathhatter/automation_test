package com.bigbasket.mobileapp.model.product.gift;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Gift implements Parcelable {
    public static final Parcelable.Creator<Gift> CREATOR = new Parcelable.Creator<Gift>() {
        @Override
        public Gift createFromParcel(Parcel source) {
            return new Gift(source);
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };

    @SerializedName(Constants.ITEMS)
    private ArrayList<GiftItem> giftItems;
    @SerializedName(Constants.COMMON_MSG)
    private String commonMsg;
    @SerializedName(Constants.COMMON_MSG_NUM_CHARS)
    private int commonMsgNumChars;
    private int count;
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;
    @SerializedName(Constants.GIFT_SUMMARY)
    private ArrayList<String> giftSummaryMsg;
    @SerializedName(Constants.GIFT_LINK)
    private String giftLink;

    public Gift(Parcel source) {
        boolean isGiftNull = source.readByte() == (byte) 1;
        if (!isGiftNull) {
            this.giftItems = new ArrayList<>();
            source.readTypedList(giftItems, GiftItem.CREATOR);
        }
        this.commonMsg = source.readString();
        this.count = source.readInt();
        boolean isBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!isBaseImgUrlNull) {
            this.baseImgUrl = source.readString();
        }
        boolean isGiftMsgNull = source.readByte() == (byte) 1;
        if (!isGiftMsgNull) {
            this.giftSummaryMsg = new ArrayList<>();
            source.readStringList(this.giftSummaryMsg);
        }
        boolean isGiftLinkNull = source.readByte() == (byte) 1;
        if (!isGiftLinkNull) {
            this.giftLink = source.readString();
        }
        this.commonMsgNumChars = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isGiftNull = giftItems == null;
        dest.writeByte(isGiftNull ? (byte) 1 : (byte) 0);
        if (!isGiftNull) {
            dest.writeTypedList(giftItems);
        }
        dest.writeString(commonMsg);
        dest.writeInt(count);
        boolean isBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(isBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!isBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        boolean isGiftMsgNull = giftSummaryMsg == null;
        dest.writeByte(isGiftMsgNull ? (byte) 1 : (byte) 0);
        if (!isGiftMsgNull) {
            dest.writeStringList(giftSummaryMsg);
        }
        boolean isGiftLinkNull = giftLink == null;
        dest.writeByte(isGiftLinkNull ? (byte) 1 : (byte) 0);
        if (!isGiftLinkNull) {
            dest.writeString(giftLink);
        }
        dest.writeInt(commonMsgNumChars);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<GiftItem> getGiftItems() {
        return giftItems;
    }

    public String getCommonMsg() {
        return commonMsg == null ? "" : commonMsg;
    }

    public void setCommonMsg(String commonMsg) {
        if (commonMsg == null) {
            commonMsg = "";
        }
        this.commonMsg = commonMsg.trim();
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<String> getGiftSummaryMsg() {
        return giftSummaryMsg;
    }

    public String getGiftLink() {
        return giftLink;
    }

    public int getCommonMsgNumChars() {
        return commonMsgNumChars;
    }

    /**
    Returns Count & Total Price of gift-items
     */
    public Pair<Integer, Double> getGiftItemSelectedCountAndTotalPrice() {
        int numGiftItemsToWrap = 0;
        double giftItemTotal = 0;
        for (GiftItem giftItem : giftItems) {
            if (giftItem.getReservedQty() > 0) {
                numGiftItemsToWrap++;
                giftItemTotal += giftItem.getReservedQty() * giftItem.getGiftWrapCharge();
            }
        }
        return new Pair<>(numGiftItemsToWrap, giftItemTotal);
    }
}
