package com.bigbasket.mobileapp.fragment.product;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
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
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SubCategoryListFragment extends BaseSectionFragment {

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
                    mSectionData = subCategoryCallback.apiResponseContent.categoryLandingApiCategoryKeyContent.sectionData;
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

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());

        ArrayList<Object> result;
        if (!response_ok) {
            subCategoryAdapter.insert(subCategoryModel, responseVersion, mSectionData, topCatSlug);
        } else {
            result = subCategoryAdapter.getSubCategory(topCatSlug);

            if (result != null && result.size() == 2) {
                subCategoryModel = (SubCategoryModel) result.get(0);
                mSectionData = (SectionData) result.get(1);
            }
        }

        //display section
        LinearLayout subCategoryPageLayout = new LinearLayout(getActivity());
        subCategoryPageLayout.setOrientation(LinearLayout.VERTICAL);

        final List<Category> categoryArrayList = new ArrayList<>();

        Category allCategories = new Category("All " + topCatName.trim(), topCatSlug);
        categoryArrayList.add(allCategories);

        if (subCategoryModel != null && subCategoryModel.getCategory() != null) {
            for (int i = 0; i < subCategoryModel.getCategory().size(); i++) {
                Category subCat = subCategoryModel.getCategory().get(i);
                categoryArrayList.add(subCat);
            }
        }

        if (mSectionData != null) {
            View sectionView = getSectionView();
            if (sectionView != null) {
                subCategoryPageLayout.addView(sectionView);
            }
        }

        final ExpandableListView subCategoryExpandableView = new ExpandableListView(getActivity());
        subCategoryExpandableView.setGroupIndicator(null);
        subCategoryExpandableView.setDivider(new ColorDrawable(getResources().getColor(R.color.uiv3_divider_color)));
        subCategoryExpandableView.setDividerHeight(1);
        subCategoryExpandableView.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        SubCategoryListAdapter subCategoryListAdapter = new SubCategoryListAdapter<>(this, categoryArrayList, getActivity());
        subCategoryExpandableView.setAdapter(subCategoryListAdapter);

        subCategoryExpandableView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (categoryArrayList.get(groupPosition).getCategory() != null &&
                        categoryArrayList.get(groupPosition).getCategory().size() > 0) {
                } else {
                    Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_CATEGORY);
                    if (categoryArrayList.get(groupPosition).getFilter() != null)
                        intent.putExtra(Constants.FILTER, categoryArrayList.get(groupPosition).getFilter());
                    if (categoryArrayList.get(groupPosition).getSortBy() != null)
                        intent.putExtra(Constants.SORT_BY, categoryArrayList.get(groupPosition).getSortBy());
                    intent.putExtra(Constants.CATEGORY_SLUG, categoryArrayList.get(groupPosition).getSlug());
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
                return false;
            }
        });

        subCategoryExpandableView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_CATEGORY);
                intent.putExtra(Constants.CATEGORY_SLUG, categoryArrayList.get(groupPosition).getCategory().get(childPosition).getSlug());
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return false;
            }
        });
        subCategoryPageLayout.addView(subCategoryExpandableView);
        contentView.addView(subCategoryPageLayout);
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Browse by Category";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SubCategoryListFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.CATEGORY_LANDING_SCREEN;
    }
}