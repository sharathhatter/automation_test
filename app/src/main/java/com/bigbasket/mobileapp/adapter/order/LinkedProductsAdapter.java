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
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.shipments.LinkedShipments;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class LinkedProductsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Product> products;
    private Typeface typeface;

    public LinkedProductsAdapter(Context context, LinkedShipments linkedShipments,
                                 Typeface typeface) {
        this.context = context;
        this.products = linkedShipments.getSkuList();
        this.typeface = typeface;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = products.get(position);
        QcListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.uiv3_order_qc_layout, parent, false);
            viewHolder = new QcListAdapter.ViewHolder(convertView, typeface);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (QcListAdapter.ViewHolder) convertView.getTag();
        }
        TextView txtProductBrand = viewHolder.getTxtProductBrand();
        TextView txtProductDesc = viewHolder.getTxtProductDesc();
        TextView txtQty = viewHolder.getTxtQty();
        TextView txtSalePrice = viewHolder.getTxtStockAvailability();
        TextView txtSerialNum = viewHolder.getTxtSerialNum();

        txtProductBrand.setText(product.getBrand());
        txtProductDesc.setText(product.getDescription());
        txtSerialNum.setText((position + 1) + ". ");
        String qty = String.valueOf(product.getNoOfItemsInCart());
        if (!TextUtils.isEmpty(product.getWeight())) {
            qty += " X " + product.getWeight();
        }
        txtQty.setText(qty);

        Double totalItemPrice = Double.parseDouble(product.getSellPrice()) * product.getNoOfItemsInCart();
        txtSalePrice.setText(UIUtil.asRupeeSpannable(totalItemPrice,
                FontHolder.getInstance(context).getFaceRupee()));
        return convertView;
    }
}
