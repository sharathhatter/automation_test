package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

import java.util.HashMap;

public interface SubNavigationAware {
    void onSubNavigationRequested(Section section, SectionItem sectionItem, String baseImgUrl,
                                  HashMap<Integer, Renderer> rendererHashMap);
}
