package com.bigbasket.mobileapp.model.navigation;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

public class SectionNavigationItem {
    private boolean isSeparator;
    private boolean isHome;
    private Section section;
    private SectionItem sectionItem;

    public SectionNavigationItem(boolean isSeparator) {
        this.isSeparator = isSeparator;
    }

    public SectionNavigationItem(boolean isSeparator, boolean isHome, Section section, SectionItem sectionItem) {
        this.isSeparator = isSeparator;
        this.isHome = isHome;
        this.section = section;
        this.sectionItem = sectionItem;
    }

    public SectionNavigationItem(boolean isSeparator, Section section, SectionItem sectionItem) {
        this.isSeparator = isSeparator;
        this.section = section;
        this.sectionItem = sectionItem;
    }

    public boolean isSeparator() {
        return isSeparator;
    }

    public Section getSection() {
        return section;
    }

    public SectionItem getSectionItem() {
        return sectionItem;
    }

    public boolean isHome() {
        return isHome;
    }
}
