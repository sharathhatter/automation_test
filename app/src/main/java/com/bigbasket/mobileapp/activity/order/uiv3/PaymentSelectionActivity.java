package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PlaceOrderApiResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponseContent;
import com.bigbasket.mobileapp.handler.DuplicateClickAware;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.payment.MobikwikInitializer;
import com.bigbasket.mobileapp.handler.payment.PaymentInitiator;
import com.bigbasket.mobileapp.handler.payment.PayuInitializer;
import com.bigbasket.mobileapp.handler.payment.PayzappInitializer;
import com.bigbasket.mobileapp.handler.payment.PostPaymentHandler;
import com.bigbasket.mobileapp.handler.payment.ValidatePaymentHandler;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.MobikwikAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.interfaces.payment.PayuPaymentAware;
import com.bigbasket.mobileapp.interfaces.payment.PayzappPaymentAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PayzappPostParams;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MutableLong;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.google.gson.Gson;
import com.payu.sdk.PayU;

import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentSelectionActivity extends BackButtonActivity
        implements PayzappPaymentAware, PayuPaymentAware,
        OnPostPaymentListener, OnPaymentValidationListener, MobikwikAware {

    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private ArrayList<PaymentType> mPaymentTypeList;
    private String mPotentialOrderId;
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private TextView mTxtApplicableVoucherCount;
    private String mAppliedVoucherCode;
    private String mSelectedPaymentMethod;
    private OrderDetails mOrderDetails;
    private String mHDFCPayzappTxnId;
    private String mPayuTxnId;
    private ArrayList<Order> mOrdersCreated;
    private String mAddMoreLink;
    private String mAddMoreMsg;
    private MutableLong mElapsedTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mElapsedTime = new MutableLong();

        setNextScreenNavigationContext(TrackEventkeys.CO_PAYMENT);
        mPotentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);

        if (TextUtils.isEmpty(mPotentialOrderId)) return;
        setTitle(getString(R.string.placeorder));

        renderFooter();
        renderPaymentDetails();
        trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null, null, null, false, true);
    }

    @Override
    public void onResume(){
        super.onResume();
        processMobikWikResponse();
    }

    private void processMobikWikResponse(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String txnId = preferences.getString(Constants.MOBIKWIK_ORDER_ID, null);
        if(!TextUtils.isEmpty(txnId)){
            String txnStatus = preferences.getString(Constants.MOBIKWIK_STATUS, null);
            String txnMsg = preferences.getString(Constants.MOBIKWIK_STATUS_MSG, null);
            if(!TextUtils.isEmpty(txnStatus) && Integer.parseInt(txnStatus) == 0){
                String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
                int mobiKwikTxnId = Integer.parseInt(txnId);
                String mMobiKwikTxnId = String.valueOf(mobiKwikTxnId / 1000); //todo remove this
                new ValidatePaymentHandler<>(this, mPotentialOrderId, mMobiKwikTxnId, fullOrderId).start();
            }else {
                showAlertDialog(null, txnMsg, Constants.SOURCE_PLACE_ORDER);
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.MOBIKWIK_ORDER_ID);
            editor.remove(Constants.MOBIKWIK_STATUS);
            editor.remove(Constants.MOBIKWIK_STATUS_MSG);
            editor.commit();
        }
    }

    private void renderFooter() {
        mOrderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        if (mOrderDetails == null) return;
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, mOrderDetails.getFormattedFinalTotal(),
                getString(R.string.placeOrderCaps), false);
        layoutCheckoutFooter.setOnClickListener(new DuplicateClickAware(mElapsedTime) {
            @Override
            public void onActualClick(View view) {
                onPlaceOrderAction();
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.PAYMENT_MODE, mSelectedPaymentMethod);
                map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_CLICKED, map, null, null, false, true);
            }
        });
    }

    private void renderPaymentDetails() {
        mActiveVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        mAppliedVoucherCode = getIntent().getStringExtra(Constants.EVOUCHER_CODE);

        mPaymentTypeList = getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES);
        ArrayList<CreditDetails> creditDetails = getIntent().getParcelableArrayListExtra(Constants.CREDIT_DETAILS);
        renderPaymentMethodsAndSummary(creditDetails);
    }

    private void renderPaymentMethodsAndSummary(@Nullable ArrayList<CreditDetails> creditDetails) {
        // Show invoice and other order details
        LayoutInflater inflater = getLayoutInflater();
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int secondaryColor = getResources().getColor(R.color.uiv3_secondary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);
        LinearLayout layoutOrderSummaryInfo = (LinearLayout) findViewById(R.id.layoutOrderSummaryInfo);
        layoutOrderSummaryInfo.removeAllViews();

        String numItems = mOrderDetails.getTotalItems() + " Item" + (mOrderDetails.getTotalItems() > 1 ? "s" : "");
        View orderItemsRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.orderItems),
                numItems, normalColor, secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderItemsRow);

        View subTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.subTotal),
                UIUtil.asRupeeSpannable(mOrderDetails.getSubTotal(), faceRupee), normalColor, secondaryColor,
                faceRobotoRegular);
        layoutOrderSummaryInfo.addView(subTotalRow);

        View deliveryChargeRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                UIUtil.asRupeeSpannable(mOrderDetails.getDeliveryCharge(), faceRupee), normalColor,
                secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(deliveryChargeRow);

        if (creditDetails != null && creditDetails.size() > 0) {
            for (CreditDetails creditDetail : creditDetails) {
                View creditDetailRow = UIUtil.getOrderSummaryRow(inflater, creditDetail.getMessage(),
                        UIUtil.asRupeeSpannable(creditDetail.getCreditValue(), faceRupee), normalColor,
                        secondaryColor, faceRobotoRegular);
                layoutOrderSummaryInfo.addView(creditDetailRow);
            }
        }

        View finalTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.finalTotal),
                UIUtil.asRupeeSpannable(mOrderDetails.getFinalTotal(), faceRupee), orderTotalLabelColor,
                orderTotalValueColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(finalTotalRow);

        mTxtApplyVoucher = (TextView) findViewById(R.id.txtApplyVoucher);
        mTxtApplyVoucher.setTypeface(faceRobotoRegular);
        OnShowAvailableVouchersListener showAvailableVouchersListener = new OnShowAvailableVouchersListener();
        mTxtApplyVoucher.setOnClickListener(showAvailableVouchersListener);
        mTxtApplicableVoucherCount = (TextView) findViewById(R.id.txtApplicableVoucherCount);
        mTxtApplicableVoucherCount.setTypeface(faceRobotoRegular);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setText(mActiveVouchersList.size() + " " +
                    (mActiveVouchersList.size() > 1 ?
                            getString(R.string.voucherApplicablePlural) :
                            getString(R.string.voucherApplicableSingular)));
            mTxtApplicableVoucherCount.setVisibility(View.VISIBLE);
        } else {
            mTxtApplicableVoucherCount.setVisibility(View.GONE);
        }
        mTxtApplicableVoucherCount.setOnClickListener(showAvailableVouchersListener);

        mTxtRemoveVoucher = (TextView) findViewById(R.id.txtRemoveVoucher);
        mTxtRemoveVoucher.setTypeface(faceRobotoRegular);
        mTxtRemoveVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(getString(R.string.removeVoucherHeading), getString(R.string.removeVoucherDesc),
                        DialogButton.YES, DialogButton.CANCEL, Constants.REMOVE_VOUCHER, mAppliedVoucherCode,
                        getString(R.string.remove));
            }
        });

        if (!TextUtils.isEmpty(mAppliedVoucherCode)) {
            showVoucherAppliedText(mAppliedVoucherCode);
        } else {
            onVoucherRemoved();
        }

        TextView lblAmountFromWallet = (TextView) findViewById(R.id.lblAmountFromWallet);
        lblAmountFromWallet.setTypeface(faceRobotoRegular);

        boolean isInHDFCPayMode = HDFCPayzappHandler.isInHDFCPayMode(this);
        if (isInHDFCPayMode) {
            // Now check whether Payzapp is actually present
            boolean hasHdfc = false;
            for (PaymentType paymentType : mPaymentTypeList) {
                if (paymentType.getValue().equals(Constants.HDFC_POWER_PAY)) {
                    hasHdfc = true;
                    break;
                }
            }
            isInHDFCPayMode = hasHdfc;
        }
        RadioGroup layoutPaymentOptions = (RadioGroup) findViewById(R.id.layoutPaymentOptions);
        layoutPaymentOptions.removeAllViews();

        if (mOrderDetails.getFinalTotal() <= 0) {
            lblAmountFromWallet.setVisibility(View.VISIBLE);
            mSelectedPaymentMethod = mPaymentTypeList.get(0).getValue();
        } else {
            lblAmountFromWallet.setVisibility(View.GONE);
            int i = 0;
            for (final PaymentType paymentType : mPaymentTypeList) {
                if (isInHDFCPayMode && !paymentType.getValue().equals(Constants.HDFC_POWER_PAY)) {
                    continue;
                }
                RadioButton rbtnPaymentType = UIUtil.
                        getPaymentOptionRadioButton(layoutPaymentOptions, this, inflater);
                rbtnPaymentType.setText(paymentType.getDisplayName());
                rbtnPaymentType.setId(i);
                boolean isSelected = TextUtils.isEmpty(mSelectedPaymentMethod) ? i == 0 :
                        mSelectedPaymentMethod.equals(paymentType.getValue());
                if (isSelected) {
                    rbtnPaymentType.setChecked(true);
                    mSelectedPaymentMethod = paymentType.getValue();
                }
                rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mSelectedPaymentMethod = paymentType.getValue();
                        }
                    }
                });
                layoutPaymentOptions.addView(rbtnPaymentType);
                i++;
            }
        }
    }

    @Override
    public void initializeMobikwik(HashMap<String, String> paymentParams) {
        /*
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.P_ORDER_ID, mPotentialOrderId);
        editor.putString(Constants.MOBIKWIK_ADD_MORE_LINK, mAddMoreLink);
        editor.putString(Constants.MOBIKWIK_ADD_MORE_MSG, mAddMoreMsg);
        editor.putString(Constants.MOBIKWIK_ORDER_CREATED, new Gson().toJson(mOrdersCreated));
        String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
        editor.putString(Constants.ORDER_ID, fullOrderId);
        editor.commit();
        */
        MobikwikInitializer.initiate(paymentParams, this);
    }

    private class OnShowAvailableVouchersListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent availableVoucherListActivity = new Intent(getCurrentActivity(), AvailableVoucherListActivity.class);
            availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
            startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
        }
    }

    public void onVoucherApplied(String voucher, OrderDetails orderDetails,
                                 ArrayList<CreditDetails> creditDetails) {
        if (!TextUtils.isEmpty(voucher)) {
            showVoucherAppliedText(voucher);
            mOrderDetails = orderDetails;
            renderPaymentMethodsAndSummary(creditDetails);
        }
    }

    private void showVoucherAppliedText(String voucher) {
        mTxtApplyVoucher.setVisibility(View.GONE);
        mTxtRemoveVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setText("eVoucher: " + voucher + " Applied!");
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.GONE);
        }
        mAppliedVoucherCode = voucher;
    }

    public void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.postVoucher(mPotentialOrderId, voucherCode, new Callback<ApiResponse<PostVoucherApiResponseContent>>() {
                @Override
                public void success(ApiResponse<PostVoucherApiResponseContent> postVoucherApiResponse, Response response) {
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    HashMap<String, String> map = new HashMap<>();
                    switch (postVoucherApiResponse.status) {
                        case 0:
                            onVoucherSuccessfullyApplied(voucherCode,
                                    postVoucherApiResponse.apiResponseContent.orderDetails,
                                    postVoucherApiResponse.apiResponseContent.creditDetails);
                            break;
                        default:
                            handler.sendEmptyMessage(postVoucherApiResponse.status, postVoucherApiResponse.message);
                            map.put(TrackEventkeys.FAILURE_REASON, postVoucherApiResponse.message);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, map);
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
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
                }
            });
        } else {
            handler.sendOfflineError();
        }
    }

    public void removeVoucher() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.removeVoucher(mPotentialOrderId, new Callback<ApiResponse<PostVoucherApiResponseContent>>() {
            @Override
            public void success(ApiResponse<PostVoucherApiResponseContent> removeVoucherApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (removeVoucherApiResponse.status) {
                    case 0:
                        Toast.makeText(getCurrentActivity(),
                                getString(R.string.voucherWasRemoved), Toast.LENGTH_SHORT).show();
                        onVoucherRemoved(removeVoucherApiResponse.apiResponseContent.orderDetails,
                                removeVoucherApiResponse.apiResponseContent.creditDetails);
                        break;
                    default:
                        handler.sendEmptyMessage(removeVoucherApiResponse.status,
                                removeVoucherApiResponse.message);
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

    /**
     * Callback when the voucher has been successfully applied
     */
    private void onVoucherSuccessfullyApplied(String voucherCode, OrderDetails orderDetails,
                                              ArrayList<CreditDetails> creditDetails) {
        onVoucherApplied(voucherCode, orderDetails, creditDetails);
    }

    private void onVoucherRemoved() {
        mAppliedVoucherCode = null;
        mTxtApplyVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setVisibility(View.GONE);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            mTxtApplicableVoucherCount.setVisibility(View.VISIBLE);
        }
    }

    private void onVoucherRemoved(OrderDetails orderDetails,
                                  ArrayList<CreditDetails> creditDetails) {
        onVoucherRemoved();
        mOrderDetails = orderDetails;
        renderPaymentMethodsAndSummary(creditDetails);
    }

    private void onPlaceOrderAction() {
        placeOrder(null);
    }

    private boolean isCreditCardPayment() {
        return mSelectedPaymentMethod != null &&
                (mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY) ||
                        mSelectedPaymentMethod.equals(Constants.PAYU) ||
                        mSelectedPaymentMethod.equals(Constants.MOBIKWIK_WALLET));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
            if (resultCode == RESULT_OK) {
                new ValidatePaymentHandler<>(this, mPotentialOrderId, mPayuTxnId, fullOrderId).start();
            } else {
                new ValidatePaymentHandler<>(this, mPotentialOrderId, mPayuTxnId, fullOrderId).start();
            }
        }else if (requestCode == Constants.MOBIKWIK_REQUEST_CODE) {
            String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
            new ValidatePaymentHandler<>(this, mPotentialOrderId, mPayuTxnId, fullOrderId).start();
        } else {
            switch (resultCode) {
                case NavigationCodes.VOUCHER_APPLIED:
                    if (data != null) {
                        String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
                        if (!TextUtils.isEmpty(voucherCode)) {
                            applyVoucher(voucherCode);
                        }
                    }
                    break;
                case NavigationCodes.GO_TO_SLOT_SELECTION:
                    setResult(NavigationCodes.GO_TO_SLOT_SELECTION);
                    finish();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    private void placeOrder(String txnId) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(isCreditCardPayment() ? getString(R.string.placeOrderPleaseWait) : getString(R.string.please_wait),
                false);
        bigBasketApiService.placeOrder(mPotentialOrderId, txnId, mSelectedPaymentMethod,
                new Callback<OldApiResponse<PlaceOrderApiResponseContent>>() {
                    @Override
                    public void success(OldApiResponse<PlaceOrderApiResponseContent> placeOrderApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (placeOrderApiResponse.status.equals(Constants.OK)) {
                            postOrderCreation(placeOrderApiResponse.apiResponseContent.orders,
                                    placeOrderApiResponse.apiResponseContent.addMoreLink,
                                    placeOrderApiResponse.apiResponseContent.addMoreMsg);
                        } else {
                            handler.sendEmptyMessage(placeOrderApiResponse.getErrorTypeAsInt(), placeOrderApiResponse.message);
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

    private void postOrderCreation(ArrayList<Order> orders, String addMoreLink,
                                   String addMoreMsg) {
        ((CartInfoAware) getCurrentActivity()).markBasketDirty();

        if (isCreditCardPayment()) {
            mOrdersCreated = orders;
            mAddMoreLink = addMoreLink;
            mAddMoreMsg = addMoreMsg;
            getPaymentParams();
        } else {
            showOrderThankyou(orders, addMoreLink, addMoreMsg);
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
        setNextScreenNavigationContext(TrackEventkeys.CO_PAYMENT);

        Intent invoiceIntent = new Intent(this, OrderInvoiceActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        invoiceIntent.putExtra(Constants.ADD_MORE_LINK, addMoreLink);
        invoiceIntent.putExtra(Constants.ADD_MORE_MSG, addMoreMsg);
        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void initializeHDFCPayzapp(PayzappPostParams payzappPostParams) {
        mHDFCPayzappTxnId = payzappPostParams.getTxnId();
        PayzappInitializer.initiate(this, payzappPostParams);
    }

    @Override
    public void initializePayu(HashMap<String, String> paymentParams) {
        mPayuTxnId = paymentParams.get(PayU.TXNID);
        PayuInitializer.initiate(paymentParams, this);
    }

    private void getPaymentParams() {
        new PaymentInitiator<>(this, mPotentialOrderId, mSelectedPaymentMethod)
                .initiate();
    }

    private void validateHdfcPayzappResponse(String pgTxnId, String dataPickupCode, String txnId) {
        new PostPaymentHandler<>(this, mPotentialOrderId, mSelectedPaymentMethod, txnId,
                true, mOrderDetails.getFormattedFinalTotal(), null)
                .setDataPickupCode(dataPickupCode)
                .setPgTxnId(pgTxnId)
                .start();
    }

    private void communicateHdfcPayzappResponseFailure(String resCode, String resDesc) {
        new PostPaymentHandler<>(this, mPotentialOrderId, mSelectedPaymentMethod,
                mHDFCPayzappTxnId, false, mOrderDetails.getFormattedFinalTotal(), null)
                .setErrResCode(resCode)
                .setErrResDesc(resDesc)
                .start();
    }

    @Override
    public void onPostPaymentFailure(String txnId) {
        // When transaction has failed. Place order as Cash on Delivery.
        // User can later use 'Pay Now' to complete order.
        String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
        new ValidatePaymentHandler<>(this, mPotentialOrderId, txnId, fullOrderId).start();
    }

    @Override
    public void onPostPaymentSuccess(String txnId) {
        // Now Validate payment from server for excess collection
        String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
        new ValidatePaymentHandler<>(this, mPotentialOrderId, txnId, fullOrderId).start();
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg) {
        if (status || msg == null) {
            showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
        } else {
            // Show a message and then take to Order thank-you page
            showAlertDialog(null, msg, Constants.SOURCE_PLACE_ORDER);
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, @Nullable String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.REMOVE_VOUCHER:
                    removeVoucher();
                    break;
                case Constants.SOURCE_PLACE_ORDER:
                    showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, null, valuePassed);
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, null, valuePassed);
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_payment_option;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PAYMENT_SELECTION_SCREEN;
    }
}
