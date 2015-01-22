package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class OrderListAdapter extends BaseAdapter {

    private Context context;
    private Typeface typeface;
    private Typeface faceRupee;
    private ArrayList<Order> orders;
    private boolean showFulfillmentInfo;
    private boolean isInShopFromPreviousOrderMode;

    public OrderListAdapter(Context context, Typeface typeface, Typeface faceRupee,
                            ArrayList<Order> orders, boolean showFulfillmentInfo,
                            boolean isInShopFromPreviousOrderMode) {
        this.context = context;
        this.typeface = typeface;
        this.orders = orders;
        this.faceRupee = faceRupee;
        this.showFulfillmentInfo = showFulfillmentInfo;
        this.isInShopFromPreviousOrderMode = isInShopFromPreviousOrderMode;
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderListRowHolder rowHolder;
        if (convertView == null) {
            int layoutId = isInShopFromPreviousOrderMode ? R.layout.uiv3_shop_from_order_list_row : R.layout.uiv3_order_list_row;
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            rowHolder = new OrderListRowHolder(convertView);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (OrderListRowHolder) convertView.getTag();
        }

        TextView txtOrderId = rowHolder.getTxtOrderId();
        TextView txtSlotDate = rowHolder.getTxtSlotDate();
        TextView txtNumItems = rowHolder.getTxtNumItems();

        Order order = orders.get(position);
        String deliveryDate = order.getDeliveryDate().replace("between", "");
        if (!isInShopFromPreviousOrderMode) {
            TextView txtDisplayName = rowHolder.getTxtDisplayName();
            TextView txtFulfilledBy = rowHolder.getTxtFulfilledBy();
            TextView txtSlotTime = rowHolder.getTxtSlotTime();
            TextView txtOrderStatus = rowHolder.getTxtOrderStatus();
            TextView txtOrderTotal = rowHolder.getTxtOrderTotal();
            if (showFulfillmentInfo) {
                txtDisplayName.setText(order.getFulfillmentInfo().getDisplayName());
                txtFulfilledBy.setText(order.getFulfillmentInfo().getFulfilledBy());
            } else {
                txtDisplayName.setVisibility(View.GONE);
                txtFulfilledBy.setVisibility(View.GONE);
                txtSlotDate.setPadding(0, 6, 0, 0);
            }
            txtSlotTime.setText(deliveryDate.substring(15, deliveryDate.length()).trim());
            txtOrderTotal.setText(UIUtil.asRupeeSpannable(order.getOrderValue(), faceRupee));
            String orderStatus = order.getOrderStatus();
            txtOrderStatus.setText(orderStatus);
            if (orderStatus.equalsIgnoreCase(Constants.CANCELLED)) {
                txtOrderStatus.setBackgroundColor(context.getResources().getColor(R.color.dark_red));
            } else {
                txtOrderStatus.setBackgroundColor(context.getResources().getColor(R.color.dark_green));
            }
        }

        txtSlotDate.setText(deliveryDate.substring(0, 15).trim());
        txtOrderId.setText(order.getOrderNumber());

        int numItems = order.getItemsCount();
        String numItemsStr = numItems + " Item";
        if (numItems > 1) {
            numItemsStr += "s";
        }
        txtNumItems.setText(numItemsStr);
        return convertView;
    }

    private class OrderListRowHolder {
        private View base;

        private TextView txtSlotDate;
        private TextView txtSlotTime;
        private TextView txtOrderStatus;
        private TextView txtOrderId;
        private TextView txtNumItems;
        private TextView txtOrderTotal;
        private TextView txtDisplayName;
        private TextView txtFulfilledBy;

        private OrderListRowHolder(View base) {
            this.base = base;
        }

        public TextView getTxtSlotDate() {
            if (txtSlotDate == null) {
                txtSlotDate = (TextView) base.findViewById(R.id.txtSlotDate);
                txtSlotDate.setTypeface(typeface);
            }
            return txtSlotDate;
        }

        public TextView getTxtSlotTime() {
            if (txtSlotTime == null) {
                txtSlotTime = (TextView) base.findViewById(R.id.txtSlotTime);
                txtSlotTime.setTypeface(typeface);
            }
            return txtSlotTime;
        }

        public TextView getTxtOrderStatus() {
            if (txtOrderStatus == null) {
                txtOrderStatus = (TextView) base.findViewById(R.id.txtOrderStatus);
                txtOrderStatus.setTypeface(typeface);
            }
            return txtOrderStatus;
        }

        public TextView getTxtOrderId() {
            if (txtOrderId == null) {
                txtOrderId = (TextView) base.findViewById(R.id.txtOrderId);
                txtOrderId.setTypeface(typeface);
            }
            return txtOrderId;
        }

        public TextView getTxtNumItems() {
            if (txtNumItems == null) {
                txtNumItems = (TextView) base.findViewById(R.id.txtNumItems);
                txtNumItems.setTypeface(typeface);
            }
            return txtNumItems;
        }

        public TextView getTxtOrderTotal() {
            if (txtOrderTotal == null) {
                txtOrderTotal = (TextView) base.findViewById(R.id.txtOrderTotal);
            }
            return txtOrderTotal;
        }

        public TextView getTxtDisplayName() {
            if (txtDisplayName == null) {
                txtDisplayName = (TextView) base.findViewById(R.id.txtDisplayName);
            }
            return txtDisplayName;
        }

        public TextView getTxtFulfilledBy() {
            if (txtFulfilledBy == null) {
                txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);
            }
            return txtFulfilledBy;
        }
    }
}
