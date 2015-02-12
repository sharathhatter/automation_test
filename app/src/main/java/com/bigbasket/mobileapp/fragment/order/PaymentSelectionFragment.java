package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.AvailableVoucherListActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.OnApplyVoucherListener;
import com.bigbasket.mobileapp.interfaces.OnObservableScrollEvent;
import com.bigbasket.mobileapp.interfaces.PostVoucherAppliedListener;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class PaymentSelectionFragment extends BaseFragment implements PostVoucherAppliedListener {

    private CartSummary mCartSummary;
    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private LinkedHashMap<String, String> mPaymentTypeMap;
    private String mAmtPayable, mWalletUsed, mWalletRemaining;//, mpaymentMethod, potentialOrderId;
    private TextView mLblTransactionFailed;
    private TextView mTxtTransactionFailureReason;
    private TextView mLblSelectAnotherMethod;
    private TextView mTxtApplyVoucher;
    private TextView mTxtRemoveVoucher;
    private String mAppliedVoucherCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        mCartSummary = args.getParcelable(Constants.C_SUMMARY);
        mActiveVouchersList = args.getParcelableArrayList(Constants.VOUCHERS);
        mAmtPayable = args.getString(Constants.AMT_PAYABLE);
        mWalletUsed = args.getString(Constants.WALLET_USED);
        mAppliedVoucherCode = args.getString(Constants.EVOUCHER_CODE);

        if (TextUtils.isEmpty(mWalletUsed)) {
            mWalletUsed = "0";
        }
        mWalletRemaining = args.getString(Constants.WALLET_REMAINING);
        if (TextUtils.isEmpty(mWalletRemaining)) {
            mWalletRemaining = "0";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor mEditor = preferences.edit();

        ArrayList<PaymentType> paymentTypes = args.getParcelableArrayList(Constants.PAYMENT_TYPES);
        mPaymentTypeMap = new LinkedHashMap<>();
        for (PaymentType paymentType : paymentTypes) {
            mPaymentTypeMap.put(paymentType.getDisplayName(), paymentType.getValue());
        }

        PayuResponse payuResponse = PayuResponse.getInstance(getActivity());
        if (payuResponse != null && payuResponse.isSuccess()) {
            renderPayuFailedToCreateOrderScenario();
        } else {
            renderPaymentOptions();
            trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null);

            String payuFailureReason = args.getString(Constants.PAYU_CANCELLED);
            if (!TextUtils.isEmpty(payuFailureReason)) {
                displayPayuFailure(payuFailureReason);
            }
        }
    }

    private void renderPayuFailedToCreateOrderScenario() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_payment_option, contentView, false);
        View layoutPaymentContainer = base.findViewById(R.id.layoutPaymentContainer);
        layoutPaymentContainer.setVisibility(View.GONE);

        contentView.addView(base);
    }

    @Nullable
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    private void renderPaymentOptions() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_payment_option, contentView, false);

        View layoutPressOrderReview = base.findViewById(R.id.layoutPressOrderReviewContainer);
        layoutPressOrderReview.setVisibility(View.GONE);

        TextView lblPaymentMethod = (TextView) base.findViewById(R.id.lblPaymentMethod);
        View layoutChoosePayment = base.findViewById(R.id.layoutChoosePayment);

        double amtPayable = Double.parseDouble(mAmtPayable);
        double walletUsed = Double.parseDouble(mWalletUsed);
        double walletRemaining = Double.parseDouble(mWalletRemaining);

        TextView lblWalletUsed = (TextView) base.findViewById(R.id.lblWalletUsed);
        TextView txtWalletUsed = (TextView) base.findViewById(R.id.txtWalletUsed);
        TextView lblAmtToPay = (TextView) base.findViewById(R.id.lblOrderTotal);
        TextView txtAmtToPay = (TextView) base.findViewById(R.id.txtOrderTotal);
        TextView lblWalletRemaining = (TextView) base.findViewById(R.id.lblWalletRemaining);
        TextView txtWalletRemaining = (TextView) base.findViewById(R.id.txtWalletRemaining);
        TextView lblSaving = (TextView) base.findViewById(R.id.lblSaving);
        TextView txtSaving = (TextView) base.findViewById(R.id.txtSaving);

        lblAmtToPay.setTypeface(faceRobotoRegular);
        txtAmtToPay.setTypeface(faceRobotoRegular);
        txtAmtToPay.setText(asRupeeSpannable(amtPayable));

        if (walletUsed > 0) {
            lblWalletUsed.setTypeface(faceRobotoRegular);
            txtWalletUsed.setTypeface(faceRobotoRegular);
            txtWalletUsed.setText(asRupeeSpannable(walletUsed));

            lblWalletRemaining.setTypeface(faceRobotoRegular);
            txtWalletRemaining.setTypeface(faceRobotoRegular);
            txtWalletRemaining.
                    setText(walletRemaining > 0 ? asRupeeSpannable(walletRemaining) : "0");
        } else {
            lblWalletRemaining.setVisibility(View.GONE);
            lblWalletUsed.setVisibility(View.GONE);
            txtWalletRemaining.setVisibility(View.GONE);
            txtWalletUsed.setVisibility(View.GONE);
        }

        if (mCartSummary.getSavings() > 0) {
            lblSaving.setTypeface(faceRobotoRegular);
            txtSaving.setTypeface(faceRobotoRegular);
            txtSaving.setText(asRupeeSpannable(mCartSummary.getSavings()));
        } else {
            lblSaving.setVisibility(View.GONE);
            txtSaving.setVisibility(View.GONE);
        }

        if (amtPayable > 0) {
            RadioGroup layoutPaymentOptions = (RadioGroup) base.findViewById(R.id.layoutPaymentOptions);
            int i = 0;
            for (final Map.Entry<String, String> entrySet : mPaymentTypeMap.entrySet()) {
                RadioButton rbtnPaymentType = getPaymentOptionRadioButton(layoutPaymentOptions);
                rbtnPaymentType.setText(entrySet.getKey());
                rbtnPaymentType.setId(i);
                if (i == 0) {
                    if (getCurrentActivity() != null) {
                        rbtnPaymentType.setChecked(true);
                        ((SelectedPaymentAware) getCurrentActivity()).
                                setPaymentMethod(entrySet.getValue());
                    } else {
                        return;
                    }
                }
                rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked && getCurrentActivity() != null) {
                            ((SelectedPaymentAware) getCurrentActivity()).
                                    setPaymentMethod(entrySet.getValue());
                        }
                    }
                });
                layoutPaymentOptions.addView(rbtnPaymentType);
                i++;
            }
            lblPaymentMethod.setTypeface(faceRobotoRegular);
        } else {
            lblPaymentMethod.setVisibility(View.GONE);
            layoutChoosePayment.setVisibility(View.GONE);
        }

        mTxtApplyVoucher = (TextView) base.findViewById(R.id.txtApplyVoucher);
        mTxtApplyVoucher.setTypeface(faceRobotoRegular);
        mTxtApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent availableVoucherListActivity = new Intent(getActivity(), AvailableVoucherListActivity.class);
                availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
                startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
            }
        });

        mTxtRemoveVoucher = (TextView) base.findViewById(R.id.txtRemoveVoucher);
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
        ObservableScrollView scrollViewPaymentOption = (ObservableScrollView) base.findViewById(R.id.scrollViewPaymentOption);
        scrollViewPaymentOption.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int i, boolean b, boolean b2) {

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
                if (scrollState == ScrollState.UP) {
                    ((OnObservableScrollEvent) getActivity()).onScrollUp();
                } else if (scrollState == ScrollState.DOWN) {
                    ((OnObservableScrollEvent) getActivity()).onScrollDown();
                }
            }
        });
        mLblTransactionFailed = (TextView) base.findViewById(R.id.lblTransactionFailed);
        mTxtTransactionFailureReason = (TextView) base.findViewById(R.id.txtTransactionFailedReason);
        mLblSelectAnotherMethod = (TextView) base.findViewById(R.id.lblSelectAnotherMethod);

        mLblTransactionFailed.setTypeface(faceRobotoRegular);
        mTxtTransactionFailureReason.setTypeface(faceRobotoRegular);
        mLblTransactionFailed.setVisibility(View.GONE);
        mTxtTransactionFailureReason.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setVisibility(View.GONE);
        mLblSelectAnotherMethod.setTypeface(faceRobotoRegular);

        contentView.addView(base);
    }

    private RadioButton getPaymentOptionRadioButton(ViewGroup parent) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.uiv3_payment_option_rbtn, parent, false);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_small));
        radioButton.setLayoutParams(layoutParams);
        radioButton.setTypeface(faceRobotoRegular);
        return radioButton;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        switch (resultCode) {
            case NavigationCodes.VOUCHER_APPLIED:
                if (data != null) {
                    String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
                    if (!TextUtils.isEmpty(voucherCode) && getActivity() != null) {
                        ((OnApplyVoucherListener) getActivity()).applyVoucher(voucherCode);
                    }
                }
                break;
            case Constants.PAYU_ABORTED:
                displayPayuFailure(getString(R.string.youAborted));
                break;
            case Constants.PAYU_FAILED:
                displayPayuFailure(getString(R.string.failedToProcess));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;

        }
    }

    public void displayPayuFailure(String reason) {
        if (mTxtTransactionFailureReason == null || mLblTransactionFailed == null
                || mLblSelectAnotherMethod == null) return;
        mTxtTransactionFailureReason.setVisibility(View.VISIBLE);
        mLblTransactionFailed.setVisibility(View.VISIBLE);
        mLblSelectAnotherMethod.setVisibility(View.GONE);

        mTxtTransactionFailureReason.setText(reason);
    }

    @Override
    public void setTitle() {
        // Do nothing
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PaymentSelectionFragment.class.getName();
    }

    @Override
    public void onVoucherApplied(String voucher) {
        if (!TextUtils.isEmpty(voucher)) {
            mTxtApplyVoucher.setVisibility(View.GONE);
            mTxtRemoveVoucher.setVisibility(View.VISIBLE);
            mAppliedVoucherCode = voucher;
        }
    }

    @Override
    public void onVoucherRemoved() {
        mTxtApplyVoucher.setVisibility(View.VISIBLE);
        mTxtRemoveVoucher.setVisibility(View.GONE);
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, @Nullable String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equals(Constants.REMOVE_VOUCHER)
                && valuePassed != null) {
            ((OnApplyVoucherListener) getActivity()).removeVoucher(valuePassed.toString());
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }
}