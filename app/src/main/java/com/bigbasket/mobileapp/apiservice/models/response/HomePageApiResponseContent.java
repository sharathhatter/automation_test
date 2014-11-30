package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class HomePageApiResponseContent {

    public ArrayList<Section> sections;

    @SerializedName(Constants.DESTINATIONS_INFO)
    public ArrayList<DestinationInfo> destinationInfos;
}
