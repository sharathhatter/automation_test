package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.request.ValidatePaymentRequest;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentTypes;
import com.bigbasket.mobileapp.factory.payment.FundWalletPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.ValidatePayment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PaytmResponseHolder;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;

public class FundWalletActivity extends BackButtonActivity implements OnPaymentValidationListener,
        CityListDisplayAware, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mTxnId;
    private String mFinalTotal;
    private FundWalletPrepaymentProcessingTask<FundWalletActivity> mFundWalletPrepaymentProcessingTask;
    private boolean isPayUOptionVisible;
    private ViewGroup mProgressLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_FUND_WALLET);
        trackEvent(TrackingAware.FUND_WALLET_SHOWN, null);
        setTitle(getString(R.string.fundWallet));
        mProgressLayout = (ViewGroup) findViewById(R.id.layoutLoading);
        new GetCitiesTask<>(this).startTask();
    }

    @Override
    public void onReadyToDisplayCity(ArrayList<City> cities) {
        getPaymentTypes();
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
        if (mFundWalletPrepaymentProcessingTask != null) {
            mFundWalletPrepaymentProcessingTask.cancel(true);
        }
    }

    private void getPaymentTypes() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetPaymentTypes>> call =
                bigBasketApiService.getFundWalletPayments(getPreviousScreenName(), "yes", "yes", "yes", "yes", "yes");
        call.enqueue(new BBNetworkCallback<ApiResponse<GetPaymentTypes>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetPaymentTypes> getPaymentTypesApiResponse) {

                switch (getPaymentTypesApiResponse.status) {
                    case 0:
                        renderFundWallet(getPaymentTypesApiResponse.apiResponseContent.paymentTypes);
                        break;
                    default:
                        handler.sendEmptyMessage(getPaymentTypesApiResponse.status,
                                getPaymentTypesApiResponse.message, true);
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

    private void renderFundWallet(ArrayList<PaymentType> paymentTypeList) {
        final TextView txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtAmount.setTypeface(faceRobotoRegular);

        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null, getString(R.string.fundWallet),
                true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiateWalletFunding(txtAmount.getText().toString());
            }
        });
        for (PaymentType paymentType : paymentTypeList) {
            if (paymentType.getValue().equals(Constants.PAYUMONEY_WALLET)) {
                isPayUOptionVisible = true;
                break;
            }
        }
        PaymentMethodsView paymentMethodsView = (PaymentMethodsView) findViewById(R.id.layoutPaymentOptions);
        paymentMethodsView.setPaymentMethods(paymentTypeList, 0, true, false);
    }

    private void initiateWalletFunding(String amount) {
        TextInputLayout textInputAmount = (TextInputLayout) findViewById(R.id.textInputAmount);
        UIUtil.resetFormInputField(textInputAmount);
        if (TextUtils.isEmpty(amount)) {
            UIUtil.reportFormInputFieldError(textInputAmount, getString(R.string.invalidAmount));
            return;
        }
        if (TextUtils.isEmpty(mSelectedPaymentMethod)) {
            showAlertDialog(getString(R.string.missingPaymentMethod));
            return;
        }
        try {
            mFinalTotal = UIUtil.formatAsMoney(Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            showAlertDialog(getString(R.string.invalidAmount));
            return;
        }
        initFundWalletPrepaymentProcessingTask(amount);

    }

    public void initFundWalletPrepaymentProcessingTask(String amount) {
        mFundWalletPrepaymentProcessingTask =
                new FundWalletPrepaymentProcessingTask<FundWalletActivity>(this, null, null,
                        mSelectedPaymentMethod, false, true, amount, isPayUOptionVisible) {
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
                                        "Fund wallet preprocessing error without error response"));
                            }
                        }
                    }
                };
        mFundWalletPrepaymentProcessingTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        onStateNotSaved();
        ValidatePaymentRequest validatePaymentRequest =
                new ValidatePaymentRequest(mTxnId, null, null, mSelectedPaymentMethod);
        validatePaymentRequest.setFinalTotal(mFinalTotal);
        validatePaymentRequest.setIsWallet(true);
        boolean handled = new ValidatePayment<>(this, validatePaymentRequest)
                .onActivityResult(requestCode, resultCode, data);
        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onFundWalletFailure() {
        UIUtil.showPaymentFailureDlg(this);
    }

    private void onFundWalletSuccess() {
        HashMap<String, String> attrs = new HashMap<>();
        attrs.put(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        trackEvent(TrackingAware.FUND_WALLET_DONE, attrs);

        setResult(RESULT_OK);
        finish();
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
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mFinalTotal = amount;
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, @Nullable ArrayList<Order> orders) {
        if (status) {
            onFundWalletSuccess();
        } else {
            onFundWalletFailure();
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_fund_wallet;
    }

    @Override
    public void onPaymentOptionSelected(String paymentTypeValue) {
        mSelectedPaymentMethod = paymentTypeValue;
    }

}
