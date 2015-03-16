package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.product.Product;

import java.util.List;


public class ProductListSpinnerAdapter extends ArrayAdapter<Product> {
    private BaseActivity ctx;
    List<Product> productArrayList;
    private Typeface typeface;
    private Typeface faceRupee;
    private int eightDp;
    private int fourDp;

    public ProductListSpinnerAdapter(BaseActivity ctx, int resource, List<Product> productArrayList,
                                     Typeface typeface, Typeface faceRupee) {
        super(ctx, resource, productArrayList);
        this.ctx = ctx;
        this.productArrayList = productArrayList;
        this.typeface = typeface;
        this.faceRupee = faceRupee;
        this.eightDp = (int) ctx.getResources().getDimension(R.dimen.padding_small);
        this.fourDp = (int) ctx.getResources().getDimension(R.dimen.padding_mini);
    }

    @Override
    public Product getItem(int position) {
        return productArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Product product = productArrayList.get(position);
        SpinnerDropDownViewHolder spinnerDropDownViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.product_spinner_dropdown_row, parent, false);
            spinnerDropDownViewHolder = new SpinnerDropDownViewHolder(row);
            row.setTag(spinnerDropDownViewHolder);
        } else {
            spinnerDropDownViewHolder = (SpinnerDropDownViewHolder) row.getTag();
        }
        TextView txtProductPkgDesc = spinnerDropDownViewHolder.getTxtProductPkgDesc();
        TextView txtProductSellPrice = spinnerDropDownViewHolder.getTxtProductSellPrice();
        txtProductPkgDesc.setText(product.getWeightAndPackDesc());

        row.setPadding(fourDp, eightDp, eightDp, eightDp);
        String rupeeSymbol = "`";
        String sellPrice = (ctx).getDecimalAmount(Double.parseDouble(product.getSellPrice()))+"";
        SpannableString spannableString = new SpannableString(rupeeSymbol + sellPrice);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), 0,
                rupeeSymbol.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new CustomTypefaceSpan("", typeface),
                rupeeSymbol.length(),
                spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        txtProductSellPrice.setVisibility(View.VISIBLE);
        txtProductSellPrice.setText(spannableString);
        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpinnerViewHolder spinnerViewHolder;
        Product product = productArrayList.get(position);
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.product_spinner_row, parent, false);
            spinnerViewHolder = new SpinnerViewHolder(row);
            row.setTag(spinnerViewHolder);
        } else {
            spinnerViewHolder = (SpinnerViewHolder) row.getTag();
        }
        TextView txtProductPkgDesc = spinnerViewHolder.getTxtProductPkgDesc();
        txtProductPkgDesc.setText(product.getWeightAndPackDesc());
        return row;
    }

    private class SpinnerDropDownViewHolder {
        private TextView txtProductPkgDesc;
        private TextView txtProductSellPrice;
        private View itemView;

        private SpinnerDropDownViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public TextView getTxtProductPkgDesc() {
            if (txtProductPkgDesc == null) {
                txtProductPkgDesc = (TextView) itemView.findViewById(R.id.txtProductPkgDesc);
                txtProductPkgDesc.setTypeface(typeface);
            }
            return txtProductPkgDesc;
        }

        public TextView getTxtProductSellPrice() {
            if (txtProductSellPrice == null) {
                txtProductSellPrice = (TextView) itemView.findViewById(R.id.txtProductSellPrice);
                txtProductSellPrice.setTypeface(typeface);
            }
            return txtProductSellPrice;
        }
    }

    private class SpinnerViewHolder {
        private TextView txtProductPkgDesc;
        private View itemView;

        private SpinnerViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public TextView getTxtProductPkgDesc() {
            if (txtProductPkgDesc == null) {
                txtProductPkgDesc = (TextView) itemView.findViewById(R.id.txtProductPkgDesc);
                txtProductPkgDesc.setTypeface(typeface);
            }
            return txtProductPkgDesc;
        }
    }
}



