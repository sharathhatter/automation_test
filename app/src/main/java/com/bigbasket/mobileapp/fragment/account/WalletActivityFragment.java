package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.OrderDetailActivity;
import com.bigbasket.mobileapp.adapter.account.WalletActivityListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.callbacks.CallbackOrderInvoice;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.InvoiceDataAware;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;

/**
 * Created by jugal on 19/9/14.
 */
public class WalletActivityFragment extends BaseFragment implements InvoiceDataAware {

    private ArrayList<WalletDataItem> walletActivityData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            walletActivityData = savedInstanceState.getParcelableArrayList(Constants.WALLET_DATA);
        } else {
            walletActivityData = getArguments().getParcelableArrayList(Constants.WALLET_ACTIVITY_DATA);
        }
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
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;

        ListView walletActivityList = new ListView(getActivity());
        walletActivityList.setDivider(null);
        walletActivityList.setDividerHeight(0);
        walletActivityList.setAdapter(new WalletActivityListAdapter(getActivity(), walletActivityData, faceRobotoRegular));
        walletActivityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    WalletDataItem walletDataItem = walletActivityData.get(position);
                    String orderId = walletDataItem.getOrderNumber();
                    if (orderId != null && !orderId.equals("null")) {
                        if (DataUtil.isInternetAvailable(getActivity())) {
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
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getInvoice(orderId, new CallbackOrderInvoice<>(getActivity()));
    }

    @Override
    public void onDisplayOrderInvoice(OrderInvoice orderInvoice) {
        Intent orderDetailIntent = new Intent(getCurrentActivity(), OrderDetailActivity.class);
        orderDetailIntent.putExtra(Constants.ORDER_REVIEW_SUMMARY, orderInvoice);
        startActivityForResult(orderDetailIntent, NavigationCodes.GO_TO_HOME);
    }


    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Wallet Activity";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return WalletActivityFragment.class.getName();
    }
}
