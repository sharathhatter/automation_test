package com.bigbasket.mobileapp.activity.product;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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
import com.bigbasket.mobileapp.adapter.ProductListPagerAdapter;
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
import android.os.AsyncTask;

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
    private int mHeaderSelectedIdx;
    private ArrayList<FilteredOn> mFilteredOns;
    private ArrayList<FilterOptionCategory> mFilterOptionCategories;
    private String mSortedOn;
    private ArrayList<Option> mSortOptions;
    private HashMap<String, Integer> mCartInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProducts();
    }

    private void getProducts() {
        mTitlePassedViaIntent = getIntent().getStringExtra(Constants.TITLE);
        setTitle(mTitlePassedViaIntent);
        mNameValuePairs = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_QUERY);
        loadProductTabs();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNameValuePairs != null)
            setNextScreenNavigationContext(NameValuePair.buildNavigationContext(mNameValuePairs));
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
        // Reset all Globals
        mViewPager = null;
        mMapForTabWithNoProducts = null;
        mArrayTabTypeAndFragmentPosition = null;

        HashMap<String, String> paramMap = NameValuePair.toMap(mNameValuePairs);
        setNextScreenNavigationContext(NameValuePair.buildNavigationContext(mNameValuePairs));
        new ProductListTask<>(this, paramMap, getCurrentNavigationContext()).startTask();
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
    public void setProductTabData(ProductTabData productTabData) {
        if (getDrawerLayout() != null) {
            getDrawerLayout().closeDrawers();
        }
        mHeaderSelectedIdx = productTabData.getHeaderSelectedIndex();
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();

        mFilteredOns = productTabData.getFilteredOn();
        mFilterOptionCategories = productTabData.getFilterOptionItems();
        mSortedOn = productTabData.getSortedOn();
        mSortOptions = productTabData.getSortOptions();

        SectionData sectionData = productTabData.getContentSectionData();
        View contentSectionView = null;
        boolean hasProducts = productTabData.getProductTabInfos() != null &&
                productTabData.getProductTabInfos().size() > 0;
        if (sectionData != null) {
            if (!hasProducts) {
                contentSectionView = new SectionView(this, faceRobotoRegular, sectionData, "Product List").getRecyclerView(contentFrame);
            } else {
                contentSectionView = new SectionView(this, faceRobotoRegular, sectionData, "Product List").getView();
            }
            if (contentSectionView != null) {
                contentFrame.addView(contentSectionView);
            }
        }
        boolean showFilters = hasProducts && productTabData.getFilterOptionItems() != null
                && productTabData.getFilterOptionItems().size() > 0;
        toggleFilterSortView(showFilters);
        mCartInfo = productTabData.getCartInfo();
        if (hasProducts) {
            // Setup title
            if (productTabData.getHeaderSection() != null &&
                    productTabData.getHeaderSection().getSectionItems() != null &&
                    productTabData.getHeaderSection().getSectionItems().size() > 0) {
                mTitlePassedViaIntent = "";
                setTitle(null);
            } else if (!TextUtils.isEmpty(productTabData.getScreenName())) {
                mTitlePassedViaIntent = productTabData.getScreenName();
                setTitle(productTabData.getScreenName());
            } else if (!TextUtils.isEmpty(mTitlePassedViaIntent)) {
                setTitle(mTitlePassedViaIntent);
            }

            // Setup content
            if (productTabData.getProductTabInfos().size() > 1) {
                findViewById(R.id.slidingTabs).setVisibility(View.VISIBLE);
                displayProductTabs(productTabData, contentFrame);
            } else {
                // When only one product tab
                findViewById(R.id.slidingTabs).setVisibility(View.GONE);
                ProductTabInfo productTabInfo = productTabData.getProductTabInfos().get(0);
                ProductInfo productInfo = productTabInfo.getProductInfo();
                Bundle bundle = getBundleForProductListFragment(productTabInfo, productInfo,
                        productTabData.getBaseImgUrl(), true);
                GenericProductListFragment genericProductListFragment = new GenericProductListFragment();
                genericProductListFragment.setArguments(bundle);
                // Not using onChangeFragment/addToMainLayout since their implementation has been changed in this class
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, genericProductListFragment, genericProductListFragment.getFragmentTxnTag())
                        .commit();
                if (productTabData.getHeaderSection() != null &&
                        productTabData.getHeaderSection().getSectionItems() != null &&
                        productTabData.getHeaderSection().getSectionItems().size() > 0) {
                    setTitle(null);
                } else {
                    String title = TextUtils.isEmpty(mTitlePassedViaIntent) ?
                            productTabInfo.getTabName() : mTitlePassedViaIntent;
                    setTitle(title);
                }
                logProductListingShownEvent(productTabInfo.getTabType());
                renderHeaderDropDown(productTabData.getHeaderSection());
            }
        } else if (contentSectionView == null) {
            findViewById(R.id.slidingTabs).setVisibility(View.GONE);
            UIUtil.showEmptyProductsView(this, contentFrame, getString(R.string.noProducts),
                    R.drawable.empty_smart_basket);
            renderHeaderDropDown(null);
        } else if (!TextUtils.isEmpty(productTabData.getScreenName())) {
            mTitlePassedViaIntent = productTabData.getScreenName();
            setTitle(productTabData.getScreenName());
        }
    }

    private void toggleFilterSortView(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        findViewById(R.id.layoutFilter).setVisibility(visibility);
        findViewById(R.id.layoutSort).setVisibility(visibility);
    }

    private void displayProductTabs(ProductTabData productTabData, ViewGroup contentFrame) {
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
                if (i == 0)
                    logProductListingShownEvent(productTabInfo.getTabType());
            }
        }

        ProductListPagerAdapter statePagerAdapter =
                new ProductListPagerAdapter(this, getSupportFragmentManager(), bbTabs);
        mViewPager.setAdapter(statePagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                HashMap<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(Constants.TAB_NAME, productTabInfos.get(position).getTabType());
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.PRODUCT_LIST_TAB_CHANGED, eventAttribs);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout pagerSlidingTabStrip = (TabLayout) findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setupWithViewPager(mViewPager);
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
        renderHeaderDropDown(productTabData.getHeaderSection());

        if (tabTypeWithNoProducts.size() > 0) {
            ArrayList<NameValuePair> newNameValuePairs;
            if (mNameValuePairs != null) {
                newNameValuePairs = new ArrayList<>(mNameValuePairs);
            } else {
                newNameValuePairs = new ArrayList<>();
            }

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
    }


    public void logProductListingShownEvent(String mTabType) {
        if (mTabType == null) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAB_NAME, mTabType);
        map = getProductListEventParams(mNameValuePairs, map);
        trackEvent(TrackingAware.PRODUCT_LIST_SHOWN, map);
    }


    private HashMap<String, String> getProductListEventParams(ArrayList<NameValuePair> nameValuePairs,
                                             HashMap<String, String> map) {
        if (nameValuePairs == null || nameValuePairs.size() == 0) return map;
        boolean is_express= false, filterApplied = false;
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (nameValuePair.getName() != null && nameValuePair.getValue() != null) {
                if(nameValuePair.getName().equalsIgnoreCase("is_express"))
                    is_express = true;
                else if(nameValuePair.getName().equalsIgnoreCase("filter_on"))
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
            mNameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
            mNameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery));
            if (getSupportFragmentManager().getFragments() != null &&
                    getSupportFragmentManager().getFragments().size() > 0) {
                // New product list is requested over current page, so change nc by copying next-nc
                setCurrentNavigationContext(referrer);
            }
            loadProductTabs();
        }
    }

    private void setProductListForFragmentAtPosition(int position) {
        String tabType = mArrayTabTypeAndFragmentPosition.get(position);
        if (tabType == null || mViewPager == null) return;
        Fragment fragment = ((ProductListPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(position);
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
        Fragment fragment = ((ProductListPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(position);
        if (fragment != null) {
            ((ProductListAwareFragment) fragment).setLazyProductLoadingFailure();
            ((ProductListAwareFragment) fragment).setProductListView();
        }
    }

    @Nullable
    private Fragment getCurrentFragment() {
        if (mViewPager == null) return null;
        int currentPosition = mViewPager.getCurrentItem();
        return ((ProductListPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(currentPosition);
    }

    @Override
    @Nullable
    public ArrayList<Product> provideProductsIfAvailable(String tabType) {
        if (mMapForTabWithNoProducts != null) {
            // This means product has been downloaded
            ArrayList<Product> products = mMapForTabWithNoProducts.get(tabType);
            if (products != null) {
                return products;
            } else {
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment != null) {
                    ((ProductListAwareFragment) getCurrentFragment()).setLazyProductLoadingFailure();
                }
            }
        }
        return null;
    }

    private void renderHeaderDropDown(@Nullable final Section headSection) {
        Toolbar toolbar = getToolbar();
        if (mToolbarTextDropdown == null) {
            mToolbarTextDropdown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                .withCtx(this)
                .withDefaultSelectedIdx(mHeaderSelectedIdx)
                .withFallbackHeaderTitle(mTitlePassedViaIntent)
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
        loadProductTabs();
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
        } else if (resultCode == NavigationCodes.BASKET_CHANGED) {
            onBasketChanged(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void applyFilter(@Nullable ArrayList<FilteredOn> filteredOns) {
        if (mNameValuePairs == null) return;
        NameValuePair filterOnNameValuePair = getFilterOnNameValuePair();
        if (filterOnNameValuePair == null) {
            if (filteredOns == null) {
                return;
            }
            filterOnNameValuePair = new NameValuePair(Constants.FILTER_ON,
                    new Gson().toJson(filteredOns));
            mNameValuePairs.add(filterOnNameValuePair);
            trackFilterAppliedEvent(filteredOns);
        } else {
            if (filteredOns != null) {
                filterOnNameValuePair.setValue(new Gson().toJson(filteredOns));
                trackFilterAppliedEvent(filteredOns);
            } else {
                mNameValuePairs.remove(filterOnNameValuePair);
            }
        }
        loadProductTabs();
    }

    private void applySort(String sortedOn, String mSortedOnName) {
        if (sortedOn == null || sortedOn.equals(mSortedOn)) return;
        NameValuePair sortedOnNameValuePair = getSortedOnNameValuePair();
        if (sortedOnNameValuePair == null) {
            sortedOnNameValuePair = new NameValuePair(Constants.SORT_ON, sortedOn);
            mNameValuePairs.add(sortedOnNameValuePair);
        } else {
            sortedOnNameValuePair.setValue(sortedOn);
        }
        trackSortByEvent(mSortedOnName);
        loadProductTabs();
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
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket, Product product,
                                                    String qty, @Nullable View productView, @Nullable HashMap<String, Integer> cartInfoMap,
                                                    @Nullable EditText editTextQty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty,
                viewIncQty, btnAddToBasket, product, qty, productView, cartInfoMap, editTextQty);
        if (cartInfoMap != null) {
            // Sync local cartInfoMap with this one
            mCartInfo = cartInfoMap;
            // Update in-memory fragments
            if (mViewPager != null) { // if list page don't have tabs
                setProductListForFragmentAtPosition(mViewPager.getCurrentItem() - 1);
                setProductListForFragmentAtPosition(mViewPager.getCurrentItem() + 1);
            }
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
                ((ProductListAwareFragment) fragment).redrawProductList();
            }
        }
    }

    private void redrawFragment(int position) {
        if (mViewPager == null) return;
        Fragment fragment = ((ProductListPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(position);
        if (fragment != null) {
            ((ProductListAwareFragment) fragment).redrawProductList();
        }
    }

    @Override
    public void onBasketChanged(Intent data) {
        super.onBasketChanged(data);
        getProducts();
    }
}