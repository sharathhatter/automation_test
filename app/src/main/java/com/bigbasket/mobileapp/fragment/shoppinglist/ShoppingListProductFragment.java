package com.bigbasket.mobileapp.fragment.shoppinglist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListDetail;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ShoppingListProductFragment extends ProductListAwareFragment {

    private ShoppingListName mShoppingListName;
    private ShoppingListDetail mShoppingListDetail;
    private String mBaseImgUrl;

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
        if(mShoppingListName.isSystem()){
            if(mShoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG))
                return TrackEventkeys.SMART_BASKET;
            else
                return TrackEventkeys.SYSTEM_SHOPPING_LIST;
        }else {
            return TrackEventkeys.SHOPPING_LIST;
        }
    }

    @Override
    public void restoreProductList(Bundle savedInstanceState) {
        mShoppingListName = getArguments().getParcelable(Constants.SHOPPING_LIST_NAME);
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

    @Override
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
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
        contentView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View shopListHeaderLayout = inflater.inflate(R.layout.my_shopping_list_listheader, null);
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
                    if (checkInternetConnection()) {
                        //todo add all product event
//                            showAlertDialog(getCurrentActivity(), null, "Are you sure you want to add all products to the basket?",
//                                    "addall");
                    } else {
                        Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        contentView.addView(shopListHeaderLayout);
        renderProducts(productList);
    }

    private void renderProducts(ArrayList<Product> productList) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 2, 3);

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setHandler(new BigBasketMessageHandler<>(getCurrentActivity()))
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(!mShoppingListName.isSystem())
                .setShoppingListName(mShoppingListName)
                .setRupeeTypeface(faceRupee)
                .build();

        ProductListRecyclerAdapter productListAdapter = new ProductListRecyclerAdapter(productList, mBaseImgUrl,
                getCurrentActivity(), productViewDisplayDataHolder, this, productList.size(), getSourceName());

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