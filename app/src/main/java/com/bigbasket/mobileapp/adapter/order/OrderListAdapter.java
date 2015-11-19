package com.bigbasket.mobileapp.adapter.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.payment.PayNowActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class
        OrderListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_DATA = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    private T context;
    private ArrayList<Order> orders;
    private int totalPages, currentPage, orderListSize;
    private Typeface faceRobotoRegular, faceRobotoBold;

    public OrderListAdapter(T context, ArrayList<Order> orders, int
            totalPages, int currentPage, int orderListSize) {
        this.context = context;
        this.orders = orders;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.orderListSize = orderListSize;
        this.faceRobotoRegular = FontHolder.getInstance(((AppOperationAware) context)
                .getCurrentActivity()).getFaceRobotoRegular();
        this.faceRobotoBold = FontHolder.getInstance(((AppOperationAware) context)
                .getCurrentActivity()).getFaceRobotoBold();
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setOrderList(ArrayList<Order> orders) {//CALL20
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
        if (position >= orders.size() && currentPage == totalPages) return VIEW_TYPE_EMPTY;
        return position == orders.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(((AppOperationAware) context).getCurrentActivity());
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_order_list_row, parent, false);
                return new OrderListRowHolder(row);
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, parent, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(((AppOperationAware) context).getCurrentActivity());
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
            ViewGroup layoutOrderData = rowHolder.getLayoutOrderData();
            Button btnPayNow = rowHolder.getBtnPayNow();
            if (order.getOrderState() == 0) { //active order
                txtSlotTime.setPadding(0, 10, 0, 0);
                txtOrderId.setPadding(0, 0, 0, 0);
                layoutOrderData.setBackgroundResource(R.drawable.red_border);
                imgOrderType.setImageResource(R.drawable.active_order);
                txtSlotTime.setVisibility(View.VISIBLE);
            } else if (order.getOrderState() == 1) { //delivered
                txtOrderId.setPadding(0, 10, 0, 0);
                layoutOrderData.setBackgroundColor(((AppOperationAware) context).getCurrentActivity().getResources().getColor(R.color.uiv3_large_list_item_bck));
                imgOrderType.setImageResource(R.drawable.complete_order);
                txtSlotTime.setVisibility(View.GONE);
            } else { //cancel
                txtOrderId.setPadding(0, 10, 0, 0);
                layoutOrderData.setBackgroundColor(((AppOperationAware) context).getCurrentActivity().getResources().getColor(R.color.uiv3_large_list_item_bck));
                imgOrderType.setImageResource(R.drawable.order_cancel);
                txtSlotTime.setVisibility(View.GONE);
            }

            if (order.canPay()) {
                btnPayNow.setVisibility(View.VISIBLE);
                btnPayNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(((AppOperationAware) context).getCurrentActivity(), PayNowActivity.class);
                        intent.putExtra(Constants.ORDER_ID, order.getOrderId());
                        ((AppOperationAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    }
                });
            } else {
                btnPayNow.setVisibility(View.GONE);
            }

            TextView txtAmount = rowHolder.getTxtAmount();
            String prefix = " `";
            String orderValStr = UIUtil.formatAsMoney(Double.parseDouble(order.getOrderValue()));
            int prefixLen = prefix.length();
            SpannableString spannableMrp = new SpannableString(prefix + orderValStr);
            spannableMrp.setSpan(new CustomTypefaceSpan("", BaseActivity.faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtAmount.setText(spannableMrp);
            if (orderListSize - 1 == position && currentPage < totalPages && totalPages > 1) {
                ((OrderListActivity) context).getMoreOrders(currentPage + 1);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OrderListActivity) ((AppOperationAware) context).getCurrentActivity()).onOrderItemClicked(order);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return orders.size() + 1;
    }


    private class OrderListRowHolder extends RecyclerView.ViewHolder {

        private TextView txtSlotDate;
        private TextView txtSlotTime;
        private TextView txtOrderId;
        private TextView txtNumItems;
        private ViewGroup layoutOrderData;
        private ImageView imgOrderType;
        private TextView txtOrderStatus;
        private Button btnPayNow;
        private TextView txtAmount;


        private OrderListRowHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtOrderStatus() {
            if (txtOrderStatus == null)
                txtOrderStatus = (TextView) itemView.findViewById(R.id.txtOrderStatus);
            return txtOrderStatus;
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

        public Button getBtnPayNow() {
            if (btnPayNow == null)
                btnPayNow = (Button) itemView.findViewById(R.id.btnPayNow);
            return btnPayNow;
        }

        public TextView getTxtAmount() {
            if (txtAmount == null)
                txtAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            return txtAmount;
        }
    }
}
