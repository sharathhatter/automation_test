package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

public interface SectionAware {
    SectionData getSectionData();

    void setSectionData(SectionData sectionData);

    void setScreenName(String screenName);
}
