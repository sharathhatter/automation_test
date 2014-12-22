package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.view.uiv2.ProductView;

import java.util.List;

public class ProductListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected BaseActivity context;
    private String baseImgUrl;
    private ProductViewDisplayDataHolder productViewDisplayDataHolder;
    private ActivityAware activityAware;
    private List<Product> products;
    private String sourceName;

    protected int serverListSize = -1;

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_DATA = 1;
    public static final int VIEW_TYPE_EMPTY = 2;

    public ProductListRecyclerAdapter(List<Product> products, String baseImgUrl, BaseActivity context,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      ActivityAware activityAware, int productCount, String sourceName) {
        this.context = context;
        this.baseImgUrl = baseImgUrl;
        this.productViewDisplayDataHolder = productViewDisplayDataHolder;
        this.activityAware = activityAware;
        this.products = products;
        this.serverListSize = productCount;
        this.sourceName = sourceName;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= serverListSize && serverListSize > 0) {
            return VIEW_TYPE_EMPTY;
        }
        return position >= products.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_product_row, viewGroup, false);
                return new ProductViewHolder(row);
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, viewGroup, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(context);
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_DATA) {
            Product product = products.get(position);
            ProductView.setProductView((ProductViewHolder) viewHolder, product, baseImgUrl,
                    new ProductDetailOnClickListener(product.getSku(), activityAware),
                    productViewDisplayDataHolder,
                    false, activityAware, sourceName);
        } else if (getItemViewType(position) == VIEW_TYPE_LOADING) {
            ((ProductListAwareFragment) activityAware).loadMoreProducts();
        }
    }

    @Override
    public int getItemCount() {
        return products.size() + 1;
    }
}

