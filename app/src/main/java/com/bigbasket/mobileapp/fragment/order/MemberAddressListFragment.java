package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.CreatePotentialOrderTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.OrderQcDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressListFragment extends BaseFragment implements AddressSelectionAware,
        CreatePotentialOrderAware {

    protected ArrayList<Address> mAddressArrayList;
    private MemberAddressListAdapter memberAddressListAdapter;
    private boolean mFromAccountPage;
    private String addressId;
    private ViewGroup layoutCheckoutFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        mFromAccountPage = args != null && args.getBoolean(Constants.FROM_ACCOUNT_PAGE, false);
        if(mFromAccountPage){
            setNextScreenNavigationContext(TrackEventkeys.NC_ACCOUNT_ADDRESS);
        }else {
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
                    new MemberAddressListAdapter<>(this, addressObjectList, mFromAccountPage);
            addressRecyclerView.setAdapter(memberAddressListAdapter);
        } else {
            hideCheckOutBtn = true;
            emptyAddressView(noDeliveryAddLayout, addressRecyclerView, addressView);
        }

        layoutCheckoutFooter = (ViewGroup) addressView.findViewById(R.id.layoutCheckoutFooter);
        String total = getArguments() != null ? getArguments().getString(Constants.TOTAL_BASKET_VALUE) : null;
        UIUtil.setUpFooterButton(getCurrentActivity(), layoutCheckoutFooter, total,
                getString(R.string.continueCaps), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressId != null) {
                    createPotentialOrder(addressId);
                } else if (memberAddressListAdapter.getSelectedAddress() != null) {
                    addressId = memberAddressListAdapter.getSelectedAddress().getId();
                    createPotentialOrder(addressId);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.pleaseChooseAddress),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!mFromAccountPage && !hideCheckOutBtn) {
            layoutCheckoutFooter.setVisibility(View.VISIBLE);
        } else {
            layoutCheckoutFooter.setVisibility(View.GONE);
        }

        contentView.addView(addressView);
    }

    @Override
    public void onAddNewAddressClicked() {
        showCreateAddressForm();
    }

    protected void showCreateAddressForm() {
        showAddressForm(null);
        HashMap<String, String> map = new HashMap<>();
        if (mFromAccountPage) {
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
        memberAddressFormIntent.putExtra(Constants.FROM_ACCOUNT_PAGE, mFromAccountPage);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
    }

    @Override
    public void onAddressSelected(Address address) {
        this.addressId = address.getId();
        memberAddressListAdapter.notifyDataSetChanged();
    }

    @Override
    public String getSelectedAddressId() {
        return this.addressId;
    }

    private void createPotentialOrder(String addressId) {
        new CreatePotentialOrderTask<>(this, addressId).startTask();
    }

    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        Bundle args = getArguments();
        mFromAccountPage = args != null && args.getBoolean(Constants.FROM_ACCOUNT_PAGE, false);
        return mFromAccountPage ? "Delivery Address" : "Pick an address";
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
                String addressId = data.getStringExtra(Constants.MEMBER_ADDRESS_ID);
                if (!TextUtils.isEmpty(addressId) && !mFromAccountPage) {
                    this.addressId = addressId;
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
        } else if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            createPotentialOrder(addressId);
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