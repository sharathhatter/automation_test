package com.bigbasket.mobileapp.fragment.order;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.slot.*;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SlotSelectionFragment extends BaseFragment {

    private List<SlotGroup> slotGroupList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadSlots();
    }

    private void loadSlots() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String addressId = Address.getAddressIdFromPreferences(getActivity());
        String potentialOrderId = preferences.getString(Constants.POTENTIAL_ORDER_ID, null);
        String url = MobileApiUrl.getBaseAPIUrl() + Constants.POST_DELIVERY_ADDR;
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.P_ORDER_ID, potentialOrderId);
        params.put(Constants.ADDRESS_ID, addressId);
        startAsyncActivity(url, params, true, true, null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        String url = httpOperationResult.getUrl();
        if (url.contains(Constants.POST_DELIVERY_ADDR)) {
            try {
                JSONObject responseJsonObj = new JSONObject(httpOperationResult.getReponseString());
                String status = responseJsonObj.getString(Constants.STATUS);
                switch (status) {
                    case Constants.OK:
                        JSONObject response = responseJsonObj.getJSONObject(Constants.RESPONSE);
                        JSONArray slotsInfo = response.getJSONArray(Constants.SLOTS_INFO);
                        slotGroupList = ParserUtil.parseSlotsList(slotsInfo);
                        displaySlotGroups();
                        break;
                    case Constants.ERROR:
                        showErrorMsg("Server Error");
                        break;
                }
            } catch (JSONException ex) {
                // TODO : Change this later on
                showErrorMsg("Invalid response");
            }
        } else {
            super.onAsyncTaskComplete(httpOperationResult);
        }
    }

    private void displaySlotGroups() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        contentView.removeAllViews();
        if (slotGroupList.size() == 1) {
            ListView listViewSlots = new ListView(getActivity());
            listViewSlots.setDividerHeight(0);
            listViewSlots.setDivider(null);
            contentView.addView(listViewSlots);
            fillSlotList(listViewSlots, slotGroupList.get(0));
        } else {
            // info message for multiple slot selection

            LinearLayout msgLayout = new LinearLayout(getActivity());
            final LinearLayout.LayoutParams main1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            main1Params.setMargins(20, 5, 20, 10);
            msgLayout.setLayoutParams(main1Params);
            contentView.addView(msgLayout);

            TextView infoMsgMultipleSlotSelection = new TextView(getActivity());
            infoMsgMultipleSlotSelection.setText(getResources().getString(R.string.multipleDeliverySlotMsg1));
            infoMsgMultipleSlotSelection.setGravity(Gravity.CENTER_HORIZONTAL);
            infoMsgMultipleSlotSelection.setTextColor(getResources().getColor(R.color.active_order_red_color));
            infoMsgMultipleSlotSelection.setTypeface(faceRobotoRegular);
            msgLayout.addView(infoMsgMultipleSlotSelection);

            View viewLine = new View(getActivity());
            final LinearLayout.LayoutParams layoutLine2Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            layoutLine2Params.setMargins(0, 20, 0, 0);
            viewLine.setLayoutParams(layoutLine2Params);
            viewLine.setBackgroundColor(getResources().getColor(R.color.strokeLine));
            contentView.addView(viewLine);


            SlotGroupListAdapter slotGroupListAdapter = new SlotGroupListAdapter();
            ListView slotGroupListView = new ListView(getActivity());
            slotGroupListView.setAdapter(slotGroupListAdapter);
            slotGroupListView.setDivider(null);
            slotGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SlotGroup slotGroup = slotGroupList.get(position);
                    showSlotListDialog(slotGroup);
                }
            });
            contentView.addView(slotGroupListView);
        }
    }

    public void showSlotListDialog(SlotGroup slotGroup) {
//        slotListDialog = new Dialog(this);
//        slotListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        slotListDialog.setContentView(R.layout.dialog_list_layout);
//        TextView txtDialogTitle = (TextView) slotListDialog.findViewById(R.id.txtDialogTitle);
//        txtDialogTitle.setText(StringUtils.abbreviate(slotGroup.getFulfillmentInfo().getFulfilledBy(), 25));
//        ListView lstViewSlot = (ListView) slotListDialog.findViewById(R.id.listViewDialog);
//        fillSlotList(lstViewSlot, slotGroup);
//        ImageView imgCloseBtn = (ImageView) slotListDialog.findViewById(R.id.imgCloseBtn);
//        imgCloseBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (slotListDialog != null && slotListDialog.isShowing()) {
//                    slotListDialog.dismiss();
//                } else {
//                    return;
//                }
//            }
//        });
//        slotListDialog.show();
    }

    private void fillSlotList(ListView listViewSlot, final SlotGroup slotGroup) {
        final List<BaseSlot> flattenedSlotGroupList =
                Slot.getFlattenedSlotGroupList(slotGroup.getSlotList());
        Slot nxtAvailableSlot = slotGroup.getNextAvailableSlot();
        if (nxtAvailableSlot != null) {
            SlotHeader slotHeader = new SlotHeader("Next available slot as of now");
            flattenedSlotGroupList.add(0, slotHeader);
            flattenedSlotGroupList.add(1, nxtAvailableSlot);
        }
        SlotListAdapter slotListAdapter = new SlotListAdapter(flattenedSlotGroupList);
        listViewSlot.setAdapter(slotListAdapter);
        listViewSlot.setDivider(null);
        listViewSlot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaseSlot baseSlot = flattenedSlotGroupList.get(position);
                if (baseSlot instanceof Slot) {
                    Slot slot = (Slot) baseSlot;
                    if (!slot.isAvailable()) {
                        return;
                    }
                    onSlotSelected(slotGroup, slot);
                }
            }
        });
    }

    private void onSlotSelected(SlotGroup slotGroup, Slot slot) {
        slotGroup.setSelectedSlot(slot);
        if (slotGroupList.size() > 1) {
//            if (slotListDialog != null && slotListDialog.isShowing()) {
//                slotListDialog.dismiss();
//            } else {
//                return;
//            }
//            slotGroupListAdapter.notifyDataSetChanged();
//            if (areAllSlotGroupsSelected()) {
//                startCheckout();
//            }
        } else {
            startCheckout();
        }
    }

    private void startCheckout() {
        if (!areAllSlotGroupsSelected()) {
            showErrorMsg(getString(R.string.selectAllSlotsErrMsg));
            return;
        }
        ArrayList<SelectedSlotType> selectedSlotTypeList = new ArrayList<>();
        for (SlotGroup slotGroup : slotGroupList) {
            Slot slot = slotGroup.getSelectedSlot();
            selectedSlotTypeList.add(new SelectedSlotType(slotGroup.getFulfillmentInfo().getFulfillmentId(),
                    slot.getSlotId(), slot.getSlotDate()));
        }

        PaymentSelectionFragment paymentSelectionFragment = new PaymentSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.SLOTS, selectedSlotTypeList);
        paymentSelectionFragment.setArguments(bundle);
        changeFragment(paymentSelectionFragment);
    }

    private boolean areAllSlotGroupsSelected() {
        for (SlotGroup slotGroup : slotGroupList) {
            if (!slotGroup.isSelected()) {
                return false;
            }
        }
        return true;
    }


    private class SlotListAdapter extends BaseAdapter {
        private List<BaseSlot> flattenedSlotGroupList;
        private int slotNotAvailableTextColor;
        private int size;

        public SlotListAdapter(List<BaseSlot> flattenedSlotGroupList) {
            this.flattenedSlotGroupList = flattenedSlotGroupList;
            this.slotNotAvailableTextColor = getResources().getColor(R.color.slotSeletionNotAvailableColor);
            this.size = flattenedSlotGroupList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return flattenedSlotGroupList.get(position);
        }

        @Override
        public int getCount() {
            return this.size;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BaseSlot baseSlot = flattenedSlotGroupList.get(position);
            if (baseSlot instanceof Slot) {
                View slotView = getSlotView((Slot) baseSlot, convertView);
                View listSeparator = slotView.findViewById(R.id.listSeparator);
                LinearLayout itemTextLayout = (LinearLayout) slotView.findViewById(R.id.itemTextLayout);
                if ((position + 1) < size && flattenedSlotGroupList.get(position + 1) instanceof SlotHeader) {
                    listSeparator.setVisibility(View.VISIBLE);
                    itemTextLayout.setPadding(0, 16, 0, 16);
                } else {
                    itemTextLayout.setPadding(0, 16, 0, 0);
                    listSeparator.setVisibility(View.GONE);
                }
                return slotView;
            }
            return getHeaderView((SlotHeader) baseSlot, convertView);
        }

        private View getHeaderView(SlotHeader slotHeader, View row) {
            if (row == null || row.getTag() == null || !row.getTag().toString().equalsIgnoreCase(Constants.IS_SLOT_HEADER)) {
                row = getInflatedHeaderView();
                row.setTag(Constants.IS_SLOT_HEADER);
            }
            TextView txtHeaderMsg = (TextView) row.findViewById(R.id.txtHeaderMsg);

            txtHeaderMsg.setTypeface(faceRobotoRegular);

            txtHeaderMsg.setText(slotHeader.getFormattedDisplayName());
            return row;
        }

        private View getSlotView(Slot slot, View row) {
            if (row == null || row.getTag() == null || !row.getTag().toString().equalsIgnoreCase(Constants.IS_SLOT)) {
                row = getInflatedSlotView();
                row.setTag(Constants.IS_SLOT);
                row.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
            }
            TextView txtTitle = (TextView) row.findViewById(R.id.itemTitle);
            TextView txtSubTitle = (TextView) row.findViewById(R.id.itemSubTitle);
            ImageView imgRow = (ImageView) row.findViewById(R.id.itemImg);

            txtTitle.setTypeface(faceRobotoRegular);
            txtSubTitle.setTypeface(faceRobotoRegular);

            txtTitle.setText(slot.getFormattedSlotDate());
            txtSubTitle.setText(slot.getFormattedDisplayName());

            if (slot.isAvailable()) {
                imgRow.setImageResource(R.drawable.van);
            } else {
                imgRow.setImageResource(R.drawable.deliveryslot_deactive);
            }
            return row;
        }

        private View getInflatedHeaderView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.uiv3_list_title, null);
        }

        private View getInflatedSlotView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.uiv3_list_icon_and_two_texts_row, null);
        }
    }

    private class SlotGroupListAdapter extends BaseAdapter {
        @Override
        public Object getItem(int position) {
            return slotGroupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return slotGroupList.size();
        }

        private class SlotGroupHolder {
            private View base;
            private TextView txtListRowHeader;
            private TextView txtListRowSubHeader;
            private TextView txtListRowContent;
            private ImageView imgLstRow;

            private SlotGroupHolder(View base) {
                this.base = base;
            }

            public TextView getTxtListRowHeader() {
                if (txtListRowHeader == null) {
                    txtListRowHeader = (TextView) base.findViewById(R.id.txtListRowHeader);
                    txtListRowHeader.setTypeface(faceRobotoRegular);
                }
                return txtListRowHeader;
            }

            public TextView getTxtListRowSubHeader() {
                if (txtListRowSubHeader == null) {
                    txtListRowSubHeader = (TextView) base.findViewById(R.id.txtListRowSubHeader);
                    txtListRowSubHeader.setTypeface(faceRobotoRegular);
                }
                return txtListRowSubHeader;
            }

            public TextView getTxtListRowContent() {
                if (txtListRowContent == null) {
                    txtListRowContent = (TextView) base.findViewById(R.id.txtListRowContent);
                    txtListRowContent.setTypeface(faceRobotoRegular);
                }
                return txtListRowContent;
            }

            public ImageView getImgLstRow() {
                if (imgLstRow == null) {
                    imgLstRow = (ImageView) base.findViewById(R.id.imgLstRow);
                }
                return imgLstRow;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            SlotGroupHolder slotGroupHolder;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_row_with_arrow, null);
                slotGroupHolder = new SlotGroupHolder(row);
                row.setTag(slotGroupHolder);
            } else {
                slotGroupHolder = (SlotGroupHolder) row.getTag();
            }
            SlotGroup slotGroup = slotGroupList.get(position);
            TextView txtListRowHeader = slotGroupHolder.getTxtListRowHeader();
            TextView txtListRowSubHeader = slotGroupHolder.getTxtListRowSubHeader();
            TextView txtListRowContent = slotGroupHolder.getTxtListRowContent();

            ImageView imgLstRow = slotGroupHolder.getImgLstRow();
            if (slotGroup.isSelected()) {
                imgLstRow.setVisibility(View.VISIBLE);
                txtListRowContent.setVisibility(View.VISIBLE);
                txtListRowContent.setText(slotGroup.getSelectedSlot().getFormattedSlotDate()
                        + ", " + slotGroup.getSelectedSlot().getFormattedDisplayName());
            } else {
                txtListRowContent.setVisibility(View.GONE);
                imgLstRow.setVisibility(View.GONE);
            }
            FulfillmentInfo fulfillmentInfo = slotGroup.getFulfillmentInfo();
            txtListRowHeader.setText(fulfillmentInfo.getFulfilledBy());
            if (TextUtils.isEmpty(fulfillmentInfo.getDisplayName())) {
                txtListRowSubHeader.setVisibility(View.GONE);
            } else {
                txtListRowSubHeader.setVisibility(View.VISIBLE);
                txtListRowSubHeader.setText(Html.fromHtml(fulfillmentInfo.getDisplayName()));
            }
            return row;
        }
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Choose Slot";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SlotSelectionFragment.class.getName();
    }
}