package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayNowParamsResponse;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.factory.payment.PayNowPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.PayNowDetail;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;

/**
 * Don't do the mistake of moving this to Fragment. I've done all that, and these 3rd Party SDKs
 * don't handle fragments well.
 */
public class PayNowActivity extends BackButtonActivity implements OnPaymentValidationListener,
        CityListDisplayAware, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mOrderId;
    @Nullable
    private String mTxnId;
    private String mFinalTotal;
    private PayNowPrepaymentProcessingTask<PayNowActivity> mPayNowPrepaymentProcessingTask;
    private boolean isPayUOptionVisible;
    private ViewGroup mProgressLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_PAY_NOW);
        trackEvent(TrackingAware.PAY_NOW_SHOWN, null);
        setTitle(getString(R.string.payNow));

        mOrderId = getIntent().getStringExtra(Constants.ORDER_ID);
        mProgressLayout = (ViewGroup) findViewById(R.id.layoutLoading);
        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        getPayNowParams();
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
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mTxnId == null) {
            mTxnId = savedInstanceState.getString(Constants.TXN_ID);
        }
        if (mSelectedPaymentMethod == null) {
            mSelectedPaymentMethod = savedInstanceState.getString(Constants.PAYMENT_METHOD);
        }
        if (mFinalTotal == null) {
            mFinalTotal = savedInstanceState.getString(Constants.FINAL_TOTAL);
        }
    }

    private void getPayNowParams() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetPayNowParamsResponse>> call =
                bigBasketApiService.getPayNowDetails(getPreviousScreenName(), mOrderId, "yes", "yes", "yes", "yes", "yes");
        call.enqueue(new BBNetworkCallback<ApiResponse<GetPayNowParamsResponse>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetPayNowParamsResponse> payNowParamsApiResponse) {

                switch (payNowParamsApiResponse.status) {
                    case 0:
                        displayPayNowSummary(payNowParamsApiResponse.apiResponseContent.amount,
                                payNowParamsApiResponse.apiResponseContent.payNowDetailList,
                                payNowParamsApiResponse.apiResponseContent.paymentTypes);
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
                                      ArrayList<PaymentType> paymentTypes) {
        displayOrderSummary(payNowDetailList);
        displayPaymentMethods(paymentTypes);
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.payNow), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayNow(amount);
            }
        });
    }

    private void startPayNow(String total) {
        if (mSelectedPaymentMethod == null) return;
        mFinalTotal = total;
        initPayNowPrepaymentProcessingTask();
    }


    public void initPayNowPrepaymentProcessingTask() {
        mPayNowPrepaymentProcessingTask = new PayNowPrepaymentProcessingTask<PayNowActivity>(this,
                null, mOrderId, mSelectedPaymentMethod, true, false, isPayUOptionVisible) {
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
                    if(Constants.BB_WALLET.equals(paymentMethod)) {
                        onPayNowSuccess();
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
            PaytmResponseHolder.processPaytmResponse(this);
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
        if (mOrderId != null) {
            ValidatePaymentRequest validatePaymentRequest =
                    new ValidatePaymentRequest(mTxnId, mOrderId, null, mSelectedPaymentMethod);
            validatePaymentRequest.setFinalTotal(mFinalTotal);
            validatePaymentRequest.setIsPayNow(true);
            handled = new ValidatePayment<>(this, validatePaymentRequest)
                    .onActivityResult(requestCode, resultCode, data);
        }
        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onPayNowSuccess() {
        HashMap<String, String> attrs = new HashMap<>();
        attrs.put(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        trackEvent(TrackingAware.PAY_NOW_DONE, attrs);
        Intent intent = new Intent(this, PayNowThankyouActivity.class);
        intent.putExtra(Constants.ORDER_ID, mOrderId);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private void onPayNowFailure() {
        UIUtil.showPaymentFailureDlg(this);
    }

    @Override
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mFinalTotal = amount;
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, @Nullable ArrayList<Order> orders) {
        if (status) {
            onPayNowSuccess();
        } else {
            onPayNowFailure();
        }
    }

    private void displayOrderSummary(ArrayList<PayNowDetail> payNowDetailList) {
        LayoutInflater inflater = getLayoutInflater();

        // Show order & invoice details
        int normalColor = ContextCompat.getColor(this, R.color.uiv3_primary_text_color);

        ViewGroup layoutOrderSummaryInfo = (ViewGroup) findViewById(R.id.layoutOrderSummaryInfo);

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
        for (PaymentType paymentType : paymentTypeList) {
            if (paymentType.getValue().equals(Constants.PAYUMONEY_WALLET)) {
                isPayUOptionVisible = true;
                break;
            }
        }
        PaymentMethodsView paymentMethodsView = (PaymentMethodsView) findViewById(R.id.layoutPaymentOptions);
        paymentMethodsView.setPaymentMethods(paymentTypeList, 0, true, false);
    }

    @Override
    public void showProgressDialog(String msg, boolean cancelable, boolean isDeterminate) {
        if (mProgressLayout == null) return;
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressDialog() {
        if (mProgressLayout == null) return;
        mProgressLayout.setVisibility(View.GONE);
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
