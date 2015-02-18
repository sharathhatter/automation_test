package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.model.general.MessageParamInfo;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceAgeCheck;
import com.bigbasket.mobileapp.model.order.PharmaPrescriptionInfo;
import com.bigbasket.mobileapp.model.order.SavedPrescription;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.view.BBWebView;
import com.bigbasket.mobileapp.view.uiv3.TermAndConditionDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgeValidationActivity extends BackButtonActivity {
    private MarketPlace marketPlace;
    private HashMap<String, Boolean> hashMapRadioBtnAgeCheckNo = new HashMap<>();
    private boolean isPharmaRadioBtnNoSelected;
    private Button btnListFooter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        marketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        renderMarketPlaceValidationErrors();
        trackEvent(TrackingAware.PRE_CHECKOUT_AGE_LEGAL_SHOWN, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == Constants.PRESCRIPTION_UPLOADED)
            goToQCPage((COReserveQuantity) data.getParcelableExtra(Constants.CO_RESERVE_QTY_DATA));
        else if (resultCode == Constants.PRESCRIPTION_CHOSEN)
            goToQCPage((COReserveQuantity) data.getParcelableExtra(Constants.CO_RESERVE_QTY_DATA));
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToQCPage(COReserveQuantity coReserveQuantity1) {
        Intent intent = new Intent(getCurrentActivity(), CheckoutQCActivity.class);
        intent.putExtra(Constants.CO_RESERVE_QTY_DATA, coReserveQuantity1);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        getCurrentActivity().finish();// don't remove it, fix for back button
    }

    @Override
    public void onCOReserveQuantityCheck() {
        goToQCPage(getCOReserveQuantity());
    }

    //After bulk remove
    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        this.marketPlace = marketPlace;
        isPharmaRadioBtnNoSelected = false;
        hashMapRadioBtnAgeCheckNo.clear();
        if (!marketPlace.isPharamaPrescriptionNeeded() && !marketPlace.isAgeCheckRequired()
                && !marketPlace.hasTermsAndCond()) {
            proceedToQc();
        } else {
            renderMarketPlaceValidationErrors();
        }
    }

    private void proceedToQc() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
        new COReserveQuantityCheckTask<>(getCurrentActivity(), pharmaPrescriptionId).startTask();
    }

    private void renderMarketPlaceValidationErrors() {
        if (marketPlace == null) return;
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layoutRelativeMain = (RelativeLayout) inflater.inflate(R.layout.uiv3_checkout_qc_scroll, contentView, false);
        LinearLayout baseView = (LinearLayout) layoutRelativeMain.findViewById(R.id.layoutMainLayout);
        contentView.addView(layoutRelativeMain);
        btnListFooter = (Button) layoutRelativeMain.findViewById(R.id.btnListFooter);

        renderTermsAndConditions(baseView);
        renderAgeValidations(baseView, inflater);
        renderPharmaPrescriptionValidations(baseView, inflater);


        btnListFooter.setTypeface(faceRobotoRegular);
        if (marketPlace.isPharamaPrescriptionNeeded()) {
            btnListFooter.setText(getString(R.string.uploadPrescription));
            btnListFooter.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
        } else {
            btnListFooter.setText("CONTINUE");
            btnListFooter.setTag(Constants.CONTINUE_BTN_TAG);
        }
        btnListFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPharmaRadioBtnNoSelected) { // check if pharma radio btn no selected
                    showAlertDialog(getResources().getString(R.string.pharma_msg));
                } else if (hashMapRadioBtnAgeCheckNo.size() > 0) {  //check if any age-validation radio btn no selected
                    showAlertDialog(getResources().getString(R.string.age_validation_required));
                } else {
                    if (marketPlace.isAgeCheckRequired()) {
                        trackEvent(TrackingAware.PRE_CHECKOUT_AGE_LEGAL_ACCEPTED, null);
                    }

                    if (String.valueOf(btnListFooter.getTag()).equals(Constants.CONTINUE_BTN_TAG)) {
                        proceedToQc();
                    } else {
                        ArrayList<SavedPrescription> savedPrescriptionArrayList = marketPlace.getSavedPrescription();
                        if (savedPrescriptionArrayList != null && savedPrescriptionArrayList.size() > 0) {
                            Intent intent = new Intent(getCurrentActivity(), PrescriptionListActivity.class);
                            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        } else {
                            Intent intent = new Intent(getCurrentActivity(), UploadNewPrescriptionActivity.class);
                            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }


                    }
                }
            }
        });
    }

    private void renderTermsAndConditions(LinearLayout base) {
        if (marketPlace.hasTermsAndCond()) {
            BBWebView webView = new BBWebView(this);
            webView.loadData(marketPlace.getTermsAndCond(), "text/html", "UTF-8");
            base.addView(webView);
        }
    }

    private void renderAgeValidations(LinearLayout base, LayoutInflater inflater) {
        if (!marketPlace.isAgeCheckRequired() || marketPlace.getAgeCheckRequiredDetail() == null)
            return;

        for (final MarketPlaceAgeCheck marketPlaceAgeCheck : marketPlace.getAgeCheckRequiredDetail()) {
            View ageLayout = inflater.inflate(R.layout.uiv3_age_validation_layout, base, false);
            TextView txtAgeMsg = (TextView) ageLayout.findViewById(R.id.txtAgeMsg);
            txtAgeMsg.setTypeface(faceRobotoRegular);
            txtAgeMsg.setText(marketPlaceAgeCheck.getAgeMessage());

            final RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
            rbtnYes.setTag(marketPlaceAgeCheck.getFulfillmentId());
            final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
            rbtnNo.setTag(marketPlaceAgeCheck.getFulfillmentId());
            rbtnYes.setTypeface(faceRobotoRegular);
            rbtnNo.setTypeface(faceRobotoRegular);
            rbtnYes.setText(getString(R.string.preYesRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit() + " year");
            rbtnNo.setText(getString(R.string.preNoRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit()
                    + " year, remove " + marketPlaceAgeCheck.getDisplayName() + " from this order");

            rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        hashMapRadioBtnAgeCheckNo.remove(rbtnYes.getTag().toString());
                    }
                }
            });

            rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        hashMapRadioBtnAgeCheckNo.put(rbtnNo.getTag().toString(), true);
                        showAlertDialog(null,
                                "Remove all " + marketPlaceAgeCheck.getDisplayName() + " products from Basket?",
                                DialogButton.YES, DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET_VIA_AGE,
                                marketPlaceAgeCheck.getFulfillmentId(), "Yes");
                    }
                }
            });
            base.addView(ageLayout);
        }
    }

    private void renderPharmaPrescriptionValidations(LinearLayout base, LayoutInflater inflater) {
        if (!marketPlace.isPharamaPrescriptionNeeded() || marketPlace.getPharmaPrescriptionInfo() == null)
            return;
        PharmaPrescriptionInfo pharmaPrescriptionInfo = marketPlace.getPharmaPrescriptionInfo();
        View ageLayout = inflater.inflate(R.layout.uiv3_age_validation_layout, base, false);

        TextView txtheaderMsg = (TextView) ageLayout.findViewById(R.id.txtAgeMsg);
        txtheaderMsg.setTypeface(faceRobotoRegular);
        txtheaderMsg.setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));

        MessageInfo msgInfo = pharmaPrescriptionInfo.getMsgInfo();
        if (msgInfo != null && msgInfo.getParams() != null) {
            ArrayList<Class<?>> activitiesList = new ArrayList<>();
            ArrayList<Integer> fragmentCodeArrayList = new ArrayList<>();
            for (int i = 0; i < msgInfo.getParams().size(); i++) {
                activitiesList.add(BackButtonActivity.class);
                fragmentCodeArrayList.add(FragmentCodes.START_VIEW_BASKET);
            }

            if (!TextUtils.isEmpty(msgInfo.getMessageStr())) {
                MessageFormatUtil<AgeValidationActivity> messageFormatUtil = new MessageFormatUtil<>();
                SpannableStringBuilder msgContent = messageFormatUtil.
                        replaceStringArgWithDisplayNameAndLink(this, msgInfo.getMessageStr(),
                                msgInfo.getParams(), activitiesList, fragmentCodeArrayList);
                txtheaderMsg.setMovementMethod(LinkMovementMethod.getInstance());
                if (msgContent != null) {
                    txtheaderMsg.setText(msgContent, TextView.BufferType.SPANNABLE);
                    txtheaderMsg.setSelected(true);
                    txtheaderMsg.setVisibility(View.VISIBLE);
                }
            }
        }


        RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
        final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
        rbtnYes.setTypeface(faceRobotoRegular);
        rbtnNo.setTypeface(faceRobotoRegular);
        rbtnYes.setText(getString(R.string.YesRadioBtnPharmaMsg));
        rbtnNo.setText(getString(R.string.NoRadioBtnPharmaMsg));

        ArrayList<MessageParamInfo> messageParamInfos = marketPlace.getPharmaPrescriptionInfo().getMsgInfo().getParams();
        String fulFillmentIds = null;
        int pharmaInfoSize = messageParamInfos.size();
        if (pharmaInfoSize == 1) {
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
        } else {
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
            for (int j = 1; j < pharmaInfoSize; j++) {
                fulFillmentIds += "," + messageParamInfos.get(j).getInternalValue();
            }
        }
        if (fulFillmentIds != null) {
            rbtnNo.setTag(fulFillmentIds);
        } else {
            assert false : "Fulfillment info ID(s) should not be Null";
            return;
        }

        if (isPharmaRadioBtnNoSelected)
            rbtnNo.setChecked(true);
        rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnListFooter.setText(getString(R.string.uploadPrescription));
                    btnListFooter.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
                    isPharmaRadioBtnNoSelected = false;
                }
            }
        });
        rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnListFooter.setText("CONTINUE");
                    btnListFooter.setTag(Constants.CONTINUE_BTN_TAG);
                    isPharmaRadioBtnNoSelected = true;
                    showAlertDialog(null,
                            "Remove all pharma products from basket", DialogButton.YES,
                            DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET_VIA_PHARMA, String.valueOf(rbtnNo.getTag()));
                }
            }
        });


        base.addView(ageLayout);

        // Display any additional terms and condition
        if (msgInfo != null && msgInfo.getParams() != null) {
            final ArrayList<String> termAndCondition = new ArrayList<>();
            for (MessageParamInfo messageParamInfo : msgInfo.getParams()) {
                if (!TextUtils.isEmpty(messageParamInfo.getExtraInfo()))
                    termAndCondition.add(messageParamInfo.getExtraInfo());
            }
            if (termAndCondition.size() > 0) {
                TextView txtPharmaTcLink = (TextView) ageLayout.findViewById(R.id.txtPharmaTcLink);
                txtPharmaTcLink.setVisibility(View.VISIBLE);
                txtPharmaTcLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TermAndConditionDialog termAndConditionDialog = TermAndConditionDialog.
                                newInstance(termAndCondition);
                        termAndConditionDialog.show(getSupportFragmentManager(), Constants.OTP_REFERRAL_DIALOG);
                    }
                });
            }
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, final Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET_VIA_AGE:
                    trackEvent(TrackingAware.PRE_CHECKOUT_AGE_LEGAL_REJECTED, null);
                    bulkRemoveProducts(valuePassed);
                    break;
                case Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET_VIA_PHARMA:
                    trackEvent(TrackingAware.PRE_CHECKOUT_PHARMA_PRESCRIPTION_NOT_PROVIDED, null);
                    bulkRemoveProducts(valuePassed);
                    break;
                case ApiErrorCodes.BASKET_EMPTY_STR:
                    goToHome();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    private void bulkRemoveProducts(Object fulfillmentInfoIds) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) handler.sendOfflineError();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.cartBulkRemove(fulfillmentInfoIds.toString(), new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse cartBulkRemoveApiResponseCallback, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (cartBulkRemoveApiResponseCallback.status == 0) {
                    CartSummary cartInfo = cartBulkRemoveApiResponseCallback.cartSummary;
                    ((CartInfoAware) getCurrentActivity()).setCartInfo(cartInfo);
                    ((CartInfoAware) getCurrentActivity()).updateUIForCartInfo();
                    ((CartInfoAware) getCurrentActivity()).markBasketDirty();
                    if (cartInfo.getNoOfItems() == 0) {
                        showAlertDialogFinish(null, getResources().getString(R.string.basketEmpty));
                    } else {
                        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
                    }
                } else {
                    handler.sendEmptyMessage(cartBulkRemoveApiResponseCallback.status,
                            cartBulkRemoveApiResponseCallback.message);
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
                handler.handleRetrofitError(error);
            }
        });
    }
}
