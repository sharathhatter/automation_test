package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

public interface SectionAware {
    public SectionData getSectionData();

    public void setSectionData(SectionData sectionData);

    public void setScreenName(String screenName);
}
