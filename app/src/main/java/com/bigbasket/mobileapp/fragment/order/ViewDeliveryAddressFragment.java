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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.order.MemberAddressFormActivity;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
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

public class ViewDeliveryAddressFragment extends BaseFragment implements AddressSelectionAware,
        CreatePotentialOrderAware, BasketDeltaUserActionListener, OnAddressChangeListener, OnBasketDeltaListener {

    private Address mSelectedAddress;
    private ViewGroup layoutCheckoutFooter;
    private Typeface faceRobotoLight;
    private RelativeLayout noDeliveryAddLayout;
    private boolean hasGifts = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_delivery_address_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        faceRobotoLight = FontHolder.getInstance(getActivity()).getFaceRobotoLight();
        setNextScreenNavigationContext(TrackEventkeys.CO_ADDRESS);
        hasGifts = getActivity().getIntent().getBooleanExtra(Constants.HAS_GIFTS, false);
        getDeliveryAddress();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED) {
            if (data != null) {
                Address address = data.getParcelableExtra(Constants.UPDATE_ADDRESS);
                if (address != null) {
                    this.mSelectedAddress = address;
                    if (layoutCheckoutFooter != null) {
                        layoutCheckoutFooter.setVisibility(View.VISIBLE);
                    }
                    if (noDeliveryAddLayout != null) {
                        noDeliveryAddLayout.setVisibility(View.GONE);
                    }
                    showDeliveryAddress(address);
                }
            }
            // Forcefully calling get-app-data-dynamic, as user might have change location
            AppDataDynamic.reset(getActivity());
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

    private void renderCheckOutProgressView(View contentView, boolean hasGifts) {
        LinearLayout layoutCheckoutProgress = (LinearLayout) contentView.findViewById(R.id.layoutCheckoutProgressContainer);
        layoutCheckoutProgress.removeAllViews();
        layoutCheckoutProgress.setVisibility(View.VISIBLE);
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
        if (checkoutProgressView != null) layoutCheckoutProgress.addView(checkoutProgressView, 0);
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
            HashMap<String, String> cityMap = UIUtil.getUserCityDetails(getActivity());
            if (cityMap != null) {
                String cityName = cityMap.get(Constants.CITY);
                String cityId = cityMap.get(Constants.CITY_ID);
                if (!TextUtils.isEmpty(cityName)) {
                    mSelectedAddress = new Address(false, "", "", "", "", "", "", "", false, false, false);
                    mSelectedAddress.setIsPartial(true);
                    mSelectedAddress.setCityName(cityName.trim());
                    if (!TextUtils.isEmpty(cityId)) {
                        mSelectedAddress.setCityId(Integer.parseInt(cityId));
                    }
                    showDeliveryAddress(mSelectedAddress);
                }
            }
        }
    }

    private void emptyAddressView(RelativeLayout noDeliveryAddLayout, LinearLayout layoutAddressDetails, TextView txtAddressLabel, TextView txtDeliveryAddress, TextView txtChangeAddress, TextView txtInComplete, ImageView imageViewEditLoc, View contentView) {

        noDeliveryAddLayout.setVisibility(View.VISIBLE);
        layoutAddressDetails.setVisibility(View.GONE);
        txtAddressLabel.setVisibility(View.GONE);
        txtDeliveryAddress.setVisibility(View.GONE);
        txtChangeAddress.setVisibility(View.GONE);
        txtInComplete.setVisibility(View.GONE);
        imageViewEditLoc.setVisibility(View.GONE);

        ImageView imgEmptyPage = (ImageView) contentView.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_delivery_address);
        TextView txtEmptyMsg1 = (TextView) contentView.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noAddressMsg1);
        TextView txtEmptyMsg2 = (TextView) contentView.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setText(R.string.noAddressMsg2);
        Button btnBlankPage = (Button) contentView.findViewById(R.id.btnBlankPage);
        btnBlankPage.setText(getString(R.string.adAddresCaps));
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchChangeAddress();
            }
        });
    }

    private void showDeliveryAddress(final Address selectedAddress) {
        if (getActivity() == null) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        noDeliveryAddLayout = (RelativeLayout) contentView.findViewById(R.id.noDeliveryAddLayout);
        layoutCheckoutFooter = (ViewGroup) contentView.findViewById(R.id.layoutCheckoutFooter);
        LinearLayout layoutAddressDetails = (LinearLayout) contentView.findViewById(R.id.layoutAddressDetails);
        TextView txtAddressLabel = (TextView) contentView.findViewById(R.id.txtAddressLbl);
        TextView txtInComplete = (TextView) contentView.findViewById(R.id.partial_address_textView);
        TextView txtDeliveryAddress = (TextView) contentView.findViewById(R.id.txtDeliveryAddress);
        TextView txtName = (TextView) contentView.findViewById(R.id.txtName);
        TextView txtPh = (TextView) contentView.findViewById(R.id.txtPh);
        TextView txtChangeAddress = (TextView) contentView.findViewById(R.id.txtChangeAddress);

        ImageView imageViewEditLoc = (ImageView) contentView.findViewById(R.id.imgEditLoc);

        txtChangeAddress.setVisibility(View.VISIBLE);
        txtDeliveryAddress.setTypeface(faceRobotoMedium);

        if (selectedAddress != null) {

            renderCheckOutProgressView(contentView, hasGifts);

            noDeliveryAddLayout.setVisibility(View.GONE);
            layoutCheckoutFooter.setVisibility(View.VISIBLE);
            layoutAddressDetails.setVisibility(View.VISIBLE);
            txtAddressLabel.setVisibility(View.VISIBLE);

            txtDeliveryAddress.setVisibility(View.VISIBLE);

            imageViewEditLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddressForm(selectedAddress);
                }
            });
            imageViewEditLoc.setVisibility(View.VISIBLE);

            if (selectedAddress.isPartial()) {
                txtInComplete.setVisibility(View.VISIBLE);
            } else {
                txtInComplete.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(selectedAddress.getContactNum())) {
                txtPh.setVisibility(View.VISIBLE);
                txtPh.setTypeface(faceRobotoLight);
                txtPh.setText(selectedAddress.getContactNum());
            }
            if (!TextUtils.isEmpty(selectedAddress.getName())) {
                if (selectedAddress.getName().trim().length() > 0) {
                    txtName.setVisibility(View.VISIBLE);
                    txtName.setTypeface(faceRobotoMedium);
                    txtName.setText(selectedAddress.getName().trim());
                }
            }

            txtDeliveryAddress.setText(selectedAddress.toString().trim());

            trackEvent(TrackingAware.DELIVERY_ADDRESS_SHOWN, null);
            String total = getArguments() != null ? getArguments().getString(Constants.TOTAL_BASKET_VALUE) : null;
            UIUtil.setUpFooterButton(getCurrentActivity(), layoutCheckoutFooter, total,
                    getString(R.string.continueCaps), true);
            layoutCheckoutFooter.setOnClickListener(new AddressListFooterButtonOnClickListener());
            txtChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchChangeAddress();
                }
            });
        } else {
            LinearLayout layoutCheckoutProgress = (LinearLayout) contentView.findViewById(R.id.layoutCheckoutProgressContainer);
            layoutCheckoutProgress.removeAllViews();
            layoutCheckoutProgress.setVisibility(View.GONE);
            layoutCheckoutFooter.setVisibility(View.GONE);
            emptyAddressView(noDeliveryAddLayout, layoutAddressDetails, txtAddressLabel, txtDeliveryAddress, txtChangeAddress, txtInComplete, imageViewEditLoc, contentView);
        }
    }

    private void launchChangeAddress() {
        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.CHANGE_ADDRESS_FRAGMENT);
        intent.putExtra(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.CHECKOUT);
        startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
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
        setNextScreenNavigationContext(TrackEventkeys.CO_QC);
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
                ViewGroup contentView = getContentView();
                if (contentView == null) return;
                if (!hasGifts) {
                    renderCheckOutProgressView(contentView, true);
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

    private class AddressListFooterButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
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

    @NonNull
    @Override
    public String getInteractionName() {
        return "ViewDeliveryAddressFragment";
    }
}
