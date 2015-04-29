package com.bigbasket.mobileapp.fragment.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.WalletRule;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.CurrentWalletBalance;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class DoWalletFragment extends BaseFragment {

    boolean oneYearBack1 = false, oneYearBack2 = false, oneYearBack3 = false;
    private int numMonth1, numMonth2, numMonth3, maxDays1, maxDays2, maxDays3;
    private int[] maxDayOfMonth = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    private String month1 = "", month2 = "", month3 = "", monthClickText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getCurrentMemberWalletBalance();
    }

    private void getCurrentMemberWalletBalance() {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            handler.sendOfflineError(true);
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getCurrentWalletBalance(new Callback<ApiResponse<CurrentWalletBalance>>() {
            @Override
            public void success(ApiResponse<CurrentWalletBalance> currentWalletBalCallback, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (currentWalletBalCallback.status == 0) {
                    renderWalletMonthActivity(currentWalletBalCallback.apiResponseContent.currentBalance,
                            currentWalletBalCallback.apiResponseContent.walletRule);
                    trackEvent(TrackingAware.WALLET_SUMMARY_SHOWN, null);
                } else {
                    handler.sendEmptyMessage(currentWalletBalCallback.status, currentWalletBalCallback.message);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void renderDeliveryTokenData(WalletRule walletRule, View view) {
        if (walletRule == null) return;
        LinearLayout layoutDeliveryToken = (LinearLayout) view.findViewById(R.id.layoutDeliveryToken);
        layoutDeliveryToken.setVisibility(View.VISIBLE);

        TextView txtTokenMsg = (TextView) view.findViewById(R.id.txtTokenMsg);

        String prefixTokenAmt = walletRule.voucherPerRule > 1 ? walletRule.voucherPerRule + " Free delivery tokens for every"
                : walletRule.voucherPerRule + " Free delivery token for every";
        prefixTokenAmt += " `";
        String mrpStrTokenAmt = UIUtil.formatAsMoney(
                Double.parseDouble(walletRule.amountPerVoucher + "")) + "/- added to wallet*";
        int prefixEndBalLen = prefixTokenAmt.length();
        SpannableString spannableEndingBal = new SpannableString(prefixTokenAmt + mrpStrTokenAmt);

        spannableEndingBal.setSpan(new CustomTypefaceSpan("", faceRupee), prefixEndBalLen - 1,
                prefixEndBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtTokenMsg.setText(spannableEndingBal);

        TextView txtAvailableToken = (TextView) view.findViewById(R.id.txtAvailableToken);
        txtAvailableToken.setText("Available Delivery Token: " + walletRule.availableDeliveryToken);

        TextView txtVoucherTc = (TextView) view.findViewById(R.id.txtVoucherTc);
        if (!TextUtils.isEmpty(walletRule.termAndCondition)) {
            txtVoucherTc.setText(walletRule.termAndCondition);
            txtVoucherTc.setVisibility(View.VISIBLE);
        }
    }


    public void renderIntent(ArrayList<WalletDataItem> walletDataItemArrayList) {
        WalletActivityFragment walletActivityFragment = new WalletActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.WALLET_ACTIVITY_DATA, walletDataItemArrayList);
        walletActivityFragment.setArguments(bundle);
        changeFragment(walletActivityFragment);

    }


    private void renderWalletMonthActivity(float currentBalance, WalletRule walletRule) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_dowallet, contentView, false);
        contentView.addView(view);
        ((TextView) view.findViewById(R.id.walletInfoMsg1)).setTypeface(faceRobotoLight);

        String prefixBal = "Current Balance `";
        String mrpStrBal = currentBalance + "";
        int prefixBalLen = prefixBal.length();
        SpannableString spannableBal = new SpannableString(prefixBal + " " + mrpStrBal);
        spannableBal.setSpan(new CustomTypefaceSpan("", faceRupee), prefixBalLen - 1,
                prefixBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        TextView txtCurrentBalance = (TextView) view.findViewById(R.id.txtcurrentBalance);
        txtCurrentBalance.setText(spannableBal);
        txtCurrentBalance.setTypeface(faceRobotoRegular);

        //get month

        final Calendar date = Calendar.getInstance();
        String[] nameOfMonth = {"January", "February", "March", "April",
                "May", "June", "July", "August", "September", "October",
                "November", "December"};
        int numberOfMonth = date.get(Calendar.MONTH);
        numberOfMonth = numberOfMonth + 1;

        if (numberOfMonth == 0) {
            numMonth1 = 12;
            numberOfMonth = 11;
            oneYearBack1 = true;
            maxDays1 = numberOfMonth;
        } else {
            numMonth1 = numberOfMonth;
            numberOfMonth = numberOfMonth - 1;
            maxDays1 = numberOfMonth;
        }
        int year = date.get(Calendar.YEAR);
        if (oneYearBack1)
            year = year - 1;
        month1 = nameOfMonth[numberOfMonth];
        month1 = month1 + " " + year;


        if (numberOfMonth == 0) {
            numMonth2 = 12;
            numberOfMonth = 11;
            oneYearBack2 = true;
            maxDays2 = numberOfMonth;
        } else {
            numMonth2 = numberOfMonth;
            numberOfMonth = numberOfMonth - 1;
            maxDays2 = numberOfMonth;
        }
        if (oneYearBack2)
            year = year - 1;
        month2 = nameOfMonth[numberOfMonth];
        month2 = month2 + " " + year;


        if (numberOfMonth == 0) {
            numMonth3 = 12;
            numberOfMonth = 11;
            oneYearBack3 = true;
            maxDays3 = numberOfMonth;
        } else {
            numMonth3 = numberOfMonth;
            numberOfMonth = numberOfMonth - 1;
            maxDays3 = numberOfMonth;
        }
        if (oneYearBack3)
            year = year - 1;
        month3 = nameOfMonth[numberOfMonth];
        month3 = month3 + " " + year;

        TextView walletActivityFor = (TextView) view.findViewById(R.id.walletActivityFor);
        walletActivityFor.setTypeface(faceRobotoRegular);


        TextView month1TxtView = (TextView) view.findViewById(R.id.month1);
        month1TxtView.setText(month1);
        month1TxtView.setTypeface(faceRobotoRegular);


        TextView month2TxtView = (TextView) view.findViewById(R.id.month2);
        month2TxtView.setText(month2);
        month2TxtView.setTypeface(faceRobotoRegular);


        TextView month3TxtView = (TextView) view.findViewById(R.id.month3);
        month3TxtView.setText(month3);
        month3TxtView.setTypeface(faceRobotoRegular);

        month1TxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DataUtil.isInternetAvailable(getActivity())) {

                    final Calendar date = Calendar.getInstance();
                    int year = date.get(Calendar.YEAR);
                    if (oneYearBack1)
                        year = year - 1;
                    date.add(Calendar.MONTH, 0);
                    date.set(Calendar.DAY_OF_MONTH, 1);
                    int m1 = maxDayOfMonth[maxDays1];
                    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
                        m1 = 28;
                    final String dateFrom = "01-" + (numMonth1) + "-" + year;
                    final String dateTo = m1 + "-" + (numMonth1) + "-" + year;
                    Log.d("Date to::::::::::::", dateTo);
                    Log.d("Date from::::::::::", dateFrom);

                    monthClickText = month1;
                    logWalletActivityClickEvent(numMonth1, year);
                    getWalletActivityForMonth(dateFrom, dateTo);
                } else {
                    String msg = "Cannot proceed with the operation. No network connection.";
                    showErrorMsg(msg);
                }


            }
        });

        month2TxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataUtil.isInternetAvailable(getActivity())) {
                    final Calendar date = Calendar.getInstance();
                    int year = date.get(Calendar.YEAR);
                    if (oneYearBack2)
                        year = year - 1;
                    date.add(Calendar.MONTH, -1);
                    date.set(Calendar.DAY_OF_MONTH, 1);

                    int m2 = maxDayOfMonth[maxDays2];
                    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
                        m2 = 28;
                    final String dateFrom = "01-" + (numMonth2) + "-" + year;
                    final String dateTo = m2 + "-" + (numMonth2) + "-" + year;
                    Log.d("Date to::::::::::::", dateTo);
                    Log.d("Date from::::::::::", dateFrom);
                    monthClickText = month2;
                    logWalletActivityClickEvent(numMonth2, year);
                    getWalletActivityForMonth(dateFrom, dateTo);
                } else {
                    String msg = "Cannot proceed with the operation. No network connection.";
                    showErrorMsg(msg);
                }
            }
        });
        month3TxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataUtil.isInternetAvailable(getActivity())) {

                    final Calendar date = Calendar.getInstance();
                    int year = date.get(Calendar.YEAR);
                    if (oneYearBack2 || oneYearBack3)
                        year = year - 1;
                    date.add(Calendar.MONTH, -2);
                    date.set(Calendar.DAY_OF_MONTH, 1);
                    int m3 = maxDayOfMonth[maxDays3];
                    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
                        m3 = 28;
                    final String dateFrom = "01-" + (numMonth3) + "-" + year;
                    final String dateTo = m3 + "-" + (numMonth3) + "-" + year;
                    Log.d("Date to::::::::::::", dateTo);
                    Log.d("Date from::::::::::", dateFrom);
                    monthClickText = month1;
                    logWalletActivityClickEvent(numMonth3, year);
                    getWalletActivityForMonth(dateFrom, dateTo);

                } else {
                    String msg = "Cannot proceed with the operation. No network connection.";
                    showErrorMsg(msg);
                }
            }
        });

        renderDeliveryTokenData(walletRule, view);
    }

    private void logWalletActivityClickEvent(int month, int year) {
        final HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.MONTH, String.valueOf(month));
        map.put(TrackEventkeys.YEAR, String.valueOf(year));
        trackEvent(TrackingAware.WALLET_ACTIVITY_FOR_MONTH_CLICKED, map);
    }


    private void getWalletActivityForMonth(final String dateFrom, final String dateTo) {
        if (!DataUtil.isInternetAvailable(getActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getWalletActivity(dateFrom, dateTo,
                new Callback<ApiResponse<ArrayList<WalletDataItem>>>() {
                    @Override
                    public void success(ApiResponse<ArrayList<WalletDataItem>> walletActivityCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (walletActivityCallback.status == 0) {
                            if (walletActivityCallback.apiResponseContent != null &&
                                    walletActivityCallback.apiResponseContent.size() > 0) {
                                renderIntent(walletActivityCallback.apiResponseContent);
                            } else {
                                showErrorMsg(getString(R.string.noActivityErrorMsg) + " " + monthClickText);
                            }
                        } else {
                            handler.sendEmptyMessage(walletActivityCallback.status, walletActivityCallback.message);
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

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

//    @Override
//    public LinearLayout getContentView() {
//        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutDoWallet) : null;
//    }

    @Override
    public String getTitle() {
        return "My Wallet";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return DoWalletFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_WALLET_SCREEN;
    }
}
