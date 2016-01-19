package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.apiservice.models.response.GetDeliveryAddressApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.interfaces.BasketDeltaUserActionListener;
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.interfaces.OnBasketDeltaListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.CreatePotentialOrderTask;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.task.uiv3.PostGiftTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BasketDeltaDialog;
import com.bigbasket.mobileapp.view.uiv3.OrderQcDialog;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

public class ViewDeliveryAddressFragment extends BaseFragment implements AddressSelectionAware,
        CreatePotentialOrderAware, BasketDeltaUserActionListener, OnAddressChangeListener, OnBasketDeltaListener {

    ProgressBar mLoadingBar;
    private Address mSelectedAddress;
    private ViewGroup layoutCheckoutFooter;
    private Typeface faceRobotoLight;
    private boolean hasGifts = false;
    private RelativeLayout layoutAddressDetails;
    private TextView txtAddressLabel;
    private TextView txtPartialAddress;
    private TextView txtDeliveryAddress;
    private TextView txtName;
    private TextView txtPh;
    private TextView txtChangeAddress;
    private ImageView imageViewEditLoc;
    private TextView txtExpressDelivery;
    private LinearLayout layoutCheckoutProgressContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        faceRobotoLight = FontHolder.getInstance(getActivity()).getFaceRobotoLight();
        View contentView = inflater.inflate(R.layout.uiv3_delivery_address_layout, container, false);
        mLoadingBar = (ProgressBar) contentView.findViewById(R.id.loading_bar);
        layoutCheckoutProgressContainer =
                (LinearLayout) contentView.findViewById(R.id.layoutCheckoutProgressContainer);
        layoutCheckoutFooter = (ViewGroup) contentView.findViewById(R.id.layoutCheckoutFooter);
        layoutAddressDetails = (RelativeLayout) contentView.findViewById(R.id.layoutAddressDetails);
        txtAddressLabel = (TextView) contentView.findViewById(R.id.txtAddressLbl);
        txtPartialAddress = (TextView) contentView.findViewById(R.id.txtPartialAddress);
        txtDeliveryAddress = (TextView) contentView.findViewById(R.id.txtAddress);
        txtName = (TextView) contentView.findViewById(R.id.txtName);
        txtPh = (TextView) contentView.findViewById(R.id.txtPh);
        txtChangeAddress = (TextView) contentView.findViewById(R.id.txtChangeAddress);
        txtExpressDelivery = (TextView) contentView.findViewById(R.id.txtExpressDelivery);
        contentView.findViewById(R.id.imgLocation).setVisibility(View.INVISIBLE);
        txtExpressDelivery.setVisibility(View.GONE);
        txtPartialAddress.setVisibility(View.GONE);
        txtPh.setVisibility(View.GONE);
        txtName.setVisibility(View.GONE);

        txtDeliveryAddress.setTypeface(faceRobotoMedium);
        txtName.setTypeface(faceRobotoMedium);

        imageViewEditLoc = (ImageView) contentView.findViewById(R.id.imgEditIcon);
        txtChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedAddress != null) {
                    launchChangeAddress();
                } else {
                    showAddressForm(null);
                }
            }
        });
        imageViewEditLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedAddress != null) {
                    showAddressForm(mSelectedAddress);
                }
            }
        });
        txtPartialAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedAddress != null) {
                    showAddressForm(mSelectedAddress);
                }
            }
        });
        layoutCheckoutFooter.setOnClickListener(new AddressListFooterButtonOnClickListener());

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCurrentScreenName(TrackEventkeys.CO_ADDRESS);
        hasGifts = getActivity().getIntent().getBooleanExtra(Constants.HAS_GIFTS, false);
        getDeliveryAddress();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED) {
            boolean changed = false;
            if (data != null) {
                Address address = data.getParcelableExtra(Constants.UPDATE_ADDRESS);
                if (address != null) {
                    changed = mSelectedAddress == null
                            || mSelectedAddress.getId() == null || address.getId() == null
                            || !mSelectedAddress.getId().equals(address.getId())
                            || mSelectedAddress.getPincode() == null || address.getPincode() == null
                            || !mSelectedAddress.getPincode().equals(address.getPincode());
                    this.mSelectedAddress = address;
                    if (layoutCheckoutFooter != null) {
                        layoutCheckoutFooter.setVisibility(View.VISIBLE);
                    }
                    showDeliveryAddress(address);
                }
            }
            if (changed) {
                // Forcefully calling get-app-data-dynamic, as user might have change location
                AppDataDynamic.reset(getActivity());
            }
        } else if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            postDeliveryAddress();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        new ChangeAddressTask<>(this, mSelectedAddress.getId(), null, null, null, true).startTask();
    }

    private void renderCheckOutProgressView(boolean hasGifts) {
        layoutCheckoutProgressContainer.removeAllViews();
        layoutCheckoutProgressContainer.setVisibility(View.VISIBLE);
        View checkoutProgressView;
        if (hasGifts) {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.gift), getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{};
            int selectedPos = 0;
            checkoutProgressView = UIUtil.getCheckoutProgressView(getActivity(), null, array_txtValues, array_compPos, selectedPos);
        } else {
            String[] array_txtValues = new String[]{getString(R.string.address),
                    getString(R.string.slots), getString(R.string.order)};
            Integer[] array_compPos = new Integer[]{};
            int selectedPos = 0;
            checkoutProgressView = UIUtil.getCheckoutProgressView(getActivity(), null, array_txtValues, array_compPos, selectedPos);
        }
        if (checkoutProgressView != null) {
            layoutCheckoutProgressContainer.addView(checkoutProgressView, 0);
        }
    }

    private void getDeliveryAddress() {
        if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
            showAlertDialog(null,
                    getString(R.string.notSignedIn),
                    DialogButton.OK, DialogButton.NONE, NavigationCodes.GO_TO_LOGIN, null, null);
            return;
        }
        mSelectedAddress = null;
        AppDataDynamic appDataDynamic = AppDataDynamic.getInstance(getActivity());
        mSelectedAddress = appDataDynamic.getUserDeliveryAddress();
        if (mSelectedAddress != null) {
            showDeliveryAddress(mSelectedAddress);
        } else {
            // App data dynamic failed or there is no address returned
            // Make a network call to get delivery Address
            fetchDeliveryAddress();
        }
    }

    private void fetchDeliveryAddress() {
        mLoadingBar.setVisibility(View.VISIBLE);
        layoutAddressDetails.setVisibility(View.GONE);
        txtChangeAddress.setVisibility(View.GONE);
        layoutCheckoutFooter.setVisibility(View.GONE);
        renderCheckOutProgressView(hasGifts);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        Call<ApiResponse<GetDeliveryAddressApiResponseContent>> call = bigBasketApiService.getDeliveryAddresses(getPreviousScreenName());
        call.enqueue(new BBNetworkCallback<ApiResponse<GetDeliveryAddressApiResponseContent>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetDeliveryAddressApiResponseContent> deliveryAddrResp) {
                mSelectedAddress = null;
                switch (deliveryAddrResp.status) {
                    case 0:
                        ArrayList<Address> addrList = deliveryAddrResp.apiResponseContent.addresses;
                        if (addrList != null && !addrList.isEmpty()) {
                            for (Address addr : addrList) {
                                if (addr.isSelected()) {
                                    mSelectedAddress = addr;
                                    showDeliveryAddress(addr);
                                    break; // break the for-loop
                                }
                            }
                        }
                        if (mSelectedAddress != null) {
                            break; //break from switch case
                        }
                        //Continue and show empty address
                    case ApiErrorCodes.EMPTY_ADDRESS:
                        showDeliveryAddress(null);
                        break;
                    default:
                        handler.sendEmptyMessage(deliveryAddrResp.status,
                                deliveryAddrResp.message, true);
                        break;
                }
            }
        });
    }


    private void showDeliveryAddress(final Address selectedAddress) {
        if (getActivity() == null) return;

        mLoadingBar.setVisibility(View.GONE);
        layoutAddressDetails.setVisibility(View.VISIBLE);
        txtChangeAddress.setVisibility(View.VISIBLE);
        renderCheckOutProgressView(hasGifts);

        if (selectedAddress != null) {
            layoutAddressDetails.setVisibility(View.VISIBLE);
            txtAddressLabel.setVisibility(View.VISIBLE);
            txtDeliveryAddress.setVisibility(View.VISIBLE);
            txtChangeAddress.setText(R.string.changeAllCaps);

            imageViewEditLoc.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(selectedAddress.getContactNum())) {
                txtPh.setVisibility(View.VISIBLE);
                txtPh.setTypeface(faceRobotoLight);
                txtPh.setText(selectedAddress.getContactNum());
            } else {
                txtPh.setVisibility(View.GONE);
            }
            if (!UIUtil.isEmpty(selectedAddress.getAddressNickName())) {
                txtName.setVisibility(View.VISIBLE);
                txtName.setText(selectedAddress.getAddressNickName());
            } else if (!UIUtil.isEmpty(selectedAddress.getAltAddressNickName())) {
                txtName.setText(selectedAddress.getAltAddressNickName());
            } else {
                txtName.setVisibility(View.GONE);
            }

            if (selectedAddress.isPartial()) {
                imageViewEditLoc.setVisibility(View.VISIBLE);
                txtPartialAddress.setVisibility(View.VISIBLE);
            } else {
                imageViewEditLoc.setVisibility(View.GONE);
                txtPartialAddress.setVisibility(View.GONE);
            }

            if (selectedAddress.isExpress()) {
                txtExpressDelivery.setVisibility(View.VISIBLE);
            } else {
                txtExpressDelivery.setVisibility(View.GONE);
            }

            txtDeliveryAddress.setText(selectedAddress.toString().trim());

            trackEvent(TrackingAware.CHECKOUT_ADDRESS_SHOWN, null);
            trackEventsOnFabric(TrackingAware.CHECKOUT_ADDRESS_SHOWN, null);
            String total = getArguments() != null ? getArguments().getString(Constants.TOTAL_BASKET_VALUE) : null;
            UIUtil.setUpFooterButton(getCurrentActivity(), layoutCheckoutFooter, total,

                    getString(R.string.continueCaps), true);
            layoutCheckoutFooter.setVisibility(View.VISIBLE);
        } else {
            layoutCheckoutFooter.setVisibility(View.GONE);
            txtName.setVisibility(View.GONE);
            imageViewEditLoc.setVisibility(View.GONE);
            imageViewEditLoc.setVisibility(View.GONE);
            txtPartialAddress.setVisibility(View.GONE);
            txtExpressDelivery.setVisibility(View.GONE);

            txtDeliveryAddress.setVisibility(View.VISIBLE);
            layoutAddressDetails.setVisibility(View.VISIBLE);

            txtDeliveryAddress.setText(R.string.no_delivery_address);

            txtChangeAddress.setText(R.string.add);

        }
    }

    private void launchChangeAddress() {
        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.CHANGE_ADDRESS_FRAGMENT);
        intent.putExtra(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.CHECKOUT);
        startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, map);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ViewDeliveryAddressFragment.class.getSimpleName();
    }

    @Override
    public void onAddressSelected(Address address) {

    }

    @Nullable
    @Override
    public Address getSelectedAddress() {
        return this.mSelectedAddress;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.delivery_address);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.deliveryAddressContainer) : null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_DELIVERY_ADDRESS_SCREEN;
    }

    @Override
    public void onUpdateBasket(String addressId, String lat, String lng, @Nullable String area) {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        new ChangeAddressTask<>(this, addressId, lat, lng, area, false).startTask();
    }

    @Override
    public void onNoBasketUpdate() {
        //Reverting the user address to the previous address incase of qc dialog dismiss
        AppDataDynamic appDataDynamic = AppDataDynamic.getInstance(getActivity());
        mSelectedAddress = appDataDynamic.getUserDeliveryAddress();
        showDeliveryAddress(mSelectedAddress);
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
        setCurrentScreenName(TrackEventkeys.CO_QC);
        launchSlotSelection(createPotentialOrderResponseContent);
    }

    private void launchSlotSelection(CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        if (createPotentialOrderResponseContent.gift != null
                && createPotentialOrderResponseContent.gift.getCount() > 0) {
            Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
            Bundle bundle = new Bundle();
            Gift gift = createPotentialOrderResponseContent.gift;
            // Reserve all items as gift for now
            if (gift != null && gift.getCount() > 0 && gift.getGiftItems() != null) {
                if (!hasGifts) {
                    renderCheckOutProgressView(true);
                    hasGifts = true;
                }
                for (GiftItem giftItem : gift.getGiftItems()) {
                    if (giftItem.isReadOnly() || (giftItem.getReservedQty() <= 0 && giftItem.getQuantity() > 0)) {
                        giftItem.setReservedQty(giftItem.getQuantity());
                    }
                }
            }
            bundle.putParcelable(Constants.GIFTS, gift);
            bundle.putString(Constants.P_ORDER_ID, createPotentialOrderResponseContent.potentialOrderId);
            bundle.putInt(Constants.FRAGMENT_CODE, FragmentCodes.START_GIFTFRAGMENT);
            intent.putExtras(bundle);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            new PostGiftTask<>(getCurrentActivity(), createPotentialOrderResponseContent.potentialOrderId, null,
                    TrackEventkeys.CO_GIFT_OPS).startTask();
        }
    }

    @Override
    public void onAllProductsHavingQcError() {
        finish();
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

    @Override
    public void onBasketDelta(String addressId, String lat, String lng, String title, String msg, @Nullable String area, boolean hasQcError, ArrayList<QCErrorData> qcErrorDatas) {
        new BasketDeltaDialog<>().show(this, title, msg, hasQcError, qcErrorDatas, addressId,
                getString(R.string.reviewBasket), lat, lng, area);
        markBasketChanged(null);
    }

    @Override
    public void onNoBasketDelta(String addressId, String lat, String lng, @Nullable String area) {
        new CreatePotentialOrderTask<>(this, addressId).startTask();
    }

    private void showAddressForm(Address address) {
        if (getActivity() == null) return;
        Intent memberAddressFormIntent = new Intent(getActivity(), MemberAddressFormActivity.class);
        memberAddressFormIntent.putExtra(Constants.UPDATE_ADDRESS, address);
        startActivityForResult(memberAddressFormIntent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "ViewDeliveryAddressFragment";
    }

    private class AddressListFooterButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
            trackEvent(TrackingAware.CHECKOUT_ADDRESS_CLICKED_CONTI, map, null, null, false, true);
            if (mSelectedAddress == null) {
                mSelectedAddress = getSelectedAddress();
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
}
