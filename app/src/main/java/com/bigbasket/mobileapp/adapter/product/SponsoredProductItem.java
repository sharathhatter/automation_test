package com.bigbasket.mobileapp.adapter.product;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;

import java.util.ArrayList;

/**
 * Created by muniraju on 16/12/15.
 */
public class SponsoredProductItem extends AbstractProductItem {

    private final SectionData sectionData;
    private final int sectionIndex;
    private boolean isSeen;

    public SponsoredProductItem(SectionData sectionData, int sectionIndex) {
        super(ProductListRecyclerAdapter.VIEW_TYPE_SPONSORED_PRODUCT_DATA);
        this.sectionData = sectionData;
        this.sectionIndex = sectionIndex;
    }

    public SectionData getSectionData() {
        return sectionData;
    }

    public Section getSection() {
        if(sectionData != null && sectionData.getSections() != null &&
                !sectionData.getSections().isEmpty()) {
            ArrayList<Section> sections = sectionData.getSections();
            if(sectionIndex >=0 && sectionIndex < sections.size()){
                return sections.get(sectionIndex);
            }
        }

        return null;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
