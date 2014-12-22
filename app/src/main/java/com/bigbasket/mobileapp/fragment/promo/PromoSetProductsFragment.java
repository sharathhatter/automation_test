package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.ProductListRecyclerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
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
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PromoSetProductsFragment extends ProductListAwareFragment implements CartInfoAware, BasketOperationAware {

    private View promoSummaryView = null;
    private int promoId;
    private String promoType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void restoreProductList(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        promoId = bundle.getInt(Constants.PROMO_ID);
        int setId = bundle.getInt(Constants.SET_ID);
        promoType = bundle.getString(Constants.PROMO_TYPE);
        String baseImgUrl = bundle.getString(Constants.BASE_IMG_URL);
        String productsListStr = bundle.getString(Constants.PRODUCT_LIST);
        double saving = bundle.getDouble(Constants.SAVING, 0);
        String promoInfoMsg = bundle.getString(Constants.INFO_MESSAGE);
        ArrayList<String> criteriaMsgs = bundle.getStringArrayList(Constants.CRITERIA_MSGS);
        int numPromoCompletedInBasket = bundle.getInt(Constants.NUM_IN_BASKET, 0);

        displayPromoSummary(promoInfoMsg, criteriaMsgs, saving, numPromoCompletedInBasket);
        ArrayList<Product> products = null;
        if (!TextUtils.isEmpty(productsListStr)) {
            products = ParserUtil.parseProductList(productsListStr);
        }
        renderPromoSet(setId, products, baseImgUrl);
    }

    @Override
    public void loadProducts() {
        // Not using the product-list API
    }

    @Override
    public void loadMoreProducts() {

    }

    @Override
    public String getProductListSlug() {
        return null;
    }

    @Override
    public String getProductQueryType() {
        return null;
    }

    private void renderPromoSet(final int setId,
                                ArrayList<Product> products, String baseImgUrl) {

        if (products != null) {
            displayProductList(products, baseImgUrl);
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
                                promoSetProductsApiResponseContent.apiResponseContent.baseImgUrl);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    hideProgressDialog();
                }
            });
        }

        if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO) ||
                promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO)) {
            // TODO : Add Bundle button implementation
            //View headerTitleLayout = getHeader().findViewById(R.id.header_title_layout);
//            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            btnParams.setMargins(5, 6, 10, 0);
//
//            int fiveDp = 6;
//            Button btnAddBundle = getLinearLayoutYellowButton("Add Bundle", fiveDp, 0, 0, fiveDp, fiveDp, fiveDp);
//            btnAddBundle.setBackgroundResource(R.drawable.yellow_button_border);
//            btnAddBundle.setTypeface(faceRobotoSlabNrml);
//            btnAddBundle.setTextColor(getResources().getColor(R.color.light_grey_text));
//            btnAddBundle.setTextSize(scaleToScreenIndependentPixel(9));
//            RelativeLayout header = getHeaderLayout();
//            header.addView(btnAddBundle, btnParams);
//            btnAddBundle.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    addBundle();
//                }
//            });
        }
    }

    private void addBundle() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.addPromoBundle(String.valueOf(promoId), new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse addBundleApiResponse, Response response) {
                hideProgressDialog();
                int status = addBundleApiResponse.status;
                if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                        || status == ApiErrorCodes.INVALID_FIELD || status == ApiErrorCodes.INVALID_PROMO) {
                    showErrorMsg(addBundleApiResponse.message);
                } else if (status == 0) {
                    // Operation Successful, now do a get-promo-summary API call
                    setCartInfo(addBundleApiResponse.cartSummary);
                    updateUIForCartInfo();
                    getPromoSummary();
                } else {
                    showErrorMsg("Server Error");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                showErrorMsg("Server Error");
            }
        });
    }

    private void getPromoSummary() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getPromoSummary(String.valueOf(promoId), new Callback<ApiResponse<PromoSummaryApiResponseContent>>() {
            @Override
            public void success(ApiResponse<PromoSummaryApiResponseContent> promoSummaryApiResponseContent, Response response) {
                hideProgressDialog();
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
                hideProgressDialog();
                showErrorMsg("Server Error");
            }
        });
    }

    private void displayPromoSummary(String promoInfoMsg, ArrayList<String> criteriaMsgs,
                                     double saving, int numPromoCompletedInBasket) {
        if (getActivity() == null || getCurrentActivity() == null) return;
        View promoSummaryView = getPromoSummaryView();
        TextView txtPromoInfoMsg = (TextView) promoSummaryView.findViewById(R.id.txtPromoInfoMsg);
        txtPromoInfoMsg.setTypeface(faceRobotoRegular);
        txtPromoInfoMsg.setText(promoInfoMsg);

        LinearLayout layoutCriteriaMsg = (LinearLayout) promoSummaryView.findViewById(R.id.layoutCriteriaMsg);
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

        TextView txtNumCompletedOffer = (TextView) promoSummaryView.findViewById(R.id.txtNumCompletedOffer);
        txtNumCompletedOffer.setTypeface(faceRobotoRegular);
        txtNumCompletedOffer.setText(PromoDetail.
                getNumCompletedInBasketSpannable(getResources().getColor(R.color.promo_txt_green_color),
                        numPromoCompletedInBasket));

        TextView txtSaving = (TextView) promoSummaryView.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);
        String savingFormattedAmount = UIUtil.formatAsMoney(saving);
        txtSaving.setText(PromoDetail.
                getSavingSpannable(getResources().getColor(R.color.promo_txt_green_color),
                        savingFormattedAmount, faceRupee));
    }

    private void displayProductList(ArrayList<Product> products, String baseImgUrl) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        showProgressDialog(getString(R.string.please_wait));
        RecyclerView productRecyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 2, 3);

        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .build();

        ProductListRecyclerAdapter productListAdapter = new ProductListRecyclerAdapter(products, baseImgUrl,
                getCurrentActivity(), productViewDisplayDataHolder, this, products.size(), getSourceName());

        productRecyclerView.setAdapter(productListAdapter);

        contentView.addView(productRecyclerView);
        if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO) ||
                promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {
            // TODO : Add Bundle button implementation
//            RelativeLayout relativeLayout = new RelativeLayout(getActivity());
//            LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            relativeLayout.setLayoutParams(relativeLayoutParams);
//            int eightDp = 8;
//            relativeLayout.setPadding(eightDp, eightDp, eightDp, eightDp);
//
//            Button btnAddBundle = new Button(getActivity());
//            btnAddBundle.setBackgroundResource(R.drawable.button_bg);
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//            btnAddBundle.setTypeface(null, Typeface.BOLD);
//            btnAddBundle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//            btnAddBundle.setPadding(eightDp, eightDp, eightDp, eightDp);
//            btnAddBundle.setText(getResources().getString(R.string.add_bundle));
//            btnAddBundle.setLayoutParams(layoutParams);
//            btnAddBundle.setGravity(Gravity.CENTER_HORIZONTAL);
//            btnAddBundle.setTextColor(getResources().getColor(R.color.dark_grey));
//            btnAddBundle.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    addBundle();
//                }
//            });
//            relativeLayout.addView(btnAddBundle);
//            contentView.addView(relativeLayout);
        }
        try {
            hideProgressDialog();
        } catch (IllegalArgumentException e) {
        }
    }

    private View getPromoSummaryView() {
        if (promoSummaryView == null) {

            LinearLayout contentView = getContentView();
            if (contentView == null) return null;

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            promoSummaryView = inflater.inflate(R.layout.promo_info_box, null);
            View layoutPromoInfoBox = promoSummaryView.findViewById(R.id.layoutPromoInfoBox);
            int fiveDp = 5;
            layoutPromoInfoBox.setPadding(0, fiveDp, 0, fiveDp);
            contentView.addView(promoSummaryView);
        }
        return promoSummaryView;
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView, ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket, EditText editTextQty, Product product, String qty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, imgDecQty, imgIncQty, btnAddToBasket, editTextQty, product, qty);
        getPromoSummary();
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void parcelProductList(Bundle outState) {
        // Not doing anything
    }

    @Override
    public String getTitle() {
        return "Promotion";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PromoSetProductsFragment.class.getName();
    }


    @Override
    public String getSourceName(){
        return TrackEventkeys.PROMO_CAT;
    }
}