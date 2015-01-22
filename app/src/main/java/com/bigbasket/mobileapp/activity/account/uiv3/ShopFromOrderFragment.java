package com.bigbasket.mobileapp.activity.account.uiv3;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetProductsForOrderApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShopFromOrderFragment extends ProductListAwareFragment {

    private ArrayList<Product> mProducts;
    private String mOrderId;

    @Override
    public String getTitle() {
        return TextUtils.isEmpty(mOrderId) ? getString(R.string.shopFromOrder) : mOrderId;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopFromOrderFragment.class.getName();
    }

    @Override
    public void restoreProductList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mProducts = savedInstanceState.getParcelableArrayList(Constants.PRODUCTS);
            if (mProducts != null) {
                loadProducts();
                return;
            }
        }
        mOrderId = getArguments().getString(Constants.ORDER_ID);
        if (TextUtils.isEmpty(mOrderId)) {
            return;
        }
        setTitle(mOrderId);
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getProductsForOrder(mOrderId, new Callback<ApiResponse<GetProductsForOrderApiResponseContent>>() {
            @Override
            public void success(ApiResponse<GetProductsForOrderApiResponseContent> getProductsForOrderApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (getProductsForOrderApiResponse.status) {
                    case 0:
                        mProducts = getProductsForOrderApiResponse.apiResponseContent.products;
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TrackEventkeys.ORDER_ID, mOrderId);
                        trackEvent(TrackingAware.SHOP_FROM_PAST_ORDER_SHOWN, map);
                        loadProducts();
                        break;
                    default:
                        handler.sendEmptyMessage(getProductsForOrderApiResponse.status, getProductsForOrderApiResponse.message,
                                true);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error);
            }
        });
    }

    @Override
    public void loadProducts() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 2, 2, contentView);

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

        ProductListRecyclerAdapter productListRecyclerAdapter = new ProductListRecyclerAdapter(mProducts, null,
                productViewDisplayDataHolder, this, mProducts.size(),
                TrackEventkeys.PAST_ORDER);

        productRecyclerView.setAdapter(productListRecyclerAdapter);
        contentView.addView(productRecyclerView);
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }

    @Override
    public String getSourceName() {
        return TrackEventkeys.SHOP_FROM_PAST_ORDER;
    }

    @Override
    @Nullable
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mProducts != null) {
            outState.putParcelableArrayList(Constants.PRODUCTS, mProducts);
        }
        super.onSaveInstanceState(outState);
    }
}
