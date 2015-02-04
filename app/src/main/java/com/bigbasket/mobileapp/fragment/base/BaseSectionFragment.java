package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.SectionView;

public abstract class BaseSectionFragment extends BaseFragment {

    protected SectionData mSectionData;

    public void displaySections(LinearLayout mainLayout) {
        SectionView sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData);
        sectionView.displaySections(mainLayout);
    }

    protected void retainSectionState(Bundle outState) {
        if (mSectionData != null) {
            outState.putParcelable(Constants.SECTIONS, mSectionData);
        }
    }

    protected boolean tryRestoreSectionState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSectionData = savedInstanceState.getParcelable(Constants.SECTIONS);
            return mSectionData != null && mSectionData.getSections() != null &&
                    mSectionData.getSections().size() > 0;
        }
        return false;
    }
}