package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.bigbasket.mobileapp.apiservice.models.response.GetPayzappPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.handler.payment.MobikwikInitializer;
import com.bigbasket.mobileapp.handler.payment.PayuInitializer;
import com.bigbasket.mobileapp.handler.payment.PayzappInitializer;
import com.bigbasket.mobileapp.handler.payment.PostPaymentHandler;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.payu.sdk.PayU;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FundWalletActivity extends BackButtonActivity implements OnPostPaymentListener {

    private String mSelectedPaymentMethod;
    private String mHDFCPayzappTxnId;
    private Double mFinalTotal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.NAVIGATION_CTX_FUND_WALLET);
        trackEvent(TrackingAware.FUND_WALLET_SHOWN, null);
        setTitle(getString(R.string.fundWallet));
        getPaymentTypes();
    }


    @Override
    public void onResume() {
        super.onResume();
        processMobikWikResponse();
    }

    private void processMobikWikResponse() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String txnId = preferences.getString(Constants.MOBIKWIK_ORDER_ID, null);
        if (!TextUtils.isEmpty(txnId)) {
            String txnStatus = preferences.getString(Constants.MOBIKWIK_STATUS, null);
            if (!TextUtils.isEmpty(txnStatus) && Integer.parseInt(txnStatus) == 0) {
                onFundWalletSuccess();
            } else {
                showAlertDialog(getString(R.string.transactionFailed),
                        getString(R.string.txnFailureMsg));
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.MOBIKWIK_ORDER_ID);
            editor.remove(Constants.MOBIKWIK_STATUS);
            editor.commit();
        }
    }

    private void getPaymentTypes() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getFundWalletPayments("yes", "yes", "yes", new Callback<ApiResponse<GetPaymentTypes>>() {
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
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        try {
            mFinalTotal = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showAlertDialog(getString(R.string.invalidAmount));
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        switch (mSelectedPaymentMethod) {
            case Constants.PAYU:
                bigBasketApiService.postFundWallet(mSelectedPaymentMethod, amount, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
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
                                PayuInitializer.initiate(getPrepaidPaymentApiResponse.apiResponseContent.postParams,
                                        getCurrentActivity());
                                break;
                            default:
                                handler.sendEmptyMessage(getPrepaidPaymentApiResponse.status,
                                        getPrepaidPaymentApiResponse.message);
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
                break;
            case Constants.MOBIKWIK_PAYMENT:
                bigBasketApiService.postFundWallet(mSelectedPaymentMethod, amount, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
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
                                MobikwikInitializer.initiate(getPrepaidPaymentApiResponse.apiResponseContent.postParams,
                                        getCurrentActivity());
                                break;
                            default:
                                handler.sendEmptyMessage(getPrepaidPaymentApiResponse.status,
                                        getPrepaidPaymentApiResponse.message);
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
                break;
            case Constants.HDFC_POWER_PAY:
                bigBasketApiService.postPayzappFundWallet(mSelectedPaymentMethod, amount, new Callback<ApiResponse<GetPayzappPaymentParamsResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPayzappPaymentParamsResponse> getPayzappPaymentParamsApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (getPayzappPaymentParamsApiResponse.status) {
                            case 0:
                                mHDFCPayzappTxnId = getPayzappPaymentParamsApiResponse.apiResponseContent.payzappPostParams.getTxnId();
                                PayzappInitializer.initiate(getCurrentActivity(),
                                        getPayzappPaymentParamsApiResponse.apiResponseContent.payzappPostParams);
                                break;
                            default:
                                handler.sendEmptyMessage(getPayzappPaymentParamsApiResponse.status,
                                        getPayzappPaymentParamsApiResponse.message);
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
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            if (resultCode == RESULT_OK) {
                WPayResponse res = WibmoSDK.processInAppResponseWPay(data);
                String pgTxnId = res.getWibmoTxnId();
                String dataPickupCode = res.getDataPickUpCode();
                validateHdfcPayzappResponse(pgTxnId, dataPickupCode, mHDFCPayzappTxnId);
            } else {
                if (data != null) {
                    String resCode = data.getStringExtra("ResCode");
                    String resDesc = data.getStringExtra("ResDesc");
                    communicateHdfcPayzappResponseFailure(resCode, resDesc);
                } else {
                    communicateHdfcPayzappResponseFailure(null, null);
                }
            }
        } else if (requestCode == PayU.RESULT) {
            if (resultCode == RESULT_OK) {
                onFundWalletSuccess();
            } else {
                onFundWalletFailure();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onFundWalletFailure() {
        showAlertDialog(getString(R.string.transactionFailed),
                getString(R.string.txnFailureMsg));
    }

    private void onFundWalletSuccess() {
        HashMap<String, String> attrs = new HashMap<>();
        attrs.put(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        trackEvent(TrackingAware.FUND_WALLET_DONE, attrs);

        setResult(RESULT_OK);
        finish();
    }

    private void validateHdfcPayzappResponse(String pgTxnId, String dataPickupCode, String txnId) {
        new PostPaymentHandler<>(this, null, mSelectedPaymentMethod, txnId,
                true, UIUtil.formatAsMoney(mFinalTotal), null)
                .isWallet(true)
                .setDataPickupCode(dataPickupCode)
                .setPgTxnId(pgTxnId)
                .start();
    }

    private void communicateHdfcPayzappResponseFailure(String resCode, String resDesc) {
        new PostPaymentHandler<>(this, null, mSelectedPaymentMethod,
                mHDFCPayzappTxnId, false, UIUtil.formatAsMoney(mFinalTotal), null)
                .isWallet(true)
                .setErrResCode(resCode)
                .setErrResDesc(resDesc)
                .start();
    }

    @Override
    public void onPostPaymentFailure(String txnId) {
        onFundWalletFailure();
    }

    @Override
    public void onPostPaymentSuccess(String txnId) {
        onFundWalletSuccess();
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_fund_wallet;
    }
}
