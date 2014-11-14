package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.adapter.account.MemberAddressListAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.etsy.android.grid.StaggeredGridView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;


public class MemberAddressListFragment extends BaseFragment {

    protected ArrayList<Address> addressArrayList;
    private boolean fromAccountPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        fromAccountPage = args != null && args.getBoolean(Constants.FROM_ACCOUNT_PAGE, false);
        if (savedInstanceState != null) {
            addressArrayList = savedInstanceState.getParcelableArrayList(Constants.ADDRESSES);
            if (addressArrayList != null) {
                showAddresses();
                return;
            }
        }
        loadAddresses();
    }

    private void loadAddresses() {
        if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
            ((BaseActivity) getActivity()).showAlertDialog(getActivity(), "BigBasket",
                    "You are not signed in.\nPlease sign-in to continue", Constants.LOGIN_REQUIRED);
            return;
        }
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_DELIVERY_ADDR, null, false, true, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        if (httpOperationResult.getUrl().contains(Constants.GET_DELIVERY_ADDR)) {
            JsonObject jsonObject = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            switch (status) {
                case 0:
                    parseAddresses(jsonObject);
                    break;
                case ExceptionUtil.INTERNAL_SERVER_ERROR:
                    ((BaseActivity) getActivity()).showAlertDialog(getActivity(), "BigBasket", "Server Error");
                    break;
                case ExceptionUtil.EMPTY_ADDRESS:
                    showCreateAddressForm();
                    break;
                default:
                    String msg = jsonObject.get(Constants.MESSAGE).getAsString();
                    ((BaseActivity) getActivity()).showAlertDialog(getActivity(), "BigBasket", msg);
                    break;
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void parseAddresses(JsonObject jsonObject) {
        JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
        addressArrayList = ParserUtil.parseAddressList(
                responseJsonObject.get(Constants.ADDRESSES).toString());
        showAddresses();
    }

    private void showAddresses() {
        if (addressArrayList.size() > 0) {
            renderAddressList();
        } else {
            showCreateAddressForm();
        }
    }

    private void renderAddressList() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addressView = layoutInflater.inflate(R.layout.uiv3_fab_staggered_grid, null);

        StaggeredGridView addressListView = (StaggeredGridView) addressView.findViewById(R.id.fabStaggeredGridView);
        MemberAddressListAdapter memberAddressListAdapter =
                new MemberAddressListAdapter(addressArrayList, getActivity(), fromAccountPage);
        addressListView.setAdapter(memberAddressListAdapter);
        addressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OnAddressSelected(addressArrayList.get(position));
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) addressView.findViewById(R.id.btnFab);
        floatingActionButton.attachToListView(addressListView);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateAddressForm();
            }
        });
        contentView.addView(addressView);
    }

    protected void showCreateAddressForm() {
        showAddressForm(null);
    }

    protected void showAddressForm(Address address) {
        if (getActivity() == null) return;
        Intent memberAddressFormIntent = new Intent(getActivity(), MemberAddressFormActivity.class);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, Constants.ADDRESS_CREATED_MODIFIED);
    }

    protected void OnAddressSelected(Address address) {
        if (!fromAccountPage) {
            launchSlotSelection(address.getId());
        } else {
            showAddressForm(address);
        }
    }

    private void launchSlotSelection(String addressId) {
        Address.setAddressIdInPreferences(getActivity(), addressId);
        changeFragment(new SlotSelectionFragment());
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Choose Delivery Address";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return MemberAddressListFragment.class.getName();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (addressArrayList != null) {
            outState.putParcelableArrayList(Constants.ADDRESSES, addressArrayList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setFragmentSuspended(false);
        if (resultCode == Constants.ADDRESS_CREATED_MODIFIED) {
            if (data != null) {
                String addressId = data.getStringExtra(Constants.MEMBER_ADDRESS_ID);
                if (!TextUtils.isEmpty(addressId) && !fromAccountPage) {
                    launchSlotSelection(addressId);
                } else {
                    loadAddresses();
                }
            } else {
                loadAddresses();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}