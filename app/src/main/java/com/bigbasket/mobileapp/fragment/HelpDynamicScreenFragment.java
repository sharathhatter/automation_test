package com.bigbasket.mobileapp.fragment;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.model.section.SectionData;

public class HelpDynamicScreenFragment extends DynamicScreenFragment {

    @Override
    protected void loadDynamicScreen() {

        SectionData sectionData = getSectionData();
        boolean hasOnlyOneSection = sectionData != null && sectionData.getSections() != null
                && sectionData.getSections().size() == 1;
        if (hasOnlyOneSection) {
            ViewGroup contentView = getContentView();
            if (contentView == null) return;

            // Render sections
            showProgressView();
            contentView.removeAllViews();

            View sectionView = getSectionView(true);
            if (sectionView != null) {
                ViewGroup.LayoutParams lp = sectionView.getLayoutParams();
                if (lp != null && lp instanceof LinearLayout.LayoutParams) {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ((LinearLayout.LayoutParams) lp).gravity = Gravity.CENTER;
                    sectionView.setLayoutParams(lp);
                }
                contentView.addView(sectionView);
            }
        } else {
            super.loadDynamicScreen();
        }
    }
}
