package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class PrePaymentParamsResponse implements Parcelable {

    public static final Parcelable.Creator<PrePaymentParamsResponse> CREATOR = new Parcelable.Creator<PrePaymentParamsResponse>() {
        @Override
        public PrePaymentParamsResponse[] newArray(int size) {
            return new PrePaymentParamsResponse[size];
        }

        @Override
        public PrePaymentParamsResponse createFromParcel(Parcel source) {
            return new PrePaymentParamsResponse(source);
        }
    };

    @SerializedName(Constants.POST_PARAMS)
    public HashMap<String, String> postParams;

    public PrePaymentParamsResponse(Parcel parcel) {
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

    public PrePaymentParamsResponse(HashMap<String, String> postParams) {
        this.postParams = postParams;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
