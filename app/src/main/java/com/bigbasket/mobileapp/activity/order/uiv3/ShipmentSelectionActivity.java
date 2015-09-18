package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.LinkedProductsAdapter;
import com.bigbasket.mobileapp.adapter.shipment.SlotListAdapter;
import com.bigbasket.mobileapp.apiservice.models.request.SelectedShipment;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.shipments.BaseShipmentAction;
import com.bigbasket.mobileapp.model.shipments.Shipment;
import com.bigbasket.mobileapp.model.shipments.Slot;
import com.bigbasket.mobileapp.model.shipments.SlotDisplay;
import com.bigbasket.mobileapp.task.PostShipmentTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShipmentSelectionActivity extends BackButtonActivity {

    @Nullable
    public HashMap<String, BaseShipmentAction> mDefaultShipmentActions;

    @Nullable
    public HashMap<String, HashMap<String, BaseShipmentAction>> mToggleShipmentActions;

    private ArrayList<Shipment> mShipments;
    private boolean mHasUserToggledShipments;
    private ArrayList<Integer> mSelectedShipmentIndx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_DELIVERY_OPS);
        setTitle(getString(R.string.chooseSlot));
        mShipments = getIntent().getParcelableArrayListExtra(Constants.SHIPMENTS);

        String defaultActionsStr = getIntent().getStringExtra(Constants.DEFAULT_ACTIONS);
        String toggleActionsStr = getIntent().getStringExtra(Constants.ON_TOGGLE_ACTIONS);
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(defaultActionsStr)) {
            Type type = new TypeToken<HashMap<String, BaseShipmentAction>>() {
            }.getType();
            mDefaultShipmentActions = gson.fromJson(defaultActionsStr, type);
        }
        if (!TextUtils.isEmpty(toggleActionsStr)) {
            Type type = new TypeToken<HashMap<String, HashMap<String, BaseShipmentAction>>>() {
            }.getType();
            mToggleShipmentActions = gson.fromJson(toggleActionsStr, type);
        }
        if (mShipments == null || mShipments.size() == 0) return;
        renderFooter();
        renderShipments();
        trackEvent(TrackingAware.CHECKOUT_DELIVERY_OPTION_SHOWN, null, null, null, false, true);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_shipment_layout;
    }

    private void renderFooter() {
        OrderDetails orderDetails = getIntent().getParcelableExtra(Constants.ORDER_DETAILS);
        if (orderDetails == null) return;
        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, orderDetails.getFormattedFinalTotal(),
                getString(R.string.continueCaps), true);
        layoutCheckoutFooter.setOnClickListener(new OnPostShipmentClickListener());
    }

    private void renderShipments() {
        displayShipmentsBasedOnViewState(mDefaultShipmentActions);
    }

    private void displayShipmentsBasedOnViewState(@Nullable HashMap<String, BaseShipmentAction> shipmentActionHashMap) {
        mSelectedShipmentIndx = new ArrayList<>();

        LinearLayout layoutShipmentContainer = (LinearLayout) findViewById(R.id.layoutShipmentContainer);
        layoutShipmentContainer.removeAllViews();
        int marginVertical = (int) getResources().getDimension(R.dimen.margin_normal);
        SwitchCompat switchToggleDelivery = (SwitchCompat) findViewById(R.id.switchToggleDelivery);
        TextView txtShipmentAnnotation = (TextView) findViewById(R.id.txtShipmentAnnotation);
        ViewGroup layoutShipmentToggleContainer = (ViewGroup) findViewById(R.id.layoutShipmentToggleContainer);
        boolean hasBeenMadeVisible = false;
        for (int i = 0; i < mShipments.size(); i++) {
            final Shipment shipment = mShipments.get(i);
            View shipmentView = getLayoutInflater().inflate(R.layout.shipment_row,
                    layoutShipmentContainer, false);

            BaseShipmentAction shipmentAction = shipmentActionHashMap != null ?
                    shipmentActionHashMap.get(shipment.getShipmentId()) : null;
            String shipmentName = shipment.getShipmentName();
            String actionName = null;
            boolean applyBottom = true;
            if (shipmentAction != null) {
                if (!TextUtils.isEmpty(shipmentAction.getViewState())) {
                    switch (shipmentAction.getViewState()) {
                        case Constants.HIDDEN:
                            continue;
                        case Constants.SHOW:
                            if (isAdjacentShipmentDisabled(i, shipmentActionHashMap)) {
                                applyBottom = i != 0;
                            }
                            mSelectedShipmentIndx.add(i);
                            shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck));
                            break;
                        case Constants.DISABLED:
                            applyBottom = i != 0;
                            shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck_disabled));
                            break;
                    }
                }

                if (!TextUtils.isEmpty(shipmentAction.getActionMsg())) {
                    actionName = shipmentAction.getActionMsg();
                    if (TextUtils.isEmpty(shipmentAction.getActionState())) {
                        if (!hasBeenMadeVisible) {
                            switchToggleDelivery.setVisibility(View.GONE);
                        }
                    } else {
                        switchToggleDelivery.setChecked(shipmentAction.getActionState().equalsIgnoreCase(Constants.ON));
                        switchToggleDelivery.setOnCheckedChangeListener(new OnShipmentToggleListener(shipment));
                        switchToggleDelivery.setVisibility(View.VISIBLE);
                        layoutShipmentToggleContainer.setVisibility(View.VISIBLE);
                        hasBeenMadeVisible = true;
                    }
                } else {
                    if (!hasBeenMadeVisible) {
                        switchToggleDelivery.setVisibility(View.GONE);
                    }
                }
            } else {
                if (isAdjacentShipmentDisabled(i, shipmentActionHashMap)) {
                    applyBottom = i != 0;
                }
                shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck));
