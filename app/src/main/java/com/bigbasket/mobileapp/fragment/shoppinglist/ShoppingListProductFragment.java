package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    @Override
    public void loadProducts() {
        loadShoppingListProducts();
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }


    @Override
    public String getNavigationCtx() {
        ShoppingListSummary shoppingListSummary = getArguments().getParcelable(Constants.SHOPPING_LIST_SUMMARY);
        String shoppingListSlug = shoppingListSummary.getShoppingListName().getSlug();
        if (shoppingListSlug.equals(Constants.SMART_BASKET_SLUG))
            return TrackEventkeys.NAVIGATION_CTX_SMART_BASKET;
        else
            return TrackEventkeys.NAVIGATION_CTX_SHOPPING_LIST;
    }

    private void loadShoppingListProducts() {
        ShoppingListSummary shoppingListSummary = getArguments().getParcelable(Constants.SHOPPING_LIST_SUMMARY);
        String baseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
        renderShoppingListItems(shoppingListSummary, baseImgUrl);
    }

    private void renderShoppingListItems(final ShoppingListSummary shoppingListSummary,
                                         String baseImgUrl) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        contentView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View shopListHeaderLayout = inflater.inflate(R.layout.uiv3_shopping_list_products_header, contentView, false);
        TextView brandNameTxt = (TextView) shopListHeaderLayout.findViewById(R.id.brandNameTxt);

        brandNameTxt.setText(UIUtil.abbreviate(shoppingListSummary.getFacetName(), 25));
        brandNameTxt.setTypeface(faceRobotoRegular);

        Button btnAddAllToBasket = (Button) shopListHeaderLayout.findViewById(R.id.btnAddAllToBasket);
        if (Product.areAllProductsOutOfStock(shoppingListSummary.getProducts())) {
            btnAddAllToBasket.setVisibility(View.GONE);
        } else {
            btnAddAllToBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog(null, getString(R.string.addAllProducts),
                            DialogButton.YES, DialogButton.CANCEL, Constants.ADD_ALL, shoppingListSummary, getString(R.string.yesTxt));
                }
            });
        }
        contentView.addView(shopListHeaderLayout);
        renderProducts(shoppingListSummary, baseImgUrl);
    }


    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equalsIgnoreCase(Constants.ADD_ALL)) {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            addAllItemsToBasket((ShoppingListSummary) valuePassed);
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    private void addAllItemsToBasket(ShoppingListSummary shoppingListSummary) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        String shoppingListSlug = getArguments().getString(Constants.SHOPPING_LIST_SLUG);
        if (shoppingListSummary.getFacetSlug().equals(Constants.SMART_BASKET_SLUG)) {
            trackEvent(TrackingAware.SMART_BASKET + "." + shoppingListSummary.getFacetName() + " Add All", null);
            bigBasketApiService.addAllToBasketSmartBasket(shoppingListSlug,
                    shoppingListSummary.getFacetSlug(),
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
                                            addAllToBasketSmartBasketCallBack.message, false);
                                    break;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isSuspended()) return;
                            hideProgressView();
                            handler.handleRetrofitError(error, true);
                        }
                    });
        } else {
            trackEvent(TrackingAware.SHOPPING_LIST + "." + shoppingListSummary.getFacetName() + " Add All", null);
            bigBasketApiService.addAllToBasketShoppingList(shoppingListSlug,
                    shoppingListSummary.getFacetSlug(),
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
                                            addAllToBasketShoppingListCallBack.message, false);
                                    break;
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isSuspended()) return;
                            hideProgressView();
                            handler.handleRetrofitError(error, true);
                        }
                    });
        }
    }

    private void renderProducts(ShoppingListSummary shoppingListSummary, String baseImgUrl) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setHandler(new BigBasketMessageHandler<>(getCurrentActivity()))
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(false)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(!shoppingListSummary.getShoppingListName().isSystem())
                .setShoppingListName(shoppingListSummary.getShoppingListName())
                .setRupeeTypeface(faceRupee)
                .build();

        ProductListRecyclerAdapter productListAdapter = new ProductListRecyclerAdapter(shoppingListSummary.getProducts(),
                baseImgUrl,
                productViewDisplayDataHolder, this, shoppingListSummary.getProducts().size(), getNavigationCtx());

        productRecyclerView.setAdapter(productListAdapter);
        contentView.addView(productRecyclerView);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void postShoppingListItemDeleteOperation() {
        loadShoppingListProducts();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShoppingListProductFragment.class.getName();
    }
}