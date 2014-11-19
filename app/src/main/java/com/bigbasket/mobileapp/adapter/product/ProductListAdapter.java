package com.bigbasket.mobileapp.adapter.product;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.InfiniteListAbstractAdapter;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.view.uiv2.ProductView;

import java.util.List;

public class ProductListAdapter extends InfiniteListAbstractAdapter<Product> {
    protected BaseActivity context;
    private String baseImgUrl;
    private ProductViewDisplayDataHolder productViewDisplayDataHolder;
    private ProductListAwareFragment fragment;

    public ProductListAdapter(List<Product> products, String baseImgUrl, BaseActivity context,
                              ProductViewDisplayDataHolder productViewDisplayDataHolder,
                              ProductListAwareFragment fragment, int totalItems) {
        super(context, products, totalItems);
        this.context = context;
        this.productViewDisplayDataHolder = productViewDisplayDataHolder;
        this.baseImgUrl = baseImgUrl;
        this.fragment = fragment;
    }

    @Override
    public View getDataView(int position, View convertView, ViewGroup parent) {
        final Product product = dataList.get(position);
        View row = convertView;
        ProductViewHolder productViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_product_row, null);
            productViewHolder = new ProductViewHolder(row);
            row.setTag(productViewHolder);
        } else {
            productViewHolder = (ProductViewHolder) row.getTag();
        }
        ProductView.setProductView(productViewHolder, product, baseImgUrl,
                new ProductDetailOnClickListener(product.getSku(), fragment),
                productViewDisplayDataHolder,
                context, false, fragment);
        return row;
    }

    @Override
    public void nextPage() {
        fragment.loadMoreProducts();
    }
}
