package com.bigbasket.mobileapp.activity.payment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPaymentTypes;
import com.bigbasket.mobileapp.factory.payment.FundWalletPaymentHandler;
import com.bigbasket.mobileapp.factory.payment.PostPaymentProcessor;
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
import com.enstage.wibmo.sdk.WibmoSDK;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FundWalletActivity extends BackButtonActivity implements OnPostPaymentListener,
        CityListDisplayAware, PaymentTxnInfoAware {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mHDFCPayzappTxnId;
    private double mFinalTotal;

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
        if (mHDFCPayzappTxnId != null) {
            outState.putString(Constants.TXN_ID, mHDFCPayzappTxnId);
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
        if (mHDFCPayzappTxnId == null) {
            mHDFCPayzappTxnId = savedInstanceState.getString(Constants.TXN_ID);
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
        processMobikWikResponse();
    }

    private void processMobikWikResponse() {
        SharedPreferences preferences = getSharedPreferences(Constants.MOBIKWIK_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        String txnId = preferences.getString(Constants.MOBIKWIK_ORDER_ID, null);
        if (!TextUtils.isEmpty(txnId)) {
            String txnStatus = preferences.getString(Constants.MOBIKWIK_STATUS, null);
            if (!TextUtils.isEmpty(txnStatus) && Integer.parseInt(txnStatus) == 0) {
                onFundWalletSuccess();
            } else {
                onFundWalletFailure();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.MOBIKWIK_ORDER_ID);
            editor.remove(Constants.MOBIKWIK_STATUS);
            editor.apply();
        }
    }

    private void getPaymentTypes() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getFundWalletPayments("yes", "yes", "yes", "yes",
                new Callback<ApiResponse<GetPaymentTypes>>() {
                    @Override
                    public void success(ApiResponse<GetPaymentTypes> getPaymentTypesApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
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
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error, false);
                    }
                });
    }

    private void renderFundWallet(ArrayList<PaymentType> paymentTypes) {
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

        ViewGroup layoutPaymentOptions = (ViewGroup) findViewById(R.id.layoutPaymentOptions);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < paymentTypes.size(); i++) {
            final PaymentType paymentType = paymentTypes.get(i);
            RadioButton rbtnPaymentType = UIUtil.
                    getPaymentOptionRadioButton(layoutPaymentOptions, this, inflater);
            rbtnPaymentType.setText(paymentType.getDisplayName());
            rbtnPaymentType.setId(i);
            if (i == 0) {
                mSelectedPaymentMethod = paymentType.getValue();
                rbtnPaymentType.setChecked(true);
            }
            layoutPaymentOptions.addView(rbtnPaymentType);
            rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        mSelectedPaymentMethod = paymentType.getValue();
                    }
                }
            });
        }
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
        new FundWalletPaymentHandler<>(this, null, null, mSelectedPaymentMethod, false, true, amount)
                .initiate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            new PostPaymentProcessor<>(this, mHDFCPayzappTxnId)
                    .withIsFundWallet(true)
                    .processPayzapp(data, resultCode, UIUtil.formatAsMoney(mFinalTotal));
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onFundWalletSuccess();
            } else {
                onFundWalletFailure();
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
    public void setTxnId(String txnId) {
        mHDFCPayzappTxnId = txnId;
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
}
