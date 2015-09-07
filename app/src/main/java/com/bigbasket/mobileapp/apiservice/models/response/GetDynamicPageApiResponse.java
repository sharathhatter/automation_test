package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class GetDynamicPageApiResponse {

    @SerializedName(Constants.SECTION_INFO)
    public SectionData sectionData;

    @SerializedName(Constants.CACHE_DURATION)
    public int cacheDuration = 10000000;
}
