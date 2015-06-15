package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.adapter.shipment.SlotListAdapter;
import com.bigbasket.mobileapp.apiservice.models.request.SelectedShipment;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.shipments.BaseShipmentAction;
import com.bigbasket.mobileapp.model.shipments.Shipment;
import com.bigbasket.mobileapp.model.shipments.ShipmentAction;
import com.bigbasket.mobileapp.model.shipments.Slot;
import com.bigbasket.mobileapp.model.shipments.SlotDisplay;
import com.bigbasket.mobileapp.model.shipments.ToggleShipmentAction;
import com.bigbasket.mobileapp.task.PostShipmentTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShipmentSelectionActivity extends BackButtonActivity {

    private LinkedHashMap<String, Shipment> mShipmentLinkedHashMap;
    private boolean mHasUserToggledShipments;
    private ArrayList<String> selectedShipmentIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseSlot));
        ArrayList<Shipment> shipments = getIntent().getParcelableArrayListExtra(Constants.SHIPMENTS);
        if (shipments == null || shipments.size() == 0) return;
        mShipmentLinkedHashMap = getShipmentsMap(shipments);
        renderFooter();
        renderShipments();
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
                getString(R.string.choosePayment));
        layoutCheckoutFooter.setOnClickListener(new OnPostShipmentClickListener());
    }

    private void renderShipments() {
        HashMap<String, BaseShipmentAction> shipmentActionHashMap = new HashMap<>();
        for (Map.Entry<String, Shipment> entry : mShipmentLinkedHashMap.entrySet()) {
            Shipment shipment = entry.getValue();
            ShipmentAction shipmentAction = shipment.getShipmentAction();
            if (shipmentAction != null) {
                BaseShipmentAction baseShipmentAction =
                        new BaseShipmentAction(shipmentAction.getActionMsg(),
                                shipmentAction.getViewState(), shipmentAction.getActionState());
                shipmentActionHashMap.put(shipment.getShipmentId(), baseShipmentAction);
            }
        }
        displayShipmentsBasedOnViewState(shipmentActionHashMap);
    }

    private void displayShipmentsBasedOnViewState(HashMap<String, BaseShipmentAction> shipmentActionHashMap) {
        selectedShipmentIds = new ArrayList<>();
        TextView txtDeliverablesHeading = (TextView) findViewById(R.id.txtDeliverablesHeading);
        txtDeliverablesHeading.setTypeface(faceRobotoRegular);
        txtDeliverablesHeading.setText(mShipmentLinkedHashMap.size() > 1 ? R.string.deliverableTextPlural :
                R.string.deliverableTextSingular);

        LinearLayout layoutShipmentContainer = (LinearLayout) findViewById(R.id.layoutShipmentContainer);
        layoutShipmentContainer.removeAllViews();
        if (mShipmentLinkedHashMap == null) return;
        for (Map.Entry<String, Shipment> entry : mShipmentLinkedHashMap.entrySet()) {
            final Shipment shipment = entry.getValue();
            View shipmentView = getLayoutInflater().inflate(R.layout.shipment_row,
                    layoutShipmentContainer, false);

            SwitchCompat switchToggleDelivery = (SwitchCompat) shipmentView.findViewById(R.id.switchToggleDelivery);
            BaseShipmentAction shipmentAction = shipmentActionHashMap.get(shipment.getShipmentId());
            String shipmentName = shipment.getShipmentName();
            if (shipmentAction != null) {
                if (!TextUtils.isEmpty(shipmentAction.getViewState())) {
                    switch (shipmentAction.getViewState()) {
                        case Constants.HIDDEN:
                            continue;
                        case Constants.SHOW:
                            selectedShipmentIds.add(shipment.getShipmentId());
                            shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck));
                            break;
                        case Constants.DISABLED:
                            shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck_disabled));
                            break;
                    }
                }
                if (!TextUtils.isEmpty(shipmentAction.getActionMsg())) {
                    shipmentName = shipmentAction.getActionMsg();
                    switchToggleDelivery.setChecked(!TextUtils.isEmpty(shipmentAction.getActionState())
                            && shipmentAction.getActionState().equalsIgnoreCase(Constants.ON));
                    switchToggleDelivery.setOnCheckedChangeListener(new OnShipmentToggleListener(shipment));
                } else {
                    switchToggleDelivery.setVisibility(View.GONE);
                }
            } else {
                shipmentView.setBackgroundColor(getResources().getColor(R.color.uiv3_large_list_item_bck));
                switchToggleDelivery.setVisibility(View.GONE);
                selectedShipmentIds.add(shipment.getShipmentId());
            }
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
                        Intent intent = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
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
                HashMap<String, BaseShipmentAction> shipmentActionHashMap = new HashMap<>();
                ShipmentAction shipmentAction = shipment.getShipmentAction();
                if (shipmentAction != null && shipmentAction.getShipmentActionInfo() != null) {
                    ArrayList<ToggleShipmentAction> hide = shipmentAction.getShipmentActionInfo().getHide();
                    ArrayList<ToggleShipmentAction> show = shipmentAction.getShipmentActionInfo().getShow();
                    if (hide != null && hide.size() > 0) {
                        updateMapFromToggleList(shipmentActionHashMap, hide);
                    }
                    if (show != null && show.size() > 0) {
                        updateMapFromToggleList(shipmentActionHashMap, show);
                    }
                    displayShipmentsBasedOnViewState(shipmentActionHashMap);
                }
            } else {
                renderShipments();
            }
        }

        private void updateMapFromToggleList(HashMap<String, BaseShipmentAction> shipmentActionHashMap,
                                             ArrayList<ToggleShipmentAction> toggleShipmentActions) {
            for (ToggleShipmentAction toggleShipmentAction : toggleShipmentActions) {
                shipmentActionHashMap.put(toggleShipmentAction.getId(),
                        new BaseShipmentAction(toggleShipmentAction.getActionMsg(),
                                toggleShipmentAction.getViewState(),
                                toggleShipmentAction.getActionState()));
            }
        }
    }

    private class OnPostShipmentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedShipmentIds == null || selectedShipmentIds.size() == 0) {
                showToast(getString(R.string.selectAllSlotsErrMsg));
                return;
            }
            ArrayList<SelectedShipment> selectedShipments = new ArrayList<>();
            for (String shipmentId : selectedShipmentIds) {
                Shipment shipment = mShipmentLinkedHashMap.get(shipmentId);
                if (shipment.getSelectedSlot() == null) {
                    showToast(getString(R.string.selectAllSlotsErrMsg));
                    break;
                }
                SelectedShipment selectedShipment = new
                        SelectedShipment(shipmentId, shipment.getFulfillmentId(),
                        shipment.getSelectedSlot().getSlotId(), shipment.getSelectedSlot().getSlotDate(),
                        shipment.getSelectedSlot().getSlotTime());
                selectedShipments.add(selectedShipment);
            }
            String potentialOrderId = getIntent().getStringExtra(Constants.P_ORDER_ID);
            if (potentialOrderId == null) return;
            new PostShipmentTask<>(getCurrentActivity(), selectedShipments, potentialOrderId).startTask();
        }
    }

    @Nullable
    private LinkedHashMap<String, Shipment> getShipmentsMap(ArrayList<Shipment> shipments) {
        if (shipments == null) return null;
        LinkedHashMap<String, Shipment> shipmentLinkedHashMap = new LinkedHashMap<>();
        for (Shipment shipment : shipments) {
            shipmentLinkedHashMap.put(shipment.getShipmentId(), shipment);
        }
        return shipmentLinkedHashMap;
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
            selectedSlot = slots.get(0);
            shipment.setSelectedSlot(selectedSlot);
            if (selectedSlot.getSlotDisplay() != null) {
                showSelectedSlot(selectedSlot, txtReadonlySelectedSlot);
            }
        } else {
            txtReadonlySelectedSlot.setVisibility(View.GONE);
            selectedSlot = slots.get(0);
            shipment.setSelectedSlot(selectedSlot);
            if (selectedSlot.getSlotDisplay() != null) {
                showSelectedSlot(selectedSlot, btnSelectedSlot);
            }
            btnSelectedSlot.setOnClickListener(new OnSelectSlotClickListener(shipment));
        }
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
}
