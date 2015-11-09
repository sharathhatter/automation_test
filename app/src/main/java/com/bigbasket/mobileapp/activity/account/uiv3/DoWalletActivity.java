package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.payment.FundWalletActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.WalletRule;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.CurrentWalletBalance;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit.Call;


public class DoWalletActivity extends BackButtonActivity {

    private boolean oneYearBack1 = false, oneYearBack2 = false, oneYearBack3 = false;
    private int numMonth1, numMonth2, numMonth3, maxDays1, maxDays2, maxDays3;
    private int[] maxDayOfMonth = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    private String month1 = "", month2 = "", month3 = "", monthClickText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentNavigationContext(TrackEventkeys.ACCOUNT_MENU);
        setNextScreenNavigationContext(TrackEventkeys.NAVIGATION_CTX_WALLET_SUMMARY);
        setTitle(getString(R.string.wallet_activity));
        getCurrentMemberWalletBalance(false);
    }

    private void getCurrentMemberWalletBalance(final boolean hasWalletBeenFunded) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        Call<ApiResponse<CurrentWalletBalance>> call = bigBasketApiService.getCurrentWalletBalance();
        call.enqueue(new BBNetworkCallback<ApiResponse<CurrentWalletBalance>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<CurrentWalletBalance> currentWalletBalCallback) {
                if (currentWalletBalCallback.status == 0) {
                    renderWalletMonthActivity(currentWalletBalCallback.apiResponseContent.currentBalance,
                            currentWalletBalCallback.apiResponseContent.walletRule,
                            hasWalletBeenFunded);
                    trackEvent(TrackingAware.WALLET_SUMMARY_SHOWN, null);
                } else {
                    handler.sendEmptyMessage(currentWalletBalCallback.status, currentWalletBalCallback.message);
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressView();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
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
            txtVoucherTc.setText("*" + walletRule.termAndCondition);
            txtVoucherTc.setVisibility(View.VISIBLE);
        }
    }


    private void renderIntent(ArrayList<WalletDataItem> walletDataItemArrayList) {
        Intent intent = new Intent(getCurrentActivity(), WalletActivity.class);
        intent.putParcelableArrayListExtra(Constants.WALLET_ACTIVITY_DATA, walletDataItemArrayList);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }


    private void renderWalletMonthActivity(float currentBalance, WalletRule walletRule,
                                           boolean hasWalletBeenFunded) {
        if (getCurrentActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_dowallet, contentView, false);
        contentView.addView(view);
        ((TextView) view.findViewById(R.id.walletInfoMsg1)).setTypeface(faceRobotoRegular);
        ((TextView) view.findViewById(R.id.walletInfoMsg2)).setTypeface(faceRobotoRegular);
        ((TextView) view.findViewById(R.id.walletInfoMsg3)).setTypeface(faceRobotoRegular);

        String prefixBal = "Current Balance `";
        String mrpStrBal = UIUtil.formatAsMoney((double) (currentBalance)) + "";
        int prefixBalLen = prefixBal.length();
        SpannableString spannableBal = new SpannableString(prefixBal + " " + mrpStrBal);
        spannableBal.setSpan(new CustomTypefaceSpan("", faceRupee), prefixBalLen - 1,
                prefixBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        TextView walletActivitySubHeading = (TextView) view.findViewById(R.id.walletActivitySubHeading);
        walletActivitySubHeading.setTypeface(faceRobotoMedium);

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

                if (DataUtil.isInternetAvailable(getCurrentActivity())) {

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
                    getWalletActivityForMonth(dateFrom, dateTo);
                } else {
                    handler.sendOfflineError(false);
                }


            }
        });

        month2TxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataUtil.isInternetAvailable(getCurrentActivity())) {
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
                    getWalletActivityForMonth(dateFrom, dateTo);
                } else {
                    handler.sendOfflineError(false);
                }
            }
        });
        month3TxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataUtil.isInternetAvailable(getCurrentActivity())) {

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
                    monthClickText = month3;
                    getWalletActivityForMonth(dateFrom, dateTo);

                } else {
                    handler.sendOfflineError(false);
                }
            }
        });

        renderDeliveryTokenData(walletRule, view);
        if (hasWalletBeenFunded) {
            Snackbar.make(contentView, getString(R.string.fundWalletSuccess),
                    Snackbar.LENGTH_LONG).show();
        }

    }

    private void getWalletActivityForMonth(final String dateFrom, final String dateTo) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<ArrayList<WalletDataItem>>> call = bigBasketApiService.getWalletActivity(dateFrom, dateTo);
        call.enqueue(new BBNetworkCallback<ApiResponse<ArrayList<WalletDataItem>>>(this) {
            @Override
            public void onSuccess(ApiResponse<ArrayList<WalletDataItem>> walletActivityCallback) {
                if (walletActivityCallback.status == 0) {
                    if (walletActivityCallback.apiResponseContent != null &&
                            walletActivityCallback.apiResponseContent.size() > 0) {
                        renderIntent(walletActivityCallback.apiResponseContent);
                    } else {
                        showAlertDialog(getString(R.string.noActivityErrorMsg) + " " + monthClickText);
                    }
                } else {
                    handler.sendEmptyMessage(walletActivityCallback.status, walletActivityCallback.message);
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

    public void onFundWalletBtnClicked(View v) {
        Intent intent = new Intent(this, FundWalletActivity.class);
        startActivityForResult(intent, NavigationCodes.FUND_WALLET);
    }

    private void onWalletFunded() {
        getCurrentMemberWalletBalance(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (requestCode == NavigationCodes.FUND_WALLET && resultCode == RESULT_OK) {
            onWalletFunded();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_WALLET_SCREEN;
    }
}
