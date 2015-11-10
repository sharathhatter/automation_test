package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
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
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductInfo;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.BBUrlEncodeUtils;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
                    bigBasketApiService.productNextPage(BBUrlEncodeUtils.urlEncode(mNameValuePairs));
            call.enqueue(new BBNetworkCallback<ApiResponse<ProductNextPageResponse>>(this) {
                @Override
                public void onSuccess(ApiResponse<ProductNextPageResponse> productNextPageApiResponse) {
                    setNextPageLoading(false);
                    if (productNextPageApiResponse.status == 0) {
                        mProductInfo.setCurrentPage(nextPage);
                        HashMap<String, ArrayList<Product>> productMap = productNextPageApiResponse.apiResponseContent.productListMap;
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
                    mProductListRecyclerAdapter.setLoadingFailed(true);
                    mProductListRecyclerAdapter.notifyDataSetChanged();
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
        List<Product> existingProductList = mProductListRecyclerAdapter.getProducts();
        int insertedAt = existingProductList.size();
        existingProductList.addAll(products);
        mProductListRecyclerAdapter.notifyItemRangeInserted(insertedAt, products.size());
    }

    public void insertProductList(@Nullable ArrayList<Product> products) {
        if (mProductInfo == null) return;
        hideProgressView();
        mProductInfo.setCurrentPage(1);
        mProductInfo.setProducts(products);
        setProductListView();
    }

    public void updateProductInfo(@NonNull ProductInfo productInfo, ArrayList<NameValuePair> nameValuePairs) {
        hideProgressView();
        mProductInfo = productInfo;
        mProductInfo.setCurrentPage(productInfo.getCurrentPage());
        mProductInfo.setProducts(productInfo.getProducts());
        mNameValuePairs = NameValuePair.toMap(nameValuePairs);
        setProductListView();
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
            RecyclerView productRecyclerView = (RecyclerView) getActivity().
                    getLayoutInflater().inflate(R.layout.uiv3_recyclerview, contentView, false);
            UIUtil.configureRecyclerView(productRecyclerView, getActivity(), 1, 1);

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
            mProductListRecyclerAdapter = new ProductListRecyclerAdapter(products, mBaseImgUrl,
                    productViewDisplayDataHolder, this, mProductInfo.getProductCount(),
                    getNextScreenNavigationContext(), cartInfo,
                    mHasSingleTab ? TrackEventkeys.SINGLE_TAB_NAME : mTabType);

            productRecyclerView.setAdapter(mProductListRecyclerAdapter);
            contentView.addView(productRecyclerView);
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
    public String getNextScreenNavigationContext() {
        if (getCurrentActivity() != null) {
            return getCurrentActivity().getNextScreenNavigationContext();
        }
        return null;
    }

    @Nullable
    @Override
    public String getCurrentNavigationContext() {
        if (getCurrentActivity() != null) {
            return getCurrentActivity().getCurrentNavigationContext();
        }
        return null;
    }

    @Override
    public void setNextScreenNavigationContext(@Nullable String nc) {
        if (getCurrentActivity() != null) {
            getCurrentActivity().setNextScreenNavigationContext(nc);
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
}