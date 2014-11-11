package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
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

    public ProductListSpinnerAdapter(BaseActivity ctx, int resource, List<Product> productArrayList,
                                     Typeface typeface, Typeface faceRupee) {
        super(ctx, resource, productArrayList);
        this.ctx = ctx;
        this.productArrayList = productArrayList;
        this.typeface = typeface;
        this.faceRupee = faceRupee;
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
        return getCustomView(position, convertView, true, null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, false, " (More sizes available)");
    }

    private View getInflatedView() {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.product_spinner_row, null);
    }

    private View getCustomView(int position, View convertView, boolean showPrice, @Nullable String additionalTxt) {
        View row = convertView;
        Product product = productArrayList.get(position);
        if (row == null) {
            row = getInflatedView();
        }
        TextView txtProductPkgDesc = (TextView) row.findViewById(R.id.txtProductPkgDesc);
        TextView txtProductSellPrice = (TextView) row.findViewById(R.id.txtProductSellPrice);
        txtProductPkgDesc.setTypeface(typeface);
        txtProductPkgDesc.setText(product.getWeightAndPackDesc() +
                (TextUtils.isEmpty(additionalTxt) ? "" : additionalTxt));
        txtProductPkgDesc.setTextColor(ctx.getResources().getColor(R.color.mrpColor));

        if (showPrice) {
            //row.setPadding(fourDp, fiveDp, tenDp, fiveDp);
            String rupeeSymbol = "`";
            String sellPrice = product.getSellPrice();
            SpannableString spannableString = new SpannableString(rupeeSymbol + sellPrice);
            spannableString.setSpan(new CustomTypefaceSpan("", faceRupee), 0,
                    rupeeSymbol.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new CustomTypefaceSpan("", typeface),
                    rupeeSymbol.length(),
                    spannableString.length(),
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            txtProductSellPrice.setVisibility(View.VISIBLE);
            txtProductSellPrice.setText(spannableString);
            txtProductSellPrice.setTextColor(ctx.getResources().getColor(R.color.mrpColor));
        } else {
            //row.setPadding(0, 0, 0, 0);
            txtProductPkgDesc.setGravity(Gravity.LEFT);
            txtProductSellPrice.setVisibility(View.GONE);
        }
        return row;
    }
}



