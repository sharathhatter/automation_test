package com.bigbasket.mobileapp.fragment.product;

import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.SubCategoryApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Category;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SubCategoryListFragment extends BaseFragment {

    private SubCategoryModel subCategoryModel;
    private String topCatSlug;
    private String topCatName;
    private String topCatVersion;
    private int imageCounter = 0;
    private int width, imageheight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topCatSlug = getArguments().getString(Constants.TOP_CATEGORY_SLUG);
        topCatName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        topCatVersion = getArguments().getString(Constants.TOP_CATEGORY_VERSION); ///todo testing needs to be done


        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        imageheight = metrics.heightPixels * 18 / 100;
        loadSubCategoryList();
    }

    private void loadSubCategoryList() {
        if (checkInternetConnection()) {
            SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());
            String version = subCategoryAdapter.getVersion(topCatSlug);
            if (topCatVersion != null && topCatVersion.equals(version)) {
                renderSubCategory(null, true, null, null);
            } else {
                String categorySlug = topCatSlug;
                getSubCategoryData(categorySlug, version);
            }
        } else {
            String msg = "Cannot proceed with the operation. No network connection.";
            showErrorMsg(msg);
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
                if (subCategoryCallback.status == 0) {
                    String responseVersion = subCategoryCallback.apiResponseContent.responseVersion;
                    boolean response_ok = subCategoryCallback.apiResponseContent.a_ok;
                    ArrayList<String> bannerArrayList =
                            subCategoryCallback.apiResponseContent.categoryLandingApiCategoryKeyContent.bannerArrayList;
                    if (!response_ok) {
                        subCategoryModel = subCategoryCallback.apiResponseContent.categoryLandingApiCategoryKeyContent.subCategoryModel;
                    }
                    renderSubCategory(responseVersion, response_ok, bannerArrayList, subCategoryModel);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.PRODUCT_TOP_CAT, topCatName);
                    trackEvent(TrackingAware.BROWSE_CATEGORY_LANDING, map);
                } else {
                    handler.sendEmptyMessage(subCategoryCallback.status);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error);
            }
        });
    }

    private void renderSubCategory(String responseVersion, boolean response_ok, ArrayList<String> bannerArrayList,
                                   SubCategoryModel subCategoryModel) {

        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());

        ArrayList<Object> result;
        if (!response_ok) {
            try {
                subCategoryAdapter.insert(subCategoryModel, responseVersion, bannerArrayList, topCatSlug);
                //subCategoryAdapter.close();
            } catch (Exception e) {
                //subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");
            } finally {
                //subCategoryAdapter.close();
            }
        } else {
            try {
                result = subCategoryAdapter.getSubCategory(topCatSlug);
                subCategoryModel = (SubCategoryModel) result.get(0);
                bannerArrayList = (ArrayList<String>) result.get(1);
            } catch (SQLiteException e) {
                //subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");

            } finally {
                //subCategoryAdapter.close();
            }
        }

        // banner images
        renderBanner(bannerArrayList, contentView);

        final List<Category> categoryArrayList = new ArrayList<>();

        // subcat added by client
        Category newLaunchesCategory = new Category("New Launches", "launches");
        categoryArrayList.add(newLaunchesCategory);

        Category offersCategory = new Category("Offers", "offers");
        categoryArrayList.add(offersCategory);

        Category allCategories = new Category("All " + topCatName.trim(), topCatSlug);
        categoryArrayList.add(allCategories);

        for (int i = 0; i < subCategoryModel.getCategory().size(); i++) {
            Category subCat = subCategoryModel.getCategory().get(i);
            if (subCat.getCategory() != null && subCat.getCategory().size() > 0) {
                Category offersBottomCategory = new Category("Offers", "offerssub");
                subCat.getCategory().add(0, offersBottomCategory);

                Category allBottomCategory = new Category("All " + subCategoryModel.getCategory().get(i).getName(),
                        subCat.getSlug());
                subCat.getCategory().add(1, allBottomCategory);
            }
            categoryArrayList.add(subCat);

        }

        final ExpandableListView subCategoryExpandableView = new ExpandableListView(getActivity());
        subCategoryExpandableView.setGroupIndicator(null);
        subCategoryExpandableView.setDivider(new ColorDrawable(getResources().getColor(R.color.strokeLine)));
        subCategoryExpandableView.setDividerHeight(1);
        subCategoryExpandableView.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        SubCategoryListAdapter subCategoryListAdapter = new SubCategoryListAdapter(this, categoryArrayList, getActivity());
        subCategoryExpandableView.setAdapter(subCategoryListAdapter);
        contentView.addView(subCategoryExpandableView);

        subCategoryExpandableView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (categoryArrayList.get(groupPosition).getCategory() != null &&
                        categoryArrayList.get(groupPosition).getCategory().size() > 0) {
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    changeFragment(categoryProductsFragment);
                    return false;
                }
                return false;
            }
        });

        subCategoryExpandableView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childPosition == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    changeFragment(categoryProductsFragment);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getCategory().get(childPosition).getSlug());
                    CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                    categoryProductsFragment.setArguments(bundle);
                    changeFragment(categoryProductsFragment);
                }
                return false;
            }
        });

    }

    private void renderBanner(final ArrayList<String> bannerArrList, LinearLayout contentView) {
        if (bannerArrList == null || bannerArrList.size() == 0) return;

        if (bannerArrList.size() > 0) {
            final LinearLayout childfirst1 = new LinearLayout(getActivity());
            final LinearLayout.LayoutParams childParams1 =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageheight);
            childfirst1.setLayoutParams(childParams1);
            childfirst1.setOrientation(LinearLayout.VERTICAL);
            contentView.addView(childfirst1);
            final ProgressBar parentprocessBar = new ProgressBar(getActivity(), null,
                    android.R.attr.progressBarStyleInverse);
            final LinearLayout.LayoutParams processBarParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            processBarParams.gravity = Gravity.CENTER;
            parentprocessBar.setLayoutParams(processBarParams);
            childfirst1.addView(parentprocessBar);

            final ImageView imageView = new ImageView(getActivity());
            if (bannerArrList.size() > 1) {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        final LinearLayout.LayoutParams processBarParams1 = new LinearLayout.LayoutParams(width, imageheight);
                        imageView.setLayoutParams(processBarParams1);
                        String bannerUrl = bannerArrList.get(imageCounter);
                        if (bannerUrl.startsWith("//"))
                            bannerUrl = "http:" + bannerUrl;

                        ImageLoader.getInstance().displayImage(bannerUrl, imageView);
                        imageCounter++;
                        if (imageCounter >= bannerArrList.size()) {
                            imageCounter = 0;
                        }
                        childfirst1.postDelayed(this, 5000);
                    }
                };
                childfirst1.postDelayed(runnable, 5000);
                childfirst1.removeAllViews();
                childfirst1.addView(imageView);
            } else if (bannerArrList.size() == 1) {
                String bannerUrl = bannerArrList.get(imageCounter);
                if (bannerUrl.startsWith("//"))
                    bannerUrl = "http:" + bannerUrl;
                ImageLoader.getInstance().displayImage(bannerUrl, imageView);
                childfirst1.removeAllViews();
                childfirst1.addView(imageView);
            } else {
                imageView.setBackgroundColor(0);
                imageView.setBackgroundResource(R.drawable.noimage);
            }
        }

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

}