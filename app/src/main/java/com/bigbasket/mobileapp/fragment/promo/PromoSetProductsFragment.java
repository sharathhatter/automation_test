package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.AbstractProductItem;
import com.bigbasket.mobileapp.adapter.product.NormalProductItem;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartInfo;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSetProductsApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PromoSummaryApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.model.promo.PromoMessage;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Call;


public class PromoSetProductsFragment extends ProductListAwareFragment implements CartInfoAware, BasketOperationAware {

    private View promoProductListView = null;
    private int promoId;
    private String promoType;
    @Nullable
    private ProductListRecyclerAdapter productListAdapter;
    @Nullable
    private HashMap<String, Integer> cartInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void productListOnActivityCreated() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NC_PROMO_PRODUCT_LISTING);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
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
        if (promoProductListView == null) return;
        displayPromoSummary(promoInfoMsg, criteriaMsgs, saving, numPromoCompletedInBasket);
        renderProductList(products);
    }

    private void renderProductList(ArrayList<Product> products) {
        Bundle bundle = getArguments();
        String baseImgUrl = bundle.getString(Constants.BASE_IMG_URL);
        int setId = bundle.getInt(Constants.SET_ID);
        if (cartInfo == null) {
            String cartString = bundle.getString(Constants.CART_INFO);
            if (!TextUtils.isEmpty(cartString)) {
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, Integer>>() {
                }.getType();
                cartInfo = gson.fromJson(cartString, type);
            }
        }
        renderPromoSet(setId, products, baseImgUrl);
    }

    @Override
    public void loadMoreProducts() {

    }

    private void renderPromoSet(final int setId,
                                ArrayList<Product> products, String baseImgUrl) {

        if (products != null) {
            displayProductList(products, baseImgUrl);
        } else {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<PromoSetProductsApiResponseContent>> call =
                    bigBasketApiService.getPromoSetProducts(String.valueOf(promoId), String.valueOf(setId));
            call.enqueue(new BBNetworkCallback<ApiResponse<PromoSetProductsApiResponseContent>>(this) {
                @Override
                public void onSuccess(ApiResponse<PromoSetProductsApiResponseContent> promoSetProductsApiResponseContent) {
                    int status = promoSetProductsApiResponseContent.status;
                    if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                            || status == ApiErrorCodes.INVALID_FIELD || status == ApiErrorCodes.PROMO_CRITERIA_SET_NOT_EXISTS) {
                        showErrorMsg(promoSetProductsApiResponseContent.message);
                    } else if (status == 0) {
                        displayProductList(promoSetProductsApiResponseContent.apiResponseContent.promoSetProducts,
                                promoSetProductsApiResponseContent.apiResponseContent.baseImgUrl);
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
    }

    private void addBundle(final ArrayList<Product> products, final String baseImgUrl) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<CartInfo>> call = bigBasketApiService.addPromoBundle(String.valueOf(promoId));
        call.enqueue(new BBNetworkCallback<ApiResponse<CartInfo>>(this) {
            @Override
            public void onSuccess(ApiResponse<CartInfo> addBundleApiResponse) {
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
                    cartInfo = addBundleApiResponse.apiResponseContent.cartInfo;
                    if (addBundleApiResponse.apiResponseContent.cartInfo != null) {
                        notifyPromoProducts(products, baseImgUrl);
                    }
                    markBasketDirty();
                } else {
                    handler.sendEmptyMessage(addBundleApiResponse.status, addBundleApiResponse.message);
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

    private void getPromoSummary() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        Call<ApiResponse<PromoSummaryApiResponseContent>> call = bigBasketApiService.getPromoSummary(String.valueOf(promoId));
        call.enqueue(new BBNetworkCallback<ApiResponse<PromoSummaryApiResponseContent>>(this) {
            @Override
            public void onSuccess(ApiResponse<PromoSummaryApiResponseContent> promoSummaryApiResponseContent) {
                int status = promoSummaryApiResponseContent.status;
                if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                        || status == ApiErrorCodes.INVALID_FIELD) {
                    showErrorMsg(promoSummaryApiResponseContent.message);
                } else if (status == 0) {
                    PromoMessage promoMessage = promoSummaryApiResponseContent.apiResponseContent.promoMessage;
                    displayPromoSummary(promoMessage.getPromoMessage(), promoMessage.getCriteriaMessages(),
                            promoSummaryApiResponseContent.apiResponseContent.saving,
                            promoSummaryApiResponseContent.apiResponseContent.numInBasket);
                } else {
                    handler.sendEmptyMessage(promoSummaryApiResponseContent.status,
                            promoSummaryApiResponseContent.message);
                }
            }

            @Override
            public boolean updateProgress() {
                return true;
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
                getNumCompletedInBasketSpannable(ContextCompat.getColor(getActivity(), R.color.promo_txt_green_color),
                        numPromoCompletedInBasket));

        TextView txtSaving = (TextView) promoProductListView.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);
        String savingFormattedAmount = UIUtil.formatAsMoney(saving);
        txtSaving.setText(PromoDetail.
                getSavingSpannable(ContextCompat.getColor(getActivity(), R.color.promo_txt_green_color),
                        savingFormattedAmount, faceRupee));
    }

    private ProductViewDisplayDataHolder getProductDisplayHolder() {
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

    private void displayProductList(final ArrayList<Product> products, final String baseImgUrl) {
        if (getActivity() == null) return;
        if (products == null) {
            showAlertDialogFinish(getString(R.string.error), getString(R.string.server_error));
            return;
        }

        LinearLayout layoutPromoProductList = (LinearLayout) promoProductListView.findViewById(R.id.layoutPromoProductList);
        if (layoutPromoProductList == null) return;
        layoutPromoProductList.removeAllViews();

        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, layoutPromoProductList);
        ArrayList<AbstractProductItem> productItems = new ArrayList<>(products.size());
        for(Product p: products){
            productItems.add(new NormalProductItem(p));
        }
        if (cartInfo == null) {
            productListAdapter = new ProductListRecyclerAdapter(productItems, baseImgUrl,
                    getProductDisplayHolder(), this, products.size(), getCurrentScreenName(),
                    TrackEventkeys.SINGLE_TAB_NAME);
        } else {
            productListAdapter = new ProductListRecyclerAdapter(productItems, baseImgUrl,
                    getProductDisplayHolder(), this, products.size(), getCurrentScreenName(),
                    cartInfo, TrackEventkeys.SINGLE_TAB_NAME);
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
                    trackEvent(TrackingAware.PLACE_ORDER_KNOW_MORE_LINK_CLICKED, null);
                    addBundle(products, baseImgUrl);
                }
            });
        } else {
            layoutAddBundle.setVisibility(View.GONE);
        }

        HashMap<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(getArguments().getString(Constants.PROMO_NAME)))
            map.put(TrackEventkeys.PROMO_NAME, getArguments().getString(Constants.PROMO_NAME));
        trackEvent(TrackingAware.PROMO_SET_PRODUCTS_SHOWN, map);
    }

    private void notifyPromoProducts(ArrayList<Product> products, String baseImgUrl) {
        displayProductList(products, baseImgUrl);
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
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation,
                                                    @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                    @Nullable WeakReference<View> viewDecQtyRef,
                                                    @Nullable WeakReference<View> viewIncQtyRef,
                                                    @Nullable WeakReference<View> btnAddToBasketRef,
                                                    Product product, String qty,
                                                    @Nullable WeakReference<View> productViewRef,
                                                    @Nullable WeakReference<HashMap<String, Integer>> cartInfoMapRef,
                                                    @Nullable WeakReference<EditText> editTextQtyRef) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextViewRef, viewDecQtyRef,
                viewIncQtyRef, btnAddToBasketRef, product, qty, productViewRef, cartInfoMapRef, editTextQtyRef);
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
            } else if (getCurrentActivity() != null) {
                getCurrentActivity().triggerActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "PromoSetProductsFragment";
    }
}