package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SavedPrescription implements Parcelable {

    public static final Parcelable.Creator<SavedPrescription> CREATOR = new Parcelable.Creator<SavedPrescription>() {
        @Override
        public SavedPrescription createFromParcel(Parcel source) {
            return new SavedPrescription(source);
        }

        @Override
        public SavedPrescription[] newArray(int size) {
            return new SavedPrescription[size];
        }
    };
    @SerializedName(Constants.DATE_CREATED)
    private String dateCreated;
    @SerializedName(Constants.PATIENT_NAME)
    private String patientName;
    @SerializedName(Constants.DOCTOR_NAME)
    private String doctorName;

//    @SerializedName(Constants.PRESCRIPTION_IMG_URL)
//    private String prescriptionImageUrl;
    @SerializedName(Constants.PHARMA_PRESCRIPTION_ID)
    private int pharmaPrescriptionId;
    @SerializedName(Constants.PRESCRIPTION_NAME)
    private String prescriptionName;

    SavedPrescription(Parcel source) {
        this.dateCreated = source.readString();
        this.patientName = source.readString();
        this.doctorName = source.readString();
        this.pharmaPrescriptionId = source.readInt();
        //this.prescriptionImageUrl = source.readString();
        this.prescriptionName = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dateCreated);
        dest.writeString(this.patientName);
        dest.writeString(this.doctorName);
        dest.writeInt(this.pharmaPrescriptionId);
        //dest.writeString(this.prescriptionImageUrl);
        dest.writeString(this.prescriptionName);

    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public int getPharmaPrescriptionId() {
        return pharmaPrescriptionId;
    }

//    public String getPrescriptionImageUrl() {
//        return prescriptionImageUrl;
//    }
//
//    public void setPrescriptionImageUrl(String prescriptionImageUrl) {
//        this.prescriptionImageUrl = prescriptionImageUrl;
//    }

    public String getPrescriptionName() {
        return prescriptionName;
    }

}
