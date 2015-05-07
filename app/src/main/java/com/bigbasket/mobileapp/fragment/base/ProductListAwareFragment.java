package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductNextPageResponse;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.interfaces.LazyProductListAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public abstract class ProductListAwareFragment extends BaseSectionFragment implements
        ShoppingListNamesAware, InfiniteProductListAware {

    private String selectedProductId;
    private ProductListRecyclerAdapter mProductListRecyclerAdapter;
    private HashMap<String, String> mNameValuePairs;
    private boolean mIsNextPageLoading;
    private ProductInfo mProductInfo;
    private String mBaseImgUrl;
    private String mTabType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadProducts();
        logProductListingEvent();
    }

    private void logProductListingEvent() {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getNavigationCtx());
        trackEvent(TrackingAware.PRODUCT_LIST_SHOWN, map);
    }

    public void loadProducts() {
        if (getArguments() != null) {
            mProductInfo = getArguments().getParcelable(Constants.PRODUCT_INFO);
            mBaseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
            ArrayList<NameValuePair> nameValuePairs = getArguments().getParcelableArrayList(Constants.PRODUCT_QUERY);
            mNameValuePairs = NameValuePair.toMap(nameValuePairs);
            mTabType = getArguments().getString(Constants.TAB_TYPE);
            setProductListView();
        }
    }

    public void loadMoreProducts() {
        if (isNextPageLoading() || getCurrentActivity() == null) return;
        final int nextPage = Math.max(mProductInfo.getCurrentPage(), 1) + 1;

        if (nextPage <= mProductInfo.getTotalPages()) {
            setNextPageLoading(true);
            mNameValuePairs.put(Constants.CURRENT_PAGE, String.valueOf(nextPage));
            mNameValuePairs.put(Constants.TAB_TYPE, new Gson().toJson(new String[]{mTabType}));

            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
            bigBasketApiService.productNextPage(mNameValuePairs, new Callback<ApiResponse<ProductNextPageResponse>>() {
                @Override
                public void success(ApiResponse<ProductNextPageResponse> productNextPageApiResponse, Response response) {
                    setNextPageLoading(false);
                    if (isSuspended()) return;
                    if (productNextPageApiResponse.status == 0) {
                        mProductInfo.setCurrentPage(nextPage);
                        HashMap<String, ArrayList<Product>> productMap = productNextPageApiResponse.apiResponseContent.productListMap;
                        if (productMap != null && productMap.size() > 0) {
                            updateProductList(productMap.get(mTabType));
                        } else {
                            // Add some code
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    setNextPageLoading(false);
                }
            });
        }
    }

    private void updateProductList(ArrayList<Product> products) {
        if (products == null || products.size() == 0) return;
        List<Product> existingProductList = mProductListRecyclerAdapter.getProducts();
        int insertedAt = existingProductList.size();
        existingProductList.addAll(products);
        mProductListRecyclerAdapter.notifyItemRangeInserted(insertedAt, products.size());
    }

//    @Override
//    public void onBackResume() {
//        super.onBackResume();
//
//        if (getActivity() != null && getActivity() instanceof ProductListActivity) {
//            displayProductCount();
//        }
//    }

    public void insertProductList(ArrayList<Product> products) {
        if (mProductInfo == null) return;
        hideProgressView();
        mProductInfo.setProducts(products);
        setProductListView();
    }

    private void setProductListView() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        ArrayList<Product> products = mProductInfo != null ? mProductInfo.getProducts() : null;
        if (products == null || products.size() == 0) {
            products = ((LazyProductListAware) getActivity()).provideProductsIfAvailable(mTabType);
        }
        if (products != null && products.size() > 0) {
            //View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_fab_recycler_view, contentView, false);
            RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

//            FloatingActionButton fabFilterSort = (FloatingActionButton) base.findViewById(R.id.btnFab);
//            fabFilterSort.setImageResource(R.drawable.filter_white);
//
//            if ((productInfo.getFilterOptions() != null && productInfo.getFilterOptions().size() > 0)
//                    || (productInfo.getSortOptions().size() > 0)) {
//                fabFilterSort.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onSortFilterScreenRequested();
//                    }
//                });
//            } else {
//                fabFilterSort.setVisibility(View.GONE);
//            }

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
            mProductListRecyclerAdapter = new ProductListRecyclerAdapter(products, mBaseImgUrl,
                    productViewDisplayDataHolder, this, mProductInfo.getProductCount(),
                    getNavigationCtx());

            productRecyclerView.setAdapter(mProductListRecyclerAdapter);
            contentView.addView(productRecyclerView);
        } else {
            if (mProductInfo != null && mProductInfo.getCurrentPage() == -1) {
                showProgressView();
            }
//            productRecyclerView.setVisibility(View.GONE);
//            View emptyPageLayout = base.findViewById(R.id.noDeliveryAddLayout);
//            showNoProductsFoundView(emptyPageLayout);
        }

//        if (sectionView != null) {
//            contentView.addView(sectionView);
//        }
//        contentView.addView(base);
//
//
//        if (sectionView == null && productInfo == null) {
//            LayoutInflater inflater = getActivity().getLayoutInflater();
//            View emptyPageView = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
//            showNoProductsFoundView(emptyPageView);
//            contentView.addView(emptyPageView);
//        }
    }

//    private void addSectionToScrollView(ViewGroup contentView, View sectionView) {
//        ScrollView scrollView = new ScrollView(getActivity());
//        scrollView.addView(sectionView);
//        contentView.addView(scrollView);
//    }
//
//    private void displayProductCount() {
//        if (getCurrentActivity() == null) return;
//        if (productInfo != null && productInfo.getProductCount() > 0) {
//            String productsStr = productInfo.getProductCount() > 1 ? " Products" : " Product";
//            if (getCurrentActivity() instanceof ProductListActivity) {
//                SpannableString productCountSpannable =
//                        new SpannableString(productInfo.getProductCount() + productsStr);
//                productCountSpannable.setSpan(new CustomTypefaceSpan("", faceRobotoLight), 0, productCountSpannable.length(),
//                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                productCountSpannable.setSpan(new RelativeSizeSpan(0.8f),
//                        0, productCountSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                if (getCurrentActivity().getSupportActionBar() != null) {
//                    getCurrentActivity().getSupportActionBar().setSubtitle(productCountSpannable);
//                }
//            }
//        } else {
//            if (getCurrentActivity() instanceof ProductListActivity
//                    && getCurrentActivity().getSupportActionBar() != null) {
//                getCurrentActivity().getSupportActionBar().setSubtitle(null);
//            }
//        }
//    }

//    private void showNoProductsFoundView(View emptyPageView) {
//        ImageView imgEmptyPage = (ImageView) emptyPageView.findViewById(R.id.imgEmptyPage);
//        imgEmptyPage.setVisibility(View.INVISIBLE);
//        TextView txtEmptyMsg1 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg1);
//        txtEmptyMsg1.setText(getEmptyPageText());
//        TextView txtEmptyMsg2 = (TextView) emptyPageView.findViewById(R.id.txtEmptyMsg2);
//        txtEmptyMsg2.setVisibility(View.GONE);
//        Button btnBlankPage = (Button) emptyPageView.findViewById(R.id.btnBlankPage);
//        btnBlankPage.setText(R.string.continue_shopping_txt);
//        btnBlankPage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getCurrentActivity() == null) return;
//                getCurrentActivity().goToHome(false);
//            }
//        });
//    }

//    protected String getEmptyPageText() {
//        return getString(R.string.noProducts);
//    }
//
//    @Override
//    public void updateProductList(List<Product> nextPageProducts) {
//        if (productInfo == null || mProductListRecyclerAdapter == null) return;
//        setNextPageLoading(false);
//        List<Product> currentProducts = productInfo.getProducts();
//        int insertedAt = productInfo.getProducts().size();
//        currentProducts.addAll(nextPageProducts);
//        mProductListRecyclerAdapter.notifyItemRangeInserted(insertedAt, nextPageProducts.size());
//    }

    public abstract String getNavigationCtx();

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
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        parcelProductList(outState);
//        super.onSaveInstanceState(outState);
//    }
//
//    /**
//     * This method is called, just before the activity is destroyed, to parcel ProductListData,
//     * which is reused, when screen rotation takes place
//     *
//     * @param outState Bundle that stores the product list
//     */
//    public void parcelProductList(Bundle outState) {
//        if (productInfo != null) {
//            outState.putParcelable(Constants.PRODUCTS, productInfo);
//            retainSectionState(outState);
//        }
//    }
//

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

//    private void onSortFilterScreenRequested() {
//        Intent sortFilterIntent = new Intent(getActivity(), SortFilterActivity.class);
//        sortFilterIntent.putExtra(Constants.FILTER_OPTIONS, productInfo.getFilterOptions());
//        sortFilterIntent.putExtra(Constants.FILTERED_ON, productInfo.getFilteredOn());
//        sortFilterIntent.putExtra(Constants.PRODUCT_SORT_OPTION, productInfo.getSortOptions());
//        sortFilterIntent.putExtra(Constants.SORT_ON, productInfo.getSortedOn());
//        startActivityForResult(sortFilterIntent, NavigationCodes.FILTER_APPLIED);
//    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    public void onResume() {
        super.onResume();
        if (getCurrentActivity() != null && getCurrentActivity().isBasketDirty()) {
            syncBasket();
        }
//
//        if (getActivity() != null && getActivity() instanceof ProductListActivity) {
//            displayProductCount();
//        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        setSuspended(false);
//        if (resultCode == NavigationCodes.FILTER_APPLIED) {
//            ArrayList<FilteredOn> filteredOns = null;
//            String sortedOn = null;
//            if (data != null) {
//                sortedOn = data.getStringExtra(Constants.SORT_ON);
//                filteredOns = data.getParcelableArrayListExtra(Constants.FILTERED_ON);
//            }
//            applySortAndFilter(sortedOn, filteredOns);
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

//    private void applySortAndFilter(String sortedOn, ArrayList<FilteredOn> filteredOns) {
//        if (productInfo != null) {
//            productInfo.setSortedOn(sortedOn);
//            productInfo.setFilteredOn(filteredOns);
//            trackFilterAppliedEvent(filteredOns);
//            loadProducts();
//        }
//    }
//
//    @Override
//    public void syncBasket() {
//        restoreProductList(null);
//    }
}