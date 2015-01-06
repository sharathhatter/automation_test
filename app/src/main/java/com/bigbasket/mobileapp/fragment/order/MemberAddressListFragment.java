package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.LocateOnMapActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.SlotPaymentSelectionActivity;
import com.bigbasket.mobileapp.adapter.account.MemberAddressListAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MemberAddressListFragment extends BaseFragment implements AddressSelectionAware {

    protected ArrayList<Address> mAddressArrayList;
    private boolean mFromAccountPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        mFromAccountPage = args != null && args.getBoolean(Constants.FROM_ACCOUNT_PAGE, false);
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
                    hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                switch (getDeliveryAddressApiResponse.status) {
                    case 0:
                        mAddressArrayList = getDeliveryAddressApiResponse.apiResponseContent.addresses;
                        showAddresses();
                        break;
                    case ApiErrorCodes.EMPTY_ADDRESS:
                        showCreateAddressForm();
                        break;
                    default:
                        handler.sendEmptyMessage(getDeliveryAddressApiResponse.status);
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
            }
        });
    }

    private void showAddresses() {
        if (mAddressArrayList != null && mAddressArrayList.size() > 0) {
            if (mFromAccountPage) {
                trackEvent(TrackingAware.MY_ACCOUNT_DELIVERY_ADDRESS_SHOWN, null);
            } else {
                trackEvent(TrackingAware.CHECKOUT_ADDRESS_SHOWN, null);
            }
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
        View addressView = layoutInflater.inflate(R.layout.uiv3_fab_recycler_view, null);

        RecyclerView addressRecyclerView = (RecyclerView) addressView.findViewById(R.id.fabRecyclerView);
        UIUtil.configureRecyclerView(addressRecyclerView, getActivity(), 1, 3);
        MemberAddressListAdapter memberAddressListAdapter =
                new MemberAddressListAdapter<>(this, mAddressArrayList, faceRobotoRegular,
                        mFromAccountPage);
        addressRecyclerView.setAdapter(memberAddressListAdapter);

        FloatingActionButton floatingActionButton = (FloatingActionButton) addressView.findViewById(R.id.btnFab);
        if (addressRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            floatingActionButton.attachToRecyclerView(addressRecyclerView);
        }
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
        memberAddressFormIntent.putExtra(Constants.FROM_ACCOUNT_PAGE, mFromAccountPage);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
    }

    @Override
    public void onAddressSelected(Address address) {
        if (!mFromAccountPage) {
            trackEvent(TrackingAware.CHECKOUT_ADDRESS_CHOSEN, null);
            launchSlotSelection(address.getId());
        } else {
            showAddressForm(address);
        }
    }

    @Override
    public void onLocateOnMapClicked(Address address) {
        // Defensive check
        if (mFromAccountPage) {
            Intent intent = new Intent(getActivity(), LocateOnMapActivity.class);
            intent.putExtra(Constants.UPDATE_ADDRESS, address);
            startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
        }
    }

    private void launchSlotSelection(String addressId) {
        Intent intent = new Intent(getCurrentActivity(), SlotPaymentSelectionActivity.class);
        intent.putExtra(Constants.MEMBER_ADDRESS_ID, addressId);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        Bundle args = getArguments();
        mFromAccountPage = args != null && args.getBoolean(Constants.FROM_ACCOUNT_PAGE, false);
        return mFromAccountPage ? "Delivery Address" : "Choose Delivery Address";
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