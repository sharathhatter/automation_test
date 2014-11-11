package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;


public class PharmaPrescriptionInfo implements Parcelable {

    @SerializedName(Constants.MESSAGE_OBJ)
    private MessageInfo msgInfo;

    public PharmaPrescriptionInfo(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public PharmaPrescriptionInfo(Parcel parcel) {
        msgInfo = parcel.readParcelable(PharmaPrescriptionInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(msgInfo, flags);
    }

    public static final Parcelable.Creator<PharmaPrescriptionInfo> CREATOR = new Parcelable.Creator<PharmaPrescriptionInfo>() {
        @Override
        public PharmaPrescriptionInfo createFromParcel(Parcel source) {
            return new PharmaPrescriptionInfo(source);
        }

        @Override
        public PharmaPrescriptionInfo[] newArray(int size) {
            return new PharmaPrescriptionInfo[size];
        }
    };

    public MessageInfo getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }


    /*
    @SerializedName(Constants.MESSAGE)
    private String message;

    @SerializedName(Constants.PRESCRIPTION)
    private ArrayList<PrescriptionModel> prescriptionModelArrayList;

    @SerializedName(Constants.PHARMA_TC)
    private String PharmaTC;

    private boolean prescriptionSelected;
    private boolean _wasPrescriptionNull;


    @Override
    public int describeContents() {
        return 0;
    }

    public PharmaPrescriptionInfo(Parcel source){
        message = source.readString();
        PharmaTC = source.readString();
        _wasPrescriptionNull = source.readByte() == (byte) 1;
        if (!_wasPrescriptionNull) {
            prescriptionModelArrayList = new ArrayList<>();
            source.readTypedList(prescriptionModelArrayList, PrescriptionModel.CREATOR);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(PharmaTC);
        if(prescriptionModelArrayList == null){
            this._wasPrescriptionNull = true;
        }
        dest.writeByte(_wasPrescriptionNull ? (byte) 1 : (byte) 0);
        if (prescriptionModelArrayList != null) {
            dest.writeTypedList(prescriptionModelArrayList);
        }

    }

    public static final Parcelable.Creator<PharmaPrescriptionInfo> CREATOR = new Parcelable.Creator<PharmaPrescriptionInfo>() {
    @Override
    public PharmaPrescriptionInfo createFromParcel(Parcel source) {
        return new PharmaPrescriptionInfo(source);
    }

    @Override
    public PharmaPrescriptionInfo[] newArray(int size) {
        return new PharmaPrescriptionInfo[size];
    }
    };

//    public PharmaPrescriptionInfo(String message,
//                                  String productGroupId,
//                                  String productGroupName,
//                                  ArrayList<SavedPrescription> savedPrescription) {
//        this.message = message;
//        this.fullfillmentId = productGroupId;
//        this.productGroupName = productGroupName;
//        this.savedPrescription = savedPrescription;
//    }

    public boolean isPrescriptionSelected() {
        return prescriptionSelected;
    }

    public void setPrescriptionSelected(boolean prescriptionSelected) {
        this.prescriptionSelected = prescriptionSelected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

//    public String getFullfillmentId() {
//        return fullfillmentId;
//    }
//
//    public void setFullfillmentId(String fullfillmentId) {
//        this.fullfillmentId = fullfillmentId;
//    }
//
//    public String getProductGroupName() {
//        return productGroupName;
//    }
//
//    public void setProductGroupName(String productGroupName) {
//        this.productGroupName = productGroupName;
//    }

    public ArrayList<PrescriptionModel> getPrescriptionModelArrayList() {
        return prescriptionModelArrayList;
    }

    public void setPrescriptionModelArrayList(ArrayList<PrescriptionModel> prescriptionModelArrayList) {
        this.prescriptionModelArrayList = prescriptionModelArrayList;
    }

    public String getPharmaTC() {
        return PharmaTC;
    }

    public void setPharmaTC(String pharmaTC) {
        PharmaTC = pharmaTC;
    }
    //    public ArrayList<SavedPrescription> getSavedPrescription() {
//        return savedPrescription;
//    }
//
//    public void setSavedPrescription(ArrayList<SavedPrescription> savedPrescription) {
//        this.savedPrescription = savedPrescription;
//    }

*/
}
