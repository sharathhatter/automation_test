package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.models.response.SpecialityStoresInfoModel;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.handler.click.OnPromoClickListener;
import com.bigbasket.mobileapp.handler.click.OnSpecialityShopIconClickListener;
import com.bigbasket.mobileapp.handler.click.OnSponsoredItemClickListener;
import com.bigbasket.mobileapp.handler.click.ProductDetailOnClickListener;
import com.bigbasket.mobileapp.handler.click.basket.OnProductBasketActionListener;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.service.AnalyticsIntentService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.SectionView;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_DATA = 1;
    public static final int VIEW_TYPE_EMPTY = 2;
    public static final int VIEW_TYPE_PRODUCT_COUNT = 3;
    public static final int VIEW_TYPE_PRODUCT_LOADING_FAILED = 4;
    public static final int DELTA_FOR_NEXT_PAGE_LOAD = 5;
    public static final int VIEW_TYPE_SPONSORED_PRODUCT_DATA = 6;


    private int serverListSize = -1;
    private String baseImgUrl;
    private ProductViewDisplayDataHolder productViewDisplayDataHolder;
    private AppOperationAware activityAware;
    private List<AbstractProductItem> products;
    private String navigationCtx;
    private HashMap<String, Integer> cartInfo;
    private boolean mLoadingFailed;
    private String mTabType;
    private HashMap<String, String> storeAvailabilityMap;
    private HashMap<String, SpecialityStoresInfoModel> specialityStoreInfoHashMap;
    private View.OnClickListener mSpecialityShopClickListener;
    private View.OnClickListener mPromoClickListener;
    private View.OnClickListener productDetailOnClickListener;
    private OnProductBasketActionListener basketIncActionListener;
    private OnProductBasketActionListener basketDecActionListener;
    private int sponsoredItemsSize;

    public ProductListRecyclerAdapter(List<AbstractProductItem> products, String baseImgUrl,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      AppOperationAware activityAware, int productCount, String navigationCtx,
                                      String mTabType) {
        AppDataDynamic appDataDynamic = AppDataDynamic.getInstance(activityAware.getCurrentActivity());
        this.baseImgUrl = baseImgUrl;
        this.productViewDisplayDataHolder = productViewDisplayDataHolder;
        this.activityAware = activityAware;
        this.products = products;
        this.serverListSize = productCount;
        this.navigationCtx = navigationCtx;
        this.mTabType = mTabType;
        this.storeAvailabilityMap = appDataDynamic.getStoreAvailabilityMap();
        this.specialityStoreInfoHashMap = appDataDynamic.getSpecialityStoreDetailList();
        this.mSpecialityShopClickListener = new OnSpecialityShopIconClickListener<>(activityAware, specialityStoreInfoHashMap);
        this.mPromoClickListener = new OnPromoClickListener<>(activityAware);
        this.productDetailOnClickListener = new ProductDetailOnClickListener<>(activityAware);
        this.basketIncActionListener = new OnProductBasketActionListener(BasketOperation.INC, activityAware);
        this.basketDecActionListener = new OnProductBasketActionListener(BasketOperation.DEC, activityAware);
    }

    public ProductListRecyclerAdapter(List<AbstractProductItem> products, String baseImgUrl,
                                      ProductViewDisplayDataHolder productViewDisplayDataHolder,
                                      AppOperationAware activityAware, int productCount, String navigationCtx,
                                      HashMap<String, Integer> cartInfo, String mTabType) {
        this(products, baseImgUrl, productViewDisplayDataHolder, activityAware, productCount,
                navigationCtx, mTabType);
        this.cartInfo = cartInfo;
    }

    public void setCartInfo(HashMap<String, Integer> cartInfo) {
        this.cartInfo = cartInfo;
    }

    public void setSponsoredItemsSize(int sponsoredItemsSize) {
        this.sponsoredItemsSize = sponsoredItemsSize;
    }

    public int getTotalProductsSize() {
        return serverListSize + sponsoredItemsSize;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_PRODUCT_COUNT;
        position = getActualPosition(position);
        if (getTotalProductsSize() > 0 && position >= getTotalProductsSize()) {
            return VIEW_TYPE_EMPTY;
        }
        if (position >= products.size()) {
            return (mLoadingFailed ? VIEW_TYPE_PRODUCT_LOADING_FAILED : VIEW_TYPE_LOADING);
        } else {
            return products.get(position).getType();
        }
    }

    private int getActualPosition(int position) {
        return position - 1;
    }

    public List<AbstractProductItem> getProducts() {
        return products;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activityAware.getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_product_row, viewGroup, false);
                ProductViewHolder productViewHolder = new ProductViewHolder(row);
                productViewHolder.setSpecialityShopIconClickListener(mSpecialityShopClickListener);
                productViewHolder.setPromoClickListener(mPromoClickListener);
                productViewHolder.setProductDetailOnClickListener(productDetailOnClickListener);
                productViewHolder.setBasketIncActionListener(basketIncActionListener);
                productViewHolder.setBasketDecActionListener(basketDecActionListener);
                return productViewHolder;
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
            case VIEW_TYPE_SPONSORED_PRODUCT_DATA:
                row = inflater.inflate(R.layout.sponsored_item_holder, viewGroup, false);
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_DATA) {
            position = getActualPosition(position);
            AbstractProductItem productItem = products.get(position);

            ProductView.setProductView((ProductViewHolder) viewHolder,
                    ((NormalProductItem) productItem).getProduct(), baseImgUrl,
                    productViewDisplayDataHolder,
                    false, activityAware, navigationCtx, cartInfo, mTabType, storeAvailabilityMap,
                    specialityStoreInfoHashMap);

            int positionToCheckForNextPageLoad = position + DELTA_FOR_NEXT_PAGE_LOAD;
            if (getTotalProductsSize() > 0
                    && positionToCheckForNextPageLoad <= getTotalProductsSize()
                    && positionToCheckForNextPageLoad > products.size() && !mLoadingFailed) {
                ((InfiniteProductListAware) activityAware).loadMoreProducts();
            }
        } else if (viewType == VIEW_TYPE_PRODUCT_COUNT) {
            ProductCountViewHolder productCountViewHolder = (ProductCountViewHolder) viewHolder;
            Resources res = activityAware.getCurrentActivity().getResources();
            String productCount = res.getQuantityString(R.plurals.numberOfProductsFound,
                    serverListSize, serverListSize);
            productCountViewHolder.getTxtProductCount().setText(productCount);
        } else if (viewType == VIEW_TYPE_PRODUCT_LOADING_FAILED) {
            ((FixedLayoutViewHolder) viewHolder).getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((InfiniteProductListAware) activityAware).retryNextPage();
                }
            });
        } else if (viewType == VIEW_TYPE_SPONSORED_PRODUCT_DATA) {
            View view = ((FixedLayoutViewHolder) viewHolder).getItemView();
            ((ViewGroup) view).removeAllViews();
            SponsoredProductItem spItem = (SponsoredProductItem) products.get(getActualPosition(position));
            SectionView sectionView = new SectionView(view.getContext(),
                    productViewDisplayDataHolder.getSerifTypeface(),
                    spItem.getSectionData(), navigationCtx);
            LayoutInflater inflater = (LayoutInflater) activityAware.getCurrentActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            OnSponsoredItemClickListener<AppOperationAware> clickListener = null;
            String sectionId = null;
            Map<String, String> analyticsAttrs = null;
            if (spItem.getSection() != null && spItem.getSection().getSectionItems() != null &&
                    !spItem.getSection().getSectionItems().isEmpty()) {
                SectionItem sectionItem = spItem.getSection().getSectionItems().get(0);
                sectionId = sectionItem.getId();
                analyticsAttrs = spItem.getSectionData().getAnalyticsAttrs(sectionId);
                String screenName;
                if (TextUtils.isEmpty(spItem.getSectionData().getScreenName())) {
                    screenName = navigationCtx;
                } else {
                    screenName = spItem.getSectionData().getScreenName();
                }

                clickListener = new OnSponsoredItemClickListener<>(
                        activityAware, spItem.getSection(),
                        sectionItem,
                        screenName,
                        analyticsAttrs);
            }
            View spView = sectionView.getViewToRender(spItem.getSection(), inflater,
                    ((ViewGroup) view), 0, clickListener);
            if (spView != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                ((ViewGroup) view).addView(spView, layoutParams);
                if (!spItem.isSeen()) {
                    spItem.setIsSeen(true);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                            activityAware.getCurrentActivity().getApplicationContext());
                    String analyticsAttrsJsonString = null;
                    if (analyticsAttrs != null && !analyticsAttrs.isEmpty()) {
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                        analyticsAttrsJsonString = gson.toJson(analyticsAttrs);
                    }
                    AnalyticsIntentService.startUpdateAnalyticsEvent(
                            activityAware.getCurrentActivity(),
                            false,
                            sectionId,
                            preferences.getString(Constants.CITY_ID, null),
                            analyticsAttrsJsonString);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return products.size() + 2;  // 1 for infiniteloading view, 1 for product count
    }

    public void setLoadingFailed(boolean loadingFailed) {
        this.mLoadingFailed = loadingFailed;
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
}

