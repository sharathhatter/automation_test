package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 3/9/14.
 */
public class PrescriptionModel implements Parcelable {

    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;

    @SerializedName(Constants.FULFILLMENT_ID)
    private String filfillmentId;

    public PrescriptionModel(Parcel source) {
        displayName = source.readString();
        filfillmentId = source.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(filfillmentId);
    }


    public static final Parcelable.Creator<PrescriptionModel> CREATOR = new Parcelable.Creator<PrescriptionModel>() {
        @Override
        public PrescriptionModel createFromParcel(Parcel source) {
            return new PrescriptionModel(source);
        }

        @Override
        public PrescriptionModel[] newArray(int size) {
            return new PrescriptionModel[size];
        }
    };

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilfillmentId() {
        return filfillmentId;
    }

    public void setFilfillmentId(String filfillmentId) {
        this.filfillmentId = filfillmentId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
