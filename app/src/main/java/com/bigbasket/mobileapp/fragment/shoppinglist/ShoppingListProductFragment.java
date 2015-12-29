package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.adapter.product.AbstractProductItem;
import com.bigbasket.mobileapp.adapter.product.NormalProductItem;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
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
        String tabName = getArguments().getString(Constants.TAB_NAME);
        renderProducts(shoppingListSummary, baseImgUrl, tabName);
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
                .showQtyInput(AuthParameters.getInstance(getActivity()).isKirana())
                .useRadioButtonsForContextual(true)
                .build();
    }

    private void renderProducts(ShoppingListSummary shoppingListSummary, String baseImgUrl,
                                String tabName) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);
        ArrayList<AbstractProductItem> productItems = new ArrayList<>(shoppingListSummary.getProducts().size());
        for(Product p: shoppingListSummary.getProducts()){
            productItems.add(new NormalProductItem(p));
        }
        if (cartInfo != null) {
            productListAdapter = new ProductListRecyclerAdapter(productItems,
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(),
                    getNextScreenNavigationContext(),
                    cartInfo, tabName);
        } else {
            productListAdapter = new ProductListRecyclerAdapter(productItems,
                    baseImgUrl,
                    getProductViewHolder(shoppingListSummary), this, shoppingListSummary.getProducts().size(),
                    getNextScreenNavigationContext(), tabName);
        }
        productRecyclerView.setAdapter(productListAdapter);
        contentView.addView(productRecyclerView);
    }

    public void notifyDataChanged(HashMap<String, Integer> cartInfo,
                                  ShoppingListSummary shoppingListSummary, String baseImgUrl,
                                  String tabName) {
        this.cartInfo = cartInfo;
        renderProducts(shoppingListSummary, baseImgUrl, tabName);
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
                productListAdapter.setCartInfo(cartInfo);
                productListAdapter.notifyDataSetChanged();
            } else if (getCurrentActivity() != null) {
                getCurrentActivity().triggerActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "ShoppingListProductFragment";
    }
}