package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.AvailableVoucherListActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.PostVoucherApiResponse;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.SelectedPaymentAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PaymentSelectionFragment extends BaseFragment {

    private CartSummary mCartSummary;
    private ArrayList<ActiveVouchers> mActiveVouchersList;
    private LinkedHashMap<String, String> mPaymentTypeMap;
    private String mAmtPayable, mWalletUsed, mWalletRemaining;
    private String mPotentialOrderId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        mCartSummary = args.getParcelable(Constants.C_SUMMARY);
        mActiveVouchersList = args.getParcelableArrayList(Constants.VOUCHERS);
        mAmtPayable = args.getString(Constants.AMT_PAYABLE);
        mWalletUsed = args.getString(Constants.WALLET_USED);

        if (TextUtils.isEmpty(mWalletUsed)) {
            mWalletUsed = "0";
        }
        mWalletRemaining = args.getString(Constants.WALLET_REMAINING);
        if (TextUtils.isEmpty(mWalletRemaining)) {
            mWalletRemaining = "0";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPotentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);

        ArrayList<PaymentType> paymentTypes = args.getParcelableArrayList(Constants.PAYMENT_TYPES);
        mPaymentTypeMap = new LinkedHashMap<>();
        for (PaymentType paymentType : paymentTypes) {
            mPaymentTypeMap.put(paymentType.getDisplayName(), paymentType.getValue());
        }
        renderPaymentOptions();
        trackEvent(TrackingAware.CHECKOUT_PAYMENT_SHOWN, null);
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
        View base = inflater.inflate(R.layout.uiv3_payment_option, null);

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
                RadioButton rbtnPaymentType = getPaymentOptionRadioButton();
                rbtnPaymentType.setText(entrySet.getKey());
                rbtnPaymentType.setId(i);
                if (i == 0) {
                    if (getCurrentActivity() != null) {
                        rbtnPaymentType.setChecked(true);
                        ((SelectedPaymentAware) getCurrentActivity()).
                                setPaymentMethod(entrySet.getValue(), entrySet.getKey());
                    } else {
                        return;
                    }
                }
                rbtnPaymentType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked && getCurrentActivity() != null) {
                            trackEvent(TrackingAware.CHECKOUT_PAYMENT_CHOSEN, null);
                            ((SelectedPaymentAware) getCurrentActivity()).
                                    setPaymentMethod(entrySet.getValue(), entrySet.getKey());
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

        TextView txtApplyVoucher = (TextView) base.findViewById(R.id.txtApplyVoucher);
        final EditText editTextVoucherCode = (EditText) base.findViewById(R.id.editTextVoucherCode);
        txtApplyVoucher.setTypeface(faceRobotoRegular);
        txtApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyVoucher(editTextVoucherCode.getText().toString().trim());
            }
        });

        TextView txtViewAvailableVouchers = (TextView) base.findViewById(R.id.txtViewAvailableVouchers);
        if (mActiveVouchersList != null && mActiveVouchersList.size() > 0) {
            txtViewAvailableVouchers.setTypeface(faceRobotoRegular);
            txtViewAvailableVouchers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent availableVoucherListActivity = new Intent(getActivity(), AvailableVoucherListActivity.class);
                    availableVoucherListActivity.putParcelableArrayListExtra(Constants.VOUCHERS, mActiveVouchersList);
                    startActivityForResult(availableVoucherListActivity, NavigationCodes.VOUCHER_APPLIED);
                }
            });
        } else {
            txtViewAvailableVouchers.setVisibility(View.GONE);
        }

        contentView.addView(base);
    }

    private RadioButton getPaymentOptionRadioButton() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.uiv3_payment_option_rbtn, null);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_small));
        radioButton.setLayoutParams(layoutParams);
        radioButton.setTypeface(faceRobotoRegular);
        return radioButton;
    }

    private void applyVoucher(String voucherCode) {
        if (TextUtils.isEmpty(voucherCode)) {
            return;
        }
        if (checkInternetConnection()) {
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
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
                    switch (postVoucherApiResponse.status) {
                        case Constants.OK:
                            // TODO : Add previous applied voucher handling logic for credit card
                            String voucherMsg;
                            if (!TextUtils.isEmpty(postVoucherApiResponse.evoucherMsg)) {
                                voucherMsg = postVoucherApiResponse.evoucherMsg;
                            } else {
                                voucherMsg = "eVoucher has been successfully applied";
                            }
                            showErrorMsg(voucherMsg);
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_APPLIED, null);
                            break;
                        default:
                            handler.sendEmptyMessage(postVoucherApiResponse.getErrorTypeAsInt());
                            trackEvent(TrackingAware.CHECKOUT_VOUCHER_FAILED, null);
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
            showErrorMsg(getString(R.string.connectionOffline));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.VOUCHER_APPLIED && data != null) {
            String voucherCode = data.getStringExtra(Constants.EVOUCHER_CODE);
            if (!TextUtils.isEmpty(voucherCode)) {
                applyVoucher(voucherCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void setTitle() {
        // Do nothing
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return PaymentSelectionFragment.class.getName();
    }
}