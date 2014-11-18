package com.bigbasket.mobileapp.fragment.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.account.WalletActivityListAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.account.WalletDataItem;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.ParserUtil;

import java.util.ArrayList;

/**
 * Created by jugal on 19/9/14.
 */
public class WalletActivityFragment extends BaseFragment {

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
            String walletActivityStringData = getArguments().getString(Constants.WALLET_ACTIVITY_DATA);
            walletActivityData = ParserUtil.getListData(walletActivityStringData);
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
//                            Intent inn = new Intent(getActivity(), OrderReview.class);
//                            inn.putExtra(Constants.ORDER_ID, orderId);
//                            startActivityForResult(inn, Constants.GO_TO_HOME);
//                            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                            // TODO : Replace this with equivalent fragment call

                        } else {
                            showErrorMsg(getString(R.string.checkinternet));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        view.addView(walletActivityList);
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
