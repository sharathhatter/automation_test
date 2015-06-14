package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.SectionView;

/**
 * Created by jugal on 12/6/15.
 */
public class DiscountFragment extends BaseFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() == null) return;
        SectionData sectionData = getArguments().getParcelable(Constants.SECTIONS);
        if (sectionData != null) renderSectionData(sectionData);
    }

    private void renderSectionData(SectionData sectionData) {
        if (getCurrentActivity() == null) return;

        ViewGroup contentLayout = getContentView();
        if (contentLayout == null) return;
        contentLayout.removeAllViews();

        if (sectionData != null) {
            View contentSectionView = new SectionView(getCurrentActivity(), faceRobotoRegular,
                    sectionData, sectionData.getScreenName()).getView();
            if (contentSectionView != null) {
                // Use a scrollview as this section can be huge
                ScrollView scrollView = new ScrollView(getCurrentActivity());
                scrollView.addView(contentSectionView);
                contentLayout.addView(scrollView);
            }
        }
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return getString(R.string.discount);
    }

    @Override
    public String getScreenTag() {
        return DiscountFragment.class.getName();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return TrackEventkeys.DISCOUNT_SCREEN;
    }
}
