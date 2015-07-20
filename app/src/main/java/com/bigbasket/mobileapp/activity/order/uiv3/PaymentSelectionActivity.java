package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostPrepaidPaymentResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponseContent;
import com.bigbasket.mobileapp.handler.HDFCPowerPayHandler;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.order.PowerPayPostParams;
import com.bigbasket.mobileapp.model.order.PowerPayResponse;
import com.bigbasket.mobileapp.model.order.VoucherApplied;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentSelectionActivity extends BackButtonActivity {

    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private LinkedHashMap<String, String> mPaymentTypeMap;
    private String mPotentialOrderId;
    private ArrayList<VoucherApplied> mVoucherAppliedList;
    private HashMap<String, Boolean> mPreviouslyAppliedVoucherMap;
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private TextView mTxtApplicableVoucherCount;
    private String mAppliedVoucherCode;
    private TextView mLblTransactionFailed;
    private TextView mTxtTransactionFailureReason;
    private TextView mLblSelectAnotherMethod;
    private String mSelectedPaymentMethod;
    private OrderDetails mOrderDetails;
    private WPayInitRequest wPayInitRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPotentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);

        mLblTransactionFailed = (TextView) findViewById(R.id.lblTransactionFailed);
        mTxtTransactionFailureReason = (TextView) findViewById(R.id.txtTransactionFailedReason);
        mLblSelectAnotherMethod = (TextView) findViewById(R.id.lblSelectAnotherMethod);

        mLblTransactionFailed.setTypeface(faceRobotoRegular);
        mTxtTransactionFailureReason.setTypeface(faceRobotoRegular);
        mLblSelectAnotherMethod.setTypeface(faceRobotoRegular);

        if (TextUtils.isEmpty(mPotentialOrderId)) return;
        setTitle(getString(R.string.placeorder));

        renderFooter();
        PayuResponse payuResponse = PayuResponse.getInstance(this);
        PowerPayResponse powerPayResponse = PowerPayResponse.getInstance(this);
        boolean isPayuPending = payuResponse != null && payuResponse.isSuccess();
        boolean isHdfcPpPending = powerPayResponse != null && powerPayResponse.isSuccess();
        if (isPayuPending || isHdfcPpPending) {
            mSelectedPaymentMethod = isPayuPending ? Constants.PAYU : Constants.HDFC_POWER_PAY;
            findViewById(R.id.viewPaymentInProgress).setVisibility(View.VISIBLE);
            ArrayList<VoucherApplied> previouslyAppliedVoucherList = VoucherApplied.readFromPreference(getCurrentActivity());
            if (previouslyAppliedVoucherList == null || previouslyAppliedVoucherList.size() == 0) {
                onPlaceOrderAction();
            } else {
                mPreviouslyAppliedVoucherMap = VoucherApplied.toMap(previouslyAppliedVoucherList);
                applyVoucher(previouslyAppliedVoucherList.get(0).getVoucherCode());
            }
        } else {
            findViewById(R.id.viewPaymentInProgress).setVisibility(View.GONE);
            renderPaymentDetails();
        }
        trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null);
    }

    private void renderFooter() {
        mOrderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        if (mOrderDetails == null) return;
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, mOrderDetails.getFormattedFinalTotal(),
                getString(R.string.placeOrderCaps), false);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlaceOrderAction();
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.PAYMENT_MODE, mSelectedPaymentMethod);
                trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_CLICKED, map);
            }
        });
    }

    private void renderPaymentDetails() {
        mActiveVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        mAppliedVoucherCode = getIntent().getStringExtra(Constants.EVOUCHER_CODE);

        ArrayList<PaymentType> paymentTypes = getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES);
        mPaymentTypeMap = new LinkedHashMap<>();
        for (PaymentType paymentType : paymentTypes) {
            mPaymentTypeMap.put(paymentType.getDisplayName(), paymentType.getValue());
        }
        ArrayList<CreditDetails> creditDetails = getIntent().getParcelableArrayListExtra(Constants.CREDIT_DETAILS);
        renderPaymentMethodsAndSummary(creditDetails);
    }

    private void renderPaymentMethodsAndSummary(@Nullable ArrayList<CreditDetails> creditDetails) {
        // Show invoice and other order details
        LayoutInflater inflater = getLayoutInflater();
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int secondaryColor = getResources().getColor(R.color.uiv3_secondary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);
        LinearLayout layoutOrderSummaryInfo = (LinearLayout) findViewById(R.id.layoutOrderSummaryInfo);
        layoutOrderSummaryInfo.removeAllViews();

        String numItems = mOrderDetails.getTotalItems() + " Item" + (mOrderDetails.getTotalItems() > 1 ? "s" : "");
        View orderItemsRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.orderItems),
                numItems, normalColor, secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderItemsRow);

        View subTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.subTotal),
                UIUtil.asRupeeSpannable(mOrderDetails.getSubTotal(), faceRupee), normalColor, secondaryColor,
                faceRobotoRegular);
        layoutOrderSummaryInfo.addView(subTotalRow);

        View deliveryChargeRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                UIUtil.asRupeeSpannable(mOrderDetails.getDeliveryCharge(), faceRupee), normalColor,
                secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (creditDetails != null && creditDetails.size() > 0) {
            for (CreditDetails creditDetail : creditDetails) {
                View creditDetailRow = UIUtil.getOrderSummaryRow(inflater, creditDetail.getMessage(),
                        UIUtil.asRupeeSpannable(creditDetail.getCreditValue(), faceRupee), normalColor,
                        secondaryColor, faceRobotoRegular);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View finalTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                UIUtil.asRupeeSpannable(mOrderDetails.getFinalTotal(), faceRupee), orderTotalLabelColor,
                orderTotalValueColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(finalTotalRow);

        mTxtApplyVoucher = (TextView) findViewById(R.id.txtApplyVoucher);
        mTxtApplyVoucher.setTypeface(faceRobotoRegular);
        OnShowAvailableVouchersListener showAvailableVouchersListener = new OnShowAvailableVouchersListener();
        mTxtApplyVoucher.setOnClickListener(showAvailableVouchersListener);
        mTxtApplicableVoucherCount = (TextView) findViewById(R.id.txtApplicableVoucherCount);
        mTxtApplicableVoucherCount.setTypeface(faceRobotoRegular);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setText(mActiveVouchersList.size() + " " +
                    (mActiveVouchersList.size() > 1 ?
                            getString(R.string.voucherApplicablePlural) :
                            getString(R.string.voucherApplicableSingular)));
            mTxtApplicableVoucherCount.setVisibility(View.VISIBLE);
        } else {
            mTxtApplicableVoucherCount.setVisibility(View.GONE);
        }
        mTxtApplicableVoucherCount.setOnClickListener(showAvailableVouchersListener);

        mTxtRemoveVoucher = (TextView) findViewById(R.id.txtRemoveVoucher);
        mTxtRemoveVoucher.setTypeface(faceRobotoRegular);
        mTxtRemoveVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(getString(R.string.removeVoucherHeading), getString(R.string.removeVoucherDesc),
                        DialogButton.YES, DialogButton.CANCEL, Constants.REMOVE_VOUCHER, mAppliedVoucherCode,
                        getString(R.string.remove));
            }
        });

        if (!TextUtils.isEmpty(mAppliedVoucherCode)) {
            showVoucherAppliedText(mAppliedVoucherCode);
        } else {
            onVoucherRemoved(null);
        }

        TextView lblAmountFromWallet = (TextView) findViewById(R.id.lblAmountFromWallet);
        lblAmountFromWallet.setTypeface(faceRobotoRegular);

        boolean isInHDFCPayMode = HDFCPowerPayHandler.isInHDFCPayMode(this)
                && mPaymentTypeMap.containsValue(Constants.HDFC_POWER_PAY);
        RadioGroup layoutPaymentOptions = (RadioGroup) findViewById(R.id.layoutPaymentOptions);
        layoutPaymentOptions.removeAllViews();

        if (mOrderDetails.getFinalTotal() <= 0) {
            lblAmountFromWallet.setVisibility(View.VISIBLE);
            mSelectedPaymentMethod = mPaymentTypeMap.entrySet().iterator().next().getValue();
        } else {
            lblAmountFromWallet.setVisibility(View.GONE);
            int i = 0;
            for (final Map.Entry<String, String> entrySet : mPaymentTypeMap.entrySet()) {
                if (isInHDFCPayMode && !entrySet.getValue().equals(Constants.HDFC_POWER_PAY)) {
                    continue;
                }
                RadioButton rbtnPaymentType = getPaymentOptionRadioButton(layoutPaymentOptions);
                rbtnPaymentType.setText(entrySet.getKey());
                rbtnPaymentType.setId(i);
                boolean isSelected = TextUtils.isEmpty(mSelectedPaymentMethod) ? i == 0 :
                        mSelectedPaymentMethod.equals(entrySet.getValue());
                if (isSelected) {
                    rbtnPaymentType.setChecked(true);
                    mSelectedPaymentMethod = entrySet.getValue();
                }
                rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mSelectedPaymentMethod = entrySet.getValue();
                        }
                    }
                });
                layoutPaymentOptions.addView(rbtnPaymentType);
                i++;
            }
        }

        mLblTransactionFailed.setTypeface(faceRobotoRegular);
        mTxtTransactionFailureReason.setTypeface(faceRobotoRegular);
        mLblTransactionFailed.setVisibility(View.GONE);
        mTxtTransactionFailureReason.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setTypeface(faceRobotoRegular);
    }

    private class OnShowAvailableVouchersListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent availableVoucherListActivity = new Intent(getCurrentActivity(), AvailableVoucherListActivity.class);
            availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
            startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
        }
    }

    public void displayPayuFailure(String reason) {
        if (mTxtTransactionFailureReason == null || mLblTransactionFailed == null
                || mLblSelectAnotherMethod == null) return;
        mTxtTransactionFailureReason.setVisibility(View.VISIBLE);
        mLblTransactionFailed.setVisibility(View.VISIBLE);
        mLblSelectAnotherMethod.setVisibility(View.VISIBLE);

        mTxtTransactionFailureReason.setText(reason);
    }

    private RadioButton getPaymentOptionRadioButton(ViewGroup parent) {
        LayoutInflater inflater = getLayoutInflater();
        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.uiv3_payment_option_rbtn, parent, false);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_small));
        radioButton.setLayoutParams(layoutParams);
        radioButton.setTypeface(faceRobotoRegular);
        return radioButton;
    }


    public void onVoucherApplied(String voucher, OrderDetails orderDetails,
                                 ArrayList<CreditDetails> creditDetails) {
        if (!TextUtils.isEmpty(voucher)) {
            showVoucherAppliedText(voucher);
            mOrderDetails = orderDetails;
            renderPaymentMethodsAndSummary(creditDetails);
        }
    }

    private void showVoucherAppliedText(String voucher) {
        mTxtApplyVoucher.setVisibility(View.GONE);
        mTxtRemoveVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setText("eVoucher: " + voucher + " Applied!");
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.GONE);
        }
        mAppliedVoucherCode = voucher;
    }

    public void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.postVoucher(mPotentialOrderId, voucherCode, new Callback<ApiResponse<PostVoucherApiResponseContent>>() {
                @Override
                public void success(ApiResponse<PostVoucherApiResponseContent> postVoucherApiResponse, Response response) {
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    HashMap<String, String> map = new HashMap<>();
                    switch (postVoucherApiResponse.status) {
                        case 0:
                            if (mPreviouslyAppliedVoucherMap == null ||
                                    mPreviouslyAppliedVoucherMap.size() == 0) {
                                trackEvent(TrackingAware.CHECKOUT_VOUCHER_APPLIED, map);
                            }
                            onVoucherSuccessfullyApplied(voucherCode,
                                    postVoucherApiResponse.apiResponseContent.orderDetails,
                                    postVoucherApiResponse.apiResponseContent.creditDetails);
                            break;
                        default:
                            handler.sendEmptyMessage(postVoucherApiResponse.status, postVoucherApiResponse.message);
                            map.put(TrackEventkeys.FAILURE_REASON, postVoucherApiResponse.message);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, map);
                            break;
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
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
                }
            });
        } else {
            handler.sendOfflineError();
        }
    }

    public void removeVoucher(final String voucherCode) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.removeVoucher(mPotentialOrderId, new Callback<ApiResponse<PostVoucherApiResponseContent>>() {
            @Override
            public void success(ApiResponse<PostVoucherApiResponseContent> removeVoucherApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (removeVoucherApiResponse.status) {
                    case 0:
                        Toast.makeText(getCurrentActivity(),
                                getString(R.string.voucherWasRemoved), Toast.LENGTH_SHORT).show();
                        onVoucherRemoved(voucherCode, removeVoucherApiResponse.apiResponseContent.orderDetails,
                                removeVoucherApiResponse.apiResponseContent.creditDetails);
                        break;
                    default:
                        handler.sendEmptyMessage(removeVoucherApiResponse.status,
                                removeVoucherApiResponse.message);
                        break;
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

    /**
     * Callback when the voucher has been successfully applied
     */
    private void onVoucherSuccessfullyApplied(String voucherCode, OrderDetails orderDetails,
                                              ArrayList<CreditDetails> creditDetails) {
        if (mPreviouslyAppliedVoucherMap != null) {
            mPreviouslyAppliedVoucherMap.put(voucherCode, true);
            boolean allApplied = true;
            for (Map.Entry<String, Boolean> entry : mPreviouslyAppliedVoucherMap.entrySet()) {
                if (!entry.getValue()) {
                    allApplied = false;
                    applyVoucher(entry.getKey());
                    break;
                }
            }
            if (allApplied) {
                onPlaceOrderAction();
            }
        } else {
            if (mVoucherAppliedList == null) {
                mVoucherAppliedList = new ArrayList<>();
            }
            mVoucherAppliedList.add(new VoucherApplied(voucherCode));
            VoucherApplied.saveToPreference(mVoucherAppliedList, this);
            onVoucherApplied(voucherCode, orderDetails, creditDetails);
        }
    }

    private void onVoucherRemoved(@Nullable String voucherCode) {
        mAppliedVoucherCode = null;
        if (voucherCode != null && mPreviouslyAppliedVoucherMap != null
                && mPreviouslyAppliedVoucherMap.containsKey(voucherCode)) {
            mPreviouslyAppliedVoucherMap.remove(voucherCode);
        }
        mTxtApplyVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setVisibility(View.GONE);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.VISIBLE);
        }
    }

    private void onVoucherRemoved(@Nullable String voucherCode, OrderDetails orderDetails,
                                  ArrayList<CreditDetails> creditDetails) {
        onVoucherRemoved(voucherCode);
        mOrderDetails = orderDetails;
        renderPaymentMethodsAndSummary(creditDetails);
    }

    private void onPlaceOrderAction() {
        PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
        PowerPayResponse powerPayResponse = PowerPayResponse.getInstance(getCurrentActivity());
        boolean isPayuPending = payuResponse != null && payuResponse.isSuccess();
        boolean isHdfcPpPending = powerPayResponse != null && powerPayResponse.isSuccess();
        if (isCreditCardPayment()) {
            if (!(isPayuPending || isHdfcPpPending) && mOrderDetails.getFinalTotal() > 0) {
                startCreditCardTxnActivity(mOrderDetails.getFinalTotal());
            } else {
                if (isPayuPending) {
                    placeOrder(payuResponse.getTxnId());
                } else if (isHdfcPpPending) {
                    placeOrder(powerPayResponse.getTxnId());
                }
            }
        } else {
            placeOrder(null);
        }
    }

    private boolean isCreditCardPayment() {
        return mSelectedPaymentMethod != null &&
                (mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY) ||
                        mSelectedPaymentMethod.equals(Constants.PAYU));
    }

    private void startCreditCardTxnActivity(double amount) {
        switch (mSelectedPaymentMethod) {
            case Constants.PAYU:
                Intent intent = new Intent(getApplicationContext(), PayuTransactionActivity.class);
                intent.putExtra(Constants.P_ORDER_ID, mPotentialOrderId);
                intent.putExtra(Constants.FINAL_PAY, UIUtil.formatAsMoney(amount));
                startActivityForResult(intent, Constants.PAYU_SUCCESS);
                break;
            case Constants.HDFC_POWER_PAY:
                getHdfcPowerPayParams();
                break;
        }
    }

    private void syncContentView() {
        View paymentInProgress = findViewById(R.id.viewPaymentInProgress);
        if (paymentInProgress.getVisibility() == View.VISIBLE) {
            paymentInProgress.setVisibility(View.GONE);
            renderPaymentDetails();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            findViewById(R.id.viewPaymentInProgress).setVisibility(View.GONE);
            if (resultCode == RESULT_OK) {
                WPayResponse res = WibmoSDK.processInAppResponseWPay(data);
                String pgTxnId = res.getWibmoTxnId();
                String dataPickupCode = res.getDataPickUpCode();
                validateHdfcPowerPayResponse(pgTxnId, dataPickupCode,
                        wPayInitRequest.getTransactionInfo().getMerTxnId());
            } else {
                if (data != null) {
                    String resCode = data.getStringExtra("ResCode");
                    String resDesc = data.getStringExtra("ResDesc");
                    communicateHdfcPowerPayResponseFailure(resCode, resDesc);
                } else {
                    communicateHdfcPowerPayResponseFailure(null, null);
                }
            }
        } else {
            switch (resultCode) {
                case NavigationCodes.VOUCHER_APPLIED:
                    if (data != null) {
                        String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
                        if (!TextUtils.isEmpty(voucherCode)) {
                            applyVoucher(voucherCode);
                        }
                    }
                    break;
                case Constants.PREPAID_TXN_FAILED:
                    syncContentView();
                    trackEvent(TrackingAware.CHECKOUT_PAYMENT_GATEWAY_FAILURE, null);
                    displayPayuFailure(getString(R.string.failedToProcess));
                    break;
                case Constants.PREPAID_TXN_ABORTED:
                    syncContentView();
                    trackEvent(TrackingAware.CHECKOUT_PAYMENT_GATEWAY_ABORTED, null);
                    displayPayuFailure(getString(R.string.youAborted));
                    break;
                case Constants.PAYU_SUCCESS:
                    trackEvent(TrackingAware.CHECKOUT_PAYMENT_GATEWAY_SUCCESS, null);
                    PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                    if (payuResponse == null) {
                        showAlertDialog("Error", "Unable to place your order via credit-card." +
                                "\nPlease choose another method.\n" +
                                "In case your credit card has been charged, " +
                                "BigBasket customer service will get back to you regarding " +
                                "the payment made by you.", Constants.SOURCE_PAYU_EMPTY);
                    } else {
                        placeOrder(payuResponse.getTxnId());
                    }
                    break;
                case NavigationCodes.GO_TO_SLOT_SELECTION:
                    setResult(NavigationCodes.GO_TO_SLOT_SELECTION);
                    finish();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    private void placeOrder(String txnId) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait), false);
        bigBasketApiService.placeOrder(mPotentialOrderId, txnId, mSelectedPaymentMethod,
                new Callback<OldApiResponse<PlaceOrderApiResponseContent>>() {
                    @Override
                    public void success(OldApiResponse<PlaceOrderApiResponseContent> placeOrderApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (placeOrderApiResponse.status.equals(Constants.OK)) {
                            postOrderCreation(placeOrderApiResponse.apiResponseContent.orders,
                                    placeOrderApiResponse.apiResponseContent.addMoreLink);
                        } else if (placeOrderApiResponse.errorType != null &&
                                placeOrderApiResponse.errorType.equals(ApiErrorCodes.AMOUNT_MISMATCH_STR)) {
                            String paymentMethod = mSelectedPaymentMethod;
                            String amtTxt = null;
                            switch (paymentMethod) {
                                case Constants.PAYU:
                                    PayuResponse payuResponse = PayuResponse.getInstance(getCurrentActivity());
                                    amtTxt = payuResponse != null ? "of Rs. " + payuResponse.getAmount() + " " : "";
                                    break;
                                case Constants.HDFC_POWER_PAY:
                                    amtTxt = wPayInitRequest != null ? "of Rs. " + wPayInitRequest.getTransactionInfo().getTxnAmount() + " " : "";
                                    break;
                            }
                            showAlertDialog("Create a separate order?",
                                    "We are sorry. The payment amount " + amtTxt + "does not match the" +
                                            " order amount of Rs." + mOrderDetails.getFormattedFinalTotal() + ". Please go through the " +
                                            "payment process to complete this" +
                                            " transaction. BigBasket customer service will get back to you regarding " +
                                            "the payment made by you.",
                                    DialogButton.YES, DialogButton.NO, Constants.SOURCE_PLACE_ORDER,
                                    amtTxt);
                        } else if (placeOrderApiResponse.errorType != null
                                && placeOrderApiResponse.errorType.equals(ApiErrorCodes.INVALID_FIELD_STR)) {
                            PayuResponse.clearTxnDetail(getCurrentActivity());
                            VoucherApplied.clearFromPreference(getCurrentActivity());
                            PowerPayResponse.clearTxnDetail(getCurrentActivity());
                            showAlertDialog(getString(R.string.error),
                                    !TextUtils.isEmpty(placeOrderApiResponse.message) ?
                                            placeOrderApiResponse.message : getString(R.string.failedToProcess));
                            displayPayuFailure(getString(R.string.failedToProcess));
                        } else {
                            handler.sendEmptyMessage(placeOrderApiResponse.getErrorTypeAsInt(), placeOrderApiResponse.message);
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

    private void postOrderCreation(ArrayList<Order> orders, String addMoreLink) {
        if (orders == null || orders.size() == 0) return;
        for (Order order : orders) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.ORDER_ID, order.getOrderId());
            map.put(TrackEventkeys.ORDER_AMOUNT, order.getOrderValue());
            map.put(TrackEventkeys.ORDER_NUMBER, order.getOrderNumber());
            map.put(TrackEventkeys.ORDER_TYPE, order.getOrderType());
            if (!TextUtils.isEmpty(order.getVoucher()))
                map.put(TrackEventkeys.VOUCHER_NAME, order.getVoucher());
            map.put(TrackEventkeys.PAYMENT_MODE, order.getPaymentMethod());
            map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
            trackEvent(TrackingAware.CHECKOUT_ORDER_COMPLETE, map, null, null, true);
            trackEventAppsFlyer(TrackingAware.PLACE_ORDER, order.getOrderValue(), map);
        }

        PayuResponse.clearTxnDetail(this);
        VoucherApplied.clearFromPreference(this);
        PowerPayResponse.clearTxnDetail(this);
        ((CartInfoAware) getCurrentActivity()).markBasketDirty();
        showOrderThankyou(orders, addMoreLink);
    }

    private void showOrderThankyou(ArrayList<Order> orders, String addMoreLink) {
        setNextScreenNavigationContext(TrackEventkeys.CO_PAYMENT);
        Intent invoiceIntent = new Intent(this, OrderInvoiceActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        invoiceIntent.putExtra(Constants.ADD_MORE_LINK, addMoreLink);
        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    private void getHdfcPowerPayParams() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getPrepaidPaymentParams(mPotentialOrderId, mSelectedPaymentMethod,
                mOrderDetails.getFormattedFinalTotal(), new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPrepaidPaymentResponse> getPrepaidPaymentApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPrepaidPaymentApiResponse.status) {
                            case 0:
                                initializeHDFCPowerPay(getPrepaidPaymentApiResponse.apiResponseContent.powerPayPostParams);
                                break;
                            default:
                                handler.sendEmptyMessage(getPrepaidPaymentApiResponse.status, getPrepaidPaymentApiResponse.message);
                                break;
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

    private void validateHdfcPowerPayResponse(String pgTxnId, String dataPickupCode, String txnId) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        PowerPayResponse.createInstance(this, txnId,
                pgTxnId, dataPickupCode, true);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postPrepaidPayment(txnId, mPotentialOrderId, mSelectedPaymentMethod, "1",
                pgTxnId, dataPickupCode, mOrderDetails.getFormattedFinalTotal(),
                new PostPrepaidParamsCallback());
    }

    private void communicateHdfcPowerPayResponseFailure(String resCode, String resDesc) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        if (wPayInitRequest == null || wPayInitRequest.getTransactionInfo() == null) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postPrepaidPayment(wPayInitRequest.getTransactionInfo().getMerTxnId(),
                mPotentialOrderId, mSelectedPaymentMethod, "0",
                resCode, resDesc,
                new PostPrepaidParamsCallback());
    }

    private void initializeHDFCPowerPay(PowerPayPostParams powerPayPostParams) {
        new PowerPayTriggerAsyncTask().execute(powerPayPostParams);
    }

    private void startHDFCPowerPay(PowerPayPostParams powerPayPostParams) {
        wPayInitRequest = new WPayInitRequest();

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTxnAmount(powerPayPostParams.getFormattedAmount());
        transactionInfo.setTxnCurrency(powerPayPostParams.getCurrency());
        transactionInfo.setSupportedPaymentType(powerPayPostParams.getPaymentChoices());
        transactionInfo.setTxnDesc(powerPayPostParams.getTxnDesc());
        transactionInfo.setMerTxnId(powerPayPostParams.getTxnId());
        if (powerPayPostParams.getAppData() != null) {
            transactionInfo.setMerAppData(powerPayPostParams.getAppData());
        }

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerAppId(powerPayPostParams.getMerchantAppId());
        merchantInfo.setMerId(powerPayPostParams.getMerchantId());
        merchantInfo.setMerCountryCode(powerPayPostParams.getCountryCode());

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustEmail(powerPayPostParams.getEmail());
        customerInfo.setCustDob(powerPayPostParams.getDob());
        customerInfo.setCustName(powerPayPostParams.getName());
        customerInfo.setCustMobile(powerPayPostParams.getMobile());

        wPayInitRequest.setTransactionInfo(transactionInfo);
        wPayInitRequest.setMerchantInfo(merchantInfo);
        wPayInitRequest.setCustomerInfo(customerInfo);

        wPayInitRequest.setMsgHash(powerPayPostParams.getMsgHash());
        WibmoSDK.startForInApp(this, wPayInitRequest);
    }

    private class PowerPayTriggerAsyncTask extends AsyncTask<PowerPayPostParams, Integer, PowerPayPostParams> {

        @Override
        protected void onPreExecute() {
            showProgressDialog(getString(R.string.please_wait));
        }

        @Override
        protected PowerPayPostParams doInBackground(PowerPayPostParams... params) {
            PowerPayPostParams powerPayPostParams = params[0];
            WibmoSDK.setWibmoIntentActionPackage(powerPayPostParams.getPkgName());
            WibmoSDKConfig.setWibmoDomain(powerPayPostParams.getServerUrl());
            WibmoSDK.init(getApplicationContext());
            return powerPayPostParams;
        }

        @Override
        protected void onPostExecute(PowerPayPostParams powerPayPostParams) {
            hideProgressDialog();
            startHDFCPowerPay(powerPayPostParams);
        }
    }

    private class PostPrepaidParamsCallback implements Callback<ApiResponse<PostPrepaidPaymentResponse>> {
        @Override
        public void success(ApiResponse<PostPrepaidPaymentResponse> postPrepaidPaymentApiResponse, Response response) {
            if (isSuspended()) return;
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            switch (postPrepaidPaymentApiResponse.status) {
                case 0:
                    if (postPrepaidPaymentApiResponse.apiResponseContent.paymentStatus) {
                        placeOrder(PowerPayResponse.getInstance(getCurrentActivity()).getTxnId());
                    } else {
                        displayPayuFailure(getString(R.string.failedToProcess));
                    }
                    break;
                default:
                    handler.sendEmptyMessage(postPrepaidPaymentApiResponse.status, postPrepaidPaymentApiResponse.message);
                    break;
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
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, @Nullable String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equals(Constants.REMOVE_VOUCHER)
                && valuePassed != null) {
            removeVoucher(valuePassed.toString());
        } else if (sourceName != null) {
            // When user clicks the Yes button in Alert Dialog that is shown when there's a amount mismatch
            switch (sourceName) {
                case Constants.SOURCE_PLACE_ORDER:
                    PayuResponse.clearTxnDetail(this);
                    PowerPayResponse.clearTxnDetail(this);
                    trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_AMOUNT_MISMATCH, null);
                    if (isCreditCardPayment()) {
                        startCreditCardTxnActivity(mOrderDetails.getFinalTotal());
                    } else {
                        placeOrder(null);
                    }
                    break;
                case Constants.SOURCE_POST_PAYMENT:
                    PayuResponse.clearTxnDetail(this);
                    finish();
                    break;
                case Constants.SOURCE_PAYU_EMPTY:
                    finish();
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, null, valuePassed);
        }
    }

    @Override
    protected void onNegativeButtonClicked(DialogInterface dialogInterface, String sourceName) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.SOURCE_PLACE_ORDER:
                    findViewById(R.id.viewPaymentInProgress).setVisibility(View.GONE);
                    renderPaymentDetails();
                default:
                    super.onNegativeButtonClicked(dialogInterface, sourceName);
                    break;
            }
        } else {
            super.onNegativeButtonClicked(dialogInterface, null);
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_payment_option;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PAYMENT_SELECTION_SCREEN;
    }
}
