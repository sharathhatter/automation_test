package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponse;
import com.bigbasket.mobileapp.handler.HDFCPowerPayHandler;
import com.bigbasket.mobileapp.interfaces.PostVoucherAppliedListener;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.CreditDetails;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.model.order.PowerPayResponse;
import com.bigbasket.mobileapp.model.order.VoucherApplied;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentSelectionActivity extends BackButtonActivity {

    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private LinkedHashMap<String, String> mPaymentTypeMap;
    private String mPotentialOrderId;
    private ArrayList<VoucherApplied> mVoucherAppliedList;
    private HashMap<String, Boolean> mPreviouslyAppliedVoucherMap;  // TODO: Add handling
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private String mAppliedVoucherCode;
    private TextView mLblTransactionFailed;
    private TextView mTxtTransactionFailureReason;
    private TextView mLblSelectAnotherMethod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPotentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);

        mLblTransactionFailed = (TextView) findViewById(R.id.lblTransactionFailed);
        mTxtTransactionFailureReason = (TextView) findViewById(R.id.txtTransactionFailedReason);
        mLblSelectAnotherMethod = (TextView) findViewById(R.id.lblSelectAnotherMethod);

        mLblTransactionFailed.setTypeface(faceRobotoRegular);
        mTxtTransactionFailureReason.setTypeface(faceRobotoRegular);
        mLblSelectAnotherMethod.setTypeface(faceRobotoRegular);

        if (TextUtils.isEmpty(mPotentialOrderId)) return;
        setTitle(getString(R.string.placeorder));

        PayuResponse payuResponse = PayuResponse.getInstance(this);
        PowerPayResponse powerPayResponse = PowerPayResponse.getInstance(this);
        String payuFailureReason = getIntent().getStringExtra(Constants.PAYU_CANCELLED);
        if (TextUtils.isEmpty(payuFailureReason)
                && ((payuResponse != null && payuResponse.isSuccess())
                || (powerPayResponse != null && powerPayResponse.isSuccess()))) {
            renderPayuFailedToCreateOrderScenario();
        } else {
            renderPaymentDetails();
            if (!TextUtils.isEmpty(payuFailureReason)) {
                PayuResponse.clearTxnDetail(this);
                PowerPayResponse.clearTxnDetail(this);
                displayPayuFailure(payuFailureReason);
            }
        }
    }

    private void renderPaymentDetails() {
        mActiveVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        mAppliedVoucherCode = getIntent().getStringExtra(Constants.EVOUCHER_CODE);

        ArrayList<PaymentType> paymentTypes = getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES);
        mPaymentTypeMap = new LinkedHashMap<>();
        for (PaymentType paymentType : paymentTypes) {
            mPaymentTypeMap.put(paymentType.getDisplayName(), paymentType.getValue());
        }
        renderPaymentMethodsAndSummary();
    }

    private void renderPayuFailedToCreateOrderScenario() {
        View layoutPaymentContainer = findViewById(R.id.layoutPaymentContainer);
        layoutPaymentContainer.setVisibility(View.GONE);
    }

    private void renderPaymentMethodsAndSummary() {
        View layoutPressOrderReview = findViewById(R.id.layoutPressOrderReviewContainer);
        layoutPressOrderReview.setVisibility(View.GONE);

        // Show invoice and other order details
        LayoutInflater inflater = getLayoutInflater();
        int normalColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int secondaryColor = getResources().getColor(R.color.uiv3_secondary_text_color);
        int orderTotalLabelColor = getResources().getColor(R.color.uiv3_primary_text_color);
        int orderTotalValueColor = getResources().getColor(R.color.uiv3_ok_label_color);
        LinearLayout layoutOrderSummaryInfo = (LinearLayout) findViewById(R.id.layoutOrderSummaryInfo);

        OrderDetails orderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        ArrayList<CreditDetails> creditDetails = getIntent().getParcelableArrayListExtra(Constants.CREDIT_DETAILS);

        View paymentInformationRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.paymentMethod),
                orderDetails.getPaymentMethodDisplay(), normalColor, secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(paymentInformationRow);

        String numItems = orderDetails.getTotalItems() + " Item" + (orderDetails.getTotalItems() > 1 ? "s" : "");
        View orderItemsRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.orderItems),
                numItems, normalColor, secondaryColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(orderItemsRow);

        View subTotalRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.subTotal),
                UIUtil.asRupeeSpannable(orderDetails.getSubTotal(), faceRupee), normalColor, secondaryColor,
                faceRobotoRegular);
        layoutOrderSummaryInfo.addView(subTotalRow);

        View deliveryChargeRow = UIUtil.getOrderSummaryRow(inflater, getString(R.string.deliveryCharges),
                UIUtil.asRupeeSpannable(orderDetails.getDeliveryCharge(), faceRupee), normalColor,
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
                UIUtil.asRupeeSpannable(orderDetails.getFinalTotal(), faceRupee), orderTotalLabelColor,
                orderTotalValueColor, faceRobotoRegular);
        layoutOrderSummaryInfo.addView(finalTotalRow);

        mTxtApplyVoucher = (TextView) findViewById(R.id.txtApplyVoucher);
        mTxtApplyVoucher.setTypeface(faceRobotoRegular);
        mTxtApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent availableVoucherListActivity = new Intent(getCurrentActivity(), AvailableVoucherListActivity.class);
                availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
                startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
            }
        });

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
            onVoucherApplied(mAppliedVoucherCode);
        } else {
            onVoucherRemoved();
        }

        boolean isInHDFCPayMode = HDFCPowerPayHandler.isInHDFCPayMode(this)
                && mPaymentTypeMap.containsValue(Constants.HDFC_POWER_PAY);
        RadioGroup layoutPaymentOptions = (RadioGroup) findViewById(R.id.layoutPaymentOptions);
        int i = 0;
        for (final Map.Entry<String, String> entrySet : mPaymentTypeMap.entrySet()) {
            if (isInHDFCPayMode && !entrySet.getValue().equals(Constants.HDFC_POWER_PAY)) {
                continue;
            }
            RadioButton rbtnPaymentType = getPaymentOptionRadioButton(layoutPaymentOptions);
            rbtnPaymentType.setText(entrySet.getKey());
            rbtnPaymentType.setId(i);
            if (i == 0) {
                if (getCurrentActivity() != null) {
                    rbtnPaymentType.setChecked(true);
//                    ((SelectedPaymentAware) getCurrentActivity()).
//                            setPaymentMethod(entrySet.getValue());
                } else {
                    return;
                }
            }
            rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked && getCurrentActivity() != null) {
//                        ((SelectedPaymentAware) getCurrentActivity()).
//                                setPaymentMethod(entrySet.getValue());
//                    }
                }
            });
            layoutPaymentOptions.addView(rbtnPaymentType);
            i++;
        }

        mLblTransactionFailed.setTypeface(faceRobotoRegular);
        mTxtTransactionFailureReason.setTypeface(faceRobotoRegular);
        mLblTransactionFailed.setVisibility(View.GONE);
        mTxtTransactionFailureReason.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setTypeface(faceRobotoRegular);
    }

    public void displayPayuFailure(String reason) {
        if (mTxtTransactionFailureReason == null || mLblTransactionFailed == null
                || mLblSelectAnotherMethod == null) return;
        mTxtTransactionFailureReason.setVisibility(View.VISIBLE);
        mLblTransactionFailed.setVisibility(View.VISIBLE);
        mLblSelectAnotherMethod.setVisibility(View.GONE);

        mTxtTransactionFailureReason.setText(reason);
    }

    private RadioButton getPaymentOptionRadioButton(ViewGroup parent) {
        LayoutInflater inflater = getLayoutInflater();
        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.uiv3_payment_option_rbtn, parent, false);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_small));
        radioButton.setLayoutParams(layoutParams);
        radioButton.setTypeface(faceRobotoRegular);
        return radioButton;
    }


    public void onVoucherApplied(String voucher) {
        if (!TextUtils.isEmpty(voucher)) {
            mTxtApplyVoucher.setVisibility(View.GONE);
            mTxtRemoveVoucher.setVisibility(View.VISIBLE);
            mAppliedVoucherCode = voucher;
        }
    }

    public void onVoucherRemoved() {
        mTxtApplyVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setVisibility(View.GONE);
    }

    public void applyVoucher(final String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
            showProgressDialog(getString(R.string.please_wait));
            bigBasketApiService.postVoucher(mPotentialOrderId, voucherCode, new Callback<PostVoucherApiResponse>() {
                @Override
                public void success(PostVoucherApiResponse postVoucherApiResponse, Response response) {
                    if (isSuspended()) return;
                    try {
                        hideProgressDialog();
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.POTENTIAL_ORDER, mPotentialOrderId);
                    map.put(TrackEventkeys.VOUCHER_NAME, voucherCode);
                    switch (postVoucherApiResponse.status) {
                        case Constants.OK:
                            if (mPreviouslyAppliedVoucherMap == null ||
                                    mPreviouslyAppliedVoucherMap.size() == 0) {
                                trackEvent(TrackingAware.CHECKOUT_VOUCHER_APPLIED, map);
                            }
                            onVoucherSuccessfullyApplied(voucherCode);
                            break;
                        default:
                            handler.sendEmptyMessage(postVoucherApiResponse.getErrorTypeAsInt(), postVoucherApiResponse.message);
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

    public void removeVoucher(final String voucherCode) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.removeVoucher(mPotentialOrderId, new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse removeVoucherApiResponse, Response response) {
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
                        onVoucherRemoved(voucherCode);
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
    private void onVoucherSuccessfullyApplied(String voucherCode) {
        if (mPreviouslyAppliedVoucherMap != null) {
            mPreviouslyAppliedVoucherMap.put(voucherCode, true);
            boolean allApplied = true;
            for (Map.Entry<String, Boolean> entry : mPreviouslyAppliedVoucherMap.entrySet()) {
                if (!entry.getValue()) {
                    allApplied = false;
                    applyVoucher(entry.getKey());
                    break;
                }
            }
            if (allApplied) {
                // TODO : Plugin place order
            }
        } else {
            if (mVoucherAppliedList == null) {
                mVoucherAppliedList = new ArrayList<>();
            }
            mVoucherAppliedList.add(new VoucherApplied(voucherCode));
            VoucherApplied.saveToPreference(mVoucherAppliedList, this);
            onVoucherApplied(voucherCode);
        }
    }

    private void onVoucherRemoved(String voucherCode) {
        if (mPreviouslyAppliedVoucherMap != null
                && mPreviouslyAppliedVoucherMap.containsKey(voucherCode)) {
            mPreviouslyAppliedVoucherMap.remove(voucherCode);
        }
        for (Fragment fg : getSupportFragmentManager().getFragments()) {
            if (fg instanceof PostVoucherAppliedListener) {
                ((PostVoucherAppliedListener) fg).onVoucherRemoved();
            }
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, @Nullable String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equals(Constants.REMOVE_VOUCHER)
                && valuePassed != null) {
            removeVoucher(valuePassed.toString());
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_payment_option;
    }
}
