package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.SectionView;


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
            RecyclerView recyclerView = new SectionView(getCurrentActivity(), faceRobotoRegular,
                    sectionData, Constants.DISCOUNT_PAGE, false, true).getRecyclerView(contentLayout);
            if (recyclerView != null) {
                contentLayout.addView(recyclerView);
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
        return getString(R.string.discounts);
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
