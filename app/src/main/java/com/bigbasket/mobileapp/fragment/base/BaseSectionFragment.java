package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.interfaces.SectionAware;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.SectionView;
import com.newrelic.agent.android.instrumentation.Trace;

import java.util.ArrayList;

public abstract class BaseSectionFragment extends BaseFragment implements SectionAware {

    private SectionData mSectionData;
    private String mScreenName;
    private boolean mSaveData = true;
    private SectionView sectionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tryRestoreSectionState(savedInstanceState);
    }

    @Nullable
    public View getSectionView(boolean isHelp) {
        if(sectionView == null) {
            sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData,
                    mScreenName, isHelp, true);
        }
        return sectionView.getView();
    }

    @NonNull
    public Pair<RecyclerView, ArrayList<Integer>> getSectionRecylerView(ViewGroup parent) {
        if(sectionView == null) {
            sectionView = new SectionView(getActivity(), faceRobotoRegular, mSectionData, mScreenName);
        }
        RecyclerView recyclerView = sectionView.getRecyclerView(parent);
        ArrayList<Integer> dynamicTiles = sectionView.getDynamicTiles();
        return new Pair<>(recyclerView, dynamicTiles);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSectionData != null && mSaveData) {
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

    @Trace
    @Override
    public void setSectionData(SectionData sectionData) {
        if (mSectionData != null && sectionData != null
                && mSectionData.getSections() != null
                && sectionData.getSections() != null
                && sectionData.getSections().size() == mSectionData.getSections().size()) {
            ArrayList<Section> newSections = sectionData.getSections();
            ArrayList<Section> oldSection = mSectionData.getSections();
            for (int i = 0; i < newSections.size(); i++) {
                if (newSections.get(i).equals(oldSection.get(i))) {
                    newSections.get(i).setIsShown(oldSection.get(0).isShown());
                }
            }
        }
        mSectionData = sectionData;
        if(sectionView != null) {
            sectionView.setSectionData(mSectionData);
        }
    }

    protected void saveSectionData(boolean saveData) {
        mSaveData = saveData;
    }

}