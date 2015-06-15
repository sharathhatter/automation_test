package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.List;


public class ProductListSpinnerAdapter extends BaseAdapter {
    List<Product> productArrayList;
    private Context ctx;
    private Typeface typeface;
    private Typeface faceRupee;
    private int dp16;
    private int dp32;
    private Product currentProduct;
    private int unSelectedTextColor;
    private int selectedTextColor;
    private String baseImgUrl;

    public ProductListSpinnerAdapter(Context ctx, List<Product> productArrayList,
                                     Typeface typeface, Typeface faceRupee,
                                     Product currentProduct, String baseImgUrl) {
        this.ctx = ctx;
        this.productArrayList = productArrayList;
        this.typeface = typeface;
        this.faceRupee = faceRupee;
        this.dp16 = (int) ctx.getResources().getDimension(R.dimen.padding_normal);
        this.dp32 = (int) ctx.getResources().getDimension(R.dimen.padding_large);
        this.currentProduct = currentProduct;
        this.unSelectedTextColor = ctx.getResources().getColor(R.color.uiv3_secondary_text_color);
        this.selectedTextColor = ctx.getResources().getColor(R.color.uiv3_dialog_header_text_bkg);
        this.baseImgUrl = baseImgUrl;
    }

    @Override
    public int getCount() {
        return productArrayList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
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

        row.setPadding(dp32, position == 0 ? dp32 : dp16, dp32,
                position == getCount() - 1 ? dp32 : dp16);

        if (currentProduct.getWeightAndPackDesc().equals(product.getWeightAndPackDesc())) {
            txtProductPkgDesc.setTextColor(selectedTextColor);
        } else {
            txtProductPkgDesc.setTextColor(unSelectedTextColor);
        }
        String rupeeSymbol = "`";
        String sellPrice = UIUtil.formatAsMoney(Double.parseDouble(product.getSellPrice())) + "";
        SpannableString spannableString = new SpannableString(rupeeSymbol + sellPrice);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), 0,
                rupeeSymbol.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new CustomTypefaceSpan("", typeface),
                rupeeSymbol.length(),
                spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        txtProductSellPrice.setVisibility(View.VISIBLE);
        txtProductSellPrice.setText(spannableString);
        UIUtil.preLoadImage(TextUtils.isEmpty(baseImgUrl) ? product.getImageUrl() :
                baseImgUrl + product.getImageUrl(), ctx);
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

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }
}



