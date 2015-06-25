package com.bigbasket.mobileapp.model.section;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class SectionUtil {
    private SectionUtil() {
    }

    private static Set<String> getSectionTypesToSplit() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add(Section.AD_IMAGE);
        stringSet.add(Section.MENU);
        return stringSet;
    }

    public static ArrayList<Section> preserveMemory(ArrayList<Section> sectionsToSplit) {
        if (sectionsToSplit == null || sectionsToSplit.size() == 0) return null;
        Set<String> sectionTypesThatCanBeSplitted = getSectionTypesToSplit();
        ArrayList<Section> modifiedSectionList = new ArrayList<>();
        for (Section originalSection : sectionsToSplit) {
            if (!TextUtils.isEmpty(originalSection.getSectionType())
                    && sectionTypesThatCanBeSplitted.contains(originalSection.getSectionType())) {
                ArrayList<Section> splittedSectionList = splitSection(originalSection);
                if (splittedSectionList != null && splittedSectionList.size() > 0) {
                    modifiedSectionList.addAll(splittedSectionList);
                }
            } else {
                modifiedSectionList.add(originalSection);
            }
        }
        return modifiedSectionList;
    }

    @Nullable
    private static ArrayList<Section> splitSection(Section section) {
        ArrayList<Section> splittedSectionList = new ArrayList<>();
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        if (sectionItems == null || sectionItems.size() == 0) return null;
        if (sectionItems.size() == 1) {
            splittedSectionList.add(section);
        } else {
            for (int i = 0; i < sectionItems.size(); i++) {
                Section newSection;
                ArrayList<SectionItem> singularSectionItemList = new ArrayList<>();
                singularSectionItemList.add(sectionItems.get(i));
                if (i == 0) {
                    newSection = new Section(section.getTitle(), section.getDescription(),
                            section.getSectionType(), singularSectionItemList, section.getMoreSectionItem());
                } else {
                    newSection = new Section(null, null, section.getSectionType(), singularSectionItemList, null);
                }
                splittedSectionList.add(newSection);
            }
        }
        return splittedSectionList;
    }
}
