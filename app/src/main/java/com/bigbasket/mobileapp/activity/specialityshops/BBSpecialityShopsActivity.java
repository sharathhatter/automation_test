package com.bigbasket.mobileapp.activity.specialityshops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.specialityshops.StoreListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.interfaces.LaunchStoreListAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityShopsListData;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv3.HeaderSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BBSpecialityShopsActivity extends BBActivity implements LaunchStoreListAware {

    private TextView mToolbarTextDropDown;
    private String category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSpecialityShops();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_sstore_list_activity;
    }

    private void getSpecialityShops() {
        if (getIntent() != null) {
            category = getIntent().getStringExtra(Constants.CATEGORY);
            setTitle(category);
            loadSpecialityShops(category);
        }
    }

    private void renderStoreList(String baseImgUrl, ArrayList<SpecialityStore> storeList) {
        RecyclerView recyclerViewStoreList = (RecyclerView) findViewById(R.id.store_list);
        StoreListRecyclerAdapter<BBSpecialityShopsActivity> storeListRecyclerAdapter = new StoreListRecyclerAdapter<>(BBSpecialityShopsActivity.this, baseImgUrl, storeList);
        recyclerViewStoreList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStoreList.setAdapter(storeListRecyclerAdapter);
        logViewSpecialityShopsEvent(category);
    }

    private void showStoreEmptyMsg(String location) {
        RecyclerView recyclerViewStoreList = (RecyclerView) findViewById(R.id.store_list);
        recyclerViewStoreList.setVisibility(View.GONE);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssList);
        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_store_list_data, layout, false);

        TextView txtMsg = (TextView) base.findViewById(R.id.textView_empty_text);
        String emptyMsg = getString(R.string.store_empty) + category + getString(R.string.available_in) + " \n";
        if (TextUtils.isEmpty(location)) {
            emptyMsg = getString(R.string.store_empty) + category + getString(R.string.available_in);
            location = getString(R.string.your_loc);
        }
        String strMsg = emptyMsg + location + getString(R.string.adding_newStores);
        Spannable spannable = new SpannableString(strMsg);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), 0, emptyMsg.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (!location.equalsIgnoreCase(getString(R.string.your_loc))) {
            spannable.setSpan(new CustomTypefaceSpan("", faceRobotoBold), emptyMsg.length(), emptyMsg.length() + location.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uiv3_status_bar_background)), emptyMsg.length(), emptyMsg.length() + location.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else
            spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), emptyMsg.length(), emptyMsg.length() + location.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), emptyMsg.length() + location.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtMsg.setText(spannable);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int dp = (int) getResources().getDimension(R.dimen.margin_medium_large);
        params.setMargins(0, dp, 0, 0);
        params.addRule(RelativeLayout.BELOW, getToolbar().getId());
        layout.addView(base, params);
    }

    private void loadSpecialityShops(final String catVal) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getApplicationContext());
        showProgressView();
        bigBasketApiService.getSpecialityShops(catVal, new Callback<ApiResponse<SpecialityShopsListData>>() {
            @Override
            public void success(ApiResponse<SpecialityShopsListData> specialityStoreListApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (specialityStoreListApiResponse.status == 0) {
                    Section headerSection = specialityStoreListApiResponse.apiResponseContent.getHeaderSection();
                    if (headerSection != null && headerSection.getSectionItems().size() > 0) {
                        renderHeaderDropDown(headerSection, specialityStoreListApiResponse.apiResponseContent.getHeaderSelectedIndex(), catVal);
                    }
                    ArrayList<SpecialityStore> storeList = specialityStoreListApiResponse.apiResponseContent.getStoreList();
                    if (storeList != null && storeList.size() > 0) {
                        renderStoreList(specialityStoreListApiResponse.apiResponseContent.getBaseImgUrl(), storeList);
                    } else {
                        final ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(BBSpecialityShopsActivity.this).getAddressSummaries();
                        if (addressSummaries != null && addressSummaries.size() > 0) {
                            showStoreEmptyMsg(addressSummaries.get(0).getArea() + "," + addressSummaries.get(0).getCityName());
                            renderHeaderDropDown(null, 0, category);
                        } else showStoreEmptyMsg(null);
                    }
                } else handler.sendEmptyMessage(specialityStoreListApiResponse.status,
                        specialityStoreListApiResponse.message, true);
            }

            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void renderHeaderDropDown(@Nullable final Section headSection, int mHeaderSelectedIdx,
                                      String screenName) {
        Toolbar toolbar = getToolbar();
        if (mToolbarTextDropDown == null) {
            mToolbarTextDropDown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                .withCtx(this)
                .withDefaultSelectedIdx(mHeaderSelectedIdx)
                .withFallbackHeaderTitle(!TextUtils.isEmpty(category) ? category : screenName)
                .withHeadSection(headSection)
                .withImgCloseChildDropdown((ImageView) findViewById(R.id.imgCloseChildDropdown))
                .withLayoutChildToolbarContainer((ViewGroup) findViewById(R.id.layoutChildToolbarContainer))
                .withLayoutListHeader((ViewGroup) findViewById(R.id.layoutListHeader))
                .withListHeaderDropdown((ListView) findViewById(R.id.listHeaderDropdown))
                .withToolbar(getToolbar())
                .withTxtChildDropdownTitle((TextView) findViewById(R.id.txtListDialogTitle))
                .withTxtToolbarDropdown(mToolbarTextDropDown)
                .withTypeface(faceRobotoRegular)
                .build()
                .setView();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SPECIALITYSHOPS_LISTING_PAGE;
    }

    private void logViewSpecialityShopsEvent(String categoryName) {
        if (categoryName == null) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.CATEGORY, categoryName);
        trackEvent(TrackingAware.SPECIALITYSHOPS_LIST_SHOWN, map);
        trackEventAppsFlyer(TrackingAware.SPECIALITYSHOPS_LIST_SHOWN);
    }

    @Override
    public void launchStoreList(String destinationSlug) {
        category = destinationSlug;
        loadSpecialityShops(category);
    }
}
