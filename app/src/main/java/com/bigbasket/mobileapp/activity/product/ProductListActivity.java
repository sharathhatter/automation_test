package com.bigbasket.mobileapp.activity.product;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.LazyProductListAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductTabData;
import com.bigbasket.mobileapp.model.product.ProductTabInfo;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
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

        if (paramMap != null && paramMap.containsKey(Constants.TYPE)) {
            Map<String, String> eventAttribs = new HashMap<>();
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, paramMap.get(Constants.TYPE));
            trackEvent(TrackingAware.PRODUCT_LIST_SHOWN, eventAttribs);
        }

        new ProductListTask<>(this, paramMap).startTask();
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
                displayProductTabs(productTabData, contentFrame);
            } else {
                // When only one product tab
                ProductTabInfo productTabInfo = productTabData.getProductTabInfos().get(0);
                ProductInfo productInfo = productTabInfo.getProductInfo();
                Bundle bundle = getBundleForProductListFragment(productTabInfo, productInfo,
                        productTabData.getBaseImgUrl());
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
                renderHeaderDropDown(productTabData.getHeaderSection());
            }
        } else if (contentSectionView == null) {
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
        View base = getLayoutInflater().inflate(R.layout.uiv3_swipe_tab_view, contentFrame, false);
        mViewPager = (ViewPager) base.findViewById(R.id.pager);
        if (mViewPager == null) return;

        ArrayList<BBTab> bbTabs = new ArrayList<>();
        ArrayList<String> tabTypeWithNoProducts = new ArrayList<>();
        final ArrayList<ProductTabInfo> productTabInfos = productTabData.getProductTabInfos();
        for (int i = 0; i < productTabInfos.size(); i++) {
            ProductTabInfo productTabInfo = productTabInfos.get(i);
            ProductInfo productInfo = productTabInfo.getProductInfo();
            if (productInfo != null) {
                Bundle bundle = getBundleForProductListFragment(productTabInfo,
                        productInfo, productTabData.getBaseImgUrl());
                bbTabs.add(new BBTab<>(productTabInfo.getTabName(),
                        GenericProductListFragment.class, bundle));
                if (productInfo.getCurrentPage() == -1) {
                    if (mArrayTabTypeAndFragmentPosition == null) {
                        mArrayTabTypeAndFragmentPosition = new SparseArray<>();
                    }
                    mArrayTabTypeAndFragmentPosition.put(i, productTabInfo.getTabType());
                    tabTypeWithNoProducts.add(productTabInfo.getTabType());
                }
            }
        }

        ProductListPagerAdapter statePagerAdapter =
                new ProductListPagerAdapter(this, getSupportFragmentManager(), bbTabs);
        mViewPager.setAdapter(statePagerAdapter);

        SmartTabLayout pagerSlidingTabStrip = (SmartTabLayout) base.findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setViewPager(mViewPager);
        pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                HashMap<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(Constants.TAB_NAME, productTabInfos.get(position).getTabName());
                trackEvent(TrackingAware.PRODUCT_LIST_TAB_CHANGED, eventAttribs);
            }

            @Override
            public void onPageSelected(int position) {

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
    }

    private Bundle getBundleForProductListFragment(ProductTabInfo productTabInfo,
                                                   ProductInfo productInfo,
                                                   String baseImgUrl) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.PRODUCT_INFO, productInfo);
        bundle.putString(Constants.BASE_IMG_URL, baseImgUrl);
        bundle.putParcelableArrayList(Constants.PRODUCT_QUERY, mNameValuePairs);
        bundle.putString(Constants.TAB_TYPE, productTabInfo.getTabType());
        bundle.putString(TrackEventkeys.NAVIGATION_CTX, getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
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
        ViewGroup layoutChildToolbarContainer = (ViewGroup) findViewById(R.id.layoutChildToolbarContainer);
        ListView listChildProducts = (ListView) findViewById(R.id.listHeaderDropdown);
        TextView txtChildDropdownTitle = (TextView) findViewById(R.id.txtListDialogTitle);
        if (headSection != null && headSection.getSectionItems() != null
                && headSection.getSectionItems().size() > 0) {

            final OnChildDropdownRequested onChildDropdownRequested = new OnChildDropdownRequested(layoutChildToolbarContainer);
            if (mToolbarTextDropdown == null) {
                mToolbarTextDropdown = (TextView) getLayoutInflater().inflate(R.layout.uiv3_product_header_text, toolbar, false);
                mToolbarTextDropdown.setTypeface(faceRobotoRegular);
                toolbar.addView(mToolbarTextDropdown);
                mToolbarTextDropdown.setOnClickListener(onChildDropdownRequested);
            }
            findViewById(R.id.imgCloseChildDropdown).setOnClickListener(onChildDropdownRequested);

            if (mHeaderSelectedIdx >= headSection.getSectionItems().size()) {
                // Defensive check
                mHeaderSelectedIdx = 0;
            }

            SectionTextItem title = headSection.getSectionItems().get(mHeaderSelectedIdx).getTitle();
            mToolbarTextDropdown.setText(title != null ? title.getText() : "");
            txtChildDropdownTitle.setTypeface(faceRobotoRegular);
            txtChildDropdownTitle.setText(title != null ? title.getText() : "");
            txtChildDropdownTitle.setOnClickListener(onChildDropdownRequested);

            BBArrayAdapter bbArrayAdapter = new BBArrayAdapter<>(this, R.layout.uiv3_product_header_list_item, headSection.getSectionItems(),
                    faceRobotoRegular, getResources().getColor(R.color.uiv3_primary_text_color), Color.WHITE);
            bbArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            listChildProducts.setAdapter(bbArrayAdapter);

            listChildProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position != Spinner.INVALID_POSITION) {
                        if (position != mHeaderSelectedIdx) {
                            new OnSectionItemClickListener<>(getCurrentActivity(), headSection,
                                    headSection.getSectionItems().get(position),
                                    TrackingAware.PRODUCT_LIST_HEADER).onClick(view);
                        }
                        onChildDropdownRequested.onClick(view);  // Manually simulate click to hide dropdown
                    }
                }
            });
            findViewById(R.id.layoutListHeader).setOnClickListener(onChildDropdownRequested);

        } else {
            layoutChildToolbarContainer.setVisibility(View.GONE);
            listChildProducts.setAdapter(null);
            toolbar.setTitle(mTitlePassedViaIntent);
            if (mToolbarTextDropdown != null) {
                mToolbarTextDropdown.setOnClickListener(null);
                toolbar.removeView(mToolbarTextDropdown);
            }
        }
    }

    private class OnChildDropdownRequested implements View.OnClickListener {

        private ViewGroup layoutChildToolbarContainer;

        public OnChildDropdownRequested(ViewGroup layoutChildToolbarContainer) {
            this.layoutChildToolbarContainer = layoutChildToolbarContainer;
        }

        @Override
        public void onClick(View v) {
            if (layoutChildToolbarContainer.getVisibility() == View.VISIBLE) {
                layoutChildToolbarContainer.setVisibility(View.GONE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
                UIUtil.changeStatusBarColor(getCurrentActivity(),
                        R.color.uiv3_status_bar_background);
            } else {
                layoutChildToolbarContainer.setVisibility(View.VISIBLE);
                UIUtil.changeStatusBarColor(getCurrentActivity(),
                        R.color.uiv3_grey_status_bar_color);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
            trackEvent(TrackingAware.PRODUCT_LIST_HEADER_CLICKED, null);
        }
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
        loadProductTabs();
    }

    @Override
    public void doSearch(String searchQuery) {
        if (!TextUtils.isEmpty(searchQuery)) {
            mTitlePassedViaIntent = searchQuery;
            mNameValuePairs = new ArrayList<>();
            mNameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
            mNameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery));
            loadProductTabs();
        }
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
                            applySort(mSortOptions.get(selectedPosition).getSortSlug());
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
        Intent sortFilterIntent = new Intent(this, FilterActivity.class);
        sortFilterIntent.putExtra(Constants.FILTER_OPTIONS, mFilterOptionCategories);
        sortFilterIntent.putExtra(TrackEventkeys.NAVIGATION_CTX,
                getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX) != null ?
                        getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX) : "pc");
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

    private void applySort(String sortedOn) {
        if (sortedOn == null || sortedOn.equals(mSortedOn)) return;
        NameValuePair sortedOnNameValuePair = getSortedOnNameValuePair();
        if (sortedOnNameValuePair == null) {
            sortedOnNameValuePair = new NameValuePair(Constants.SORT_ON, sortedOn);
            mNameValuePairs.add(sortedOnNameValuePair);
        } else {
            sortedOnNameValuePair.setValue(sortedOn);
        }
        loadProductTabs();
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
            Map<String, String> eventAttribs = new HashMap<>();
            for (FilteredOn filteredOn : filteredOnArrayList) {
                eventAttribs.put(TrackEventkeys.FILTER_NAME, filteredOn.getFilterSlug());
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.PRODUCT_LISTING_SCREEN);
                trackEvent(TrackingAware.FILTER_APPLIED, eventAttribs,
                        TrackEventkeys.PRODUCT_LISTING_SCREEN, null, false);
            }
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket, Product product,
                                                    String qty, @Nullable View productView, @Nullable HashMap<String, Integer> cartInfoMap) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty, viewIncQty, btnAddToBasket, product, qty, productView, cartInfoMap);
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