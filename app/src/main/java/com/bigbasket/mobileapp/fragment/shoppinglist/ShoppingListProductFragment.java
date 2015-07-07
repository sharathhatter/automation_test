package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    @Nullable
    private ProductListRecyclerAdapter productListAdapter;

    @Nullable
    private HashMap<String, Integer> cartInfo;

    @Override
    public void loadProducts() {
        loadShoppingListProducts();
    }

    @Override
    public void loadMoreProducts() {
        // Do nothing
    }

    private void loadShoppingListProducts() {
        ShoppingListSummary shoppingListSummary = getArguments().getParcelable(Constants.SHOPPING_LIST_SUMMARY);
        String baseImgUrl = getArguments().getString(Constants.BASE_IMG_URL);
        renderProducts(shoppingListSummary, baseImgUrl);
    }

    private ProductViewDisplayDataHolder getProductViewHolder(ShoppingListSummary shoppingListSummary) {
        return new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setSansSerifMediumTypeface(faceRobotoMedium)
                .setHandler(new BigBasketMessageHandler<>(getCurrentActivity()))
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(false)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(!shoppingListSummary.getShoppingListName().isSystem())
                .setShoppingListName(shoppingListSummary.getShoppingListName())
                .setRupeeTypeface(faceRupee)
                .build();
    }

    private void renderProducts(ShoppingListSummary shoppingListSummary, String baseImgUrl) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

        if (cartInfo != null) {
            productListAdapter = new ProductListRecyclerAdapter(shoppingListSummary.getProducts(),
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(),
                    getNextScreenNavigationContext(),
                    cartInfo);
        } else {
            productListAdapter = new ProductListRecyclerAdapter(shoppingListSummary.getProducts(),
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(),
                    getNextScreenNavigationContext());
        }
        productRecyclerView.setAdapter(productListAdapter);
        contentView.addView(productRecyclerView);
    }

    public void notifyDataChanged(HashMap<String, Integer> cartInfo,
                                  ShoppingListSummary shoppingListSummary, String baseImgUrl) {
        this.cartInfo = cartInfo;
        renderProducts(shoppingListSummary, baseImgUrl);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void postShoppingListItemDeleteOperation() {
        if (getActivity() == null) return;
        ((ShoppingListSummaryActivity) getActivity()).loadShoppingListSummary();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.BASKET_CHANGED && data != null) {
            String productId = data.getStringExtra(Constants.SKU_ID);
            int productInQty = data.getIntExtra(Constants.NO_ITEM_IN_CART, 0);
            if (!TextUtils.isEmpty(productId) && getActivity() != null
                    && productListAdapter != null) {
                if (cartInfo == null) {
                    cartInfo = new HashMap<>();
                }
                cartInfo.put(productId, productInQty);
                productListAdapter.notifyDataSetChanged();
            } else if (getCurrentActivity() != null) {
                getCurrentActivity().triggerActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void logProductListingEvent() {
        ShoppingListSummary shoppingListSummary = getArguments().getParcelable(Constants.SHOPPING_LIST_SUMMARY);
        ShoppingListName shoppingListName = shoppingListSummary.getShoppingListName();
        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(Constants.TYPE, shoppingListSummary.getFacetSlug());
        if (shoppingListName != null) {
            String nc = shoppingListName.getNc();
            nc += "." + shoppingListSummary.getFacetSlug();
            setNextScreenNavigationContext(nc);
            eventAttribs.put(Constants.NAME, shoppingListName.getSlug());
            if (!shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
                trackEvent(TrackingAware.SHOP_LST_SUMMARY_SHOWN, eventAttribs);
            } else {
                trackEvent(TrackingAware.SMART_BASKET_SUMMARY_SHOWN, eventAttribs);
            }
        }
    }
}