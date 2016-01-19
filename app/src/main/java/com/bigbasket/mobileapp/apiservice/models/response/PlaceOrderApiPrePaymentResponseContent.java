package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class PlaceOrderApiPrePaymentResponseContent extends PlaceOrderReponseContent {
    public static final Parcelable.Creator<PlaceOrderApiPrePaymentResponseContent> CREATOR = new Parcelable.Creator<PlaceOrderApiPrePaymentResponseContent>() {
        @Override
        public PlaceOrderApiPrePaymentResponseContent[] newArray(int size) {
            return new PlaceOrderApiPrePaymentResponseContent[size];
        }

        @Override
        public PlaceOrderApiPrePaymentResponseContent createFromParcel(Parcel source) {
            return new PlaceOrderApiPrePaymentResponseContent(source);
        }
    };

    @SerializedName(Constants.POST_PARAMS)
    public HashMap<String, String> postParams;


    public PlaceOrderApiPrePaymentResponseContent(Parcel parcel) {
        super(parcel);
        int postParamsSize = parcel.readInt();
        postParams = new HashMap<>(postParamsSize);
        for (int i = 0; i < postParamsSize; i++) {
            String valKey = parcel.readString();
            boolean wasValueNull = parcel.readByte() == (byte) 1;
            if (!wasValueNull) {
                postParams.put(valKey, parcel.readString());
            } else {
                postParams.put(valKey, null);
            }
        }
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (postParams != null && !postParams.isEmpty()) {
            dest.writeInt(postParams.size());
            for (Map.Entry<String, String> valEntry : postParams.entrySet()) {
                dest.writeString(valEntry.getKey());
                boolean wasValueNull = valEntry.getValue() == null;
                dest.writeByte(wasValueNull ? (byte) 1 : (byte) 0);
                if (!wasValueNull) {
                    dest.writeString(valEntry.getValue());
                }
            }
        } else {
            dest.writeInt(0);
        }
    }
}
