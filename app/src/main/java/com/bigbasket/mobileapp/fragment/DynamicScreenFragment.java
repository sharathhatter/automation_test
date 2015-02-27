package com.bigbasket.mobileapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.interfaces.DynamicScreenAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.task.GetDynamicPageTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

import retrofit.RetrofitError;

public class DynamicScreenFragment extends BaseSectionFragment implements DynamicScreenAware {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String screenName = getArguments().getString(Constants.SCREEN);
        if (TextUtils.isEmpty(screenName)) {
            return;
        }
        new GetDynamicPageTask<>(this, screenName, true, true).startTask();
    }

    private void loadDynamicScreen() {
        LinearLayout contentView = getContentView();
        SectionData sectionData = getSectionData();
        if (contentView == null || sectionData == null || sectionData.getSections() == null
                || sectionData.getSections().size() == 0) return;

        // Render sections
        showProgressView();

        ScrollView contentScrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentScrollView.setLayoutParams(layoutParams);

        View sectionView = getSectionView();
        if (sectionView != null) {
            contentScrollView.addView(sectionView);
        }

        contentView.removeAllViews();
        contentView.addView(contentScrollView);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.DYNAMIC_SCREEN;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return DynamicScreenFragment.class.getName();
    }

    @Override
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData) {
        setSectionData(sectionData);
        setScreenName(screenName);
        loadDynamicScreen();
        HashMap<String, String> screenNameMap = null;
        if (!TextUtils.isEmpty(screenName)) {
            screenNameMap = new HashMap<>();
            screenNameMap.put(Constants.SCREEN, screenName);
        }
        trackEvent(TrackingAware.DYNAMIC_SCREEN_SHOWN, screenNameMap);
    }

    @Override
    public void onDynamicScreenFailure(RetrofitError error) {
        handler.handleRetrofitError(error, true);
    }

    @Override
    public void onDynamicScreenFailure(int error, String msg) {
        handler.sendEmptyMessage(error, msg, true);
    }
}
