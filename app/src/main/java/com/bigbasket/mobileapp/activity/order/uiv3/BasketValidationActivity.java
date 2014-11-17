package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.bigbasket.mobileapp.handler.MessageHandler;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceItems;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;

public class BasketValidationActivity extends BackButtonActivity {

    private MarketPlace mMarketPlace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mMarketPlace = savedInstanceState.getParcelable(Constants.MARKET_PLACE_INTENT); // File IO, so not putting it inside above If as AND
            if (mMarketPlace != null) {
                renderBasketValidationErrors();
                return;
            }
        }
        mMarketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        if (mMarketPlace == null) {
            return;
        }
        renderBasketValidationErrors();
    }

    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        this.mMarketPlace = marketPlace;
        if (marketPlace.isRuleValidationError()) {
            renderBasketValidationErrors();
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()) {
            Handler handler = new MessageHandler(getCurrentActivity(), marketPlace);
            handler.sendEmptyMessage(MessageCode.GO_AGE_VALIDATION);
        } else {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
            new COReserveQuantityCheckTask(getCurrentActivity(), pharmaPrescriptionId).execute();
        }
    }

    private void renderBasketValidationErrors() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        if (!mMarketPlace.isRuleValidationError() || mMarketPlace.getMarketPlaceRuleValidators().size() == 0)
            return;
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mMarketPlace.getMarketPlaceRuleValidators().size(); i++) {
            final RelativeLayout marketPlaceBaseLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error, null);
            LinearLayout addRemoveItemsLinearLayout = (LinearLayout) marketPlaceBaseLayout.findViewById(R.id.addRemoveItemsLinearLayout);
            contentView.addView(marketPlaceBaseLayout);
            LinearLayout layoutRuleNameAndMsg = (LinearLayout) marketPlaceBaseLayout.findViewById(R.id.layoutRuleNameAndMsg);
            layoutRuleNameAndMsg.setVisibility(View.VISIBLE);
            RelativeLayout layoutRuleTotalPriceAndWeight = (RelativeLayout) marketPlaceBaseLayout.findViewById(R.id.layoutRuleTotalPriceAndWeight);
            layoutRuleTotalPriceAndWeight.setVisibility(View.VISIBLE);

            TextView txtRuleName = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleName);
            TextView txtRuleDesc = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleDesc);

            txtRuleName.setText(mMarketPlace.getMarketPlaceRuleValidators().get(i).getRuleName());
            txtRuleDesc.setText(mMarketPlace.getMarketPlaceRuleValidators().get(i).getRuleDesc());

            String label = "";
            if (mMarketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel().equalsIgnoreCase("volume")) {
                label = label + "lt";
            } else if (mMarketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel().equalsIgnoreCase("weight")) {
                label = label + "Kg";
            }
            TextView txtRuleTotalWeight = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleTotalWeight);
            txtRuleTotalWeight.setText(getString(R.string.ruleTotal) + " " +
                    mMarketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
                    getDecimalAmount(mMarketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalQty()) + " " + label);


            TextView txtRuleTotalPriceValue = (TextView) marketPlaceBaseLayout.findViewById(R.id.txtRuleTotalPriceValue);
            if (!TextUtils.isEmpty(String.valueOf(mMarketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()))) {
                String prefix = " `";
                String mrpStr = getDecimalAmount(mMarketPlace.getMarketPlaceRuleValidators().get(i).getRuleTotalPrice()) + " ";
                int prefixLen = prefix.length();
                SpannableString spannableMrp = new SpannableString(prefix + mrpStr);
                spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                        prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtRuleTotalPriceValue.setText(spannableMrp);
            }

            for (int j = 0; j < mMarketPlace.getMarketPlaceRuleValidators().get(i).getItems().size(); j++) {
                final MarketPlaceItems marketPlaceItems = mMarketPlace.getMarketPlaceRuleValidators().get(i).getItems().get(j);
                RelativeLayout addRemoveLinearLayout = (RelativeLayout) inflater.inflate(R.layout.uiv3_basket_validation_error_products, null);
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
                        mMarketPlace.getMarketPlaceRuleValidators().get(i).getWeightLabel() + ": " +
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
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        MobileApiUrl.getBaseAPIUrl() + Constants.CART_DEC, BasketOperation.DELETE, marketPlaceItems.getSku(),
                                        null, null, null, null, null);
                                basketOperationTask.execute();
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
                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        MobileApiUrl.getBaseAPIUrl() + Constants.CART_INC, BasketOperation.ADD, marketPlaceItems.getSku(),
                                        null, null, null, null, null);
                                basketOperationTask.execute();
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

                                BasketOperationTask<BaseActivity> basketOperationTask = new BasketOperationTask<>(getCurrentActivity(),
                                        MobileApiUrl.getBaseAPIUrl() + Constants.CART_SET_ITEM, BasketOperation.EMPTY, marketPlaceItems.getSku(),
                                        null, null, null, null, null, "0");
                                basketOperationTask.execute();
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
        if (mMarketPlace != null) {
            outState.putParcelable(Constants.MARKET_PLACE_INTENT, mMarketPlace);
        }
        super.onSaveInstanceState(outState);
    }
}
