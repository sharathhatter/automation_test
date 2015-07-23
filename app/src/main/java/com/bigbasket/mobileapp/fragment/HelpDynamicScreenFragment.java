package com.bigbasket.mobileapp.fragment;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;

public class HelpDynamicScreenFragment extends DynamicScreenFragment {

    @Override
    protected void loadDynamicScreen() {

        SectionData sectionData = getSectionData();
        boolean hasOnlyOneSection = sectionData != null && sectionData.getSections() != null
                && sectionData.getSections().size() == 1;
        if (hasOnlyOneSection) {
            // When only one section
            ViewGroup contentView = getContentView();
            if (contentView == null) return;

            // Render sections
            showProgressView();
            contentView.removeAllViews();

            View sectionView = getSectionView(true);
            if (sectionView != null) {
                if (contentView instanceof LinearLayout) {
                    ((LinearLayout) contentView).setGravity(Gravity.CENTER);
                }
                Section firstSection = sectionData.getSections().get(0);
                Renderer sectionRender = sectionData.getRenderersMap() != null ?
                        sectionData.getRenderersMap().get(firstSection.getRenderingId()) : null;
                if (sectionRender != null) {
                    contentView.setBackgroundColor(sectionRender.getNativeBkgColor());
                }
                contentView.addView(sectionView);
            }
        } else {
            // When multiple sections
            super.loadDynamicScreen();
        }
    }
}
