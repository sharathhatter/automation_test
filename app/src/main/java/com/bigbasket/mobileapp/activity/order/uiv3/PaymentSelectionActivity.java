package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.bigbasket.mobileapp.factory.payment.PaymentHandler;
import com.bigbasket.mobileapp.factory.payment.PostPaymentProcessor;
import com.bigbasket.mobileapp.handler.DuplicateClickAware;
import com.bigbasket.mobileapp.handler.HDFCPayzappHandler;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.handler.payment.MobikwikResponseHandler;
import com.bigbasket.mobileapp.handler.payment.ValidatePaymentHandler;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.interfaces.payment.OnPaymentValidationListener;
import com.bigbasket.mobileapp.interfaces.payment.OnPostPaymentListener;
import com.bigbasket.mobileapp.interfaces.payment.PaymentTxnInfoAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MutableLong;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.bigbasket.mobileapp.view.PaymentMethodsView;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

public class PaymentSelectionActivity extends BackButtonActivity
        implements OnPostPaymentListener, OnPaymentValidationListener, PaymentTxnInfoAware, PaymentMethodsView.OnPaymentOptionSelectionListener {

    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private ArrayList<PaymentType> paymentTypeList;
    private String mPotentialOrderId;
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private TextView mTxtApplicableVoucherCount;
    private String mAppliedVoucherCode;
    private String mSelectedPaymentMethod;
    private OrderDetails mOrderDetails;
    private String mTxnId;
    private ArrayList<Order> mOrdersCreated;
    private String mAddMoreLink;
    private String mAddMoreMsg;
    private MutableLong mElapsedTime;
    private boolean mIsPaymentWarningDisplayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mElapsedTime = new MutableLong();

        setNextScreenNavigationContext(TrackEventkeys.CO_PAYMENT);
        mPotentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);

        if (TextUtils.isEmpty(mPotentialOrderId)) return;
        setTitle(getString(R.string.placeorder));

        mOrderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        if (mOrderDetails == null) return;
        renderPaymentDetails();
        setUpNewCheckoutFlowMsg();
        renderFooter(false);
        trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null, null, null, false, true);
        MoEngageWrapper.suppressInAppMessageHere(moEHelper);
        if (savedInstanceState != null) {
            if (mOrdersCreated == null) {
                mOrdersCreated = savedInstanceState.getParcelableArrayList(Constants.ORDERS);
            }
            if (mTxnId == null) {
                mTxnId = savedInstanceState.getString(Constants.TXN_ID);
            }
            if (mSelectedPaymentMethod == null) {
                mSelectedPaymentMethod = savedInstanceState.getString(Constants.PAYMENT_METHOD);
            }
            mIsPaymentWarningDisplayed = savedInstanceState.getBoolean(Constants.PAYMENT_STATUS, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mOrdersCreated != null) {
            outState.putParcelableArrayList(Constants.ORDERS, mOrdersCreated);
        }
        if (mTxnId != null) {
            outState.putString(Constants.TXN_ID, mTxnId);
        }
        if (mSelectedPaymentMethod != null) {
            outState.putString(Constants.PAYMENT_METHOD, mSelectedPaymentMethod);
        }
        outState.putBoolean(Constants.PAYMENT_STATUS, mIsPaymentWarningDisplayed);
        super.onSaveInstanceState(outState);
    }

    private void setUpNewCheckoutFlowMsg() {
        final String newFlowUrl = getIntent().getStringExtra(Constants.NEW_FLOW_URL);
        TextView txtNewCheckoutFlowMsg = (TextView) findViewById(R.id.txtNewCheckoutFlow);
        txtNewCheckoutFlowMsg.setTypeface(faceRobotoRegular);

        TextView lblKnowMore = (TextView) findViewById(R.id.lblKnowMore);
        if (TextUtils.isEmpty(newFlowUrl)) {
            lblKnowMore.setVisibility(View.GONE);
        } else {
            SpannableString spannableString = new SpannableString(lblKnowMore.getText());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            lblKnowMore.setText(spannableString);
            lblKnowMore.setTypeface(faceRobotoRegular);
            lblKnowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trackEvent(TrackingAware.PLACE_ORDER_KNOW_MORE_LINK_CLICKED, null);
                    Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                    intent.putExtra(Constants.WEBVIEW_URL, newFlowUrl);
                    intent.putExtra(Constants.WEBVIEW_TITLE, "");
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        }

        String prefix = getString(R.string.newStr) + "\n";
        String msg = getString(R.string.newCheckoutFlowMsg);

        SpannableString spannableString = new SpannableString(prefix + msg);
        spannableString.setSpan(new ForegroundColorSpan(getResources()
                        .getColor(R.color.uiv3_dialog_header_text_bkg)),
                0, prefix.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                0, prefix.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        txtNewCheckoutFlowMsg.setText(spannableString);
    }

    private void toggleNewCheckoutFlowMsg(boolean show) {
        View layoutKnowMore = findViewById(R.id.layoutKnowMore);
        layoutKnowMore.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        processMobikWikResponse();
        if (isPaymentPending()) {
            openPaymentGateway();
        }
    }

    private boolean isPaymentPending() {
        return mIsPaymentWarningDisplayed && !TextUtils.isEmpty(mSelectedPaymentMethod)
                && mOrdersCreated != null;
    }

    private void processMobikWikResponse() {
        String txnId = MobikwikResponseHandler.getLastTransactionID();
        if (!TextUtils.isEmpty(txnId)) {
            if (mOrdersCreated != null) {
                new PostPaymentProcessor<>(this, txnId)
                        .withPotentialOrderId(mPotentialOrderId)
                        .withOrderId(mOrdersCreated.get(0).getOrderNumber())
                        .processPayment();
            }
            MobikwikResponseHandler.clear();
        }
    }

    private void renderCheckOutProgressView() {
        LinearLayout layoutPaymentContainer = (LinearLayout) findViewById(R.id.layoutCheckoutProgressContainer);
        layoutPaymentContainer.removeAllViews();
        boolean hasGifts = getIntent().getBooleanExtra(Constants.HAS_GIFTS, false);
        View checkoutProgressView;
        if (hasGifts) {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.gift), getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{0, 1, 2};
            int selectedPos = 3;
            checkoutProgressView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        } else {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{0, 1};
            int selectedPos = 2;
            checkoutProgressView = UIUtil.getCheckoutProgressView(this, null, array_txtValues, array_compPos, selectedPos);
        }
        if (checkoutProgressView != null) layoutPaymentContainer.addView(checkoutProgressView, 0);
    }

    private void renderFooter(boolean refresh) {
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, mOrderDetails.getFormattedFinalTotal(),
                getString(isCreditCardPayment() ? R.string.placeOrderAndPayCaps : R.string.placeOrderCaps),
                false);
        if (!refresh) {
            layoutCheckoutFooter.setOnClickListener(new DuplicateClickAware(mElapsedTime) {
                @Override
                public void onActualClick(View view) {
                    placeOrder();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.PAYMENT_MODE, mSelectedPaymentMethod);
                    map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                    trackEvent(TrackingAware.CHECKOUT_PLACE_ORDER_CLICKED, map, null, null, false, true);
                }
            });
        }
    }

    private void renderPaymentDetails() {
        mActiveVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        mAppliedVoucherCode = getIntent().getStringExtra(Constants.EVOUCHER_CODE);

        paymentTypeList = getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES);

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
                Bundle data = new Bundle(1);
                data.putString(Constants.EVOUCHER_CODE, mAppliedVoucherCode);
                showAlertDialog(getString(R.string.removeVoucherHeading),
                        getString(R.string.removeVoucherDesc),
                        DialogButton.YES, DialogButton.CANCEL,
                        Constants.REMOVE_VOUCHER_DIALOG_REQUEST, data,
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
            for (PaymentType paymentType : paymentTypeList) {
                if (paymentType.getValue().equals(Constants.HDFC_POWER_PAY)) {
                    hasHdfc = true;
                    break;
                }
            }
            isInHDFCPayMode = hasHdfc;
        }
        LinearLayout layoutPaymentOptions = (LinearLayout) findViewById(R.id.layoutPaymentOptions);
        layoutPaymentOptions.removeAllViews();

        if (mOrderDetails.getFinalTotal() <= 0) {
            lblAmountFromWallet.setVisibility(View.VISIBLE);
            mSelectedPaymentMethod = paymentTypeList.get(0).getValue();
        } else {
            lblAmountFromWallet.setVisibility(View.GONE);

            PaymentMethodsView mPaymentMethodsView = new PaymentMethodsView(this);
            mPaymentMethodsView.setPaymentMethods(paymentTypeList, false, isInHDFCPayMode);
            layoutPaymentOptions.addView(mPaymentMethodsView);
        }
        renderCheckOutProgressView();
    }

    private void onVoucherApplied(String voucher, OrderDetails orderDetails,
                                  ArrayList<CreditDetails> creditDetails) {
        if (!TextUtils.isEmpty(voucher)) {
            showVoucherAppliedText(voucher);
            mOrderDetails = orderDetails;
            renderPaymentMethodsAndSummary(creditDetails);
            renderFooter(true);
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

    private void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            Call<ApiResponse<PostVoucherApiResponseContent>> call = bigBasketApiService.postVoucher(mPotentialOrderId, voucherCode);
            call.enqueue(new BBNetworkCallback<ApiResponse<PostVoucherApiResponseContent>>(this) {
                @Override
                public void onSuccess(ApiResponse<PostVoucherApiResponseContent> postVoucherApiResponse) {
                    switch (postVoucherApiResponse.status) {
                        case 0:
                            onVoucherSuccessfullyApplied(voucherCode,
                                    postVoucherApiResponse.apiResponseContent.orderDetails,
                                    postVoucherApiResponse.apiResponseContent.creditDetails);
                            break;
                        default:
                            HashMap<String, String> map = new HashMap<>();
                            handler.sendEmptyMessage(postVoucherApiResponse.status, postVoucherApiResponse.message);
                            map.put(TrackEventkeys.FAILURE_REASON, postVoucherApiResponse.message);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, map);
                            break;
                    }
                }

                @Override
                public void onFailure(int httpErrorCode, String msg) {
                    super.onFailure(httpErrorCode, msg);
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
                }

                @Override
                public void onFailure(Throwable t) {
                    super.onFailure(t);
                    trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
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
        } else {
            handler.sendOfflineError();
        }
    }

    private void removeVoucher() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<PostVoucherApiResponseContent>> call = bigBasketApiService.removeVoucher(mPotentialOrderId);
        call.enqueue(new BBNetworkCallback<ApiResponse<PostVoucherApiResponseContent>>(this) {
            @Override
            public void onSuccess(ApiResponse<PostVoucherApiResponseContent> removeVoucherApiResponse) {
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
        renderFooter(true);
    }

    private boolean isCreditCardPayment() {
        return mSelectedPaymentMethod != null &&
                (mSelectedPaymentMethod.equals(Constants.HDFC_POWER_PAY) ||
                        mSelectedPaymentMethod.equals(Constants.PAYU) ||
                        mSelectedPaymentMethod.equals(Constants.MOBIKWIK_WALLET) ||
                        mSelectedPaymentMethod.equals(Constants.PAYTM_WALLET) ||
                        mSelectedPaymentMethod.equals(Constants.PAYUMONEY_WALLET));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            new PostPaymentProcessor<>(this, mTxnId)
                    .withPotentialOrderId(mPotentialOrderId)
                    .processPayzapp(data, resultCode, mOrderDetails.getFormattedFinalTotal());
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            new PostPaymentProcessor<>(this, mTxnId)
                    .withPotentialOrderId(mPotentialOrderId)
                    .withOrderId(mOrdersCreated.get(0).getOrderNumber())
                    .processPayment();
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

    private void placeOrder() {
        if (TextUtils.isEmpty(mSelectedPaymentMethod)) {
            showToast(getString(R.string.missingPaymentMethod));
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(isCreditCardPayment() ? getString(R.string.placeOrderPleaseWait) : getString(R.string.please_wait),
                false);
        Call<OldApiResponse<PlaceOrderApiResponseContent>> call = bigBasketApiService.placeOrder(mPotentialOrderId, mSelectedPaymentMethod);
        call.enqueue(new BBNetworkCallback<OldApiResponse<PlaceOrderApiResponseContent>>(this) {
            @Override
            public void onSuccess(OldApiResponse<PlaceOrderApiResponseContent> placeOrderApiResponse) {
                if (placeOrderApiResponse.status.equals(Constants.OK)) {
                    postOrderCreation(placeOrderApiResponse.apiResponseContent.orders,
                            placeOrderApiResponse.apiResponseContent.addMoreLink,
                            placeOrderApiResponse.apiResponseContent.addMoreMsg);
                } else {
                    handler.sendEmptyMessage(placeOrderApiResponse.getErrorTypeAsInt(), placeOrderApiResponse.message);
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

    private void postOrderCreation(ArrayList<Order> orders, String addMoreLink,
                                   String addMoreMsg) {
        ((CartInfoAware) getCurrentActivity()).markBasketDirty();

        if (isCreditCardPayment()) {
            mOrdersCreated = orders;
            mAddMoreLink = addMoreLink;
            mAddMoreMsg = addMoreMsg;
            openPaymentGateway();
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

        Intent invoiceIntent = new Intent(this, OrderThankyouActivity.class);
        invoiceIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ORDER_THANKYOU);
        invoiceIntent.putExtra(Constants.ORDERS, orders);
        invoiceIntent.putExtra(Constants.ADD_MORE_LINK, addMoreLink);
        invoiceIntent.putExtra(Constants.ADD_MORE_MSG, addMoreMsg);

        // Empty all the parameters to free up some memory
        mActiveVouchersList = null;
        paymentTypeList = null;
        mPotentialOrderId = null;
        mTxtApplyVoucher = null;
        mTxtRemoveVoucher = null;
        mTxtApplicableVoucherCount = null;
        mAppliedVoucherCode = null;
        mSelectedPaymentMethod = null;
        mOrderDetails = null;
        mTxnId = null;
        mAddMoreLink = null;
        mAddMoreMsg = null;
        mElapsedTime = null;

        startActivityForResult(invoiceIntent, NavigationCodes.GO_TO_HOME);
    }

    private void openPaymentGateway() {
        mIsPaymentWarningDisplayed = true;
        final View paymentInProgressView = findViewById(R.id.layoutPaymentInProgress);
        paymentInProgressView.setVisibility(View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        final int totalDuration = 5000;
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(totalDuration);
        progressBar.setProgress(0);

        ((TextView) findViewById(R.id.lblOrderPlaced)).setTypeface(faceRobotoRegular);

        new CountDownTimer(totalDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(totalDuration - (int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(totalDuration - 100);
                getPaymentParams();
            }
        }.start();
    }

    private void getPaymentParams() {
        if (isSuspended()) return;
        mIsPaymentWarningDisplayed = false;
        new PaymentHandler<>(this, mPotentialOrderId, mOrdersCreated.get(0).getOrderNumber(),
                mSelectedPaymentMethod, false, false).initiate();
    }

    @Override
    public void setTxnId(String txnId) {
        mTxnId = txnId;
    }

    @Override
    public void onPostPaymentFailure(String txnId, String paymentType) {
        // When transaction has failed. Place order as Cash on Delivery.
        // User can later use 'Pay Now' to complete order.
        String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
        new ValidatePaymentHandler<>(this, mPotentialOrderId, txnId, fullOrderId).start();
    }

    @Override
    public void onPostPaymentSuccess(String txnId, String paymentType) {
        // Now Validate payment from server for excess collection
        String fullOrderId = mOrdersCreated.get(0).getOrderNumber();
        new ValidatePaymentHandler<>(this, mPotentialOrderId, txnId, fullOrderId).start();
    }

    @Override
    public void onPaymentValidated(boolean status, @Nullable String msg, ArrayList<Order> orders) {
        mOrdersCreated = orders;
        if (status || msg == null) {
            showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
        } else {
            // Show a message and then take to Order thank-you page
            showAlertDialog(null, msg, Constants.SOURCE_PLACE_ORDER_DIALOG_REQUEST);
        }
    }

    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
            case Constants.REMOVE_VOUCHER_DIALOG_REQUEST:
                removeVoucher();
                break;
            case Constants.SOURCE_PLACE_ORDER_DIALOG_REQUEST:
                showOrderThankyou(mOrdersCreated, mAddMoreLink, mAddMoreMsg);
                break;
            default:
                super.onPositiveButtonClicked(sourceName, valuePassed);
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

    @Override
    public void onPaymentOptionSelected(String paymentTypeValue) {
        mSelectedPaymentMethod = paymentTypeValue;
        toggleNewCheckoutFlowMsg(isCreditCardPayment());
        renderFooter(true);
    }

    private class OnShowAvailableVouchersListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent availableVoucherListActivity = new Intent(getCurrentActivity(), AvailableVoucherListActivity.class);
            availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
            startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
        }
    }
}
