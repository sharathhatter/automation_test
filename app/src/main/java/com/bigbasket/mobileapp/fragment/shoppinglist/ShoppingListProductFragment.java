package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    //private ProductListRecyclerAdapter productListAdapter;
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
        renderProducts(shoppingListSummary, baseImgUrl, null);
    }

    private ProductViewDisplayDataHolder getProductViewHolder(ShoppingListSummary shoppingListSummary){
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

    private void renderProducts(ShoppingListSummary shoppingListSummary, String baseImgUrl,
                                HashMap<String, Integer> cartInfo) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);

        ProductListRecyclerAdapter productListAdapter;
        if(cartInfo!=null){
            productListAdapter = new ProductListRecyclerAdapter(shoppingListSummary.getProducts(),
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(), getNavigationCtx(),
                    cartInfo);
        }else {
            productListAdapter = new ProductListRecyclerAdapter(shoppingListSummary.getProducts(),
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(), getNavigationCtx());
        }
        productRecyclerView.setAdapter(productListAdapter);
        contentView.addView(productRecyclerView);
    }

    public void notifyDataChanged(HashMap<String, Integer> cartInfo,
                                  ShoppingListSummary shoppingListSummary, String baseImgUrl){
        renderProducts(shoppingListSummary, baseImgUrl, cartInfo);
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
}