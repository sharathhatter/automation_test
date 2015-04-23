package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.account.MemberSummary;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderSummary implements Parcelable {

    public static final Parcelable.Creator<OrderSummary> CREATOR = new Parcelable.Creator<OrderSummary>() {
        @Override
        public OrderSummary createFromParcel(Parcel source) {
            return new OrderSummary(source);
        }

        @Override
        public OrderSummary[] newArray(int size) {
            return new OrderSummary[size];
        }
    };
    @SerializedName(Constants.MEMBER_DETAILS)
    private MemberSummary memberSummary;
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;
    @SerializedName(Constants.PRODUCT_DETAILS)
    private ArrayList<CartItemList> cartItems;
    @SerializedName(Constants.SLOTS_INFO)
    private ArrayList<SlotGroup> slotGroups;
    @SerializedName(Constants.ORDER_DETAILS)
    private OrderDetails orderDetails;
    @SerializedName(Constants.CREDIT_DETAILS)
    private ArrayList<CreditDetails> creditDetails;
    @SerializedName(Constants.ANNOTATION_INFO)
    private ArrayList<AnnotationInfo> annotationInfos;

    public OrderSummary() {
        cartItems = new ArrayList<>();
        slotGroups = new ArrayList<>();
        creditDetails = new ArrayList<>();
        annotationInfos = new ArrayList<>();
    }

    OrderSummary(Parcel source) {
        memberSummary = source.readParcelable(OrderSummary.class.getClassLoader());
        boolean _wasBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!_wasBaseImgUrlNull) {
            baseImgUrl = source.readString();
        }
        cartItems = new ArrayList<>();
        slotGroups = new ArrayList<>();
        annotationInfos = new ArrayList<>();

        source.readTypedList(cartItems, CartItemList.CREATOR);
        source.readTypedList(slotGroups, SlotGroup.CREATOR);
        orderDetails = source.readParcelable(OrderSummary.class.getClassLoader());
        boolean _wasCreditDetailsNull = source.readByte() == (byte) 1;
        if (!_wasCreditDetailsNull) {
            creditDetails = new ArrayList<>();
            source.readTypedList(creditDetails, CreditDetails.CREATOR);
        }
        source.readTypedList(annotationInfos, AnnotationInfo.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(memberSummary, flags);
        boolean _wasBaseImgUrlNull = baseImgUrl == null;
        dest.writeByte(_wasBaseImgUrlNull ? (byte) 1 : (byte) 0);
        if (!_wasBaseImgUrlNull) {
            dest.writeString(baseImgUrl);
        }
        dest.writeTypedList(cartItems);
        dest.writeTypedList(slotGroups);
        dest.writeParcelable(orderDetails, flags);
        boolean _wasCreditDetailsNull = creditDetails == null;
        dest.writeByte(_wasCreditDetailsNull ? (byte) 1 : (byte) 0);
        if (!_wasCreditDetailsNull) {
            dest.writeTypedList(creditDetails);
        }
        dest.writeTypedList(annotationInfos);
    }

    public MemberSummary getMemberSummary() {
        return memberSummary;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public ArrayList<CartItemList> getCartItems() {
        return cartItems;
    }

    public ArrayList<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public ArrayList<CreditDetails> getCreditDetails() {
        return creditDetails;
    }

    public ArrayList<AnnotationInfo> getAnnotationInfos() {
        return annotationInfos;
    }
}
