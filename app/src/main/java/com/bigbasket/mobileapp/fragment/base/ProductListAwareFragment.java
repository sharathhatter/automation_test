package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.AbstractProductItem;
import com.bigbasket.mobileapp.adapter.product.NormalProductItem;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.adapter.product.SponsoredProductInfo;
import com.bigbasket.mobileapp.adapter.product.SponsoredProductItem;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ProductNextPageResponse;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.interfaces.LazyProductListAware;
import com.bigbasket.mobileapp.interfaces.ProductListDataAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.ads.SponsoredAds;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.LeakCanaryObserver;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit.Call;


public abstract class ProductListAwareFragment extends BaseSectionFragment implements
        ShoppingListNamesAware, InfiniteProductListAware {

    private String selectedProductId;
    private ProductListRecyclerAdapter mProductListRecyclerAdapter;
    private HashMap<String, String> mNameValuePairs;
    private boolean mIsNextPageLoading;
    private ProductInfo mProductInfo;
    private String mBaseImgUrl;
    private String mTabType;
    private boolean mHasProductLoadingFailed;
    private boolean mHasSingleTab;
    private SponsoredProductInfo mSponsoredSectionInfo;
    private RecyclerView mProductRecyclerView;
    private int mInjectWindowRetries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        productListOnActivityCreated();
    }

    public void productListOnActivityCreated() {
        loadProducts();
    }

    public void loadProducts() {
        if (getArguments() != null) {
            mProductInfo = getArguments().getParcelable(Constants.PRODUCT_INFO);
            mBaseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
            ArrayList<NameValuePair> nameValuePairs = getArguments().getParcelableArrayList(Constants.PRODUCT_QUERY);
            mNameValuePairs = NameValuePair.toMap(nameValuePairs);
            mTabType = getArguments().getString(Constants.TAB_TYPE);
            mHasSingleTab = getArguments().getBoolean(Constants.SINGLE_TAB, false);
            setProductListView();
        }
    }

    public void setSponsoredSectionData(SponsoredAds sponsoredSectionData) {
        if (sponsoredSectionData == null || sponsoredSectionData.getSections() == null
                || sponsoredSectionData.getSections().isEmpty()) {
            if (mSponsoredSectionInfo != null) {
                mSponsoredSectionInfo.reset();
            }
            return;
        }
        if (mSponsoredSectionInfo == null) {
            mSponsoredSectionInfo = new SponsoredProductInfo(sponsoredSectionData);
        } else {
            mSponsoredSectionInfo.reset(sponsoredSectionData);
        }
        mInjectWindowRetries = 0;

        injectSponsoredProducts();
    }

    private void injectSponsoredProducts() {
        if (getCurrentActivity() == null || mSponsoredSectionInfo == null
                || !mSponsoredSectionInfo.hasMoreItems()
                || mProductListRecyclerAdapter == null) {
            return;
        }
        if (mSponsoredSectionInfo.getLastInjectedPosition() < 0) {
            //Determine injection window
            if (mProductRecyclerView != null && mProductRecyclerView.getLayoutManager() != null) {
                RecyclerView.LayoutManager layoutManager = mProductRecyclerView.getLayoutManager();
                int lastVisiblePosition = RecyclerView.NO_POSITION;
                int firstVisiblePosition = RecyclerView.NO_POSITION;
                if (layoutManager instanceof LinearLayoutManager) {
                    firstVisiblePosition = ((LinearLayoutManager) layoutManager)
                            .findFirstVisibleItemPosition();
                    lastVisiblePosition = ((LinearLayoutManager) layoutManager)
                            .findLastVisibleItemPosition();
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    int[] pos = ((StaggeredGridLayoutManager) layoutManager)
                            .findFirstVisibleItemPositions(null);
                    if (pos != null && pos.length > 0) {
                        Arrays.sort(pos);
                        firstVisiblePosition = pos[0];
                    }
                    pos = ((StaggeredGridLayoutManager) layoutManager)
                            .findLastVisibleItemPositions(null);
                    if (pos != null && pos.length > 0) {
                        Arrays.sort(pos);
                        lastVisiblePosition = pos[pos.length - 1];
                    }
                } else {
                    //unknown layoutmanager, Use hard coded positions
                    firstVisiblePosition = 0;
                    try {
                        lastVisiblePosition = getResources()
                                .getInteger(R.integer.default_sponsored_items_window);
                    } catch (IllegalStateException ex) {
                        //Ignore
                    }
                }

                if (firstVisiblePosition != RecyclerView.NO_POSITION
                        && lastVisiblePosition != RecyclerView.NO_POSITION) {
                    mSponsoredSectionInfo.setStartPosition(lastVisiblePosition);
                } else {
                    if (mInjectWindowRetries >= 5) {
                        //Could not determine after 5 retries, use hard coded window values
                        firstVisiblePosition = 0;
                        try {
                            lastVisiblePosition = getResources()
                                    .getInteger(R.integer.default_sponsored_items_window);
                        } catch (IllegalStateException ex) {
                            //Ignore
                        }
                        mSponsoredSectionInfo.setStartPosition(lastVisiblePosition);
                    } else {
                        //UI is not ready, try again after 500ms
                        mProductRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                injectSponsoredProducts();
                            }
                        }, 500);
                        mInjectWindowRetries++;
                        return;
                    }
                }
            }
        }

        List<AbstractProductItem> existingProducts = mProductListRecyclerAdapter.getProducts();
        if (existingProducts != null) {
            int nextInjectPosition = mSponsoredSectionInfo.getNextInjectPosition();
            while (nextInjectPosition != RecyclerView.NO_POSITION
                    && nextInjectPosition <= existingProducts.size()
                    && mSponsoredSectionInfo.getNextSponsoredItem() != null) {
                SponsoredProductItem spItem = new SponsoredProductItem (
                        mSponsoredSectionInfo.getSponsoredAds().getSectionData(),
                        mSponsoredSectionInfo.getNextSponsoredItemIndex());

                if (nextInjectPosition > 0 && (nextInjectPosition) < existingProducts.size()) {
                    existingProducts.add(nextInjectPosition, spItem);
                } else {
                    existingProducts.add(spItem);
                    nextInjectPosition = existingProducts.size();
                }
                if (mProductListRecyclerAdapter != null) {
                    mProductListRecyclerAdapter.setSponsoredItemsSize(
                            mSponsoredSectionInfo.getTotalItems()
                                    - mSponsoredSectionInfo.getRemainingItems());
                }
                // +1 to include the product count label
                mProductListRecyclerAdapter.notifyItemInserted(nextInjectPosition + 1);
                mSponsoredSectionInfo.setLastInjectedPosition(nextInjectPosition);
                mSponsoredSectionInfo.setRemainingItems(
                        mSponsoredSectionInfo.getRemainingItems() - 1);
                nextInjectPosition = mSponsoredSectionInfo.getNextInjectPosition();
            }

            //TODO: Reached the end of productlist and still some sponsored items left unseen,
            //Decided to ignore such sponsored items for now
        }

    }


    @Override
    public void retryNextPage() {
        loadMoreProducts();
        mProductListRecyclerAdapter.setLoadingFailed(false);
        mProductListRecyclerAdapter.notifyDataSetChanged();
    }

    public void loadMoreProducts() {
        if (!checkInternetConnection()) {
            mProductListRecyclerAdapter.setLoadingFailed(true);
        }
        if (isNextPageLoading() || getCurrentActivity() == null) return;
        final int nextPage = Math.max(mProductInfo.getCurrentPage(), 1) + 1;

        if (nextPage <= mProductInfo.getTotalPages()) {
            setNextPageLoading(true);
            mNameValuePairs.put(Constants.CURRENT_PAGE, String.valueOf(nextPage));
            mNameValuePairs.put(Constants.TAB_TYPE, new Gson().toJson(new String[]{mTabType}));

            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
            Call<ApiResponse<ProductNextPageResponse>> call =
                    bigBasketApiService.productNextPage(getPreviousScreenName(), mNameValuePairs);
            call.enqueue(new BBNetworkCallback<ApiResponse<ProductNextPageResponse>>(this) {
                @Override
                public void onSuccess(ApiResponse<ProductNextPageResponse> productNextPageApiResponse) {
                    setNextPageLoading(false);
                    if (productNextPageApiResponse.status == 0) {
                        mProductInfo.setCurrentPage(nextPage);
                        HashMap<String, ArrayList<Product>> productMap =
                                productNextPageApiResponse.apiResponseContent.productListMap;
                        if (productMap != null && productMap.size() > 0) {
                            updateProductList(productMap.get(mTabType));
                        } else {
                            mProductListRecyclerAdapter.setLoadingFailed(true);
                            mProductListRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(int httpErrorCode, String msg) {
                    failure();
                }

                @Override
                public void onFailure(Throwable t) {
                    failure();
                }

                public void failure() {
                    setNextPageLoading(false);
                    if (mProductListRecyclerAdapter != null) {
                        mProductListRecyclerAdapter.setLoadingFailed(true);
                        mProductListRecyclerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public boolean updateProgress() {
                    return true;
                }
            });
        }
    }

    private void updateProductList(ArrayList<Product> products) {
        if (products == null || products.size() == 0) return;
        List<AbstractProductItem> existingProductList = mProductListRecyclerAdapter.getProducts();
        int insertedAt = existingProductList.size();
        for (Product p : products) {
            existingProductList.add(new NormalProductItem(p));
        }
        // +1 to include the product count label
        mProductListRecyclerAdapter.notifyItemRangeInserted(insertedAt + 1, products.size());
        if (mSponsoredSectionInfo != null && mSponsoredSectionInfo.hasMoreItems()) {
            injectSponsoredProducts();
        }
    }

    public void insertProductList(@Nullable ArrayList<Product> products) {
        if (mProductInfo == null) return;
        hideProgressView();
        mProductInfo.setCurrentPage(1);
        mProductInfo.setProducts(products);
        setProductListView();
        injectSponsoredProducts();
    }

    public void updateProductInfo(@NonNull ProductInfo productInfo, ArrayList<NameValuePair> nameValuePairs) {
        hideProgressView();
        mProductInfo = productInfo;
        mProductInfo.setCurrentPage(productInfo.getCurrentPage());
        mProductInfo.setProducts(productInfo.getProducts());
        mNameValuePairs = NameValuePair.toMap(nameValuePairs);
        setProductListView();
        injectSponsoredProducts();
    }

    public void setLazyProductLoadingFailure() {
        mHasProductLoadingFailed = true;
    }

    public void setProductListView() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        HashMap<String, Integer> cartInfo = getActivity() instanceof ProductListDataAware ?
                ((ProductListDataAware) getActivity()).getCartInfo() : null;
        ArrayList<Product> products = mProductInfo != null ? mProductInfo.getProducts() : null;
        if (products == null || products.size() == 0 && !mHasProductLoadingFailed) {
            Pair<ArrayList<Product>, Integer> pair = ((LazyProductListAware) getActivity()).provideProductsIfAvailable(mTabType);
            if (pair != null) {
                products = pair.first;
                mProductInfo.setCurrentPage(pair.second);
            }
        }
        if (products != null && products.size() > 0) {
            mProductRecyclerView = (RecyclerView) getActivity().
                    getLayoutInflater().inflate(R.layout.uiv3_recyclerview, contentView, false);
            UIUtil.configureRecyclerView(mProductRecyclerView, getActivity(), 1, 1);

            // Set product-list data
            AuthParameters authParameters = AuthParameters.getInstance(getActivity());
            ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                    .setCommonTypeface(faceRobotoRegular)
                    .setSansSerifMediumTypeface(faceRobotoMedium)
                    .setRupeeTypeface(faceRupee)
                    .setHandler(handler)
                    .setLoggedInMember(!authParameters.isAuthTokenEmpty())
                    .setShowShoppingListBtn(true)
                    .setShowBasketBtn(true)
                    .setShowShopListDeleteBtn(false)
                    .showQtyInput(authParameters.isKirana())
                    .build();
            List<AbstractProductItem> productItems = new ArrayList<>(products.size());
            for (Product p : products) {
                productItems.add(new NormalProductItem(p));
            }
            mProductListRecyclerAdapter = new ProductListRecyclerAdapter(productItems, mBaseImgUrl,
                    productViewDisplayDataHolder, this, mProductInfo.getProductCount(),
                    getCurrentScreenName(), cartInfo,
                    mHasSingleTab ? TrackEventkeys.SINGLE_TAB_NAME : mTabType);
            mProductRecyclerView.setAdapter(mProductListRecyclerAdapter);
            contentView.addView(mProductRecyclerView);
        } else {
            if (mHasProductLoadingFailed) {
                UIUtil.showEmptyProductsView(getCurrentActivity(), contentView, getString(R.string.productTabErrorMsg),
                        R.drawable.ic_error_red_36dp);

                ((ProductListDataAware) getActivity()).setTabNameWithEmptyProductView(mTabType);
            } else {
                if (mProductInfo != null) {
                    if (mProductInfo.getCurrentPage() == -1) {
                        showProgressView();
                    } else {
                        UIUtil.showEmptyProductsView(getCurrentActivity(), contentView, getString(R.string.noProducts),
                                R.drawable.empty_smart_basket);
                        ((ProductListDataAware) getActivity()).setTabNameWithEmptyProductView(mTabType);
                    }
                } else {
                    UIUtil.showEmptyProductsView(getCurrentActivity(), contentView, getString(R.string.noProducts),
                            R.drawable.empty_smart_basket);
                    ((ProductListDataAware) getActivity()).setTabNameWithEmptyProductView(mTabType);
                }
            }
        }
    }

    public void redrawProductList(HashMap<String, Integer> cartInfo) {
        if (mProductListRecyclerAdapter != null) {
            mProductListRecyclerAdapter.setCartInfo(cartInfo);
            mProductListRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public String getCurrentScreenName() {
        if (getCurrentActivity() != null) {
            return getCurrentActivity().getCurrentScreenName();
        }
        return null;
    }

    @Nullable
    @Override
    public String getPreviousScreenName() {
        if (getCurrentActivity() != null) {
            return getCurrentActivity().getPreviousScreenName();
        }
        return null;
    }

    @Override
    public void setCurrentScreenName(@Nullable String nc) {
        if (getCurrentActivity() != null) {
            getCurrentActivity().setCurrentScreenName(nc);
        }
    }

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        if (shoppingListNames == null) {
            shoppingListNames = new ArrayList<>();
        }
        ShoppingListNamesDialog shoppingListNamesDialog = ShoppingListNamesDialog.newInstance(shoppingListNames);
        shoppingListNamesDialog.setTargetFragment(getFragment(), 0);
        shoppingListNamesDialog.show(getFragment().getFragmentManager(), Constants.SHOP_LST);
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
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }


    @Override
    public void postAddToShoppingListOperation() {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }


    private ProductListAwareFragment getFragment() {
        return this;
    }

    @Nullable
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
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

    @Override
    public void createNewShoppingList() {
        new CreateShoppingListTask<>(this).showDialog();
    }

    @Override
    public void onNewShoppingListCreated(String listName) {
        if (getCurrentActivity() == null) return;
        Toast.makeText(getCurrentActivity(),
                "List \"" + listName
                        + "\" was created successfully", Toast.LENGTH_LONG).show();
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_LISTING_SCREEN;
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation,
                                                    @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                    @Nullable WeakReference<View> viewDecQtyRef,
                                                    @Nullable WeakReference<View> viewIncQtyRef,
                                                    @Nullable WeakReference<View> btnAddToBasketRef,
                                                    Product product, String qty,
                                                    @Nullable WeakReference<View> productViewRef,
                                                    @Nullable WeakReference<HashMap<String, Integer>> cartInfoMapRef,
                                                    @Nullable WeakReference<EditText> editTextQtyRef) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextViewRef, viewDecQtyRef, viewIncQtyRef,
                btnAddToBasketRef, product, qty, productViewRef, cartInfoMapRef, editTextQtyRef);
        if (cartInfoMapRef != null && cartInfoMapRef.get() != null) {
            if (getActivity() instanceof BasketOperationAware) {
                ((BasketOperationAware) getActivity()).setBasketOperationResponse(basketOperationResponse);
                ((BasketOperationAware) getActivity()).updateUIAfterBasketOperationSuccess(basketOperation,
                        null, null, null, null, product, qty, null, cartInfoMapRef, editTextQtyRef);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProductListRecyclerAdapter = null;  // Clear this
        // Now setup a leak-canary observer
        LeakCanaryObserver.Factory.observe(this);
    }

    @Override
    protected void observeMemoryLeak() {
        // Do nothing for this fragment, as this fragment registers ref-watcher for itself after cleaning-up
    }
}