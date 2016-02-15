package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.adapter.account.WalletActivityListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;

import retrofit2.Call;


public class WalletActivity extends BackButtonActivity implements InvoiceDataAware {

    private ArrayList<WalletDataItem> walletActivityData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.NAVIGATION_CTX_WALLET_ACTIVITIES);
        setTitle(getString(R.string.wallet_activity_header));
        if (savedInstanceState != null) {
            walletActivityData = savedInstanceState.getParcelableArrayList(Constants.WALLET_DATA);
        } else {
            walletActivityData = getIntent().getParcelableArrayListExtra(Constants.WALLET_ACTIVITY_DATA);
        }
        if (walletActivityData == null) return;
        renderWalletActivityList(walletActivityData);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (walletActivityData != null) {
            outState.putParcelableArrayList(Constants.WALLET_DATA, walletActivityData);
        }
        super.onSaveInstanceState(outState);
    }


    private void renderWalletActivityList(final ArrayList<WalletDataItem> walletActivityData) {
        if (getCurrentActivity() == null) return;
        ViewGroup view = getContentView();
        if (view == null) return;

        ListView walletActivityList = new ListView(getCurrentActivity());
        walletActivityList.setDivider(null);
        walletActivityList.setPadding(0, 0, 0, 4);
        walletActivityList.setDividerHeight(0);
        walletActivityList.setAdapter(new WalletActivityListAdapter<>(getCurrentActivity(),
                walletActivityData, faceRobotoRegular, faceRupee));
        walletActivityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    WalletDataItem walletDataItem = walletActivityData.get(position);
                    String orderId = walletDataItem.getOrderNumber();
                    if (orderId != null && !orderId.equals("null")) {
                        if (DataUtil.isInternetAvailable(getCurrentActivity())) {
                            showInvoice(orderId);
                        } else {
                            handler.sendOfflineError(true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        view.addView(walletActivityList);
    }

    private void showInvoice(String orderId) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<OrderInvoice>> call = bigBasketApiService.getInvoice(getPreviousScreenName(), orderId);
        call.enqueue(new CallbackOrderInvoice<>(getCurrentActivity()));
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        orderDetailIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_WALLET_ACTIVITIES);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_WALLET_ACTIVITY_SCREEN;
    }
}
