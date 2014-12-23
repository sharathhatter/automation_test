package com.bigbasket.mobileapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.HomePageApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateVersionInfoApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends BaseSectionFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean sectionStateRestored = tryRestoreSectionState(savedInstanceState);
        if (sectionStateRestored) {
            renderHomePage();
        } else {
            new GetCartCountTask<>(getCurrentActivity()).startTask();
            requestHomePage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).removePharmaPrescriptionId();
    }

    private boolean isVisitorUpdateNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedVersionNumber = preferences.getString(Constants.VERSION_NAME, null);
        String appVersionName = getAppVersion();
        return TextUtils.isEmpty(storedVersionNumber) ||
                (!TextUtils.isEmpty(appVersionName) && !appVersionName.equals(storedVersionNumber));
    }

    private void updateMobileVisitorInfo() {
        // Update app-version number in Mobile Visitor
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.updateVersionNumber(preferences.getString(Constants.DEVICE_ID, null),
                getAppVersion(), new Callback<ApiResponse<UpdateVersionInfoApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<UpdateVersionInfoApiResponseContent> updateVersionInfoApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (updateVersionInfoApiResponse.status) {
                            case 0:
                                SharedPreferences.Editor editor =
                                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.putString(Constants.VERSION_NAME, getAppVersion());
                                editor.commit();
                                if (updateVersionInfoApiResponse.apiResponseContent.userDetails != null) {
                                    UIUtil.updateStoredUserDetails(getActivity(),
                                            updateVersionInfoApiResponse.apiResponseContent.userDetails,
                                            AuthParameters.getInstance(getActivity()).getMemberEmail(),
                                            updateVersionInfoApiResponse.apiResponseContent.mId);
                                    AuthParameters.updateInstance(getActivity());
                                    if (getCurrentActivity() != null &&
                                            !AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                                        getCurrentActivity().updateKonotor();
                                    }
                                }
                                getHomePage();
                                Log.d("HomeFragment", getResources().getString(R.string.versionNoUpdated));
                                break;
                            default:
                                Intent result = new Intent();
                                result.putExtra(Constants.FORCE_REGISTER_DEVICE, true);
                                getActivity().setResult(Constants.FORCE_REGISTER_CODE, result);
                                if (getCurrentActivity() == null) return;
                                getCurrentActivity().onLogoutRequested();
                                getActivity().finish();
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private String getAppVersion() {
        String appVersionName;
        try {
            appVersionName = getActivity().getPackageManager().
                    getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = null;
        }
        return appVersionName;
    }

    private void requestHomePage() {
        if (isVisitorUpdateNeeded()) {
            updateMobileVisitorInfo();
        } else {
            getHomePage();
        }
    }

    private void getHomePage() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.loadHomePage(new Callback<ApiResponse<HomePageApiResponseContent>>() {
            @Override
            public void success(ApiResponse<HomePageApiResponseContent> homePageApiResponse, Response response) {
                hideProgressView();
                mSections = homePageApiResponse.apiResponseContent.sections;
                ArrayList<DestinationInfo> destinationInfos =
                        homePageApiResponse.apiResponseContent.destinationInfos;
                if (destinationInfos != null && destinationInfos.size() > 0) {
                    mDestinationInfoHashMap = new HashMap<>();
                    for (DestinationInfo destinationInfo : destinationInfos) {
                        mDestinationInfoHashMap.put(destinationInfo.getDestinationInfoId(), destinationInfo);
                    }
                }
                renderHomePage();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressView();
            }
        });
    }

    private void renderHomePage() {
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        // Filter sections
        Set<String> supportedSectionTypes = Section.getSupportedSectionTypes();
        for (Iterator<Section> iterator = mSections.iterator(); iterator.hasNext(); ) {
            Section section = iterator.next();
            if (!supportedSectionTypes.contains(section.getSectionType())) {
                iterator.remove();
            }
        }

        // Render sections
        showProgressView();

        ScrollView contentScrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentScrollView.setLayoutParams(layoutParams);

        LinearLayout homePageLayout = new LinearLayout(getActivity());
        homePageLayout.setOrientation(LinearLayout.VERTICAL);

        displaySections(homePageLayout);
        contentScrollView.addView(homePageLayout);

        contentView.removeAllViews();
        contentView.addView(contentScrollView);
    }

    @Override
    public String getTitle() {
        return "BigBasket";
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        retainSectionState(outState);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return Constants.HOME;
    }
}
