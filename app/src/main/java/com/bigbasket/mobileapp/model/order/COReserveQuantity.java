package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class COReserveQuantity implements Parcelable {

    public static final Parcelable.Creator<COReserveQuantity> CREATOR = new Parcelable.Creator<COReserveQuantity>() {
        @Override
        public COReserveQuantity createFromParcel(Parcel source) {
            return new COReserveQuantity(source);
        }

        @Override
        public COReserveQuantity[] newArray(int size) {
            return new COReserveQuantity[size];
        }
    };
    private boolean status = false;
    @SerializedName(Constants.QC_HAS_VALIDATION_ERRORS)
    private boolean qcHasErrors = false;
    private int qc_len;
    @SerializedName(Constants.QC_ORDER_ID)
    private int potentialOrderId;
    @SerializedName(Constants.QC_VALIDATION_ERROR_DATA)
    private List<QCErrorData> qcErrorData;

    public COReserveQuantity(Parcel source) {
        status = source.readByte() == (byte) 1;
        qcHasErrors = source.readByte() == (byte) 1;
        qc_len = source.readInt();
        potentialOrderId = source.readInt();
        boolean _wasQcErrorDataNull = source.readByte() == (byte) 1;
        if (!_wasQcErrorDataNull) {
            qcErrorData = new ArrayList<>();
            source.readTypedList(qcErrorData, QCErrorData.CREATOR);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(status ? (byte) 1 : (byte) 0);
        dest.writeByte(qcHasErrors ? (byte) 1 : (byte) 0);
        dest.writeInt(qc_len);
        dest.writeInt(potentialOrderId);
        boolean _wasQcErrorDataNull = qcErrorData == null;
        dest.writeByte(_wasQcErrorDataNull ? (byte) 1 : (byte) 0);
        if (!_wasQcErrorDataNull) {
            dest.writeTypedList(qcErrorData);
        }
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isQcHasErrors() {
        return qcHasErrors;
    }

    public int getPotentialOrderId() {
        return potentialOrderId;
    }

    public List<QCErrorData> getQcErrorData() {
        return qcErrorData;
    }

    public int getQc_len() {
        return qc_len;
    }

    public void setQc_len(int qc_len) {
        this.qc_len = qc_len;
    }
}
