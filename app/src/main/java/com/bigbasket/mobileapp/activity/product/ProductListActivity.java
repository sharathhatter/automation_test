package com.bigbasket.mobileapp.activity.product;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapterWithFragmentRegistration;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductNextPageResponse;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.fragment.product.GenericProductListFragment;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.LazyProductListAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.product.ProductTabInfo;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.SectionView;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.bigbasket.mobileapp.view.uiv3.HeaderSpinnerView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ProductListActivity extends BBActivity implements ProductListDataAware, LazyProductListAware {

    private ArrayList<NameValuePair> mNameValuePairs;
    @Nullable
    private ViewPager mViewPager;
    private HashMap<String, ArrayList<Product>> mMapForTabWithNoProducts;
    private SparseArray<String> mArrayTabTypeAndFragmentPosition;
    private String mTitlePassedViaIntent;
    private TextView mToolbarTextDropdown;
    private ArrayList<FilteredOn> mFilteredOns;
    private ArrayList<FilterOptionCategory> mFilterOptionCategories;
    private String mSortedOn;
    private ArrayList<Option> mSortOptions;
    private HashMap<String, Integer> mCartInfo;
    private String tabType;
    private ArrayList<String> mTabNameWithEmptyProductView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProducts(0);
    }

    private void getProducts(int currentTabIndex) {
        mTitlePassedViaIntent = getIntent().getStringExtra(Constants.TITLE);
        setTitle(mTitlePassedViaIntent);
        mNameValuePairs = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_QUERY);
        loadProductTabs(currentTabIndex, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNameValuePairs != null) {
            setNextScreenNavigationContext(NameValuePair.buildNavigationContext(mNameValuePairs));
        }
    }

    @Override
    @LayoutRes
    public int getMainLayout() {
        return R.layout.uiv3_product_list_layout;
    }

    private void loadProductTabs(int currentTabIndex, boolean isFilterOrSortApplied) {
        if (mNameValuePairs == null || mNameValuePairs.size() == 0) {
            return;
        }
        // Reset all Globals
        if (!isFilterOrSortApplied) {
            mViewPager = null;
            mMapForTabWithNoProducts = null;
            mArrayTabTypeAndFragmentPosition = null;
        }

        HashMap<String, String> paramMap = NameValuePair.toMap(mNameValuePairs);
        setNextScreenNavigationContext(NameValuePair.buildNavigationContext(mNameValuePairs));
        new ProductListTask<>(this, paramMap, getCurrentNavigationContext(), currentTabIndex,
                isFilterOrSortApplied).startTask();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
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
    public void setProductTabData(ProductTabData productTabData, int currentTabIndex,
                                  boolean isFilterOrSortApplied) {
        if (currentTabIndex < 0) {
            currentTabIndex = 0;
        }

        if (getDrawerLayout() != null) {
            getDrawerLayout().closeDrawers();
        }

        SectionData sectionData = productTabData.getContentSectionData();
        View contentSectionView = null;
        boolean hasProducts = productTabData.getProductTabInfos() != null &&
                productTabData.getProductTabInfos().size() > 0;
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);

        if (!isFilterOrSortApplied) {
            contentFrame.removeAllViews();
            if (sectionData != null) {
                if (!hasProducts) {
                    contentSectionView = new SectionView(this, faceRobotoRegular, sectionData,
                            "Product List").getRecyclerView(contentFrame);
                } else {
                    contentSectionView = new SectionView(this, faceRobotoRegular, sectionData,
                            "Product List").getView();
                }
                if (contentSectionView != null) {
                    contentFrame.addView(contentSectionView);
                }
            }
        }

        mCartInfo = productTabData.getCartInfo();
        if (hasProducts) {
            // Setup content
            int numTabs = productTabData.getProductTabInfos().size();
            if (currentTabIndex >= numTabs) {
                currentTabIndex = 0;
            }
            if (numTabs > 1) {
                findViewById(R.id.slidingTabs).setVisibility(View.VISIBLE);
                displayProductTabs(productTabData, contentFrame, currentTabIndex, true);
            } else if (isFilterOrSortApplied) {
                //header
                renderHeaderDropDown(productTabData.getProductTabInfos().get(0).getHeaderSection(),
                        productTabData.getProductTabInfos().get(0).getHeaderSelectedIndex(),
                        productTabData.getScreenName());
                //sort and filter
                ProductTabInfo productTabInfo = productTabData.getProductTabInfos().get(0);
                setCurrentTabSortAndFilter(productTabInfo.getFilterOptionItems(),
                        productTabInfo.getFilteredOn(), productTabInfo.getSortOptions(),
                        productTabInfo.getSortedOn(), true);

                ProductInfo productInfo = productTabInfo.getProductInfo();
                ArrayList<Product> products = productInfo != null ? productInfo.getProducts() : null;
                if (mMapForTabWithNoProducts == null) {
                    mMapForTabWithNoProducts = new HashMap<>();
                }
                mMapForTabWithNoProducts.put(tabType, products);
                renderFilterAndSortProductList(productTabInfo, tabType, currentTabIndex);
            } else {
                // When only one product tab
                findViewById(R.id.slidingTabs).setVisibility(View.GONE);
                ProductTabInfo productTabInfo = productTabData.getProductTabInfos().get(0);
                tabType = productTabInfo.getTabType();
                ProductInfo productInfo = productTabInfo.getProductInfo();
                Bundle bundle = getBundleForProductListFragment(productTabInfo, productInfo,
                        productTabData.getBaseImgUrl(), true);
                GenericProductListFragment genericProductListFragment = new GenericProductListFragment();
                genericProductListFragment.setArguments(bundle);
                // Not using onChangeFragment/addToMainLayout since their implementation has been changed in this class
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, genericProductListFragment, genericProductListFragment.getFragmentTxnTag())
                        .commit();
                if (productTabData.getProductTabInfos().get(0).getHeaderSection() != null) {
                    renderHeaderDropDown(productTabData.getProductTabInfos().get(0).getHeaderSection(),
                            productTabData.getProductTabInfos().get(0).getHeaderSelectedIndex(),
                            productTabData.getScreenName());
                } else {
                    String title = TextUtils.isEmpty(mTitlePassedViaIntent) ?
                            productTabInfo.getTabName() : mTitlePassedViaIntent;
                    setTitle(title);
                }
                logProductListingShownEvent(productTabInfo.getTabType());
                setCurrentTabSortAndFilter(productTabInfo.getFilterOptionItems(),
                        productTabInfo.getFilteredOn(), productTabInfo.getSortOptions(),
                        productTabInfo.getSortedOn(), true);
            }
        } else if (contentSectionView == null) {
            findViewById(R.id.slidingTabs).setVisibility(View.GONE);
            UIUtil.showEmptyProductsView(this, contentFrame, getString(R.string.noProducts),
                    R.drawable.empty_smart_basket);
            toggleFilterSortView(false);
            renderHeaderDropDown(null, 0, productTabData.getScreenName());
        } else if (!TextUtils.isEmpty(productTabData.getScreenName())) {
            mTitlePassedViaIntent = productTabData.getScreenName();
            setTitle(productTabData.getScreenName());
        }
    }


    @Override
    public void setTabNameWithEmptyProductView(String tabName) {
        if (mTabNameWithEmptyProductView == null) {
            mTabNameWithEmptyProductView = new ArrayList<>();
        }
        mTabNameWithEmptyProductView.add(tabName);
    }

    private void setCurrentTabSortAndFilter(@Nullable ArrayList<FilterOptionCategory> filterOptionItems,
                                            @Nullable ArrayList<FilteredOn> filteredOns,
                                            @Nullable ArrayList<Option> sortOptions,
                                            @Nullable String sortedOn,
                                            boolean hasProducts) {

        if ((filterOptionItems == null || filterOptionItems.size() == 0) &&
                (sortOptions == null || sortOptions.size() == 0)) {
            toggleFilterSortView(false);
            return;
        }

        boolean showFilters = hasProducts && filterOptionItems != null
                && filterOptionItems.size() > 0;
        toggleFilterSortView(showFilters);
        mFilteredOns = filteredOns;
        mFilterOptionCategories = filterOptionItems;
        mSortedOn = sortedOn;
        mSortOptions = sortOptions;
        if (mNameValuePairs != null) {
            updateNameValuePairWithFilterOns(filteredOns, false);
            updateNameValuePairsWithSortedOn(sortedOn);
        }
    }


    private void toggleFilterSortView(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        findViewById(R.id.layoutFilter).setVisibility(visibility);
        findViewById(R.id.layoutSort).setVisibility(visibility);
    }

    private void displayProductTabs(final ProductTabData productTabData, ViewGroup contentFrame,
                                    final int currentTabIndex, final boolean hasProducts) {
        mViewPager = (ViewPager) getLayoutInflater().inflate(R.layout.uiv3_viewpager, contentFrame, false);
        if (mViewPager == null) return;

        ArrayList<BBTab> bbTabs = new ArrayList<>();
        ArrayList<String> tabTypeWithNoProducts = new ArrayList<>();
        final ArrayList<ProductTabInfo> productTabInfos = productTabData.getProductTabInfos();
        for (int i = 0; i < productTabInfos.size(); i++) {
            ProductTabInfo productTabInfo = productTabInfos.get(i);
            ProductInfo productInfo = productTabInfo.getProductInfo();
            if (productInfo != null) {
                Bundle bundle = getBundleForProductListFragment(productTabInfo,
                        productInfo, productTabData.getBaseImgUrl(), false);
                bbTabs.add(new BBTab<>(productTabInfo.getTabName(),
                        GenericProductListFragment.class, bundle));
                if (productInfo.getCurrentPage() == -1) {
                    if (mArrayTabTypeAndFragmentPosition == null) {
                        mArrayTabTypeAndFragmentPosition = new SparseArray<>();
                    }
                    mArrayTabTypeAndFragmentPosition.put(i, productTabInfo.getTabType());
                    tabTypeWithNoProducts.add(productTabInfo.getTabType());
                }
                if (i == 0) {
                    tabType = productTabInfo.getTabType();
                    logProductListingShownEvent(productTabInfo.getTabType());
                }
            }
        }

        TabPagerAdapterWithFragmentRegistration statePagerAdapter =
                new TabPagerAdapterWithFragmentRegistration(this, getSupportFragmentManager(), bbTabs);
        mViewPager.setAdapter(statePagerAdapter);
        ProductTabInfo productTabInfo = productTabData.getProductTabInfos() != null ?
                productTabData.getProductTabInfos().get(currentTabIndex) : null;

        if (productTabInfo != null) {
            setCurrentTabSortAndFilter(productTabInfo.getFilterOptionItems(), productTabInfo.getFilteredOn(),
                    productTabInfo.getSortOptions(), productTabInfo.getSortedOn(), hasProducts);
        } else {
            setCurrentTabSortAndFilter(null, null, null, null, hasProducts);
        }

        TabLayout pagerSlidingTabStrip = (TabLayout) findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(currentTabIndex);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tabType = productTabInfos.get(position).getTabType();
                if (mViewPager == null || mViewPager.getAdapter() == null) return;
                Fragment fragment = ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(position);
                if (fragment == null || fragment.getArguments() == null) return;
                ProductListAwareFragment pFrag = (ProductListAwareFragment) fragment;
                Bundle args = pFrag.getArguments();
                if (mTabNameWithEmptyProductView != null && mTabNameWithEmptyProductView.contains(tabType)) {
                    toggleFilterSortView(false);
                } else {
                    ArrayList<FilterOptionCategory> filterOptionItems = args.getParcelableArrayList(Constants.FILTER_OPTIONS);
                    ArrayList<FilteredOn> filteredOns = args.getParcelableArrayList(Constants.FILTER_ON);
                    ArrayList<Option> sortOpts = args.getParcelableArrayList(Constants.PRODUCT_SORT_OPTION);
                    String sortOn = args.getString(Constants.SORT_ON);

                    setCurrentTabSortAndFilter(filterOptionItems, filteredOns, sortOpts, sortOn,
                            hasProducts);
                }
                Section headerSection = args.getParcelable(Constants.HEADER_SECTION);
                int headerSelIndx = args.getInt(Constants.HEADER_SEL);
                renderHeaderDropDown(headerSection, headerSelIndx, productTabData.getScreenName());
                HashMap<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(Constants.TAB_NAME, productTabInfos.get(position).getTabType());
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.PRODUCT_LIST_TAB_CHANGED, eventAttribs);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (productTabData.getContentSectionData() != null) {
            LinearLayout layoutProducts = new LinearLayout(this);
            layoutProducts.setOrientation(LinearLayout.VERTICAL);
            View sectionView = new SectionView(this, faceRobotoRegular,
                    productTabData.getContentSectionData(), "Product List").getView();
            if (sectionView != null) {
                layoutProducts.addView(sectionView);
            }
            layoutProducts.addView(mViewPager);
            contentFrame.addView(layoutProducts);
        } else {
            contentFrame.addView(mViewPager);
        }

        // Setup title
        if (productTabData.getProductTabInfos() != null &&
                productTabInfo != null &&
                productTabInfo.getHeaderSection() != null) {
            mTitlePassedViaIntent = "";
            renderHeaderDropDown(productTabInfo.getHeaderSection(),
                    productTabInfo.getHeaderSelectedIndex(),
                    productTabData.getScreenName());
        } else if (!TextUtils.isEmpty(productTabData.getScreenName())) {
            mTitlePassedViaIntent = productTabData.getScreenName();
            setTitle(productTabData.getScreenName());
        } else if (!TextUtils.isEmpty(mTitlePassedViaIntent)) {
            setTitle(mTitlePassedViaIntent);
        }

        if (tabTypeWithNoProducts.size() > 0) {
            ArrayList<NameValuePair> newNameValuePairs;
            if (mNameValuePairs != null) {
                newNameValuePairs = new ArrayList<>(mNameValuePairs);
            } else {
                newNameValuePairs = new ArrayList<>();
            }

            newNameValuePairs.add(new NameValuePair(Constants.TAB_TYPE, new Gson().toJson(tabTypeWithNoProducts)));
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            bigBasketApiService.productNextPage(NameValuePair.toMap(newNameValuePairs),
                    new Callback<ApiResponse<ProductNextPageResponse>>() {
                        @Override
                        public void success(ApiResponse<ProductNextPageResponse> productNextPageApiResponse, Response response) {
                            if (isSuspended()) return;
                            if (productNextPageApiResponse.status == 0) {
                                if (mMapForTabWithNoProducts != null) {
                                    if (productNextPageApiResponse.apiResponseContent.productListMap != null) {
                                        mMapForTabWithNoProducts.putAll(productNextPageApiResponse.apiResponseContent.productListMap);
                                    }
                                } else {
                                    mMapForTabWithNoProducts = productNextPageApiResponse.apiResponseContent.productListMap;
                                }
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
    }

    private void renderFilterAndSortProductList(ProductTabInfo productTabInfo,
                                                String filterAndSortTabName, int currentTabIndex) {
        if (filterAndSortTabName == null || mViewPager == null || productTabInfo == null
                || productTabInfo.getProductInfo() == null) return;
        Fragment fragment = ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(currentTabIndex);
        if (fragment != null) {
            ArrayList<Product> products = productTabInfo.getProductInfo().getProducts();
            if (products == null) {
                ((ProductListAwareFragment) fragment).setLazyProductLoadingFailure();
            }
            ((ProductListAwareFragment) fragment).updateProductInfo(productTabInfo.getProductInfo(), mNameValuePairs);
            if (fragment.getArguments() != null) {
                fragment.getArguments().putParcelable(Constants.PRODUCT_INFO, productTabInfo.getProductInfo());
                fragment.getArguments().putParcelableArrayList(Constants.FILTER_ON, productTabInfo.getFilteredOn());
                fragment.getArguments().putParcelableArrayList(Constants.FILTER_OPTIONS, productTabInfo.getFilterOptionItems());
                fragment.getArguments().putString(Constants.SORT_ON, productTabInfo.getSortedOn());
                fragment.getArguments().putParcelableArrayList(Constants.PRODUCT_SORT_OPTION, productTabInfo.getSortOptions());
                fragment.getArguments().putParcelable(Constants.HEADER_SECTION, productTabInfo.getHeaderSection());
                fragment.getArguments().putInt(Constants.HEADER_SEL, productTabInfo.getHeaderSelectedIndex());
                fragment.getArguments().putParcelableArrayList(Constants.PRODUCT_QUERY, mNameValuePairs);
            }
        }
    }


    public void logProductListingShownEvent(String mTabType) {
        if (mTabType == null) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAB_NAME, mTabType);
        map = getProductListEventParams(mNameValuePairs, map);
        trackEvent(TrackingAware.PRODUCT_LIST_SHOWN, map);
        trackEventAppsFlyer(TrackingAware.PRODUCT_LIST_SHOWN);
    }


    private HashMap<String, String> getProductListEventParams(ArrayList<NameValuePair> nameValuePairs,
                                                              HashMap<String, String> map) {
        if (nameValuePairs == null || nameValuePairs.size() == 0) return map;
        boolean is_express = false, filterApplied = false;
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (nameValuePair.getName() != null && nameValuePair.getValue() != null) {
                if (nameValuePair.getName().equalsIgnoreCase("is_express"))
                    is_express = true;
                else if (nameValuePair.getName().equalsIgnoreCase("filter_on"))
                    filterApplied = true;
                else
                    map.put(nameValuePair.getName(), nameValuePair.getValue());
            }
        }
        map.put(TrackEventkeys.IS_EXPRESS, is_express ? "yes" : "no");
        map.put(TrackEventkeys.FILTER_APPLIED, filterApplied ? "yes" : "no");
        return map;
    }

    private Bundle getBundleForProductListFragment(ProductTabInfo productTabInfo,
                                                   ProductInfo productInfo,
                                                   String baseImgUrl, boolean singleTab) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.PRODUCT_INFO, productInfo);
        bundle.putString(Constants.BASE_IMG_URL, baseImgUrl);
        bundle.putParcelableArrayList(Constants.PRODUCT_QUERY, mNameValuePairs);

        bundle.putParcelableArrayList(Constants.FILTER_ON, productTabInfo.getFilteredOn());
        bundle.putParcelableArrayList(Constants.FILTER_OPTIONS, productTabInfo.getFilterOptionItems());
        bundle.putString(Constants.SORT_ON, productTabInfo.getSortedOn());
        bundle.putParcelableArrayList(Constants.PRODUCT_SORT_OPTION, productTabInfo.getSortOptions());
        bundle.putParcelable(Constants.HEADER_SECTION, productTabInfo.getHeaderSection());
        bundle.putInt(Constants.HEADER_SEL, productTabInfo.getHeaderSelectedIndex());

        bundle.putString(Constants.TAB_TYPE, productTabInfo.getTabType());
        bundle.putBoolean(Constants.SINGLE_TAB, singleTab);

        return bundle;
    }

    private void notifyEmptyFragmentAboutFailure() {
        if (mArrayTabTypeAndFragmentPosition != null) {
            for (int i = 0; i < mArrayTabTypeAndFragmentPosition.size(); i++) {
                notifyFragmentAtPositionAboutFailure(i);
            }
        }
    }

    private void setUpProductsInEmptyFragments() {
        if (mArrayTabTypeAndFragmentPosition != null && mViewPager != null) {
            int currentPosition = mViewPager.getCurrentItem();
            setProductListForFragmentAtPosition(currentPosition);
            setProductListForFragmentAtPosition(currentPosition + 1);
            setProductListForFragmentAtPosition(currentPosition - 1);
        }
    }

    @Override
    public void doSearch(String searchQuery, String referrer) {
        if (!TextUtils.isEmpty(searchQuery)) {
            mTitlePassedViaIntent = searchQuery;
            mNameValuePairs = new ArrayList<>();
            mNameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH));
            mNameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery));
            if (getSupportFragmentManager().getFragments() != null &&
                    getSupportFragmentManager().getFragments().size() > 0) {
                // New product list is requested over current page, so change nc by copying next-nc
                setCurrentNavigationContext(referrer);
            }
            loadProductTabs(mViewPager != null ? mViewPager.getCurrentItem() : 0, false);
        }
    }

    private void setProductListForFragmentAtPosition(int position) {
        String tabType = mArrayTabTypeAndFragmentPosition.get(position);
        if (tabType == null || mViewPager == null) return;
        Fragment fragment = ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(position);
        if (fragment != null) {
            ArrayList<Product> products = mMapForTabWithNoProducts != null ?
                    mMapForTabWithNoProducts.get(tabType) : null;
            if (products != null) {
                ((ProductListAwareFragment) fragment).insertProductList(products);
            } else {
                ((ProductListAwareFragment) fragment).insertProductList(null);
            }
        }
    }

    private void notifyFragmentAtPositionAboutFailure(int position) {
        String tabType = mArrayTabTypeAndFragmentPosition.get(position);
        if (tabType == null || mViewPager == null) return;
        Fragment fragment = ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(position);
        if (fragment != null) {
            ((ProductListAwareFragment) fragment).setLazyProductLoadingFailure();
            ((ProductListAwareFragment) fragment).setProductListView();
        }
    }

    @Nullable
    private Fragment getCurrentFragment() {
        if (mViewPager == null) return null;
        int currentPosition = mViewPager.getCurrentItem();
        return ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(currentPosition);
    }

    @Override
    @Nullable
    public Pair<ArrayList<Product>, Integer> provideProductsIfAvailable(String tabType) {
        if (mMapForTabWithNoProducts != null) {
            // This means product has been downloaded
            ArrayList<Product> products = mMapForTabWithNoProducts.get(tabType);
            if (products != null) {
                return new Pair<>(products, 1);
            } else {
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment != null) {
                    ((ProductListAwareFragment) getCurrentFragment()).setLazyProductLoadingFailure();
                }
            }
        }
        return null;
    }

    private void renderHeaderDropDown(@Nullable final Section headSection, int mHeaderSelectedIdx,
                                      String screenName) {
        Toolbar toolbar = getToolbar();
        if (mToolbarTextDropdown == null) {
            mToolbarTextDropdown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                .withCtx(this)
                .withDefaultSelectedIdx(mHeaderSelectedIdx)
                .withFallbackHeaderTitle(!TextUtils.isEmpty(mTitlePassedViaIntent) ? mTitlePassedViaIntent : screenName)
                .withHeadSection(headSection)
                .withImgCloseChildDropdown((ImageView) findViewById(R.id.imgCloseChildDropdown))
                .withLayoutChildToolbarContainer((ViewGroup) findViewById(R.id.layoutChildToolbarContainer))
                .withLayoutListHeader((ViewGroup) findViewById(R.id.layoutListHeader))
                .withListHeaderDropdown((ListView) findViewById(R.id.listHeaderDropdown))
                .withToolbar(getToolbar())
                .withTxtChildDropdownTitle((TextView) findViewById(R.id.txtListDialogTitle))
                .withTxtToolbarDropdown(mToolbarTextDropdown)
                .withTypeface(faceRobotoRegular)
                .build()
                .setView();
    }

    @Override
    public boolean isNextPageLoading() {
        return false;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {

    }

    @Override
    public void launchProductList(ArrayList<NameValuePair> nameValuePairs,
                                  @Nullable String sectionName, @Nullable String sectionItemName) {
        mNameValuePairs = nameValuePairs;
        if (!TextUtils.isEmpty(sectionItemName)) {
            mTitlePassedViaIntent = sectionItemName;
        }
        if (getSupportFragmentManager().getFragments() != null &&
                getSupportFragmentManager().getFragments().size() > 0) {
            // New product list is requested over current page, so change nc by copying next-nc
            setCurrentNavigationContext(getNextScreenNavigationContext());
        }
        loadProductTabs(mViewPager != null ? mViewPager.getCurrentItem() : 0, false);
    }

    public void onFooterViewClicked(View v) {
        switch (v.getId()) {
            case R.id.layoutFilter:
                onFilterScreenRequested();
                break;
            case R.id.layoutSort:
                onSortRequested();
                break;
        }
    }

    private void onSortRequested() {
        if (mSortOptions == null || mSortOptions.size() == 0) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int checkedItem = 0;
        String[] sortOptnsArr = new String[mSortOptions.size()];
        for (int i = 0; i < mSortOptions.size(); i++) {
            sortOptnsArr[i] = mSortOptions.get(i).getSortName();
            if (mSortedOn != null && mSortOptions.get(i).getSortSlug().equals(mSortedOn)) {
                checkedItem = i;
            }
        }
        builder.setTitle(R.string.sortBy)
                .setSingleChoiceItems(sortOptnsArr, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.sort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if (selectedPosition != ListView.INVALID_POSITION) {
                            applySort(mSortOptions.get(selectedPosition).getSortSlug(),
                                    mSortOptions.get(selectedPosition).getSortName());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new OnDialogShowListener());
        alertDialog.show();
    }

    private void onFilterScreenRequested() {
        trackEvent(TrackingAware.PRODUCT_LIST_FILTER_CLICKED, null);
        Intent sortFilterIntent = new Intent(this, FilterActivity.class);
        sortFilterIntent.putExtra(Constants.FILTER_OPTIONS, mFilterOptionCategories);
        sortFilterIntent.putExtra(Constants.FILTERED_ON, mFilteredOns);
        startActivityForResult(sortFilterIntent, NavigationCodes.FILTER_APPLIED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.FILTER_APPLIED) {
            ArrayList<FilteredOn> filteredOns = null;
            if (data != null) {
                filteredOns = data.getParcelableArrayListExtra(Constants.FILTERED_ON);
            }
            applyFilter(filteredOns);
        } else if (resultCode == NavigationCodes.SHOPPING_LIST_MODIFIED) {
            getProducts(mViewPager != null ? mViewPager.getCurrentItem() : 0);
        } else if (resultCode == NavigationCodes.BASKET_CHANGED) {
            if (data != null && !TextUtils.isEmpty(data.getStringExtra(Constants.SKU_ID)) &&
                    data.getIntExtra(Constants.PRODUCT_NO_ITEM_IN_CART, 0) > 0) {
                mCartInfo.put(data.getStringExtra(Constants.SKU_ID),
                        data.getIntExtra(Constants.PRODUCT_NO_ITEM_IN_CART, 0));
                setCartInfo(mCartInfo);
            } else {
                getProducts(mViewPager != null ? mViewPager.getCurrentItem() : 0);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void applyFilter(@Nullable ArrayList<FilteredOn> filteredOns) {
        if (mNameValuePairs == null) return;
        updateNameValuePairWithFilterOns(filteredOns, true);
        callApiForSortAndApplied();
    }

    private void updateNameValuePairWithFilterOns(@Nullable ArrayList<FilteredOn> filteredOns,
                                                  boolean trackFilterEvent) {
        NameValuePair filterOnNameValuePair = getFilterOnNameValuePair();
        if (filterOnNameValuePair == null) {
            if (filteredOns == null) {
                return;
            }
            filterOnNameValuePair = new NameValuePair(Constants.FILTER_ON,
                    new Gson().toJson(filteredOns));
            mNameValuePairs.add(filterOnNameValuePair);
            if (trackFilterEvent) {
                trackFilterAppliedEvent(filteredOns);
            }
        } else {
            if (filteredOns != null) {
                filterOnNameValuePair.setValue(new Gson().toJson(filteredOns));
                if (trackFilterEvent) {
                    trackFilterAppliedEvent(filteredOns);
                }
            } else {
                mNameValuePairs.remove(filterOnNameValuePair);
            }
        }
    }

    private void applySort(String sortedOn, String mSortedOnName) {
        if (sortedOn == null || sortedOn.equals(mSortedOn)) return;
        updateNameValuePairsWithSortedOn(sortedOn);
        trackSortByEvent(mSortedOnName);
        callApiForSortAndApplied();

    }

    private void updateNameValuePairsWithSortedOn(String sortedOn) {
        if (sortedOn == null) sortedOn = "";
        NameValuePair sortedOnNameValuePair = getSortedOnNameValuePair();
        if (sortedOnNameValuePair == null) {
            sortedOnNameValuePair = new NameValuePair(Constants.SORT_ON, sortedOn);
            mNameValuePairs.add(sortedOnNameValuePair);
        } else {
            sortedOnNameValuePair.setValue(sortedOn);
        }
    }

    private void callApiForSortAndApplied() {
        ArrayList<String> sortAndFilterArrayList = new ArrayList<>();
        sortAndFilterArrayList.add(tabType);
        mNameValuePairs.add(new NameValuePair(Constants.TAB_TYPE, new Gson().toJson(sortAndFilterArrayList)));
        loadProductTabs(mViewPager != null ? mViewPager.getCurrentItem() : 0, true);
    }

    private void trackSortByEvent(String sortedOn) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAME, sortedOn);
        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        trackEvent(TrackingAware.SORT_BY, map);
    }

    @Nullable
    private NameValuePair getSortedOnNameValuePair() {
        for (NameValuePair nameValuePair : mNameValuePairs) {
            if (nameValuePair.getName().equals(Constants.SORT_ON)) {
                return nameValuePair;
            }
        }
        return null;
    }

    @Nullable
    private NameValuePair getFilterOnNameValuePair() {
        for (NameValuePair nameValuePair : mNameValuePairs) {
            if (nameValuePair.getName().equals(Constants.FILTER_ON)) {
                return nameValuePair;
            }
        }
        return null;
    }

    private void trackFilterAppliedEvent(ArrayList<FilteredOn> filteredOnArrayList) {
        if (filteredOnArrayList == null || filteredOnArrayList.size() == 0) {
            trackEvent(TrackingAware.FILTER_CLEARED, null);
        } else {
            if (mFilterOptionCategories != null && mFilterOptionCategories.size() > 0)
                new FilterTrackEvent(this, getNextScreenNavigationContext(),
                        mFilterOptionCategories, filteredOnArrayList).execute();
        }
    }


    private static class FilterTrackEvent extends AsyncTask<Void, Void, Map<String, String>> {

        private Context context;
        private Map<String, String> eventAttribs;
        private String mNextScreenNavigationContext;
        private ArrayList<FilterOptionCategory> mFilterOptionCategories;
        private ArrayList<FilteredOn> filteredOnArrayList;

        FilterTrackEvent(Context context, String mNextScreenNavigationContext,
                         ArrayList<FilterOptionCategory> mFilterOptionCategories,
                         ArrayList<FilteredOn> filteredOnArrayList) {
            this.context = context;
            this.mNextScreenNavigationContext = mNextScreenNavigationContext;
            this.mFilterOptionCategories = mFilterOptionCategories;
            this.filteredOnArrayList = filteredOnArrayList;
            eventAttribs = new HashMap<>();
        }

        @Override
        protected Map<String, String> doInBackground(Void... Void) {
            for (FilteredOn filteredOn : filteredOnArrayList) {
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, mNextScreenNavigationContext);
                eventAttribs.put(filteredOn.getFilterSlug(), UIUtil.strJoin(getFilterDisplayName(filteredOn,
                        mFilterOptionCategories), ","));
            }
            return eventAttribs;
        }

        @Override
        protected void onPostExecute(Map<String, String> map) {
            super.onPostExecute(map);
            ((ActivityAware) context).getCurrentActivity().trackEvent(TrackingAware.FILTER_APPLIED, map,
                    null, null, false);
        }
    }

    private static ArrayList<String> getFilterDisplayName(final FilteredOn filteredOn,
                                                          ArrayList<FilterOptionCategory> mFilterOptionCategories) {
        final ArrayList<String> filterDisplayNameArrayList = new ArrayList<>();
        for (FilterOptionCategory filterOptionCategory : mFilterOptionCategories) {
            if (filterOptionCategory.getFilterSlug().equals(filteredOn.getFilterSlug())) {
                for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                    for (String filterValue : filteredOn.getFilterValues()) {
                        if (filterOptionItem.getFilterValueSlug().equals(filterValue)) {
                            filterDisplayNameArrayList.add(filterOptionItem.getDisplayName());
                        }
                    }
                }
            }
        }
        return filterDisplayNameArrayList;
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket, Product product,
                                                    String qty, @Nullable View productView, @Nullable HashMap<String, Integer> cartInfoMap,
                                                    @Nullable EditText editTextQty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty,
                viewIncQty, btnAddToBasket, product, qty, productView, cartInfoMap, editTextQty);
        if (cartInfoMap != null) {
            // Sync local cartInfoMap with this one
            //mCartInfo = cartInfoMap;
            setCartInfo(cartInfoMap);
            // Update in-memory fragments
//            if (mViewPager != null) { // if list page don't have tabs
//                if(mViewPager.getCurrentItem() == 1){
//                    redrawFragment(0);
//                }else {
//                    setProductListForFragmentAtPosition(mViewPager.getCurrentItem() - 1);
//                }
//                setProductListForFragmentAtPosition(mViewPager.getCurrentItem() + 1);
//            }
        }
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null && Constants.NOT_ALPHANUMERIC_TXT_SHOPPING_LIST.equals(sourceName)) {
            new CreateShoppingListTask<>(this).showDialog();
        }
    }

    @Nullable
    @Override
    public HashMap<String, Integer> getCartInfo() {
        return mCartInfo;
    }

    @Override
    public void setCartInfo(HashMap<String, Integer> cartInfo) {
        mCartInfo = cartInfo;
        // Refresh product list

        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            redrawFragment(currentItem - 1);
            redrawFragment(currentItem);
            redrawFragment(currentItem + 1);
        } else {
            // There's only one fragment
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(GenericProductListFragment.class.getName());
            if (fragment != null) {
                ((ProductListAwareFragment) fragment).redrawProductList(mCartInfo);
            }
        }
    }

    private void redrawFragment(int position) {
        if (mViewPager == null) return;
        Fragment fragment = ((TabPagerAdapterWithFragmentRegistration) mViewPager.getAdapter()).getRegisteredFragment(position);
        if (fragment != null) {
            ((ProductListAwareFragment) fragment).redrawProductList(mCartInfo);
        }
    }

}