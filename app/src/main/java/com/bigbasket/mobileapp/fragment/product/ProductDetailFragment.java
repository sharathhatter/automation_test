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

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductAdditionalInfo;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;


public class ProductDetailFragment extends BaseFragment implements ShoppingListNamesAware {


    private ArrayList<ShoppingListName> shoppingListNames;
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
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.PRODUCT_DETAIL + "?" +
                Constants.PROD_ID + "=" + productId;
        startAsyncActivity(url, null, false, false, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.PRODUCT_DETAIL)) {
            JsonObject httpResponseJsonOj = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            String status = httpResponseJsonOj.get(Constants.STATUS).getAsString();
            switch (status) {
                case Constants.OK:
                    JsonObject productJsonObj = httpResponseJsonOj.get(Constants.PRODUCT).getAsJsonObject();
                    mProduct = ParserUtil.parseProduct(productJsonObj, null);
                    renderProductDetail();
                    break;
                default:
                    showErrorMsg("Server Error");
                    // TODO : Add error handling
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
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
        View productRow = inflater.inflate(R.layout.uiv3_product_row, null);
        LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        productRow.setLayoutParams(productRowParams);
        ProductView.setProductView(new ProductViewHolder(productRow), mProduct, null, null, productViewDisplayDataHolder,
                getBaseActivity(), false, this);
        layoutProductDetail.addView(productRow);

        ArrayList<ProductAdditionalInfo> productAdditionalInfos = mProduct.getProductAdditionalInfos();
        if (productAdditionalInfos != null && productAdditionalInfos.size() > 0) {
            for (ProductAdditionalInfo productAdditionalInfo : productAdditionalInfos) {
                View additionalInfoView = inflater.inflate(R.layout.uiv3_product_add_desc, null);
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
    public ArrayList<ShoppingListName> getShoppingListNames() {
        return shoppingListNames;
    }

    @Override
    public void setShoppingListNames(ArrayList<ShoppingListName> shoppingListNames) {
        this.shoppingListNames = shoppingListNames;
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