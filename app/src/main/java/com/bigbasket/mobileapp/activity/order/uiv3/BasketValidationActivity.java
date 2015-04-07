package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceItems;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

public class BasketValidationActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
    }

    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        if (marketPlace == null) return;
        if (marketPlace.isRuleValidationError()) {
            renderBasketValidationErrors(marketPlace);
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()
                || marketPlace.hasTermsAndCond()) {
            Intent intent = new Intent(getCurrentActivity(), AgeValidationActivity.class);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            getCurrentActivity().finish();// don't remove it, fix for back button
        } else {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
            new COReserveQuantityCheckTask<>(getCurrentActivity(), pharmaPrescriptionId).startTask();
        }
    }


    @Override
    public void onCOReserveQuantityCheck() {
        Intent intent = new Intent(getCurrentActivity(), CheckoutQCActivity.class);
        intent.putExtra(Constants.CO_RESERVE_QTY_DATA, coReserveQuantity);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        getCurrentActivity().finish();// don't remove it, fix for back button
    }

    private void renderBasketValidationErrors(MarketPlace marketPlace) {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        base.removeAllViews();
        LinearLayout contentView = new LinearLayout(this);
        contentView.removeAllViews();
        contentView.setOrientation(LinearLayout.VERTICAL);
        if (!marketPlace.isRuleValidationError() || marketPlace.getMarketPlaceRuleValidators() == null ||
                marketPlace.getMarketPlaceRuleValidators().size() == 0)
            return;
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < marketPlace.getMarketPlaceRuleValidators().size(); i++) {
            final RelativeLayout marketPlaceBaseLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error,
                    contentView, false);

            LinearLayout addRemoveItemsLinearLayout = (LinearLayout) marketPlaceBaseLayout.findViewById(R.id.addRemoveItemsLinearLayout);
            contentView.addView(marketPlaceBaseLayout);
            LinearLayout layoutRuleNameAndMsg = (LinearLayout) marketPlaceBaseLayout.findViewById(R.id.layoutRuleNameAndMsg);
            layoutRuleNameAndMsg.setVisibility(View.VISIBLE);
            RelativeLayout layoutRuleTotalPriceAndWeight = (RelativeLayout) marketPlaceBaseLayout.findViewById(R.id.layoutRuleTotalPriceAndWeight);
            layoutRuleTotalPriceAndWeight.setVisibility(View.VISIBLE);

            TextView txtRuleName = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleName);
            TextView txtRuleDesc = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleDesc);

            txtRuleName.setText(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleName());
            txtRuleDesc.setText(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleDesc());

            String label = "";
            if (marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel().equalsIgnoreCase("volume")) {
                label = label + "lt";
            } else if (marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel().equalsIgnoreCase("weight")) {
                label = label + "Kg";
            }
            TextView txtRuleTotalWeight = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtTopCategory);
            txtRuleTotalWeight.setTypeface(faceRobotoRegular);
            txtRuleTotalWeight.setText(getString(R.string.ruleTotal) + " " +
                    marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
                    UIUtil.formatAsMoney(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalQty()) + " " + label);

            TextView txtRuleTotalPriceValue = (TextView) marketPlaceBaseLayout.findViewById(R.id.topCatTotal);
            txtRuleTotalPriceValue.setTypeface(faceRobotoRegular);
            TextView txtTotalPriceValue = (TextView) marketPlaceBaseLayout.findViewById(R.id.topCatTotalItems);
            txtTotalPriceValue.setText(getString(R.string.ruleTotalPrice));
            txtTotalPriceValue.setTypeface(faceRobotoRegular);

            if (!TextUtils.isEmpty(String.valueOf(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()))) {
                String prefix = " `";
                String mrpStr = UIUtil.formatAsMoney(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()) + " ";
                int prefixLen = prefix.length();
                SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
                spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                        prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtRuleTotalPriceValue.setText(spannableMrp);
            }

            for (int j = 0; j < marketPlace.getMarketPlaceRuleValidators().get(i).getItems().size(); j++) {
                final MarketPlaceItems marketPlaceItems = marketPlace.getMarketPlaceRuleValidators().get(i).getItems().get(j);
                RelativeLayout addRemoveLinearLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error_products,
                        addRemoveItemsLinearLayout, false);
                TextView txtIndex = (TextView) addRemoveLinearLayout.findViewById(R.id.txtIndex);
                TextView txtItemInBasketAndProductDesc = (TextView) addRemoveLinearLayout.findViewById(R.id.txtItemInBasketAndProductDesc);
                final TextView txtInBasket = (TextView) addRemoveLinearLayout.findViewById(R.id.txtInBasket);
                TextView txtTotalPrice = (TextView) addRemoveLinearLayout.findViewById(R.id.txtTotalPrice);
                final ImageView imgRemove = (ImageView) addRemoveLinearLayout.findViewById(R.id.imgRemove);
                marketPlaceItems.setRuleValidationArrayIndex(i);
                marketPlaceItems.setItemIndex(j);
                final TextView txtIncBasketQty = (TextView) addRemoveLinearLayout.findViewById(R.id.txtIncBasketQty);
                final TextView txtDecBasketQty = (TextView) addRemoveLinearLayout.findViewById(R.id.txtDecBasketQty);

                double itemTotalVolume = marketPlaceItems.getTotalQty();
                txtIndex.setText(String.valueOf(j + 1) + ".");
                txtIndex.setTypeface(faceRobotoRegular);

                txtItemInBasketAndProductDesc.setText(marketPlaceItems.getItemInCart() + " quantity of " + marketPlaceItems.getDesc());
                txtItemInBasketAndProductDesc.setTypeface(faceRobotoRegular);
                txtInBasket.setText(UIUtil.formatAsMoney(itemTotalVolume) + " " + label + " in ");
