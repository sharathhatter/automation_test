package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.ShipmentSelectionActivity;
import com.bigbasket.mobileapp.adapter.account.MemberAddressListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.interfaces.BasketDeltaUserActionListener;
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.service.GetAppDataDynamicIntentService;
import com.bigbasket.mobileapp.task.CreatePotentialOrderTask;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BasketDeltaDialog;
import com.bigbasket.mobileapp.view.uiv3.OrderQcDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressListFragment extends BaseFragment implements AddressSelectionAware,
        CreatePotentialOrderAware, BasketDeltaUserActionListener, OnAddressChangeListener {

    protected ArrayList<Address> mAddressArrayList;
    private MemberAddressListAdapter memberAddressListAdapter;
    private int mAddressPageMode;
    private Address mSelectedAddress;
    private ViewGroup layoutCheckoutFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
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

    private void loadAddresses() {
        if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
            ((BaseActivity) getActivity()).showAlertDialog("BigBasket",
                    "You are not signed in.\nPlease sign-in to continue", NavigationCodes.GO_TO_LOGIN);
            return;
        }
        mSelectedAddress = null; // Reset
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.getDeliveryAddresses(new Callback<ApiResponse<GetDeliveryAddressApiResponseContent>>() {
            @Override
            public void success(ApiResponse<GetDeliveryAddressApiResponseContent> getDeliveryAddressApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
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
        ArrayList<Object> addressObjectList = new ArrayList<>();
        boolean hideCheckOutBtn = false;
        if (mAddressArrayList != null && mAddressArrayList.size() > 0) {
            addressRecyclerView.setVisibility(View.VISIBLE);
            noDeliveryAddLayout.setVisibility(View.GONE);
            addressObjectList.add(getString(R.string.delivery_add));
            addressObjectList.addAll(mAddressArrayList);
            addressObjectList.add(2, getString(R.string.other_address));
            addressObjectList.add(3, getString(R.string.addAnAddress));

            memberAddressListAdapter =
                    new MemberAddressListAdapter<>(this, addressObjectList,
                            mAddressPageMode != MemberAddressPageMode.CHECKOUT);
            addressRecyclerView.setAdapter(memberAddressListAdapter);
        } else {
            hideCheckOutBtn = true;
            emptyAddressView(noDeliveryAddLayout, addressRecyclerView, addressView);
        }

        layoutCheckoutFooter = (ViewGroup) addressView.findViewById(R.id.layoutCheckoutFooter);
        String total = getArguments() != null ? getArguments().getString(Constants.TOTAL_BASKET_VALUE) : null;
        UIUtil.setUpFooterButton(getCurrentActivity(), layoutCheckoutFooter, total,
                getString(R.string.continueCaps), true);
        if (mAddressPageMode == MemberAddressPageMode.CHECKOUT && !hideCheckOutBtn) {
            layoutCheckoutFooter.setOnClickListener(new AddressListFooterButtonOnClickListener());
            layoutCheckoutFooter.setVisibility(View.VISIBLE);
        } else {
            layoutCheckoutFooter.setVisibility(View.GONE);
        }

        contentView.addView(addressView);
    }

    private class AddressListFooterButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
            trackEvent(TrackingAware.CHECKOUT_ADDRESS_CLICKED_CONTI, map, null, null, false, true);
            if (mSelectedAddress == null) {
                mSelectedAddress = memberAddressListAdapter.getSelectedAddress();
            }
            if (mSelectedAddress != null) {
                if (mSelectedAddress.isPartial()) {
                    showAddressForm(mSelectedAddress);
                } else {
                    postDeliveryAddress();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.pleaseChooseAddress),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAddNewAddressClicked() {
        showCreateAddressForm();
    }

    protected void showCreateAddressForm() {
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

    protected void showAddressForm(Address address) {
        if (getActivity() == null) return;
        Intent memberAddressFormIntent = new Intent(getActivity(), MemberAddressFormActivity.class);
        memberAddressFormIntent.putExtra(Constants.ADDRESS_PAGE_MODE, mAddressPageMode);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
    }

    @Override
    public void onAddressSelected(Address address) {
        this.mSelectedAddress = address;
        memberAddressListAdapter.notifyDataSetChanged();
        if (mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
            trackEvent(TrackingAware.CHECKOUT_ADDRESS_SELECTED, null, null, null, false, true);
        } else if (mAddressPageMode == MemberAddressPageMode.ADDRESS_SELECT) {
            Intent intent = new Intent();
            intent.putExtra(Constants.ADDRESS_ID, mSelectedAddress.getId());
            getActivity().setResult(NavigationCodes.ADDRESS_CREATED_MODIFIED, intent);
            finish();
        }
    }

    @Override
    public Address getSelectedAddress() {
        return this.mSelectedAddress;
    }

    private void postDeliveryAddress() {
        if (mSelectedAddress == null) {
            Toast.makeText(getActivity(), getString(R.string.pleaseChooseAddress),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // First check the basket for changes, as product's
        // availability may change when address changes
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        new ChangeAddressTask<>(this, mSelectedAddress.getId(), null, null, true).startTask();
    }

    @Override
    public void onBasketDelta(String addressId, String lat, String lng,
                              String title, String msg, boolean hasQcError,
                              ArrayList<QCErrorData> qcErrorDatas) {
        new BasketDeltaDialog<>().show(this, title, msg, hasQcError, qcErrorDatas, addressId,
                getString(R.string.reviewBasket), lat, lng);
    }

    @Override
    public void onAddressChanged(ArrayList<AddressSummary> addressSummaries) {
        if (getCurrentActivity() == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.FRAGMENT_CODE, String.valueOf(NavigationCodes.GO_TO_BASKET));
        editor.apply();
        ((BBActivity) getActivity()).onAddressChanged(addressSummaries);
    }

    @Override
    public void onAddressNotSupported(String msg) {
        if (getCurrentActivity() == null) return;
        ((BBActivity) getCurrentActivity()).onAddressNotSupported(msg);
    }

    @Override
    public void onNoBasketDelta(String addressId, String lat, String lng) {
        new CreatePotentialOrderTask<>(this, addressId).startTask();
    }

    @Override
    public void onUpdateBasket(String addressId, String lat, String lng) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        new ChangeAddressTask<>(this, addressId, lat, lng, false).startTask();
    }

    @Override
    public void onNoBasketUpdate() {

    }

    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        Bundle args = getArguments();
        mAddressPageMode = args != null ? args.getInt(Constants.ADDRESS_PAGE_MODE,
                MemberAddressPageMode.CHECKOUT) : MemberAddressPageMode.CHECKOUT;
        switch (mAddressPageMode) {
            case MemberAddressPageMode.ADDRESS_SELECT:
                return "Choose address";
            case MemberAddressPageMode.ACCOUNT:
                return "Delivery address";
            default:
                return "Pick an address";
        }
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return MemberAddressListFragment.class.getName();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAddressArrayList != null) {
            outState.putParcelableArrayList(Constants.ADDRESSES, mAddressArrayList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED) {
            if (data != null) {
                Address address = data.getParcelableExtra(Constants.UPDATE_ADDRESS);
                if (address != null && mAddressPageMode == MemberAddressPageMode.CHECKOUT) {
                    this.mSelectedAddress = address;
                    if (layoutCheckoutFooter != null) {
                        layoutCheckoutFooter.setVisibility(View.VISIBLE);
                    }
                    loadAddresses();
                } else {
                    getActivity().setResult(NavigationCodes.ACCOUNT_UPDATED);
                    loadAddresses();
                }
            } else {
                getActivity().setResult(NavigationCodes.ACCOUNT_UPDATED);
                loadAddresses();
            }
            // Forcefully calling get-app-data-dynamic, as user might have change location
            AppDataDynamic.reset(getActivity());
        } else if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            postDeliveryAddress();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onPotentialOrderCreated(CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        if (createPotentialOrderResponseContent.hasQcErrors &&
                createPotentialOrderResponseContent.qcErrorDatas != null &&
                createPotentialOrderResponseContent.qcErrorDatas.size() > 0) {
            new OrderQcDialog<>().show(this, createPotentialOrderResponseContent);
            trackEvent(TrackingAware.CHECKOUT_QC_SHOWN, null);
        } else {
            launchSlotSelection(createPotentialOrderResponseContent);
        }
    }

    @Override
    public void postOrderQc(CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        setNextScreenNavigationContext(TrackEventkeys.CO_QC);
        launchSlotSelection(createPotentialOrderResponseContent);
    }

    @Override
    public void onAllProductsHavingQcError() {
        finish();
    }

    private void launchSlotSelection(CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        Intent intent = new Intent(getCurrentActivity(), ShipmentSelectionActivity.class);
        intent.putParcelableArrayListExtra(Constants.SHIPMENTS, createPotentialOrderResponseContent.shipments);
        intent.putExtra(Constants.CITY_MODE, createPotentialOrderResponseContent.cityMode);
        intent.putExtra(Constants.ORDER_DETAILS, createPotentialOrderResponseContent.orderDetails);
        intent.putExtra(Constants.P_ORDER_ID, createPotentialOrderResponseContent.potentialOrderId);
        if (createPotentialOrderResponseContent.defaultShipmentActions != null) {
            intent.putExtra(Constants.DEFAULT_ACTIONS,
                    new Gson().toJson(createPotentialOrderResponseContent.defaultShipmentActions));
        }
        if (createPotentialOrderResponseContent.toggleShipmentActions != null) {
            intent.putExtra(Constants.ON_TOGGLE_ACTIONS,
                    new Gson().toJson(createPotentialOrderResponseContent.toggleShipmentActions));
        }
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_DELIVERY_ADDRESS_SCREEN;
    }
}