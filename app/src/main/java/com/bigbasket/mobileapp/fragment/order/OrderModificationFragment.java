package com.bigbasket.mobileapp.fragment.order;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderModification;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;


public class OrderModificationFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadOrderModifications();
    }

    private void loadOrderModifications() {
        if (getArguments() == null) return;
        OrderInvoice orderInvoice = getArguments().getParcelable(Constants.ACTION_TAB_TAG);
        if (orderInvoice != null && orderInvoice.getOrderModifications() != null && orderInvoice.getOrderModifications().size() > 0) {
            renderOrderModifications(orderInvoice.getOrderModifications());
        }
    }

    private void renderOrderModifications(ArrayList<OrderModification> orderModifications) {
        if (getActivity() == null) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        LayoutInflater inflater = getActivity().getLayoutInflater();

        ListView orderModListView = new ListView(getActivity());
        orderModListView.setDivider(null);
        orderModListView.setDividerHeight(0);
        orderModListView.addHeaderView(inflater.inflate(R.layout.uiv3_order_modification_table_header, orderModListView, false));
        OrderModListAdapter orderModListAdapter = new OrderModListAdapter(orderModifications);
        orderModListView.setAdapter(orderModListAdapter);

        contentView.addView(orderModListView);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderModificationFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_MODIFICATION_SCREEN;
    }

    private class OrderModListAdapter extends BaseAdapter {

        private ArrayList<OrderModification> orderModifications;

        public OrderModListAdapter(ArrayList<OrderModification> orderModifications) {
            this.orderModifications = orderModifications;
        }

        @Override
        public int getCount() {
            return orderModifications.size();
        }

        @Override
        public Object getItem(int position) {
            return orderModifications.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OrderModification orderModification = orderModifications.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.uiv3_order_modification_table_row, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            TextView txtOrderModMsg = viewHolder.getTxtOrderModMsg();
            TextView txtOrderModType = viewHolder.getTxtOrderModType();
            txtOrderModType.setText(orderModification.getType());
            txtOrderModMsg.setText(orderModification.getMessage());
            return convertView;
        }

        private class ViewHolder {
            private View base;
            private TextView txtOrderModType;
            private TextView txtOrderModMsg;

            public ViewHolder(View base) {
                this.base = base;
            }

            public TextView getTxtOrderModType() {
                if (txtOrderModType == null) {
                    txtOrderModType = (TextView) base.findViewById(R.id.txtOrderModType);
                    txtOrderModType.setTypeface(faceRobotoRegular);
                }
                return txtOrderModType;
            }

            public TextView getTxtOrderModMsg() {
                if (txtOrderModMsg == null) {
                    txtOrderModMsg = (TextView) base.findViewById(R.id.txtOrderModMsg);
                    txtOrderModMsg.setTypeface(faceRobotoRegular);
                }
                return txtOrderModMsg;
            }
        }
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "OrderModificationFragment";
    }
}