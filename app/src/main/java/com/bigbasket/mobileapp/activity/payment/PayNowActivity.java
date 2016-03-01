package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayNowParamsResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.factory.payment.PayNowPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.BigBasketRetryMessageHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.PayNowDetail;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.model.wallet.WalletOption;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Don't do the mistake of moving this to Fragment. I've done all that, and these 3rd Party SDKs
 * don't handle fragments well.
 */
public class PayNowActivity extends BackButtonActivity implements OnPaymentValidationListener,
        CityListDisplayAware, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    private static final String PAY_NOW_DETAILS = "paynow_details";
    private static final String TXN_ORDER_ID = "txn_order_id";
    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mOrderId;
    @Nullable
    private String mTxnId;
    private String mFinalTotal;
    private PayNowPrepaymentProcessingTask<PayNowActivity> mPayNowPrepaymentProcessingTask;
    private boolean isPayUOptionVisible;
    private ArrayList<PayNowDetail> mPayNowDetailList;
    private ArrayList<PaymentType> mPaymentTypes;
    private String mTxnOrderId;
    private CheckBox walletOptionsCheckBox;
    private WalletOption mWalletOption;
    private PaymentMethodsView paymentMethodsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_PAY_NOW);
        trackEvent(TrackingAware.SINGLE_PAY_NOW_SHOWN, null);
        setTitle(getString(R.string.payNow));

        paymentMethodsView = (PaymentMethodsView) findViewById(R.id.layoutPaymentOptions);
        walletOptionsCheckBox = (CheckBox) findViewById(R.id.wallet_option_checkbox);
        walletOptionsCheckBox.setTypeface(faceRobotoRegular);
        walletOptionsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPayNowParams(true);
            }
        });

        mOrderId = getIntent().getStringExtra(Constants.ORDER_ID);
        if (savedInstanceState != null) {
            mTxnId = savedInstanceState.getString(Constants.TXN_ID);
            mSelectedPaymentMethod = savedInstanceState.getString(Constants.PAYMENT_METHOD);
            mFinalTotal = savedInstanceState.getString(Constants.FINAL_TOTAL);
            mPayNowDetailList = savedInstanceState.getParcelableArrayList(PAY_NOW_DETAILS);
            mPaymentTypes = savedInstanceState.getParcelableArrayList(Constants.PAYMENT_TYPES);
            mTxnOrderId = savedInstanceState.getString(TXN_ORDER_ID);
            mWalletOption = savedInstanceState.getParcelable(Constants.WALLET_OPTION);
        }
        if (mFinalTotal != null && mPayNowDetailList != null && mPaymentTypes != null) {
            displayPayNowSummary(mFinalTotal, mPayNowDetailList, mPaymentTypes, mWalletOption);
        } else {
            String csPhoneNumber = UIUtil.getCustomerSupportPhoneNumber(this);
            if (TextUtils.isEmpty(csPhoneNumber) || CityManager.isCityDataExpired(this)) {
                //this task is needed to get the phone number for the city
                // to be shown to user in case of issue in payment validation
                new GetCitiesTask<>(this).startTask();
            } else {
                getPayNowParams(false);
            }
        }
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        getPayNowParams(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mTxnId != null) {
            outState.putString(Constants.TXN_ID, mTxnId);
        }
        if (mSelectedPaymentMethod != null) {
            outState.putString(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        }
        if (mFinalTotal != null) {
            outState.putString(Constants.FINAL_TOTAL, mFinalTotal);
        }
        if (mPayNowDetailList != null) {
            outState.putParcelableArrayList(PAY_NOW_DETAILS, mPayNowDetailList);
        }
        if (mPaymentTypes != null) {
            outState.putParcelableArrayList(Constants.PAYMENT_TYPES, mPaymentTypes);
        }
        if (mPayNowPrepaymentProcessingTask != null && mPayNowPrepaymentProcessingTask.getTxnOrderId() != null) {
            outState.putString(TXN_ORDER_ID, mPayNowPrepaymentProcessingTask.getTxnOrderId());
        }
        if (mWalletOption != null) {
            outState.putParcelable(Constants.WALLET_OPTION, mWalletOption);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * @param walletClicked: to differentiate between normal flow call an the call after the checkbox has been clicked
     *                       wallet clicked: true : send 'wallet' key as the state of the checkbox
     *                       false: send the 'wallet' key as 1,
     *                       1: user wants to use BBwallet
     *                       0: user doesnt want to use BBwallet
     */
    private void getPayNowParams(boolean walletClicked) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetPayNowParamsResponse>> call = null;
        if (!walletClicked) {
            call = bigBasketApiService.getPayNowDetails(getPreviousScreenName(), mOrderId, 1, "yes", "yes", "yes", "yes", "yes");
        } else {
            int val = walletOptionsCheckBox.isChecked() ? 1 : 0;
            call = bigBasketApiService.getPayNowDetails(getPreviousScreenName(), mOrderId, val, "yes", "yes", "yes", "yes", "yes");
        }

        call.enqueue(new BBNetworkCallback<ApiResponse<GetPayNowParamsResponse>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetPayNowParamsResponse> payNowParamsApiResponse) {

                switch (payNowParamsApiResponse.status) {
                    case 0:
                        updateParamtersAndUpdateView(payNowParamsApiResponse.apiResponseContent);
                        break;
                    default:
                        handler.sendEmptyMessage(payNowParamsApiResponse.status,
                                payNowParamsApiResponse.message, true);
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

    private void displayPayNowSummary(final String amount, ArrayList<PayNowDetail> payNowDetailList,
                                      ArrayList<PaymentType> paymentTypes, WalletOption walletOption) {
        mFinalTotal = amount;
        mPayNowDetailList = payNowDetailList;
        mPaymentTypes = paymentTypes;
        mWalletOption = walletOption;
        renderWalletOptionCheckbox();
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.payNow), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayNow(mFinalTotal);
            }
        });
    }

    /**
     * render the state of the checkbox based on the response of server
     */
    private void renderWalletOptionCheckbox() {
        String orderPrefix = mWalletOption.getWalletMessage().concat(getString(R.string.balance)).concat(" `");
        String orderValStr = UIUtil.formatAsMoney(Double.parseDouble(mWalletOption.getWalletBalance()));
        int prefixLen = orderPrefix.length();
        SpannableStringBuilder spannableMrp = new SpannableStringBuilder(orderPrefix);
        spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableMrp.append(orderValStr);
        walletOptionsCheckBox.setText(spannableMrp);
        switch (mWalletOption.getWalletState().toLowerCase()) {
            case Constants.DISABLED:
                /*
                user can't click
                by default it is checked
                 */
                walletOptionsCheckBox.setChecked(true);
                walletOptionsCheckBox.setEnabled(false);
                break;
            case Constants.OFF:
                /*
                user can change the option
                by default the option is not checked
                 */
                walletOptionsCheckBox.setChecked(false);
                walletOptionsCheckBox.setEnabled(true);
                break;
            case Constants.ON:
                /*
                user can change the option
                by default the option is checked
                 */
                walletOptionsCheckBox.setChecked(true);
                walletOptionsCheckBox.setEnabled(true);
                break;
            default:
                walletOptionsCheckBox.setVisibility(View.GONE);
        }

        displayOrderSummary(mPayNowDetailList);
        displayPaymentMethods(mPaymentTypes);

    }

    private void updateParamtersAndUpdateView(GetPayNowParamsResponse getPayNowParamsResponse) {
        displayPayNowSummary(getPayNowParamsResponse.amount,
                getPayNowParamsResponse.payNowDetailList,
                getPayNowParamsResponse.paymentTypes,
                getPayNowParamsResponse.walletOption);
    }

    private void startPayNow(String total) {
        mFinalTotal = total;
        initPayNowPrepaymentProcessingTask();
        hideProgressDialog();
    }

    public void initPayNowPrepaymentProcessingTask() {
        /**
         * making the mSelectedpayment method null if the paymentmethods view not visible
         * i.e. payment is being done from wallet.
         */

        if ((paymentMethodsView.getVisibility() != View.VISIBLE)) {
            mSelectedPaymentMethod = null;
        }
        int val = walletOptionsCheckBox.isChecked() ? 1 : 0;
        mPayNowPrepaymentProcessingTask = new PayNowPrepaymentProcessingTask<PayNowActivity>(this,
                null, mOrderId, mSelectedPaymentMethod, true, false, isPayUOptionVisible, val) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog(getString(R.string.please_wait), false);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                hideProgressDialog();
                super.onPostExecute(success);
                if (isPaused() || isCancelled() || isSuspended()) {
                    return;
                }
                if (!success) {
                    if (errorResponse != null) {
                        if (errorResponse.isException()) {
                            //TODO: Possible network error retry
                            getHandler().handleRetrofitError(errorResponse.getThrowable(), false);
                        } else if (errorResponse.getCode() == ErrorResponse.HTTP_ERROR) {
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
                } else {
                    mTxnOrderId = getTxnOrderId();
                    if (TextUtils.isEmpty(paymentMethod)) {
                        onPayNowSuccess(getIntent().<Order>getParcelableArrayListExtra(Constants.ORDER));
                    }
                }
            }
        };
        mPayNowPrepaymentProcessingTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PaytmResponseHolder.hasPendingTransaction()) {
            PaytmResponseHolder.processPaytmResponse(this, new BigBasketRetryMessageHandler(this, this));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPayNowPrepaymentProcessingTask != null) {
            mPayNowPrepaymentProcessingTask.cancel(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        boolean handled = false;
        if (mFinalTotal != null && mSelectedPaymentMethod != null) {
            onStateNotSaved();
            ValidatePaymentRequest validatePaymentRequest =
                    new ValidatePaymentRequest(mTxnId, mTxnOrderId, null, mSelectedPaymentMethod);
            validatePaymentRequest.setFinalTotal(mFinalTotal);
            validatePaymentRequest.setIsPayNow(true);
            handled = new ValidatePayment<>(this, validatePaymentRequest, new BigBasketRetryMessageHandler(this, this))
                    .onActivityResult(requestCode, resultCode, data);
        }
        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void onPayNowSuccess(ArrayList<Order> orders) {
        if (orders != null && orders.size() > 0) {
            HashMap<String, String> attrs = new HashMap<>();
            attrs.put(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
            trackEvent(TrackingAware.PAY_NOW_DONE, attrs);
            Intent intent = new Intent(this, PayNowThankyouActivity.class);
            intent.putExtra(Constants.ORDERS, orders);
            intent.putExtra(Constants.IS_FROM_PAYNOW, true);
            startActivity(intent);
            setResult(NavigationCodes.REFRESH_ORDERS);
        }
        finish();
    }

    private void onPayNowFailure() {
        showAlertDialog(getString(R.string.transactionFailed),
                UIUtil.getPaymentFailureDialogtext(this),
                getString(R.string.ok),
                null,
                Constants.PAYMENT_VALIDATION_FAILED, null);

    }

    @Override
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mFinalTotal = amount;
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, @Nullable ArrayList<Order> orders) {
        if (status) {
            onPayNowSuccess(orders);
        } else {
            onPayNowFailure();
        }
    }

    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case ApiErrorCodes.INVALID_FIELD://error that cannot be retried and unexpected..logging the order number
                if (mPayNowPrepaymentProcessingTask != null && mPayNowPrepaymentProcessingTask.getTxnOrderId() != null) {
                    Map<String, String> eventAttribs = new HashMap<>();
                    eventAttribs.put(Constants.TXT_ORDER_ID, mPayNowPrepaymentProcessingTask.getTxnOrderId());
                    trackEvent(TrackingAware.VALIDATE_PAYMENT_API_ERROR_ORDER_ID, eventAttribs);
                }
            case Constants.OFFLINE_PAYMENT_SHOW_THANKYOU_ABORT_CONFIRMATION_DIALOG://retry option,validating payment again
                if (mPayNowPrepaymentProcessingTask != null && mPayNowPrepaymentProcessingTask.getTxnOrderId() != null) {
                    ValidatePaymentRequest validatePaymentRequest =
                            new ValidatePaymentRequest(mTxnId, mPayNowPrepaymentProcessingTask.getTxnOrderId(), null, mSelectedPaymentMethod);
                    validatePaymentRequest.setFinalTotal(mFinalTotal);
                    validatePaymentRequest.setIsPayNow(true);
                    new ValidatePayment<>(this, validatePaymentRequest, new BigBasketRetryMessageHandler(this, this)).validate(null);
                }
                break;
            case Constants.PAYMENT_VALIDATION_FAILED:
                setResult(NavigationCodes.REFRESH_ORDERS);
                finish();
                break;
            default:
                super.onPositiveButtonClicked(sourceName, valuePassed);
        }
    }

    private void displayOrderSummary(ArrayList<PayNowDetail> payNowDetailList) {
        LayoutInflater inflater = getLayoutInflater();

        // Show order & invoice details
        int normalColor = ContextCompat.getColor(this, R.color.uiv3_primary_text_color);

        ViewGroup layoutOrderSummaryInfo = (ViewGroup) findViewById(R.id.layoutOrderSummaryInfo);
        layoutOrderSummaryInfo.removeAllViews();


        if (payNowDetailList != null && payNowDetailList.size() > 0) {
            for (PayNowDetail payNowDetail : payNowDetailList) {
                View creditDetailRow;
                if (!TextUtils.isEmpty(payNowDetail.getValueType())
                        && payNowDetail.getValueType().equals(Constants.AMOUNT)) {
                    creditDetailRow = UIUtil.getOrderSummaryRow(inflater, payNowDetail.getMsg(),
                            UIUtil.asRupeeSpannable(payNowDetail.getValue(), faceRupee),
                            normalColor, faceRobotoRegular);
                } else {
                    creditDetailRow = UIUtil.getOrderSummaryRow(inflater, payNowDetail.getMsg(),
                            payNowDetail.getValue(),
                            normalColor, faceRobotoRegular);
                }
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

    }

    private void displayPaymentMethods(ArrayList<PaymentType> paymentTypeList) {
        if (paymentTypeList.size() > 0) {
            int i = 0;
            int selectedPaymentMethodPos = 0;
            boolean showDefaultSelection = TextUtils.isEmpty(mSelectedPaymentMethod);
            for (PaymentType paymentType : paymentTypeList) {
                if (paymentType.getValue().equals(Constants.PAYUMONEY_WALLET)) {
                    isPayUOptionVisible = true;
                }
                if (!showDefaultSelection && paymentType.getValue().equals(mSelectedPaymentMethod)) {
                    selectedPaymentMethodPos = i;
                }
                i++;
            }
            paymentMethodsView.setVisibility(View.VISIBLE);
            paymentMethodsView.removeAllViews();
            paymentMethodsView.setPaymentMethods(paymentTypeList, selectedPaymentMethodPos, showDefaultSelection, false);
        } else {
            paymentMethodsView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_pay_now;
    }

    @Override
    public void onPaymentOptionSelected(String paymentTypeValue) {
        mSelectedPaymentMethod = paymentTypeValue;
    }
}
