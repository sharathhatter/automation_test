package com.bigbasket.mobileapp.fragment.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.adapter.account.AddressListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AddressChangeAware;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

public class ChangeAddressFragment extends BaseFragment implements AddressSelectionAware, OnAddressChangeListener, AddressChangeAware {

    private ArrayList<Address> mAddressArrayList;
    private AddressListAdapter<ChangeAddressFragment> addressListAdapter;
    private int mAddressPageMode;
    private Address mSelectedAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        mAddressPageMode = args != null ?
                args.getInt(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.CHECKOUT) : MemberAddressPageMode.CHECKOUT;
        if (mAddressPageMode == MemberAddressPageMode.ACCOUNT) {
            setNextScreenNavigationContext(TrackEventkeys.NC_ACCOUNT_ADDRESS);
        } else {
            setNextScreenNavigationContext(TrackEventkeys.CO_ADDRESS);
        }
        if (savedInstanceState != null) {
            mAddressArrayList = savedInstanceState.getParcelableArrayList(Constants.ADDRESSES);
            if (mAddressArrayList != null) {
                showAddresses();
                return;
            }
        }
        loadAddresses();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED) {
            if (data != null) {
                Address address = data.getParcelableExtra(Constants.UPDATE_ADDRESS);
                if (address != null && mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
                    loadAddresses();
                } else {
                    getActivity().setResult(NavigationCodes.ACCOUNT_UPDATED);
                    loadAddresses();
                }
            } else {
                if (mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
                    getActivity().setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED);
                } else {
                    getActivity().setResult(NavigationCodes.ACCOUNT_UPDATED);
                    loadAddresses();
                }
            }
            // Forcefully calling get-app-data-dynamic, as user might have change location
            AppDataDynamic.reset(getActivity());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadAddresses() {
        if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
            ((BaseActivity) getActivity()).showAlertDialog("BigBasket",
                    getString(R.string.notSignedIn), NavigationCodes.GO_TO_LOGIN);
            return;
        }
        mSelectedAddress = null; // Reset
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        Call<ApiResponse<GetDeliveryAddressApiResponseContent>> call = bigBasketApiService.getDeliveryAddresses();
        call.enqueue(new BBNetworkCallback<ApiResponse<GetDeliveryAddressApiResponseContent>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetDeliveryAddressApiResponseContent> getDeliveryAddressApiResponse) {
                switch (getDeliveryAddressApiResponse.status) {
                    case 0:
                        mAddressArrayList = getDeliveryAddressApiResponse.apiResponseContent.addresses;
                        showAddresses();
                        break;
                    case ApiErrorCodes.EMPTY_ADDRESS:
                        handleEmptyAddresses();
                        break;
                    default:
                        handler.sendEmptyMessage(getDeliveryAddressApiResponse.status,
                                getDeliveryAddressApiResponse.message, true);
                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                hideProgressView();
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_new_address, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_address:
                showCreateAddressForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddresses() {
        if (mAddressArrayList != null && mAddressArrayList.size() > 0) {
            trackEvent(TrackingAware.DELIVERY_ADDRESS_SHOWN, null);
            renderAddressList();
        } else {
            handleEmptyAddresses();
        }
    }

    private void handleEmptyAddresses() {
        renderAddressList();
    }

    private void emptyAddressView(RelativeLayout noDeliveryAddLayout, RecyclerView addressRecyclerView, View addressView) {
        noDeliveryAddLayout.setVisibility(View.VISIBLE);
        addressRecyclerView.setVisibility(View.GONE);
        ImageView imgEmptyPage = (ImageView) addressView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_delivery_address);
        TextView txtEmptyMsg1 = (TextView) addressView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noAddressMsg1);
        TextView txtEmptyMsg2 = (TextView) addressView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setText(R.string.noAddressMsg2);
        Button btnBlankPage = (Button) addressView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setText(getString(R.string.adAddresCaps));
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddressForm(null);
            }
        });
    }

    private void renderAddressList() {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addressView = layoutInflater.inflate(R.layout.uiv3_fab_recycler_view, contentView, false);

        RecyclerView addressRecyclerView = (RecyclerView) addressView.findViewById(R.id.fabRecyclerView);
        UIUtil.configureRecyclerView(addressRecyclerView, getActivity(), 1, 3);
        RelativeLayout noDeliveryAddLayout = (RelativeLayout) addressView.findViewById(R.id.noDeliveryAddLayout);
        if (mAddressArrayList != null && mAddressArrayList.size() > 0) {
            addressRecyclerView.setVisibility(View.VISIBLE);
            noDeliveryAddLayout.setVisibility(View.GONE);

            addressListAdapter = new AddressListAdapter<>(this, mAddressArrayList);
            addressRecyclerView.setAdapter(addressListAdapter);
        } else {
            emptyAddressView(noDeliveryAddLayout, addressRecyclerView, addressView);
        }
        contentView.addView(addressView);
    }

    @Override
    public void onAddNewAddressClicked() {
        showCreateAddressForm();
    }

    private void showCreateAddressForm() {
        showAddressForm(null);
        HashMap<String, String> map = new HashMap<>();
        if (mAddressPageMode != MemberAddressPageMode.CHECKOUT) {
            setCurrentNavigationContext(TrackEventkeys.NAVIGATION_CTX_MY_ACCOUNT);
            trackEvent(TrackingAware.NEW_ADDRESS_CLICKED, map);
        } else {
            setCurrentNavigationContext(TrackEventkeys.CO_BASKET);
            trackEvent(TrackingAware.CHECKOUT_CREATE_ADDRESS_SHOWN, map);
        }
    }

    @Override
    public void onEditAddressClicked(Address address) {
        showAddressForm(address);
    }

    private void showAddressForm(Address address) {
        if (getActivity() == null) return;
        Intent memberAddressFormIntent = new Intent(getActivity(), MemberAddressFormActivity.class);
        memberAddressFormIntent.putExtra(Constants.ADDRESS_PAGE_MODE, mAddressPageMode);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
    }

    @Override
    public void onAddressSelected(Address address) {
        this.mSelectedAddress = address;
        addressListAdapter.notifyDataSetChanged();
        if (mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
            Intent intent = new Intent();
            intent.putExtra(Constants.UPDATE_ADDRESS, mSelectedAddress);
            getActivity().setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED, intent);
            finish();
        } else if (mAddressPageMode == MemberAddressPageMode.ADDRESS_SELECT) {
            Intent intent = new Intent();
            intent.putExtra(Constants.ADDRESS_ID, mSelectedAddress.getId());
            getActivity().setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED, intent);
            finish();
        }
    }

    @Nullable
    @Override
    public Address getSelectedAddress() {
        return this.mSelectedAddress;
    }

    @Override
    protected String getTitle() {
        Bundle args = getArguments();
        mAddressPageMode = args != null ? args.getInt(Constants.ADDRESS_PAGE_MODE,
                MemberAddressPageMode.CHECKOUT) : MemberAddressPageMode.CHECKOUT;
        switch (mAddressPageMode) {
            case MemberAddressPageMode.ADDRESS_SELECT:
                return getString(R.string.chooseAddress);
            case MemberAddressPageMode.ACCOUNT:
                return getString(R.string.delivery_address);
            case MemberAddressPageMode.CHECKOUT:
                return getString(R.string.chooseDelAddress);
            default:
                return getString(R.string.delivery_address);
        }
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_DELIVERY_ADDRESS_SCREEN;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ChangeAddressFragment.class.getName();
    }

    @Override
    public void onAddressChanged(ArrayList<AddressSummary> addressSummaries, @Nullable String selectedAddressId) {
        if (getCurrentActivity() == null) return;
        ((BBActivity) getActivity()).onAddressChanged(addressSummaries, selectedAddressId);
        finish();
    }

    @Override
    public void onAddressNotSupported(String msg) {
        if (getCurrentActivity() == null) return;
        ((BBActivity) getCurrentActivity()).onAddressNotSupported(msg);
    }
}
