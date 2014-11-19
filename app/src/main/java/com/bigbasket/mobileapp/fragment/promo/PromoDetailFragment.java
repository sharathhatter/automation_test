package com.bigbasket.mobileapp.fragment.promo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
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
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.promo.PromoCategory;
import com.bigbasket.mobileapp.model.promo.PromoDetail;
import com.bigbasket.mobileapp.model.promo.PromoSet;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
            HashMap<String, String> promoDetailRequestParamMap = new HashMap<>();
            promoDetailRequestParamMap.put(Constants.PROMO_ID, String.valueOf(promoId));
            startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_PROMO_DETAIL,
                    promoDetailRequestParamMap, false, true, null);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.GET_PROMO_DETAIL)) {
            String responseString = httpOperationResult.getReponseString();
            JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            if (status == ExceptionUtil.PROMO_NOT_EXIST || status == ExceptionUtil.PROMO_NOT_ACTIVE
                    || status == ExceptionUtil.INVALID_INPUT) {
                String errMsg = jsonObject.get(Constants.MESSAGE).getAsString();
                // TODO : Improve error handling
                showErrorMsg(errMsg);
            } else if (status == 0) {
                mPromoDetail = ParserUtil.parsePromoDetail(jsonObject);
                if (mPromoDetail != null) {
                    renderPromoDetail();
                    JsonObject cartSummaryJsonObj = jsonObject.get(Constants.CART_SUMMARY).getAsJsonObject();
                    CartSummary cartSummary = ParserUtil.parseCartSummary(cartSummaryJsonObj);
                    setCartInfo(cartSummary);
                    updateUIForCartInfo();
                } else {
                    showErrorMsg("Server Error");
                }
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    public void renderPromoDetail() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater layoutInflater = (LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View base = layoutInflater.inflate(R.layout.uiv3_promo_detail, null);
        renderPromoList(base);

        LinearLayout layoutMain = (LinearLayout) base.findViewById(R.id.layoutMain);

        Resources resources = getResources();

        ImageLoader imageLoader = ImageLoader.getInstance();

        TextView txtPromoName = (TextView) base.findViewById(R.id.txtPromoName);
        txtPromoName.setTypeface(faceRobotoRegular);
        txtPromoName.setText(mPromoDetail.getPromoName());

        TextView txtPromoDesc = (TextView) base.findViewById(R.id.txtPromoDesc);
        txtPromoDesc.setTypeface(faceRobotoRegular);
        txtPromoDesc.setText(mPromoDetail.getPromoDesc());

        ImageView imageView = (ImageView) base.findViewById(R.id.imgPromoIcon);
        imageLoader.displayImage(mPromoDetail.getPromoIcon(), imageView);


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
                promoSavingFormatted));

        String promoType = mPromoDetail.getPromoType();

        if (promoType.equalsIgnoreCase(Promo.PromoType.FREE)) {
            boolean isRedeemed = true;
            for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                View promoSetLayout = getPromoSetView(promoSet, mPromoDetail);
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
                    mPromoDetail);
            layoutMain.addView(fixedFreeComboView);
            View freePromoView = getFreePromoMsgView(false);
            layoutMain.addView(freePromoView);
            addFreeProductToLayout(mPromoDetail, layoutMain, layoutInflater);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.FIXED_COMBO)) {
            View fixedComboView = getPromoSetBar("View All Combo Products",
                    mPromoDetail);
            layoutMain.addView(fixedComboView);
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNT_PRICE)
                || promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_DISCOUNTED_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.DISCOUNTED_BIN)
                || promoType.equalsIgnoreCase(Promo.PromoType.CUSTOMIZED_COMBO)) {
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail);
                    layoutMain.addView(promoSetLayout);
                }
            }
        } else if (promoType.equalsIgnoreCase(Promo.PromoType.MIN_ORDER_FREE_PRODUCT)
                || promoType.equalsIgnoreCase(Promo.PromoType.MEMBER_REFERRAL)) {
            boolean isRedeemed = true;
            if (mPromoDetail.getPromoRedemptionInfo() != null &&
                    mPromoDetail.getPromoRedemptionInfo().getPromoSets() != null) {
                for (PromoSet promoSet : mPromoDetail.getPromoRedemptionInfo().getPromoSets()) {
                    View promoSetLayout = getPromoSetView(promoSet, mPromoDetail);
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
        if (getActivity() == null || getBaseActivity() == null) return;
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
            View base = layoutInflater.inflate(R.layout.uiv3_stretched_product_row, null);
            LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            productRowParams.setMargins(8, 8, 8, 0);

            ProductView.setProductView(new ProductViewHolder(base), freeProduct, promoDetail.getBaseImgUrl(),
                    new ProductDetailOnClickListener(freeProduct.getSku(), this), productViewDisplayDataHolder,
                    getBaseActivity(), false, null);
            base.setLayoutParams(productRowParams);
            view.addView(base);
        }
    }

    private View getPromoSetBar(String text, PromoDetail promoDetail) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, null);
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

    private View getPromoSetView(PromoSet promoSet, PromoDetail promoDetail) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View base = layoutInflater.inflate(R.layout.promo_set_row, null);
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
            bundle.putString(Constants.PROMO_NAME, promoDetail.getPromoName());
            bundle.putDouble(Constants.SAVING, promoDetail.getSaving());
            bundle.putString(Constants.INFO_MESSAGE, promoDetail.getPromoRedemptionInfo().getPromoMessage().getPromoMessage());
            bundle.putStringArrayList(Constants.CRITERIA_MSGS, promoDetail.getPromoRedemptionInfo().getPromoMessage().getCriteriaMessages());
            bundle.putInt(Constants.NUM_IN_BASKET, promoDetail.getNumPromoCompletedInBasket());
            if (promoSet != null) {
                bundle.putInt(Constants.SET_ID, promoSet.getSetId());
            }
            promoSetProductsFragment.setArguments(bundle);
            changeFragment(promoSetProductsFragment);
        }
    }

    private void renderPromoList(View base) {
        if (mPromoCategory == null) return;
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
            regularColor = getActivity().getResources().getColor(R.color.uiv3_list_primary_text_color);
            highlightedColor = getActivity().getResources().getColor(R.color.uiv3_link_color);
            textSize = getActivity().getResources().getDimension(R.dimen.primary_text_size);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Promo promo = (Promo) getItem(position);
            TextView txtPromoName = null;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.uiv3_list_title, null);
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
        return "Promotion Detail";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PromoDetailFragment.class.getName();
    }
}