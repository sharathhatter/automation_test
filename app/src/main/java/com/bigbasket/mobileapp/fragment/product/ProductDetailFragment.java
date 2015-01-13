package com.bigbasket.mobileapp.fragment.product;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ProductDetailApiResponse;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductAdditionalInfo;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ProductDetailFragment extends BaseFragment implements ShoppingListNamesAware {

    private String selectedProductId;
    private Product mProduct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_product_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mProduct = savedInstanceState.getParcelable(Constants.PRODUCT);
            if (mProduct != null) {
                renderProductDetail();
                return;
            }
        }
        loadProductDetail();
    }

    private void loadProductDetail() {
        Bundle args = getArguments();
        if (args == null) return;
        String productId = args.getString(Constants.SKU_ID);
        if (TextUtils.isEmpty(productId)) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.productDetails(productId, new Callback<ProductDetailApiResponse>() {
            @Override
            public void success(ProductDetailApiResponse productDetailApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (productDetailApiResponse.status) {
                    case Constants.OK:
                        mProduct = productDetailApiResponse.product;

                        HashMap<String, String> map = new HashMap<>();
                        map.put(TrackEventkeys.PRODUCT_TOP_CAT, mProduct.getTopLevelCategoryName());
                        map.put(TrackEventkeys.PRODUCT_CAT, mProduct.getProductCategoryName());
                        map.put(TrackEventkeys.PRODUCT_BRAND, mProduct.getBrand());
                        String desc = mProduct.getDescription();
                        if (!TextUtils.isEmpty(mProduct.getPackageDescription()))
                            desc = " " + mProduct.getWeightAndPackDesc();
                        map.put(TrackEventkeys.PRODUCT_DESC, desc);
                        trackEvent(TrackingAware.BROWSE_PRODUCT_DETAILS, map);

                        renderProductDetail();
                        break;
                    default:
                        handler.sendEmptyMessage(productDetailApiResponse.getErrorTypeAsInt());
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


    private void renderProductDetail() {
        if (getActivity() == null || getView() == null) return;

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .build();

        LinearLayout layoutProductDetail = (LinearLayout) getView().findViewById(R.id.layoutProductDetail);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View productRow = inflater.inflate(R.layout.uiv3_stretched_product_row, layoutProductDetail, false);
        LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        productRow.setLayoutParams(productRowParams);
        ProductView.setProductView(new ProductViewHolder(productRow), mProduct, null, null, productViewDisplayDataHolder,
                false, this, TrackEventkeys.PRODUCT_DETAIL);
        layoutProductDetail.addView(productRow);

        ArrayList<ProductAdditionalInfo> productAdditionalInfos = mProduct.getProductAdditionalInfos();
        if (productAdditionalInfos != null && productAdditionalInfos.size() > 0) {
            for (ProductAdditionalInfo productAdditionalInfo : productAdditionalInfos) {
                View additionalInfoView = inflater.inflate(R.layout.uiv3_product_add_desc, layoutProductDetail, false);
                TextView txtProductAddDescTitle = (TextView) additionalInfoView.findViewById(R.id.txtProductAddDescTitle);
                TextView txtProductAddDescContent = (TextView) additionalInfoView.findViewById(R.id.txtProductAddDescContent);
                txtProductAddDescContent.setTypeface(faceRobotoRegular);
                txtProductAddDescTitle.setTypeface(faceRobotoRegular);
                txtProductAddDescContent.setText(Html.fromHtml(productAdditionalInfo.getContent()));
                txtProductAddDescTitle.setText(Html.fromHtml(productAdditionalInfo.getTitle()));
                layoutProductDetail.addView(additionalInfoView);
            }
        }
    }

    @Override
    public String getTitle() {
        return "Detail";
    }

    @Override
    public LinearLayout getContentView() {
        return null;
    }

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        if (shoppingListNames == null || shoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), "Create a new shopping list", Toast.LENGTH_SHORT).show();
        } else {
            ShoppingListNamesDialog shoppingListNamesDialog = ShoppingListNamesDialog.newInstance(shoppingListNames);
            shoppingListNamesDialog.setTargetFragment(this, 0);
            shoppingListNamesDialog.show(getFragmentManager(), Constants.SHOP_LST);
        }
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

    }

    @Override
    public void addToShoppingList(List<ShoppingListName> selectedShoppingListNames) {
        if (getActivity() == null) return;
        if (selectedShoppingListNames == null || selectedShoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.chooseShopList), Toast.LENGTH_SHORT).show();
            return;
        }
        ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                new ShoppingListDoAddDeleteTask<>(this, selectedShoppingListNames,
                        ShoppingListOption.ADD_TO_LIST);
        shoppingListDoAddDeleteTask.startTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mProduct != null) {
            outState.putParcelable(Constants.PRODUCT, mProduct);
        }
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ProductDetailFragment.class.getName();
    }
}