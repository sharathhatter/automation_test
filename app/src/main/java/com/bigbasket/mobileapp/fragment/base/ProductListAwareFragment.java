package com.bigbasket.mobileapp.fragment.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.product.SortFilterActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.SortAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductListData;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.ProductListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;
import com.google.gson.Gson;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ProductListAwareFragment extends BaseSectionFragment implements ProductListDataAware,
        ShoppingListNamesAware, SortAware, InfiniteProductListAware {

    protected ProductListData productListData;
    private String selectedProductId;
    private ProductListRecyclerAdapter mProductListRecyclerAdapter;
    private View mFooterView;
    private boolean mIsNextPageLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreProductList(savedInstanceState);
        logProductListingEvent();
    }

    private void logProductListingEvent() {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getNavigationCtx());
        trackEvent(TrackingAware.PRODUCT_LIST_SHOWN, map);
    }

    /**
     * This method is called by onActivityCreated and can be used to load products on fresh start or restore state,
     * when screen is rotated
     *
     * @param savedInstanceState Bundle containing parceled data, when the screen was rotated
     */
    public void restoreProductList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            productListData = savedInstanceState.getParcelable(Constants.PRODUCTS);
            tryRestoreSectionState(savedInstanceState);
            updateData();
        } else {
            loadProducts();
        }
    }

    public void loadProducts() {
        getProductListAsyncTask().startTask();
    }

    public void loadMoreProducts() {
        if (isNextPageLoading()) return;
        int nextPage = productListData.getCurrentPage() + 1;
        if (nextPage <= productListData.getTotalPages()) {
            setNextPageLoading(true);
            if (mFooterView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                mFooterView = inflater.inflate(R.layout.uiv3_list_loading_footer, null);
            }
            getProductListAsyncTask(nextPage).startTask();
        }
    }

    public ProductListTask<ProductListAwareFragment> getProductListAsyncTask() {
        ArrayList<NameValuePair> nameValuePairs = getProductQueryParams();
        HashMap<String, String> paramMap = NameValuePair.toMap(nameValuePairs);
        return new ProductListTask<>(this, paramMap);
    }

    public ProductListTask<ProductListAwareFragment> getProductListAsyncTask(int page) {
        ArrayList<NameValuePair> nameValuePairs = getProductQueryParams();
        HashMap<String, String> paramMap = NameValuePair.toMap(nameValuePairs);
        return new ProductListTask<>(page, this, paramMap);
    }

    public ProductListData getProductListData() {
        return productListData;
    }

    public void setProductListData(ProductListData productListData) {
        this.productListData = productListData;
    }

    @Override
    public void updateData() {
        setProductListView();
    }

    @Override
    public void onBackResume() {
        super.onBackResume();

        if (getActivity() != null && getActivity() instanceof ProductListActivity) {
            displayProductCount();
        }
    }

    private void setProductListView() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        final View sectionView = getSectionView();
        displayProductCount();

        if (productListData != null && productListData.getProductCount() > 0) {

            View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_product_layout, contentView, false);
            RecyclerView productRecyclerView = (RecyclerView) base.findViewById(R.id.fabRecyclerView);
            UIUtil.configureRecyclerView(productRecyclerView, getActivity(), 1, 1);

            FloatingActionButton fabFilterSort = (FloatingActionButton) base.findViewById(R.id.btnFab);
            fabFilterSort.setImageResource(R.drawable.filter_white);

            if ((productListData.getFilterOptions() != null && productListData.getFilterOptions().size() > 0)
                    || (productListData.getSortOptions().size() > 0)) {
                fabFilterSort.attachToRecyclerView(productRecyclerView);
                fabFilterSort.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSortFilterScreenRequested();
                    }
                });
            } else {
                fabFilterSort.setVisibility(View.GONE);
            }

            // Set product-list data
            AuthParameters authParameters = AuthParameters.getInstance(getActivity());
            ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                    .setCommonTypeface(faceRobotoRegular)
                    .setRupeeTypeface(faceRupee)
                    .setHandler(handler)
                    .setLoggedInMember(!authParameters.isAuthTokenEmpty())
                    .setShowShoppingListBtn(true)
                    .setShowBasketBtn(true)
                    .setShowShopListDeleteBtn(false)
                    .build();
            mProductListRecyclerAdapter = new ProductListRecyclerAdapter(productListData.getProducts(), productListData.getBaseImgUrl(),
                    productViewDisplayDataHolder, this, productListData.getProductCount(),
                    getNavigationCtx());

            productRecyclerView.setAdapter(mProductListRecyclerAdapter);

            if (sectionView != null) {
                contentView.addView(sectionView);
            }
            contentView.addView(base);

        } else if (sectionView != null) {
            addSectionToScrollView(contentView, sectionView);
        }

        if (sectionView == null && (productListData == null || productListData.getProductCount() == 0)) {
            showNoProductsFoundView(contentView);
        }
    }

    private void addSectionToScrollView(ViewGroup contentView, View sectionView) {
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(sectionView);
        contentView.addView(scrollView);
    }

    private void displayProductCount() {
        if (productListData != null && productListData.getProductCount() > 0) {
            String productsStr = productListData.getProductCount() > 1 ? " Products" : " Product";
            if (getCurrentActivity() instanceof ProductListActivity) {
                SpannableString productCountSpannable =
                        new SpannableString(productListData.getProductCount() + productsStr);
                productCountSpannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), 0, productCountSpannable.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                productCountSpannable.setSpan(new RelativeSizeSpan(0.8f),
                        0, productCountSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                getCurrentActivity().getSupportActionBar().setSubtitle(productCountSpannable);
            }
        } else {
            if (getCurrentActivity() instanceof ProductListActivity) {
                getCurrentActivity().getSupportActionBar().setSubtitle(null);
            }
        }
    }

    protected void showNoProductsFoundView(LinearLayout contentView) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        contentView.removeAllViews();
        View emptyPageView = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setVisibility(View.INVISIBLE);
        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noProducts);
        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);
        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setText(R.string.continue_shopping_txt);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentActivity() == null) return;
                getCurrentActivity().goToHome(false);
            }
        });
        contentView.addView(emptyPageView);
    }


    @Override
    public void updateProductList(List<Product> nextPageProducts) {
        if (productListData == null || mProductListRecyclerAdapter == null) return;
        setNextPageLoading(false);
        List<Product> currentProducts = productListData.getProducts();
        int insertedAt = productListData.getProducts().size();
        currentProducts.addAll(nextPageProducts);
        mProductListRecyclerAdapter.notifyItemRangeInserted(insertedAt, nextPageProducts.size());
    }

    public abstract String getNavigationCtx();

    @Nullable
    public abstract ArrayList<NameValuePair> getInputForApi();

    protected ArrayList<FilteredOn> getProductRefinedByFilter() {
        return null;
    }

    protected String getProductRefinedBySortedOn() {
        return null;
    }

    @NonNull
    @Override
    public ArrayList<NameValuePair> getProductQueryParams() {
        ArrayList<FilteredOn> filteredOnArrayList = null;
        String sortedOn = null;
        ProductListData productListData = getProductListData();
        if (productListData != null) {
            filteredOnArrayList = productListData.getFilteredOn();
            sortedOn = productListData.getSortedOn();
        }
        if (TextUtils.isEmpty(sortedOn)) {
            sortedOn = getProductRefinedBySortedOn();
        }
        if (filteredOnArrayList == null || filteredOnArrayList.size() == 0) {
            filteredOnArrayList = getProductRefinedByFilter();
        }

        ArrayList<NameValuePair> nameValuePairs = getInputForApi();
        if (nameValuePairs == null) {
            nameValuePairs = new ArrayList<>();
        }
        if (!TextUtils.isEmpty(sortedOn)) {
            nameValuePairs.add(new NameValuePair(Constants.SORT_ON, sortedOn));
        }
        if (filteredOnArrayList != null && !filteredOnArrayList.isEmpty()) {
            Gson gson = new Gson();
            String filteredOn = gson.toJson(filteredOnArrayList);
            nameValuePairs.add(new NameValuePair(Constants.FILTER_ON, filteredOn));
        }
        return nameValuePairs;
    }

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        if (shoppingListNames == null || shoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.createAShoppingList), Toast.LENGTH_SHORT).show();
        } else {
            ShoppingListNamesDialog shoppingListNamesDialog = ShoppingListNamesDialog.newInstance(shoppingListNames);
            shoppingListNamesDialog.setTargetFragment(getFragment(), 0);
            shoppingListNamesDialog.show(getFragment().getFragmentManager(), Constants.SHOP_LST);
        }
    }

    @Override
    public String getSelectedProductId() {
        return selectedProductId;
    }

    @Override
    public void setSelectedProductId(String selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    @Override
    public void postShoppingListItemDeleteOperation() {

    }

    private ProductListAwareFragment getFragment() {
        return this;
    }

    @Nullable
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public List<Option> getSortOptions() {
        return productListData.getSortOptions();
    }

    @Override
    public String getSortedOn() {
        return productListData.getSortedOn();
    }

    @Override
    public void setSortedOn(String sortedOn) {
        this.productListData.setSortedOn(sortedOn);
        logSortByEvent(sortedOn);
    }

    private void logSortByEvent(String sortedOn) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.TYPE, sortedOn);
        map.put(TrackEventkeys.NAVIGATION_CTX, getNavigationCtx());
        trackEvent(TrackingAware.SORT_BY, map);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        parcelProductList(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * This method is called, just before the activity is destroyed, to parcel ProductListData,
     * which is reused, when screen rotation takes place
     *
     * @param outState Bundle that stores the product list
     */
    public void parcelProductList(Bundle outState) {
        if (productListData != null) {
            outState.putParcelable(Constants.PRODUCTS, productListData);
            retainSectionState(outState);
        }
    }

    @Override
    public boolean isNextPageLoading() {
        return mIsNextPageLoading;
    }

    @Override
    public void setNextPageLoading(boolean isNextPageLoading) {
        this.mIsNextPageLoading = isNextPageLoading;
    }

    @Override
    public void addToShoppingList(List<ShoppingListName> selectedShoppingListNames) {
        if (getActivity() == null) return;
        if (selectedShoppingListNames == null || selectedShoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.chooseShopList), Toast.LENGTH_SHORT).show();
            return;
        }
        ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                new ShoppingListDoAddDeleteTask<>(this, selectedShoppingListNames, ShoppingListOption.ADD_TO_LIST);
        shoppingListDoAddDeleteTask.startTask();
    }

    private void trackFilterAppliedEvent(ArrayList<FilteredOn> filteredOnArrayList) {
        if (filteredOnArrayList == null || filteredOnArrayList.size() == 0) {
            trackEvent(TrackingAware.FILTER_CLEARED, null);
        } else {
            Map<String, String> eventAttribs = new HashMap<>();
            for (FilteredOn filteredOn : filteredOnArrayList) {
                eventAttribs.put(TrackEventkeys.FILTER_NAME, filteredOn.getFilterSlug());
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNavigationCtx());
                trackEvent(TrackingAware.FILTER_APPLIED, eventAttribs, getNavigationCtx(), null, false);
            }
        }
    }

    private void onSortFilterScreenRequested() {
        Intent sortFilterIntent = new Intent(getActivity(), SortFilterActivity.class);
        sortFilterIntent.putExtra(Constants.FILTER_OPTIONS, productListData.getFilterOptions());
        sortFilterIntent.putExtra(Constants.FILTERED_ON, productListData.getFilteredOn());
        sortFilterIntent.putExtra(Constants.PRODUCT_SORT_OPTION, productListData.getSortOptions());
        sortFilterIntent.putExtra(Constants.SORT_ON, productListData.getSortedOn());
        startActivityForResult(sortFilterIntent, NavigationCodes.FILTER_APPLIED);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void onResume() {
        super.onResume();
        if (getCurrentActivity() != null && getCurrentActivity().isBasketDirty()) {
            syncBasket();
        }

        if (getActivity() != null && getActivity() instanceof ProductListActivity) {
            displayProductCount();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.FILTER_APPLIED) {
            ArrayList<FilteredOn> filteredOns = null;
            String sortedOn = null;
            if (data != null) {
                sortedOn = data.getStringExtra(Constants.SORT_ON);
                filteredOns = data.getParcelableArrayListExtra(Constants.FILTERED_ON);
            }
            applySortAndFilter(sortedOn, filteredOns);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void applySortAndFilter(String sortedOn, ArrayList<FilteredOn> filteredOns) {
        if (productListData != null) {
            productListData.setSortedOn(sortedOn);
            productListData.setFilteredOn(filteredOns);
            trackFilterAppliedEvent(filteredOns);
            loadProducts();
        }
    }

    @Override
    public void syncBasket() {
        restoreProductList(null);
    }
}