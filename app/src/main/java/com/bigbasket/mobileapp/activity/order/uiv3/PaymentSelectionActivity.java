package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiPayZappResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiPrePaymentResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponseContent;
import com.bigbasket.mobileapp.factory.payment.OrderPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.DuplicateClickAware;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentOptionsKnowMoreDialogCallback;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MutableLong;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

public class PaymentSelectionActivity extends BackButtonActivity
        implements OnPaymentValidationListener, PaymentTxnInfoAware,
        PaymentMethodsView.OnPaymentOptionSelectionListener, PaymentOptionsKnowMoreDialogCallback {

    private static final java.lang.String IS_PREPAYMENT_TASK_PAUSED = "is_prepayment_task_paused";
    private static final java.lang.String IS_PREPAYMENT_TASK_STARTED = "is_prepayment_task_started";
    private static final java.lang.String IS_PREPAYMENT_ABORT_INITIATED = "is_prepayment_abort_initiated";
    private final String PAYMENT_PARAMS = "payment_params";
    private final String PAYZAPP_PAYMENT_PARAMS = "payzapp_payment_params";
    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private ArrayList<PaymentType> paymentTypeList;
    private String mPotentialOrderId;
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private TextView mTxtApplicableVoucherCount;
    private String mAppliedVoucherCode;
    private String mSelectedPaymentMethod;
    private OrderDetails mOrderDetails;
    @Nullable
    private String mOrderAmount; // Only applicable for Payzapp
    private String mTxnId;
    private ArrayList<Order> mOrdersCreated;
    private String mAddMoreLink;
    private String mAddMoreMsg;
    private MutableLong mElapsedTime;
    private boolean mIsPrepaymentProcessingStarted;
    private OrderPrepaymentProcessingTask<PaymentSelectionActivity> mOrderPrepaymentProcessingTask;
    private boolean mIsPrepaymentAbortInitiated;
    private boolean isPayUOptionVisible;
    private PayzappPostParams mPayzappPostParams;
    private HashMap<String, String> mPaymentParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mElapsedTime = new MutableLong();

        setCurrentScreenName(TrackEventkeys.CO_PAYMENT);
        mPotentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);

        if (TextUtils.isEmpty(mPotentialOrderId)) return;
        setTitle(getString(R.string.placeorder));

        mOrderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        if (mOrderDetails == null) return;
        renderPaymentDetails();
        renderFooter(false);
        MoEngageWrapper.suppressInAppMessageHere(moEHelper);
        if (savedInstanceState != null) {
            if (mOrdersCreated == null) {
                mOrdersCreated = savedInstanceState.getParcelableArrayList(Constants.ORDERS);
            }
            if (mTxnId == null) {
                mTxnId = savedInstanceState.getString(Constants.TXN_ID);
            }
            if (mSelectedPaymentMethod == null) {
                mSelectedPaymentMethod = savedInstanceState.getString(Constants.PAYMENT_METHOD);
            }
            mIsPrepaymentProcessingStarted =
                    savedInstanceState.getBoolean(IS_PREPAYMENT_TASK_STARTED, false);
            mIsPrepaymentAbortInitiated =
                    savedInstanceState.getBoolean(IS_PREPAYMENT_ABORT_INITIATED, false);
            if (isPaymentPending()) {
                startPrepaymentProcessing(savedInstanceState);
            }
            mOrderAmount = savedInstanceState.getString(Constants.AMOUNT);
        } else {
            trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null, null, null, false, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mOrdersCreated != null) {
            outState.putParcelableArrayList(Constants.ORDERS, mOrdersCreated);
        }
        if (mTxnId != null) {
            outState.putString(Constants.TXN_ID, mTxnId);
        }
        if (mSelectedPaymentMethod != null) {
            outState.putString(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        }
        if (mOrderPrepaymentProcessingTask != null) {
            outState.putBoolean(IS_PREPAYMENT_TASK_STARTED, mIsPrepaymentProcessingStarted);
            outState.putBoolean(IS_PREPAYMENT_TASK_PAUSED, mOrderPrepaymentProcessingTask.isPaused());
            outState.putBoolean(IS_PREPAYMENT_ABORT_INITIATED, mIsPrepaymentAbortInitiated);
        }
        if (mOrderAmount != null) {
            outState.putString(Constants.AMOUNT, mOrderAmount);
        }
        if (mPaymentParams != null) {
            Gson gson = new Gson();
            String jsonPaymentParams = gson.toJson(mPaymentParams);
            outState.putString(PAYMENT_PARAMS, jsonPaymentParams);
        }
        if (mPayzappPostParams != null) {
            outState.putParcelable(PAYZAPP_PAYMENT_PARAMS, mPayzappPostParams);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrderPrepaymentProcessingTask != null && !mIsPrepaymentAbortInitiated) {
            mOrderPrepaymentProcessingTask.resume();
        }
        if (PaytmResponseHolder.hasPendingTransaction()) {
            PaytmResponseHolder.processPaytmResponse(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOrderPrepaymentProcessingTask != null) {
            mOrderPrepaymentProcessingTask.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOrderPrepaymentProcessingTask != null) {
            mOrderPrepaymentProcessingTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (isPaymentPending()) {
            if (mOrderPrepaymentProcessingTask != null) {
                mOrderPrepaymentProcessingTask.pause();
            }
            showAlertDialog(null, getString(R.string.abort_payment_transaction_confirmation),
                    getString(R.string.yesTxt), getString(R.string.noTxt),
                    Constants.PREPAYMENT_ABORT_CONFIRMATION_DIALOG, null);
            mIsPrepaymentAbortInitiated = true;
        } else {
            super.onBackPressed();
        }
    }

    private boolean isPaymentPending() {
        return mIsPrepaymentProcessingStarted && !TextUtils.isEmpty(mSelectedPaymentMethod)
                && mOrdersCreated != null;
    }

    private void renderCheckOutProgressView() {
        LinearLayout layoutPaymentContainer = (LinearLayout) findViewById(R.id.layoutCheckoutProgressContainer);
        layoutPaymentContainer.removeAllViews();
        boolean hasGifts = getIntent().getBooleanExtra(Constants.HAS_GIFTS, false);
        View checkoutProgressView;
        if (hasGifts) {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.gift), getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{0, 1, 2};
            int selectedPos = 3;
            checkoutProgressView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        } else {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{0, 1};
            int selectedPos = 2;
            checkoutProgressView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        }
        if (checkoutProgressView != null) layoutPaymentContainer.addView(checkoutProgressView, 0);
    }

    private void renderFooter(boolean refresh) {
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, mOrderDetails.getFormattedFinalTotal(),
                getString(isCreditCardPayment() ? R.string.placeOrderAndPayCaps : R.string.placeOrderCaps),
                false);
        if (!refresh) {
            layoutCheckoutFooter.setOnClickListener(new DuplicateClickAware(mElapsedTime) {
                @Override
                public void onActualClick(View view) {
                    if (TextUtils.isEmpty(mSelectedPaymentMethod)) {
                        showToast(getString(R.string.missingPaymentMethod));
                        return;
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.PAYMENT_MODE, mSelectedPaymentMethod);
                    map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
                    trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_CLICKED, map, null, null, false, true);

                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (isCreditCardPayment()
                            && prefs.getBoolean(Constants.SHOW_PAYMENT_OPTIONS_KNOW_MORE, true)) {
                        PaymentOrderInfoDialog dialog = PaymentOrderInfoDialog.newInstance(
                                Constants.KNOW_MORE_DIALOG_ID,
                                getIntent().getStringExtra(Constants.NEW_FLOW_URL));
                        dialog.show(getSupportFragmentManager(), getScreenTag() + "#KnowmoreDialog");
                    } else {
                        placeOrder();
                    }
                }
            });
        }
    }

    private void renderPaymentDetails() {
        mActiveVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        mAppliedVoucherCode = getIntent().getStringExtra(Constants.EVOUCHER_CODE);

        paymentTypeList = getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES);

        ArrayList<CreditDetails> creditDetails = getIntent().getParcelableArrayListExtra(Constants.CREDIT_DETAILS);
        renderPaymentMethodsAndSummary(creditDetails);
    }

    private void renderPaymentMethodsAndSummary(@Nullable ArrayList<CreditDetails> creditDetails) {
        // Show invoice and other order details
        LayoutInflater inflater = getLayoutInflater();
        int normalColor = ContextCompat.getColor(this, R.color.uiv3_primary_text_color);
        int secondaryColor = ContextCompat.getColor(this, R.color.uiv3_secondary_text_color);
        int orderTotalLabelColor = ContextCompat.getColor(this, R.color.uiv3_primary_text_color);
        int orderTotalValueColor = ContextCompat.getColor(this, R.color.uiv3_ok_label_color);
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
                Bundle data = new Bundle(1);
                data.putString(Constants.EVOUCHER_CODE, mAppliedVoucherCode);
                showAlertDialog(getString(R.string.removeVoucherHeading),
                        getString(R.string.removeVoucherDesc),
                        DialogButton.YES, DialogButton.CANCEL,
                        Constants.REMOVE_VOUCHER_DIALOG_REQUEST, data,
                        getString(R.string.remove));
            }
        });

        if (!TextUtils.isEmpty(mAppliedVoucherCode)) {
            showVoucherAppliedText(mAppliedVoucherCode);
        } else {
            onVoucherRemoved();
        }

        TextView lblAmountFromWallet = (TextView) findViewById(R.id.lblAmountFromWallet);
        lblAmountFromWallet.setTypeface(faceRobotoRegular);

        boolean isInHDFCPayMode = HDFCPayzappHandler.isInHDFCPayMode(this);
        if (isInHDFCPayMode) {
            // Now check whether Payzapp is actually present
            boolean hasHdfc = false;
            for (PaymentType paymentType : paymentTypeList) {
                if (paymentType.getValue().equals(Constants.HDFC_POWER_PAY)) {
                    hasHdfc = true;
                    break;
                }
            }
            isInHDFCPayMode = hasHdfc;
        }
        for (PaymentType paymentType : paymentTypeList) {
            if (paymentType.getValue().equals(Constants.PAYUMONEY_WALLET)) {
                isPayUOptionVisible = true;
                break;
            }
        }

        PaymentMethodsView paymentMethodsView = (PaymentMethodsView) findViewById(R.id.layoutPaymentOptions);
        paymentMethodsView.removeAllViews();

        if (mOrderDetails.getFinalTotal() <= 0) {
            lblAmountFromWallet.setVisibility(View.VISIBLE);
            mSelectedPaymentMethod = paymentTypeList.get(0).getValue();
        } else {
            lblAmountFromWallet.setVisibility(View.GONE);

            paymentMethodsView.setPaymentMethods(paymentTypeList, false, isInHDFCPayMode);
        }
        renderCheckOutProgressView();
    }

    private void onVoucherApplied(String voucher, OrderDetails orderDetails,
                                  ArrayList<CreditDetails> creditDetails) {
        if (!TextUtils.isEmpty(voucher)) {
            showVoucherAppliedText(voucher);
            mOrderDetails = orderDetails;
            renderPaymentMethodsAndSummary(creditDetails);
            renderFooter(true);
        }
    }

    private void showVoucherAppliedText(String voucher) {
        mTxtApplyVoucher.setVisibility(View.GONE);
        mTxtRemoveVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setText(getString(R.string.evoucher_applied_format, voucher));
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.GONE);
        }
        mAppliedVoucherCode = voucher;
    }

    private void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<PostVoucherApiResponseContent>> call =
                    bigBasketApiService.postVoucher(getCurrentScreenName(), mPotentialOrderId, voucherCode);
            call.enqueue(new BBNetworkCallback<ApiResponse<PostVoucherApiResponseContent>>(this) {
                @Override
                public void onSuccess(ApiResponse<PostVoucherApiResponseContent> postVoucherApiResponse) {
                    switch (postVoucherApiResponse.status) {
                        case 0:
                            onVoucherSuccessfullyApplied(voucherCode,
                                    postVoucherApiResponse.apiResponseContent.orderDetails,
                                    postVoucherApiResponse.apiResponseContent.creditDetails);
                            break;
                        default:
                            HashMap<String, String> map = new HashMap<>();
                            handler.sendEmptyMessage(postVoucherApiResponse.status, postVoucherApiResponse.message);
                            map.put(TrackEventkeys.FAILURE_REASON, postVoucherApiResponse.message);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, map);
                            break;
                    }
                }

                @Override
                public void onFailure(int httpErrorCode, String msg) {
                    super.onFailure(httpErrorCode, msg);
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
                }

                @Override
                public void onFailure(Throwable t) {
                    super.onFailure(t);
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
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
        } else {
            handler.sendOfflineError();
        }
    }

    private void removeVoucher() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<PostVoucherApiResponseContent>> call =
                bigBasketApiService.removeVoucher(getCurrentScreenName(), mPotentialOrderId);
        call.enqueue(new BBNetworkCallback<ApiResponse<PostVoucherApiResponseContent>>(this) {
            @Override
            public void onSuccess(ApiResponse<PostVoucherApiResponseContent> removeVoucherApiResponse) {
                switch (removeVoucherApiResponse.status) {
                    case 0:
                        Toast.makeText(getCurrentActivity(),
                                getString(R.string.voucherWasRemoved), Toast.LENGTH_SHORT).show();
                        onVoucherRemoved(removeVoucherApiResponse.apiResponseContent.orderDetails,
                                removeVoucherApiResponse.apiResponseContent.creditDetails);
                        break;
                    default:
                        handler.sendEmptyMessage(removeVoucherApiResponse.status,
                                removeVoucherApiResponse.message);
                        break;
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

    /**
     * Callback when the voucher has been successfully applied
     */
    private void onVoucherSuccessfullyApplied(String voucherCode, OrderDetails orderDetails,
                                              ArrayList<CreditDetails> creditDetails) {
        onVoucherApplied(voucherCode, orderDetails, creditDetails);
    }

    private void onVoucherRemoved() {
        mAppliedVoucherCode = null;
        mTxtApplyVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setVisibility(View.GONE);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.VISIBLE);
        }
    }

    private void onVoucherRemoved(OrderDetails orderDetails,
                                  ArrayList<CreditDetails> creditDetails) {
        onVoucherRemoved();
        mOrderDetails = orderDetails;
        renderPaymentMethodsAndSummary(creditDetails);
        renderFooter(true);
    }

    private boolean isCreditCardPayment() {
        return mSelectedPaymentMethod != null &&
                (mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY) ||
                        mSelectedPaymentMethod.equals(Constants.PAYU) ||
                        mSelectedPaymentMethod.equals(Constants.MOBIKWIK_WALLET) ||
                        mSelectedPaymentMethod.equals(Constants.PAYTM_WALLET) ||
                        mSelectedPaymentMethod.equals(Constants.PAYUMONEY_WALLET));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        boolean handled = false;
        if (mOrdersCreated != null) {
            ValidatePaymentRequest validatePaymentRequest =
                    new ValidatePaymentRequest(mTxnId, mOrdersCreated.get(0).getOrderNumber(),
                            mPotentialOrderId, mSelectedPaymentMethod);
            validatePaymentRequest.setFinalTotal(mOrderAmount);
            ValidatePayment validatePayment = new ValidatePayment<>(this, validatePaymentRequest);
            handled = validatePayment.onActivityResult(requestCode, resultCode, data);
        }
        if (!handled) {
            switch (resultCode) {
                case NavigationCodes.VOUCHER_APPLIED:
                    if (data != null) {
                        String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
                        if (!TextUtils.isEmpty(voucherCode)) {
                            applyVoucher(voucherCode);
                        }
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

    /**
     * checking the payment type and based on the calling placeOrder API.
     */
    private void placeOrder() {
        showProgressDialog(isCreditCardPayment() ? getString(R.string.placeOrderPleaseWait) : getString(R.string.please_wait),
                false);
        if (Constants.HDFC_POWER_PAY.equals(mSelectedPaymentMethod)) {
            placeOrderWithPayZappPaymentMethod();
        } else {
            placeOrderWithPrePaymentMethod();
        }
    }

    /**
     * placing order with the payment method apart from PayZapp
     */
    private void placeOrderWithPrePaymentMethod() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Call<OldApiResponse<PlaceOrderApiPrePaymentResponseContent>> call =
                bigBasketApiService.placeOrderWithPrePayment(getCurrentScreenName(), mPotentialOrderId, mSelectedPaymentMethod);
        call.enqueue(new BBNetworkCallback<OldApiResponse<PlaceOrderApiPrePaymentResponseContent>>(this) {
            @Override
            public void onSuccess(OldApiResponse<PlaceOrderApiPrePaymentResponseContent> placeOrderApiPrePaymentResponse) {
                if (placeOrderApiPrePaymentResponse.status.equals(Constants.OK)) {
                    if (placeOrderApiPrePaymentResponse.apiResponseContent.postParams != null) {
                        mPaymentParams = placeOrderApiPrePaymentResponse.apiResponseContent.postParams;
                    }
                    postOrderCreation(placeOrderApiPrePaymentResponse.apiResponseContent.orders,
                            placeOrderApiPrePaymentResponse.apiResponseContent.addMoreLink,
                            placeOrderApiPrePaymentResponse.apiResponseContent.addMoreMsg);
                } else {
                    handler.sendEmptyMessage(placeOrderApiPrePaymentResponse.getErrorTypeAsInt(), placeOrderApiPrePaymentResponse.message);
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

    /****
     * method to order using PayZapp payment parameters
     */
    private void placeOrderWithPayZappPaymentMethod() {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Call<OldApiResponse<PlaceOrderApiPayZappResponseContent>> call =
                bigBasketApiService.placeOrderWithPayZapp(getCurrentScreenName(), mPotentialOrderId, mSelectedPaymentMethod);
        call.enqueue(new BBNetworkCallback<OldApiResponse<PlaceOrderApiPayZappResponseContent>>(this) {
            @Override
            public void onSuccess(OldApiResponse<PlaceOrderApiPayZappResponseContent> placeOrderApiPayZappResponseContent) {
                if (placeOrderApiPayZappResponseContent.status.equals(Constants.OK)) {
                    if (placeOrderApiPayZappResponseContent.apiResponseContent.payzappPostParams != null) {
                        mPayzappPostParams = placeOrderApiPayZappResponseContent.apiResponseContent.payzappPostParams;
                    }
                    postOrderCreation(placeOrderApiPayZappResponseContent.apiResponseContent.orders,
                            placeOrderApiPayZappResponseContent.apiResponseContent.addMoreLink,
                            placeOrderApiPayZappResponseContent.apiResponseContent.addMoreMsg);
                } else {
                    handler.sendEmptyMessage(placeOrderApiPayZappResponseContent.getErrorTypeAsInt(), placeOrderApiPayZappResponseContent.message);
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


    private void postOrderCreation(ArrayList<Order> orders, String addMoreLink,
                                   String addMoreMsg) {
        ((CartInfoAware) getCurrentActivity()).markBasketDirty();

        if (isCreditCardPayment()) {
            mOrdersCreated = orders;
            mAddMoreLink = addMoreLink;
            mAddMoreMsg = addMoreMsg;
            startPrepaymentProcessing(null);
        } else {
            showOrderThankyou(orders, addMoreLink, addMoreMsg);
        }
    }

    private void showOrderThankyou(ArrayList<Order> orders, String addMoreLink, String addMoreMsg) {
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
        setCurrentScreenName(TrackEventkeys.CO_PAYMENT);

        Intent invoiceIntent = new Intent(this, OrderThankyouActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        invoiceIntent.putExtra(Constants.ADD_MORE_LINK, addMoreLink);
        invoiceIntent.putExtra(Constants.ADD_MORE_MSG, addMoreMsg);

        // Empty all the parameters to free up some memory
        mActiveVouchersList = null;
        paymentTypeList = null;
        mPotentialOrderId = null;
        mTxtApplyVoucher = null;
        mTxtRemoveVoucher = null;
        mTxtApplicableVoucherCount = null;
        mAppliedVoucherCode = null;
        mSelectedPaymentMethod = null;
        mOrderDetails = null;
        mTxnId = null;
        mAddMoreLink = null;
        mAddMoreMsg = null;
        mElapsedTime = null;
        mOrderAmount = null;

        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    private void startPrepaymentProcessing(Bundle savedInstanceState) {
        mIsPrepaymentProcessingStarted = true;
        final View paymentInProgressView = findViewById(R.id.layoutPaymentInProgress);
        paymentInProgressView.setVisibility(View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ((TextView) findViewById(R.id.lblOrderPlaced)).setTypeface(faceRobotoRegular);

        //getting values from the bundle and setting the payment parameters
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PAYMENT_PARAMS)) {
                try {
                    Gson gson = new Gson();
                    Type stringStringMap = new TypeToken<HashMap<String, String>>() {
                    }.getType();
                    mPaymentParams = gson.fromJson(savedInstanceState.getString(PAYMENT_PARAMS), stringStringMap);
                } catch (Exception e) {
                    Crashlytics.logException(new ClassCastException(
                            "Exception while getting values from bundle"));
                }
            }
            if (savedInstanceState.containsKey(PAYZAPP_PAYMENT_PARAMS)) {
                try {
                    mPayzappPostParams = savedInstanceState.getParcelable(PAYZAPP_PAYMENT_PARAMS);
                } catch (Exception e) {
                    Crashlytics.logException(new ClassCastException(
                            "Exception while getting values from bundle"));
                }
            }
        }
        mOrderPrepaymentProcessingTask =
                new OrderPrepaymentProcessingTask<PaymentSelectionActivity>(this,
                        mPotentialOrderId, mOrdersCreated.get(0).getOrderNumber(),
                        mSelectedPaymentMethod, false, false, isPayUOptionVisible) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mIsPrepaymentProcessingStarted = true;
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);
                        if (isPaused() || isCancelled() || isSuspended()) {
                            return;
                        }
                        mIsPrepaymentProcessingStarted = false;
                        if (!success) {
                            if (errorResponse != null) {
                                if (errorResponse.isException()) {
                                    //TODO: Possible network error retry
                                    getHandler().handleRetrofitError(errorResponse.getThrowable(), false);
                                } else if (errorResponse.getErrorType() == ErrorResponse.HTTP_ERROR) {
                                    getHandler().handleHttpError(errorResponse.getCode(),
                                            errorResponse.getMessage(), false);
                                } else {
                                    getHandler().sendEmptyMessage(errorResponse.getCode(),
                                            errorResponse.getMessage(), false);
                                }
                            } else {
                                //Should never happen
                                Crashlytics.logException(new IllegalStateException(
                                        "OrderPreprocessing error without error response"));
                            }
                        }
                        mOrderPrepaymentProcessingTask = null;
                    }
                };
        /**
         * setting the payment parameters
         */
        mOrderPrepaymentProcessingTask.setPaymentParams(mPaymentParams);
        mOrderPrepaymentProcessingTask.setPayZappPaymentParams(mPayzappPostParams);

        if (savedInstanceState != null
                && savedInstanceState.getBoolean(IS_PREPAYMENT_TASK_PAUSED, false)) {
            mOrderPrepaymentProcessingTask.pause();
        } else {
            mOrderPrepaymentProcessingTask.setMinDuration(5000);
        }
        mOrderPrepaymentProcessingTask.execute();
    }

    @Override
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mOrderAmount = amount;
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, @NonNull ArrayList<Order> orders) {
        mOrdersCreated = orders;
        if (status || msg == null) {
            showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
        } else {
            // Show a message and then take to Order thank-you page
            showAlertDialog(null, msg, Constants.SOURCE_PLACE_ORDER_DIALOG_REQUEST);
        }
    }

    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case Constants.REMOVE_VOUCHER_DIALOG_REQUEST:
                removeVoucher();
                break;
            case Constants.SOURCE_PLACE_ORDER_DIALOG_REQUEST:
                showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                break;
            case Constants.PREPAYMENT_ABORT_CONFIRMATION_DIALOG:
                String txnId = null;
                mIsPrepaymentAbortInitiated = false;
                if (mOrderPrepaymentProcessingTask != null) {
                    mOrderPrepaymentProcessingTask.cancel(true);
                    txnId = mOrderPrepaymentProcessingTask.getTransactionId();
                    mOrderPrepaymentProcessingTask = null;
                }
                mIsPrepaymentProcessingStarted = false;
                String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
                if (!TextUtils.isEmpty(txnId)) {
                    ValidatePaymentRequest validatePaymentRequest =
                            new ValidatePaymentRequest(txnId, fullOrderId, mPotentialOrderId,
                                    null);  // Passing payment method as null to convert it to COD
                    new ValidatePayment<>(this, validatePaymentRequest).validate(null);
                } else {
                    showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                }
                break;
            default:
                super.onPositiveButtonClicked(sourceName, valuePassed);
        }
    }

    @Override
    protected void onNegativeButtonClicked(int requestCode, Bundle data) {
        switch (requestCode) {
            case Constants.PREPAYMENT_ABORT_CONFIRMATION_DIALOG:
                mIsPrepaymentAbortInitiated = false;
                if (mOrderPrepaymentProcessingTask != null
                        && mOrderPrepaymentProcessingTask.isPaused()
                        && !mOrderPrepaymentProcessingTask.isCancelled()) {
                    mOrderPrepaymentProcessingTask.resume();
                } else {
                    startPrepaymentProcessing(null);
                }
                break;
            default:
                super.onNegativeButtonClicked(requestCode, data);
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

    @Override
    public void onPaymentOptionSelected(String paymentTypeValue) {
        mSelectedPaymentMethod = paymentTypeValue;
        renderFooter(true);
    }

    @Override
    public void onKnowMoreConfirmed(int id, boolean isPositive) {
        if (id == Constants.KNOW_MORE_DIALOG_ID) {
            if (isPositive) {
                placeOrder();
            } else {
                trackEvent(PLACE_ORDER_KNOW_MORE_DIALOG_CANCEL_CLICKED, null);
            }
        }
    }

    @Override
    public void onKnowMoreCancelled(int id) {
        if (id == Constants.KNOW_MORE_DIALOG_ID) {
            trackEvent(PLACE_ORDER_KNOW_MORE_DIALOG_CANCELLED, null);
        }
    }

    public static class PaymentOrderInfoDialog extends AppCompatDialogFragment
            implements Dialog.OnClickListener {

        private static final String ARG_DIALOG_IDENTIFIER = "arg_dialog_identifier";
        private static final String ARG_KNOW_MORE_URL = "arg_know_more_url";

        private CheckBox mDonotShowCheckbox;
        private PaymentOptionsKnowMoreDialogCallback callback;
        private String mKnowMoreUrl;

        public static PaymentOrderInfoDialog newInstance(Fragment parentFragment, int dialogId,
                                                         String knowMoreUrl) {

            Bundle args = new Bundle();
            args.putInt(ARG_DIALOG_IDENTIFIER, dialogId);
            args.putString(ARG_KNOW_MORE_URL, knowMoreUrl);

            PaymentOrderInfoDialog fragment = new PaymentOrderInfoDialog();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, dialogId);
            return fragment;
        }

        public static PaymentOrderInfoDialog newInstance(int dialogId,
                                                         String knowMoreUrl) {
            return newInstance(null, dialogId, knowMoreUrl);
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            updateCallBack();
        }

        @Override
        public void setTargetFragment(Fragment fragment, int requestCode) {
            super.setTargetFragment(fragment, requestCode);
            updateCallBack();
        }

        private void updateCallBack() {
            if (getTargetFragment() instanceof PaymentOptionsKnowMoreDialogCallback) {
                callback = (PaymentOptionsKnowMoreDialogCallback) getTargetFragment();
            } else if (getActivity() instanceof PaymentOptionsKnowMoreDialogCallback) {
                callback = (PaymentOptionsKnowMoreDialogCallback) getActivity();
            } else {
                callback = null;
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            TextView msg = (TextView) getDialog().findViewById(R.id.message);
            msg.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());

            SpannableStringBuilder spannableBuilder =
                    new SpannableStringBuilder(getString(R.string.payment_order_info));

            mKnowMoreUrl = null;

            if (getArguments() != null) {
                mKnowMoreUrl = getArguments().getString(ARG_KNOW_MORE_URL);
            }
            if (!TextUtils.isEmpty(mKnowMoreUrl)) {
                spannableBuilder.append(' ');
                ClickableSpan knowMoreClickable = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Activity activity = getActivity();
                        if (activity == null) {
                            return;
                        }
                        if (activity instanceof TrackingAware) {
                            ((TrackingAware) activity).trackEvent(
                                    TrackingAware.PLACE_ORDER_KNOW_MORE_LINK_CLICKED, null);
                        }
                        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                        intent.putExtra(Constants.WEBVIEW_URL, mKnowMoreUrl);
                        intent.putExtra(Constants.WEBVIEW_TITLE, "");
                        startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.setColor(ContextCompat.getColor(getActivity(), R.color.uiv3_link_color));
                        ds.setUnderlineText(true);
                    }
                };
                int start = spannableBuilder.length();
                spannableBuilder.append(getString(R.string.know_more));
                spannableBuilder.setSpan(knowMoreClickable, start, spannableBuilder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            msg.setText(spannableBuilder);
            msg.setMovementMethod(LinkMovementMethod.getInstance());

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //NOTE: DO NOT USE getLayoutInflater(savedInstanceState), causes StackOverflowError
            View view = getActivity().getLayoutInflater().inflate(
                    R.layout.fragment_payment_order_info, null, false);
            mDonotShowCheckbox = (CheckBox) view.findViewById(R.id.dont_show_check_box);

            AlertDialog.Builder alertDiaBuilder = new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setPositiveButton(R.string.lblContinue, this)
                    .setNegativeButton(R.string.cancel, this);

            AlertDialog dialog = alertDiaBuilder.create();
            WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.gravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
            dialog.getWindow().setBackgroundDrawableResource(R.color.white);
            return dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (which == AlertDialog.BUTTON_POSITIVE) {
                if (mDonotShowCheckbox != null && mDonotShowCheckbox.isChecked()) {
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(
                                    activity.getApplicationContext());
                    prefs.edit().putBoolean(Constants.SHOW_PAYMENT_OPTIONS_KNOW_MORE, false).apply();
                }
            }
            if (callback != null) {
                Bundle args = getArguments();
                callback.onKnowMoreConfirmed(
                        args != null ? args.getInt(ARG_DIALOG_IDENTIFIER, 0) : 0,
                        which == AlertDialog.BUTTON_POSITIVE);
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (callback != null) {
                Bundle args = getArguments();
                callback.onKnowMoreCancelled(
                        args != null ? args.getInt(ARG_DIALOG_IDENTIFIER, 0) : 0);
            }
        }
    }

    private class OnShowAvailableVouchersListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent availableVoucherListActivity = new Intent(getCurrentActivity(),
                    AvailableVoucherListActivity.class);
            availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
            startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
        }
    }
}
