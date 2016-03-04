package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.factory.payment.OrderPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketRetryMessageHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by manu on 4/2/16.
 */
public class PostOrderCreationActivity extends BaseActivity implements PaymentTxnInfoAware, OnPaymentValidationListener {
    private OrderPrepaymentProcessingTask<PostOrderCreationActivity> mOrderPrepaymentProcessingTask;
    private boolean mIsPrepaymentAbortInitiated;
    private boolean isPayUOptionVisible;
    private PayzappPostParams mPayzappPostParams;
    private HashMap<String, String> mPaymentParams;
    private boolean mIsPrepaymentProcessingStarted;
    private String mPotentialOrderId;
    private ArrayList<Order> mOrdersCreated;
    private String mSelectedPaymentMethod;
    private String mTxnId;
    private String mOrderAmount;
    private String mAddMoreLink, mAddMoreMsg;
    private ValidatePaymentRequest validatePaymentRequest;

    //variables for payment results
    private String REQUEST_CODE = "request_code";
    private String RESULT_CODE = "result_code";
    private String INTENT_DATA = "intent_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_order_creation);
        setCurrentScreenName(TrackEventkeys.CO_PAYMENT_POST_ORDER_CREATION);

        if (savedInstanceState != null) {
            mTxnId = savedInstanceState.getString(Constants.TXN_ID);
            mIsPrepaymentProcessingStarted =
                    savedInstanceState.getBoolean(PaymentSelectionActivity.IS_PREPAYMENT_TASK_STARTED, false);
            mIsPrepaymentAbortInitiated =
                    savedInstanceState.getBoolean(PaymentSelectionActivity.IS_PREPAYMENT_ABORT_INITIATED, false);
            mOrderAmount = savedInstanceState.getString(Constants.AMOUNT);

        }

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mPotentialOrderId = bundle.getString(Constants.P_ORDER_ID);
            mOrdersCreated = bundle.getParcelableArrayList(Constants.ORDERS);
            mSelectedPaymentMethod = bundle.getString(Constants.PAYMENT_METHOD);
            isPayUOptionVisible = bundle.getBoolean(Constants.PAYU_SELECTED, false);
            try {
                Gson gson = new Gson();
                Type stringStringMap = new TypeToken<HashMap<String, String>>() {
                }.getType();
                mPaymentParams = gson.fromJson(bundle.getString(PaymentSelectionActivity.PAYMENT_PARAMS), stringStringMap);
            } catch (Exception e) {
                Crashlytics.logException(new ClassCastException(
                        "Exception while getting values from bundle"));
            }
            mPayzappPostParams = bundle.getParcelable(PaymentSelectionActivity.PAYZAPP_PAYMENT_PARAMS);
            mAddMoreLink = bundle.getString(Constants.ADD_MORE_LINK);
            mAddMoreMsg = bundle.getString(Constants.ADD_MORE_MSG);
            if (savedInstanceState != null) {
                //Start the payment processing only if it was kept pending before going to background
                if (isPaymentPending()) {
                    startPrepaymentProcessing(savedInstanceState);
                }
            } else {
                startPrepaymentProcessing(null);
            }
        }
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PAYMENT_SCREEN;
    }

    private void startPrepaymentProcessing(Bundle savedInstanceState) {
        mIsPrepaymentProcessingStarted = true;
        final View paymentInProgressView = findViewById(R.id.layoutPaymentInProgress);
        paymentInProgressView.setVisibility(View.VISIBLE);

        mOrderPrepaymentProcessingTask =
                new OrderPrepaymentProcessingTask<PostOrderCreationActivity>(this,
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
                && savedInstanceState.getBoolean(PaymentSelectionActivity.IS_PREPAYMENT_TASK_PAUSED, false)) {
            mOrderPrepaymentProcessingTask.pause();
        } else {
            mOrderPrepaymentProcessingTask.setMinDuration(3000);
        }

        mOrderPrepaymentProcessingTask.execute();
    }

    @Override
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mOrderAmount = amount;
    }


    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
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
                    validatePaymentRequest =
                            new ValidatePaymentRequest(txnId, fullOrderId, mPotentialOrderId,
                                    null);  // Passing payment method as null to convert it to COD
                    HashMap<String, String> additionalParams = null;
                    if(mSelectedPaymentMethod != null) {
                        switch (mSelectedPaymentMethod) {
                            case Constants.HDFC_POWER_PAY:
                                additionalParams = new HashMap<>(3);
                                additionalParams.put(Constants.ERR_RES_CODE, "-1");
                                additionalParams.put(Constants.ERR_RES_DESC, "User cancelled");
                                additionalParams.put(Constants.STATUS, "0");
                                break;
                        }
                    }
                    new ValidatePayment<>(this, validatePaymentRequest,
                            new BigBasketRetryMessageHandler(this, this))
                            .validate(additionalParams);
                } else {
                    showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                }
                break;
            case ApiErrorCodes.INVALID_FIELD://error that cannot be retried and unexpected..logging the order number in Crashlytics
                if (mOrdersCreated != null) {
                    Crashlytics.log(ApiErrorCodes.INVALID_FIELD + mOrdersCreated.get(0).getOrderNumber());
                }
            case Constants.OFFLINE_PAYMENT_SHOW_THANKYOU_ABORT_CONFIRMATION_DIALOG://retry option,validating payment again
                /**
                 * if the payment method is Paytm pass the bundle to paytmresponse handler
                 * if payment methods is others, call the onActivity result passing the request_code, result_code,and the intent data.
                 */
                if (mSelectedPaymentMethod.equalsIgnoreCase(Constants.PAYTM_WALLET)) {
                    PaytmResponseHolder.processPaytmRetryResponse(this, new BigBasketRetryMessageHandler(this, this),valuePassed);
                } else if (valuePassed != null) {
                    onActivityResult(valuePassed.getInt(REQUEST_CODE), valuePassed.getInt(RESULT_CODE), (Intent) valuePassed.getParcelable(INTENT_DATA));
                }
                trackEvent(TrackingAware.ORDER_VALIDATION_RETRY_SELECTED, null);
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
            case Constants.OFFLINE_PAYMENT_SHOW_THANKYOU_ABORT_CONFIRMATION_DIALOG:
                showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                break;
            default:
                super.onNegativeButtonClicked(requestCode, data);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
        Workaround to avoid IllegalStateException: Can not perform this action after onSaveInstanceState
        Invoke onStateNotSaved() before performing fragment operations,
        super.onActivityResult() would invoke the same and avoid this error
        Otherwise fragment operations have to be performed after onResumeFragments call
        */

        onStateNotSaved();
        setSuspended(false);
        boolean handled = false;
        if (mOrdersCreated != null) {
            Bundle bundle = new Bundle(3);
            bundle.putInt(REQUEST_CODE, requestCode);
            bundle.putInt(RESULT_CODE, resultCode);
            bundle.putParcelable(INTENT_DATA, data);

            validatePaymentRequest =
                    new ValidatePaymentRequest(mTxnId, mOrdersCreated.get(0).getOrderNumber(),
                            mPotentialOrderId, mSelectedPaymentMethod);
            validatePaymentRequest.setFinalTotal(mOrderAmount);
            ValidatePayment validatePayment = new ValidatePayment<>(this, validatePaymentRequest, new BigBasketRetryMessageHandler(this, this, bundle));
            handled = validatePayment.onActivityResult(requestCode, resultCode, data);
            if (!handled) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mTxnId != null) {
            outState.putString(Constants.TXN_ID, mTxnId);
        }
        if (mOrderPrepaymentProcessingTask != null) {
            outState.putBoolean(PaymentSelectionActivity.IS_PREPAYMENT_TASK_STARTED, mIsPrepaymentProcessingStarted);
            outState.putBoolean(PaymentSelectionActivity.IS_PREPAYMENT_TASK_PAUSED, mOrderPrepaymentProcessingTask.isPaused());
            outState.putBoolean(PaymentSelectionActivity.IS_PREPAYMENT_ABORT_INITIATED, mIsPrepaymentAbortInitiated);
        }
        if (mOrderAmount != null) {
            outState.putString(Constants.AMOUNT, mOrderAmount);
        }

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, @Nullable ArrayList<Order> orders) {
        mOrdersCreated = orders;
        if (status || msg == null) {
            showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
        } else {
            // Show a message and then take to Order thank-you page
            showAlertDialog(null, msg, Constants.SOURCE_PLACE_ORDER_DIALOG_REQUEST);
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
        setCurrentScreenName(TrackEventkeys.CO_PAYMENT_POST_ORDER_CREATION);

        Intent invoiceIntent = new Intent(this, OrderThankyouActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        invoiceIntent.putExtra(Constants.ADD_MORE_LINK, addMoreLink);
        invoiceIntent.putExtra(Constants.ADD_MORE_MSG, addMoreMsg);

        // Empty all the parameters to free up some memory
        mPotentialOrderId = null;
        mSelectedPaymentMethod = null;
        mTxnId = null;
        mAddMoreLink = null;
        mAddMoreMsg = null;
        mOrderAmount = null;

        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrderPrepaymentProcessingTask != null && !mIsPrepaymentAbortInitiated) {
            mOrderPrepaymentProcessingTask.resume();
        }
        if (PaytmResponseHolder.hasPendingTransaction()) {
            PaytmResponseHolder.processPaytmResponse(this, new BigBasketRetryMessageHandler(this, this));
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
}
