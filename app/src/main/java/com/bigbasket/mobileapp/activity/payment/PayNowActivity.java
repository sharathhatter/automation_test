package com.bigbasket.mobileapp.activity.payment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayNowParamsResponse;
import com.bigbasket.mobileapp.factory.payment.PayNowPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.PostPaymentProcessor;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.PayNowDetail;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.crashlytics.android.Crashlytics;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.mobikwik.sdk.MobikwikSDK;
import com.mobikwik.sdk.lib.MKTransactionResponse;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

/**
 * Don't do the mistake of moving this to Fragment. I've done all that, and these 3rd Party SDKs
 * don't handle fragments well.
 */
public class PayNowActivity extends BackButtonActivity implements OnPostPaymentListener,
        CityListDisplayAware, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mOrderId;
    @Nullable
    private String mTxnId;
    private double mFinalTotal;
    private PayNowPrepaymentProcessingTask<PayNowActivity> mPayNowPrepaymentProcessingTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NAVIGATION_CTX_PAY_NOW);
        trackEvent(TrackingAware.PAY_NOW_SHOWN, null);
        setTitle(getString(R.string.payNow));

        mOrderId = getIntent().getStringExtra(Constants.ORDER_ID);
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
        if (mFinalTotal != 0) {
            outState.putDouble(Constants.FINAL_TOTAL, mFinalTotal);
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
        if (mFinalTotal == 0) {
            mFinalTotal = savedInstanceState.getDouble(Constants.FINAL_TOTAL);
        }
    }

    private void getPayNowParams() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetPayNowParamsResponse>> call = bigBasketApiService.getPayNowDetails(mOrderId, "yes", "yes", "yes", "yes", "yes");
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
                startPayNow(Double.parseDouble(amount));
            }
        });
    }

    private void startPayNow(double total) {
        mFinalTotal = total;
        if (mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY)) {
            if (handlePermission(Manifest.permission.READ_PHONE_STATE, Constants.PERMISSION_REQUEST_CODE_READ_PHONE_STATE))
                initPayNowPrepaymentProcessingTask();
        } else {
            initPayNowPrepaymentProcessingTask();
        }
    }


    public void initPayNowPrepaymentProcessingTask() {
        mPayNowPrepaymentProcessingTask = new PayNowPrepaymentProcessingTask<PayNowActivity>(this,
                null, mOrderId, mSelectedPaymentMethod, true, false) {
            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (isPaused() || isCancelled() || isSuspended()) {
                    return;
                }
                if (!success) {
                    if (errorResponse != null) {
                        if (errorResponse.isException()) {
                            //TODO: Possible network error retry
                            getHandler().handleRetrofitError(errorResponse.getThrowable(), false);
                        } else if (errorResponse.getCode() > 0) {
                            getHandler().handleHttpError(errorResponse.getCode(),
                                    errorResponse.getMessage(), false);
                        } else {
                            getHandler().sendEmptyMessage(-1 * errorResponse.getCode(),
                                    errorResponse.getMessage(), false);
                        }
                    } else {
                        //Should never happen
                        Crashlytics.logException(new IllegalStateException(
                                "OrderPreprocessing error without error response"));
                    }
                }
            }
        };
        mPayNowPrepaymentProcessingTask.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_READ_PHONE_STATE:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.READ_PHONE_STATE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        initPayNowPrepaymentProcessingTask();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        showToast(getString(R.string.select_different_payment_method));
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPayNowPrepaymentProcessingTask != null) {
            mPayNowPrepaymentProcessingTask.cancel(true);
        }
    }

    private void processMobikWikResponse(int status) {
        if (status == 0) {
            onPayNowSuccess();
        } else {
            onPayNowFailure();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            new PostPaymentProcessor<>(this, mTxnId)
                    .withOrderId(mOrderId)
                    .withIsPayNow(true)
                    .processPayzapp(data, resultCode, UIUtil.formatAsMoney(mFinalTotal));
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onPayNowSuccess();
            } else {
                onPayNowFailure();
            }
        } else if (requestCode == MobikwikPayment.MOBIKWIK_REQ_CODE) {
            if (data != null) {
                MKTransactionResponse response = (MKTransactionResponse) data.getSerializableExtra(MobikwikSDK.EXTRA_TRANSACTION_RESPONSE);
                if (response != null) {
                    if (!TextUtils.isEmpty(response.orderId)) {
                        try {
                            if (BuildConfig.DEBUG) {
                                processMobikWikResponse(Integer.parseInt(response.statusCode));
                            } else {
                                processMobikWikResponse(Integer.parseInt(response.statusCode));
                            }
                        } catch (NumberFormatException e) {
                            Crashlytics.logException(e);
                        }
                    } else {
                        Crashlytics.logException(new IllegalArgumentException());
                    }
                } else {
                    Crashlytics.logException(new IllegalArgumentException());
                }
            }
        } else {
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
        mFinalTotal = Double.parseDouble(amount);
    }

    @Override
    public void onPostPaymentFailure(String txnId, String paymentType) {
        onPayNowFailure();
    }

    @Override
    public void onPostPaymentSuccess(String txnId, String paymentType) {
        onPayNowSuccess();
    }

    private void displayOrderSummary(ArrayList<PayNowDetail> payNowDetailList) {
        LayoutInflater inflater = getLayoutInflater();

        // Show order & invoice details
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);

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
        PaymentMethodsView paymentMethodsView = (PaymentMethodsView) findViewById(R.id.layoutPaymentOptions);
        paymentMethodsView.setPaymentMethods(paymentTypeList, 0, true, false);
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
