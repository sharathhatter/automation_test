package com.bigbasket.mobileapp.adapter.shipment;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.shipments.Slot;

import java.util.List;

public class SlotListAdapter<T> extends BaseAdapter {
    private List<Object> flattenedSlotDateTimeList;
    private T ctx;
    private Typeface typeface;
    private Slot selectedSlot;

    private int size;
    private int VIEW_TYPE_HEADER = 0;
    private int VIEW_TYPE_ITEM = 1;

    public SlotListAdapter(T ctx, List<Object> flattenedSlotDateTimeList, Typeface typeface,
                           Slot selectedSlot) {
        this.flattenedSlotDateTimeList = flattenedSlotDateTimeList;
        this.size = flattenedSlotDateTimeList.size();
        this.ctx = ctx;
        this.typeface = typeface;
        this.selectedSlot = selectedSlot;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (flattenedSlotDateTimeList.get(position) instanceof String) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return flattenedSlotDateTimeList.get(position);
    }

    @Override
    public int getCount() {
        return this.size;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object possibleSlot = flattenedSlotDateTimeList.get(position);
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_ITEM) {
            View slotView = getSlotView((Slot) possibleSlot, convertView, parent);
            View listSeparator = slotView.findViewById(R.id.listSeparator);
            if ((position + 1) < size && getItemViewType(position + 1) == VIEW_TYPE_HEADER) {
                listSeparator.setVisibility(View.VISIBLE);
            } else {
                listSeparator.setVisibility(View.GONE);
            }
            return slotView;
        }
        return getHeaderView((String) possibleSlot, convertView, parent);
    }

    @SuppressWarnings("unchecked")
    private View getHeaderView(String slotHeader, View row, ViewGroup parent) {
        SlotHeaderViewHolder slotHeaderViewHolder;
        BaseActivity activity = ((AppOperationAware) ctx).getCurrentActivity();
        if (row == null) {
            row = activity.getLayoutInflater().inflate(R.layout.uiv3_list_title, parent, false);
            slotHeaderViewHolder = new SlotHeaderViewHolder(row);
            row.setBackgroundColor(activity.getResources().getColor(R.color.uiv3_list_bkg_light_color));
            row.setTag(slotHeaderViewHolder);
        } else {
            slotHeaderViewHolder = (SlotHeaderViewHolder) row.getTag();
        }
        TextView txtHeaderMsg = slotHeaderViewHolder.getTxtHeaderMsg();
        txtHeaderMsg.setText(slotHeader);
        return row;
    }

    @SuppressWarnings("unchecked")
    private View getSlotView(Slot slot, View row, ViewGroup parent) {
        SlotViewHolder slotViewHolder;
        BaseActivity activity = ((AppOperationAware) ctx).getCurrentActivity();
        if (row == null) {
            int layoutId = R.layout.uiv3_slot_list_row;
            row = activity.getLayoutInflater().inflate(layoutId, parent, false);
            row.setBackgroundColor(activity.getResources().getColor(R.color.uiv3_list_bkg_light_color));
            slotViewHolder = new SlotViewHolder(row);
            row.setTag(slotViewHolder);
        } else {
            slotViewHolder = (SlotViewHolder) row.getTag();
        }
        TextView txtTitle = slotViewHolder.getTxtTitle();
        ImageView imgRow = slotViewHolder.getImgRow();

        imgRow.setImageResource(R.drawable.ic_access_time_grey600_24dp);

        if (!slot.isAvailable()) {
            SpannableString slotDateUnderlineSpannable = new SpannableString(slot.getSlotDisplay().getTime());
            slotDateUnderlineSpannable.setSpan(new StrikethroughSpan(), 0, slotDateUnderlineSpannable.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            txtTitle.setText(slotDateUnderlineSpannable);
        } else {
            txtTitle.setText(slot.getSlotDisplay().getTime());
        }

        CheckBox itemTick = slotViewHolder.getItemTick();
        if (slot.getSlotId().equals(selectedSlot.getSlotId())
                && slot.getSlotDate().equals(selectedSlot.getSlotDate())) {
            itemTick.setChecked(true);
            itemTick.setVisibility(View.VISIBLE);
            row.setBackgroundColor(activity.getResources().getColor(R.color.uiv3_list_bkg_color));
        } else {
            itemTick.setChecked(false);
            itemTick.setVisibility(View.INVISIBLE);
            row.setBackgroundColor(activity.getResources().getColor(R.color.uiv3_list_bkg_light_color));
        }
        return row;
    }

    private class SlotViewHolder {
        private TextView txtTitle;
        private ImageView imgRow;
        private CheckBox itemTick;
        private View base;

        private SlotViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtTitle() {
            if (txtTitle == null) {
                txtTitle = (TextView) base.findViewById(R.id.itemTitle);
                txtTitle.setTypeface(typeface);
            }
            return txtTitle;
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

        public SlotHeaderViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtHeaderMsg() {
            if (txtHeaderMsg == null) {
                txtHeaderMsg = (TextView) base.findViewById(R.id.txtHeaderMsg);
                txtHeaderMsg.setTypeface(typeface);
            }
            return txtHeaderMsg;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return flattenedSlotDateTimeList.get(position) instanceof Slot &&
                ((Slot) flattenedSlotDateTimeList.get(position)).isAvailable();
    }
}
