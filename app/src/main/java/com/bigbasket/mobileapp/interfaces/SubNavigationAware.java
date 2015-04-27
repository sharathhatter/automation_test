package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

public interface SubNavigationAware {
    public void onSubNavigationRequested(Section section, SectionItem sectionItem);
}
