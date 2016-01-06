package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.AbstractProductItem;
import com.bigbasket.mobileapp.adapter.product.NormalProductItem;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetProductsForOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponseWithCart;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Call;

public class ShopFromOrderFragment extends ProductListAwareFragment {

    private ArrayList<Product> mProducts;
    private String mOrderId, mOrderNumber;
    @Nullable
    private ProductListRecyclerAdapter productListRecyclerAdapter;

    @Override
    public String getTitle() {
        return TextUtils.isEmpty(mOrderNumber) ? getString(R.string.shopFromOrder) : mOrderNumber;
    }

    @Override
    public void productListOnActivityCreated() {
        // Don't do anything
    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_SHOP_FROM_ORDER);
        loadProducts();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShopFromOrderFragment.class.getName();
    }

    @Override
    public void loadProducts() {
        mOrderId = getArguments().getString(Constants.ORDER_ID);
        mOrderNumber = getArguments().getString(Constants.ORDER_NUMBER);
        if (TextUtils.isEmpty(mOrderId)) {
            return;
        }
        setTitle(mOrderNumber);
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetProductsForOrderApiResponseContent>> call =
                bigBasketApiService.getProductsForOrder(getPreviousScreenName(), mOrderId);
        call.enqueue(new BBNetworkCallback<ApiResponse<GetProductsForOrderApiResponseContent>>(this) {
            @Override
            public void onSuccess(ApiResponse<GetProductsForOrderApiResponseContent> getProductsForOrderApiResponse) {
                switch (getProductsForOrderApiResponse.status) {
                    case 0:
                        mProducts = getProductsForOrderApiResponse.apiResponseContent.products;
                        displayProducts();
                        break;
                    default:
                        handler.sendEmptyMessage(getProductsForOrderApiResponse.status, getProductsForOrderApiResponse.message,
                                true);
                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    public void displayProducts() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View shopFromOrderLayout = inflater.inflate(R.layout.uiv3_shop_from_order, contentView, false);

        View layoutAddAll = shopFromOrderLayout.findViewById(R.id.layoutAddAll);
        if (Product.areAllProductsOutOfStock(mProducts)) {
            layoutAddAll.setVisibility(View.GONE);
        } else {
            ((TextView) shopFromOrderLayout.findViewById(R.id.txtAddAll)).setTypeface(faceRobotoRegular);
            layoutAddAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog(null, getString(R.string.addAllProducts) + "?",
                            DialogButton.YES, DialogButton.CANCEL,
                            Constants.ADD_ALL_DIALOG_REQUEST, null, getString(R.string.yesTxt));
                }
            });
        }

        ViewGroup layoutEmptyList = (ViewGroup) shopFromOrderLayout.findViewById(R.id.layoutEmptyList);
        RecyclerView productRecyclerView = (RecyclerView) shopFromOrderLayout.findViewById(R.id.recyclerView);

        if (mProducts == null || mProducts.size() == 0) {
            productRecyclerView.setVisibility(View.GONE);
            layoutEmptyList.setVisibility(View.VISIBLE);
            shopFromOrderLayout.findViewById(R.id.btnBlankPage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            productRecyclerView.setVisibility(View.VISIBLE);
            layoutEmptyList.setVisibility(View.GONE);

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

            ArrayList<AbstractProductItem> productItems = new ArrayList<>(mProducts.size());
            for (Product p : mProducts) {
                productItems.add(new NormalProductItem(p));
            }
            productListRecyclerAdapter = new ProductListRecyclerAdapter(productItems, null,
                    productViewDisplayDataHolder, this, mProducts.size(),
                    getCurrentScreenName(), TrackEventkeys.SINGLE_TAB_NAME);

            productRecyclerView.setAdapter(productListRecyclerAdapter);
        }
        contentView.addView(shopFromOrderLayout);
        logShopFromOrderEvent();
    }


    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        if (sourceName == Constants.ADD_ALL_DIALOG_REQUEST) {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            addAllItemsToBasket();
        } else {
            super.onPositiveButtonClicked(sourceName, valuePassed);
        }
    }

    private void addAllItemsToBasket() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        Call<OldApiResponseWithCart> call = bigBasketApiService.addAllToBasketPastOrders(getPreviousScreenName(),mOrderId);
        call.enqueue(new BBNetworkCallback<OldApiResponseWithCart>(this) {
            @Override
            public void onSuccess(OldApiResponseWithCart addAllToBasketPastOrdersCallBack) {
                switch (addAllToBasketPastOrdersCallBack.status) {
                    case Constants.OK:
                        setCartSummary(addAllToBasketPastOrdersCallBack);
                        updateUIForCartInfo();
                        loadProducts();
                        break;
                    case Constants.ERROR:
                        handler.sendEmptyMessage(addAllToBasketPastOrdersCallBack.getErrorTypeAsInt(),
                                addAllToBasketPastOrdersCallBack.message);

                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                hideProgressView();
                return true;
            }
        });
    }

    private void logShopFromOrderEvent() {
        if (getArguments() == null || mOrderId == null) return;
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ORDER_ID, mOrderId);
        trackEvent(TrackingAware.SHOP_FROM_PAST_ORDER, eventAttribs);
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mProducts != null) {
            outState.putParcelableArrayList(Constants.PRODUCTS, mProducts);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.BASKET_CHANGED && data != null) {
            String productId = data.getStringExtra(Constants.SKU_ID);
            int productInQty = data.getIntExtra(Constants.NO_ITEM_IN_CART, 0);
            if (!TextUtils.isEmpty(productId) && getActivity() != null
                    && mProducts != null && productListRecyclerAdapter != null) {
                for (Product product : mProducts) {
                    if (product.getSku().equals(productId)) {
                        product.setNoOfItemsInCart(productInQty);
                        break;
                    }
                }
                productListRecyclerAdapter.notifyDataSetChanged();
            } else if (getCurrentActivity() != null) {
                getCurrentActivity().triggerActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "ShopFromOrderFragment";
    }
}
