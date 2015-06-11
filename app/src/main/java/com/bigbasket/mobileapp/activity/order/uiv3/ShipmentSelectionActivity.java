package com.bigbasket.mobileapp.activity.order.uiv3;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.adapter.shipment.SlotListAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.order.OrderDetails;
import com.bigbasket.mobileapp.model.shipments.Shipment;
import com.bigbasket.mobileapp.model.shipments.Slot;
import com.bigbasket.mobileapp.model.shipments.SlotDisplay;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class ShipmentSelectionActivity extends BackButtonActivity {

    private ArrayList<Shipment> mShipments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.chooseSlot));
        mShipments = getIntent().getParcelableArrayListExtra(Constants.SHIPMENTS);
        if (mShipments == null || mShipments.size() == 0) return;
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
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getCurrentActivity(), PaymentSelectionActivity.class);
                intent.putParcelableArrayListExtra(Constants.SHIPMENTS, mShipments);
                intent.putExtra(Constants.ORDER_DETAILS, getIntent().getParcelableExtra(Constants.ORDER_DETAILS));
                intent.putExtra(Constants.EVOUCHER_CODE, getIntent().getParcelableExtra(Constants.EVOUCHER_CODE));
                intent.putParcelableArrayListExtra(Constants.VOUCHERS,
                        getIntent().getParcelableArrayListExtra(Constants.VOUCHERS));
                intent.putParcelableArrayListExtra(Constants.PAYMENT_TYPES,
                        getIntent().getParcelableArrayListExtra(Constants.PAYMENT_TYPES));
                intent.putExtra(Constants.CREDIT_DETAILS,
                        getIntent().getParcelableArrayListExtra(Constants.CREDIT_DETAILS));
                intent.putExtra(Constants.P_ORDER_ID,
                        getIntent().getStringExtra(Constants.P_ORDER_ID));
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        });
    }

    private void renderShipments() {
        TextView txtDeliverablesHeading = (TextView) findViewById(R.id.txtDeliverablesHeading);
        txtDeliverablesHeading.setTypeface(faceRobotoRegular);
        txtDeliverablesHeading.setText(mShipments.size() > 1 ? R.string.deliverableTextSingular :
                R.string.deliverableTextSingular);

        LinearLayout layoutShipmentContainer = (LinearLayout) findViewById(R.id.layoutShipmentContainer);
        for (final Shipment shipment : mShipments) {
            View shipmentView = getLayoutInflater().inflate(R.layout.shipment_row,
                    layoutShipmentContainer, false);
            TextView txtShipmentName = (TextView) shipmentView.findViewById(R.id.txtShipmentName);
            txtShipmentName.setTypeface(faceRobotoRegular);
            txtShipmentName.setText(shipment.getShipmentName());

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

            String deliveryChargelbl = getString(R.string.delivery_charges) + " ";
            String rupeeSym = "`";
            SpannableString spannableString = new SpannableString(deliveryChargelbl + rupeeSym +
                    shipment.getDeliveryCharge());
            spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), deliveryChargelbl.length(),
                    deliveryChargelbl.length() + rupeeSym.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtDeliveryCharge.setText(spannableString);

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
                    slotDisplay.getDate() + "\t" + slotDisplay.getTime();
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
