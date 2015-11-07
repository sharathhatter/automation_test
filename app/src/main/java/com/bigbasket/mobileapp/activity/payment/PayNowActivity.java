package com.bigbasket.mobileapp.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPayNowParamsResponse;
import com.bigbasket.mobileapp.factory.payment.PayNowPaymentHandler;
import com.bigbasket.mobileapp.factory.payment.PostPaymentProcessor;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.handler.payment.MobikwikResponseHandler;
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
import com.enstage.wibmo.sdk.WibmoSDK;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

/**
 * Don't do the mistake of moving this to Fragment. I've done all that, and these 3rd Party SDKs
 * don't handle fragments well.
 */
public class PayNowActivity extends BackButtonActivity implements OnPostPaymentListener,
        CityListDisplayAware, PaymentTxnInfoAware {

    @Nullable
    private String mSelectedPaymentMethod;
    @Nullable
    private String mOrderId;
    @Nullable
    private String mHDFCPayzappTxnId;
    private double mFinalTotal;

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
        new PayNowPaymentHandler<>(this, null, mOrderId, mSelectedPaymentMethod, true, false).initiate();
    }

    @Override
    public void onResume() {
        super.onResume();
        processMobikWikResponse();
    }

    private void processMobikWikResponse() {
        String txnId = MobikwikResponseHandler.getLastTransactionID();
        if (!TextUtils.isEmpty(txnId)) {
            if (MobikwikResponseHandler.wasTransactionSuccessful()) {
                onPayNowSuccess();
            } else {
                onPayNowFailure();
            }
            MobikwikResponseHandler.clear();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            new PostPaymentProcessor<>(this, mHDFCPayzappTxnId)
                    .withOrderId(mOrderId)
                    .withIsPayNow(true)
                    .processPayzapp(data, resultCode, UIUtil.formatAsMoney(mFinalTotal));
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onPayNowSuccess();
            } else {
                onPayNowFailure();
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
    public void setTxnId(String txnId) {
        mHDFCPayzappTxnId = txnId;
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

    private void displayPaymentMethods(ArrayList<PaymentType> paymentTypes) {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup layoutPaymentOptions = (ViewGroup) findViewById(R.id.layoutPaymentOptions);

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

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_pay_now;
    }
}
