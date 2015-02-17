package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceItems;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class BasketValidationActivity extends BackButtonActivity {

    private MarketPlace marketPlace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            marketPlace = savedInstanceState.getParcelable(Constants.MARKET_PLACE_INTENT); // File IO, so not putting it inside above If as AND
            if (marketPlace != null) {
                renderBasketValidationErrors();
                return;
            }
        }
        marketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        if (marketPlace == null) {
            return;
        }
        renderBasketValidationErrors();
        trackEvent(TrackingAware.PRE_CHECKOUT_CWR_APPICABLE, null);
    }

    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        this.marketPlace = marketPlace;
        if (marketPlace.isRuleValidationError()) {
            renderBasketValidationErrors();
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()) {
            BigBasketMessageHandler handler = new BigBasketMessageHandler<>(this, marketPlace);
            handler.sendEmptyMessage(NavigationCodes.GO_AGE_VALIDATION, null);
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

    private void renderBasketValidationErrors() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        if (!marketPlace.isRuleValidationError() || marketPlace.getMarketPlaceRuleValidators().size() == 0)
            return;
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < marketPlace.getMarketPlaceRuleValidators().size(); i++) {
            final RelativeLayout marketPlaceBaseLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error, contentView, false);
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
            TextView txtRuleTotalWeight = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleTotalWeight);
            txtRuleTotalWeight.setText(getString(R.string.ruleTotal) + " " +
                    marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
                    getDecimalAmount(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalQty()) + " " + label);


            TextView txtRuleTotalPriceValue = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleTotalPriceValue);
            if (!TextUtils.isEmpty(String.valueOf(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()))) {
                String prefix = " `";
                String mrpStr = getDecimalAmount(marketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()) + " ";
                int prefixLen = prefix.length();
                SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
                spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                        prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtRuleTotalPriceValue.setText(spannableMrp);
            }

            for (int j = 0; j < marketPlace.getMarketPlaceRuleValidators().get(i).getItems().size(); j++) {
                final MarketPlaceItems marketPlaceItems = marketPlace.getMarketPlaceRuleValidators().get(i).getItems().get(j);
                RelativeLayout addRemoveLinearLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error_products, addRemoveItemsLinearLayout, false);
                TextView txtIndex = (TextView) addRemoveLinearLayout.findViewById(R.id.txtIndex);
                TextView txtItemInBasketAndProductDesc = (TextView) addRemoveLinearLayout.findViewById(R.id.txtItemInBasketAndProductDesc);
                TextView txtTotalQty = (TextView) addRemoveLinearLayout.findViewById(R.id.txtTotalQty);
                TextView txtTotalPrice = (TextView) addRemoveLinearLayout.findViewById(R.id.txtTotalPrice);
                final ImageView imgRemove = (ImageView) addRemoveLinearLayout.findViewById(R.id.imgRemove);
                marketPlaceItems.setRuleValidationArrayIndex(i);
                marketPlaceItems.setItemIndex(j);
                final ImageView imgAdd = (ImageView) addRemoveLinearLayout.findViewById(R.id.imgAdd);
                final ImageView imgDec = (ImageView) addRemoveLinearLayout.findViewById(R.id.imgDec);

                double itemTotalVolume = marketPlaceItems.getTotalQty();
                txtIndex.setText(String.valueOf(j + 1) + ".");
                txtIndex.setTypeface(faceRobotoRegular);

                txtItemInBasketAndProductDesc.setText(marketPlaceItems.getItemInCart() + " quantity of " + marketPlaceItems.getDesc());
                txtItemInBasketAndProductDesc.setTypeface(faceRobotoRegular);
                txtTotalQty.setText(getString(R.string.ruleTotal) + " " +
                        marketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
                        getDecimalAmount(itemTotalVolume) + " " + label);

                if (!TextUtils.isEmpty(String.valueOf(marketPlaceItems.getSalePrice()))) {
                    String prefix = "Total Price: `";
                    String mrpStr = getDecimalAmount(marketPlaceItems.getSalePrice()) + " ";
                    int prefixLen = prefix.length();
                    SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
                    spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                            prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtTotalPrice.setText(spannableMrp);
                }


                addRemoveItemsLinearLayout.addView(addRemoveLinearLayout);


                if (imgRemove != null && imgAdd != null && imgDec != null) {
                    imgDec.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                                Product product = new Product(marketPlaceItems.getProductBrand(),
                                        marketPlaceItems.getDesc(), marketPlaceItems.getSku(),
                                        marketPlaceItems.getTopLevelCategoryName(), marketPlaceItems.getTopLevelCategoryName());
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.DEC, product,
                                        null, null, null, null, null, TrackingAware.BASKET_DECREMENT, TrackEventkeys.MARKET_QC);
                                basketOperationTask.startTask();
                            } else {
                                Toast toast = Toast.makeText(getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                toast.show();
                            }
                        }
                    });

                    imgAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                                Product product = new Product(marketPlaceItems.getProductBrand(),
                                        marketPlaceItems.getDesc(), marketPlaceItems.getSku(),
                                        marketPlaceItems.getTopLevelCategoryName(), marketPlaceItems.getProductCategoryName());
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.INC, product,
                                        null, null, null, null, null, TrackingAware.BASKET_INCREMENT, TrackEventkeys.MARKET_QC);
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
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        BasketOperation.EMPTY, product,
                                        null, null, null, null, null, "0", TrackingAware.BASKET_REMOVE, TrackEventkeys.MARKET_QC);
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (marketPlace != null) {
            outState.putParcelable(Constants.MARKET_PLACE_INTENT, marketPlace);
        }
        super.onSaveInstanceState(outState);
    }
}
