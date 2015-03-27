package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.model.promo.PromoSet;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv2.ProductView;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PromoDetailFragment extends BaseFragment {

    private PromoDetail mPromoDetail;
    private PromoCategory mPromoCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mPromoDetail = savedInstanceState.getParcelable(Constants.PROMO_DETAIL);
            if (mPromoDetail != null) {
                mPromoCategory = savedInstanceState.getParcelable(Constants.PROMO_CATS);
                renderPromoDetail();
                return;
            }
        }

        int promoId = getArguments().getInt(Constants.PROMO_ID, -1);
        mPromoCategory = getArguments().getParcelable(Constants.PROMO_CATS);
        getPromoDetail(promoId);
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        int promoId = getArguments().getInt(Constants.PROMO_ID, -1);
        getPromoDetail(promoId);
    }

    private void getPromoDetail(int promoId) {
        if (promoId > -1) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
            showProgressView();
            bigBasketApiService.getPromoDetail(String.valueOf(promoId), new Callback<ApiResponse<PromoDetailApiResponseContent>>() {
                @Override
                public void success(ApiResponse<PromoDetailApiResponseContent> promoDetailApiResponseContentApiResponse, Response response) {
                    if (isSuspended()) return;
                    hideProgressView();
                    int status = promoDetailApiResponseContentApiResponse.status;
                    if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                            || status == ApiErrorCodes.INVALID_INPUT) {
                        showAlertDialogFinish(null, promoDetailApiResponseContentApiResponse.message);
                    } else if (status == 0) {
                        mPromoDetail = promoDetailApiResponseContentApiResponse.apiResponseContent.promoDetail;
                        if (mPromoDetail != null) {
                            renderPromoDetail();
                            setCartInfo(promoDetailApiResponseContentApiResponse.cartSummary);
                            updateUIForCartInfo();
                            trackEvent(TrackingAware.PROMO_DETAIL_SHOWN, null);
                        } else {
                            handler.sendEmptyMessage(promoDetailApiResponseContentApiResponse.status,
                                    promoDetailApiResponseContentApiResponse.message, true);
                        }
                    } //TODO Sid check if error handling needed.
                }

                @Override
                public void failure(RetrofitError error) {
                    if (isSuspended()) return;
                    hideProgressView();
                    handler.handleRetrofitError(error);
                }
            });
        }
    }

    public void renderPromoDetail() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        setTitle(mPromoDetail.getPromoName());
        LayoutInflater layoutInflater = (LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View base = layoutInflater.inflate(R.layout.uiv3_promo_detail, contentView, false);
        renderPromoList(base);

        LinearLayout layoutMain = (LinearLayout) base.findViewById(R.id.layoutMain);

        Resources resources = getResources();

        TextView txtPromoName = (TextView) base.findViewById(R.id.txtPromoName);
        txtPromoName.setTypeface(faceRobotoRegular);
        txtPromoName.setText(mPromoDetail.getPromoName());

        TextView txtPromoDesc = (TextView) base.findViewById(R.id.txtPromoDesc);
        txtPromoDesc.setTypeface(faceRobotoRegular);
        txtPromoDesc.setText(mPromoDetail.getPromoDesc());

        ImageView imageView = (ImageView) base.findViewById(R.id.imgPromoIcon);
        UIUtil.displayAsyncImage(imageView, mPromoDetail.getPromoIcon());

        TextView txtPromoTermsAndCond2 = (TextView) base.findViewById(R.id.txtPromoTermsAndCond2);
        txtPromoTermsAndCond2.setTypeface(faceRobotoRegular);

        String condition1, condition2;
        if (mPromoDetail.getTermsAndCond().contains("Limit")) {
            String condition = mPromoDetail.getTermsAndCond();
            int idx = condition.indexOf("Limit");
            condition1 = condition.substring(0, idx);
            condition2 = condition.substring(idx);
            txtPromoTermsAndCond2.setVisibility(View.VISIBLE);
            Spannable spannableLimit = new SpannableString(condition2);
            spannableLimit.setSpan(new CustomTypefaceSpan("", faceRobotoRegular), 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            txtPromoTermsAndCond2.setText(spannableLimit);
        } else {
            condition1 = mPromoDetail.getTermsAndCond();
        }

        TextView txtPromoTermsAndCond = (TextView) base.findViewById(R.id.txtPromoTermsAndCond1);
        txtPromoTermsAndCond.setTypeface(faceRobotoRegular);
        txtPromoTermsAndCond.setText(condition1);

        TextView txtPromoInfoMsg = (TextView) base.findViewById(R.id.txtPromoInfoMsg);
        String promoInfoMsg = mPromoDetail.getPromoRedemptionInfo().getPromoMessage().getPromoMessage();
        txtPromoInfoMsg.setTypeface(faceRobotoRegular);
        txtPromoInfoMsg.setText(promoInfoMsg);

        LinearLayout layoutCriteriaMsg = (LinearLayout) base.findViewById(R.id.layoutCriteriaMsg);
        List<Spannable> criteriaSpannable = mPromoDetail.getPromoRedemptionInfo().getPromoMessage().getCriteriaMsgSpannableList();
        if (criteriaSpannable != null && criteriaSpannable.size() > 0) {
            layoutCriteriaMsg.removeAllViews();
            layoutCriteriaMsg.setVisibility(View.VISIBLE);
            for (Spannable spannable : criteriaSpannable) {
                TextView txtPromoMsg = UIUtil.getPromoMsgTextView(getActivity());
                txtPromoMsg.setTypeface(faceRobotoRegular);
                txtPromoMsg.setText(spannable);
                layoutCriteriaMsg.addView(txtPromoMsg);
            }
        } else {
            layoutCriteriaMsg.setVisibility(View.GONE);
        }

        TextView txtNumCompletedOffer = (TextView) base.findViewById(R.id.txtNumCompletedOffer);
        txtNumCompletedOffer.setTypeface(faceRobotoRegular);
        txtNumCompletedOffer.setText(PromoDetail.getNumCompletedInBasketSpannable(
                resources.getColor(R.color.promo_txt_green_color), mPromoDetail.getNumPromoCompletedInBasket()));

        TextView txtSaving = (TextView) base.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);
        String promoSavingFormatted = UIUtil.formatAsMoney(mPromoDetail.getSaving());
        txtSaving.setText(PromoDetail.getSavingSpannable(resources.getColor(R.color.promo_txt_green_color),
                promoSavingFormatted, faceRupee));

        String promoType = mPromoDetail.getPromoType();

        if (promoType.equalsIgnoreCase(Promo.PromoType.FREE)) {
            boolean isRedeemed = true;
            for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, layoutMain);
                layoutMain.addView(promoSetLayout);
                if (!(promoSet.getPromoCriteriaVal() <= 0 ||
                        promoSet.getPromoCriteriaVal() <= promoSet.getValueInBasket())) {
                    isRedeemed = false;
                }
            }
            View freePromoView = getFreePromoMsgView(isRedeemed);
            layoutMain.addView(freePromoView);
            if (mPromoDetail.getFreeProducts() != null && mPromoDetail.getFreeProducts().length() > 0) {
                addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
            }

        } else if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO)) {
            View fixedFreeComboView = getPromoSetBar("View All Combo Products",
                    mPromoDetail, layoutMain);
            layoutMain.addView(fixedFreeComboView);
            View freePromoView = getFreePromoMsgView(false);
            layoutMain.addView(freePromoView);
            addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {
            View fixedComboView = getPromoSetBar("View All Combo Products",
                    mPromoDetail, layoutMain);
            layoutMain.addView(fixedComboView);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNT_PRICE)
                || promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_DISCOUNTED_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNTED_BIN)
                || promoType.equalsIgnoreCase(Promo.PromoType.CUSTOMIZED_COMBO)) {
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, layoutMain);
                    layoutMain.addView(promoSetLayout);
                }
            }
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_FREE_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.MEMBER_REFERRAL)) {
            boolean isRedeemed = true;
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, layoutMain);
                    layoutMain.addView(promoSetLayout);
                    if (!(promoSet.getPromoCriteriaVal() <= 0 ||
                            promoSet.getPromoCriteriaVal() <= promoSet.getValueInBasket())) {
                        isRedeemed = false;
                    }
                }
            }
            View freePromoView = getFreePromoMsgView(isRedeemed);
            layoutMain.addView(freePromoView);
            if (mPromoDetail.getFreeProducts() != null && mPromoDetail.getFreeProducts().length() > 0) {
                addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
            }
        }

        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void addFreeProductToLayout(PromoDetail promoDetail, LinearLayout view,
                                        LayoutInflater layoutInflater) {
        if (getActivity() == null || getCurrentActivity() == null) return;
        String freeProductStr = promoDetail.getFreeProducts();
        ArrayList<Product> freeProducts = ParserUtil.parseProductList(freeProductStr);
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(false)
                .setShowShopListDeleteBtn(false)
                .build();
        for (Product freeProduct : freeProducts) {
            View base = layoutInflater.inflate(R.layout.uiv3_product_row, view, false);
            LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            productRowParams.setMargins(8, 8, 8, 0);

            ProductView.setProductView(new ProductViewHolder(base), freeProduct, promoDetail.getBaseImgUrl(),
                    new ProductDetailOnClickListener(freeProduct.getSku(), this), productViewDisplayDataHolder,
                    false, null, getNavigationCtx());
            base.setLayoutParams(productRowParams);
            view.addView(base);
        }
        trackEvent(TrackingAware.PROMO_SET_PRODUCTS_SHOWN, null);
    }

    private View getPromoSetBar(String text, PromoDetail promoDetail, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, parent, false);
        TextView txtSetName = (TextView) base.findViewById(R.id.txtSetName);
        txtSetName.setTypeface(faceRobotoRegular);
        txtSetName.setText(text);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        int margin = 20;
        layoutParams.setMargins(margin, margin, margin, margin);
        txtSetName.setLayoutParams(layoutParams);
        TextView txtValNeeded = (TextView) base.findViewById(R.id.txtValueNeed);
        txtValNeeded.setVisibility(View.GONE);
        TextView txtValInBasket = (TextView) base.findViewById(R.id.txtValueInBasket);
        txtValInBasket.setTypeface(faceRobotoRegular);
        txtValInBasket.setVisibility(View.GONE);
        base.setOnClickListener(new PromoSetActivityHandler(promoDetail));
        return base;
    }

    private View getFreePromoMsgView(boolean isRedeemed) {
        TextView txtDescription = new TextView(getActivity());
        txtDescription.setTextSize(14);
        txtDescription.setTextColor(getResources().getColor(R.color.active_order_red_color));
        int fourDp = 4;
        txtDescription.setPadding(fourDp, 0, fourDp, 0);
        LinearLayout.LayoutParams txtDescParams = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtDescription.setText(getResources().getString(isRedeemed ? R.string.free_promo_redeemed_txt : R.string.free_promo_txt));
        txtDescription.setTypeface(faceRobotoRegular);
        txtDescription.setLayoutParams(txtDescParams);
        txtDescription.setGravity(Gravity.CENTER_HORIZONTAL);
        return txtDescription;
    }

    private View getPromoSetView(PromoSet promoSet, PromoDetail promoDetail, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, parent, false);
        base.setOnClickListener(new PromoSetActivityHandler(promoDetail, promoSet));

        TextView txtSetName = (TextView) base.findViewById(R.id.txtSetName);
        txtSetName.setTypeface(faceRobotoRegular);
        txtSetName.setText(promoSet.getName());

        TextView txtValNeeded = (TextView) base.findViewById(R.id.txtValueNeed);
        txtValNeeded.setTypeface(faceRobotoRegular);

        TextView txtValInBasket = (TextView) base.findViewById(R.id.txtValueInBasket);
        txtValInBasket.setTypeface(faceRobotoRegular);

        boolean isRedeemed = promoSet.getPromoCriteriaVal() <= 0 ||
                promoSet.getPromoCriteriaVal() <= promoSet.getValueInBasket();

        switch (promoSet.getSetType()) {
            case Constants.QUANTITY:
                String valInBasket = promoSet.getValueInBasket() + " in basket";
                txtValInBasket.setText(valInBasket);
                if (!isRedeemed && promoSet.getValType().equalsIgnoreCase(PromoSet.CRITERIA)) {
                    String moreQtyNeeded = String.valueOf(Math.abs(promoSet.getPromoCriteriaVal() - promoSet.getValueInBasket()));
                    txtValNeeded.setVisibility(View.VISIBLE);
                    txtValNeeded.setText(moreQtyNeeded +
                            " more needed for the promo");
                }
                break;
            case Constants.AMOUNT:
                String prefix = "`";
                valInBasket = prefix + promoSet.getValueInBasket() + " in basket";
                Spannable valInBasketSpan = new SpannableString(valInBasket);
                valInBasketSpan.setSpan(new CustomTypefaceSpan("", faceRupee),
                        0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtValInBasket.setText(valInBasketSpan);
                if (!isRedeemed && promoSet.getValType().equalsIgnoreCase(PromoSet.CRITERIA)) {
                    String moreAmountNeeded = String.valueOf(Math.abs(promoSet.getPromoCriteriaVal() - promoSet.getValueInBasket()));
                    String txt = "`" + moreAmountNeeded
                            + " more needed for the promo";
                    Spannable spannable = new SpannableString(txt);
                    spannable.setSpan(new CustomTypefaceSpan("", faceRupee),
                            0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtValNeeded.setVisibility(View.VISIBLE);
                    txtValNeeded.setText(spannable);
                }
                break;
        }

        txtValInBasket.setTextColor(getResources().getColor(isRedeemed ?
                R.color.green_color : R.color.dark_black));

        if (isRedeemed) {
            txtValNeeded.setVisibility(View.INVISIBLE);
            ImageView imgTick = (ImageView) base.findViewById(R.id.imgTick);
            imgTick.setVisibility(View.VISIBLE);
        } else if (promoSet.getValType().equalsIgnoreCase(PromoSet.PROMO_PRODUCT)) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    16, 16);
            ImageView imgTick = (ImageView) base.findViewById(R.id.imgTick);
            imgTick.setLayoutParams(layoutParams);
            imgTick.setImageResource(R.drawable.promo_flash_icon);
            imgTick.setVisibility(View.VISIBLE);
        }
        return base;
    }

    private class PromoSetActivityHandler implements View.OnClickListener {

        private PromoDetail promoDetail;
        private PromoSet promoSet;

        public PromoSetActivityHandler(PromoDetail promoDetail) {
            this.promoDetail = promoDetail;
        }

        public PromoSetActivityHandler(PromoDetail promoDetail, PromoSet promoSet) {
            this.promoDetail = promoDetail;
            this.promoSet = promoSet;
        }

        public void onClick(View v) {
            PromoSetProductsFragment promoSetProductsFragment = new PromoSetProductsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.PROMO_ID, promoDetail.getId());
            bundle.putString(Constants.PROMO_TYPE, promoDetail.getPromoType());
            bundle.putString(Constants.BASE_IMG_URL, promoDetail.getBaseImgUrl());
            bundle.putString(Constants.PROMO_NAME, promoDetail.getPromoName());
            String productListStr = null;
            if (!promoDetail.getPromoType().equalsIgnoreCase(Promo.PromoType.FREE)) {
                if (promoDetail.getPromoType().equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO))
                    productListStr = promoDetail.getFixedComboProducts();
                else {
                    if (promoDetail.getFreeProducts() != null) {
                        productListStr = promoDetail.getFreeProducts();
                    } else if (promoDetail.getFixedComboProducts() != null) {
                        productListStr = promoDetail.getFixedComboProducts();
                    }
                }
            }
            bundle.putString(Constants.PRODUCT_LIST, productListStr);
            bundle.putDouble(Constants.SAVING, promoDetail.getSaving());
            bundle.putString(Constants.PROMO_NAME, promoDetail.getPromoName());
            bundle.putString(Constants.INFO_MESSAGE, promoDetail.getPromoRedemptionInfo().getPromoMessage().getPromoMessage());
            bundle.putStringArrayList(Constants.CRITERIA_MSGS, promoDetail.getPromoRedemptionInfo().getPromoMessage().getCriteriaMessages());
            bundle.putInt(Constants.NUM_IN_BASKET, promoDetail.getNumPromoCompletedInBasket());
            if (promoSet != null) {
                bundle.putInt(Constants.SET_ID, promoSet.getSetId());
                bundle.putString(Constants.NAME, promoSet.getName());
            }
            promoSetProductsFragment.setArguments(bundle);
            changeFragment(promoSetProductsFragment);
        }
    }

    private void renderPromoList(View base) {
        if (mPromoCategory == null || mPromoCategory.getPromos() == null ||
                mPromoCategory.getPromos().size() == 0) return;
        ListView lstPromoNames = (ListView) base.findViewById(R.id.lstPromoNames);
        if (lstPromoNames == null) return;
        if (mPromoCategory.getPromos().size() <= 1) {
            View layoutPromoNameListContainer = base.findViewById(R.id.layoutPromoNameListContainer);
            View viewPromoDetailPageSeparator = base.findViewById(R.id.viewPromoDetailPageSeparator);
            layoutPromoNameListContainer.setVisibility(View.GONE);
            viewPromoDetailPageSeparator.setVisibility(View.GONE);
        }
        TextView txtPromoCategoryName = (TextView) base.findViewById(R.id.txtHeaderMsg);
        txtPromoCategoryName.setTypeface(faceRobotoRegular);
        txtPromoCategoryName.setText(mPromoCategory.getName());
        txtPromoCategoryName.setTextColor(getActivity().getResources().getColor(R.color.uiv3_primary_text_color));
        txtPromoCategoryName.setTextSize(getActivity().getResources().getDimension(R.dimen.primary_text_size));
        PromoNameListAdapter promoNameListAdapter = new PromoNameListAdapter();
        lstPromoNames.setAdapter(promoNameListAdapter);
        lstPromoNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getPromoDetail(mPromoCategory.getPromos().get(position).getId());
            }
        });
    }

    private class PromoNameListAdapter extends BaseAdapter {

        private int highlightedColor;
        private int regularColor;
        private float textSize;

        @Override
        public int getCount() {
            return mPromoCategory.getPromos().size();
        }

        @Override
        public Object getItem(int position) {
            return mPromoCategory.getPromos().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public PromoNameListAdapter() {
            regularColor = getActivity().getResources().getColor(R.color.uiv3_primary_text_color);
            highlightedColor = getActivity().getResources().getColor(R.color.uiv3_link_color);
            textSize = getActivity().getResources().getDimension(R.dimen.primary_text_size);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Promo promo = (Promo) getItem(position);
            TextView txtPromoName = null;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.uiv3_list_title, parent, false);
                txtPromoName = (TextView) row.findViewById(R.id.txtHeaderMsg);
                txtPromoName.setTypeface(faceRobotoRegular);
                txtPromoName.setTextSize(textSize);
            }
            if (txtPromoName == null) {
                txtPromoName = (TextView) row.findViewById(R.id.txtHeaderMsg);
            }
            txtPromoName.setText("- " + promo.getPromoName());
            if (mPromoDetail.getId() == promo.getId()) {
                txtPromoName.setTextColor(highlightedColor);
            } else {
                txtPromoName.setTextColor(regularColor);
            }
            return row;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPromoDetail != null) {
            outState.putParcelable(Constants.PROMO_DETAIL, mPromoDetail);
            outState.putParcelable(Constants.PROMO_CATS, mPromoCategory);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        String promoName = getArguments() != null ? getArguments().getString(Constants.PROMO_NAME) : null;
        return TextUtils.isEmpty(promoName) ? "Promotion Detail" : promoName;
    }

    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_PROMO_DETAIL;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PromoDetailFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PROMO_DETAIL_SCREEN;
    }
}