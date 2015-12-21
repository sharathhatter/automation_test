package com.bigbasket.mobileapp.activity.payment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentTypes;
import com.bigbasket.mobileapp.factory.payment.FundWalletPrepaymentProcessingTask;
import com.bigbasket.mobileapp.factory.payment.PostPaymentProcessor;
import com.bigbasket.mobileapp.factory.payment.impl.MobikwikPayment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.CityListDisplayAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.task.uiv3.GetCitiesTask;
import com.bigbasket.mobileapp.util.Constants;
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

public class FundWalletActivity extends BackButtonActivity implements OnPostPaymentListener,
        CityListDisplayAware, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mTxnId;
    private double mFinalTotal;
    private FundWalletPrepaymentProcessingTask<FundWalletActivity> mFundWalletPrepaymentProcessingTask;
    private boolean isPayUOptionVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NAVIGATION_CTX_FUND_WALLET);
        trackEvent(TrackingAware.FUND_WALLET_SHOWN, null);
        setTitle(getString(R.string.fundWallet));
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFundWalletPrepaymentProcessingTask != null) {
            mFundWalletPrepaymentProcessingTask.cancel(true);
        }
    }

    private void processMobikWikResponse(int status) {
        if (status == 0) {
            onFundWalletSuccess();
        } else {
            onFundWalletFailure();
        }
    }

    private void getPaymentTypes() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetPaymentTypes>> call = bigBasketApiService.getFundWalletPayments("yes", "yes", "yes", "yes", "yes");
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
            mFinalTotal = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showAlertDialog(getString(R.string.invalidAmount));
            return;
        }
        if ((mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY))) {
            if (handlePermission(Manifest.permission.READ_PHONE_STATE, Constants.PERMISSION_REQUEST_CODE_READ_PHONE_STATE)) {
                initFundWalletPrepaymentProcessingTask(amount);
            }
        } else {
            initFundWalletPrepaymentProcessingTask(amount);
        }


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
                                        "Fund wallet preprocessing error without error response"));
                            }
                        }
                    }
                };
        mFundWalletPrepaymentProcessingTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_READ_PHONE_STATE:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.READ_PHONE_STATE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        initFundWalletPrepaymentProcessingTask(String.valueOf(mFinalTotal));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            new PostPaymentProcessor<>(this, mTxnId)
                    .withIsFundWallet(true)
                    .processPayzapp(data, resultCode, UIUtil.formatAsMoney(mFinalTotal));
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onFundWalletSuccess();
            } else {
                onFundWalletFailure();
            }
        } else if (requestCode == MobikwikPayment.MOBIKWIK_REQ_CODE) {
            if (data != null) {
                MKTransactionResponse response = (MKTransactionResponse) data.getSerializableExtra(MobikwikSDK.EXTRA_TRANSACTION_RESPONSE);
                if (response != null) {
                    try {
                        processMobikWikResponse(Integer.parseInt(response.statusCode));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public void setTxnDetails(String txnId, String amount) {
        mTxnId = txnId;
        mFinalTotal = Double.parseDouble(amount);
    }

    @Override
    public void onPostPaymentFailure(String txnId, String paymentType) {
        onFundWalletFailure();
    }

    @Override
    public void onPostPaymentSuccess(String txnId, String paymentType) {
        onFundWalletSuccess();
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
