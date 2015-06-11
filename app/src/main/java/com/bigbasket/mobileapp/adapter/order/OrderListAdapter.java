package com.bigbasket.mobileapp.adapter.order;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.order.Order;

import java.util.ArrayList;

public class OrderListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_DATA = 1;
    public static final int VIEW_TYPE_EMPTY = 2;

    private T context;
    private ArrayList<Order> orders;
    private int totalPages, currentPage;

    public OrderListAdapter(T context, ArrayList<Order> orders, int
            totalPages) {
        this.context = context;
        this.orders = orders;
        this.totalPages = totalPages;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= orders.size() && currentPage == totalPages) return VIEW_TYPE_EMPTY;
        return position == orders.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(((ActivityAware) context).getCurrentActivity());
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_order_list_row, parent, false);
                return new OrderListRowHolder(row);
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, parent, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(((ActivityAware) context).getCurrentActivity());
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_DATA) {
            OrderListRowHolder rowHolder = (OrderListRowHolder) holder;
            final Order order = orders.get(position);

            String[] dateTime = order.getDeliveryDate().split(",");
            TextView txtSlotDate = rowHolder.getTxtSlotDate();
            txtSlotDate.setTypeface(BaseActivity.faceRobotoBold);
            txtSlotDate.setText(dateTime[0].trim() + ", " + dateTime[1].trim());


            TextView txtSlotTime = rowHolder.getTxtSlotTime();
            txtSlotTime.setText(dateTime[2].trim());
            txtSlotTime.setTypeface(BaseActivity.faceRobotoRegular);


            TextView txtOrderId = rowHolder.getTxtOrderId();
            txtOrderId.setTypeface(BaseActivity.faceRobotoRegular);
            txtOrderId.setText(order.getOrderNumber());


            TextView txtNumItems = rowHolder.getTxtNumItems();
            txtNumItems.setTypeface(BaseActivity.faceRobotoRegular);
            int numItems = order.getItemsCount();
            String numItemsStr = numItems + " Item";
            if (numItems > 1) {
                numItemsStr += "s";
            }
            txtNumItems.setText(numItemsStr);


            ImageView imgOrderType = rowHolder.getImgOrderType();
            LinearLayout layoutOrderData = rowHolder.getLayoutOrderData();
            if (order.getOrderState() == 0) { //active order
                txtSlotTime.setPadding(0, 10, 0, 0);
                txtOrderId.setPadding(0, 0, 0, 0);
                layoutOrderData.setBackgroundResource(R.drawable.red_boarder);
                imgOrderType.setImageResource(R.drawable.active_order);
                txtSlotTime.setVisibility(View.VISIBLE);
            } else if (order.getOrderState() == 1) { //delivered
                txtOrderId.setPadding(0, 10, 0, 0);
                layoutOrderData.setBackgroundColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.uiv3_large_list_item_bck));
                imgOrderType.setImageResource(R.drawable.complete_order);
                txtSlotTime.setVisibility(View.GONE);
            } else { //cancel
                txtOrderId.setPadding(0, 10, 0, 0);
                layoutOrderData.setBackgroundColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.uiv3_large_list_item_bck));
                imgOrderType.setImageResource(R.drawable.order_cancel);
                txtSlotTime.setVisibility(View.GONE);
            }

            if (orders.size() - 1 == position && currentPage != totalPages && totalPages != 0) {
                ((OrderListActivity) context).getMoreOrders();
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OrderListActivity) ((ActivityAware) context).getCurrentActivity()).onOrderItemClicked(order);
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
        private LinearLayout layoutOrderData;
        private ImageView imgOrderType;


        private OrderListRowHolder(View itemView) {
            super(itemView);
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

        public LinearLayout getLayoutOrderData() {
            if (layoutOrderData == null)
                layoutOrderData = (LinearLayout) itemView.findViewById(R.id.layoutOrderData);
            return layoutOrderData;
        }

        public ImageView getImgOrderType() {
            if (imgOrderType == null)
                imgOrderType = (ImageView) itemView.findViewById(R.id.imgOrderType);
            return imgOrderType;
        }

    }
}
