package com.bigbasket.mobileapp.activity.product;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.ProductListPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductNextPageResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.fragment.product.GenericProductListFragment;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.LazyProductListAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.product.ProductTabInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.SectionView;
import com.bigbasket.mobileapp.view.uiv3.BBArrayAdapter;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.google.gson.Gson;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ProductListActivity extends BBActivity implements ProductListDataAware, LazyProductListAware {

    private ArrayList<NameValuePair> mNameValuePairs;
    private ProductListPagerAdapter mStatePagerAdapter;
    private HashMap<String, ArrayList<Product>> mMapForTabWithNoProducts;
    private HashMap<String, Integer> mArrayTabTypeAndFragmentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        mNameValuePairs = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_QUERY);
        loadProductTabs();
    }

    @Override
    public void onNoFragmentsInLayout() {
        // Don't finish, as the product view-pager is still active.
    }

    @Override
    @LayoutRes
    public int getMainLayout() {
        return R.layout.uiv3_product_list_layout;
    }

    private void loadProductTabs() {
        if (mNameValuePairs == null || mNameValuePairs.size() == 0) {
            return;
        }
        HashMap<String, String> paramMap = NameValuePair.toMap(mNameValuePairs);
        new ProductListTask<>(this, paramMap).startTask();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void syncBasket() {
        // Don't remove the IS_BASKET_DIRTY flag, as Fragment also needs to refresh, only update count
        new GetCartCountTask<>(this).startTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {
        handleFragmentChange(newFragment.getClass().getName(), newFragment.getArguments(), null);
    }

    @Override
    public void addToMainLayout(AbstractFragment newFragment, String tag) {
        handleFragmentChange(newFragment.getClass().getName(), newFragment.getArguments(), tag);
    }

    private void handleFragmentChange(String fragmentClassName, Bundle fragmentArgs, String tag) {
        if (TextUtils.isEmpty(fragmentClassName)) return;
        Intent data = new Intent();
        if (fragmentArgs != null) {
            data.putExtras(fragmentArgs);
        }
        data.putExtra(Constants.FRAGMENT_CLASS_NAME, fragmentClassName);
        data.putExtra(Constants.FRAGMENT_TAG, tag);
        setResult(NavigationCodes.LAUNCH_FRAGMENT, data);
        finish();
    }

    @Override
    public void setProductTabData(ProductTabData productTabData) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        if (productTabData.getProductTabInfos() != null &&
                productTabData.getProductTabInfos().size() > 0) {
            View base = getLayoutInflater().inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);
            ViewPager viewPager = (ViewPager) base.findViewById(R.id.pager);

            ArrayList<BBTab> bbTabs = new ArrayList<>();
            ArrayList<String> tabTypeWithNoProducts = new ArrayList<>();
            ArrayList<ProductTabInfo> productTabInfos = productTabData.getProductTabInfos();
            for (int i = 0; i < productTabInfos.size(); i++) {
                ProductTabInfo productTabInfo = productTabInfos.get(i);
                ProductInfo productInfo = productTabInfo.getProductInfo();
                if (productInfo != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.PRODUCT_INFO, productInfo);
                    bundle.putString(Constants.BASE_IMG_URL, productTabData.getBaseImgUrl());
                    bundle.putParcelableArrayList(Constants.PRODUCT_QUERY, mNameValuePairs);
                    bundle.putString(Constants.TAB_TYPE, productTabInfo.getTabType());
                    bbTabs.add(new BBTab<>(productTabInfo.getTabName() + " (" + productInfo.getProductCount() + ")",
                            GenericProductListFragment.class, bundle));
                    if (productInfo.getCurrentPage() == -1) {
                        if (mArrayTabTypeAndFragmentPosition == null) {
                            mArrayTabTypeAndFragmentPosition = new HashMap<>();
                        }
                        mArrayTabTypeAndFragmentPosition.put(productTabInfo.getTabType(), i);
                        tabTypeWithNoProducts.add(productTabInfo.getTabType());
                    }
                }
            }

            mStatePagerAdapter = new ProductListPagerAdapter(this, getSupportFragmentManager(), bbTabs);
            viewPager.setAdapter(mStatePagerAdapter);

            SmartTabLayout pagerSlidingTabStrip = (SmartTabLayout) base.findViewById(R.id.slidingTabs);
            pagerSlidingTabStrip.setViewPager(viewPager);
            if (productTabData.getContentSectionData() != null) {
                LinearLayout layoutProducts = new LinearLayout(this);
                layoutProducts.setOrientation(LinearLayout.VERTICAL);
                View sectionView = new SectionView(this, faceRobotoRegular, productTabData.getContentSectionData(), "Product List").getView();
                if (sectionView != null) {
                    layoutProducts.addView(sectionView);
                }
                layoutProducts.addView(base);
                contentFrame.addView(layoutProducts);
            } else {
                contentFrame.addView(base);
            }
            renderHeaderDropDown(productTabData.getHeaderSection());

            if (tabTypeWithNoProducts.size() > 0) {
                ArrayList<NameValuePair> newNameValuePairs = new ArrayList<>(mNameValuePairs);
                newNameValuePairs.add(new NameValuePair(Constants.TAB_TYPE, new Gson().toJson(tabTypeWithNoProducts)));
                BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
                bigBasketApiService.productNextPage(NameValuePair.toMap(newNameValuePairs), new Callback<ApiResponse<ProductNextPageResponse>>() {
                    @Override
                    public void success(ApiResponse<ProductNextPageResponse> productNextPageApiResponse, Response response) {
                        if (isSuspended()) return;
                        if (productNextPageApiResponse.status == 0) {
                            mMapForTabWithNoProducts = productNextPageApiResponse.apiResponseContent.productListMap;
                            setUpProductsInEmptyFragments();
                        } else {
                            notifyEmptyFragmentAboutFailure();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        notifyEmptyFragmentAboutFailure();
                    }
                });
            }
        } else {
            // TODO: Show empty product view
        }
    }

    private void notifyEmptyFragmentAboutFailure() {
        if (mArrayTabTypeAndFragmentPosition != null) {
            for (Map.Entry<String, Integer> entry : mArrayTabTypeAndFragmentPosition.entrySet()) {
                Fragment fragment = mStatePagerAdapter.getItem(entry.getValue());
                if (fragment != null) {
                    ((ProductListAwareFragment) fragment).setLazyProductLoadingFailure();
                }
            }
        }
    }

    private void setUpProductsInEmptyFragments() {
        if (mArrayTabTypeAndFragmentPosition != null) {
            for (Map.Entry<String, Integer> entry : mArrayTabTypeAndFragmentPosition.entrySet()) {
                Fragment fragment = mStatePagerAdapter.getItem(entry.getValue());
                if (fragment != null) {
                    ArrayList<Product> products = mMapForTabWithNoProducts != null ?
                            mMapForTabWithNoProducts.get(entry.getKey()) : null;
                    if (products != null) {
                        ((ProductListAwareFragment) fragment).insertProductList(products);
                    } else {
                        ((ProductListAwareFragment) fragment).insertProductList(null);
                    }
                }
            }
        }
    }

    @Override
    @Nullable
    public ArrayList<Product> provideProductsIfAvailable(String tabType) {
        if (mMapForTabWithNoProducts != null) {
            return mMapForTabWithNoProducts.get(tabType);
        }
        return null;
    }

    private void renderHeaderDropDown(final Section headSection) {
        if (headSection != null && headSection.getSectionItems() != null
                && headSection.getSectionItems().size() > 0) {
            Spinner spinner = new Spinner(this);
            BBArrayAdapter bbArrayAdapter = new BBArrayAdapter<>(this, android.R.layout.simple_spinner_item, headSection.getSectionItems(),
                    faceRobotoRegular, Color.WHITE, getResources().getColor(R.color.uiv3_primary_text_color));
            bbArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(bbArrayAdapter);
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != Spinner.INVALID_POSITION) {
                        new OnSectionItemClickListener<>(getCurrentActivity(), headSection,
                                headSection.getSectionItems().get(position), "").onClick(view);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            Toolbar toolbar = getToolbar();
            toolbar.addView(spinner);
        }
    }

    @Override
    public boolean isNextPageLoading() {
        return false;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {

    }
}