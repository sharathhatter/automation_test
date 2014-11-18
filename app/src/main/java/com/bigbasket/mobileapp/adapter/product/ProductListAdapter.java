package com.bigbasket.mobileapp.adapter.product;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.InfiniteListAbstractAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.util.Constants;
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
        if (row == null || row.getTag() == null ||
                !row.getTag().toString().equalsIgnoreCase(Constants.IS_PRODUCT)) {
            row = getInflatedProductView();
            row.setTag(Constants.IS_PRODUCT);
        }
        row = ProductView.getProductView(row, product, baseImgUrl,
                new ProductDetailOnClickListener(product.getSku(), fragment),
                productViewDisplayDataHolder,
                context, false, fragment);
        return row;
    }

    private View getInflatedProductView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.uiv3_product_row, null);
    }

    @Override
    public void nextPage() {
        fragment.loadMoreProducts();
    }
}
