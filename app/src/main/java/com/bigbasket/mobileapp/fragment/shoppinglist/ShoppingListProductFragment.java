package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListDetailsApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListDetail;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    private ShoppingListName mShoppingListName;
    private ShoppingListDetail mShoppingListDetail;
    private String mBaseImgUrl;
    private String topcatName;

    @Override
    public void loadProducts() {
        loadShoppingListProducts();
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }

    @Override
    public void updateData() {
        // Do nothing
    }

    @Override
    public String getSourceName() {
        if (mShoppingListName.isSystem()) {
            if (mShoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG))
                return TrackEventkeys.SMART_BASKET;
            else
                return TrackEventkeys.SYSTEM_SHOPPING_LIST;
        } else {
            return TrackEventkeys.SHOPPING_LIST;
        }
    }

    @Override
    public void restoreProductList(Bundle savedInstanceState) {
        mShoppingListName = getArguments().getParcelable(Constants.SHOPPING_LIST_NAME);
        topcatName = getArguments().getString(Constants.TOP_CATEGORY_NAME);
        if (savedInstanceState != null) {
            mShoppingListDetail = savedInstanceState.getParcelable(Constants.SHOPPING_LIST_ITEMS);
            if (mShoppingListDetail != null) {
                mBaseImgUrl = savedInstanceState.getString(Constants.BASE_IMG_URL);
                renderShoppingListItems();
                return;
            }
        }
        loadProducts();
    }

    @Nullable
    @Override
    public ArrayList<NameValuePair> getInputForApi() {
        return null;
    }

    private void loadShoppingListProducts() {
        Bundle bundle = getArguments();
        String topCatSlug = bundle.getString(Constants.TOP_CAT_SLUG);

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getShoppingListDetails(mShoppingListName.getSlug(), topCatSlug,
                new Callback<ApiResponse<GetShoppingListDetailsApiResponse>>() {
                    @Override
                    public void success(ApiResponse<GetShoppingListDetailsApiResponse> getShoppingListDetailsApiResponse, Response response) {
                        if (isSuspended()) return;
                        hideProgressView();
                        switch (getShoppingListDetailsApiResponse.status) {
                            case 0:
                                mBaseImgUrl = getShoppingListDetailsApiResponse.apiResponseContent.baseImgUrl;
                                mShoppingListDetail = getShoppingListDetailsApiResponse.apiResponseContent.shoppingListDetail;
                                renderShoppingListItems();
                                break;
                            default:
                                handler.sendEmptyMessage(getShoppingListDetailsApiResponse.status);
                                break;
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

    private void renderShoppingListItems() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        if (mShoppingListDetail == null || mShoppingListDetail.getProducts() == null
                || mShoppingListDetail.getProducts().size() == 0) {
            Toast.makeText(getActivity(), "This shopping list has no products", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.SHOPPING_LIST_NAME, mShoppingListName.getName());
        map.put(TrackEventkeys.PRODUCT_TOP_CAT, topcatName);
        if (mShoppingListName.isSystem()) {
            trackEvent(TrackingAware.SHOP_LST_SYSTEM_LIST_CATEGORY_DETAIL, map);
        } else {
            trackEvent(TrackingAware.SHOP_LST_CATEGORY_DETAIL, null);
        }
        contentView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View shopListHeaderLayout = inflater.inflate(R.layout.uiv3_shopping_list_products_header, contentView, false);
        TextView brandNameTxt = (TextView) shopListHeaderLayout.findViewById(R.id.brandNameTxt);
        String topname = mShoppingListDetail.getTopCategoryName();

        topname = topname.replaceAll("<br/>", " ");
        brandNameTxt.setText(UIUtil.abbreviate(topname, 25));
        brandNameTxt.setTypeface(faceRobotoRegular);

        ArrayList<Product> productList = mShoppingListDetail.getProducts();
        Button btnAddAllToBasket = (Button) shopListHeaderLayout.findViewById(R.id.btnAddAllToBasket);
        if (Product.areAllProductsOutOfStock(productList)) {
            btnAddAllToBasket.setVisibility(View.GONE);
        } else {
            btnAddAllToBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog(null, getString(R.string.addAllProducts),
                            DialogButton.YES, DialogButton.CANCEL, Constants.ADD_ALL, null, getString(R.string.yesTxt));
                }
            });
        }
        contentView.addView(shopListHeaderLayout);
        renderProducts(productList);
    }


    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equalsIgnoreCase(Constants.ADD_ALL)) {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            addAllItemsToBasket();
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    private void addAllItemsToBasket() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        if (mShoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
            bigBasketApiService.addAllToBasketSmartBasket(mShoppingListName.getSlug(),
                    mShoppingListDetail.getTopCategorySlug(),
                    new Callback<OldApiResponse<CartSummary>>() {
                        @Override
                        public void success(OldApiResponse<CartSummary> addAllToBasketSmartBasketCallBack, Response response) {
                            if (isSuspended()) return;
                            hideProgressView();
                            switch (addAllToBasketSmartBasketCallBack.status) {
                                case Constants.OK:
                                    setCartInfo(addAllToBasketSmartBasketCallBack.apiResponseContent);
                                    updateUIForCartInfo();
                                    loadShoppingListProducts();
                                    break;
                                case Constants.ERROR:
                                    handler.sendEmptyMessage(addAllToBasketSmartBasketCallBack.getErrorTypeAsInt(),
                                            addAllToBasketSmartBasketCallBack.message);
                                    break;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isSuspended()) return;
                            hideProgressView();
                            handler.handleRetrofitError(error);
                        }
                    });
        } else {
            bigBasketApiService.addAllToBasketShoppingList(mShoppingListName.getSlug(),
                    mShoppingListDetail.getTopCategorySlug(),
                    new Callback<OldApiResponse<CartSummary>>() {
                        @Override
                        public void success(OldApiResponse<CartSummary> addAllToBasketShoppingListCallBack, Response response) {
                            if (isSuspended()) return;
                            hideProgressView();
                            switch (addAllToBasketShoppingListCallBack.status) {
                                case Constants.OK:
                                    setCartInfo(addAllToBasketShoppingListCallBack.apiResponseContent);
                                    updateUIForCartInfo();
                                    loadShoppingListProducts();
                                    break;
                                case Constants.ERROR:
                                    handler.sendEmptyMessage(addAllToBasketShoppingListCallBack.getErrorTypeAsInt(),
                                            addAllToBasketShoppingListCallBack.message);
                                    break;
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
    }

    private void renderProducts(ArrayList<Product> productList) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setHandler(new BigBasketMessageHandler<>(getCurrentActivity()))
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(false)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(!mShoppingListName.isSystem())
                .setShoppingListName(mShoppingListName)
                .setRupeeTypeface(faceRupee)
                .build();

        ProductListRecyclerAdapter productListAdapter = new ProductListRecyclerAdapter(productList, mBaseImgUrl,
                productViewDisplayDataHolder, this, productList.size(), getSourceName());

        productRecyclerView.setAdapter(productListAdapter);
        contentView.addView(productRecyclerView);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void parcelProductList(Bundle outState) {
        if (mShoppingListDetail != null) {
            outState.putParcelable(Constants.SHOPPING_LIST_ITEMS, mShoppingListDetail);
            outState.putString(Constants.BASE_IMG_URL, mBaseImgUrl);
        }
    }

    @Override
    public void postShoppingListItemDeleteOperation() {
        loadShoppingListProducts();
    }

    @Override
    public String getTitle() {
        return "Shopping List Products";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListProductFragment.class.getName();
    }
}