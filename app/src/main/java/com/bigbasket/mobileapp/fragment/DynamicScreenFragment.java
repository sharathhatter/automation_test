package com.bigbasket.mobileapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDynamicPageApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionUtil;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;


public class DynamicScreenFragment extends BaseSectionFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String screenName = getArguments().getString(Constants.SCREEN);
        if (TextUtils.isEmpty(screenName)) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        Call<ApiResponse<GetDynamicPageApiResponse>> call = bigBasketApiService.getDynamicPage(getPreviousScreenName(), Constants.ANDROID,
                DataUtil.getAppVersion(getActivity()), screenName);
        call.enqueue(new BBNetworkCallback<ApiResponse<GetDynamicPageApiResponse>>(this) {
            @Override
            public void onSuccess(ApiResponse<GetDynamicPageApiResponse> getDynamicPageApiResponse) {
                switch (getDynamicPageApiResponse.status) {
                    case 0:
                        SectionData sectionData = getDynamicPageApiResponse.apiResponseContent.sectionData;
                        if (sectionData != null) {
                            sectionData.setSections(SectionUtil.preserveMemory(sectionData.getSections()));
                        }
                        onDynamicScreenSuccess(screenName, sectionData);
                        break;
                    default:
                        onDynamicScreenFailure(getDynamicPageApiResponse.status,
                                getDynamicPageApiResponse.message);
                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return false;
                }
                return true;
            }

            @Override
            public void onFailure(int httpErrorCode, String msg) {
                onDynamicScreenHttpFailure(httpErrorCode, msg);
            }

            @Override
            public void onFailure(Throwable t) {
                if (isSuspended()) return;
                onDynamicScreenFailure(t);
            }
        });
    }

    protected void loadDynamicScreen() {
        ViewGroup contentView = getContentView();
        SectionData sectionData = getSectionData();
        if (contentView == null || sectionData == null || sectionData.getSections() == null
                || sectionData.getSections().size() == 0) return;

        // Render sections
        showProgressView();
        contentView.removeAllViews();

        Pair<RecyclerView, ArrayList<Integer>> pair = getSectionRecylerView(contentView);
        if (pair.first != null) {
            contentView.addView(pair.first);
        }

        if (!TextUtils.isEmpty(sectionData.getScreenName())) {
            setTitle(sectionData.getScreenName());
        }
    }

    @Override
    public String getTitle() {
        return getSectionData() != null ? getSectionData().getScreenName() : null;
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
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

    public void onDynamicScreenFailure(Throwable t) {
        handler.handleRetrofitError(t, true);
    }

    public void onDynamicScreenFailure(int error, String msg) {
        handler.sendEmptyMessage(error, msg, true);
    }

    public void onDynamicScreenHttpFailure(int error, String msg) {
        handler.handleHttpError(error, msg, true);
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "DynamicScreenFragment";
    }
}
