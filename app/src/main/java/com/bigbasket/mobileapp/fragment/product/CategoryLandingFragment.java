package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SubCategoryApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseSectionFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Category;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CategoryLandingFragment extends BaseSectionFragment {

    private String topCatSlug;
    private String topCatName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topCatSlug = getArguments().getString(Constants.TOP_CATEGORY_SLUG);
        topCatName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        String topCatVersion = getArguments().getString(Constants.TOP_CATEGORY_VERSION);
        loadSubCategoryList(topCatVersion);
    }

    private void loadSubCategoryList(String topCatVersion) {
        if (checkInternetConnection()) {
            SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());
            String version = subCategoryAdapter.getVersion(topCatSlug);
            if (topCatVersion != null && topCatVersion.equals(version)) {
                renderSubCategory(null, true, null);
            } else {
                String categorySlug = topCatSlug;
                getSubCategoryData(categorySlug, version);
            }
        } else {
            handler.sendOfflineError(true);
        }
    }

    private void getSubCategoryData(String categorySlug, String version) {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getSubCategoryData(categorySlug, version, new Callback<ApiResponse<SubCategoryApiResponse>>() {
            @Override
            public void success(ApiResponse<SubCategoryApiResponse> subCategoryCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                SubCategoryModel subCategoryModel = null;
                if (subCategoryCallback.status == 0) {
                    String responseVersion = subCategoryCallback.apiResponseContent.responseVersion;
                    boolean response_ok = subCategoryCallback.apiResponseContent.a_ok;
                    setSectionData(subCategoryCallback.apiResponseContent.categoryLandingApiCategoryKeyContent.sectionData);
                    if (!response_ok) {
                        subCategoryModel = subCategoryCallback.apiResponseContent.categoryLandingApiCategoryKeyContent.subCategoryModel;
                    }
                    renderSubCategory(responseVersion, response_ok, subCategoryModel);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.PRODUCT_TOP_CAT, topCatName);
                    trackEvent(TrackingAware.CATEGORY_LANDING_SHOWN, map);
                } else {
                    handler.sendEmptyMessage(subCategoryCallback.status,
                            subCategoryCallback.message, true);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void renderSubCategory(String responseVersion, boolean response_ok,
                                   SubCategoryModel subCategoryModel) {

        if (getActivity() == null) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());

        ArrayList<Object> result;
        if (!response_ok) {
            subCategoryAdapter.insert(subCategoryModel, responseVersion, getSectionData(), topCatSlug);
        } else {
            result = subCategoryAdapter.getSubCategory(topCatSlug);

            if (result != null && result.size() == 2) {
                subCategoryModel = (SubCategoryModel) result.get(0);
                setSectionData((SectionData) result.get(1));
            }
        }

        final List<Category> categoryArrayList = new ArrayList<>();

        Category allCategories = new Category("All " + topCatName.trim(), topCatSlug);
        categoryArrayList.add(allCategories);

        if (subCategoryModel != null && subCategoryModel.getCategory() != null) {
            for (int i = 0; i < subCategoryModel.getCategory().size(); i++) {
                Category subCat = subCategoryModel.getCategory().get(i);
                categoryArrayList.add(subCat);
            }
        }

        RecyclerView subCategoryRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

        SubCategoryListAdapter subCategoryListAdapter = new SubCategoryListAdapter<>(this, categoryArrayList, getSectionView());
        subCategoryRecyclerView.setAdapter(subCategoryListAdapter);

        contentView.addView(subCategoryRecyclerView);
    }

    @Nullable
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return !TextUtils.isEmpty(topCatName) ? topCatName : "Browse by Category";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return CategoryLandingFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.CATEGORY_LANDING_SCREEN;
    }
}