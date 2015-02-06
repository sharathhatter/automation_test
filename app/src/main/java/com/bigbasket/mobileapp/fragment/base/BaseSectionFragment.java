package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.SectionView;

public abstract class BaseSectionFragment extends BaseFragment {

    protected SectionData mSectionData;

    @Nullable
    public View getSectionView() {
        SectionView sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData);
        return sectionView.getView();
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