package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.QCErrorData;

import java.util.ArrayList;

public class QcListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QCErrorData> qcErrorDatas;
    private Typeface typeface;

    public QcListAdapter(Context context, ArrayList<QCErrorData> qcErrorDatas, Typeface typeface) {
        this.context = context;
        this.qcErrorDatas = qcErrorDatas;
        this.typeface = typeface;
    }

    @Override
    public int getCount() {
        return qcErrorDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return qcErrorDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        QCErrorData qcErrorData = qcErrorDatas.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.uiv3_order_qc_layout,
                    parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TextView txtProductBrand = viewHolder.getTxtProductBrand();
        TextView txtProductDesc = viewHolder.getTxtProductDesc();
        TextView txtQty = viewHolder.getTxtQty();
        TextView txtStockAvailability = viewHolder.getTxtStockAvailability();

        txtProductBrand.setText(qcErrorData.getProduct().getBrand());
        txtProductDesc.setText((position + 1) + ". " + qcErrorData.getProduct().getDescription());
        String qty = qcErrorData.getOriginalQuantity();
        if (!TextUtils.isEmpty(qcErrorData.getProduct().getWeight())) {
            qty += " X " + qcErrorData.getProduct().getWeight();
        }
        txtQty.setText(qty);
        if (Double.parseDouble(qcErrorData.getReservedQuantity()) > 0) {
            txtStockAvailability.setText("Only " + qcErrorData.getReservedQuantity() +
                    " available");
        } else {
            txtStockAvailability.setText(R.string.out_of_stock);
        }
        return convertView;
    }

    private class ViewHolder {
        private View itemView;
        private TextView txtProductDesc;
        private TextView txtProductBrand;
        private TextView txtQty;
        private TextView txtStockAvailability;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public TextView getTxtProductDesc() {
            if (txtProductDesc == null) {
                txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
                txtProductDesc.setTypeface(typeface);
            }
            return txtProductDesc;
        }

        public TextView getTxtProductBrand() {
            if (txtProductBrand == null) {
                txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
                txtProductBrand.setTypeface(typeface);
            }
            return txtProductBrand;
        }

        public TextView getTxtQty() {
            if (txtQty == null) {
                txtQty = (TextView) itemView.findViewById(R.id.txtQty);
                txtQty.setTypeface(typeface);
            }
            return txtQty;
        }

        public TextView getTxtStockAvailability() {
            if (txtStockAvailability == null) {
                txtStockAvailability = (TextView) itemView.findViewById(R.id.txtStockAvailability);
                txtStockAvailability.setTypeface(typeface);
            }
            return txtStockAvailability;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
