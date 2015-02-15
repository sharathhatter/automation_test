package com.bigbasket.mobileapp.model.navigation;

import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

public class SectionNavigationItem {
    private Section section;
    private SectionItem sectionItem;

    public SectionNavigationItem(Section section) {
        this.section = section;
    }

    public SectionNavigationItem(Section section, SectionItem sectionItem) {
        this.section = section;
        this.sectionItem = sectionItem;
    }

    public boolean isHeader() {
        return section != null && sectionItem == null;
    }

    public Section getSection() {
        return section;
    }

    public SectionItem getSectionItem() {
        return sectionItem;
    }

}
