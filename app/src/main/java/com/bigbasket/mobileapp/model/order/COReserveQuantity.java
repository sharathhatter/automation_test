package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class COReserveQuantity implements Parcelable {

    private boolean status = false;
    private boolean qcHasErrors = false;
    private int qc_len;
    private int orderId;
    private List<QCErrorData> QCErrorData = new ArrayList<>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(status ? (byte) 1 : (byte) 0);
        dest.writeByte(qcHasErrors ? (byte) 1 : (byte) 0);
        dest.writeInt(qc_len);
        dest.writeInt(orderId);

    }

    public COReserveQuantity(Parcel source) {

    }

    public COReserveQuantity() {
    }

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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isQcHasErrors() {
        return qcHasErrors;
    }

    public void setQcHasErrors(boolean qcHasErrors) {
        this.qcHasErrors = qcHasErrors;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public List<QCErrorData> getQCErrorData() {
        return QCErrorData;
    }

    public void setQCErrorData(List<QCErrorData> QCErrorData) {
        this.QCErrorData = QCErrorData;
    }

    public int getQc_len() {
        return qc_len;
    }

    public void setQc_len(int qc_len) {
        this.qc_len = qc_len;
    }
}
