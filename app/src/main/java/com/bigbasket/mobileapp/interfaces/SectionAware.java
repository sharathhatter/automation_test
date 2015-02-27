package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

public interface SectionAware {
    public void setSectionData(SectionData sectionData);
    public SectionData getSectionData();
    public void setScreenName(String screenName);
}
