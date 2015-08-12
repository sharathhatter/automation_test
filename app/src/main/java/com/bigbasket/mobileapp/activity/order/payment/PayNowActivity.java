package com.bigbasket.mobileapp.activity.order.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.bigbasket.mobileapp.apiservice.models.response.GetPayzappPaymentParamsResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetPrepaidPaymentResponse;
import com.bigbasket.mobileapp.handler.payment.PayuInitializer;
import com.bigbasket.mobileapp.handler.payment.PayzappInitializer;
import com.bigbasket.mobileapp.handler.payment.PostPaymentHandler;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderInvoiceDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.payu.sdk.PayU;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Don't do the mistake of moving this to Fragment. I've done all that, and these 3rd Party SDKs
 * don't handle fragments well.
 */
public class PayNowActivity extends BackButtonActivity implements OnPostPaymentListener {

    private String mSelectedPaymentMethod;
    private String mOrderId;
    private String mHDFCPayzappTxnId;
    private double mFinalTotal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.payNow));

        mOrderId = getIntent().getStringExtra(Constants.ORDER_ID);
        getPayNowParams();
    }

    private void getPayNowParams() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getPayNowDetails(mOrderId, "yes", "yes",
                new Callback<ApiResponse<GetPayNowParamsResponse>>() {
                    @Override
                    public void success(ApiResponse<GetPayNowParamsResponse> payNowParamsApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        switch (payNowParamsApiResponse.status) {
                            case 0:
                                displayPayNowSummary(payNowParamsApiResponse.apiResponseContent.orderNumber,
                                        payNowParamsApiResponse.apiResponseContent.invoiceNumber,
                                        payNowParamsApiResponse.apiResponseContent.paymentTypes,
                                        payNowParamsApiResponse.apiResponseContent.creditDetails,
                                        payNowParamsApiResponse.apiResponseContent.orderDetails);
                                break;
                            default:
                                handler.sendEmptyMessage(payNowParamsApiResponse.status,
                                        payNowParamsApiResponse.message, true);
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
    }

    private void displayPayNowSummary(String fullOrderId, String invoiceNumber,
                                      ArrayList<PaymentType> paymentTypes,
                                      @Nullable ArrayList<CreditDetails> creditDetailsArrayList,
                                      final OrderInvoiceDetails orderInvoiceDetails) {
        displayOrderSummary(fullOrderId, invoiceNumber, creditDetailsArrayList, orderInvoiceDetails);
        displayPaymentMethods(paymentTypes);
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, null,
                getString(R.string.payNow), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayNow(orderInvoiceDetails.getTotal());
            }
        });
    }

    private void startPayNow(double total) {
        mFinalTotal = total;
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));

        switch (mSelectedPaymentMethod) {
            case Constants.PAYU:
                bigBasketApiService.postPayNowDetails(mOrderId, mSelectedPaymentMethod, new Callback<ApiResponse<GetPrepaidPaymentResponse>>() {
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
            case Constants.HDFC_POWER_PAY:
                bigBasketApiService.postPayzappPayNowDetails(mOrderId, mSelectedPaymentMethod, new Callback<ApiResponse<GetPayzappPaymentParamsResponse>>() {
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
                onPayNowSuccess();
            } else {
                onPayNowFailure();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onPayNowSuccess() {
        Intent intent = new Intent(this, PayNowThankyouActivity.class);
        intent.putExtra(Constants.ORDER_ID, mOrderId);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    private void onPayNowFailure() {
        showAlertDialog(getString(R.string.transactionFailed),
                getString(R.string.payNowFailure));
    }

    private void validateHdfcPayzappResponse(String pgTxnId, String dataPickupCode, String txnId) {
        new PostPaymentHandler<>(this, null, mSelectedPaymentMethod, txnId,
                true, UIUtil.formatAsMoney(mFinalTotal), mOrderId)
                .setPayNow(true)
                .setDataPickupCode(dataPickupCode)
                .setPgTxnId(pgTxnId)
                .start();
    }

    private void communicateHdfcPayzappResponseFailure(String resCode, String resDesc) {
        new PostPaymentHandler<>(this, null, mSelectedPaymentMethod,
                mHDFCPayzappTxnId, false, UIUtil.formatAsMoney(mFinalTotal), mOrderId)
                .setPayNow(true)
                .setErrResCode(resCode)
                .setErrResDesc(resDesc)
                .start();
    }

    @Override
    public void onPostPaymentFailure(String txnId) {
        onPayNowFailure();
    }

    @Override
    public void onPostPaymentSuccess(String txnId) {
        onPayNowSuccess();
    }

    private void displayOrderSummary(String fullOrderId, String invoiceNumber,
                                     @Nullable ArrayList<CreditDetails> creditDetailsArrayList,
                                     OrderInvoiceDetails orderInvoiceDetails) {
        LayoutInflater inflater = getLayoutInflater();

        // Show order & invoice details
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);

        ViewGroup layoutOrderSummaryInfo = (ViewGroup) findViewById(R.id.layoutOrderSummaryInfo);

        View orderNumberRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.ordernumber),
                fullOrderId, normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderNumberRow);

        View invoiceNumberRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.invoicenumber),
                invoiceNumber, normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(invoiceNumberRow);

        int numOrderItems = orderInvoiceDetails.getTotalItems();
        String itemsStr = numOrderItems + " item";
        if (numOrderItems > 1) {
            itemsStr += "s";
        }
        View orderItemsNumRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.orderItems),
                itemsStr, normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderItemsNumRow);

        View subTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.subTotal),
                UIUtil.asRupeeSpannable(orderInvoiceDetails.getSubTotal(), faceRupee),
                normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(subTotalRow);

        if (orderInvoiceDetails.getVatValue() > 0) {
            View vatRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.vat),
                    UIUtil.asRupeeSpannable(orderInvoiceDetails.getVatValue(), faceRupee),
                    normalColor, faceRobotoRegular);
            layoutOrderSummaryInfo.addView(vatRow);
        }

        View deliveryChargeRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                UIUtil.asRupeeSpannable(orderInvoiceDetails.getDeliveryCharge(), faceRupee),
                normalColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (creditDetailsArrayList != null && creditDetailsArrayList.size() > 0) {
            for (CreditDetails creditDetails : creditDetailsArrayList) {
                View creditDetailRow = UIUtil.getOrderSummaryRow(inflater, creditDetails.getMessage(),
                        UIUtil.asRupeeSpannable(creditDetails.getCreditValue(), faceRupee),
                        normalColor, faceRobotoRegular);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View finalTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                UIUtil.asRupeeSpannable(orderInvoiceDetails.getTotal(), faceRupee),
                orderTotalLabelColor,
                orderTotalValueColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(finalTotalRow);
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
