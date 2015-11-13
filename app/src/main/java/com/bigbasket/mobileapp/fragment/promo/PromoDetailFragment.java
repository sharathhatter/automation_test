package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PromoDetailApiResponseContent;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
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
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Call;


public class PromoDetailFragment extends BaseFragment {

    private PromoDetail mPromoDetail;
    private PromoCategory mPromoCategory;
    private HashMap<String, Integer> cartInfo;

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
        mPromoCategory = getArguments().getParcelable(Constants.PROMO_CATS);
        setNextScreenNavigationContext(TrackEventkeys.NC_PROMO_DETAIL);
    }

    @Override
    public void onResume() {
        super.onResume();
        int promoId = getArguments().getInt(Constants.PROMO_ID, -1);
        getPromoDetail(promoId);
    }


    private void getPromoDetail(int promoId) {
        if (promoId > -1) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
            showProgressView();
            Call<ApiResponse<PromoDetailApiResponseContent>> call = bigBasketApiService.getPromoDetail(String.valueOf(promoId));
            call.enqueue(new BBNetworkCallback<ApiResponse<PromoDetailApiResponseContent>>(this, true) {
                @Override
                public void onSuccess(ApiResponse<PromoDetailApiResponseContent> promoDetailApiResponseContentApiResponse) {
                    int status = promoDetailApiResponseContentApiResponse.status;
                    if (status == ApiErrorCodes.PROMO_NOT_EXIST || status == ApiErrorCodes.PROMO_NOT_ACTIVE
                            || status == ApiErrorCodes.INVALID_INPUT) {
                        showAlertDialogFinish(null, promoDetailApiResponseContentApiResponse.message);
                    } else if (status == 0) {
                        mPromoDetail = promoDetailApiResponseContentApiResponse.apiResponseContent.promoDetail;
                        cartInfo = promoDetailApiResponseContentApiResponse.apiResponseContent.cartInfo;
                        if (mPromoDetail != null) {
                            renderPromoDetail();
                            setCartSummary(promoDetailApiResponseContentApiResponse.cartSummary);
                            updateUIForCartInfo();
                            HashMap<String, String> map = new HashMap<>();
                            if (!TextUtils.isEmpty(mPromoDetail.getPromoName())) {
                                map.put(TrackEventkeys.PROMO_NAME, mPromoDetail.getPromoName());
                            }
                            trackEvent(TrackingAware.PROMO_DETAIL_SHOWN, map);
                        } else {
                            handler.sendEmptyMessage(promoDetailApiResponseContentApiResponse.status,
                                    promoDetailApiResponseContentApiResponse.message, true);
                        }
                    }
                }

                @Override
                public boolean updateProgress() {
                    hideProgressView();
                    return true;
                }
            });
        }
    }

    public void renderPromoDetail() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
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
                View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, cartInfo, layoutMain);
                layoutMain.addView(promoSetLayout);
                if (!(promoSet.getPromoCriteriaVal() <= 0 ||
                        promoSet.getPromoCriteriaVal() <= promoSet.getValueInBasket())) {
                    isRedeemed = false;
                }
            }
            View freePromoView = getFreePromoMsgView(isRedeemed);
            layoutMain.addView(freePromoView);
            if (mPromoDetail.getFreeProducts() != null && mPromoDetail.getFreeProducts().size() > 0) {
                addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
            }

        } else if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO)) {
            View fixedFreeComboView = getPromoSetBar("View All Combo Products",
                    mPromoDetail, cartInfo, layoutMain);
            layoutMain.addView(fixedFreeComboView);
            View freePromoView = getFreePromoMsgView(false);
            layoutMain.addView(freePromoView);
            addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {
            View fixedComboView = getPromoSetBar("View All Combo Products",
                    mPromoDetail, cartInfo, layoutMain);
            layoutMain.addView(fixedComboView);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNT_PRICE)
                || promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_DISCOUNTED_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNTED_BIN)
                || promoType.equalsIgnoreCase(Promo.PromoType.CUSTOMIZED_COMBO)) {
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, cartInfo, layoutMain);
                    layoutMain.addView(promoSetLayout);
                }
            }
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_FREE_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.MEMBER_REFERRAL)) {
            boolean isRedeemed = true;
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail, cartInfo, layoutMain);
                    layoutMain.addView(promoSetLayout);
                    if (!(promoSet.getPromoCriteriaVal() <= 0 ||
                            promoSet.getPromoCriteriaVal() <= promoSet.getValueInBasket())) {
                        isRedeemed = false;
                    }
                }
            }
            View freePromoView = getFreePromoMsgView(isRedeemed);
            layoutMain.addView(freePromoView);
            if (mPromoDetail.getFreeProducts() != null && mPromoDetail.getFreeProducts().size() > 0) {
                addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
            }
        }

        contentView.removeAllViews();
        contentView.addView(base);
    }

    private void addFreeProductToLayout(PromoDetail promoDetail, LinearLayout view,
                                        LayoutInflater layoutInflater) {
        if (getActivity() == null || getCurrentActivity() == null) return;
        ArrayList<Product> freeProducts = promoDetail.getFreeProducts();
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setSansSerifMediumTypeface(faceRobotoMedium)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(false)
                .setShowBasketBtn(false)
                .setShowShopListDeleteBtn(false)
                .showQtyInput(AuthParameters.getInstance(getActivity()).isKirana())
                .build();
        for (Product freeProduct : freeProducts) {
            View base = layoutInflater.inflate(R.layout.uiv3_product_row, view, false);
            base.findViewById(R.id.viewSeparator).setVisibility(View.GONE);
            LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            productRowParams.setMargins(8, 8, 8, 0);

            ProductView.setProductView(new ProductViewHolder(base, null), freeProduct, promoDetail.getBaseImgUrl(),
                    null, productViewDisplayDataHolder, false, getCurrentActivity(), getNextScreenNavigationContext(), null, "none",
                    null, null);
            base.setLayoutParams(productRowParams);
            view.addView(base);
        }
    }

    private View getPromoSetBar(String text, PromoDetail promoDetail, HashMap<String, Integer> cartInfo,
                                ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, parent, false);
        TextView txtSetName = (TextView) base.findViewById(R.id.txtSetName);
        txtSetName.setTypeface(faceRobotoRegular);
        txtSetName.setText(text);
        TextView txtValNeeded = (TextView) base.findViewById(R.id.txtValueNeed);
        txtValNeeded.setVisibility(View.GONE);
        TextView txtValInBasket = (TextView) base.findViewById(R.id.txtValueInBasket);
        txtValInBasket.setTypeface(faceRobotoRegular);
        txtValInBasket.setVisibility(View.GONE);
        base.setOnClickListener(new PromoSetActivityHandler(promoDetail, cartInfo));
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

    private View getPromoSetView(PromoSet promoSet, PromoDetail promoDetail,
                                 HashMap<String, Integer> cartInfo, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, parent, false);
        base.setOnClickListener(new PromoSetActivityHandler(promoDetail, cartInfo, promoSet));

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
                String valInBasket = UIUtil.formatAsMoney((double) promoSet.getValueInBasket()) + " in basket";
                txtValInBasket.setText(valInBasket);
                if (!isRedeemed && promoSet.getValType().equalsIgnoreCase(PromoSet.CRITERIA)) {
                    String moreQtyNeeded = UIUtil.roundOrInt(Math.abs(promoSet.getPromoCriteriaVal() - promoSet.getValueInBasket()));
                    txtValNeeded.setVisibility(View.VISIBLE);
                    txtValNeeded.setText(moreQtyNeeded +
                            " more needed for the promo");
                }
                break;
            case Constants.AMOUNT:
                String prefix = "`";
                valInBasket = prefix + UIUtil.formatAsMoney((double) promoSet.getValueInBasket()) + " in basket";
                Spannable valInBasketSpan = new SpannableString(valInBasket);
                valInBasketSpan.setSpan(new CustomTypefaceSpan("", faceRupee),
                        0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtValInBasket.setText(valInBasketSpan);
                if (!isRedeemed && promoSet.getValType().equalsIgnoreCase(PromoSet.CRITERIA)) {
                    String moreAmountNeeded = UIUtil.roundOrInt(Math.abs(promoSet.getPromoCriteriaVal() -
                            promoSet.getValueInBasket()));
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

        ImageView imgTick = (ImageView) base.findViewById(R.id.imgTick);
        if (isRedeemed) {
            txtValNeeded.setVisibility(View.GONE);
            imgTick.setVisibility(View.VISIBLE);
        } else if (promoSet.getValType().equalsIgnoreCase(PromoSet.PROMO_PRODUCT)) {
            imgTick.setImageResource(R.drawable.ic_star_outline_grey600_24dp);
            imgTick.setVisibility(View.VISIBLE);
        } else {
            imgTick.setVisibility(View.GONE);
        }
        return base;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPromoDetail != null) {
            outState.putParcelable(Constants.PROMO_DETAIL, mPromoDetail);
            outState.putParcelable(Constants.PROMO_CATS, mPromoCategory);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        String promoName = getArguments() != null ? getArguments().getString(Constants.PROMO_NAME) : null;
        return TextUtils.isEmpty(promoName) ? "Promotion Detail" : promoName;
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

    private class PromoSetActivityHandler implements View.OnClickListener {

        private PromoDetail promoDetail;
        private PromoSet promoSet;
        private HashMap<String, Integer> cartInfo;

        public PromoSetActivityHandler(PromoDetail promoDetail,
                                       HashMap<String, Integer> cartInfo) {
            this.promoDetail = promoDetail;
            this.cartInfo = cartInfo;
        }

        public PromoSetActivityHandler(PromoDetail promoDetail, HashMap<String, Integer> cartInfo,
                                       PromoSet promoSet) {
            this.promoDetail = promoDetail;
            this.promoSet = promoSet;
            this.cartInfo = cartInfo;
        }

        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), BackButtonActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_SET_PRODUCTS);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.PROMO_ID, promoDetail.getId());
            bundle.putString(Constants.PROMO_TYPE, promoDetail.getPromoType());
            bundle.putString(Constants.BASE_IMG_URL, promoDetail.getBaseImgUrl());
            bundle.putString(Constants.CART_INFO, new Gson().toJson(cartInfo));
            bundle.putString(Constants.PROMO_NAME, promoDetail.getPromoName());
            ArrayList<Product> products = null;
            if (!promoDetail.getPromoType().equalsIgnoreCase(Promo.PromoType.FREE)) {
                if (promoDetail.getPromoType().equalsIgnoreCase(Promo.PromoType.FIXED_FREE_COMBO))
                    products = promoDetail.getFixedComboProducts();
                else {
                    if (promoDetail.getFreeProducts() != null) {
                        products = promoDetail.getFreeProducts();
                    } else if (promoDetail.getFixedComboProducts() != null) {
                        products = promoDetail.getFixedComboProducts();
                    }
                }
            }
            bundle.putParcelableArrayList(Constants.PRODUCT_LIST, products);
            bundle.putDouble(Constants.SAVING, promoDetail.getSaving());
            bundle.putString(Constants.PROMO_NAME, promoDetail.getPromoName());
            bundle.putString(Constants.INFO_MESSAGE, promoDetail.getPromoRedemptionInfo().getPromoMessage().getPromoMessage());
            bundle.putStringArrayList(Constants.CRITERIA_MSGS, promoDetail.getPromoRedemptionInfo().getPromoMessage().getCriteriaMessages());
            bundle.putInt(Constants.NUM_IN_BASKET, promoDetail.getNumPromoCompletedInBasket());
            if (promoSet != null) {
                bundle.putInt(Constants.SET_ID, promoSet.getSetId());
                bundle.putString(Constants.NAME, promoSet.getName());
            }
            intent.putExtras(bundle);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }

    private class PromoNameListAdapter extends BaseAdapter {

        private int highlightedColor;
        private int regularColor;
        private float textSize;

        public PromoNameListAdapter() {
            regularColor = getActivity().getResources().getColor(R.color.uiv3_primary_text_color);
            highlightedColor = getActivity().getResources().getColor(R.color.uiv3_link_color);
            textSize = getActivity().getResources().getDimension(R.dimen.primary_text_size);
        }

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
}