//                switchToggleDelivery.setVisibility(View.GONE);
                mSelectedShipmentIndx.add(i);
            }
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) shipmentView.getLayoutParams();
            marginLayoutParams.bottomMargin = applyBottom ? marginVertical : 0;
            shipmentView.setLayoutParams(marginLayoutParams);

            if (!TextUtils.isEmpty(actionName)) {
                hasBeenMadeVisible = true;
                txtShipmentAnnotation.setTypeface(faceRobotoRegular);
                txtShipmentAnnotation.setText(actionName);
                txtShipmentAnnotation.setVisibility(View.VISIBLE);
                layoutShipmentToggleContainer.setVisibility(View.VISIBLE);
            } else {
                if (!hasBeenMadeVisible) {
                    txtShipmentAnnotation.setVisibility(View.GONE);
                }
            }

            ((TextView) shipmentView.findViewById(R.id.lblDeliveryTime)).setTypeface(faceRobotoRegular);

            TextView txtShipmentName = (TextView) shipmentView.findViewById(R.id.txtShipmentName);
            txtShipmentName.setTypeface(faceRobotoRegular);
            txtShipmentName.setText(shipmentName);

            TextView txtShipmentCount = (TextView) shipmentView.findViewById(R.id.txtShipmentCount);
            txtShipmentName.setTypeface(faceRobotoMedium);

            int count = shipment.getCount();
            String itemStr = " Item";
            if (count > 1) {
                itemStr += "s";
            }
            txtShipmentCount.setText(String.valueOf(count) + itemStr);
            txtShipmentCount.setOnClickListener(new OnViewShipmentLinkedProductsListener(shipment));

            TextView txtDeliveryCharge = (TextView) shipmentView.findViewById(R.id.txtDeliveryCharge);
            txtDeliveryCharge.setTypeface(faceRobotoMedium);

            if (Double.parseDouble(shipment.getDeliveryCharge()) > 0) {
                String deliveryChargelbl = getString(R.string.delivery_charges) + " ";
                String rupeeSym = "`";
                SpannableString spannableString = new SpannableString(deliveryChargelbl + rupeeSym +
                        shipment.getDeliveryCharge());
                spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), deliveryChargelbl.length(),
                        deliveryChargelbl.length() + rupeeSym.length(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtDeliveryCharge.setText(spannableString);
            } else {
                txtDeliveryCharge.setVisibility(View.GONE);
            }

            displaySelectedSlot(shipmentView, shipment);

            ImageView imgHelp = (ImageView) shipmentView.findViewById(R.id.imgHelp);
            if (!TextUtils.isEmpty(shipment.getHelpPage())) {
                imgHelp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                        intent.putExtra(Constants.WEBVIEW_TITLE, shipment.getShipmentName());
                        intent.putExtra(Constants.WEBVIEW_URL, shipment.getHelpPage());
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                });
            } else {
                imgHelp.setVisibility(View.GONE);
            }

            layoutShipmentContainer.addView(shipmentView);
        }
        setShipmentHeaderMsg(mSelectedShipmentIndx.size());
    }

    private boolean isAdjacentShipmentDisabled(int position,
                                               @Nullable HashMap<String, BaseShipmentAction> shipmentActionHashMap) {
        if (mShipments == null || mShipments.size() <= 1) return false;
        Shipment adjShipment;
        if (position == 0) {
            adjShipment = mShipments.get(position + 1);
        } else {
            adjShipment = mShipments.get(position - 1);
        }
        BaseShipmentAction shipmentAction = shipmentActionHashMap != null ?
                shipmentActionHashMap.get(adjShipment.getShipmentId()) : null;
        return shipmentAction != null &&
                !TextUtils.isEmpty(shipmentAction.getViewState()) &&
                shipmentAction.getViewState().equals(Constants.DISABLED);
    }

    private void setShipmentHeaderMsg(int numVisibleShipments) {
        TextView txtDeliverablesHeading = (TextView) findViewById(R.id.txtDeliverablesHeading);
        if (numVisibleShipments > 1) {
            txtDeliverablesHeading.setTypeface(faceRobotoRegular);
            txtDeliverablesHeading.setText(R.string.deliverableTextPlural);
        } else {
            txtDeliverablesHeading.setVisibility(View.GONE);
        }
    }

    private class OnViewShipmentLinkedProductsListener implements View.OnClickListener {
        private Shipment shipment;

        public OnViewShipmentLinkedProductsListener(Shipment shipment) {
            this.shipment = shipment;
        }

        @Override
        public void onClick(View v) {
            showDialog();
        }

        private void showDialog() {
            View base = getLayoutInflater().inflate(R.layout.uiv3_linked_shipment_dialog, null);

            TextView txtShipmentCount = (TextView) base.findViewById(R.id.txtShipmentCount);
            int count = shipment.getCount();
            String itemStr = " Item";
            if (count > 1) {
                itemStr += "s";
            }
            txtShipmentCount.setTypeface(faceRobotoRegular);
            txtShipmentCount.setText(String.valueOf(count) + itemStr);

            TextView txtShipmentName = (TextView) base.findViewById(R.id.txtShipmentName);
            txtShipmentName.setTypeface(faceRobotoMedium);
            txtShipmentName.setText(shipment.getShipmentName());

            ListView lstLinkedShipments = (ListView) base.findViewById(R.id.lstLinkedShipments);
            LinkedProductsAdapter linkedProductsAdapter = new LinkedProductsAdapter(getCurrentActivity(),
                    shipment.getLinkedShipments(), faceRobotoRegular);
            lstLinkedShipments.setAdapter(linkedProductsAdapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity())
                    .setView(base)
                    .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private class OnShipmentToggleListener implements CompoundButton.OnCheckedChangeListener {
        private Shipment shipment;

        public OnShipmentToggleListener(Shipment shipment) {
            this.shipment = shipment;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mHasUserToggledShipments = !mHasUserToggledShipments;
            if (mHasUserToggledShipments) {
                displayShipmentsBasedOnViewState(mToggleShipmentActions != null ?
                        mToggleShipmentActions.get(shipment.getShipmentId()) : null);
            } else {
                renderShipments();
            }
        }
    }

    private class OnPostShipmentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
            trackEvent(TrackingAware.CHECKOUT_SLOT_SELECTED_CLICKED, map, null, null, false, true);
            if (mSelectedShipmentIndx == null || mSelectedShipmentIndx.size() == 0) {
                showToast(getString(R.string.selectAllSlotsErrMsg));
                return;
            }
            ArrayList<SelectedShipment> selectedShipments = new ArrayList<>();
            for (int shipmentIdx : mSelectedShipmentIndx) {
                Shipment shipment = mShipments.get(shipmentIdx);
                if (shipment.getSelectedSlot() == null) {
                    showToast(getString(R.string.selectAllSlotsErrMsg));
                    break;
                }
                SelectedShipment selectedShipment = new
                        SelectedShipment(shipment.getShipmentId(), shipment.getFulfillmentId(),
                        shipment.getSelectedSlot().getSlotId(), shipment.getSelectedSlot().getSlotDate());
                selectedShipments.add(selectedShipment);
            }
            String potentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);
            if (potentialOrderId == null) return;
            new PostShipmentTask<>(getCurrentActivity(), selectedShipments, potentialOrderId,
                    TrackEventkeys.CO_DELIVERY_OPS).startTask();
        }
    }

    private void displaySelectedSlot(View shipmentView, Shipment shipment) {
        ArrayList<Slot> slots = shipment.getSlots();
        if (slots == null || slots.size() == 0) return;
        TextView txtReadonlySelectedSlot = (TextView) shipmentView.findViewById(R.id.txtReadonlySelectedSlot);
        txtReadonlySelectedSlot.setTypeface(faceRobotoMedium);
        Button btnSelectedSlot = (Button) shipmentView.findViewById(R.id.btnSelectedSlot);
        btnSelectedSlot.setTypeface(faceRobotoMedium);
        Slot selectedSlot;
        if (slots.size() == 1) {
            btnSelectedSlot.setVisibility(View.GONE);
            selectedSlot = setAvailableSlot(slots);
            shipment.setSelectedSlot(selectedSlot);
            if (selectedSlot.getSlotDisplay() != null) {
                showSelectedSlot(selectedSlot, txtReadonlySelectedSlot);
            }
        } else {
            txtReadonlySelectedSlot.setVisibility(View.GONE);
            selectedSlot = setAvailableSlot(slots);
            shipment.setSelectedSlot(selectedSlot);
            if (selectedSlot.getSlotDisplay() != null) {
                showSelectedSlot(selectedSlot, btnSelectedSlot);
            }
            btnSelectedSlot.setOnClickListener(new OnSelectSlotClickListener(shipment));
        }
    }

    private Slot setAvailableSlot(ArrayList<Slot> slots) {
        for (Slot slot : slots) {
            if (slot.isAvailable())
                return slot;
        }
        return slots.get(0);
    }

    private void showSelectedSlot(Slot selectedSlot, TextView txtVw) {
        if (selectedSlot.getSlotDisplay() != null) {
            SlotDisplay slotDisplay = selectedSlot.getSlotDisplay();
            String display = TextUtils.isEmpty(slotDisplay.getDate()) ? slotDisplay.getTime() :
                    slotDisplay.getDate() + "     " + slotDisplay.getTime();
            txtVw.setText(display);
        }
    }

    private class OnSelectSlotClickListener implements View.OnClickListener {

        private Shipment shipment;
        private Dialog mSlotListDialog;

        public OnSelectSlotClickListener(Shipment shipment) {
            this.shipment = shipment;
        }

        @Override
        public void onClick(View v) {
            showSlotListDialog(v);
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
            trackEvent(TrackingAware.CHECKOUT_SLOT_SHOWN, map);
        }

        public void showSlotListDialog(final View v) {
            mSlotListDialog = new Dialog(getCurrentActivity());
            mSlotListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSlotListDialog.setCanceledOnTouchOutside(true);
            ListView lstViewSlot = new ListView(getCurrentActivity());
            lstViewSlot.setDivider(null);
            lstViewSlot.setDividerHeight(0);
            mSlotListDialog.setContentView(lstViewSlot);

            final List<Object> flattenedSlotGroupList =
                    Slot.getFlattenedSlotGroupList(shipment.getSlots());
            SlotListAdapter slotListAdapter = new SlotListAdapter<>(getCurrentActivity(),
                    flattenedSlotGroupList, faceRobotoRegular, shipment.getSelectedSlot());
            lstViewSlot.setAdapter(slotListAdapter);
            lstViewSlot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mSlotListDialog != null && mSlotListDialog.isShowing()) {
                        mSlotListDialog.dismiss();
                    }
                    // Defensive check
                    Slot selectedSlot;
                    if (flattenedSlotGroupList.get(position) instanceof Slot) {
                        selectedSlot = (Slot) flattenedSlotGroupList.get(position);
                        shipment.setSelectedSlot(selectedSlot);
                        if (v instanceof Button) {
                            showSelectedSlot(selectedSlot, (Button) v);
                        }

                        HashMap<String, String> map = new HashMap<>();
                        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                        if (selectedSlot.getSlotDisplay() != null)
                            map.put(TrackEventkeys.SELECTED_SLOT, selectedSlot.getSlotDisplay().getTime());
                        trackEvent(TrackingAware.CHECKOUT_SLOT_SELECTED, map);
                    }
                }
            });

            Rect displayRectangle = new Rect();
            getCurrentActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            mSlotListDialog.getWindow().setLayout(displayRectangle.width() - 20,
                    (int) (displayRectangle.height() * 0.7f));
            mSlotListDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            setResult(NavigationCodes.GO_TO_SLOT_SELECTION);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SLOT_SELECTION_SCREEN;
    }
}
