package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartInfo;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.model.promo.PromoMessage;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PromoSetProductsFragment extends ProductListAwareFragment implements CartInfoAware, BasketOperationAware {

    private View promoProductListView = null;
    private int promoId;
    private String promoType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void loadProducts() {
        Bundle bundle = getArguments();

        promoId = bundle.getInt(Constants.PROMO_ID);
        promoType = bundle.getString(Constants.PROMO_TYPE);
        ArrayList<Product> products = bundle.getParcelableArrayList(Constants.PRODUCT_LIST);
        double saving = bundle.getDouble(Constants.SAVING, 0);
        String promoInfoMsg = bundle.getString(Constants.INFO_MESSAGE);
        ArrayList<String> criteriaMsgs = bundle.getStringArrayList(Constants.CRITERIA_MSGS);
        int numPromoCompletedInBasket = bundle.getInt(Constants.NUM_IN_BASKET, 0);

        getPromoSummaryView();
        if(promoProductListView ==  null)return;
        displayPromoSummary(promoInfoMsg, criteriaMsgs, saving, numPromoCompletedInBasket);
        renderProductList(products);
    }

    private void renderProductList(ArrayList<Product> products){
        Bundle bundle = getArguments();
        String baseImgUrl = bundle.getString(Constants.BASE_IMG_URL);
        int setId = bundle.getInt(Constants.SET_ID);
        String cartString = bundle.getString(Constants.CART_INFO);
        HashMap<String, Integer> cartInfo = null;
        if (!TextUtils.isEmpty(cartString)) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Integer>>() {
            }.getType();
            cartInfo = gson.fromJson(cartString, type);
        }
        renderPromoSet(setId, products, baseImgUrl, cartInfo);
    }

    @Override
    public void loadMoreProducts() {

    }

    private void renderPromoSet(final int setId,
                                ArrayList<Product> products, String baseImgUrl,
                                final HashMap<String, Integer> cartInfo) {

        if (products != null) {
            displayProductList(products, baseImgUrl, cartInfo);
        } else {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.getPromoSetProducts(String.valueOf(promoId), String.valueOf(setId), new Callback<ApiResponse<PromoSetProductsApiResponseContent>>() {
                @Override
                public void success(ApiResponse<PromoSetProductsApiResponseContent> promoSetProductsApiResponseContent, Response response) {
                    hideProgressDialog();
                    int status = promoSetProductsApiResponseContent.status;
                    if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                            || status == ApiErrorCodes.INVALID_FIELD || status == ApiErrorCodes.PROMO_CRITERIA_SET_NOT_EXISTS) {
                        showErrorMsg(promoSetProductsApiResponseContent.message);
                    } else if (status == 0) {
                        displayProductList(promoSetProductsApiResponseContent.apiResponseContent.promoSetProducts,
                                promoSetProductsApiResponseContent.apiResponseContent.baseImgUrl, cartInfo);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    hideProgressDialog();
                }
            });
        }
    }

    private void addBundle(final ArrayList<Product> products, final String baseImgUrl) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.addPromoBundle(String.valueOf(promoId), new Callback<ApiResponse<CartInfo>>() {
            @Override
            public void success(ApiResponse<CartInfo> addBundleApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                int status = addBundleApiResponse.status;
                if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                        || status == ApiErrorCodes.INVALID_FIELD || status == ApiErrorCodes.INVALID_PROMO) {
                    showErrorMsg(addBundleApiResponse.message);
                } else if (status == 0) {
                    // Operation Successful, now do a get-promo-summary API call
                    setCartSummary(addBundleApiResponse.cartSummary);
                    updateUIForCartInfo();
                    getPromoSummary();
                    if(addBundleApiResponse.apiResponseContent.cartInfo !=null) {
                        notifyPromoProducts(products, baseImgUrl,
                                addBundleApiResponse.apiResponseContent.cartInfo);
                    }
                } else {
                    showErrorMsg("Server Error");
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
                showErrorMsg("Server Error");
            }
        });
    }

    private void getPromoSummary() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        bigBasketApiService.getPromoSummary(String.valueOf(promoId), new Callback<ApiResponse<PromoSummaryApiResponseContent>>() {
            @Override
            public void success(ApiResponse<PromoSummaryApiResponseContent> promoSummaryApiResponseContent, Response response) {
                int status = promoSummaryApiResponseContent.status;
                if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                        || status == ApiErrorCodes.INVALID_FIELD) {
                    showErrorMsg(promoSummaryApiResponseContent.message);
                } else if (status == 0) {
                    PromoMessage promoMessage = promoSummaryApiResponseContent.apiResponseContent.promoMessage;
                    if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO) ||
                            promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {
                        Toast aToastForSuccess = Toast.makeText(getActivity(), "Added bundle successfully", Toast.LENGTH_SHORT);
                        aToastForSuccess.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        aToastForSuccess.show();
                    }
                    displayPromoSummary(promoMessage.getPromoMessage(), promoMessage.getCriteriaMessages(),
                            promoSummaryApiResponseContent.apiResponseContent.saving,
                            promoSummaryApiResponseContent.apiResponseContent.numInBasket);
                } else {
                    showErrorMsg("Server Error");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    showErrorMsg("Server Error");
                } catch (IllegalArgumentException e) {
                    return;
                }

            }
        });
    }

    private void displayPromoSummary(String promoInfoMsg, ArrayList<String> criteriaMsgs,
                                     double saving, int numPromoCompletedInBasket) {
        if (getActivity() == null || getCurrentActivity() == null) return;
        TextView txtPromoInfoMsg = (TextView) promoProductListView.findViewById(R.id.txtPromoInfoMsg);
        txtPromoInfoMsg.setTypeface(faceRobotoRegular);
        txtPromoInfoMsg.setText(promoInfoMsg);

        LinearLayout layoutCriteriaMsg = (LinearLayout) promoProductListView.findViewById(R.id.layoutCriteriaMsg);
        List<Spannable> criteriaSpannable = UIUtil.createBulletSpannableList(criteriaMsgs);
        if (criteriaSpannable != null && criteriaSpannable.size() > 0) {
            layoutCriteriaMsg.removeAllViews();
            layoutCriteriaMsg.setVisibility(View.VISIBLE);
            for (Spannable spannable : criteriaSpannable) {
                TextView txtPromoMsg = UIUtil.getPromoMsgTextView(getCurrentActivity());
                txtPromoMsg.setTypeface(faceRobotoRegular);
                txtPromoMsg.setText(spannable);
                layoutCriteriaMsg.addView(txtPromoMsg);
            }
        } else {
            layoutCriteriaMsg.setVisibility(View.GONE);
        }

        TextView txtNumCompletedOffer = (TextView) promoProductListView.findViewById(R.id.txtNumCompletedOffer);
        txtNumCompletedOffer.setTypeface(faceRobotoRegular);
        txtNumCompletedOffer.setText(PromoDetail.
                getNumCompletedInBasketSpannable(getResources().getColor(R.color.promo_txt_green_color),
                        numPromoCompletedInBasket));

        TextView txtSaving = (TextView) promoProductListView.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);
        String savingFormattedAmount = UIUtil.formatAsMoney(saving);
        txtSaving.setText(PromoDetail.
                getSavingSpannable(getResources().getColor(R.color.promo_txt_green_color),
                        savingFormattedAmount, faceRupee));
    }

    private ProductViewDisplayDataHolder getProductDisplayHodler(){
        return new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setSansSerifMediumTypeface(faceRobotoMedium)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .build();
    }

    private void displayProductList(final ArrayList<Product> products, final String baseImgUrl,
                                    HashMap<String, Integer> cartInfo) {
        if (getActivity() == null) return;
        if(products==null){
            showAlertDialogFinish(getString(R.string.error), getString(R.string.server_error));
            return;
        }

        LinearLayout layoutPromoProductList = (LinearLayout)promoProductListView.findViewById(R.id.layoutPromoProductList);
        if (layoutPromoProductList == null) return;
        layoutPromoProductList.removeAllViews();

        showProgressDialog(getString(R.string.please_wait));
        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, layoutPromoProductList);

        ProductListRecyclerAdapter productListAdapter;
        if(cartInfo==null){
            productListAdapter = new ProductListRecyclerAdapter(products, baseImgUrl,
                    getProductDisplayHodler(), this, products.size(), getNavigationCtx());
        }else {
            productListAdapter = new ProductListRecyclerAdapter(products, baseImgUrl,
                    getProductDisplayHodler(), this, products.size(), getNavigationCtx(),
                    cartInfo);
        }

        productRecyclerView.setAdapter(productListAdapter);

        layoutPromoProductList.addView(productRecyclerView);

        LinearLayout layoutAddBundle = (LinearLayout) promoProductListView.findViewById(R.id.layoutAddBundle);
        if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO) ||
                promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {

            layoutAddBundle.setVisibility(View.VISIBLE);
            layoutAddBundle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBundle(products, baseImgUrl);
                }
            });
        }else {
            layoutAddBundle.setVisibility(View.GONE);
        }
        try {
            hideProgressDialog();
        } catch (IllegalArgumentException e) {
        }
    }

    private void notifyPromoProducts(ArrayList<Product> products, String baseImgUrl,
                                     HashMap<String, Integer> cartInfo){
        displayProductList(products, baseImgUrl, cartInfo);
    }

    private View getPromoSummaryView() {
        if (promoProductListView == null) {
            ViewGroup contentView = getContentView();
            if (contentView == null) return null;
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            promoProductListView = inflater.inflate(R.layout.promo_info_box, contentView, false);
            contentView.addView(promoProductListView);
        }
        return promoProductListView;
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView, View viewDecQty,
                                                    View viewIncQty, View btnAddToBasket, Product product, String qty,
                                                    @Nullable View productView, @Nullable HashMap<String, Integer> cartInfo) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty,
                viewIncQty, btnAddToBasket, product, qty, productView, cartInfo);
        getPromoSummary();
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        if (getArguments() != null) {
            String setName = getArguments().getString(Constants.NAME);
            if (!TextUtils.isEmpty(setName)) {
                return setName;
            } else {
                String promoName = getArguments().getString(Constants.PROMO_NAME);
                if (!TextUtils.isEmpty(promoName)) {
                    return promoName;
                }
            }
        }
        return "Promotion";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PromoSetProductsFragment.class.getName();
    }


    @Override
    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_PROMO;
    }
}