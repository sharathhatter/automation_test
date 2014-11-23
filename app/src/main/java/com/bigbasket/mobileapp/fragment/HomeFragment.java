package com.bigbasket.mobileapp.fragment;

import android.app.Activity;
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
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
            requestHomePage();
        }

        new GetCartCountTask<>(getCurrentActivity(),
                MobileApiUrl.getBaseAPIUrl() + Constants.C_SUMMARY).execute();
    }

    private boolean isVisitorUpdateNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedVersionNumber = preferences.getString(Constants.VERSION_NAME, null);
        String appVersionName = getAppVersion();
        return TextUtils.isEmpty(storedVersionNumber) ||
                (!TextUtils.isEmpty(appVersionName) && !appVersionName.equals(storedVersionNumber));
    }

    private void updateMobileVisitorInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String appVersionName = getAppVersion();
        // Update app-version number in Mobile Visitor
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.DEVICE_ID, preferences.getString(Constants.DEVICE_ID, null));
        params.put(Constants.APP_VERSION, appVersionName);
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_VERSION_NUMBER,
                params, true, false, null);
    }

    private String getAppVersion() {
        Activity activity = getActivity();
        String appVersionName;
        try {
            appVersionName = activity.getPackageManager().
                    getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = null;
        }
        return appVersionName;
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (getActivity() == null || getCurrentActivity() == null) return;
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.GET_VERSION_NUMBER)) {
            // Is version number call
            String responseJson = httpOperationResult.getReponseString();
            JsonObject httpResponseJsonObj = new JsonParser().parse(responseJson).getAsJsonObject();
            String status = httpResponseJsonObj.get(Constants.STATUS).getAsString();
            if (status.equalsIgnoreCase(Constants.OK)) {
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.putString(Constants.VERSION_NAME, getAppVersion());
                String memberName = httpResponseJsonObj.has(Constants.FULL_NAME) ?
                        httpResponseJsonObj.get(Constants.FULL_NAME).getAsString() : null;
                String mid = httpResponseJsonObj.has(Constants.MID_KEY) ?
                        httpResponseJsonObj.get(Constants.MID_KEY).getAsString() : null;
                if (!TextUtils.isEmpty(memberName)) {
                    editor.putString(Constants.MEMBER_FULL_NAME_KEY, memberName);
                }
                if (!TextUtils.isEmpty(mid)) {
                    editor.putString(Constants.MID_KEY, mid);
                }
                editor.commit();
                AuthParameters.updateInstance(getActivity());
                if (!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                    getCurrentActivity().updateKonotor();
                }
                getHomePage();
                Log.d("HomeFragment", getResources().getString(R.string.versionNoUpdated));
            } else {
                Intent result = new Intent();
                result.putExtra(Constants.FORCE_REGISTER_DEVICE, true);
                getActivity().setResult(Constants.FORCE_REGISTER_CODE, result);
                getCurrentActivity().onLogoutRequested();
                getActivity().finish();
            }
        } else if (url.contains(Constants.GET_HOME_PG_URL)) {
            JsonObject httpResponseObj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            int status = httpResponseObj.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    JsonObject responseJsonObj = httpResponseObj.get(Constants.RESPONSE).getAsJsonObject();
                    JsonArray sectionsJsonArray = responseJsonObj.get(Constants.SECTIONS).getAsJsonArray();
                    JsonArray destinationInfoJsonArray =
                            responseJsonObj.get(Constants.DESTINATIONS_INFO).getAsJsonArray();
                    mSections = ParserUtil.parseSectionResponse(sectionsJsonArray);
                    mDestinationInfoHashMap = ParserUtil.parseDestinationInfo(destinationInfoJsonArray);
                    renderHomePage();
                    break;
                default:
                    // TODO : Add error handling
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void requestHomePage() {
        if (isVisitorUpdateNeeded()) {
            updateMobileVisitorInfo();
        } else {
            getHomePage();
        }
    }

    private void getHomePage() {
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_HOME_PG_URL,
                null, false, true, null);
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
