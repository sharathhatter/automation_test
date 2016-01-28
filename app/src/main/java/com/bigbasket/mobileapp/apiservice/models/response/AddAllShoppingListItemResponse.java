package com.bigbasket.mobileapp.apiservice.models.response;

import android.os.Parcel;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class AddAllShoppingListItemResponse extends OldApiResponseWithCart {

    @SerializedName(Constants.CART_INFO)
    public HashMap<String, Integer> cartInfo;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if(cartInfo == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(cartInfo.size());
            Set<Map.Entry<String, Integer>> entrySet = cartInfo.entrySet();
            for (Map.Entry<String, Integer> e : entrySet) {
                dest.writeString(e.getKey());
                dest.writeInt(e.getValue());
            }
        }
    }

    public AddAllShoppingListItemResponse() {
    }

    protected AddAllShoppingListItemResponse(Parcel in) {
        super(in);
        int size = in.readInt();
        cartInfo = new HashMap<>(size);
        while (size > 0) {
            String key = in.readString();
            cartInfo.put(key, in.readInt());
        }
    }

    public static final Creator<AddAllShoppingListItemResponse> CREATOR = new Creator<AddAllShoppingListItemResponse>() {
        public AddAllShoppingListItemResponse createFromParcel(Parcel source) {
            return new AddAllShoppingListItemResponse(source);
        }

        public AddAllShoppingListItemResponse[] newArray(int size) {
            return new AddAllShoppingListItemResponse[size];
        }
    };
}