//                txtInBasket.setText(getString(R.string.ruleTotal) + " " +
//                        marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
//                        getDecimalAmount(itemTotalVolume) + " " + label);

                if (!TextUtils.isEmpty(String.valueOf(marketPlaceItems.getSalePrice()))) {
                    String prefix = "Total Price: `";
                    String mrpStr = UIUtil.formatAsMoney(marketPlaceItems.getSalePrice()) + " ";
                    int prefixLen = prefix.length();
                    SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
                    spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                            prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtTotalPrice.setText(spannableMrp);
                }


                addRemoveItemsLinearLayout.addView(addRemoveLinearLayout);


                if (imgRemove != null && txtIncBasketQty != null && txtDecBasketQty != null) {
                    txtDecBasketQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                                Product product = new Product(marketPlaceItems.getProductBrand(),
                                        marketPlaceItems.getDesc(), marketPlaceItems.getSku(),
                                        marketPlaceItems.getTopLevelCategoryName(), marketPlaceItems.getTopLevelCategoryName());
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.DEC, product,
                                        null, null, null, null, null, TrackingAware.BASKET_DECREMENT, getNavigationCtx(), null);
                                basketOperationTask.startTask();
                            } else {
                                Toast toast = Toast.makeText(getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            }
                        }
                    });

                    txtIncBasketQty.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                                Product product = new Product(marketPlaceItems.getProductBrand(),
                                        marketPlaceItems.getDesc(), marketPlaceItems.getSku(),
                                        marketPlaceItems.getTopLevelCategoryName(), marketPlaceItems.getProductCategoryName());
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.INC, product,
                                        null, null, null, null, null, TrackingAware.BASKET_INCREMENT, getNavigationCtx(), null);
                                basketOperationTask.startTask();
                            } else {
                                Toast toast = Toast.makeText(getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            }
                        }
                    });
                    imgRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                                Product product = new Product(marketPlaceItems.getProductBrand(),
                                        marketPlaceItems.getDesc(), marketPlaceItems.getSku(),
                                        marketPlaceItems.getTopLevelCategoryName(), marketPlaceItems.getProductCategoryName());
                                BasketOperationTask basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.EMPTY,
                                        product, txtInBasket, null, null, null, null, "0",
                                        TrackingAware.BASKET_REMOVE, getNavigationCtx(), null);
                                basketOperationTask.startTask();
                            } else {
                                Toast toast = Toast.makeText(getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            }
                        }
                    });
                }
            }
        }
        base.addView(contentView);

        trackEvent(TrackingAware.PRE_CHECKOUT_CWR_APPICABLE, null);
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView, View viewDecQty,
                                                    View viewIncQty, Button btnAddToBasket, EditText editTextQty,
                                                    Product product, String qty, @Nullable View productView) {
        super.updateUIAfterBasketOperationSuccess(basketOperation,
                basketCountTextView, viewDecQty, viewIncQty, btnAddToBasket, editTextQty, product, qty,
                productView);
        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
    }

    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_MARKET_PLACE_QC;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.MARKET_PLACE_QTY_CHECK_SCREEN;
    }
}
