package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.interfaces.SectionAware;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.SectionView;

public abstract class BaseSectionFragment extends BaseFragment implements SectionAware {

    private SectionData mSectionData;
    private String mScreenName;

    @Nullable
    public View getSectionView(boolean isHelp) {
        SectionView sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData,
                mScreenName, isHelp);
        return sectionView.getView();
    }

    @Nullable
    public RecyclerView getSectionRecylerView(ViewGroup parent) {
        SectionView sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData, mScreenName);
        return sectionView.getRecyclerView(parent);
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

    @Override
    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }

    @Override
    public SectionData getSectionData() {
        return mSectionData;
    }

    @Override
    public void setSectionData(SectionData sectionData) {
        mSectionData = sectionData;
    }
}