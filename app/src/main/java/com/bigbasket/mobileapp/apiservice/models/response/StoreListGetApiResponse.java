package com.bigbasket.mobileapp.apiservice.models.response;


import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityShopsListData;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StoreListGetApiResponse {

    @SerializedName(Constants.STORE_LIST_RESP)
    public SpecialityShopsListData specialityShopsListData;

    @SerializedName(Constants.HEADER_SECTION)
    public Section headerSection;

    @SerializedName(Constants.BASE_IMG_URL)
    public String baseImageUrl;

    @SerializedName(Constants.STORE_LIST)
    public ArrayList<SpecialityStore> storeList;

    @SerializedName(Constants.HEADER_SEL)
    public int headerSelectedIndex;
}
