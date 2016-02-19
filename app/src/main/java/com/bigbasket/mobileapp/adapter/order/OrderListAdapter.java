package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.GetMoreOrderAware;
import com.bigbasket.mobileapp.interfaces.OrderItemClickAware;
import com.bigbasket.mobileapp.interfaces.payment.OnOrderSelectionChanged;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class OrderListAdapter<T extends Context & AppOperationAware> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_DATA = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    private static final int ACTIVE_ORDER = 0;
    private static final int DELIVERED_ORDER = 1;
    private T context;
    private ArrayList<Order> orders;
    private int totalPages, currentPage, orderListSize;
    private Typeface faceRobotoRegular, faceRobotoBold, faceRupee;
    private onPayNowButtonClickListener onPayNowButtonClickListener;
    private onHolderItemClickListener onHolderItemClickListener;
    private boolean isInSelectionMode = false;
    private HashMap<String, Order> selectedItems;

    public OrderListAdapter(T context, ArrayList<Order> orders, int
            totalPages, int currentPage, int orderListSize) {
        this.context = context;
        this.orders = orders;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.orderListSize = orderListSize;
        FontHolder fontHolder = FontHolder.getInstance(context.getApplicationContext());
        this.faceRobotoRegular = fontHolder.getFaceRobotoRegular();
        this.faceRobotoBold = fontHolder.getFaceRobotoBold();
        this.faceRupee = fontHolder.getFaceRupee();
        onPayNowButtonClickListener = new onPayNowButtonClickListener();
        onHolderItemClickListener = new onHolderItemClickListener();
        selectedItems = new HashMap<>(orderListSize);
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setOrderList(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public void setTotalPage(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setOrderListSize(int orderListSize) {
        this.orderListSize = orderListSize;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= orders.size() && currentPage == totalPages)
            return VIEW_TYPE_EMPTY;
        return position == orders.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getCurrentActivity());
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_order_list_row, parent, false);
                return new OrderListRowHolder(row);
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, parent, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(context.getCurrentActivity());
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_DATA) {
            OrderListRowHolder rowHolder = (OrderListRowHolder) holder;
            final Order order = orders.get(position);

            View view = rowHolder.getItemView();
            view.setTag(R.id.order_details, order);
            RelativeLayout layoutOrderHolder = rowHolder.getLayoutDataHolder();
            layoutOrderHolder.setTag(R.id.order_details, order);

            Button btnPayNow = rowHolder.getBtnPayNow();

            if (order.canPay()) {
                view.setTag(R.id.can_pay, true);
                btnPayNow.setVisibility(View.VISIBLE);
                btnPayNow.setTag(R.id.order_details, order);
            } else {
                view.setTag(R.id.can_pay, false);
                btnPayNow.setVisibility(View.GONE);
            }

            String date = order.getSlotDisplay().getDate();
            String time = order.getSlotDisplay().getTime();

            TextView txtSlotDate = rowHolder.getTxtSlotDate();

            if (!TextUtils.isEmpty(date)) {
                txtSlotDate.setText(date);
                txtSlotDate.setTypeface(faceRobotoBold);
                txtSlotDate.setVisibility(View.VISIBLE);
            } else {
                txtSlotDate.setVisibility(View.INVISIBLE);
            }

            TextView txtSlotTime = rowHolder.getTxtSlotTime();
            if (!TextUtils.isEmpty(time)) {
                txtSlotTime.setText(time);
                txtSlotTime.setTypeface(faceRobotoRegular);
                txtSlotTime.setVisibility(View.VISIBLE);
            } else {
                txtSlotTime.setVisibility(View.INVISIBLE);
            }

            TextView txtOrderId = rowHolder.getTxtOrderId();
            txtOrderId.setTypeface(faceRobotoRegular);
            txtOrderId.setText(order.getOrderNumber());

            TextView txtNumItems = rowHolder.getTxtNumItems();
            txtNumItems.setTypeface(faceRobotoRegular);
            int numItems = order.getItemsCount();
            String numItemsStr = numItems + " Item";
            if (numItems > 1) {
                numItemsStr += "s";
            }
            txtNumItems.setText(numItemsStr);

            TextView txtOrderStatus = rowHolder.getTxtOrderStatus();
            txtOrderStatus.setText(order.getOrderStatus());

            ImageView imgOrderType = rowHolder.getImgOrderType();
            imgOrderType.setTag(R.id.order_details, order);
            CheckBox checkBoxPayNow = rowHolder.getCheckBoxPayNow();
            checkBoxPayNow.setTag(R.id.order_details, order);
            checkBoxPayNow.setVisibility(View.GONE);
            ViewGroup layoutOrderData = rowHolder.getLayoutOrderData();
            layoutOrderData.setTag(R.id.order_details, order);
            layoutOrderData.setOnClickListener(onHolderItemClickListener);

            if (!isInSelectionMode) {
                if (order.canPay()) {
                    btnPayNow.setVisibility(View.VISIBLE);
                } else {
                    btnPayNow.setVisibility(View.GONE);
                }
                if (order.getOrderState() == ACTIVE_ORDER) {
                    txtOrderId.setPadding(0, 0, 0, 0);
                    txtSlotTime.setPadding(0, 10, 0, 0);
                    layoutOrderData.setBackgroundResource(R.drawable.red_border);
                    imgOrderType.setImageResource(R.drawable.active_order);
                    imgOrderType.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.VISIBLE);
                } else if (order.getOrderState() == DELIVERED_ORDER) {
                    layoutOrderData.setBackgroundColor(ContextCompat.getColor(context.getCurrentActivity(), R.color.uiv3_large_list_item_bck));
                    imgOrderType.setImageResource(R.drawable.complete_order);
                    imgOrderType.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.GONE);
                } else { //cancel
                    txtOrderId.setPadding(0, 10, 0, 0);
                    layoutOrderData.setBackgroundColor(ContextCompat.getColor(context.getCurrentActivity(), R.color.uiv3_large_list_item_bck));
                    imgOrderType.setImageResource(R.drawable.order_cancel);
                    imgOrderType.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.GONE);
                }
            } else {
                btnPayNow.setVisibility(View.GONE);
                if (selectedItems.containsKey(order.getOrderId())) { // selected
                    txtSlotTime.setPadding(0, 10, 0, 0);
                    txtOrderId.setPadding(0, 0, 0, 0);
                    txtSlotTime.setVisibility(View.VISIBLE);
                    layoutOrderData.setBackgroundResource(R.drawable.red_border);
                    imgOrderType.setVisibility(View.VISIBLE);
                    imgOrderType.setImageResource(R.drawable.active_order);
                    layoutOrderData.setOnClickListener(onPayNowButtonClickListener);
                    checkBoxPayNow.setVisibility(View.VISIBLE);
                    checkBoxPayNow.setChecked(true);
                } else if (order.canPay()) { // can pay
                    txtSlotTime.setPadding(0, 10, 0, 0);
                    txtOrderId.setPadding(0, 0, 0, 0);
                    layoutOrderData.setBackgroundResource(R.drawable.red_border);
                    layoutOrderData.setOnClickListener(onPayNowButtonClickListener);
                    imgOrderType.setImageResource(R.drawable.active_order);
                    imgOrderType.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.VISIBLE);
                    // change icon to unchecked checkbox
                    checkBoxPayNow.setVisibility(View.VISIBLE);
                    checkBoxPayNow.setChecked(false);
                } else { //canceled or delivered
                    txtOrderId.setPadding(0, 10, 0, 0);
                    layoutOrderData.setBackgroundColor(ContextCompat.getColor(context.getCurrentActivity(), R.color.uiv3_large_list_item_bck));
                    layoutOrderData.setOnClickListener(null);
                    txtSlotTime.setVisibility(View.GONE);
                    imgOrderType.setVisibility(View.INVISIBLE);
                    checkBoxPayNow.setVisibility(View.GONE);
                }
            }

            TextView txtAmount = rowHolder.getTxtAmount();
            String prefix = "`";
            String orderValStr = UIUtil.formatAsMoney(Double.parseDouble(order.getOrderValue()));
            int prefixLen = prefix.length();
            SpannableString spannableMrp = new SpannableString(prefix + orderValStr);
            spannableMrp.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtAmount.setText(spannableMrp);
            if (orderListSize - 1 == position && currentPage < totalPages && totalPages > 1) {
                ((GetMoreOrderAware) context).getMoreOrders(currentPage + 1);
            }

            btnPayNow.setOnClickListener(onPayNowButtonClickListener);
            if (isInSelectionMode) {
                checkBoxPayNow.setOnClickListener(onPayNowButtonClickListener);
            } else {
                checkBoxPayNow.setOnClickListener(null);
            }
            holder.itemView.setOnClickListener(onHolderItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void clearSelection() {
        selectedItems.clear();
        isInSelectionMode = false;
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    public void toggleSelection(Order selectedOrder, View view) {
        if (selectedItems.containsKey(selectedOrder.getOrderId())) {
            selectedItems.remove(selectedOrder);
            if (selectedItems.size() == 0) {
                notifyDataSetChanged();
                isInSelectionMode = false;
            }
            if (view != null) {
                CheckBox checkBoxPayNow;
                ViewGroup layoutOrderData;
                if (view instanceof CheckBox) {
                    checkBoxPayNow = (CheckBox) view;
                    ViewGroup group = (ViewGroup) view.getParent();
                    layoutOrderData = (ViewGroup) group.findViewById(R.id.layoutOrderData);
                } else {
                    checkBoxPayNow = (CheckBox) view.findViewById(R.id.checkboxPaynow);
                    layoutOrderData = (ViewGroup) view.findViewById(R.id.layoutOrderData);
                }
                if (layoutOrderData != null)
                    layoutOrderData.setBackgroundResource(R.drawable.red_border);
                if (checkBoxPayNow != null) {
                    checkBoxPayNow.setVisibility(View.VISIBLE);
                    checkBoxPayNow.setChecked(false); // change icon unchecked checkbox
                }
            }
        } else {
            selectedItems.put(selectedOrder.getOrderId(), selectedOrder);
            if (view != null) { // change the icon to select mode
                ViewGroup group = (ViewGroup) view.getParent();
                CheckBox checkBoxPayNow = (CheckBox) group.findViewById(R.id.checkboxPaynow);
                // change icon to checked checkbox
                if (checkBoxPayNow != null) {
                    checkBoxPayNow.setVisibility(View.VISIBLE);
                    checkBoxPayNow.setChecked(true);
                }
            }
        }
        ((OnOrderSelectionChanged) context).onOrderSelectionChanged(getSelectedItemCount(), calculatePrices(getSelectedItems()));
    }

    private double calculatePrices(Collection<Order> orders) {
        double total = 0.0;
        if (orders != null && orders.size() > 0) {
            for (Order mOrder : orders) {
                total = total + Double.parseDouble(mOrder.getOrderValue());
            }
        }
        return total;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public Collection<Order> getSelectedItems() {
        return selectedItems.values();
    }

    public Collection<String> getSelectedOrderIds() {
        return selectedItems.keySet();
    }

    private class onPayNowButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            isInSelectionMode = true;
            Order mOrder = (Order) v.getTag(R.id.order_details);
            if (v instanceof Button) {
                notifyDataSetChanged();
            }
            toggleSelection(mOrder, v);
        }
    }

    private class onHolderItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Order order = (Order) v.getTag(R.id.order_details);
            if (isInSelectionMode) {
                boolean canPay = (boolean) v.getTag(R.id.can_pay);
                if (canPay) {
                    toggleSelection(order, v);
                }
            } else {
                ((OrderItemClickAware) context).onOrderItemClicked(order);
            }
        }
    }

    private class OrderListRowHolder extends RecyclerView.ViewHolder {

        private TextView txtSlotDate;
        private TextView txtSlotTime;
        private TextView txtOrderId;
        private TextView txtNumItems;
        private ViewGroup layoutOrderData;
        private ImageView imgOrderType;
        private TextView txtOrderStatus;
        private TextView txtAmount;
        private RelativeLayout layoutOrderHolder;
        private Button btnPayNow;
        private CheckBox checkBoxPayNow;

        private OrderListRowHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtOrderStatus() {
            if (txtOrderStatus == null)
                txtOrderStatus = (TextView) itemView.findViewById(R.id.txtOrderStatus);
            return txtOrderStatus;
        }

        public Button getBtnPayNow() {
            if (btnPayNow == null)
                btnPayNow = (Button) itemView.findViewById(R.id.btnPayNow);
            return btnPayNow;
        }

        public RelativeLayout getLayoutDataHolder() {
            if (layoutOrderHolder == null)
                layoutOrderHolder = (RelativeLayout) itemView.findViewById(R.id.layoutOrderHolder);
            return layoutOrderHolder;
        }

        public View getItemView() {
            return this.itemView;
        }

        public TextView getTxtSlotDate() {
            if (txtSlotDate == null) {
                txtSlotDate = (TextView) itemView.findViewById(R.id.txtSlotDate);
            }
            return txtSlotDate;
        }

        public TextView getTxtSlotTime() {
            if (txtSlotTime == null) {
                txtSlotTime = (TextView) itemView.findViewById(R.id.txtSlotTime);
            }
            return txtSlotTime;
        }

        public TextView getTxtOrderId() {
            if (txtOrderId == null) {
                txtOrderId = (TextView) itemView.findViewById(R.id.txtOrderId);
            }
            return txtOrderId;
        }

        public TextView getTxtNumItems() {
            if (txtNumItems == null) {
                txtNumItems = (TextView) itemView.findViewById(R.id.txtNumItems);
            }
            return txtNumItems;
        }

        public ViewGroup getLayoutOrderData() {
            if (layoutOrderData == null)
                layoutOrderData = (ViewGroup) itemView.findViewById(R.id.layoutOrderData);
            return layoutOrderData;
        }

        public ImageView getImgOrderType() {
            if (imgOrderType == null)
                imgOrderType = (ImageView) itemView.findViewById(R.id.imgOrderType);
            return imgOrderType;
        }

        public TextView getTxtAmount() {
            if (txtAmount == null)
                txtAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            return txtAmount;
        }

        public CheckBox getCheckBoxPayNow() {
            if (checkBoxPayNow == null)
                checkBoxPayNow = (CheckBox) itemView.findViewById(R.id.checkboxPaynow);
            return checkBoxPayNow;
        }
    }
}
