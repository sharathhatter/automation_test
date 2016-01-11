package com.bigbasket.mobileapp.model.ads;

import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.model.section.Section;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by muniraju on 11/01/16.
 */
public class SponsoredAds extends GetDynamicPageApiResponse {

    //Page start position in terms of visible windows
    @SerializedName("page_start")
    private int pageStart;
    //offset for each sponsored ad item interms of umber of products
    @SerializedName("page_offset")
    private int pageOffset;


    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageOffset() {
        if(pageOffset == 0) {
            //Default page offset
            pageOffset = 8;
        }
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }

    public ArrayList<Section> getSections() {
        return sectionData != null ? sectionData.getSections() : null;
    }
}
