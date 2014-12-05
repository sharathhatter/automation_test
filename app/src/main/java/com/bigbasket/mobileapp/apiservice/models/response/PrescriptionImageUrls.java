package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 4/12/14.
 */
public class PrescriptionImageUrls {

    @SerializedName(Constants.PRESCRIPTION_IMAGE_URLS)
    public ArrayList<String> arrayListPrescriptionImages;
}
