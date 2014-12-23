package com.bigbasket.mobileapp.fragment.order;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.SelectedSlotAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.slot.BaseSlot;
import com.bigbasket.mobileapp.model.slot.SelectedSlotType;
import com.bigbasket.mobileapp.model.slot.Slot;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.model.slot.SlotHeader;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.List;


public class SlotSelectionFragment extends BaseFragment {

    private List<SlotGroup> mSlotGroupList;
    private Dialog mSlotListDialog;
    private SlotGroupListAdapter mSlotGroupListAdapter;
    private SlotListAdapter mSlotListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSlotGroupList = getArguments().getParcelableArrayList(Constants.SLOTS_INFO);
        displaySlotGroups();
    }

    private void displaySlotGroups() {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        // Set default selected slot
        for (SlotGroup slotGroup : mSlotGroupList) {
            // Manually trigger slot selected event
            onSlotSelected(slotGroup, slotGroup.getNextAvailableSlot());
        }

        contentView.removeAllViews();
        if (mSlotGroupList.size() == 1) {
            ListView listViewSlots = new ListView(getActivity());
            listViewSlots.setDividerHeight(0);
            listViewSlots.setDivider(null);
            contentView.addView(listViewSlots);
            fillSlotList(listViewSlots, mSlotGroupList.get(0));
        } else {
            // info message for multiple slot selection
            LayoutInflater inflater = getActivity().getLayoutInflater();
            TextView infoMsgMultipleSlotSelection = (TextView)
                    inflater.inflate(R.layout.uiv3_multiple_delivery_slot_msg, null);
            infoMsgMultipleSlotSelection.setTypeface(faceRobotoRegular);
            contentView.addView(infoMsgMultipleSlotSelection);
            mSlotGroupListAdapter = new SlotGroupListAdapter();

            ListView slotGroupListView = new ListView(getActivity());
            slotGroupListView.setAdapter(mSlotGroupListAdapter);
            slotGroupListView.setDivider(null);
            slotGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SlotGroup slotGroup = mSlotGroupList.get(position);
                    showSlotListDialog(slotGroup);
                }
            });
            contentView.addView(slotGroupListView);
            trackEvent(TrackingAware.CHECKOUT_SLOT_SHOWN, null);
        }
    }

    public void showSlotListDialog(SlotGroup slotGroup) {
        mSlotListDialog = new Dialog(getActivity());
        mSlotListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSlotListDialog.setCanceledOnTouchOutside(true);
        ListView lstViewSlot = new ListView(getActivity());
        lstViewSlot.setDivider(null);
        lstViewSlot.setDividerHeight(0);
        mSlotListDialog.setContentView(lstViewSlot);
        fillSlotList(lstViewSlot, slotGroup);
        Rect displayRectangle = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        mSlotListDialog.getWindow().setLayout(displayRectangle.width() - 20,
                (int) (displayRectangle.height() * 0.7f));
        mSlotListDialog.show();
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
        mSlotListAdapter = new SlotListAdapter(flattenedSlotGroupList,
                slotGroup);
        listViewSlot.setAdapter(mSlotListAdapter);
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
        setSelectedSlot();
        if (mSlotGroupList.size() > 1) {
            if (mSlotListDialog != null && mSlotListDialog.isShowing()) {
                mSlotListDialog.dismiss();
            } else {
                return;
            }
            if (mSlotGroupListAdapter == null) return;
            mSlotGroupListAdapter.notifyDataSetChanged();
            if (areAllSlotGroupsSelected()) {
                trackEvent(TrackingAware.CHECKOUT_SLOT_CHOOSEN, null);
                setSelectedSlot();
            }
        } else {
            if (mSlotListAdapter == null) return;
            trackEvent(TrackingAware.CHECKOUT_SLOT_CHOOSEN, null);
            mSlotListAdapter.notifyDataSetChanged();
        }
    }

    private void setSelectedSlot() {
        if (getCurrentActivity() == null) return;

        if (!areAllSlotGroupsSelected()) {
            ((SelectedSlotAware) getCurrentActivity()).setSelectedSlotType(null);
            return;
        }

        ArrayList<SelectedSlotType> selectedSlotTypeList = new ArrayList<>();
        for (SlotGroup slotGroup : mSlotGroupList) {
            Slot slot = slotGroup.getSelectedSlot();
            selectedSlotTypeList.add(new SelectedSlotType(slotGroup.getFulfillmentInfo().getFulfillmentId(),
                    slot.getSlotId(), slot.getSlotDate()));
        }

        ((SelectedSlotAware) getCurrentActivity()).setSelectedSlotType(selectedSlotTypeList);
    }

    private boolean areAllSlotGroupsSelected() {
        for (SlotGroup slotGroup : mSlotGroupList) {
            if (!slotGroup.isSelected()) {
                return false;
            }
        }
        return true;
    }


    private class SlotListAdapter extends BaseAdapter {
        private List<BaseSlot> flattenedSlotGroupList;
        private SlotGroup slotGroup;
        private int size;
        private int VIEW_TYPE_HEADER = 0;
        private int VIEW_TYPE_ITEM = 1;

        public SlotListAdapter(List<BaseSlot> flattenedSlotGroupList, SlotGroup slotGroup) {
            this.flattenedSlotGroupList = flattenedSlotGroupList;
            this.size = flattenedSlotGroupList.size();
            this.slotGroup = slotGroup;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return flattenedSlotGroupList.get(position) instanceof Slot ? VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
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
            if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                View slotView = getSlotView((Slot) baseSlot, convertView);
                View listSeparator = slotView.findViewById(R.id.listSeparator);
                if ((position + 1) < size && getItemViewType(position + 1) == VIEW_TYPE_HEADER) {
                    listSeparator.setVisibility(View.VISIBLE);
                } else {
                    listSeparator.setVisibility(View.GONE);
                }
                return slotView;
            }
            return getHeaderView((SlotHeader) baseSlot, convertView);
        }

        private View getHeaderView(SlotHeader slotHeader, View row) {
            SlotHeaderViewHolder slotHeaderViewHolder;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.uiv3_list_title, null);
                slotHeaderViewHolder = new SlotHeaderViewHolder(row);
                row.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
                row.setTag(slotHeaderViewHolder);
            } else {
                slotHeaderViewHolder = (SlotHeaderViewHolder) row.getTag();
            }
            TextView txtHeaderMsg = slotHeaderViewHolder.getTxtHeaderMsg();
            txtHeaderMsg.setText(slotHeader.getFormattedDisplayName());
            return row;
        }

        private View getSlotView(Slot slot, View row) {
            SlotViewHolder slotViewHolder;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.uiv3_list_icon_and_two_texts_row, null);
                row.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
                slotViewHolder = new SlotViewHolder(row);
                row.setTag(slotViewHolder);
            } else {
                slotViewHolder = (SlotViewHolder) row.getTag();
            }
            TextView txtTitle = slotViewHolder.getTxtTitle();
            TextView txtSubTitle = slotViewHolder.getTxtSubTitle();
            ImageView imgRow = slotViewHolder.getImgRow();

            txtTitle.setText(slot.getFormattedSlotDate());
            txtSubTitle.setText(slot.getFormattedDisplayName());

            if (slot.isAvailable()) {
                imgRow.setImageResource(R.drawable.van);
            } else {
                imgRow.setImageResource(R.drawable.deliveryslot_deactive);
            }

            CheckBox itemTick = slotViewHolder.getItemTick();
            if (slot == slotGroup.getSelectedSlot()) {
                itemTick.setChecked(true);
                itemTick.setVisibility(View.VISIBLE);
                row.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));
            } else {
                itemTick.setChecked(false);
                itemTick.setVisibility(View.GONE);
                row.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
            }
            return row;
        }

        private class SlotViewHolder {
            private TextView txtTitle;
            private TextView txtSubTitle;
            private ImageView imgRow;
            private CheckBox itemTick;
            private View base;

            private SlotViewHolder(View base) {
                this.base = base;
            }

            public TextView getTxtTitle() {
                if (txtTitle == null) {
                    txtTitle = (TextView) base.findViewById(R.id.itemTitle);
                    txtTitle.setTypeface(faceRobotoRegular);
                }
                return txtTitle;
            }

            public TextView getTxtSubTitle() {
                if (txtSubTitle == null) {
                    txtSubTitle = (TextView) base.findViewById(R.id.itemSubTitle);
                    txtSubTitle.setTypeface(faceRobotoRegular);
                }
                return txtSubTitle;
            }

            public ImageView getImgRow() {
                if (imgRow == null) {
                    imgRow = (ImageView) base.findViewById(R.id.itemImg);
                }
                return imgRow;
            }

            public CheckBox getItemTick() {
                if (itemTick == null) {
                    itemTick = (CheckBox) base.findViewById(R.id.itemTick);
                }
                return itemTick;
            }
        }

        private class SlotHeaderViewHolder {
            private TextView txtHeaderMsg;
            private View base;

            private SlotHeaderViewHolder(View base) {
                this.base = base;
            }

            public TextView getTxtHeaderMsg() {
                if (txtHeaderMsg == null) {
                    txtHeaderMsg = (TextView) base.findViewById(R.id.txtHeaderMsg);
                    txtHeaderMsg.setTypeface(faceRobotoRegular);
                }
                return txtHeaderMsg;
            }
        }
    }

    private class SlotGroupListAdapter extends BaseAdapter {
        @Override
        public Object getItem(int position) {
            return mSlotGroupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mSlotGroupList.size();
        }

        private class SlotGroupHolder {
            private View base;
            private TextView txtFulfilledBy;
            private TextView txtFulfillmentDisplayName;
            private TextView txtSlotSelected;
            private CheckBox chkIsSlotSelected;

            private SlotGroupHolder(View base) {
                this.base = base;
            }

            public TextView getTxtFulfilledBy() {
                if (txtFulfilledBy == null) {
                    txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);
                    txtFulfilledBy.setTypeface(faceRobotoRegular);
                }
                return txtFulfilledBy;
            }

            public TextView getTxtFulfillmentDisplayName() {
                if (txtFulfillmentDisplayName == null) {
                    txtFulfillmentDisplayName = (TextView) base.findViewById(R.id.txtFulfillmentDisplayName);
                    txtFulfillmentDisplayName.setTypeface(faceRobotoRegular);
                }
                return txtFulfillmentDisplayName;
            }

            public TextView getTxtSlotSelected() {
                if (txtSlotSelected == null) {
                    txtSlotSelected = (TextView) base.findViewById(R.id.txtSlotSelected);
                    txtSlotSelected.setTypeface(faceRobotoRegular);
                }
                return txtSlotSelected;
            }

            public CheckBox getChkIsSlotSelected() {
                if (chkIsSlotSelected == null) {
                    chkIsSlotSelected = (CheckBox) base.findViewById(R.id.chkIsSlotSelected);
                }
                return chkIsSlotSelected;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            SlotGroupHolder slotGroupHolder;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.uiv3_slot_group_card, null);
                slotGroupHolder = new SlotGroupHolder(row);
                row.setTag(slotGroupHolder);
            } else {
                slotGroupHolder = (SlotGroupHolder) row.getTag();
            }
            SlotGroup slotGroup = mSlotGroupList.get(position);
            TextView txtFulfilledBy = slotGroupHolder.getTxtFulfilledBy();
            TextView txtFulfillmentDisplayName = slotGroupHolder.getTxtFulfillmentDisplayName();
            TextView txtSlotSelected = slotGroupHolder.getTxtSlotSelected();

            CheckBox chkIsSlotSelected = slotGroupHolder.getChkIsSlotSelected();
            if (slotGroup.isSelected()) {
                chkIsSlotSelected.setVisibility(View.VISIBLE);
                chkIsSlotSelected.setChecked(true);
                txtSlotSelected.setVisibility(View.VISIBLE);
                txtSlotSelected.setText(slotGroup.getSelectedSlot().getFormattedSlotDate()
                        + ", " + slotGroup.getSelectedSlot().getFormattedDisplayName());
            } else {
                txtSlotSelected.setVisibility(View.GONE);
                chkIsSlotSelected.setVisibility(View.GONE);
            }
            FulfillmentInfo fulfillmentInfo = slotGroup.getFulfillmentInfo();
            txtFulfilledBy.setText(fulfillmentInfo.getFulfilledBy());
            if (TextUtils.isEmpty(fulfillmentInfo.getDisplayName())) {
                txtFulfillmentDisplayName.setVisibility(View.GONE);
            } else {
                txtFulfillmentDisplayName.setVisibility(View.VISIBLE);
                txtFulfillmentDisplayName.setText(Html.fromHtml(fulfillmentInfo.getDisplayName()));
            }
            return row;
        }
    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }


    @Override
    public void setTitle() {
        // Do nothing
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SlotSelectionFragment.class.getName();
    }
}