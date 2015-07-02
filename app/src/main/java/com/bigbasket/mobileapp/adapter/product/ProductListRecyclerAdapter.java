package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.handler.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv2.ProductView;

import java.util.HashMap;
import java.util.List;

public class ProductListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_DATA = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    private static final int VIEW_TYPE_PRODUCT_COUNT = 3;
    private static final int VIEW_TYPE_PRODUCT_LOADING_FAILED = 4;

    private static final int DELTA_FOR_NEXT_PAGE_LOAD = 5;
    private static final int DELTA_FOR_PRELOADING_IMG = 3;

    protected int serverListSize = -1;
    private String baseImgUrl;
    private ProductViewDisplayDataHolder productViewDisplayDataHolder;
    private ActivityAware activityAware;
    private List<Product> products;
    private String navigationCtx;
    private HashMap<String, Integer> cartInfo;
    private boolean mLoadingFailed;

    public ProductListRecyclerAdapter(List<Product> products, String baseImgUrl,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      ActivityAware activityAware, int productCount, String navigationCtx) {
        this.baseImgUrl = baseImgUrl;
        this.productViewDisplayDataHolder = productViewDisplayDataHolder;
        this.activityAware = activityAware;
        this.products = products;
        this.serverListSize = productCount;
        this.navigationCtx = navigationCtx;
    }

    public ProductListRecyclerAdapter(List<Product> products, String baseImgUrl,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      ActivityAware activityAware, int productCount, String navigationCtx,
                                      HashMap<String, Integer> cartInfo) {
        this(products, baseImgUrl, productViewDisplayDataHolder, activityAware, productCount,
                navigationCtx);
        this.cartInfo = cartInfo;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_PRODUCT_COUNT;
        position = getActualPosition(position);
        if (position >= serverListSize && serverListSize > 0) {
            return VIEW_TYPE_EMPTY;
        }
        return position >= products.size() ? (mLoadingFailed ? VIEW_TYPE_PRODUCT_LOADING_FAILED : VIEW_TYPE_LOADING)
                : VIEW_TYPE_DATA;
    }

    private int getActualPosition(int position) {
        return position - 1;
    }

    public List<Product> getProducts() {
        return products;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activityAware.getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_product_row, viewGroup, false);
                return new ProductViewHolder(row);
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, viewGroup, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(activityAware.getCurrentActivity());
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_PRODUCT_COUNT:
                row = inflater.inflate(R.layout.uiv3_product_count_view, viewGroup, false);
                return new ProductCountViewHolder(row);
            case VIEW_TYPE_PRODUCT_LOADING_FAILED:
                row = inflater.inflate(R.layout.uiv3_list_loading_failure, viewGroup, false);
                ((TextView) row.findViewById(R.id.txtProductLoadFailed)).
                        setTypeface(productViewDisplayDataHolder.getSerifTypeface());
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_DATA) {
            position = getActualPosition(position);
            Product product = products.get(position);
            ProductView.setProductView((ProductViewHolder) viewHolder, product, baseImgUrl,
                    new ProductDetailOnClickListener(product.getSku(), activityAware),
                    productViewDisplayDataHolder,
                    false, activityAware, navigationCtx, cartInfo);

            int endPosition = position + DELTA_FOR_PRELOADING_IMG;
            if (endPosition < products.size()) {
                for (int i = position + 1; i <= endPosition; i++) {
                    Product nextProduct = products.get(i);
                    UIUtil.preLoadImage(baseImgUrl != null ? baseImgUrl + nextProduct.getImageUrl() :
                            nextProduct.getImageUrl(), activityAware.getCurrentActivity());
                }
            }
            int positionToCheckForNextPageLoad = position + DELTA_FOR_NEXT_PAGE_LOAD;
            if (positionToCheckForNextPageLoad <= serverListSize && serverListSize > 0 &&
                    positionToCheckForNextPageLoad > products.size() && !mLoadingFailed) {
                ((InfiniteProductListAware) activityAware).loadMoreProducts();
            }
        } else if (viewType == VIEW_TYPE_PRODUCT_COUNT) {
            ProductCountViewHolder productCountViewHolder = (ProductCountViewHolder) viewHolder;
            int stringResId = serverListSize > 1 ? R.string.productFoundPlural : R.string.productFound;
            productCountViewHolder.getTxtProductCount().setText(serverListSize + " " +
                    activityAware.getCurrentActivity().getString(stringResId));
        } else if (viewType == VIEW_TYPE_PRODUCT_LOADING_FAILED) {
            ((FixedLayoutViewHolder) viewHolder).getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((InfiniteProductListAware) activityAware).retryNextPage();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return products.size() + 2;  // 1 for infiniteloading view, 1 for product count
    }

    private class ProductCountViewHolder extends RecyclerView.ViewHolder {
        private TextView txtProductCount;

        public ProductCountViewHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtProductCount() {
            if (txtProductCount == null) {
                txtProductCount = (TextView) itemView.findViewById(R.id.txtProductCount);
                txtProductCount.setTypeface(productViewDisplayDataHolder.getSerifTypeface());
            }
            return txtProductCount;
        }
    }


    public void setLoadingFailed(boolean loadingFailed) {
        this.mLoadingFailed = loadingFailed;
    }
}

